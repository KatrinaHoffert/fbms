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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;

import org.apache.log4j.Logger;

public class FileOp
{
	private static Logger logger = Control.logger;
	private static String backupPath;
	private static String livePath;

	public static void copy(Path sourceFile, Path destFolder)
	{
		final Path source = sourceFile;
		final Path target = destFolder;
		File mFile = sourceFile.toFile();
		if(mFile.isDirectory())
		{
			// if the given path is a folder...
			try
			{
				// from JDK document
				Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
						Integer.MAX_VALUE, new SimpleFileVisitor<Path>()
						{
							@Override
							public FileVisitResult preVisitDirectory(Path dir,
									BasicFileAttributes attrs) throws IOException
							{
								Path targetdir = target.resolve(source.relativize(dir));
								try
								{
									Files.copy(dir, targetdir);
								}
								catch(FileAlreadyExistsException e)
								{
									if(!Files.isDirectory(targetdir))
										throw e;
								}
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
									throws IOException
							{
								Files.copy(file, target.resolve(source.relativize(file)),
										StandardCopyOption.REPLACE_EXISTING);
								return FileVisitResult.CONTINUE;
							}
						});
			}
			catch(IOException e)
			{
				String warnString = "Could copy source folder " + sourceFile.toString() + "to "
						+ destFolder.toString();
				logger.warn(warnString);
			}
		}
		else
		{
			// if the given Path is a file
			Path liveFolder = (new File(livePath)).toPath();
			Path srcFile = sourceFile.subpath(liveFolder.getNameCount(), sourceFile.getNameCount());
			Path dstFile = new File(new File(backupPath), srcFile.toString()).toPath();
			try
			{
				Files.copy(sourceFile, dstFile, StandardCopyOption.REPLACE_EXISTING);
			}
			catch(IOException e)
			{
				String warnString = "Could copy " + sourceFile.toString() + "to "
						+ destFolder.toString();
				logger.warn(warnString);
			}
		}


	}

	/**
	 * Copy everything in the List to backup folder.
	 * 
	 * @param sourceFile
	 *            source file list
	 */
	public static void copy(List<Path> sourceFiles)
	{
		File rootPath = new File(backupPath);
		for(Path path : sourceFiles)
		{
			File dstFile = new File(rootPath, path.toString());
			try
			{
				dstFile.mkdirs();
				Files.copy(path, dstFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			catch(IOException e)
			{
				String warnString = "Could copy " + path.toString() + "to " + dstFile.toString();
				logger.warn(warnString);
			}
		}
	}

	public static Path createDiff(Path beforeFile, Path afterFile)
	{
		return null;
	}

	public static Path applyDiff(Path sourceFile, Path afterFile)
	{
		return null;
	}

	public static void rename(Path file, String newName)
	{

	}

	/**
	 * Delete the specific Path. If the Path given is directory, delete the directory represented by
	 * Path.
	 * 
	 * @param file
	 *            the Path to delete.
	 */
	public static void delete(Path file)
	{
		File targetFile = file.toFile();

		// If a bad path is given...
		if(!targetFile.exists())
		{
			Control.logger.warn(targetFile.toString() + " is not existed. Unable to delete.");
			return;
		}

		// If a folder is given, recursively delete its sub-directories first.
		if(targetFile.isDirectory())
		{
			for(File f : targetFile.listFiles())
			{
				FileOp.delete(f.toPath());
			}
		}

		if(!targetFile.delete())
		{
			Control.logger.warn(file.toString() + " cannot be deleted. Operation aborted.");
			return;
		}

	}

	/**
	 * Return the file's size in bytes.
	 * 
	 * @param file
	 * @return the file's size in bytes.
	 */
	public static long fileSize(Path file)
	{
		File targetFile = file.toFile();

		return targetFile.length();
	}

	public static List<String> fileToList(Path file)
	{
		return null;
	}

	/**
	 * A utility function for converting a file to a String.
	 * 
	 * @author erickson <http://stackoverflow.com/users/3474/erickson> from
	 *         <http://stackoverflow.com/a/326440>
	 * 
	 * @param path
	 *            The path of the file to convert.
	 * @return A string containing the contents of the file.
	 * @throws IOException
	 *             If the file does not exist or cannot be opened.
	 */
	public static String fileToString(Path path) throws IOException
	{
		// Read the file as raw bytes
		byte[] encoded = Files.readAllBytes(path);

		// And encode those bytes as the default character set (eg, UTF 8)
		return Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
	}

	/**
	 * Check the given file. If it is a text file and <5 MB, returns true.
	 * 
	 * @param file
	 *            the file to be checked.
	 * @return true if file is text file and <= 5MB
	 */
	public static boolean fileValid(Path file)
	{
		String fileTypeString = null;
		try
		{
			fileTypeString = Files.probeContentType(file);
		}
		catch(Exception e)
		{
			// when failed, return false.
			return false;
		}
		if(fileSize(file) > 5242880)
		{
			return false;
		}
		return fileTypeString.startsWith("text");
	}

	/**
	 * Returns whether or not a path is a folder.
	 * 
	 * @param path
	 *            The path in question.
	 * @return True if a folder, false if a regular file.
	 */
	public static boolean isFolder(Path path)
	{
		return path.toFile().isDirectory();
	}

	/**
	 * Set the backup folder.
	 * 
	 * @param path
	 *            the backup folder.
	 */
	protected static void setBackupPath(String path)
	{
		backupPath = path;
	}

	/**
	 * Set the live folder.
	 * 
	 * @param livePath
	 *            the live folder.
	 */
	public static void setLivePath(String livePath)
	{
		FileOp.livePath = livePath;
	}


	/**
	 * Takes in a path to either the live directory or backup directory and converts it to the
	 * corresponding path to the opposite directory. For example, passing a path to something in the
	 * live directory will return the path to that file's location in the backup directory. Returns
	 * null if the file is not in the other directory.
	 * 
	 * @param inputPath
	 *            The path to convert.
	 * @return Path to file in opposite directory or null if it doesn't exist.
	 */
	public static Path convertPath(Path inputPath)
	{
		Path convertedPath = null;

		if(inputPath.startsWith(Control.liveDirectory))
		{
			// Remove the live directory from this path and add the backup directory to it
			String newPath = inputPath.toString().substring(
					Control.liveDirectory.toString().length());
			newPath = Control.backupDirectory.toString() + newPath;

			// We only want to return the path if it exists
			if(Paths.get(newPath).toFile().exists())
			{
				convertedPath = Paths.get(newPath).normalize();
			}
		}
		else if(inputPath.startsWith(Control.backupDirectory))
		{
			// Remove the backup directory from this path and add the live directory to it
			String newPath = inputPath.toString().substring(
					Control.backupDirectory.toString().length());
			newPath = Control.liveDirectory.toString() + newPath;

			// We only want to return the path if it exists
			if(Paths.get(newPath).toFile().exists())
			{
				convertedPath = Paths.get(newPath).normalize();
			}
		}

		// If the file did not exist or the input path did not start with either the backup or
		// live directory (ie, the path is not a child of either the live or backup directories),
		// we return null.
		return convertedPath;
	}

}
