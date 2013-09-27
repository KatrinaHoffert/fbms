// ==============================================================
//	This file is part of FBMS (https://code.google.com/p/fbms)
//
//	Copyright (C) 2013 Group 06
//
//	You can redistribute this code and/or modify it under
//	the terms of the GNU General Public License as published
//	by the Free Software Foundation; either version 3 of the
//	License, or (at your option) any later version
// ==============================================================

import net.contentobjects.jnotify.*;
import org.apache.log4j.*;

class DemoJnotifyListener implements JNotifyListener
{
	// Get an instance of the DemoMain logger
	static Logger logger = Logger.getLogger("DemoMain");

	// Called by library when a file in dir changes. wd is an identifier, rootPath is the
	// folder the file is located in
	public void fileRenamed(int wd, String rootPath, String oldName, String newName)
	{
		System.out.println("[" + rootPath + "] RENAMED: " + oldName + " -> " + newName);
		// Commenting out the logger lines because they change the log every time the log is
		// changed, which spams the screen with file changes. They can still go in the final
		// program, however, provided that the log file isn't kept in the live directory.
		// This creates a limitation on the program: the program must be located outside of
		// the live directory (otherwise the log, database, etc all get backed up...)
		//logger.debug("[" + rootPath + "] RENAMED: " + oldName + " -> " + newName);
	}

	// Likewise for modified files
	public void fileModified(int wd, String rootPath, String name)
	{
		System.out.println("[" + rootPath + "] MODIFIED: " + name);
		//logger.debug("[" + rootPath + "] MODIFIED: " + name);
	}

	// And deletions
	public void fileDeleted(int wd, String rootPath, String name)
	{
		System.out.println("[" + rootPath + "] DELETED: " + name);
		//logger.debug("[" + rootPath + "] DELETED: " + name);
	}

	// And creations
	public void fileCreated(int wd, String rootPath, String name)
	{
		System.out.println("[" + rootPath + "] CREATED: " + name);
		//logger.debug("[" + rootPath + "] CREATED: " + name);
	}
}