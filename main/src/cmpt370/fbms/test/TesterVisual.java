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

package cmpt370.fbms.test;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.junit.Ignore;
import org.junit.Test;

import cmpt370.fbms.DataRetriever;
import cmpt370.fbms.DbManager;
import cmpt370.fbms.Errors;
import cmpt370.fbms.FileHistory;
import cmpt370.fbms.FileInfo;
import cmpt370.fbms.FileOp;
import cmpt370.fbms.Main;
import cmpt370.fbms.RevisionInfo;

/**
 * This class runs tests on portions of the program that cannot be automatically tested for
 * correctness, such as the GUI and various visual components.
 */
public class TesterVisual
{
	// Get folder contents
	@Test
	@Ignore
	public void dataGetFolderContents() throws IOException
	{
		// Manual setup
		Path path = Paths.get("").toAbsolutePath();
		Main.backupDirectory = path;
		DbManager.initConnection();

		// Get the contents of this directory
		List<FileInfo> list = DataRetriever.getFolderContents(path);


		// And print out what we know
		for(FileInfo file : list)
		{
			System.out.println(file.fileName);
			System.out.println("\tCreated:\t" + new Date(file.createdDate * 1000));
			System.out.println("\tLast modified:\t" + new Date(file.lastModifiedDate * 1000));
			System.out.println("\tLast accessed:\t" + new Date(file.lastAccessedDate * 1000));
			if(!file.folder)
			{
				System.out.println("\tSize:\t" + file.fileSize + " B");
				System.out.println("\tRevision count:\t" + file.numberOfRevisions);
				System.out.println("\tRevisions size:\t" + file.revisionSizes + " B");
			}
		}

		// Cleanup
		Files.delete(path.resolve(".revisions.db"));
		DbManager.close();
	}


	// Insert a revision, rename it, and then obtain it
	@Test
	public void dbManagerInsertRevision() throws IOException
	{
		// Setup
		Path path = Paths.get("").toAbsolutePath();
		Main.backupDirectory = path;
		Main.liveDirectory = path;
		DbManager.initConnection();

		// Print out the database size
		System.out.println("\n--------------------------------");
		System.out.println("Size of database before: "
				+ FileOp.fileSize(path.resolve(".revisions.db")));

		// Insert a "revision" with filler content
		DbManager.insertRevision(path.resolve("README.txt"),
				FileOp.fileToString(path.resolve("README.txt")), null, 100, 50);

		// Now rename that revision
		DbManager.renameRevisions(path.resolve("README.txt"), "not-readme.txt");

		// Finally, obtain it and print it out
		List<RevisionInfo> list = DbManager.getFileRevisions(path.resolve("not-readme.txt"));
		for(RevisionInfo revision : list)
		{
			System.out.println("Found revision id = " + revision.id);
			System.out.println(revision.path);
			System.out.println("Time stamp: " + revision.time + "; Delta: " + revision.delta);
			System.out.println(revision.diff);
		}

		// And verify the database size has increased
		System.out.println("Size of database after: "
				+ FileOp.fileSize(path.resolve(".revisions.db")));

		// Cleanup
		Files.delete(path.resolve(".revisions.db"));
		DbManager.close();
	}

	// Demonstrates a fatal error with just a message
	@Test
	@Ignore
	public void errorsFatalError()
	{
		Errors.fatalError("She turned me into a newt!");
	}

	// Demonstrates a fatal error that also has a stack trace included (see also: the log)
	@Test
	@Ignore
	@SuppressWarnings("null")
	public void errorsFatalErrorWithStackTrace()
	{
		String someString = null;

		try
		{
			someString.charAt(0); // Throws an exception
		}
		catch(NullPointerException e)
		{
			Errors.fatalError("We dun goofed...", e);
		}
	}

	// Demonstrates a non-fatal error message
	@Test
	@Ignore
	public void errorsNonfatalError() throws InterruptedException
	{
		Errors.nonfatalError("Such error message!<br />&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;"
				+ "&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;So recovery!<br />&emsp;&emsp;Wow!", "Wow!");

		// Sleep so the program doesn't instantly terminate
		Thread.sleep(5000);
	}

	@Test
	@Ignore
	public void fileOpCreatDiff() throws IOException
	{
		Path diff = FileOp.createPatch(Paths.get("").resolve("authors.txt"),
				Paths.get("").resolve("README.txt"));

		FileReader reader = new FileReader(diff.toFile());

		int r;
		while((r = reader.read()) != -1)
		{
			System.out.print((char) r);
		}

		reader.close();
	}

	@Test
	@Ignore
	public void dataGetRevisionInfo()
	{
		// Initialize test
		Path path = Paths.get("").toAbsolutePath();
		Main.backupDirectory = path;
		Main.liveDirectory = path;
		DbManager.initConnection();

		// Insert revisions for a file
		FileHistory.storeRevision(path.resolve("README.txt"), path.resolve("README.txt"), 100, 200);
		FileHistory.storeRevision(path.resolve("README.txt"), path.resolve("license.txt"), 300, 400);
		FileHistory.storeRevision(path.resolve("README.txt"), path.resolve("authors.txt"), 500, 600);

		// Get the revision table for that file
		Vector<Vector<String>> tableData = DataRetriever.getRevisionInfoTable(path.resolve("README.txt"));

		// And print it out
		for(Vector<String> rows : tableData)
		{
			for(String data : rows)
			{
				System.out.println(data);
			}
		}

		// Clean up
		DbManager.close();
		FileOp.delete(path.resolve(".revisions.db"));
	}
}
