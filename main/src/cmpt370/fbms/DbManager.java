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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;


public class DbManager
{
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
						+ " path STRING, diff STRING, delta INTEGER, filesize INTEGER, time INTEGER)");
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

		PreparedStatement statement = null;
		String selectQuery = "SELECT * FROM revisions WHERE path = ?";

		try
		{
			// Fill in the prepared statement
			statement = connection.prepareStatement(selectQuery);
			statement.setString(1, file.toString());

			// Select the rows of revisions
			ResultSet revisionRows = statement.executeQuery();

			while(revisionRows.next())
			{
				// Table structure: (id INTEGER, path STRING, diff STRING, delta INTEGER, filesize
				// INTEGER, time INTEGER)
				RevisionInfo newRevision = new RevisionInfo();

				// id INTEGER
				newRevision.id = revisionRows.getLong("id");

				// path String
				newRevision.path = revisionRows.getString("path");

				// diff STRING
				newRevision.diff = revisionRows.getString("diff");

				// delta INTEGER
				newRevision.delta = revisionRows.getLong("delta");

				// filesize INTEGER
				newRevision.filesize = revisionRows.getLong("filesize");

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

	/**
	 * Inserts a revision into the database using the supplied information and the current time as
	 * the time stamp.
	 * 
	 * @param file
	 *            The path to the file that we're inserting (in the live directory).
	 * @param diff
	 *            The diff of the revision as a String (FileOp.fileToString can be useful to get
	 *            this String representation).
	 * @param delta
	 *            The difference in file size that this revision introduced (positive means that
	 *            this revision increased the file size of the file, while negative means the file
	 *            size decreased).
	 */
	public static void insertRevision(Path file, String diff, long delta, long filesize)
	{
		// Using prepared statements because the diff string can be very long
		PreparedStatement revision = null;
		String insertStatment = "INSERT INTO revisions (path, diff, delta, filesize, time) VALUES("
				+ "?, ?, ?, ?, ?)";

		try
		{
			revision = connection.prepareStatement(insertStatment);

			// path
			revision.setString(1, file.toString());
			// diff
			revision.setString(2, diff);
			// delta
			revision.setLong(3, delta);
			// filesize
			revision.setLong(4, filesize);
			// time
			revision.setLong(5, System.currentTimeMillis() / 1000L);

			revision.executeUpdate();
		}
		catch(SQLException e1)
		{
			Errors.nonfatalError("Could not insert revision into database", e1);
		}
	}

	/**
	 * Renames all instances of a certain file to a new name in the revisions database.
	 * 
	 * @param file
	 *            The path of the file we are renaming.
	 * @param newName
	 *            The new name of the file. Note this does not include the full path: just the file
	 *            name (and extension).
	 */
	public static void renameFile(Path file, String newName)
	{
		try
		{
			// Figure out the new name
			Path newPath = file.resolveSibling(newName);

			Statement statement = connection.createStatement();
			statement.executeUpdate("UPDATE revisions SET path = '" + newPath.toString()
					+ "' WHERE path = '" + file.toString() + "'");

		}
		catch(SQLException e)
		{
			Errors.nonfatalError("Could not rename revisions in database.", e);
		}
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
		try
		{
			Statement statement = connection.createStatement();

			// Search for our row
			ResultSet settingsRows = statement.executeQuery("SELECT * FROM settings WHERE name = '"
					+ settingName + "'");

			// If the row exists, update it
			if(settingsRows.next())
			{
				statement.executeUpdate("UPDATE settings SET setting = '" + settingValue
						+ "' WHERE name = '" + settingName + "'");
			}
			// If the row does not exist, insert it
			else
			{
				statement.executeUpdate("INSERT INTO settings(name, setting) VALUES('"
						+ settingName + "', '" + settingValue + "')");
			}

		}
		catch(SQLException e)
		{
			Errors.fatalError("Unable to set requested value in settings.", e);
		}
	}

	/**
	 * Trims the database of old revisions if the user has enabled the trim feature and has set a
	 * valid date for it. Trim will remove revisions older than the specified date. For example, if
	 * the user specified to remove revisions older than 30 days (155,520,000 seconds), then all
	 * revisions matching that time stamp and older will be deleted.
	 */
	public static void trimDatabase()
	{
		// Get the trim date from the database
		String timeDateConfig = getConfig("trimDate");

		// Only trim if the value is valid
		if(timeDateConfig != null && timeDateConfig.matches("^[0-9]+$"))
		{
			// Figure out the cutoff date (the oldest possible file that won't be removed)
			// 5,184,000 = seconds per day, since timeDateConfig is in days
			long cutoffDate = System.currentTimeMillis() / 1000 - Long.parseLong(timeDateConfig)
					* 5184000;

			try
			{
				// Remove the revisions with an older date
				Statement statement = connection.createStatement();
				statement.execute("DELETE FROM revisions WHERE time < " + cutoffDate);

				Control.logger.debug("Database trimmed of entries older than "
						+ Data.formatDate(cutoffDate));
			}
			catch(SQLException e)
			{
				Errors.nonfatalError("Could not remove older revisions", "Trim failed", e);
			}
		}
		else
		{
			Control.logger.debug("Database trim command encountered and ignored because trim is"
					+ " disabled or is set to an invalid value");
		}
	}

	/**
	 * Closes the database connection. Meant for tests where the connection is opened and closed for
	 * each test (as there's no way to be certain of the order the tests will be run in).
	 */
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
