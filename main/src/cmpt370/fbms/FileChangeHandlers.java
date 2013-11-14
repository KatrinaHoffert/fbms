package cmpt370.fbms;

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
				if(FileOp.fileValid(pathm))
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
							FileHistory.storeRevision(pathm, diff, FileOp.fileSize(pathm), delta);

							// Copy file over.
							Path targetDirectory = FileOp.convertPath(pathm).getParent();
							FileOp.copy(pathm, targetDirectory);

							Main.logger.debug("Handle File Modified: Found existing modified file "
									+ pathm.toFile().toString());
						}
					}
				}
				else
				{
					// If the file isn't valid, just copy (its binary or large).
					Path targetDirectory = FileOp.convertPath(pathm).getParent();
					FileOp.copy(pathm, targetDirectory);

					Main.logger.debug("Create File Handle: Found new large or binary file "
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
		// Since this is last to call all modified/created files should be dealt with.
		// We just iterate through the list and rename files.
		// All files on the list should by convention already exist.
		Main.logger.debug("Handle Renamed Files has started.");
		while(itrr.hasPrevious())
		{
			toRename = itrr.previous();
			newName = toRename.oldName.getParent().relativize(toRename.newName).toString();
			if(FileOp.convertPath(toRename.oldName).toFile().exists())
			{
				// If this is a file we're renaming update database and rename file.
				if(!FileOp.isFolder(FileOp.convertPath(toRename.oldName)))
				{
					if(FileOp.fileValid(toRename.newName))
					{
						diff = FileOp.createPatch(FileOp.convertPath(toRename.oldName),
								toRename.newName);

						if(diff.toFile().length() == 0)
						{
							Main.logger.info("File delta created in handleRename was 0 in size: "
									+ toRename.oldName.toFile().toString()
									+ " -- revision not made.");
							FileOp.rename(FileOp.convertPath(toRename.oldName), newName);
							FileHistory.renameRevision(toRename.oldName, newName);

						}
						else
						{
							// Look at size difference.
							long delta = FileOp.fileSize(toRename.newName)
									- FileOp.fileSize(FileOp.convertPath(toRename.oldName));

							// Store a revision
							FileHistory.storeRevision(toRename.newName, diff,
									FileOp.fileSize(toRename.newName), delta);

							// Copy file over.
							Path targetDirectory = FileOp.convertPath(toRename.newName).getParent();
							FileOp.copy(toRename.newName, targetDirectory);
							FileOp.delete(FileOp.convertPath(toRename.oldName));
							FileHistory.renameRevision(toRename.oldName, newName);

							Main.logger.debug("Handle Renamed: "
									+ toRename.newName.toFile().toString()
									+ "already existed and was updated.");
						}
					}
					else
					{
						Path targetDirectory = FileOp.convertPath(toRename.newName).getParent();
						FileOp.copy(toRename.newName, targetDirectory);
						FileOp.delete(FileOp.convertPath(toRename.oldName));
						FileHistory.renameRevision(toRename.oldName, newName);
						Main.logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
								+ "existed but was not a valid file and was updated.");
					}
				}
				else
				{
					FileOp.convertPath(toRename.oldName).toFile().renameTo(
							FileOp.convertPath(toRename.newName).toFile());
					DbManager.renameFolder(toRename.oldName, newName);
				}
			}
			else
			{

				Path targetDirectory = FileOp.convertPath(toRename.newName).getParent();
				FileOp.copy(toRename.newName, targetDirectory);
				FileOp.delete(toRename.oldName);
				Main.logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
						+ " did not exist and was added.");
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
