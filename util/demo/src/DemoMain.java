// ==============================================================
//	This file is part of FBMS (https://code.google.com/p/fbms)
//
//	Copyright (C) 2013 Group 06
//
//	You can redistribute this code and/or modify it under
//	the terms of the GNU General Public License as published
//	by the Free Software Foundation; either version 3 of the
//	License, or (at your option) any later version
// ==============================================================

import net.contentobjects.jnotify.*;
import java.sql.*;
import java.util.*;
import difflib.*;
import org.apache.log4j.*;
import java.nio.*;
import java.nio.file.*;
import java.io.*;

class DemoMain
{
	// Create the logger (takes in a class name for init)
	static Logger logger = Logger.getLogger(DemoMain.class);

	public static void main(String args[])
	{
		loggingStuff();
		databaseStuff();
		watcherStuff();
		diffStuff();

		System.out.println("\n=============================================\n"
			+ "Press enter to exit program. In the meantime,\n"
			+ "file system is still being monitored."
			+ "\n=============================================\n");

		// Wait for input
		Scanner sc = new Scanner(System.in);
		sc.nextLine();

		logger.info("EOF");
	}

	/////////////////////////////////////////////////////////////////
	public static void loggingStuff()
	{
		// Load the properties file for logger settings
		PropertyConfigurator.configure("log4j.properties");

		// And print a test message
		System.out.println("Logging started. See file \"example.log\".\n");
		logger.info("Started logging");
	}

	/////////////////////////////////////////////////////////////////
	public static void databaseStuff()
	{
		// Create database manager, init the table, and get the rows
		DemoDbManager db = new DemoDbManager();
		db.createTable();
		List<DemoContainer> rows = db.getRows();

		System.out.println("Reading database:");

		// Read the list of rows returned by the select query
		for(DemoContainer row : rows)
		{
			System.out.println("\tID:   " + row.id);
			System.out.println("\tName: " + row.name);
		}

		// Database should be closed when done with it
		db.closeDb();
	}

	/////////////////////////////////////////////////////////////////
	public static void watcherStuff()
	{
		// Path to either the current directory
		String path = System.getProperty("user.dir");

		// All types of file modifications watched (although we don't need deleted files)
		int mask =  JNotify.FILE_CREATED | 
					JNotify.FILE_DELETED | 
					JNotify.FILE_MODIFIED| 
					JNotify.FILE_RENAMED;
		// Subfolders too
		boolean watchSubtree = true;
		// Create listener
		DemoJnotifyListener listener = new DemoJnotifyListener();

		System.out.println("\nMonitoring file system, output to console.");

		try
		{
			// Watch for changes in the dir (see listener for what happens when
			// changes occur)
			int watchID = JNotify.addWatch(path, mask, watchSubtree, listener);
		}
		catch(Exception e)
		{
			// Send error messages to the logger
			logger.error(e);
		}

		try
		{
			// Sleep to give the (concurrent) JNotify time to notice changes (ie, so
			// the program doesn't open, run, and immediately close)
			Thread.sleep(1000);
		}
		catch(Exception e)
		{
			logger.error(e);
		}
	}

	/////////////////////////////////////////////////////////////////
	public static void diffStuff()
	{
		// Get the path of current directory
		Path currentDir = Paths.get(System.getProperty("user.dir"));

		// Manipulate the paths to point to specific files.
		Path pathToFileA = currentDir.resolve("../src/DemoMain.java");
		Path pathToFileB = currentDir.resolve("../src/DemoDbManager.java");

		// Note in the logger that we called normalize() so that we'd get something
		// like "util/demo/src" and not "util/demo/build/../src". Same path, but
		// one is easier for a human to read.
		logger.debug("Path to file A: "	+ pathToFileA.normalize());
		logger.debug("Path to file B: "	+ pathToFileB.normalize());

		System.out.println("\nCreating diffs and patching.");

		// Create a diff (note that it takes in lists of Strings)
		List<String> fileA = DemoDiff.fileToList(pathToFileA);
		List<String> fileB = DemoDiff.fileToList(pathToFileB);
		Patch<String> patch = DiffUtils.diff(fileA, fileB);

		try
		{
			// Create a PrintWriter for writing to a file
			PrintWriter writer = new PrintWriter("difference.patch", "UTF-8");
			// And loop through the diff to get all lines and print them to the patch file
			for(Delta<String> delta : patch.getDeltas())
			{
				writer.println(delta);
			}
			writer.close();
		}
		catch(IOException e)
		{
			logger.error(e);
		}

		try
		{
			// And apply the patch to File A to recover file B
			List<String> result = DiffUtils.patch(fileA, patch);

			// And write the recreated file to a physical file
			PrintWriter writer = new PrintWriter("recreated.java", "UTF-8");
			for(String line : result)
			{
				writer.println(line);
			}
			writer.close();
		}
		catch(Exception e)
		{
			logger.error(e);
		}
	}
}