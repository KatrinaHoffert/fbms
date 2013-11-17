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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import junit.framework.TestCase;
import cmpt370.fbms.DataRetriever;
import cmpt370.fbms.DbConnection;
import cmpt370.fbms.FileInfo;
import cmpt370.fbms.Main;

/**
 * This class runs tests that can be automated. There must not be any output that has to be examined
 * or such. This allows us to simply run the file and JUnit will report a success or failure.
 */
public class TesterServices extends TestCase
{
	DbConnection db;

	// Set up DbConnection for test
	public void setUp()
	{
		Main.backupDirectory = Paths.get("").toAbsolutePath();
		Main.liveDirectory = Paths.get("").toAbsolutePath();
		db = DbConnection.getInstance();
		db.initConnection();
	}

	// Test getting folder contents
	public void testDataGetFolderContents() throws IOException
	{

		// Get the folder contents of this directory
		DataRetriever revisionRetriever = new DataRetriever(Paths.get("").toAbsolutePath());
		List<FileInfo> list = revisionRetriever.getFolderContents();

		// Find the readme and assert that the information on it is logical
		boolean foundReadme = false;
		for(FileInfo file : list)
		{
			if(file.fileName.equals("README.txt"))
			{
				foundReadme = true;
				assertTrue(file.fileSize > 0);
				assertTrue(!file.folder);
				assertTrue(file.lastAccessedDate != 0);
				assertTrue(file.lastModifiedDate != 0);
				assertTrue(file.createdDate != 0);
			}
		}

		assertTrue(foundReadme);

	}

	public void testDbConnectionGetSetConfig() throws Exception
	{
		// Try to change the live directory
		db.setConfig("liveDirectory", "/some/other/path");

		// Verify it worked
		assertTrue(db.getConfig("liveDirectory").equals("/some/other/path"));

	}


	// Close Dbconnection and clean up
	public void tearDown()
	{
		db.close();
		try
		{
			Files.delete(Main.backupDirectory.resolve(".revisions.db"));
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
