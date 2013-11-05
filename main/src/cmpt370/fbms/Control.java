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
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

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
	public static List<Path> createdFiles = new LinkedList<>();
	public static List<Path> modifiedFiles = new LinkedList<>();
	public static List<RenamedFile> renamedFiles = new LinkedList<>();
	public static List<Path> deletedFiles = new LinkedList<>();
	public static boolean firstRunWizardDone = false;

	// Logger object is linked to the class
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

			logger.info("First run wizard completed");
		}
		else
		{
			// For subsequent runs, the backup directory has already been set and the live
			// directory will be retreived during DbManager.init()
			DbManager.init();

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

		// TODO: Expand this to prompt the user for a course of action.
		if(directory.toFile().listFiles() == null)
		{
			Errors.fatalError("The live directory cannot be found!");
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
					Path diffFile = FileOp.createDiff(FileOp.convertPath(file.toPath()),
							file.toPath());
					// Difference in file sizes
					long delta = FileOp.fileSize(FileOp.convertPath(file.toPath()))
							- FileOp.fileSize(file.toPath());

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

						handleDeletedFiles();
						handleCreatedFiles();
						handleModifiedFiles();
						handleRenamedFiles();

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

	private static void handleCreatedFiles()
	{

	}

	private static void handleModifiedFiles()
	{

	}

	private static void handleRenamedFiles()
	{

	}

	private static void handleDeletedFiles()
	{

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
			// Get the revision we want and make a diff for it
			Path revertedFile = FileHistory.obtainRevision(file, timestamp);
			Path diffFromCurrent = FileOp.createDiff(file, revertedFile);

			// Get the filesize of our newly reverted file as well as the delta from the old file
			long fileSize = FileOp.fileSize(revertedFile);
			long delta = fileSize - FileOp.fileSize(file);

			// Store the revision and copy the reverted file over the backup directory
			FileHistory.storeRevision(FileOp.convertPath(file), diffFromCurrent, fileSize, delta);
			FileOp.copy(revertedFile, file);

			// Finally, copy that backup directory copy to the live directory
			FileOp.copy(file, FileOp.convertPath(file).getParent());
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
