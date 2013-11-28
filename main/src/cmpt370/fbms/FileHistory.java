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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Allows the modification and retrieval of revision data (the file history).
 */
public class FileHistory
{
	// Logger instance
	private static Logger logger = Logger.getLogger(Main.class);

	private Path file;

	/**
	 * Sets the file that is being affected by member methods.
	 * 
	 * @param inFile
	 *            The file whose history we desire to interact with.
	 */
	public FileHistory(Path inFile)
	{
		file = inFile;
	}

	/**
	 * Gets the specified revision and outputs it to a temporary file, returning the path to this
	 * file.
	 * 
	 * @param file
	 *            The path of the file (in the live directory) to get the revision of.
	 * @param timestamp
	 *            The time stamp of the specific revision.
	 * @return A path to the temporary file containing the diff that makes up that revision.
	 */
	public Path getRevisionInfo(long timestamp)
	{
		// Get the revision in question
		DbConnection db = DbConnection.getInstance();
		RevisionInfo revision = db.getSpecificRevision(file, timestamp);
		Path pathToTempFile = null;
		PrintWriter output = null;

		// Figure out the extension. However, we only add the period (making it a "real" extension)
		// if there actually was an extension (since splitting on periods will return a size 1 array
		// if there are no periods)
		String[] fileNameSplit = file.getFileName().toString().split("\\.");
		String extension = fileNameSplit[fileNameSplit.length - 1];
		if(fileNameSplit.length > 1)
		{
			extension = "." + extension;
		}

		try
		{
			// Create a temporary file for the revision
			pathToTempFile = Files.createTempFile("revision", "." + extension);

			// Write the diff or binary content to that temp file
			if(revision.diff != null)
			{
				Files.write(pathToTempFile, revision.diff.getBytes());
			}
			else
			{
				Files.write(pathToTempFile, revision.binary);
			}

			logger.info("Created temporary file at " + pathToTempFile.toString() + " for revision "
					+ file.toString() + " (" + timestamp + ")");
		}
		catch(IOException e)
		{
			Errors.nonfatalError("Could not create temporary file for revision.", e);
		}
		finally
		{
			if(output != null)
			{
				output.close();
			}
		}

		return pathToTempFile;
	}

	/**
	 * Stores the revision in the database with the supplied information.
	 * 
	 * @param file
	 *            the path to the actual file (in the live directory).
	 * @param diff
	 *            the path to the diff file.
	 * @param filesize
	 *            the new file size (ie, the file size of the file in the live directory).
	 * @param delta
	 *            change in file size.
	 */
	public void storeRevision(Path diff, byte[] binary, long filesize, long delta)
	{
		// Get the diff as a String
		String diffString = null;
		if(diff != null)
		{
			// Prevent an empty diff is inserted.
			if(FileOp.fileSize(diff) <= 0)
			{
				return;
			}
			try
			{
				diffString = FileOp.fileToString(diff);
			}
			catch(IOException e)
			{
				Errors.nonfatalError("Could not store " + file.toString() + " to database.");
			}

			// Fail safe to prevent a revision from somehow having both text and binary data
			binary = null;
		}

		DbConnection db = DbConnection.getInstance();
		db.insertRevision(file, diffString, binary, delta, filesize);

		logger.debug("Revision stored for file " + file.toString() + " (file size: " + filesize
				+ "; delta: " + delta + ")");
	}

	/**
	 * Obtain the content of the file at a specific revision on the given time stamp.
	 * 
	 * The time stamp should be valid, otherwise this method will restore the file revision nearest
	 * to the given time stamp.
	 * 
	 * Returns a Path to the patched file. If error occurs, null will be returned.
	 * 
	 * @param file
	 *            a Path to a file in live directory.
	 * @param timestamp
	 *            a long representing the file version.
	 * @return a Path of patched file. null if failed.
	 */
	public Path obtainRevisionContent(long timestamp)
	{
		// Check first if the specific revision is a binary revision. If it is, we're done.
		Path specificRevision = getRevisionInfo(timestamp);
		if(!FileOp.isPlainText(specificRevision))
		{
			logger.debug("Revision for file " + file.toString() + " (" + timestamp + ") is binary");
			return specificRevision;
		}

		logger.debug("Revision for file " + file.toString() + " (" + timestamp + ") is plain text");

		// Retrieve data from database
		DbConnection db = DbConnection.getInstance();
		List<RevisionInfo> fileRevisionList = db.getFileRevisions(file);
		LinkedList<RevisionInfo> patchList = new LinkedList<>();

		// Add the records we needed to a linked list
		for(RevisionInfo revisionInfo : fileRevisionList)
		{
			if(revisionInfo.time > timestamp && revisionInfo.diff != null)
			{
				patchList.add(revisionInfo);
			}
		}

		// Sort the linked list in reverse order
		Collections.sort(patchList);
		Collections.reverse(patchList);

		// Apply diff to the file. return null if error occurs
		Path newestFile = file;
		Path tempPatchFile = null;
		try
		{
			// Get the extension for the file (ensures the correct program is used to display the
			// file)
			String[] fileNameSplit = file.getFileName().toString().split("\\.");
			String extension = fileNameSplit[fileNameSplit.length - 1];
			if(fileNameSplit.length > 1)
			{
				extension = "." + extension;
			}

			tempPatchFile = Files.createTempFile("FBMS", extension);

			for(RevisionInfo revisionInfo : patchList)
			{
				FileOp.stringToFile(revisionInfo.diff, tempPatchFile);
				newestFile = FileOp.applyPatch(newestFile, tempPatchFile);
			}
			return newestFile;
		}
		catch(IOException e)
		{
			Errors.nonfatalError("Error occurs while applying patches.", e);
			return null;
		}
	}

	/**
	 * Renames all instances of a certain file to a new name in the revisions database. This is just
	 * a wrapper for the DbManager function.
	 * 
	 * @param file
	 *            The path of the file we are renaming.
	 * @param newName
	 *            The new name of the file. Note this does not include the full path: just the file
	 *            name (and extension).
	 */
	public void renameRevision(String newName)
	{
		DbConnection db = DbConnection.getInstance();
		db.renameRevisions(file, newName);
		logger.info("File " + file.toString() + " is renamed to " + newName);
	}
}
