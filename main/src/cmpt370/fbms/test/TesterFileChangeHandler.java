package cmpt370.fbms.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;
import cmpt370.fbms.DbConnection;
import cmpt370.fbms.FileChangeHandlers;
import cmpt370.fbms.Main;
import cmpt370.fbms.RenamedFile;

public class TesterFileChangeHandler extends TestCase
{
	// Database object for test.
	protected DbConnection db;
	// Main object for test.
	protected Main main;

	protected Set<Path> createdFiles = Collections.synchronizedSet(new LinkedHashSet<Path>());
	protected Set<Path> modifiedFiles = Collections.synchronizedSet(new LinkedHashSet<Path>());
	protected Set<RenamedFile> renamedFiles = Collections.synchronizedSet(new LinkedHashSet<RenamedFile>());
	protected Set<Path> deletedFiles = Collections.synchronizedSet(new LinkedHashSet<Path>());

	protected FileChangeHandlers testHandler;

	@Override
	public void setUp()
	{
		// Create FileChangeHandlers object

		testHandler = new FileChangeHandlers(createdFiles, modifiedFiles, renamedFiles,
				deletedFiles);
		File testFile = null;
		main = Main.getInstance();

		// Create test live folder.
		Main.liveDirectory = Paths.get("FileHandlerTest/live");
		testFile = Main.liveDirectory.toFile();
		testFile.mkdirs();

		// Create test backup folder.
		Main.backupDirectory = Paths.get("FileHandlerTest/backup");
		testFile = Main.backupDirectory.toFile();
		testFile.mkdirs();

		// Initialize database.
		db = DbConnection.getInstance();
		db.initConnection();
		// db.setConfig("trimDate", "-1");
		// db.setConfig("startupScan", "true");
		// db.setConfig("disableNonFatalErrors", "false");
		// db.setConfig("maxSize", "5");
		// db.close();


	}

	public void test_handleCreatedFiles()
	{
		// Create a blank file for test.
		try
		{
			Files.createFile(Paths.get(Main.liveDirectory.toString(), "TestFile1"));
		}
		catch(IOException e)
		{}

		db.initConnection();
		// Add test file to list
		createdFiles.add(Paths.get(Main.liveDirectory.toString(), "TestFile1"));

		// Call handler to handle this.
		testHandler.handleCreatedFiles();

		// This file should be in backup folder now.

		assertTrue("File is not copied", new File("FileHandlerTest/backup/TestFile1").exists());

		// Test file should be removed, the list should be empty.
		assertTrue("Nothing should be in created files list", createdFiles.isEmpty());

		// Delete the file in backup folder.
		try
		{
			Files.deleteIfExists(new File("FileHandlerTest/backup/TestFile1").toPath());
		}
		catch(IOException e)
		{
			System.out.println("I/O Error.\nTest may behaves incorrectly.");
			e.printStackTrace();
		}

		// Add same file to renamed lists.
		RenamedFile file = new RenamedFile();
		file.newName = Paths.get(Main.liveDirectory.toString(), "TestFile1");
		file.oldName = Paths.get(Main.liveDirectory.toString(), "TestFile1");
		renamedFiles.add(file);

		// Add this file to list
		createdFiles.add(Paths.get(Main.liveDirectory.toString(), "TestFile1"));

		// Call handler to handle this.
		testHandler.handleCreatedFiles();

		// The file should not be copied.
		assertFalse("File should not be in backup folder", new File(
				"FileHandlerTest/backup/TestFile1").exists());

		// Add same file to modified lists.
		modifiedFiles.add(Paths.get(Main.liveDirectory.toString(), "TestFile1"));

		// Add this file to list
		createdFiles.add(Paths.get(Main.liveDirectory.toString(), "TestFile1"));

		// Call handler to handle this.
		testHandler.handleCreatedFiles();

		// The file should not be copied.
		assertFalse("File should not be in backup folder", new File(
				"FileHandlerTest/backup/TestFile1").exists());
		assertTrue("The file should not be in modifiedFiles.", modifiedFiles.isEmpty());

		// Delete the file in live folder.
		try
		{
			Files.deleteIfExists(new File("FileHandlerTest/live/TestFile1").toPath());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		clearLists();
	}

	public void test_handleDeletedFiles()
	{
		// Add items to lists.
		Path testPath = null;
		testPath = Paths.get(Main.liveDirectory.toString(), "TestFile1");

		createdFiles.add(testPath);
		modifiedFiles.add(testPath);
		deletedFiles.add(testPath);

		RenamedFile renamedFile = new RenamedFile();
		renamedFile.oldName = testPath;
		renamedFiles.add(renamedFile);

		testPath = Paths.get(Main.liveDirectory.toString(), "TestFile2");

		modifiedFiles.add(testPath);
		deletedFiles.add(testPath);

		renamedFile = new RenamedFile();
		renamedFile.oldName = testPath;
		renamedFiles.add(renamedFile);

		testPath = Paths.get(Main.liveDirectory.toString(), "TestFile3");

		modifiedFiles.add(testPath);
		deletedFiles.add(testPath);

		renamedFile = new RenamedFile();
		renamedFile.oldName = testPath;
		renamedFiles.add(renamedFile);

		testPath = Paths.get(Main.liveDirectory.toString(), "TestFile4");

		deletedFiles.add(testPath);

		renamedFile = new RenamedFile();
		renamedFile.oldName = testPath;
		renamedFiles.add(renamedFile);

		// handle list
		testHandler.handleDeletedFiles();


		assertTrue("createdFiles is not cleared.", createdFiles.isEmpty());
		assertTrue("deletedFiles is not cleared.", deletedFiles.isEmpty());
		assertTrue("modifiedFiles is not cleared.", modifiedFiles.isEmpty());
		assertTrue("renamedFiles is not cleared.", renamedFiles.isEmpty());

		clearLists();
	}

	public void test_handleModifiedFiles()
	{
		db.initConnection();
		// Create a blank file for test.
		try
		{
			Files.createFile(Paths.get(Main.liveDirectory.toString(), "TestFile1"));
		}
		catch(IOException e)
		{
			System.out.println("I/O Error.\nTest may behaves incorrectly.");
			e.printStackTrace();
		}

		// Add the file to list and handle it
		modifiedFiles.add(Paths.get(Main.liveDirectory.toString(), "TestFile1"));
		testHandler.handleModifiedFiles();

		// This file should be in backup folder now.
		assertTrue("File is not copied", new File("FileHandlerTest/backup/TestFile1").exists());

		// Delete the file in folder.
		try
		{
			Files.deleteIfExists(new File("FileHandlerTest/live/TestFile1").toPath());
			Files.deleteIfExists(new File("FileHandlerTest/backup/TestFile1").toPath());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		clearLists();

	}

	public void test_handleRenamedFiles()
	{
		db.initConnection();
		// Create a blank file for test.
		try
		{
			Files.createFile(Paths.get(Main.liveDirectory.toString(), "TestFile30"));
		}
		catch(IOException e)
		{
			System.out.println("I/O Error.\nTest may behaves incorrectly.");
			e.printStackTrace();
		}

		RenamedFile r = new RenamedFile();
		r.oldName = Paths.get(Main.liveDirectory.toString(), "TestFile20");
		r.newName = Paths.get(Main.liveDirectory.toString(), "TestFile30");

		renamedFiles.add(r);

		testHandler.handleRenamedFiles();

		// This file should be in backup folder now.
		assertTrue("File is not copied", new File("FileHandlerTest/backup/TestFile30").exists());
	}

	// clear lists for next list.
	private void clearLists()
	{
		createdFiles.clear();
		modifiedFiles.clear();
		deletedFiles.clear();
		renamedFiles.clear();
		// db.close();
	}

	public void tearDown()
	{
		db.close();
		Path start = Paths.get("FileHandlerTest");
		try
		{
			Files.walkFileTree(start, new SimpleFileVisitor<Path>()
			{
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
						throws IOException
				{
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException e)
						throws IOException
				{
					if(e == null)
					{
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}
					else
					{
						// directory iteration failed
						throw e;
					}
				}
			});
		}
		catch(IOException e)
		{}

	}
}
