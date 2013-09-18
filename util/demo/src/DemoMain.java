import net.contentobjects.jnotify.*;

class DemoMain
{
	public static void main(String args[])
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
			int watchID = JNotify.addWatch(path, mask, watchSubtree, listener);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			// Sleep to give the (concurrent) JNotify time to notice changes (ie, so
			// the program doesn't open, run, and immediately close)
			Thread.sleep(100000);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}
}