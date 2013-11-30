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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import junit.framework.TestCase;
import cmpt370.fbms.DbConnection;
import cmpt370.fbms.FileOp;
import cmpt370.fbms.Main;
import cmpt370.fbms.RevisionInfo;

/**
 * This class runs tests that can be automated. There must not be any output that has to be examined
 * or such. This allows us to simply run the file and JUnit will report a success or failure.
 */
public class TesterDbConnection extends TestCase
{
	DbConnection db;
	Path path;

	// Prepare DB and path.
	public void setUp()
	{
		Main.backupDirectory = Paths.get("").toAbsolutePath();
		Main.liveDirectory = Paths.get("").toAbsolutePath();
		path = Paths.get("").toAbsolutePath();
		db = DbConnection.getInstance();
		db.initConnection();
	}


	public void test_GetSetConfig() throws Exception
	{
		// Try to change the live directory
		db.setConfig("liveDirectory", "/some/other/path");

		// Verify it worked
		assertTrue(db.getConfig("liveDirectory").equals("/some/other/path"));

	}

	// Insert a revision and then obtain it
	public void test_InsertGetRevision() throws IOException
	{
		// Insert a "revision" with filler content
		db.insertRevision(path.resolve("README.txt"),
				FileOp.fileToString(path.resolve("README.txt")), null, 100, 50);

		// Then we should get it.
		List<RevisionInfo> insertedInfos = db.getFileRevisions(path.resolve("README.txt"));
		assertNotNull("Obtaining info of README.txt failed, check insert/getRevision",
				insertedInfos);

	}

	// Rename the revision we inserted.
	public void test_RenameRevision()
	{
		// Rename that revision
		db.renameRevisions(path.resolve("README.txt"), "not-readme.txt");

		// Obtain it
		List<RevisionInfo> list = db.getFileRevisions(path.resolve("not-readme.txt"));
		assertNotNull("Renaming from README.txt to not-readme.txt failed", list);
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
