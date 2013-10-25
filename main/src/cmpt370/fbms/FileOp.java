package cmpt370.fbms;

import java.io.File;
import java.nio.file.Path;
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

	public static boolean fileValid(Path file)
	{
		return false;
	}

	public static void setBackupPath(String path)
	{
		backupPath = path;
	}
}
