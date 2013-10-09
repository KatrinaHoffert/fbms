package cmpt370.fbms;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class DbManager
{
	public static ReentrantLock lock = new ReentrantLock();

	// Holds the active connection
	private static Connection connection = null;

	/**
	 * Initializes the database connection and creates tables if necessary. Returns true if it is
	 * the first run (ie, the database had to be created) and false for subsequential runs (ie, the
	 * database already existed).
	 * 
	 * Will also initialize the values for Control.liveDirectory and Control.backupDirectory if it
	 * is NOT the first run. For the first run, the directories will have to be initialized by the
	 * Control.
	 * 
	 * @return True if first run, false otherwise
	 */
	public static void init()
	{
		// We need to be able to detect if the program has been run for the first time or not
		boolean firstRun = false;

		try
		{
			// Ensure that the SQLite driver is accessible
			Class.forName("org.sqlite.JDBC");
		}
		catch(ClassNotFoundException e)
		{
			Errors.fatalError("Could not load database driver", e);
		}

		try
		{
			// Create a connection to an SQLite database. If ".revisions.db" does
			// not exist, create it in working directory
			connection = DriverManager.getConnection("jdbc:sqlite:" + Control.backupDirectory
					+ "/.revisions.db");
		}
		catch(SQLException e)
		{
			Errors.fatalError("Could not create database connection", e);
		}

		try
		{
			Statement statement = connection.createStatement();

			// Create a metadata request to check if the tables exist. This returns a result
			// set of tables. However, our pattern is very specific, so there should be either
			// zero (table does not exist) or one (table does exist) entries in the result set
			DatabaseMetaData metadata = connection.getMetaData();
			ResultSet revisionsTableExists = metadata.getTables(null, null, "revisions", null);
			ResultSet settingsTableExists = metadata.getTables(null, null, "settings", null);

			// If the revisions table is missing, create it
			if(!revisionsTableExists.next())
			{
				statement.executeUpdate("CREATE TABLE revisions (id INTEGER PRIMARY KEY ASC,"
						+ " path STRING, diff BLOB, delta INTEGER, time INTEGER)");
				firstRun = true;

				Control.logger.info("Existing revisions table not found; new table created");
			}
			else
			{
				Control.logger.info("Existing revisions table found");
			}

			// Likewise for the settings table
			if(!settingsTableExists.next())
			{
				statement.executeUpdate("CREATE TABLE settings (name STRING, setting STRING)");
				firstRun = true;

				Control.logger.info("Existing settings table not found; new table created");
			}
			else
			{
				Control.logger.info("Existing settings table found");
			}

			// If this isn't the first run, fetch the live directory. If it is the first run, that's
			// up to the Control to do
			if(!firstRun)
			{
				// Get the rows of the settings table and loop through them, searching for the
				// live directory path
				ResultSet settingsRows = statement.executeQuery("SELECT * FROM settings WHERE name = 'liveDirectory'");
				while(settingsRows.next())
				{
					if(settingsRows.getString("name").equals("liveDirectory"))
					{
						Control.liveDirectory = Paths.get(settingsRows.getString("setting"));
					}
				}

				// If we somehow didn't find it, something went horribly wrong. Assume this must
				// actually be a first run
				if(Control.liveDirectory == null)
				{
					Errors.fatalError("Could not load the live directory from existing backup");
				}
			}
		}
		catch(SQLException e)
		{
			Errors.fatalError("Database table creation failed", e);
		}
	}

	public static List<RevisionInfo> getRevisionData(Path file)
	{
		return null;
	}

	public static RevisionInfo getRevisionInfo(Path file, int timestamp)
	{
		return null;
	}

	public static void insertRevision(Path file, String diff, int timestamp, int delta)
	{

	}

	public static void renameFile(Path file, Path newName)
	{

	}

	public static String getConfig(String settingName)
	{
		return null;
	}

	public static void setConfig(String settingName, String settingValue)
	{

	}
}