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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class Data
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

				List<RevisionInfo> revisionInfoList = DbManager.getRevisionData(file.toPath());
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

		return list;
	}

	// public static List<RevisionInfo> getFileInfo(Path File){
	//
	// File[] fileInfo;
	//
	// List<RevisionInfo> list = new LinkedList<>();
	//
	// for(File file : fileInfo)
	// {
	// FileInfo currentFile = new FileInfo();
	//
	// // String fileName
	// currentFile.fileName = file.getName();
	//
	//
	// try
	// {
	// BasicFileAttributes attributes = Files.readAttributes(file.toPath(),
	// BasicFileAttributes.class);
	//
	// // long lastAccessedDate
	// currentFile.lastAccessedDate = attributes.lastAccessTime().to(TimeUnit.SECONDS);
	//
	// // long lastModifiedDate
	// currentFile.lastModifiedDate = attributes.lastModifiedTime().to(TimeUnit.SECONDS);
	//
	// // long createdDate
	// currentFile.createdDate = attributes.creationTime().to(TimeUnit.SECONDS);
	// }
	// catch(IOException e)
	// {
	// Errors.nonfatalError("Could not read file attributes of " + file.getName(), e);
	// }
	//
	// // Leave the number of revisions and file sizes out if it's a folder
	// if(!currentFile.folder)
	// {
	// // long fileSize
	// try
	// {
	// currentFile.fileSize = Files.size(file.toPath());
	// }
	// catch(IOException e)
	// {
	// Errors.nonfatalError("Could not read file size of " + file.getName(), e);
	// }
	//
	// List<RevisionInfo> revisionInfoList = DbManager.getRevisionData(file.toPath());
	// int totalRevisions = 0;
	// long totalSizes = 0;
	//
	// if(revisionInfoList != null)
	// {
	// for(RevisionInfo revisionInfo : revisionInfoList)
	// {
	// totalRevisions++;
	// totalSizes += revisionInfo.diff.length() * 2; // characters are 2 bytes
	// }
	//
	// // int numberOfRevisions
	// currentFile.numberOfRevisions = totalRevisions;
	//
	// // long revisionSizes
	// currentFile.revisionSizes = totalSizes;
	// }
	// }
	//
	// //list.add(currentFile);
	// }
	//
	// return List;
	//
	// }
	/**
	 * Takes in a path to a folder and outputs a Vector of Vectors. The parent vector is the rows.
	 * The vector inside this is the columns.
	 * 
	 * @param folder
	 *            The folder to create a table of vectors for.
	 * @return A vector of vectors of strings (a 2D vector of Strings) that the JTable can be
	 *         created from.
	 */
	public static Vector<Vector<String>> getTableData(Path folder)
	{
		Vector<Vector<String>> tableData = new Vector<>();

		// Get the contents of the folder
		List<FileInfo> files = Data.getFolderContents(folder);

		for(FileInfo file : files)
		{
			// For each file in the folder, create a Vector that will become a row in
			// our table.
			Vector<String> row = new Vector<String>();

			// Populate the columns
			row.add(file.fileName);
			row.add(Data.humanReadableByteCount(file.fileSize, false));
			row.add(Data.formatDate(file.createdDate));
			row.add(Data.formatDate(file.lastAccessedDate));
			row.add(Data.formatDate(file.lastModifiedDate));
			row.add(Integer.toString(file.numberOfRevisions));
			row.add(Data.humanReadableByteCount(file.revisionSizes, false));

			tableData.add(row);

		}

		return tableData;
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
		return DbManager.getRevisionData(file);
	}

	/**
	 * Takes in a Unix time stamp and formats it as a human readable String
	 * 
	 * @param timestamp
	 *            The Unix time stamp (seconds since 1970-01-01 00:00:00)
	 * @return A String in ISO 8601 format.
	 */
	public static String formatDate(long timestamp)
	{
		// Specify the format that the date should be in (like ISO 8601)
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// Note that the Date object is created with milliseconds, while we have seconds
		Date date = new Date(timestamp * 1000);

		return dateFormat.format(date);
	}

	/**
	 * Takes in a number of bytes and converts it to a human readable format (eg, 51355 bytes
	 * becomes 50.1 KiB.
	 * 
	 * @author aioobe <http://stackoverflow.com/users/276052/aioobe> from
	 *         <http://stackoverflow.com/a/3758880>
	 * 
	 * @param bytes
	 *            The number of bytes.
	 * @param si
	 *            If false, use powers of 2, if true, use powers of 10. For example, if using powers
	 *            of 2, then 1024 bytes = 1.0 KiB, while using powers of 10 has 1000 bytes = 1.0 kB.
	 * @return A String representation of a human readable byte count.
	 */
	public static String humanReadableByteCount(long bytes, boolean si)
	{
		// Figure out if we're using powers of 2 (non-SI) or 10 (SI)
		int unit = si ? 1000 : 1024;
		if(bytes < unit)
		{
			return bytes + " B";
		}

		// Figure out the unit being used
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");

		// Return as a formatted string
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
