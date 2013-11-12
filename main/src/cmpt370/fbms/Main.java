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

import org.apache.log4j.Logger;

import cmpt370.fbms.GUI.FrontEnd;

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
	public static List<Path> createdFiles = Collections.synchronizedList(new LinkedList<Path>());
	public static List<Path> modifiedFiles = Collections.synchronizedList(new LinkedList<Path>());
	public static List<RenamedFile> renamedFiles = Collections.synchronizedList(new LinkedList<RenamedFile>());
	public static List<Path> deletedFiles = Collections.synchronizedList(new LinkedList<Path>());
	public static boolean firstRunWizardDone = false;

	// Logger object is linked to the class
	public static Logger logger = Logger.getLogger(Main.class);

	// Watch ID for JNotify
	public static int watchId = 0;
	public static long loop = 0;

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
		Startup.startup();
		Startup.startupScan(liveDirectory);
		fileHandler();
		FrontEnd.initGui();
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
										FileChangeHandlers.handleDeletedFiles();
										FileChangeHandlers.handleCreatedFiles();
										FileChangeHandlers.handleModifiedFiles();
										FileChangeHandlers.handleRenamedFiles();
									}
								}
							}
						}

						// Every 500 loops, we run the command to trim the database
						if(loop % 500 == 0)
						{
							DbManager.trimDatabase();
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
