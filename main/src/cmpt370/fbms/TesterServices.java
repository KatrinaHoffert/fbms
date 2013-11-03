/*
	FBMS: File Backup and Management System
	Copyright (C) 2013 Group 06

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package cmpt370.fbms;

import static org.junit.Assert.assertTrue;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import net.contentobjects.jnotify.JNotify;

import org.junit.Test;

/**
 * This class runs tests that can be automated. There must not be any output that has to be examined
 * or such. This allows us to simply run the file and JUnit will report a success or failure.
 */
public class TesterServices
{
	@Test
	public void dataGetFolderContents()
	{
		Control.backupDirectory = Paths.get("").toAbsolutePath();
		DbManager.init();
		List<FileInfo> list = Data.getFolderContents(Paths.get("").toAbsolutePath());

		boolean foundReadme = false;

		for(FileInfo file : list)
		{
			// Ensure we found the readme file and that its traits are logical
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
		DbManager.close();
	}

	@Test
	public void dbManagerGetSetConfig() throws Exception
	{
		// Create database in current directory
		Control.backupDirectory = Paths.get("").toAbsolutePath();
		DbManager.init();

		DbManager.setConfig("liveDirectory", "/some/other/path");
		assertTrue(DbManager.getConfig("liveDirectory").equals("/some/other/path"));
		DbManager.close();

		Files.delete(Control.backupDirectory.resolve(".revisions.db"));
	}

	@Test
	// Note: if test fails, files named tempfile1234 or tempfile5678 may be created in the working
	// directory. If so, those must be deleted for the test to pass on subsequent runs. When
	// successful, the test deletes those files at the end of the test
	public void watcher() throws Exception
	{
		JNotify.addWatch(Paths.get("").toAbsolutePath().toString(), JNotify.FILE_CREATED
				| JNotify.FILE_DELETED | JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED, true,
				new Watcher());

		// Test creations
		Path createdFile = Files.createFile(Paths.get("").toAbsolutePath().resolve("tempfile1234"));
		assertTrue(createdFile.equals(Control.createdFiles.get(0)));

		// Test modifications
		FileOutputStream out = new FileOutputStream(createdFile.toFile());
		out.write("Hello World".getBytes());
		out.close();
		Thread.sleep(50); // Delay so that JNotify has time to spot the modification

		// Not guaranteed that this file will have been the only one modified, as the log file
		// is often modified first
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

	@Test
	public void testConvertPath()
	{
		Control.backupDirectory = Paths.get("").toAbsolutePath().resolve("lib");
		Control.liveDirectory = Paths.get("").toAbsolutePath().resolve("../util/demo/lib");

		assertTrue(FileOp.convertPath(Paths.get("").toAbsolutePath().resolve("lib/jnotify.dll")) != null);
		assertTrue(FileOp.convertPath(Paths.get("").toAbsolutePath().resolve("lib/doesNotExist")) == null);
	}
}
