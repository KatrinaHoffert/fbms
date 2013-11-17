package cmpt370.fbms;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import cmpt370.fbms.gui.FirstStartWizard;

/**
 * Runs the finer points of the startup that the program requires.
 */
public class Startup
{
	// Logger instance
	private static Logger logger = Logger.getLogger(Main.class);

	private static DbConnection db = DbConnection.getInstance();

	/**
	 * Manages all the startup functionality. First, we check if the backup directory has been set.
	 * If not, we begin the first run
	 */
	public void startup()
	{
		// Redirect standard error to the log (needs to be done first so that any errors encountered
		// reach the log; we'll still miss any possible errors that could occur before this line is
		// reached)
		System.setErr(createLoggingProxy(System.err));

		logger.info("Program started");

		// Alias the text and apply look and feel. Aliasing is not done on Windows, where text is
		// already aliased in the system look and feel.
		if(System.getProperty("os.name").toLowerCase().indexOf("windows") == -1)
		{
			System.setProperty("awt.useSystemAAFontSettings", "on");
			System.setProperty("swing.aatext", "true");
		}
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e)
		{
			Errors.nonfatalError("Could not apply look and feel.", e);
		}

		// Disable the internal logging of the MIME Magic library. We already log the exceptions
		// from it. We don't care about the internals.
		Logger.getLogger(net.sf.jmimemagic.Magic.class).setLevel(Level.OFF);
		Logger.getLogger(net.sf.jmimemagic.MagicParser.class).setLevel(Level.OFF);
		Logger.getLogger(net.sf.jmimemagic.MagicMatch.class).setLevel(Level.OFF);
		Logger.getLogger(net.sf.jmimemagic.MagicMatcher.class).setLevel(Level.OFF);
		Logger.getLogger(net.sf.jmimemagic.detectors.TextFileDetector.class).setLevel(Level.OFF);

		resolveBackupDirectory();

		// Branch based on whether or not this is considered a "first run". Control.backupDirectory
		// should
		// be set to a valid directory if resolveControl.backupDirectory() found the backup
		// directory.
		// DbManager.init() can then load the database located there. If that's not the case, we'll
		// have to run the first start wizard, which will set the database directory and possibly
		// the live directory (for imports, DbManager.init() fetches the live directory)
		if(Main.backupDirectory == null)
		{
			// Run the first run wizard. Keep the program open until that is done
			new FirstStartWizard().run();
			while(!Main.getInstance().getFirstRunWizardDone())
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
			db.initConnection();
			db.setConfig("liveDirectory", Main.liveDirectory.toString());

			// Set some default settings
			db.setConfig("trimDate", "-1");
			db.setConfig("startupScan", "true");
			db.setConfig("disableNonFatalErrors", "false");

			logger.info("First run wizard completed");
		}
		else
		{
			// For subsequent runs, the backup directory has already been set and the live
			// directory will be retrieved during DbManager.init()
			db.initConnection();

			// Live directory specified in database is invalid
			if(Main.liveDirectory == null || !Main.liveDirectory.toFile().exists())
			{
				int choice = JOptionPane.showConfirmDialog(
						null,
						"The live directory could not be found. Would you like to specify an alternative directory?",
						"Fatal error", JOptionPane.YES_NO_OPTION);
				if(choice == JOptionPane.YES_OPTION)
				{
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

					// Return value will be JFileChooser.APPROVE_OPTION iff a folder was chosen. Any
					// other value means the window was closed
					int returnVal = fileChooser.showOpenDialog(null);

					// We're a go
					if(returnVal == JFileChooser.APPROVE_OPTION)
					{
						Path chosenPath = fileChooser.getSelectedFile().toPath();

						if(!chosenPath.startsWith(Main.backupDirectory)
								&& !Main.backupDirectory.startsWith(chosenPath))
						{
							Main.liveDirectory = chosenPath;
							db.setConfig("liveDirectory", Main.liveDirectory.toString());
						}
						else
						{
							JOptionPane.showMessageDialog(null,
									"Live directory cannot be a child of the live directory and vice versa.");
							System.exit(2);
						}
					}
					else
					{
						System.exit(2);
					}
				}
				else
				{
					System.exit(2);
				}
			}

			logger.info("It is a subsequent run");
		}
		logger.info("liveDirectory = " + Main.liveDirectory);
		logger.info("backupDirectory = " + Main.backupDirectory);

		Main.getInstance().createWatcher();
	}

	/**
	 * Sends an output to the log. Used to redirect standard error.
	 * 
	 * @param realPrintStream
	 *            The output stream to send to the log.
	 * @return The passed in print stream, which is logged AND sent to standard error.
	 */
	private PrintStream createLoggingProxy(final PrintStream realPrintStream)
	{
		return new PrintStream(realPrintStream)
		{
			public void print(final String string)
			{
				realPrintStream.print(string);
				logger.error(string);
			}
		};
	}

	/**
	 * Opens the "backup_location" file in the program directory and parses its contents as a path.
	 * The parsed path is set as the backup directory. If the path is invalid, the backup directory
	 * is not set.
	 */
	public void resolveBackupDirectory()
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
					Main.backupDirectory = backupLocation.toPath();
					logger.info("Located backup location: " + Main.backupDirectory);
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
				Main.backupDirectory = null;
			}
			catch(SecurityException e)
			{
				logger.error("Security error: cannot access backup directory", e);
				Main.backupDirectory = null;
			}
			finally
			{
				if(in != null)
				{
					in.close();
				}
			}

			// If the database file isn't in the backup folder, it's not a valid folder
			if(Main.backupDirectory == null
					|| !Main.backupDirectory.resolve(".revisions.db").toFile().exists())
			{
				logger.error("The backup directory linked in \"backup_location\" is invalid: "
						+ Main.backupDirectory);
				Main.backupDirectory = null;
				JOptionPane.showMessageDialog(null,
						"A backup location record exists, but is invalid. The first-run wizard "
								+ "will be displayed.", "Error", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * Scans the live directory for byte changes on startup, allowing the detection of files that
	 * were added or modified when the program was not running. Renames, however, are a lost cause.
	 * They'll be recognized as a new file.
	 * 
	 * @param directory
	 *            The directory to base the scan on (ie, the live directory)
	 */
	public void startupScan(Path directory)
	{
		String status = db.getConfig("startupScan");
		// Check if user disabled the scan
		if(status != null && !status.equals("true"))
		{
			logger.info("Startup scan is disabled.");
			return;
		}

		for(File file : directory.toFile().listFiles())
		{
			if(!file.isDirectory())
			{
				// If the file doesn't already exist, we can just copy it over
				if(!FileOp.convertPath(file.toPath()).toFile().exists())
				{
					Path targetDirectory = FileOp.convertPath(file.toPath()).getParent();
					FileOp.copy(file.toPath(), targetDirectory);

					logger.info("Startup: Found new file " + file.toString());
				}
				// The file does exist, so determine if the file has been changed. If it
				// hasn't, we need to create a revision for this file.
				else if(!FileOp.isEqual(file.toPath(), FileOp.convertPath(file.toPath())))
				{
					// Create the diff
					Path diffFile = FileOp.createPatch(FileOp.convertPath(file.toPath()),
							file.toPath());
					// Difference in file sizes
					long delta = FileOp.fileSize(file.toPath())
							- FileOp.fileSize(FileOp.convertPath(file.toPath()));

					// Store the revision
					FileHistory fileHist = new FileHistory(file.toPath());
					fileHist.storeRevision(diffFile, null, FileOp.fileSize(file.toPath()), delta);

					// Finally, copy the file over
					Path targetDirectory = FileOp.convertPath(file.toPath()).getParent();
					FileOp.copy(file.toPath(), targetDirectory);

					logger.info("Startup: Found modified file " + file.toString());
				}
			}
			else
			{
				// Call itself recursively for directories
				startupScan(directory.resolve(file.toPath()));
			}
		}
	}
}
