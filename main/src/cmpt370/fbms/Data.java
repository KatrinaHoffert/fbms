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
import java.util.TimeZone;
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
			row.add(Long.toString(file.fileSize));
			row.add(Data.formatDate(file.createdDate));
			row.add(Data.formatDate(file.lastAccessedDate));
			row.add(Data.formatDate(file.lastModifiedDate));
			row.add(Integer.toString(file.numberOfRevisions));
			row.add(Long.toString(file.revisionSizes));

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

		// Set the timezone so we have the correct offset
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		// Note that the Date object is created with milliseconds, while we have seconds
		Date date = new Date(timestamp * 1000);

		return dateFormat.format(date);
	}
}
