package cmpt370.fbms;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

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

		resolveBackupDirectory();

		// Branch based on whether or not this is considered a "first run"
		if(backupDirectory == null)
		{
			// first run
			// get backup directory and create backup_location file
			// init db
			// set live directory
			new FirstStartWizard();
			while(liveDirectory == null || backupDirectory == null)
			{
				try
				{
					Thread.sleep(500);
				}
				catch(InterruptedException e)
				{
					logger.error(e);
				}
			}
			System.out.println("It is the first run");
			System.out.println("liveDirectory = " + liveDirectory);
			System.out.println("backupDirectory = " + backupDirectory);


			DbManager.init();
		}
		else
		{
			// not first run
			// init db
			// get live directory
			System.out.println("It is NOT the first run");
			System.out.println("liveDirectory = " + liveDirectory);
			System.out.println("backupDirectory = " + backupDirectory);


			DbManager.init();
		}
	}

	/**
	 * Opens the "backup_location" file in the program directory and parses its contents as a path.
	 * The parsed path is set as the backup directory. If the path is invalid, the backup directory
	 * is not set.
	 */
	private static void resolveBackupDirectory()
	{
		// Load the backup_location file to get the backup folder path. If it doesn't exist, it's
		// the first run
		File backup_file = new File("backup_location");
		if(backup_file.exists())
		{
			Scanner in = null;
			try
			{
				// Read the path in, and if it's valid, set the backup location to this path. If
				// it's not valid, it's the first run
				in = new Scanner(new FileReader(backup_file));
				File backupLocation = Paths.get(in.nextLine()).toFile();

				if(backupLocation.exists())
				{
					backupDirectory = backupLocation.toPath();
					logger.info("Located backup location: " + backupDirectory);
				}
			}
			catch(IOException e)
			{
				// If an exception occurs, we couldn't retrieve the backup directory, so must set it
				// to null
				logger.error("Could not read in backup_location file", e);
			}
			catch(InvalidPathException e)
			{
				logger.error("Backup directory path is invalid", e);
				backupDirectory = null;
			}
			catch(SecurityException e)
			{
				logger.error("Security error: cannot access backup directory", e);
				backupDirectory = null;
			}
			finally
			{
				if(in != null)
				{
					in.close();
				}
			}
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
