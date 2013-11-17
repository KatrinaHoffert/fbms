package cmpt370.fbms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ListIterator;

/**
 * Handles all the changes to the files that the watcher detects by taking appropriate actions.
 */
public class FileChangeHandlers
{
	/**
	 * Handles all created files identified by the watcher. Also handles recurrent entries between
	 * modified, and renamed files. All iterations start from the back of the list, that way we
	 * handle the most recent events first. This is more important for file renames but I kept it
	 * consistent.
	 */
	public static void handleCreatedFiles()
	{
		ListIterator<Path> itrc, itrm;
		ListIterator<RenamedFile> itrr;
		boolean hit, found;
		Path pathc, pathm;
		RenamedFile toRename;
		itrc = Main.createdFiles.listIterator(Main.createdFiles.size());
		found = false;
		Main.logger.debug("Handle Created Files has started.");

		// Search through all elements of created files, comparing them to instances of renamed
		// files and modified files.
		while(itrc.hasPrevious())
		{
			pathc = itrc.previous();
			hit = false; // If we've hit a duplicate already in this list.
			found = false;
			itrr = Main.renamedFiles.listIterator(Main.renamedFiles.size());
			// Check renamed files for created files duplicates.
			while(itrr.hasPrevious())
			{
				toRename = itrr.previous();
				if(pathc.toFile().equals(toRename.newName.toFile())
						|| pathc.toFile().equals(toRename.oldName.toFile()))
				{
					// If we've hit a duplicate already, we've made the diff/backup and we can
					// delete this one safely.
					if(hit == true)
					{
						itrr.remove();
						Main.logger.debug("Create Handle: Found duplicate in renamed, removing.");
					}
					// Otherwise we set found and hit to be true. It will be then removed from
					// created and left on renamed to be handled there.
					else
					{
						Main.logger.debug("Create Handle: Found same entry in renamed, leaving and deleting in created.");
						hit = true;
						found = true;
					}
				}
			}

			hit = false;
			itrm = Main.modifiedFiles.listIterator(Main.modifiedFiles.size());
			// Now we cycle through the modified list.
			while(itrm.hasPrevious())
			{
				pathm = itrm.previous();
				if(pathc.toFile().equals(pathm.toFile()))
				{
					// If we find a duplicate but already have made a diff/backup just delete the
					// duplicate.
					if(hit == true || found == true)
					{
						itrm.remove();
						Main.logger.debug("Create File Handle: Found duplicate in modified, removing:"
								+ pathm.toFile().toString());
					}
					// Otherwise we make backups/entries and set hit/found to true.
					else
					{
						Main.logger.debug("Create Handle: Found same entry in modified, leaving and deleting in created.");
						hit = true;
						found = true;
					}
				}
			}
			// If after going through both modified and renamed lists we didn't find a duplicate,
			// make a diff/backup and remove it from the created list.
			if(!found)
			{
				Main.logger.info("Create Handle: file " + pathc.toFile().toString()
						+ " was not found in any other list.");
				// If the file doesn't exist we copy it over.
				if(!FileOp.isFolder(pathc) && !FileOp.convertPath(pathc).toFile().exists())
				{
					// If the file isn't a folder and is not in the backup folder, copy it over.
					Path targetDirectory = FileOp.convertPath(pathc).getParent();
					FileOp.copy(pathc, targetDirectory);

					Main.logger.debug("Create Handle: Found new file " + pathc.toFile().toString());
				}
				else if(FileOp.isFolder(pathc) && !FileOp.convertPath(pathc).toFile().exists())
				{
					FileOp.copy(pathc, FileOp.convertPath(pathc));
				}
				// If it does exist, it was modified after creation and that takes priority.
				else
				{
					Main.modifiedFiles.add(pathc);
					Main.logger.debug("Create Handle: Move " + pathc.toFile().toString()
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
	public static void handleModifiedFiles()
	{
		ListIterator<Path> itrm = Main.modifiedFiles.listIterator(Main.modifiedFiles.size());
		ListIterator<RenamedFile> itrr;

		Path pathm, diff;
		RenamedFile toRename;
		boolean hit = false;

		// Figure out the maximum size that gets revisioned (note the config is in MB, so is
		// multiplied by 1024^2 to get bytes
		DbManager db = DbManager.getInstance();
		long maxSizeInBytes = (long) Math.round(Float.parseFloat(db.getConfig("maxSize")) * 1024 * 1024);

		Main.logger.debug("Handle Modified Files has started.");

		while(itrm.hasPrevious())
		{
			pathm = itrm.previous();
			itrr = Main.renamedFiles.listIterator(Main.renamedFiles.size());
			hit = false;
			while(itrr.hasPrevious())
			{
				toRename = itrr.previous();
				if(pathm.equals(toRename.newName) || pathm.equals(toRename.oldName))
				{
					// Clean up additional copies, will only do this if its already made a
					// diff/backup of the file.
					if(hit == true)
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

						Main.logger.debug("Create File Handle: Found new file "
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
							Main.logger.info("File delta created in handleModified was 0 in size: "
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

							Main.logger.debug("Handle File Modified: Found existing modified file "
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

					Main.logger.debug("Create File Handle: Found binary file "
							+ pathm.toFile().toString());
				}
				// It doesn't exist in the backup directory, so just copy
				{
					Path targetDirectory = FileOp.convertPath(pathm).getParent();
					FileOp.copy(pathm, targetDirectory);

					Main.logger.debug("Create File Handle: Found new unrevisioned or large file "
							+ pathm.toFile().toString());
				}
			}
			itrm.remove();
		}
	}

	public static void handleRenamedFiles()
	{
		ListIterator<RenamedFile> itrr = Main.renamedFiles.listIterator(Main.renamedFiles.size());

		RenamedFile toRename;
		Path diff;
		String newName;

		// Figure out the maximum size that gets revisioned (note the config is in MB, so is
		// multiplied by 1024^2 to get bytes
		DbManager db = DbManager.getInstance();
		long maxSizeInBytes = (long) Math.round(Float.parseFloat(db.getConfig("maxSize")) * 1024 * 1024);

		Main.logger.debug("Handle Renamed Files has started.");

		// Since this is last to call all modified/created files should be dealt with.
		// We just iterate through the list and rename files.
		while(itrr.hasPrevious())
		{
			toRename = itrr.previous();
			newName = toRename.oldName.getParent().relativize(toRename.newName).toString();
			if(FileOp.convertPath(toRename.oldName).toFile().exists())
			{
				// If this is a file we're renaming update database and rename file.
				if(!FileOp.isFolder(FileOp.convertPath(toRename.oldName)))
				{
					System.out.println(toRename.oldName.toString() + " --> "
							+ toRename.newName.toString());
					// When the new name exists in the backup directory and is plain text
					if(FileOp.convertPath(toRename.newName).toFile().exists()
							&& FileOp.isPlainText(toRename.newName))
					{
						diff = FileOp.createPatch(FileOp.convertPath(toRename.oldName),
								toRename.newName);

						// Don't add empty revisions
						if(diff.toFile().length() == 0)
						{
							Main.logger.info("File delta created in handleRename was 0 in size: "
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

							Main.logger.debug("Handle Renamed: "
									+ toRename.newName.toFile().toString()
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

						Main.logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
								+ " existed and was updated.");
					}
					// Files that don't exist in the backup directory
					else
					{
						Path targetDirectory = FileOp.convertPath(toRename.newName).getParent();
						FileOp.copy(toRename.newName, targetDirectory);

						FileHistory fileHist = new FileHistory(toRename.oldName);
						fileHist.renameRevision(newName);

						Main.logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
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
							Main.logger.info("File delta created in handleRename was 0 in size: "
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

							Main.logger.debug("Handle Renamed: "
									+ toRename.newName.toFile().toString()
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

						Main.logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
								+ " only new name existed.");
					}

					Main.logger.debug("Handle Renamed: "
							+ toRename.newName.toFile().toString()
							+ " existed in the backup directory and a revision was created when overwriting");
				}
				// Finally, we have renamed files that are not being overwritten and are not
				// renaming an existing file
				else
				{
					Path targetDirectory = FileOp.convertPath(toRename.newName).getParent();
					FileOp.copy(toRename.newName, targetDirectory);
					Main.logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
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
	public static void handleDeletedFiles()
	{

		ListIterator<Path> itrd = Main.deletedFiles.listIterator(Main.deletedFiles.size());
		ListIterator<Path> itrc = Main.createdFiles.listIterator(Main.createdFiles.size());
		ListIterator<Path> itrm = Main.modifiedFiles.listIterator(Main.modifiedFiles.size());
		ListIterator<RenamedFile> itrr = Main.renamedFiles.listIterator(Main.renamedFiles.size());
		RenamedFile toRename;
		Path pathm, pathc, pathd;
		Main.logger.debug("Handle Deleted Files has started.");

		while(itrd.hasPrevious())
		{
			pathd = itrd.previous();
			while(itrc.hasPrevious())
			{
				pathc = itrc.previous();
				if(pathd.equals(pathc))
				{
					itrc.remove();
				}
			}
			while(itrm.hasPrevious())
			{
				pathm = itrm.previous();
				if(pathd.equals(pathm))
				{
					itrm.remove();
				}

			}
			while(itrr.hasPrevious())
			{
				toRename = itrr.previous();
				if(pathd.equals(toRename.oldName))
				{
					itrr.remove();
				}
			}
			itrd.remove();

		}
	}
}
