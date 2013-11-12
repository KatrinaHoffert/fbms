/*
 * FBMS: File Backup and Management System Copyright (C) 2013 Group 06
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package cmpt370.fbms;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;

import org.apache.log4j.Logger;

import cmpt370.fbms.GUI.FirstStartWizard;
import cmpt370.fbms.GUI.FrontEnd;

// Making sure libraries are found (remove later)

public class Control
{
	// Various public variables shared amongst components
	public static Path liveDirectory = null;
	public static Path backupDirectory = null;
	public static List<Path> createdFiles = Collections.synchronizedList(new LinkedList<Path>());
	public static List<Path> modifiedFiles = Collections.synchronizedList(new LinkedList<Path>());
	public static List<RenamedFile> renamedFiles = Collections.synchronizedList(new LinkedList<RenamedFile>());
	public static List<Path> deletedFiles = Collections.synchronizedList(new LinkedList<Path>());
	public static boolean firstRunWizardDone = false;

	// Logger object is linked to the S
	public static Logger logger = Logger.getLogger(Control.class);

	// Watch ID for JNotify
	private static int watchId = 0;
	private static long loop = 0;

	/**
	 * The main method runs the startup code, initializing the database, checking for the first run,
	 * etc. The main method then loops through the watched files at intervals, checking for changes
	 * and acting appropriately
	 * 
	 * @param args
	 *            Command line arguments, not currently used
	 */
	public static void main(String[] args)
	{
		startup();
		startupScan(liveDirectory);
		fileHandler();
		FrontEnd.createAndShowGUI();
	}

	/**
	 * Manages all the startup functionality. First, we check if the backup directory has been set.
	 * If not, we begin the first run
	 */
	private static void startup()
	{
		// And print a testing message to the log
		logger.info("Program started");

		resolveBackupDirectory();

		// Branch based on whether or not this is considered a "first run". backupDirectory should
		// be set to a valid directory if resolveBackupDirectory() found the backup directory.
		// DbManager.init() can then load the database located there. If that's not the case, we'll
		// have to run the first start wizard, which will set the database directory and possibly
		// the live directory (for imports, DbManager.init() fetches the live directory)
		if(backupDirectory == null)
		{
			// Run the first run wizard. Keep the program open until that is done
			FirstStartWizard.run();
			while(!firstRunWizardDone)
			{
				try
				{
					Thread.sleep(500);
				}
				catch(InterruptedException e)
				{
					logger.error(e);
				}
			}

			// Initialize the database, then set the live directory inside this database (for
			// sequential program start-ups)
			DbManager.init();
			DbManager.setConfig("liveDirectory", liveDirectory.toString());

			// Set some default settings
			DbManager.setConfig("trimDate", "-1");
			DbManager.setConfig("startupScan", "true");
			DbManager.setConfig("disableNonFatalErrors", "false");

			logger.info("First run wizard completed");
		}
		else
		{
			// For subsequent runs, the backup directory has already been set and the live
			// directory will be retreived during DbManager.init()
			DbManager.init();

			// Live directory specified in database is invalid
			if(!liveDirectory.toFile().exists())
			{
				int choice = JOptionPane.showConfirmDialog(
						null,
						"The live directory could not be found. Would you like to specify an alternative directory?",
						"Fatal error", JOptionPane.YES_NO_OPTION);
				if(choice == JOptionPane.YES_OPTION)
				{
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

					// Return value will be JFileChooser.APPROVE_OPTION iff a folder was chosen. Any
					// other value means the window was closed
					int returnVal = fileChooser.showOpenDialog(null);

					// We're a go
					if(returnVal == JFileChooser.APPROVE_OPTION)
					{
						Path chosenPath = fileChooser.getSelectedFile().toPath();

						if(!chosenPath.startsWith(backupDirectory)
								&& !backupDirectory.startsWith(chosenPath))
						{
							liveDirectory = chosenPath;
						}
						else
						{
							JOptionPane.showMessageDialog(null,
									"Live directory cannot be a child of the live directory and vice versa.");
							System.exit(2);
						}
					}
					else
					{
						System.exit(2);
					}
				}
				else
				{
					System.exit(2);
				}
			}

			logger.info("It is a subsequent run");
		}
		logger.info("liveDirectory = " + liveDirectory);
		logger.info("backupDirectory = " + backupDirectory);

		// JNotify watcher for files. The live directory is watched for all four types of files
		// changes: creations, deletions, modifications, and renaming. We watch subfolders of the
		// live directory for changes as well. The Watcher class forms the listener for these
		// changes
		try
		{
			watchId = JNotify.addWatch(liveDirectory.toString(), JNotify.FILE_CREATED
					| JNotify.FILE_DELETED | JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED, true,
					new Watcher());
		}
		catch(JNotifyException e)
		{
			Errors.fatalError("Could not start file watcher module", e);
		}

		// Redirect standard error to the log
		System.setErr(createLoggingProxy(System.err));
	}

	/**
	 * Sends an output to the log. Used to redirect standard error.
	 * 
	 * @param realPrintStream
	 *            The output stream to send to the log.
	 * @return The passed in print stream, which is logged AND sent to standard error.
	 */
	private static PrintStream createLoggingProxy(final PrintStream realPrintStream)
	{
		return new PrintStream(realPrintStream)
		{
			public void print(final String string)
			{
				realPrintStream.print(string);
				logger.error(string);
			}
		};
	}

	/**
	 * Opens the "backup_location" file in the program directory and parses its contents as a path.
	 * The parsed path is set as the backup directory. If the path is invalid, the backup directory
	 * is not set.
	 */
	private static void resolveBackupDirectory()
	{
		// Load the backup_location file to get the backup folder path. If it doesn't exist, it's
		// the first run
		File backup_file = new File("backup_location");
		if(backup_file.exists())
		{
			Scanner in = null;
			try
			{
				// Read the path in, and if it's valid, set the backup location to this path. If
				// it's not valid, it's the first run
				in = new Scanner(new FileReader(backup_file));
				File backupLocation = Paths.get(in.nextLine()).toFile();

				if(backupLocation.exists())
				{
					backupDirectory = backupLocation.toPath();
					logger.info("Located backup location: " + backupDirectory);
				}
			}
			catch(IOException e)
			{
				// If an exception occurs, we couldn't retrieve the backup directory, so must set it
				// to null
				logger.error("Could not read in backup_location file", e);
			}
			catch(InvalidPathException e)
			{
				logger.error("Backup directory path is invalid", e);
				backupDirectory = null;
			}
			catch(SecurityException e)
			{
				logger.error("Security error: cannot access backup directory", e);
				backupDirectory = null;
			}
			finally
			{
				if(in != null)
				{
					in.close();
				}
			}

			// If the database file isn't in the backup folder, it's not a valid folder
			if(backupDirectory == null
					|| !backupDirectory.resolve(".revisions.db").toFile().exists())
			{
				logger.error("The backup directory linked in \"backup_location\" is invalid: "
						+ backupDirectory);
				backupDirectory = null;
				JOptionPane.showMessageDialog(null,
						"A backup location record exists, but is invalid. The first-run wizard "
								+ "will be displayed.", "Error", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * Scans the live directory for byte changes on startup, allowing the detection of files that
	 * were added or modified when the program was not running. Renames, however, are a lost cause.
	 * They'll be recognized as a new file.
	 * 
	 * @param directory
	 *            The directory to base the scan on (ie, the live directory)
	 */
	private static void startupScan(Path directory)
	{
		String status = DbManager.getConfig("startupScan");
		// Check if user disabled the scan
		if(status != null && !status.equals("true"))
		{
			logger.info("Startup scan is disabled.");
			return;
		}

		for(File file : directory.toFile().listFiles())
		{
			if(!file.isDirectory())
			{
				// If the file doesn't already exist, we can just copy it over
				if(!FileOp.convertPath(file.toPath()).toFile().exists())
				{
					Path targetDirectory = FileOp.convertPath(file.toPath()).getParent();
					FileOp.copy(file.toPath(), targetDirectory);

					logger.info("Startup: Found new file " + file.toString());
				}
				// The file does exist, so determine if the file has been changed. If it
				// hasn't, we need to create a revision for this file.
				else if(!FileOp.isEqual(file.toPath(), FileOp.convertPath(file.toPath())))
				{
					// Create the diff
					Path diffFile = FileOp.createPatch(FileOp.convertPath(file.toPath()),
							file.toPath());
					// Difference in file sizes
					long delta = FileOp.fileSize(file.toPath())
							- FileOp.fileSize(FileOp.convertPath(file.toPath()));

					// Store the revision
					FileHistory.storeRevision(file.toPath(), diffFile,
							FileOp.fileSize(file.toPath()), delta);

					// Finally, copy the file over
					Path targetDirectory = FileOp.convertPath(file.toPath()).getParent();
					FileOp.copy(file.toPath(), targetDirectory);

					logger.info("Startup: Found modified file " + file.toString());
				}
			}
			else
			{
				// Call itself recursively for directories
				startupScan(directory.resolve(file.toPath()));
			}
		}
	}

	/**
	 * This method sets up the loop for going through the array lists that the Watcher module
	 * populates. It also creates an infinite loop in a separate thread, allowing the program to run
	 * indefinitely in the background.
	 */
	private static void fileHandler()
	{
		// Run this stuff in a new thread
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					while(true)
					{
						logger.debug("Main service loop running at T = " + new Date().getTime()
								/ 1000);

						// Keep track of how many file loops we've done
						loop++;

						// Synchronize all lists before we handle them.
						synchronized(deletedFiles)
						{
							synchronized(createdFiles)
							{
								synchronized(modifiedFiles)
								{
									synchronized(renamedFiles)
									{
										handleDeletedFiles();
										handleCreatedFiles();
										handleModifiedFiles();
										handleRenamedFiles();
									}
								}
							}
						}

						// Every 500 loops, we run the command to trim the database
						if(loop % 500 == 0)
						{
							DbManager.trimDatabase();
						}

						// Time to wait before "polling" the file lists again. A suitable time needs
						// to be determined. This should be configurable in future versions of the
						// program
						Thread.sleep(5000);
					}
				}
				catch(InterruptedException e)
				{
					Errors.fatalError("Thread was interupted", e);
				}
			};
		}.start();
	}

	/**
	 * Handles all created files identified by the watcher. Also handles recurrent entries between
	 * modified, and renamed files. All iterations start from the back of the list, that way we
	 * handle the most recent events first. This is more important for file renames but I kept it
	 * consistent.
	 */
	private static void handleCreatedFiles()
	{
		ListIterator<Path> itrc, itrm;
		ListIterator<RenamedFile> itrr;
		boolean hit, found;
		Path pathc, pathm;
		RenamedFile toRename;
		itrc = createdFiles.listIterator(createdFiles.size());
		found = false;
		logger.debug("Handle Created Files has started.");
		// Search through all elements of created files, comparing them to instances of renamed
		// files and modified files.
		while(itrc.hasPrevious())
		{
			pathc = itrc.previous();
			hit = false; // If we've hit a duplicate already in this list.
			found = false;
			itrr = renamedFiles.listIterator(renamedFiles.size());
			// Check renamed files for created files duplicates.
			while(itrr.hasPrevious())
			{
				toRename = itrr.previous();
				if(pathc.toFile().equals(toRename.newName.toFile()))
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
			itrm = modifiedFiles.listIterator(modifiedFiles.size());
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
				if(!FileOp.convertPath(pathc).toFile().exists())
				{
					// If the file isn't a folder and is not in the backup folder, copy it over.
					Path targetDirectory = FileOp.convertPath(pathc).getParent();
					FileOp.copy(pathc, targetDirectory);

					logger.debug("Create Handle: Found new file " + pathc.toFile().toString());
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
	private static void handleModifiedFiles()
	{
		ListIterator<Path> itrm = modifiedFiles.listIterator(modifiedFiles.size());
		ListIterator<RenamedFile> itrr;
		Path pathm, diff;
		RenamedFile toRename;
		boolean hit = false;
		logger.debug("Handle Modified Files has started.");
		while(itrm.hasPrevious())
		{
			pathm = itrm.previous();
			itrr = renamedFiles.listIterator(renamedFiles.size());
			hit = false;
			while(itrr.hasPrevious())
			{
				toRename = itrr.previous();
				if(pathm.equals(toRename.newName))
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
						long delta = FileOp.fileSize(pathm)
								- FileOp.fileSize(FileOp.convertPath(pathm));

						// Store a revision
						FileHistory.storeRevision(pathm, diff, FileOp.fileSize(pathm), delta);

						// Copy file over.
						Path targetDirectory = FileOp.convertPath(pathm).getParent();
						FileOp.copy(pathm, targetDirectory);

						logger.debug("Handle File Modified: Found existing modified file "
								+ pathm.toFile().toString());
					}
				}
				else
				{
					// If the file isn't valid, just copy (its binary or large).
					Path targetDirectory = FileOp.convertPath(pathm).getParent();
					FileOp.copy(pathm, targetDirectory);

					logger.debug("Create File Handle: Found new large or binary file "
							+ pathm.toFile().toString());
				}
			}
			itrm.remove();
		}
	}

	private static void handleRenamedFiles()
	{

		ListIterator<RenamedFile> itrr = renamedFiles.listIterator(renamedFiles.size());
		RenamedFile toRename;
		Path diff;
		String newName;
		// Since this is last to call all modified/created files should be dealt with.
		// We just iterate through the list and rename files.
		// All files on the list should by convention already exist.
		logger.debug("Handle Renamed Files has started.");
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

						logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
								+ "already existed and was updated.");
					}
					else
					{
						Path targetDirectory = FileOp.convertPath(toRename.newName).getParent();
						FileOp.copy(toRename.newName, targetDirectory);
						FileOp.delete(FileOp.convertPath(toRename.oldName));
						FileHistory.renameRevision(toRename.oldName, newName);
						logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
								+ "existed but was not a valid file and was updated.");
					}
				}
			}
			else
			{

				Path targetDirectory = FileOp.convertPath(toRename.newName).getParent();
				FileOp.copy(toRename.newName, targetDirectory);

				logger.debug("Handle Renamed: " + toRename.newName.toFile().toString()
						+ "did not exist and was added.");
			}

			// Remove entry to move onto the next.
			itrr.remove();
		}

	}

	/**
	 * Compares all elements in deletedFiles to every other handle list, removing those found in
	 * deleted. DOES NOT delete files, only list entries. We want to keep the last version of a
	 * file.
	 */
	private static void handleDeletedFiles()
	{

		ListIterator<Path> itrd = deletedFiles.listIterator(deletedFiles.size());
		ListIterator<Path> itrc = createdFiles.listIterator(createdFiles.size());
		ListIterator<Path> itrm = modifiedFiles.listIterator(modifiedFiles.size());
		ListIterator<RenamedFile> itrr = renamedFiles.listIterator(renamedFiles.size());
		RenamedFile toRename;
		Path pathm, pathc, pathd;
		logger.debug("Handle Deleted Files has started.");

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

	/**
	 * Obtains the specified revision and displays it in the default program for that type of file.
	 * 
	 * @param file
	 *            The file's path (in the live directory).
	 * @param timestamp
	 *            The Unix time stamp (seconds since Unix epoch).
	 */
	public static void displayRevision(Path file, long timestamp)
	{
		Path fileToOpen = FileHistory.obtainRevision(file, timestamp);
		try
		{
			Desktop.getDesktop().open(fileToOpen.toFile());
		}
		catch(IOException e)
		{
			Errors.nonfatalError("Could not open revision file.", e);
		}
	}

	/**
	 * Displays the changes between two revisions by opening up an HTML file in the web browser.
	 * 
	 * @param file
	 *            The file's path (in the live directory).
	 * @param timestamp
	 *            The Unix time stamp (seconds since Unix epoch).
	 */
	public static void displayRevisionChanges(Path file, long timestamp)
	{
		// Displays the diff of the specified revision
		Path specificDiff = FileHistory.getRevision(FileOp.convertPath(file), timestamp);
		Path html = FileOp.prettyPrintPatch(specificDiff);

		try
		{
			Desktop.getDesktop().open(html.toFile());
		}
		catch(IOException e)
		{
			Errors.nonfatalError("Could not open revision changes file.", e);
		}
	}

	/**
	 * Reverts a revision by obtaining a specific revision and making that the current revision.
	 * 
	 * @param file
	 *            The path to the file (in the backup directory) to revert. The file must exist.
	 * @param timestamp
	 *            The time stamp (in Unix time stamp format) of the revision we want.
	 */
	public static void revertRevision(Path file, long timestamp)
	{
		if(file.toFile().exists())
		{
			// Get the revision we want
			Path revertedFile = FileHistory.obtainRevision(file, timestamp);

			logger.debug("Revert to: " + revertedFile.toFile().toString());
			try
			{
				Files.copy(revertedFile, FileOp.convertPath(file),
						StandardCopyOption.REPLACE_EXISTING);

				logger.debug("Copied " + revertedFile.toFile().toString() + " as "
						+ FileOp.convertPath(file).toFile().toString());
			}
			catch(IOException e)
			{
				Errors.nonfatalError("Could not copy file " + revertedFile.toString() + " to "
						+ FileOp.convertPath(file).toString(), e);
			}
		}
		else
		{
			Errors.nonfatalError("The file you wanted to revert does not exist!");
		}
	}

	/**
	 * Copies all files in the backup directory to the live directory.
	 */
	public static void restoreBackup(Path directory)
	{
		// Iterate through all files in the backup folder, copying them to the live directory
		for(File child : backupDirectory.toFile().listFiles())
		{
			// If we're copying a directory, we must specify the folder name instead of destination
			if(child.isDirectory())
			{
				FileOp.copy(child.toPath(), directory.resolve(child.getName()));
			}
			else
			{
				FileOp.copy(child.toPath(), directory);
			}
		}
	}

	/**
	 * Just a wrapper function for the FrontEnd.
	 * 
	 * @param sourceFile
	 *            The path of the file we want to copy.
	 * @param destFolder
	 *            The folder we want to copy this file to.
	 */
	public static void copyTo(Path sourceFile, Path destFolder)
	{
		// If we're copying a directory, we must specify the folder name instead of destination
		if(sourceFile.toFile().isDirectory())
		{
			FileOp.copy(sourceFile, destFolder.resolve(sourceFile.getFileName()));
		}
		else
		{
			FileOp.copy(sourceFile, destFolder);
		}
	}

	/**
	 * Changes the live directory. The new live directory is set in the program and database, the
	 * watcher is reassigned, and the contents of the live directory are backed up (and revisioned
	 * if applicable).
	 * 
	 * This does not check if the live directory is valid (eg, not a child of the backup directory).
	 * That is a pre-condition.
	 * 
	 * As of the time of implementation, this is currently not called from anywhere. It is expected
	 * to be added to future versions of the FrontEnd.
	 * 
	 * @param newDirectory
	 *            The new directory to use for the live directory.
	 */
	public static void changeLiveDirectory(Path newDirectory)
	{
		// Set the new directory in the database
		liveDirectory = newDirectory;
		DbManager.setConfig("liveDirectory", liveDirectory.toString());

		// Remove the old watcher
		try
		{
			JNotify.removeWatch(watchId);
		}
		catch(JNotifyException e)
		{
			Errors.fatalError(
					"Could not remove old watcher. This problem might be fixed by a restart.", e);
		}

		// Copy any existing files in the live directory to the backup directory.
		startupScan(liveDirectory);

		// And add a new watcher
		try
		{
			watchId = JNotify.addWatch(liveDirectory.toString(), JNotify.FILE_CREATED
					| JNotify.FILE_DELETED | JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED, true,
					new Watcher());
		}
		catch(JNotifyException e)
		{
			Errors.fatalError("Could not add a file watcher.", e);
		}
	}

	/**
	 * Changes the backup directory path. The contents of the existing backup directory will be
	 * copied to the new directory, and the file pointing to the location of the backup directory
	 * will be updated.
	 * 
	 * The previous backup directory is NOT deleted, but will be left as a dangling folder. Perhaps
	 * the user should be prompted if they want to delete that old backup directory.
	 * 
	 * This does not check if the backup directory is valid (eg, not a child of the live directory).
	 * That is a pre-condition.
	 * 
	 * As of the time of implementation, this is currently not called from anywhere. It is expected
	 * to be added to future versions of the FrontEnd.
	 * 
	 * @param newDirectory
	 */
	public static void changeBackupDirectory(Path newDirectory)
	{
		// Iterate through all files in the backup folder, copying them to the new backup directory
		for(File child : backupDirectory.toFile().listFiles())
		{
			if(child.isDirectory())
			{
				FileOp.copy(child.toPath(), newDirectory.resolve(child.getName()));
			}
			else
			{
				FileOp.copy(child.toPath(), newDirectory);
			}
		}

		backupDirectory = newDirectory;

		// Write the backup path to the disk
		FileOutputStream out;
		try
		{
			out = new FileOutputStream("backup_location");
			out.write(Control.backupDirectory.toString().getBytes());
			out.close();

			Control.logger.info("Backup location file modified, set to: "
					+ Control.backupDirectory.toString());
		}
		catch(IOException e)
		{
			Errors.fatalError(
					"Could not write backup path to disk. Is the program folder writeable?", e);
		}
	}
}
