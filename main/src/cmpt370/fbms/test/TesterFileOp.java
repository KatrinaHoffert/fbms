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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import cmpt370.fbms.FileOp;
import cmpt370.fbms.Main;

/**
 * Tests functionality related to file operations.
 */
public class TesterFileOp extends TestCase
{

	/**
	 * Prepare Test Environment: All temporary item will be created under folder TestFileOp.
	 * Temporary item: File(s) and Folder(s) for fileSize(Path), fileValid(Path), Copy(Path, Path),
	 * Rename(Path, String), copy(List<Path>) and for delete(Path)
	 * 
	 */
	public void setUp()
	{
		File basePath = new File("TestFileOp");
		File testPath = new File("TestFileOp\\Test1");
		File testBackupPath = new File("TestFileOp\\TestBackup");
		File nestedFile = new File("TestFileOp\\Test1\\ZeroSize.txt");
		File nestedFile1 = new File("TestFileOp\\Test1\\nested\\");
		File nestedFile2 = new File("TestFileOp\\Test1\\nested\\ZeroSize.txt");
		File mZeroFile = new File("TestFileOp\\ZeroSize.txt");
		File mSmallFile = new File("TestFileOp\\SmallSize.txt");
		File mLargeFile = new File("TestFileOp\\LargeSize.txt");
		File mBinFile = new File("TestFileOp\\BinaryFile.bin");
		basePath.mkdir();
		testPath.mkdirs();
		testBackupPath.mkdirs();
		nestedFile1.mkdirs();
		try
		{
			// create all test file needed
			Files.createFile(mZeroFile.toPath());
			Files.createFile(nestedFile.toPath());
			Files.createFile(nestedFile2.toPath());
			PrintWriter smallFile = new PrintWriter(Files.newOutputStream(mSmallFile.toPath()),
					true);
			FileOutputStream fos = new FileOutputStream(mLargeFile);
			FileChannel bigFile = fos.getChannel();
			FileOutputStream binFile = new FileOutputStream(mBinFile);
			// write data in files.
			smallFile.write("Small File Test");
			smallFile.flush();
			smallFile.close();

			bigFile.write(ByteBuffer.allocate(1), 10485760 - 1);
			bigFile.close();
			fos.close();

			byte b[] = new byte[1024];
			(new Random()).nextBytes(b);
			binFile.write(b);
			binFile.close();

		}
		catch(FileAlreadyExistsException e)
		{
			// Do nothing if test file already exists.
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Test FileOp.delete(Path). File to be deleted is TestFileOp\ZeroSize.txt
	 */
	public void testDelete()
	{
		FileOp.delete((new File("TestFileOp\\ZeroSize.txt")).toPath());
		assertFalse("Zero size file size is not deleted.",
				(new File("TestFileOp\\ZeroSize.txt").exists()));
	}

	/**
	 * Test FileOp.delete(Path). File to be deleted is TestFileOp\SmallSize.txt and
	 * TestFileOp\LargeSize.txt
	 */
	public void testfileSize()
	{
		assertEquals("fileSize() for Small file size is wrong. Should be 15.", 15,
				FileOp.fileSize(new File("TestFileOp\\SmallSize.txt").toPath()));

		assertEquals("fileSize() for Large file size is wrong. Should be 10485760.", 10485760,
				FileOp.fileSize(new File("TestFileOp\\LargeSize.txt").toPath()));
	}

	public void testisPlainText()
	{
		assertTrue("fileValid() reported false for small text file.",
				FileOp.isPlainText(new File("TestFileOp\\SmallSize.txt").toPath()));

		assertTrue("fileValid() reported false for large text file.",
				FileOp.isPlainText(new File("TestFileOp\\LargeSize.txt").toPath()));

		assertFalse("fileValid() reported true for binary file.",
				FileOp.isPlainText(new File("TestFileOp\\BinaryFile.bin").toPath()));
	}

	public void testcopySingleFile() throws IOException
	{
		FileOp.copy((new File("TestFileOp\\BinaryFile.bin")).toPath(), (new File(
				"TestFileOp\\Test1")).toPath());

		assertTrue("copy(Path, Path) faild to copy file: TestFileOp\\BinaryFile.bin", (new File(
				"TestFileOp\\Test1\\BinaryFile.bin")).exists());

		FileOp.copy((new File("TestFileOp\\Test1")).toPath(),
				(new File("TestFileOp\\Test2")).toPath());
		assertTrue("Copy folder failed", (new File("TestFileOp\\Test2\\BinaryFile.bin")).exists()
				&& new File("TestFileOp\\Test2\\ZeroSize.txt").exists()
				&& new File("TestFileOp\\Test2\\nested").exists()
				&& new File("TestFileOp\\Test2\\nested\\ZeroSize.txt").exists());
	}

	public void testMultiFiles()
	{
		List<Path> sourcePaths = new ArrayList<>();
		sourcePaths.add(new File("TestFileOp\\SmallSize.txt").toPath());
		sourcePaths.add(new File("TestFileOp\\LargeSize.txt").toPath());
		sourcePaths.add(new File("TestFileOp\\BinaryFile.bin").toPath());

		Main.backupDirectory = new File("TestFileOp\\Test2\\").toPath();
		Main.liveDirectory = new File("TestFileOp\\").toPath();
		FileOp.copy(sourcePaths);

		assertTrue("copy(List<Path>) faild to copy file: TestFileOp\\SmallSize.txt", (new File(
				"TestFileOp\\Test2\\SmallSize.txt")).exists());
		assertTrue("copy(List<Path>) faild to copy file: TestFileOp\\LargeSize.txt", (new File(
				"TestFileOp\\Test2\\LargeSize.txt")).exists());
		assertTrue("copy(List<Path>) faild to copy file: TestFileOp\\BinaryFile.bin", (new File(
				"TestFileOp\\Test2\\BinaryFile.bin")).exists());
	}

	public void testRename()
	{
		FileOp.rename((new File("TestFileOp\\SmallSize.txt")).toPath(), "SmallSize2.txt");
		assertTrue("TestFileOp\\SmallSize.txt is not renamed to SmallSize2.txt", new File(
				"TestFileOp\\SmallSize2.txt").exists());

		FileOp.rename((new File("TestFileOp\\SmallSize2.txt")).toPath(), "SmallSize.txt");
		assertTrue("TestFileOp\\SmallSize2.txt is not renamed to SmallSize.txt", new File(
				"TestFileOp\\SmallSize.txt").exists());
	}

	public void testIsEqual()
	{
		Path path = Paths.get("").toAbsolutePath();

		// Compare some files
		assertTrue(FileOp.isEqual(path.resolve("authors.txt"), path.resolve("authors.txt")));
		assertTrue(!FileOp.isEqual(path.resolve("authors.txt"), path.resolve("license.txt")));
	}

	public void testConvertPath()
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

	public void testPatch()
	{
		Path original = Paths.get("").resolve("authors.txt");
		Path modified = Paths.get("").resolve("README.txt");

		// Create the diff
		Path diff = FileOp.createPatch(original, modified);

		// Apply the diff
		Path applied = FileOp.applyPatch(modified, diff);

		assertTrue(FileOp.isEqual(original, applied));
	}

	@Override
	public void tearDown() throws Exception
	{
		File basePath = new File("TestFileOp");
		Path start = basePath.toPath();
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
			public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException
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

		basePath.delete();

	}

}
