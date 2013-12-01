package cmpt370.fbms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Handles all the changes to the files that the watcher detects by taking appropriate actions.
 */
public class FileChangeHandlers
{
	// Logger instance
	private static Logger logger = Logger.getLogger(Main.class);

	private Set<Path> createdFiles;
	private Set<Path> modifiedFiles;
	private Set<RenamedFile> renamedFiles;
	private Set<Path> deletedFiles;

	private static final int sMBtoBytes = 1024 * 1024;

	/**
	 * Creates a file change listener for the supplied lists.
	 * 
	 * @param createdFiles
	 *            List of created files.
	 * @param modifiedFiles
	 *            List of modified files.
	 * @param renamedFiles
	 *            List of renamed files.
	 * @param deletedFiles
	 *            List of deleted files.
	 */
	public FileChangeHandlers(Set<Path> createdFiles, Set<Path> modifiedFiles,
			Set<RenamedFile> renamedFiles, Set<Path> deletedFiles)
	{
		this.createdFiles = createdFiles;
		this.modifiedFiles = modifiedFiles;
		this.renamedFiles = renamedFiles;
		this.deletedFiles = deletedFiles;
	}

	/**
	 * Removes entries from lists where files do not exist anymore.
	 */
	public void validateLists()
	{
		// Remove non-existant creations
		Iterator<Path> createdIterator = createdFiles.iterator();
		while(createdIterator.hasNext())
		{
			if(!createdIterator.next().toFile().exists())
			{
				createdIterator.remove();
			}
		}

		// Remove non-existant modifications
		Iterator<Path> modifiedIterator = modifiedFiles.iterator();
		while(modifiedIterator.hasNext())
		{
			if(!modifiedIterator.next().toFile().exists())
			{
				modifiedIterator.remove();
			}
		}

		// Remove non-existant renames (we only care about the new name)
		Iterator<RenamedFile> renamedIterator = renamedFiles.iterator();
		while(renamedIterator.hasNext())
		{
			if(!renamedIterator.next().newName.toFile().exists())
			{
				renamedIterator.remove();
			}
		}

		// Remove non-existant deletions
		Iterator<Path> deletedIterator = deletedFiles.iterator();
		while(deletedIterator.hasNext())
		{
			if(!deletedIterator.next().toFile().exists())
			{
				deletedIterator.remove();
			}
		}
	}

	/**
	 * A helper method for the handlers. Create a patch for a file. If binary, leave diff field
	 * null. If text, leave binary field null.
	 * 
	 * @param oldFile
	 *            The original path to the file to compare against.
	 * @param newFile
	 *            The path to the new file to create the patch for.
	 * @param diff
	 *            The diff of the two file versions.
	 * 
	 * @param binary
	 *            Byte array for binary patches.
	 * 
	 */
	private void createPatchEntry(Path oldName, Path newName, Path diff, byte[] binary)
	{
		long delta = FileOp.fileSize(newName) - FileOp.fileSize(FileOp.convertPath(oldName));
		FileHistory fileHist = new FileHistory(newName);
		fileHist.storeRevision(diff, binary, FileOp.fileSize(newName), delta);
	}

	/**
	 * A helper method for the handlers. Create a patch for a binary file.
	 * 
	 * @param oldName
	 *            The original path to the file to compare against.
	 * @param newName
	 *            The path to the new file to create the patch for.
	 * @param errorMsg
	 *            Associated error message to prepend to the file name.
	 */
	private void copyAndRename(Path oldName, Path newName, String errorMsg)
	{
		String newNameCalculated = oldName.getParent().relativize(newName).toString();

		Path targetDirectory = FileOp.convertPath(newName).getParent();
		FileOp.copy(newName, targetDirectory);

		FileHistory fileHist = new FileHistory(oldName);
		fileHist.renameRevision(newNameCalculated);

		logger.debug(errorMsg + " " + newName.toFile().toString());
	}

	/**
	 * Simply copies a file from the live location to the corresponding backup location.
	 * 
	 * @param file
	 *            Location of the file in live.
	 * @param errorMsg
	 *            Associated error message to prepend to the file name.
	 */
	private void copyNewFile(Path file, String errorMsg)
	{
		Path targetDirectory = FileOp.convertPath(file.getParent());
		FileOp.copy(file, targetDirectory);

		logger.debug(errorMsg + " " + file.toFile().toString());
	}


	/**
	 * Handles all created files identified by the watcher. Also handles recurrent entries between
	 * modified, and renamed files. All iterations start from the back of the list, that way we
	 * handle the most recent events first. This is more important for file renames but I kept it
	 * consistent.
	 */
	public void handleCreatedFiles()
	{
		Iterator<Path> itrc, itrm;
		Iterator<RenamedFile> itrr;
		boolean hit, found;
		Path pathc, pathm;
		RenamedFile toRename;
		itrc = createdFiles.iterator();
		found = false;
		logger.debug("Handle Created Files has started.");

		// Search through all elements of created files, comparing them to instances of renamed
		// files and modified files.
		while(itrc.hasNext())
		{
			pathc = itrc.next();
			hit = false; // If we've hit a duplicate already in this list.
			found = false;
			itrr = renamedFiles.iterator();
			// Check renamed files for created files duplicates.
			while(itrr.hasNext())
			{
				toRename = itrr.next();
				if(pathc.toFile().equals(toRename.newName.toFile())
						|| pathc.toFile().equals(toRename.oldName.toFile()))
				{
					// If we've hit a duplicate already, we've made the diff/backup and we can
					// delete this one safely.
					if(hit)
					{
						// itrr.remove();
						logger.debug("Create Handle: Found duplicate in renamed, removing.");
					}
					// Otherwise we set found and hit to be true. It will be then removed from
					// created and left on renamed to be handled there.
					else
					{
						logger.debug("Create Handle: Found same entry in renamed, leaving and deleting in created.");
						hit = true;
						found = true;
					}
				}
			}

			hit = false;
			itrm = modifiedFiles.iterator();
			// Now we cycle through the modified list.
			while(itrm.hasNext())
			{
				pathm = itrm.next();
				if(pathc.toFile().equals(pathm.toFile()))
				{
					// If we find a duplicate but already have made a diff/backup just delete the
					// duplicate.
					if(hit || found)
					{
						itrm.remove();
						logger.debug("Create File Handle: Found duplicate in modified, removing:"
								+ pathm.toFile().toString());
					}
					// Otherwise we make backups/entries and set hit/found to true.
					else
					{
						logger.debug("Create Handle: Found same entry in modified, leaving and deleting in created.");
						hit = true;
						found = true;
					}
				}
			}
			// If after going through both modified and renamed lists we didn't find a duplicate,
			// make a diff/backup and remove it from the created list.
			if(!found)
			{
				logger.info("Create Handle: file " + pathc.toFile().toString()
						+ " was not found in any other list.");
				// If the file doesn't exist we copy it over.
				if(!FileOp.isFolder(pathc) && !FileOp.convertPath(pathc).toFile().exists())
				{
					// If the file isn't a folder and is not in the backup folder, copy it over.
					copyNewFile(pathc, "Create Handle: Found new file");
				}
				else if(FileOp.isFolder(pathc) && !FileOp.convertPath(pathc).toFile().exists())
				{
					FileOp.copy(pathc, FileOp.convertPath(pathc));
				}
				// If it does exist, it was modified after creation and that takes priority.
				else
				{
					modifiedFiles.add(pathc);
					logger.debug("Create Handle: Move " + pathc.toFile().toString()
							+ "to modified, it exists.");
				}
			}
			itrc.remove();
		}
	}

	/**
	 * Checks for modified files using the list created by watcher. Compares its entries against
	 * those that have been renamed. Renamed entries take priority, removes duplicate entry in its
	 * own list. Creates new copies or diffs of files when necessary.
	 */
	public void handleModifiedFiles()
	{
		Iterator<Path> itrm = modifiedFiles.iterator();
		Iterator<RenamedFile> itrr;

		Path pathm, diff;
		RenamedFile toRename;
		boolean hit = false;

		// Figure out the maximum size that gets revisioned (note the config is in MB, so is
		// multiplied by 1024^2 to get bytes
		// If we could not get it from DB, we use 5 MB.
		DbConnection db = DbConnection.getInstance();
		long maxSizeInBytes;
		try
		{
			maxSizeInBytes = (long) Math.round(Float.parseFloat(db.getConfig("maxSize"))
					* sMBtoBytes);
		}
		catch(Exception e)
		{
			maxSizeInBytes = 5 * sMBtoBytes;
		}

		logger.debug("Handle Modified Files has started.");

		while(itrm.hasNext())
		{
			pathm = itrm.next();
			itrr = renamedFiles.iterator();
			hit = false;
			while(itrr.hasNext())
			{
				toRename = itrr.next();
				if(pathm.equals(toRename.newName) || pathm.equals(toRename.oldName))
				{
					// Clean up additional copies, will only do this if its already made a
					// diff/backup of the file.
					if(hit)
					{
						// itrr.remove();
					}
					else
					{
						hit = true;
					}
				}
			}
			if(!hit)
			{
				if(pathm.toFile().isFile() && FileOp.isPlainText(pathm))
				{
					if(!FileOp.convertPath(pathm).toFile().exists())
					{
						// If the file isn't a folder and is not in the backup folder, copy it over.
						copyNewFile(pathm, "Create File Handle: Found new file");
					}
					else
					{
						// Following the conventions in startupScan...
						// If the file isn't a folder but DOES exist, make diff and copy.
						// Make diff file.
						diff = FileOp.createPatch(FileOp.convertPath(pathm), pathm);

						if(diff.toFile().length() == 0)
						{
							logger.info("File delta created in handleModified was 0 in size: "
									+ pathm.toFile().toString() + " -- revision not made.");
						}
						else
						{
							// Store a revision
							createPatchEntry(pathm, pathm, diff, null);

							// Copy file over.
							copyNewFile(pathm, "Handle File Modified: Found existing modified file");

						}
					}
				}
				// It's a binary file, so revisions are handled differently
				else if(pathm.toFile().isFile() && FileOp.fileSize(pathm) < maxSizeInBytes
						&& FileOp.convertPath(pathm).toFile().exists())
				{
					// No diffs, just store the revision
					try
					{
						createPatchEntry(pathm, pathm, null, Files.readAllBytes(pathm));
					}
					catch(IOException e)
					{
						Errors.nonfatalError("Failed to revision " + pathm.toString(), e);
					}

					// Copy it over
					copyNewFile(pathm, "Create File Handle: Found binary file ");

				}
				// It doesn't exist in the backup directory, so just copy
				{
					copyNewFile(pathm, "Create File Handle: Found new unrevisioned or large file");
				}
			}
			itrm.remove();
		}
	}

	public void handleRenamedFiles()
	{
		Iterator<RenamedFile> itrr = renamedFiles.iterator();

		RenamedFile toRename;
		Path diff;

		// Figure out the maximum size that gets revisioned (note the config is in MB, so is
		// multiplied by 1024^2 to get bytes
		// If we could not retrieve it from DB, we use 5 MB in default.
		DbConnection db = DbConnection.getInstance();
		long maxSizeInBytes;
		try
		{
			maxSizeInBytes = (long) Math.round(Float.parseFloat(db.getConfig("maxSize"))
					* sMBtoBytes);
		}
		catch(Exception e)
		{
			maxSizeInBytes = 5 * sMBtoBytes;
		}

		logger.debug("Handle Renamed Files has started.");

		// Since this is last to call all modified/created files should be dealt with.
		// We just iterate through the list and rename files.
		while(itrr.hasNext())
		{
			toRename = itrr.next();
			if(FileOp.convertPath(toRename.oldName).toFile().exists())
			{
				// If this is a file we're renaming update database and rename file.
				if(!FileOp.isFolder(FileOp.convertPath(toRename.oldName)))
				{
					// When the new name exists in the backup directory and is plain text
					if(FileOp.convertPath(toRename.newName).toFile().exists()
							&& FileOp.isPlainText(toRename.newName))
					{
						logger.info("Deemed rename operation on "
								+ toRename.oldName.toFile().toString()
								+ " has existing version in the backup directory and is plain text.");

						diff = FileOp.createPatch(FileOp.convertPath(toRename.oldName),
								toRename.newName);

						// Don't add empty revisions
						if(diff.toFile().length() == 0)
						{
							logger.info("File delta created in handleRename was 0 in size: "
									+ toRename.oldName.toFile().toString()
									+ " -- revision not made.");
							// Don't actually rename the file: make a copy (so we can access
							// revisions when moving across folders)
							copyAndRename(toRename.oldName, toRename.newName,
									"Copied in handleRenamedFiles: ");
						}
						else
						{
							// Store the revision if the file is below the max size
							if(FileOp.fileSize(toRename.newName) < maxSizeInBytes)
							{
								createPatchEntry(toRename.oldName, toRename.newName, diff, null);
							}
						}

						// Copy file over
						copyAndRename(toRename.oldName, toRename.newName,
								"Handle Renamed updated an existing file by copying:");
					}
					// Binary files (which exist in the backup directory)
					else if(FileOp.convertPath(toRename.newName).toFile().exists())
					{
						logger.info("Deemed rename operation on "
								+ toRename.oldName.toFile().toString()
								+ " has existing version in the backup directory and is binary.");

						try
						{
							// In the event of moving a file within the live directory, the old name
							// won't exist. In this case, there are no changes and no revision is
							// necessary (or even possible, since the old file == the new file)
							if(toRename.oldName.toFile().exists()
									&& FileOp.fileSize(toRename.newName) < maxSizeInBytes)
							{
								createPatchEntry(toRename.oldName, toRename.newName, null,
										Files.readAllBytes(toRename.oldName));
							}
						}
						catch(IOException e)
						{
							Errors.nonfatalError("Could not insert revision for renamed, existing "
									+ "binary file " + toRename.newName.toString(), e);
						}

						// And copy the file
						copyAndRename(toRename.oldName, toRename.newName,
								"Handle Renamed found a file and updated it:");
					}
					// Files that don't exist in the backup directory
					else
					{
						logger.info("Deemed rename operation on "
								+ toRename.oldName.toFile().toString()
								+ " does not exist in the backup directory");
						copyAndRename(toRename.oldName, toRename.newName, "");
					}
				}
				else
				{
					logger.info("Deemed rename operation on "
							+ toRename.oldName.toFile().toString()
							+ " is renaming an existing folder");

					FileOp.convertPath(toRename.oldName).toFile().renameTo(
							FileOp.convertPath(toRename.newName).toFile());

					// Gets the relative path between the two directories, which will always start
					// with going up (out of) the directory, so we remove that (the first 3
					// characters)
					String newFolderName = toRename.oldName.relativize(toRename.newName).toString().substring(
							3);

					db.renameFolder(toRename.oldName, newFolderName);
				}
			}
			else
			{
				// Case for renaming a file where the old name did not exist in the backup, but the
				// new name does, meaning that we're going to be overwriting some existing file.
				// This is a crucial use case for editors that create a swap file and copy that over
				// the real file.
				if(FileOp.convertPath(toRename.newName).toFile().exists())
				{
					// Plain text files get diffed
					if(FileOp.isPlainText(toRename.newName))
					{
						logger.info("Deemed rename operation on "
								+ toRename.oldName.toFile().toString()
								+ " does not have an old file in the backup directory but the new"
								+ " file exists, likely a swap file; file is plain text");

						// The patch is now from the new name in the backup directory to the new
						// name in the live directory
						diff = FileOp.createPatch(FileOp.convertPath(toRename.newName),
								toRename.newName);

						// Don't add empty revisions
						if(diff.toFile().length() == 0)
						{
							logger.info("File delta created in handleRename was 0 in size: "
									+ toRename.newName.toString() + " -- revision not made.");
						}
						// Store the revision if it's within the size limits
						else if(FileOp.fileSize(toRename.newName) < maxSizeInBytes)
						{
							createPatchEntry(toRename.oldName, toRename.newName, diff, null);
						}

						// Copy file over
						copyNewFile(toRename.newName,
								"Handle renamed updated an existing file by copy:");
					}
					// Binary files
					else
					{
						logger.info("Deemed rename operation on "
								+ toRename.oldName.toFile().toString()
								+ " does not have an old file in the backup directory but the new"
								+ " file exists, likely a swap file; file is binary");

						// Store the revision if it's within size limits
						if(FileOp.fileSize(toRename.newName) < maxSizeInBytes)
						{
							try
							{
								createPatchEntry(toRename.oldName, toRename.newName, null,
										Files.readAllBytes(toRename.newName));
							}
							catch(IOException e)
							{
								Errors.nonfatalError(
										"Could not insert revision for renamed, existing "
												+ "binary file " + toRename.newName.toString(), e);
							}
						}

						// And copy the file
						copyNewFile(toRename.newName,
								"Handle renamed updated a file where only the new name existed:");
					}

					logger.debug("Handle Renamed: "
							+ toRename.newName.toFile().toString()
							+ " existed in the backup directory and a revision was created when overwriting");
				}
				// Finally, we have renamed files that are not being overwritten and are not
				// renaming an existing file
				else
				{
					logger.info("Deemed rename operation on "
							+ toRename.oldName.toFile().toString()
							+ " has neither an existing old or new file. Treat like creation.");

					copyNewFile(toRename.newName,
							"Handle renamed found a file that did not exist and copied it:");
				}
			}

			// Remove entry to move onto the next.
			itrr.remove();
		}
	}

	/**
	 * Compares all elements in Control.deletedFiles to every other handle list, removing those
	 * found in deleted. DOES NOT delete files, only list entries. We want to keep the last version
	 * of a file.
	 */
	public void handleDeletedFiles()
	{
		Iterator<Path> itrd = deletedFiles.iterator();
		Iterator<Path> itrc = null;
		Iterator<Path> itrm = null;
		Iterator<RenamedFile> itrr = null;
		RenamedFile toRename;
		Path pathm, pathc, pathd;
		logger.debug("Handle Deleted Files has started.");

		while(itrd.hasNext())
		{
			pathd = itrd.next();

			// since iterators are not cycling in set, we must get iterators in each loop.

			itrc = createdFiles.iterator();
			itrm = modifiedFiles.iterator();
			itrr = renamedFiles.iterator();

			while(itrc.hasNext())
			{
				pathc = itrc.next();
				if(pathd.equals(pathc))
				{
					itrc.remove();
				}
			}
			while(itrm.hasNext())
			{
				pathm = itrm.next();
				if(pathd.equals(pathm))
				{
					itrm.remove();
				}

			}
			while(itrr.hasNext())
			{
				toRename = itrr.next();
				if(pathd.equals(toRename.oldName))
				{
					itrr.remove();
				}
			}
			itrd.remove();

		}
	}
}
