package cmpt370.fbms;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
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

	public static List<RevisionInfo> getRevisionInfo(Path file)
	{
		return null;
	}
}
