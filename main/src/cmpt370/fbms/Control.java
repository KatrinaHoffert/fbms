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

import javax.swing.JOptionPane;

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
	public static boolean firstRunWizardDone = false;

	// Logger object is linked to the class
	public static Logger logger = Logger.getLogger(Control.class);

	/**
	 * The main method runs the startup code, initializing the database, checking for the first run,
	 * etc. The main method then loops through the watched files at intervals, checking for changes
	 * and acting appropriately
	 * 
	 * @param args
	 *            Command line arguments, not currently used
	 */
	public static void main(String[] args)
	{
		startup();
	}

	/**
	 * Manages all the startup functionality. First, we check if the backup directory has been set.
	 * If not, we begin the first run
	 */
	private static void startup()
	{
		// And print a testing message to the log
		logger.info("Program started");

		resolveBackupDirectory();

		// Branch based on whether or not this is considered a "first run". backupDirectory should
		// be set to a valid directory if resolveBackupDirectory() found the backup directory.
		// DbManager.init() can then load the database located there. If that's not the case, we'll
		// have to run the first start wizard, which will set the database directory and possibly
		// the live directory (for imports, DbManager.init() fetches the live directory)
		if(backupDirectory == null)
		{
			// Run the first run wizard. Keep the program open until that is done
			FirstStartWizard.run();
			while(!firstRunWizardDone)
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

			// Initialize the database, then set the live directory inside this database (for
			// sequential program start-ups)
			DbManager.init();
			DbManager.setConfig("liveDirectory", liveDirectory.toString());

			logger.info("First run wizard completed");
		}
		else
		{
			// For subsequent runs, the backup directory has already been set and the live
			// directory will be retreived during DbManager.init()
			DbManager.init();

			logger.info("It is a subsequent run");
		}
		logger.info("liveDirectory = " + liveDirectory);
		logger.info("backupDirectory = " + backupDirectory);
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

			// If the database file isn't in the backup folder, it's not a valid folder
			if(!backupDirectory.resolve(".revisions.db").toFile().exists())
			{
				logger.error("The backup directory linked in \"backup_location\" is invalid: "
						+ backupDirectory);
				backupDirectory = null;
				JOptionPane.showMessageDialog(null,
						"A backup location record exists, but is invalid. The first-run wizard "
								+ "will be displayed.", "Error", JOptionPane.WARNING_MESSAGE);
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
