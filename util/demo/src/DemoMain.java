import net.contentobjects.jnotify.*;
import java.sql.*;
import java.util.*;
import difflib.*;
import org.apache.log4j.*;
import java.nio.*;
import java.nio.file.*;

class DemoMain
{
	// Create the logger (takes in a class name for init)
	static Logger logger = Logger.getLogger(DemoMain.class);

	public static void main(String args[])
	{
		// Load the properties file for logger settings
		PropertyConfigurator.configure("log4j.properties");

		// And print a test message
		logger.info("Started logging...");

		// Get the path of current directory
		Path currentDir = Paths.get(System.getProperty("user.dir"));

		// Manipulate the paths to point to specific files.
		Path pathToFileA = currentDir.resolve("../src/DemoMain.java");
		Path pathToFileB = currentDir.resolve("../src/DemoContainer.java");

		// Note in the logger that we called normalize() so that we'd get something
		// like "util/demo/src" and not "util/demo/build/../src". Same path, but
		// one is easier for a human to read.
		logger.debug("Path to file A: "	+ pathToFileA.normalize());
		logger.debug("Path to file B: "	+ pathToFileB.normalize());

		databaseStuff();
		watcherStuff();
	}

	/////////////////////////////////////////////////////////////////
	public static void databaseStuff()
	{
		// Create database manager, init the table, and get the rows
		DemoDbManager db = new DemoDbManager();
		db.createTable();
		List<DemoContainer> rows = db.getRows();

		// Read the list of rows returned by the select query
		for(DemoContainer row : rows)
		{
			System.out.println("ID:   " + row.id);
			System.out.println("Name: " + row.name);
		}

		// Database should be closed when done with it
		db.closeDb();
	}

	/////////////////////////////////////////////////////////////////
	public static void watcherStuff()
	{
		// Path to either the current directory
		String path = System.getProperty("user.dir");

		// All types of file modifications watched (although we don't need deleted files)
		int mask =  JNotify.FILE_CREATED | 
					JNotify.FILE_DELETED | 
					JNotify.FILE_MODIFIED| 
					JNotify.FILE_RENAMED;
		// Subfolders too
		boolean watchSubtree = true;
		// Create listener
		DemoJnotifyListener listener = new DemoJnotifyListener();

		try
		{
			// Watch for changes in the dir (see listener for what happens when
			// changes occur)
			//int watchID = JNotify.addWatch(path, mask, watchSubtree, listener);
		}
		catch(Exception e)
		{
			// Send error messages to the logger
			logger.error(e);
		}

		try
		{
			// Sleep to give the (concurrent) JNotify time to notice changes (ie, so
			// the program doesn't open, run, and immediately close)
			Thread.sleep(1000);
		}
		catch(Exception e)
		{
			logger.error(e);
		}
	}
}