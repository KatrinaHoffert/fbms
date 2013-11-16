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

import cmpt370.fbms.gui.GuiUtility;

/**
 * DbManager handles all the direct database activity, connecting and interfacing with the SQLite
 * database. No other class calls database queries directly, so changes to the database only require
 * changes to this class.
 */
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
	public static void initConnection()
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
			connection = DriverManager.getConnection("jdbc:sqlite:" + Main.backupDirectory
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
						+ " path STRING, diff STRING, binary BLOB, delta INTEGER, filesize INTEGER, time INTEGER)");
				firstRun = true;

				Main.logger.info("Existing revisions table not found; new table created");
			}
			else
			{
				Main.logger.info("Existing revisions table found");
			}

			// Likewise for the settings table
			if(!settingsTableExists.next())
			{
				statement.executeUpdate("CREATE TABLE settings (name STRING, setting STRING)");
				firstRun = true;

				Main.logger.info("Existing settings table not found; new table created");
			}
			else
			{
				Main.logger.info("Existing settings table found");
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
						Main.liveDirectory = Paths.get(settingsRows.getString("setting"));
					}
				}
			}
		}
		catch(SQLException e)
		{
			Errors.fatalError("Database table creation failed", e);
		}

		Main.logger.debug("Database connection successfully initialized");
	}

	/**
	 * Gets a list of revision info objects, which contain all the information about the revisions
	 * of a specific file.
	 * 
	 * @param file
	 *            The file to obtain revisions on.
	 * @return A list of RevisionInfo object
	 */
	public static List<RevisionInfo> getFileRevisions(Path file)
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
				// Table structure: (id INTEGER, path STRING, diff STRING, binary BLOB, delta
				// INTEGER, filesize
				// INTEGER, time INTEGER)
				RevisionInfo newRevision = new RevisionInfo();

				// id INTEGER
				newRevision.id = revisionRows.getLong("id");

				// path String
				newRevision.path = revisionRows.getString("path");

				// diff STRING
				newRevision.diff = revisionRows.getString("diff");

				// binary BLOB
				newRevision.binary = revisionRows.getBytes("binary");

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

		Main.logger.debug("Fetched revision data for " + file.toString() + "(found " + list.size()
				+ " entries)");

		return list;
	}

	/**
	 * Fetches a single revision entry with the specified time stamp.
	 * 
	 * @param file
	 *            The path (in the live directory) of the file.
	 * @param timestamp
	 *            The time stamp of the desired revision.
	 * @return The revision in question if found or null if the revision does not exist.
	 */
	public static RevisionInfo getSpecificRevision(Path file, long timestamp)
	{
		RevisionInfo revision = null;

		PreparedStatement statement = null;
		String selectQuery = "SELECT * FROM revisions WHERE path = ? AND time = ?";

		try
		{
			// Fill in the prepared statement
			statement = connection.prepareStatement(selectQuery);
			statement.setString(1, file.toString());
			statement.setString(2, Long.toString(timestamp));

			// Select the rows of revisions
			ResultSet revisionRows = statement.executeQuery();

			if(revisionRows.next())
			{
				// Table structure: (id INTEGER, path STRING, diff STRING, binary BLOB, delta
				// INTEGER, filesize INTEGER, time INTEGER)
				revision = new RevisionInfo();

				// id INTEGER
				revision.id = revisionRows.getLong("id");

				// path String
				revision.path = revisionRows.getString("path");

				// diff STRING
				revision.diff = revisionRows.getString("diff");

				// delta INTEGER
				revision.binary = revisionRows.getBytes("binary");

				// filesize INTEGER
				revision.filesize = revisionRows.getLong("filesize");

				// filesize INTEGER
				revision.filesize = revisionRows.getLong("filesize");

				// time INTEGER
				revision.time = revisionRows.getLong("time");

				Main.logger.debug("Found revision entry for " + file.toString() + " (at T = "
						+ timestamp + ")");
			}
			else
			{
				Main.logger.debug("Failed to find a revision entry for " + file.toString()
						+ " (at T = " + timestamp + ")");
			}
		}
		catch(SQLException e)
		{
			Errors.fatalError("Could not create SQL statement", e);
		}

		return revision;
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
	public static void insertRevision(Path file, String diff, byte[] binary, long delta,
			long filesize)
	{
		// Using prepared statements because the diff string can be very long
		PreparedStatement revision = null;
		String insertStatment = "INSERT INTO revisions (path, diff, binary, delta, filesize, time) VALUES("
				+ "?, ?, ?, ?, ?, ?)";

		try
		{
			revision = connection.prepareStatement(insertStatment);

			// path
			revision.setString(1, file.toString());
			// diff
			revision.setString(2, diff);
			// binary
			revision.setBytes(3, binary);
			// delta
			revision.setLong(4, delta);
			// filesize
			revision.setLong(5, filesize);
			// time
			revision.setLong(6, System.currentTimeMillis() / 1000L);

			revision.executeUpdate();
		}
		catch(SQLException e1)
		{
			Errors.nonfatalError("Could not insert revision into database", e1);
		}

		Main.logger.debug("Successfully inserted revision " + file.toString() + " (delta: " + delta
				+ "; filesize: " + filesize + ")");
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
	public static void renameRevisions(Path file, String newName)
	{
		// Figure out the new name
		Path newPath = file.resolveSibling(newName);
		try
		{
			String updateStatement = "UPDATE revisions SET path = ? WHERE path = ?";

			PreparedStatement prepStatement = connection.prepareStatement(updateStatement);

			prepStatement.setString(1, file.getParent().resolve(newName).toString());
			prepStatement.setString(2, file.toString());

			prepStatement.executeUpdate();
		}
		catch(SQLException e)
		{
			Errors.nonfatalError("Could not rename revisions in database.", e);
		}

		Main.logger.debug("Renamed revisions in database " + file.toString() + " -> "
				+ newPath.toString());
	}

	/**
	 * Renames a single folder in a path to a new name. All revisions that contain the original
	 * folder path will have the that folder updated to a new name.
	 * 
	 * @param folder
	 *            The path of the folder we want to rename (in the live directory).
	 * @param newName
	 *            The new name for that one folder.
	 */
	public static void renameFolder(Path folder, String newName)
	{
		try
		{
			// We have to get revisions that have the path in them
			String selectStatement = "SELECT * FROM revisions WHERE path LIKE ?";

			PreparedStatement prepStatement = connection.prepareStatement(selectStatement);

			// Note the added wild card, as revisions will have paths in the form [folder we
			// want]/[possibly other paths]/[file name]
			prepStatement.setString(1, folder.toString() + '%');

			ResultSet result = prepStatement.executeQuery();

			// Now loop through the found paths and update each one
			while(result.next())
			{
				Path path = Paths.get(result.getString("path"));

				// Get the "[folder we want]" part, but with our new name instead
				Path prePath = folder.resolveSibling(newName);

				// Get the part of the path after the folder we want (the "/[possibly other
				// paths]/[file name]" part)
				Path postPath = folder.relativize(path);

				// And put them together to get [renamed path]/[possibly other paths]/[file name]
				Path renamedPath = prePath.resolve(postPath);

				// Create the update statement to change this in the database
				String updateStatement = "UPDATE revisions SET path = ? WHERE path = ?";

				prepStatement = connection.prepareStatement(updateStatement);
				prepStatement.setString(1, renamedPath.toString());
				prepStatement.setString(2, path.toString());
				prepStatement.executeUpdate();

				Main.logger.debug("Renamed path " + path.toString() + " -> "
						+ renamedPath.toString());
			}
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

		Main.logger.debug("Found configuration value for " + settingName + " = " + settingValue);

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
		String insertStatment = "INSERT INTO settings(name, setting) VALUES(?, ?)";
		String updateStatement = "UPDATE settings SET setting = ? WHERE name = ?";
		try
		{
			Statement statement = connection.createStatement();

			// Search for our row
			ResultSet settingsRows = statement.executeQuery("SELECT * FROM settings WHERE name = '"
					+ settingName + "'");

			// If the row exists, update it
			if(settingsRows.next())
			{
				PreparedStatement prepStatement = connection.prepareStatement(updateStatement);

				prepStatement.setString(1, settingValue);
				prepStatement.setString(2, settingName);

				prepStatement.executeUpdate();
			}
			// If the row does not exist, insert it
			else
			{
				PreparedStatement prepStatement = connection.prepareStatement(insertStatment);

				prepStatement.setString(1, settingName);
				prepStatement.setString(2, settingValue);

				prepStatement.executeUpdate();
			}
		}
		catch(SQLException e)
		{
			Errors.fatalError("Unable to set requested value in settings.", e);
		}

		Main.logger.debug("Successfully set configuration value " + settingName + " = "
				+ settingValue);
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

				Main.logger.debug("Database trimmed of entries older than "
						+ GuiUtility.formatDate(cutoffDate));
			}
			catch(SQLException e)
			{
				Errors.nonfatalError("Could not remove older revisions", "Trim failed", e);
			}
		}
		else
		{
			Main.logger.debug("Database trim command encountered and ignored because trim is"
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
				Main.logger.error("Could not close database connection", e);
			}
		}

		Main.logger.debug("Database connection closed.");
	}
}
