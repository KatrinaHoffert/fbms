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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import cmpt370.fbms.DataRetriever;
import cmpt370.fbms.DbManager;
import cmpt370.fbms.FileInfo;
import cmpt370.fbms.FileOp;
import cmpt370.fbms.Main;

/**
 * This class runs tests that can be automated. There must not be any output that has to be examined
 * or such. This allows us to simply run the file and JUnit will report a success or failure.
 */
public class TesterServices
{
	// Test getting folder contents
	@Test
	public void dataGetFolderContents() throws IOException
	{
		// Have to manually do the startup
		Main.backupDirectory = Paths.get("").toAbsolutePath();
		DbManager db = DbManager.getInstance();
		db.initConnection();

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

		// Manually clean up
		db.close();
		Files.delete(Main.backupDirectory.resolve(".revisions.db"));
	}

	// Test setting and getting config
	@Test
	public void dbManagerGetSetConfig() throws Exception
	{
		// Create database in current directory
		Main.backupDirectory = Paths.get("").toAbsolutePath();
		DbManager db = DbManager.getInstance();
		db.initConnection();

		// Try to change the live directory
		db.setConfig("liveDirectory", "/some/other/path");

		// Verify it worked
		assertTrue(db.getConfig("liveDirectory").equals("/some/other/path"));

		// Cleanup
		db.close();
		Files.delete(Main.backupDirectory.resolve(".revisions.db"));
	}

	// Test converting between live and backup directory paths
	@Test
	public void fileOpConvertPath()
	{
		// Manually setup
		Main.backupDirectory = Paths.get("").toAbsolutePath().resolve("lib");
		Main.liveDirectory = Paths.get("").toAbsolutePath().resolve("../util/demo/lib");

		// Path is in backup directory, so should be converted to live directory path
		assertTrue(FileOp.convertPath(Paths.get("").toAbsolutePath().resolve("lib/jnotify.dll")) != Paths.get(
				"").toAbsolutePath().resolve("../util/demo/lib/jnotify.dll"));

		// Path is in neither the backup nor live directory, so should return null
		assertTrue(FileOp.convertPath(Paths.get("").toAbsolutePath().resolve("../doc/classes.txt")) == null);
	}

	// Test file equivalence
	@Test
	public void fileOpIsEqual() throws IOException
	{
		Path path = Paths.get("").toAbsolutePath();

		// Compare some files
		assertTrue(FileOp.isEqual(path.resolve("authors.txt"), path.resolve("authors.txt")));
		assertTrue(!FileOp.isEqual(path.resolve("authors.txt"), path.resolve("license.txt")));
	}

	@Test
	public void fileOpApplyDiff() throws IOException
	{
		Path original = Paths.get("").resolve("authors.txt");
		Path modified = Paths.get("").resolve("README.txt");

		// Create the diff
		Path diff = FileOp.createPatch(original, modified);

		// Apply the diff
		Path applied = FileOp.applyPatch(modified, diff);

		assertTrue(FileOp.isEqual(original, applied));
	}
}
