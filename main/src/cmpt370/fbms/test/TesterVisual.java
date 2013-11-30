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

package cmpt370.fbms.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Permission;

import junit.framework.TestCase;
import cmpt370.fbms.DbConnection;
import cmpt370.fbms.Errors;
import cmpt370.fbms.Main;

/**
 * This class runs tests on portions of the program that cannot be automatically tested for
 * correctness, such as the GUI and various visual components.
 */
public class TesterVisual extends TestCase
{
	Path path;
	DbConnection db;

	// Prepare DB and path.
	// Disable system.exit().
	public void setUp()
	{
		path = Paths.get("").toAbsolutePath();
		Main.backupDirectory = path;
		Main.liveDirectory = path;
		db = DbConnection.getInstance();
		db.initConnection();
		SecurityManager manager = new StopExitSecurityManager();
		System.setSecurityManager(manager);
	}


	// Demonstrates a fatal error with just a message
	public void test_errorsFatalError()
	{
		try
		{
			Errors.fatalError("She turned me into a newt!");
		}
		catch(SecurityException e)
		{}
	}

	// Demonstrates a fatal error that also has a stack trace included (see also: the log)
	@SuppressWarnings("null")
	public void test_errorsFatalErrorWithStackTrace()
	{
		String someString = null;

		try
		{
			someString.charAt(0); // Throws an exception
		}
		catch(NullPointerException e)
		{
			try
			{
				Errors.fatalError("We dun goofed...", e);
			}
			catch(SecurityException e1)
			{}
		}
	}

	// Demonstrates a non-fatal error message
	public void test_errorsNonfatalError() throws InterruptedException
	{
		Errors.nonfatalError("Such error message!<br />&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;"
				+ "&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;So recovery!<br />&emsp;&emsp;Wow!", "Wow!");
	}


	// Clean up
	public void tearDown()
	{
		db.close();

		// restore security manager
		SecurityManager mgr = System.getSecurityManager();
		if((mgr != null) && (mgr instanceof StopExitSecurityManager))
		{
			StopExitSecurityManager smgr = (StopExitSecurityManager) mgr;
			System.setSecurityManager(smgr.getPreviousMgr());
		}
		else
		{
			System.setSecurityManager(null);
		}

		try
		{
			Files.delete(Main.backupDirectory.resolve(".revisions.db"));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	// This SecurityManager is to disable system.exit().
	class StopExitSecurityManager extends SecurityManager
	{
		private SecurityManager _prevMgr = System.getSecurityManager();

		public void checkPermission(Permission perm)
		{}

		public void checkExit(int status)
		{
			super.checkExit(status);
			throw new SecurityException("Not allowed to exit now.");
		}

		public SecurityManager getPreviousMgr()
		{
			return _prevMgr;
		}
	}
}
