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
	// TODO: This is the first step of refactoring this class by means of breaking up the list
	// handlers into helper methods. Other forms of validation can be moved here, and validation
	// that takes place inside the handlers can be removed.
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
					if(hit == true)
					{
						itrr.remove();
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
					Path targetDirectory = FileOp.convertPath(pathc).getParent();
					FileOp.copy(pathc, targetDirectory);

					logger.debug("Create Handle: Found new file " + pathc.toFile().toString());
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
		DbConnection db = DbConnection.getInstance();
		long maxSizeInBytes;
		try
		{
			maxSizeInBytes = (long) Math.round(Float.parseFloat(db.getConfig("maxSize")) * 1024 * 1024);
		}
		catch(Exception e)
		{
			maxSizeInBytes = 5 * 1024 * 1024;
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
						itrr.remove();
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
						Path targetDirectory = FileOp.convertPath(pathm).getParent();
						FileOp.copy(pathm, targetDirectory);

						logger.debug("Create File Handle: Found new file "
								+ pathm.toFile().toString());
					}
					else
					{
						// Following the conventions in startupScan...
						// If the file isn't a folder but DOES exist, make diff and copy.
						// Make diff file.
						diff = FileOp.createPatch(FileOp.convertPath(pathm), pathm);
						// Look at size difference.

						if(diff.toFile().length() == 0)
						{
							logger.info("File delta created in handleModified was 0 in size: "
									+ pathm.toFile().toString() + " -- revision not made.");

						}
						else
						{
							long delta = FileOp.fileSize(pathm)
									- FileOp.fileSize(FileOp.convertPath(pathm));

							// Store a revision
							FileHistory fileHist = new FileHistory(pathm);
							fileHist.storeRevision(diff, null, FileOp.fileSize(pathm), delta);

							// Copy file over.
							Path targetDirectory = FileOp.convertPath(pathm).getParent();
							FileOp.copy(pathm, targetDirectory);

							logger.debug("Handle File Modified: Found existing modified file "
									+ pathm.toFile().toString());
						}
					}
				}
				// It's a binary file, so revisions are handled differently
				else if(pathm.toFile().isFile() && FileOp.fileSize(pathm) < maxSizeInBytes
						&& FileOp.convertPath(pathm).toFile().exists())
				{
					// No diffs, just store the revision
					long delta = FileOp.fileSize(pathm)
							- FileOp.fileSize(FileOp.convertPath(pathm));

					try
					{
						FileHistory fileHist = new FileHistory(pathm);
						fileHist.storeRevision(null, Files.readAllBytes(pathm),
								FileOp.fileSize(pathm), delta);
					}
					catch(IOException e)
					{
						Errors.nonfatalError("Failed to revision " + pathm.toString(), e);
					}

					// Copy it over
					Path targetDirectory = FileOp.convertPath(pathm).getParent();
					FileOp.copy(pathm, targetDirectory);

					logger.debug("Create File Handle: Found binary file "
							+ pathm.toFile().toString());
				}
				// It doesn't exist in the backup directory, so just copy
				{
					Path targetDirectory = FileOp.convertPath(pathm).getParent();
					FileOp.copy(pathm, targetDirectory);

					logger.debug("Create File Handle: Found new unrevisioned or large file "
							+ pathm.toFile().toString());
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
		String newName;

		// Figure out the maximum size that gets revisioned (note the config is in MB, so is
		// multiplied by 1024^2 to get bytes
		DbConnection db = DbConnection.getInstance();
		long maxSizeInBytes;
		try
		{
			maxSizeInBytes = (long) Math.round(Float.parseFloat(db.getConfig("maxSize")) * 1024 * 1024);
		}
		catch(Exception e)
		{
			maxSizeInBytes = 5 * 1024 * 1024;
		}

		logger.debug("Handle Renamed Files has started.");

		// Since this is last to call all modified/created files should be dealt with.
		// We just iterate through the list and rename files.
		while(itrr.hasNext())
		{
			toRename = itrr.next();
			newName = toRename.oldName.getParent().relativize(toRename.newName).toString();
			if(FileOp.convertPath(toRename.oldName).toFile().exists())
			{
				// If this is a file we're renaming update database and rename file.
				if(!FileOp.isFolder(FileOp.convertPath(toRename.oldName)))
				{
					// When the new name exists in the backup directory and is plain text
					if(FileOp.convertPath(toRename.newName).toFile().exists()
							&& FileOp.isPlainText(toRename.newName))
					{
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
							Path targetDirectory = FileOp.convertPath(toRename.newName).getParent();
							FileOp.copy(FileOp.convertPath(toRename.oldName), targetDirectory);

							FileHistory fileHist = new FileHistory(toRename.oldName);
							fileHist.renameRevision(newName);
						}
						else
						{
							// Store the revision if the file is below the max size
							if(FileOp.fileSize(toRename.newName) < maxSizeInBytes)
							{
								long delta = FileOp.fileSize(toRename.newName)
										- FileOp.fileSize(FileOp.convertPath(toRename.oldName));

								FileHistory fileHist = new FileHistory(toRename.newName);
								fileHist.storeRevision(diff, null,
										FileOp.fileSize(toRename.newName), delta);
							}

							// Copy file over
							Path targetDirectory = FileOp.convertPath(toRename.newName).getParent();
							FileOp.copy(toRename.newName, targetDirectory);

							FileHistory fileHist = new FileHistory(toRename.oldName);
							fileHist.renameRevision(newName);

							logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
									+ "already existed and was updated.");
						}
					}
					// Binary files (which exist in the backup directory)
					else if(toRename.oldName.toFile().exists())
					{
						// Store the revision
						long delta = FileOp.fileSize(toRename.newName)
								- FileOp.fileSize(FileOp.convertPath(toRename.oldName));
						try
						{
							// In the event of moving a file within the live directory, the old name
							// won't exist. In this case, there are no changes and no revision is
							// necessary (or even possible, since the old file == the new file)
							if(toRename.oldName.toFile().exists()
									&& FileOp.fileSize(toRename.newName) < maxSizeInBytes)
							{
								FileHistory fileHist = new FileHistory(toRename.newName);
								fileHist.storeRevision(null, Files.readAllBytes(toRename.oldName),
										FileOp.fileSize(toRename.newName), delta);
							}
						}
						catch(IOException e)
						{
							Errors.nonfatalError("Could not insert revision for renamed, existing "
									+ "binary file " + toRename.newName.toString(), e);
						}

						// And copy the file
						Path targetDirectory = FileOp.convertPath(toRename.newName).getParent();
						FileOp.copy(toRename.newName, targetDirectory);

						FileHistory fileHist = new FileHistory(toRename.oldName);
						fileHist.renameRevision(newName);

						logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
								+ " existed and was updated.");
					}
					// Files that don't exist in the backup directory
					else
					{
						Path targetDirectory = FileOp.convertPath(toRename.newName).getParent();
						FileOp.copy(toRename.newName, targetDirectory);

						FileHistory fileHist = new FileHistory(toRename.oldName);
						fileHist.renameRevision(newName);

						logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
								+ " existed but was not a valid file and was updated.");
					}
				}
				else
				{
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
						// The patch is now from the new name in the backup directory to the new
						// name in the live directory
						diff = FileOp.createPatch(FileOp.convertPath(toRename.newName),
								toRename.newName);

						// Don't add empty revisions
						if(diff.toFile().length() == 0)
						{
							logger.info("File delta created in handleRename was 0 in size: "
									+ toRename.newName.toFile().toString()
									+ " -- revision not made.");
						}
						else
						{
							// Store the revision if it's within the size limits
							if(FileOp.fileSize(toRename.newName) < maxSizeInBytes)
							{
								long delta = FileOp.fileSize(toRename.newName)
										- FileOp.fileSize(FileOp.convertPath(toRename.newName));

								FileHistory fileHist = new FileHistory(toRename.newName);
								fileHist.storeRevision(diff, null,
										FileOp.fileSize(toRename.newName), delta);
							}

							// Copy file over
							Path targetDirectory = FileOp.convertPath(toRename.newName).getParent();
							FileOp.copy(toRename.newName, targetDirectory);

							logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
									+ "already existed and was updated.");
						}
					}
					// Binary files
					else
					{
						// Store the revision if it's within size limits
						if(FileOp.fileSize(toRename.newName) < maxSizeInBytes)
						{
							long delta = FileOp.fileSize(toRename.newName)
									- FileOp.fileSize(FileOp.convertPath(toRename.newName));
							try
							{
								FileHistory fileHist = new FileHistory(toRename.newName);
								fileHist.storeRevision(null, Files.readAllBytes(toRename.newName),
										FileOp.fileSize(toRename.newName), delta);
							}
							catch(IOException e)
							{
								Errors.nonfatalError(
										"Could not insert revision for renamed, existing "
												+ "binary file " + toRename.newName.toString(), e);
							}
						}

						// And copy the file
						Path targetDirectory = FileOp.convertPath(toRename.newName).getParent();
						FileOp.copy(toRename.newName, targetDirectory);

						logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
								+ " only new name existed.");
					}

					logger.debug("Handle Renamed: "
							+ toRename.newName.toFile().toString()
							+ " existed in the backup directory and a revision was created when overwriting");
				}
				// Finally, we have renamed files that are not being overwritten and are not
				// renaming an existing file
				else
				{
					Path targetDirectory = FileOp.convertPath(toRename.newName).getParent();
					FileOp.copy(toRename.newName, targetDirectory);
					logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
							+ " did not exist and was added.");
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

			// Must get iterators at each loop, since iterators are not cycling.
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
