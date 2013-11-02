package cmpt370.fbms;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.log4j.Logger;

public class FileOp
{
	private static Logger logger = Logger.getLogger(FileOp.class);
	private static String backupPath;

	public static void copy(Path sourceFile, Path destFolder)
	{

	}

	public static void copy(List<Path> sourceFiles)
	{

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
			logger.warn(targetFile.toString() + " is not existed. Unable to delete.");
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
			logger.warn(file.toString() + " cannot be deleted. Operation aborted.");
			return;
		}

	}

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

	public static boolean fileValid(Path file)
	{
		return false;
	}

	public static void setBackupPath(String path)
	{
		backupPath = path;
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
