import net.contentobjects.jnotify.*;

class DemoJnotifyListener implements JNotifyListener
{
	// Called by library when a file in dir changes. wd is an identifier, rootPath is the
	// folder the file is located in
	public void fileRenamed(int wd, String rootPath, String oldName, String newName)
	{
		System.out.println("[" + rootPath + "] RENAMED: " + oldName + " -> " + newName);
	}

	// Likewise for modified files
	public void fileModified(int wd, String rootPath, String name)
	{
		System.out.println("[" + rootPath + "] MODIFIED: " + name);
	}

	// And deletions
	public void fileDeleted(int wd, String rootPath, String name)
	{
		System.out.println("[" + rootPath + "] DELETED: " + name);
	}

	// And creations
	public void fileCreated(int wd, String rootPath, String name)
	{
		System.out.println("[" + rootPath + "] CREATED: " + name);
	}
}