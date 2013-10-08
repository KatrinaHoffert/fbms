package cmpt370.fbms;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

// Making sure libraries are found (remove later)

public class Control
{
	// Various public variables shared amongst components
	public static Path liveDirectory = null;
	public static Path backupDirectory = null;
	public static List<Path> createdFiles = new LinkedList<>();
	public static List<Path> modifiedFiles = new LinkedList<>();
	public static List<RenamedFile> renamedFiles = new LinkedList<>();
	public static List<Path> deletedFiles = new LinkedList<>();

	// Logger object is linked to the class
	public static Logger logger = Logger.getLogger(Control.class);

	public static void main(String[] args)
	{
		// And print a testing message to the log
		logger.info("Program started");

		// Initialize database; returns true if the tables had to be created (ie, first run)
		if(DbManager.init())
		{
			// First run code goes here
		}
		else
		{
			// Sequential runs
		}
	}

	private static void handleCreatedFiles()
	{

	}

	private static void handleModifiedFiles()
	{

	}

	private static void handleRenamedFiles()
	{

	}

	private static void handleDeletedFiles()
	{

	}

	public static void displayRevision(Path file, int timestamp)
	{

	}

	public static void displayRevisionChanges(Path file, int timestamp)
	{

	}

	public static void revertRevision(Path file, int timestampt)
	{

	}

	public static void restoreBackup()
	{

	}

	public static void copyTo(Path sourceFile, Path destFolder)
	{

	}
}
