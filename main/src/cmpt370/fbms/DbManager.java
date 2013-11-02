package cmpt370.fbms;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class DbManager
{
	private static ReentrantLock lock = new ReentrantLock();

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
						+ " path STRING, diff STRING, delta INTEGER, time INTEGER)");
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

	/**
	 * Gets a list of revision info objects, which contain all the information about the revisions
	 * of a specific file.
	 * 
	 * @param file
	 *            The file to obtain revisions on.
	 * @return A list of RevisionInfo object
	 */
	public static List<RevisionInfo> getRevisionData(Path file)
	{
		List<RevisionInfo> list = new LinkedList<>();

		try
		{
			Statement statement = connection.createStatement();

			// Select the rows of revisions
			ResultSet revisionRows = statement.executeQuery("SELECT * FROM revisions WHERE path = '"
					+ file + "'");

			while(revisionRows.next())
			{
				// Table structure: (id INTEGER, path STRING, diff BLOB, delta INTEGER, time
				// INTEGER)
				RevisionInfo newRevision = new RevisionInfo();

				// id INTEGER
				newRevision.id = revisionRows.getLong("id");

				// path String
				newRevision.path = revisionRows.getString("path");

				// diff STRING
				newRevision.diff = revisionRows.getString("diff");

				// delta INTEGER
				newRevision.delta = revisionRows.getLong("delta");

				// time INTEGER
				newRevision.time = revisionRows.getLong("time");

				list.add(newRevision);
			}
		}
		catch(SQLException e)
		{
			Errors.fatalError("Could not create SQL statement", e);
		}


		return list;
	}

	public static RevisionInfo getRevisionInfo(Path file, long timestamp)
	{
		return null;
	}

	public static void insertRevision(Path file, String diff, long timestamp, int delta)
	{

	}

	public static void renameFile(Path file, Path newName)
	{

	}

	/**
	 * Grabs the value of a specified setting from the settings database.
	 * 
	 * @param settingName
	 *            Name of the setting you want the value of.
	 * @return Returns the value of the setting or null if it does not exist.
	 */
	public static String getConfig(String settingName)
	{
		String settingValue = null;

		try
		{
			// Get the row with the setting name
			Statement statement = connection.createStatement();
			ResultSet settingRow = statement.executeQuery("SELECT * FROM settings WHERE name = '"
					+ settingName + "'");

			// Make sure that there is a row to get data from (there should be 0 or 1)
			if(settingRow.next())
			{
				settingValue = settingRow.getString("setting");
			}
		}
		catch(SQLException e)
		{
			Errors.fatalError("Retrieval of setting value failed", e);
		}

		// Will end up returning either the setting value if it was found or null if it was not
		// found
		return settingValue;
	}

	/**
	 * Method to add a setting name and variable into the settings table, or update an existing
	 * setting to a new value.
	 * 
	 * @param settingName
	 *            The name of the setting to change or insert.
	 * @param settingValue
	 *            The value for the named setting to be updated or inserted into the table.
	 */
	public static void setConfig(String settingName, String settingValue)
	{
		if(settingName == null || settingValue == null)
		{
			Errors.fatalError("Null input provided to setConfig(), unable to proceed.");
		}

		// Build our strings for our queries concatenating with our variables.
		String update = "UPDATE settings SET setting = '" + settingValue + "' WHERE name = '"
				+ settingName + "'";
		String insert = "INSERT INTO settings(name, setting) VALUES('" + settingName + "', '"
				+ settingValue + "')";

		try
		{

			// Grab statement from connection so we can call execute for our queries.
			Statement statement = connection.createStatement();


			if(settingName.equals("liveDirectory"))
			{
				// Search for our row.
				ResultSet settingsRows = statement.executeQuery("SELECT * FROM settings WHERE name = 'liveDirectory'");

				// If the row exists, update it.
				if(settingsRows.next())
				{
					System.out.println(update);
					statement.executeUpdate(update);
				}
				// If the row does not exist, insert it.
				else
				{
					statement.executeUpdate(insert);
				}

			}
			if(settingName.equals("backupDirectory"))
			{
				ResultSet settingsRows = statement.executeQuery("SELECT * FROM settings WHERE name = 'backupDirectory'");

				// If the row exists, update it.
				if(settingsRows.next())
				{
					statement.executeUpdate(update);
				}
				// If the row does not exist, insert it.
				else
				{
					statement.executeUpdate(insert);
				}
			}

		}
		catch(SQLException e)
		{
			Errors.fatalError("Unable to set requested value in settings.", e);
		}
	}

	public static void close()
	{
		if(connection != null)
		{
			try
			{
				connection.close();
			}
			catch(SQLException e)
			{
				Control.logger.error("Could not close database connection", e);
			}
		}
	}
}
