package cmpt370.fbms;

import net.contentobjects.jnotify.JNotifyListener;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Watcher implements JNotifyListener
{
	/**
	 * Event handler for files that are renamed. The JNotify watcher will call this method if
	 * it detects a file rename operation. Such files will be added to the appropriate list
	 * in Control. Since rename operations must store both the old name and the new name, a
	 * PoD object is used to combine both Paths into one object.
	 * @param wd Unused
	 * @param rootPath The path the file is located in
	 * @param oldName The file's previous name
	 * @param newName The file's new name
	 */
	public void fileRenamed(int wd, String rootPath, String oldName, String newName)
	{
		// Strings must be converted into Path objects
		Path oldPath = Paths.get(rootPath, oldName);
		Path newPath = Paths.get(rootPath, newName);
		
		RenamedFile listObject = new RenamedFile();
		listObject.oldName = oldPath;
		listObject.newName = newPath;
		
		Control.renamedFiles.add(listObject);
		
		// Use the logger in Control to issue messages
		Control.logger.info("Renamed file " + oldName + " to " + newName + " in " + rootPath);
	}

	public void fileModified(int wd, String rootPath, String name)
	{
		
	}

	public void fileDeleted(int wd, String rootPath, String name)
	{

	}
	
	public void fileCreated(int wd, String rootPath, String name)
	{

	}
}
