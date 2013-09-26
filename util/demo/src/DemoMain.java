import net.contentobjects.jnotify.*;
import java.sql.*;
import java.util.*;

class DemoMain
{
	public static void main(String args[])
	{
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
			e.printStackTrace();
		}

		try
		{
			// Sleep to give the (concurrent) JNotify time to notice changes (ie, so
			// the program doesn't open, run, and immediately close)
			Thread.sleep(1000);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}