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

import static org.junit.Assert.assertTrue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import net.contentobjects.jnotify.JNotify;

import org.junit.Test;

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
		Control.backupDirectory = Paths.get("").toAbsolutePath();
		DbManager.init();

		// Get the folder contents of this directory
		List<FileInfo> list = Data.getFolderContents(Paths.get("").toAbsolutePath());

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
		DbManager.close();
		Files.delete(Control.backupDirectory.resolve(".revisions.db"));
	}

	// Test setting and getting config
	@Test
	public void dbManagerGetSetConfig() throws Exception
	{
		// Create database in current directory
		Control.backupDirectory = Paths.get("").toAbsolutePath();
		DbManager.init();

		// Try to change the live directory
		DbManager.setConfig("liveDirectory", "/some/other/path");

		// Verify it worked
		assertTrue(DbManager.getConfig("liveDirectory").equals("/some/other/path"));

		// Cleanup
		DbManager.close();
		Files.delete(Control.backupDirectory.resolve(".revisions.db"));
	}

	// Test the watcher detecting different types of file changes
	@Test
	public void watcher() throws Exception
	{
		// Create the watcher
		JNotify.addWatch(Paths.get("").toAbsolutePath().toString(), JNotify.FILE_CREATED
				| JNotify.FILE_DELETED | JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED, true,
				new Watcher());

		// Test creations
		Path createdFile = Files.createFile(Paths.get("").toAbsolutePath().resolve("tempfile1234"));
		Thread.sleep(50); // Delay so that JNotify has time to spot the modification
		assertTrue(createdFile.equals(Control.createdFiles.get(0)));

		// Test modifications
		FileOutputStream out = new FileOutputStream(createdFile.toFile());
		out.write("Hello World".getBytes());
		out.close();
		Thread.sleep(50);

		// Not guaranteed that this file will have been the only one modified, as the log file
		// is often modified first, thus, search till we find it
		boolean foundMatch = false;
		for(Path path : Control.modifiedFiles)
		{
			if(path.equals(createdFile))
			{
				foundMatch = true;
			}
		}
		assertTrue(foundMatch);

		// Test renaming
		Files.move(createdFile, createdFile.resolveSibling("tempfile5678"));
		Thread.sleep(50);
		assertTrue(createdFile.equals(Control.renamedFiles.get(0).oldName));
		assertTrue(createdFile.resolveSibling("tempfile5678").equals(
				Control.renamedFiles.get(0).newName));

		// Test deletion
		createdFile = createdFile.resolveSibling("tempfile5678");
		Files.delete(createdFile);

		Thread.sleep(50);
		assertTrue(createdFile.equals(Control.deletedFiles.get(0)));
	}

	// Test converting between live and backup directory paths
	@Test
	public void fileOpConvertPath()
	{
		// Manually setup
		Control.backupDirectory = Paths.get("").toAbsolutePath().resolve("lib");
		Control.liveDirectory = Paths.get("").toAbsolutePath().resolve("../util/demo/lib");

		// Path is in backup directory, so should be converted to live directory path
		assertTrue(FileOp.convertPath(Paths.get("").toAbsolutePath().resolve("lib/jnotify.dll")) != Paths.get(
				"").toAbsolutePath().resolve("../util/demo/lib/jnotify.dll"));

		// Path is in neither the backup nor live directory, so should return null
		assertTrue(FileOp.convertPath(Paths.get("").toAbsolutePath().resolve("../doc/classes.txt")) == null);
	}

	// Test copying and deleting folders with files inside
	@Test
	public void fileOpCopyFolder() throws IOException
	{
		// Copy the source directory into a new directory named test
		Path path = Paths.get("").toAbsolutePath();
		FileOp.copy(path.resolve("src"), path.resolve("test"));

		assertTrue(path.resolve("test").toFile().exists());

		// And delete it
		FileOp.delete(path.resolve("test"));

		assertTrue(!path.resolve("test").toFile().exists());
	}

	// Test copying and deleting a single file
	@Test
	public void fileOpCopyFile() throws IOException
	{
		// Copy the authors.txt file into a new folder named test
		Path path = Paths.get("").toAbsolutePath();
		FileOp.copy(path.resolve("authors.txt"), path.resolve("test"));

		assertTrue(path.resolve("test/authors.txt").toFile().exists());

		// And delete it, then the folder
		FileOp.delete(path.resolve("test/authors.txt"));
		assertTrue(!path.resolve("test/authors.txt").toFile().exists());

		FileOp.delete(path.resolve("test"));
	}

	// Test copying multiple files from the live directory to the backup directory
	@Test
	public void fileOpCopyMultiple() throws IOException
	{
		// Manual setup (this copy method won't create folders, as the live directory is presumed to
		// exist)
		Path path = Paths.get("").toAbsolutePath();
		Control.backupDirectory = path;
		Control.liveDirectory = path.resolve("../test").normalize();
		Files.createDirectory(path.resolve("../test"));

		// Create the list of files to copy
		List<Path> filesToCopy = new LinkedList<>();
		filesToCopy.add(path.resolve("src/cmpt370/fbms/Control.java"));
		filesToCopy.add(path.resolve("src/log4j.xml"));
		filesToCopy.add(path.resolve("README.txt"));

		FileOp.copy(filesToCopy);

		// And check that they're all there
		assertTrue(path.resolve("../test/src/cmpt370/fbms/Control.java").toFile().exists());
		assertTrue(path.resolve("../test/src/log4j.xml").toFile().exists());
		assertTrue(path.resolve("../test/README.txt").toFile().exists());

		// Cleanup
		FileOp.delete(path.resolve("../test").normalize());
		assertTrue(!path.resolve("../test").normalize().toFile().exists());
	}
}
