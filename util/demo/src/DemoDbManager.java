import java.sql.*;
import java.util.*;
import org.apache.log4j.*;

class DemoDbManager
{
	// Holds the connection
	private Connection connection = null;
	// Get an instance of the DemoMain logger
	static Logger logger = Logger.getLogger("DemoMain");

	/**
	 * Chooses the database driver and creates the connection.
	 */
	public DemoDbManager()
	{
		// load the sqlite-JDBC driver using the class path to find it
		try
		{
			Class.forName("org.sqlite.JDBC");
		}
		catch(ClassNotFoundException e)
		{
			logger.error(e);
		}

		try
		{
			// Create a connection. If the file named "demoDatabase.db" doesn't alread
			// exist in the current folder, create it
			connection = DriverManager.getConnection("jdbc:sqlite:demoDatabase.db");
		}
		catch(SQLException e)
		{
			// Logger will print stack trace, etc, to log
			logger.error(e);
		}

		logger.info("Database driver successfully connected to database");
	}

	/**
	 * Issues queries to create the table.
	 */
	public void createTable()
	{
		try
		{
			// Create a statement object for executing queries
			Statement statement = connection.createStatement();

			// Set timeout to 30 seconds (queries that take longer than 30 seconds will
			// fail. Great for preventing an infinite loop, but legitimate queries that
			// take a long time may need the timeout to be increased)
			statement.setQueryTimeout(30);

			// Issue queries. Drop the table if it already exists and recreate it anew
			statement.executeUpdate("DROP TABLE IF EXISTS demo");
			statement.executeUpdate("CREATE TABLE demo (id INTEGER, name STRING)");
			statement.executeUpdate("INSERT INTO demo VALUES(1, 'Some string')");
			statement.executeUpdate("INSERT INTO demo VALUES(2, 'Another string')");
			statement.executeUpdate("INSERT INTO demo VALUES(9001, 'String with high ID')");

			logger.debug("Table demo created");

			// Statements must be closed!
			close(statement);
		}
		catch(SQLException e)
		{
			logger.error(e);
		}
	}

	/**
	 * Issues a query to get the rows in the demo table.
	 * @return A list of DemoContainers, one for each row from the table.
	 */
	public List<DemoContainer> getRows()
	{
		// Create a list of containers
		List<DemoContainer> rows = new ArrayList<>();

		try
		{
			// Issue a query which returns a row
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			ResultSet rs = statement.executeQuery("SELECT * FROM demo");

			// Loop through all rows of the result set
			while(rs.next())
			{
				// Use a container class to store row info
				DemoContainer row = new DemoContainer();
				// Read the result set row
				row.id = rs.getInt("id");
				row.name = rs.getString("name");

				// Logging
				logger.debug("Added row " + row.id + " into list of rows");

				// And add the row to the list
				rows.add(row);
			}

			// Statements and result sets must be closed!
			close(statement);
			close(rs);
		}
		catch(SQLException e)
		{
			logger.error(e);
		}

		return rows;
	}

	/**
	 * Closes the database connection safely.
	 */
	public void closeDb()
	{
		// Will be null if the connection wasn't able to initialize
		if(connection != null)
		{
			try
			{
				logger.debug("Attempting to close database connection");
				connection.close();
			}
			catch(SQLException e)
			{
				logger.error(e);
			}
		}
	}

	/**
	 * Closes a statement object safely.
	 */
	private void close(Statement statement)
	{
		if(statement != null)
		{
			try
			{
				logger.debug("Attempting to close Statement " + statement);
				statement.close();
			}
			catch(SQLException e)
			{
				logger.error(e);
			}
		}
	}

	/**
	 * Closes a result set object safely.
	 */
	private void close(ResultSet rs)
	{
		if(rs != null)
		{
			try
			{
				logger.debug("Attempting to close ResultSet " + rs);
				rs.close();
			}
			catch(SQLException e)
			{
				logger.error(e);
			}
		}
	}
}