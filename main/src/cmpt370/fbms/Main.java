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

import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;

import org.apache.log4j.Logger;

import cmpt370.fbms.gui.FrontEnd;

/**
 * Main class for handling the setup of the program. Starts everything up and initiates the file
 * handlers.
 * 
 * Groups together commonly used global variables, such as the shared file lists, directories, and
 * the logger.
 */
public class Main
{
	// Various public variables shared amongst components
	public static Path liveDirectory = null;
	public static Path backupDirectory = null;

	// Instance variable for singleton pattern
	private static Main instance = null;

	// Logger object is linked to the class
	public static Logger logger = Logger.getLogger(Main.class);

	// Lists for file changes
	private final List<Path> createdFiles = Collections.synchronizedList(new LinkedList<Path>());
	private final List<Path> modifiedFiles = Collections.synchronizedList(new LinkedList<Path>());
	private final List<RenamedFile> renamedFiles = Collections.synchronizedList(new LinkedList<RenamedFile>());
	private final List<Path> deletedFiles = Collections.synchronizedList(new LinkedList<Path>());

	private boolean firstRunWizardDone = false;
	private int watchId = 0;
	private long loop = 0;

	/**
	 * The main method runs the startup code, initializing the database, checking for the first run,
	 * etc. The main method then loops through the watched files at intervals, checking for changes
	 * and acting appropriately.
	 * 
	 * @param args
	 *            Command line arguments, not currently used
	 */
	public static void main(String[] args)
	{
		// Perform startup and initialization
		Startup startup = new Startup();
		startup.startup();
		startup.startupScan(liveDirectory);

		// Start the file handler
		Main.getInstance().fileHandler();

		// Create the GUI
		FrontEnd.initGui();
	}

	/**
	 * Instantiation not allowed, use Main.getInstance().
	 */
	private Main()
	{}

	/**
	 * Gets an instance of the Main class (singleton pattern).
	 * 
	 * @return The single instance of the Main object.
	 */
	public static synchronized Main getInstance()
	{
		if(instance == null)
		{
			instance = new Main();
		}
		return instance;
	}

	/**
	 * Creates the watcher.
	 */
	public void createWatcher()
	{
		// JNotify watcher for files. The live directory is watched for all four types of files
		// changes: creations, deletions, modifications, and renaming. We watch subfolders of the
		// live directory for changes as well. The Watcher class forms the listener for these
		// changes
		try
		{
			watchId = JNotify.addWatch(Main.liveDirectory.toString(), JNotify.FILE_CREATED
					| JNotify.FILE_DELETED | JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED, true,
					new Watcher(createdFiles, modifiedFiles, renamedFiles, deletedFiles));
		}
		catch(JNotifyException e)
		{
			Errors.fatalError("Could not start file watcher module", e);
		}
	}

	/**
	 * Removes the watcher.
	 */
	public void removeWatcher()
	{
		try
		{
			JNotify.removeWatch(watchId);
		}
		catch(JNotifyException e)
		{
			Errors.fatalError(
					"Could not remove old watcher. This problem might be fixed by a restart.", e);
		}
	}

	/**
	 * Checks if the first run wizard has been completed.
	 * 
	 * @return True if first run wizard has been done in the past.
	 */
	public boolean getFirstRunWizardDone()
	{
		return firstRunWizardDone;
	}

	/**
	 * Sets whether the first run wizard has been completed.
	 * 
	 * @param done
	 *            True if we've completed the wizard.
	 */
	public void setFirstRunWizardDone(boolean done)
	{
		firstRunWizardDone = done;
	}

	/**
	 * This method sets up the loop for going through the array lists that the Watcher module
	 * populates. It also creates an infinite loop in a separate thread, allowing the program to run
	 * indefinitely in the background.
	 */
	private void fileHandler()
	{
		// Run this stuff in a new thread
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					FileChangeHandlers handlers = new FileChangeHandlers(createdFiles,
							modifiedFiles, renamedFiles, deletedFiles);
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
										handlers.validateLists();
										handlers.handleDeletedFiles();
										handlers.handleCreatedFiles();
										handlers.handleModifiedFiles();
										handlers.handleRenamedFiles();
									}
								}
							}
						}

						// Every 500 loops, we run the command to trim the database
						if(loop % 500 == 0)
						{
							DbManager db = DbManager.getInstance();
							db.trimDatabase();
						}

						// Time to wait before "polling" the file lists again.
						Thread.sleep(1000);
					}
				}
				catch(InterruptedException e)
				{
					Errors.fatalError("Thread was interupted", e);
				}
			};
		}.start();
	}
}
