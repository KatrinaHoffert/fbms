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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;

import cmpt370.fbms.gui.GuiUtility;

/**
 * DataRetriever fetches information for the GUI, bridging the gap between the GUI and data such as
 * folder and revision contents.
 */
public class DataRetriever
{
	/**
	 * Loops through all the files in a specified folder and returns a list of information about
	 * these files (and folders).
	 * 
	 * @param folder
	 *            The folder to search in
	 * @return A list of FileInfo objects that detail
	 */
	public static List<FileInfo> getFolderContents(Path folder)
	{
		File[] folderContents = folder.toFile().listFiles();

		if(folderContents == null)
		{
			Errors.fatalError("Attempted to retreive folder contents of non-folder.");
		}

		List<FileInfo> list = new LinkedList<>();

		for(File file : folderContents)
		{
			if(folder.equals(Main.backupDirectory)
					&& file.toPath().getFileName().toString().equals(".revisions.db"))
			{
				continue;
			}

			FileInfo currentFile = new FileInfo();

			// String fileName
			currentFile.fileName = file.getName();

			// boolean folder
			currentFile.folder = file.isDirectory();

			try
			{
				BasicFileAttributes attributes = Files.readAttributes(file.toPath(),
						BasicFileAttributes.class);

				// long lastAccessedDate
				currentFile.lastAccessedDate = attributes.lastAccessTime().to(TimeUnit.SECONDS);

				// long lastModifiedDate
				currentFile.lastModifiedDate = attributes.lastModifiedTime().to(TimeUnit.SECONDS);

				// long createdDate
				currentFile.createdDate = attributes.creationTime().to(TimeUnit.SECONDS);
			}
			catch(IOException e)
			{
				Errors.nonfatalError("Could not read file attributes of " + file.getName(), e);
			}

			// Leave the number of revisions and file sizes out if it's a folder
			if(!currentFile.folder)
			{
				// long fileSize
				try
				{
					currentFile.fileSize = Files.size(file.toPath());
				}
				catch(IOException e)
				{
					Errors.nonfatalError("Could not read file size of " + file.getName(), e);
				}

				List<RevisionInfo> revisionInfoList = DbManager.getFileRevisions(FileOp.convertPath(file.toPath()));
				int totalRevisions = 0;
				long totalSizes = 0;

				if(revisionInfoList != null)
				{
					for(RevisionInfo revisionInfo : revisionInfoList)
					{
						totalRevisions++;
						totalSizes += revisionInfo.diff.length() * 2; // characters are 2 bytes
					}

					// int numberOfRevisions
					currentFile.numberOfRevisions = totalRevisions;

					// long revisionSizes
					currentFile.revisionSizes = totalSizes;
				}
			}

			list.add(currentFile);
		}
		Collections.sort(list);

		return list;
	}

	/**
	 * Just a wrapper so that the FrontEnd can access the revision info for a file easily.
	 * 
	 * @param file
	 *            The path of the file revision info is required for.
	 * @return A list of RevisionInfo objects containing ALL the revision information about the
	 *         file.
	 */
	public static List<RevisionInfo> getRevisionInfo(Path file)
	{
		return DbManager.getFileRevisions(file);
	}

	/**
	 * Takes in a path to a folder and outputs a Vector of Vectors. The parent vector is the rows.
	 * The vector inside this is the columns.
	 * 
	 * @param folder
	 *            The folder to create a table of vectors for.
	 * @return A vector of vectors of strings (a 2D vector of Strings) that the JTable can be
	 *         created from.
	 */
	public static Vector<Vector<Object>> getFolderContentsTable(Path folder)
	{
		Vector<Vector<Object>> tableData = new Vector<>();

		// Get the contents of the folder
		List<FileInfo> files = DataRetriever.getFolderContents(folder);

		Main.logger.debug("Found " + files.size() + " entries for " + folder.toString());

		// Add folders
		for(FileInfo file : files)
		{
			Vector<Object> row = null;

			if(file.folder)
			{
				row = new Vector<>();

				row.add(new ImageIcon("res/folder.png"));
				row.add(file.fileName);
				row.add(GuiUtility.humanReadableBytes(file.fileSize, false));
				row.add(GuiUtility.formatDate(file.createdDate));
				row.add(GuiUtility.formatDate(file.lastAccessedDate));
				row.add(GuiUtility.formatDate(file.lastModifiedDate));
				row.add(Integer.toString(file.numberOfRevisions));
				row.add(GuiUtility.humanReadableBytes(file.revisionSizes, false));

				tableData.add(row);
			}
		}

		// Add files
		for(FileInfo file : files)
		{
			Vector<Object> row = null;

			if(!file.folder)
			{
				row = new Vector<>();

				row.add(new ImageIcon("res/file.png"));
				row.add(file.fileName);
				row.add(GuiUtility.humanReadableBytes(file.fileSize, false));
				row.add(GuiUtility.formatDate(file.createdDate));
				row.add(GuiUtility.formatDate(file.lastAccessedDate));
				row.add(GuiUtility.formatDate(file.lastModifiedDate));
				row.add(Integer.toString(file.numberOfRevisions));
				row.add(GuiUtility.humanReadableBytes(file.revisionSizes, false));

				tableData.add(row);
			}
		}

		return tableData;
	}

	/**
	 * Taking in path of a file and returning a vector that can be used to populate a table.
	 * 
	 * @param file
	 *            path to a file for which all the data needs to be generated.
	 * @return A vector of vectors of strings (a 2D vector of Strings) that the JTable can be
	 *         created from.
	 */
	public static Vector<Vector<String>> getRevisionInfoTable(Path file)
	{
		Vector<Vector<String>> revisionData = new Vector<>();
		List<RevisionInfo> revisions = getRevisionInfo(file);

		Collections.sort(revisions);

		Main.logger.debug("Found " + revisions.size() + " entries for " + file.toString());

		for(RevisionInfo revision : revisions)
		{
			Vector<String> row = new Vector<String>();

			row.add(GuiUtility.formatDate(revision.time));
			row.add(GuiUtility.humanReadableBytes(revision.filesize, false));
			if(revision.delta < 0)
			{
				row.add("<html><span style=\"color: red;\">"
						+ GuiUtility.humanReadableBytes(revision.delta, false));
			}
			else if(revision.delta > 0)
			{
				row.add("<html><span style=\"color: green;\">+"
						+ GuiUtility.humanReadableBytes(revision.delta, false));
			}
			else
			{
				row.add("<html><span style=\"color: gray;\">"
						+ GuiUtility.humanReadableBytes(revision.delta, false));
			}

			revisionData.add(row);
		}

		return revisionData;
	}
}
