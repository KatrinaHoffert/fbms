/*
	FBMS: File Backup and Management System
	Copyright (C) 2013 Group 06

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package cmpt370.fbms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

/**
 * This class runs tests on portions of the program that cannot be automatically tested for
 * correctness, such as the GUI and various visual components.
 */
public class TesterVisual
{
	@Test
	public void dataGetFolderContents() throws IOException
	{
		Path path = Paths.get("").toAbsolutePath();
		Control.backupDirectory = path;
		DbManager.init();
		List<FileInfo> list = Data.getFolderContents(path);

		for(FileInfo file : list)
		{
			System.out.println(file.fileName);
			System.out.println("\tCreated:\t" + new Date(file.createdDate * 1000));
			System.out.println("\tLast modified:\t" + new Date(file.lastModifiedDate * 1000));
			System.out.println("\tLast accessed:\t" + new Date(file.lastAccessedDate * 1000));
			if(!file.folder)
			{
				System.out.println("\tSize:\t" + file.fileSize + " B");
				System.out.println("\tRevision count:\t" + file.numberOfRevisions);
				System.out.println("\tRevisions size:\t" + file.revisionSizes + " B");
			}
		}
		Files.delete(path.resolve(".revisions.db"));
		DbManager.close();
	}

	@Test
	public void dbManagerInsertRevision() throws IOException
	{
		Path path = Paths.get("").toAbsolutePath();
		Control.backupDirectory = path;
		Control.liveDirectory = path;
		DbManager.init();

		System.out.println("\n--------------------------------");
		System.out.println("Size of database before: "
				+ FileOp.fileSize(path.resolve(".revisions.db")));

		DbManager.insertRevision(path.resolve("README.txt"),
				FileOp.fileToString(path.resolve("README.txt")), 100);

		DbManager.renameFile(path.resolve("README.txt"), "not-readme.txt");

		List<RevisionInfo> list = DbManager.getRevisionData(path.resolve("not-readme.txt"));
		for(RevisionInfo revision : list)
		{
			System.out.println("Found revision id = " + revision.id);
			System.out.println(revision.path);
			System.out.println("Time stamp: " + revision.time + "; Delta: " + revision.delta);
			System.out.println(revision.diff);
		}

		System.out.println("Size of database after: "
				+ FileOp.fileSize(path.resolve(".revisions.db")));

		Files.delete(path.resolve(".revisions.db"));
		DbManager.close();
	}

	@Test
	@Ignore
	public void errorsFatalError()
	{
		Errors.fatalError("She turned me into a newt!");
	}

	@Test
	@Ignore
	public void errorsFatalErrorWithStackTrace()
	{
		String someString = null;

		try
		{
			someString.charAt(0); // Throws an exception
		}
		catch(NullPointerException e)
		{
			Errors.fatalError("We dun goofed...", e);
		}
	}

	@Test
	@Ignore
	public void errorsNonfatalError()
	{
		Errors.nonfatalError("Such error message!<br />&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;"
				+ "&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;So recovery!<br />&emsp;&emsp;Wow!", "Wow!");
		try
		{
			Thread.sleep(5000); // So the program doesn't instantly terminate
		}
		catch(InterruptedException e)
		{}
	}
}
