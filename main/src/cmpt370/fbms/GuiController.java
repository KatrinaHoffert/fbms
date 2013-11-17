package cmpt370.fbms;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;

/**
 * Controller class for handling the GUI functionality.
 */
public class GuiController
{
	/**
	 * Obtains the specified revision and displays it in the default program for that type of file.
	 * 
	 * @param file
	 *            The file's path (in the backup directory).
	 * @param timestamp
	 *            The Unix time stamp (seconds since Unix epoch).
	 */
	public static void displayRevision(Path file, long timestamp)
	{
		FileHistory fileHist = new FileHistory(FileOp.convertPath(file));
		Path fileToOpen = fileHist.obtainRevisionContent(timestamp);
		try
		{
			Desktop.getDesktop().open(fileToOpen.toFile());
		}
		catch(IOException e)
		{
			Errors.nonfatalError("Could not open revision file.", e);
		}
	}

	/**
	 * Reverts a revision by obtaining a specific revision and making that the current revision.
	 * 
	 * @param file
	 *            The path to the file (in the backup directory) to revert. The file must exist.
	 * @param timestamp
	 *            The time stamp (in Unix time stamp format) of the revision we want.
	 */
	public static void revertRevision(Path file, long timestamp)
	{
		if(file.toFile().exists())
		{
			// Get the revision we want
			FileHistory fileHist = new FileHistory(FileOp.convertPath(file));
			Path revertedFile = fileHist.obtainRevisionContent(timestamp);
			Path diffFromCurrent = FileOp.createPatch(file, revertedFile);

			// Get the filesize of our newly reverted file as well as the delta from the old
			// file
			long fileSize = FileOp.fileSize(revertedFile);
			long delta = fileSize - FileOp.fileSize(file);

			// Store the revision and copy the reverted file over the backup directory
			fileHist.storeRevision(diffFromCurrent, null, fileSize, delta);

			Main.logger.debug("Revert to: " + revertedFile.toFile().toString());
			try
			{
				// Copy into both live and backup directories
				Files.copy(revertedFile, FileOp.convertPath(file),
						StandardCopyOption.REPLACE_EXISTING);
				Files.copy(revertedFile, file, StandardCopyOption.REPLACE_EXISTING);

				Main.logger.debug("Copied " + revertedFile.toFile().toString() + " as "
						+ FileOp.convertPath(file).toFile().toString());
			}
			catch(IOException e)
			{
				Errors.nonfatalError("Could not copy file " + revertedFile.toString() + " to "
						+ FileOp.convertPath(file).toString(), e);
			}
		}
		else
		{
			Errors.nonfatalError("The file you wanted to revert does not exist!");
		}
	}

	/**
	 * Copies all files in the backup directory to the live directory.
	 */
	public static void restoreBackup(Path directory)
	{
		// Iterate through all files in the backup folder, copying them to the live directory
		for(File child : Main.backupDirectory.toFile().listFiles())
		{
			// If we're copying a directory, we must specify the folder name instead of destination
			if(child.isDirectory())
			{
				FileOp.copy(child.toPath(), directory.resolve(child.getName()));
			}
			else
			{
				FileOp.copy(child.toPath(), directory);
			}
		}
	}

	/**
	 * Just a wrapper function for the FrontEnd.
	 * 
	 * @param sourceFile
	 *            The path of the file we want to copy.
	 * @param destFolder
	 *            The folder we want to copy this file to.
	 */
	public static void copyTo(Path sourceFile, Path destFolder)
	{
		// If we're copying a directory, we must specify the folder name instead of destination
		if(sourceFile.toFile().isDirectory())
		{
			FileOp.copy(sourceFile, destFolder.resolve(sourceFile.getFileName()));
		}
		else
		{
			FileOp.copy(sourceFile, destFolder);
		}
	}

	/**
	 * Changes the live directory. The new live directory is set in the program and database, the
	 * watcher is reassigned, and the contents of the live directory are backed up (and revisioned
	 * if applicable).
	 * 
	 * This does not check if the live directory is valid (eg, not a child of the backup directory).
	 * That is a pre-condition.
	 * 
	 * As of the time of implementation, this is currently not called from anywhere. It is expected
	 * to be added to future versions of the FrontEnd.
	 * 
	 * @param newDirectory
	 *            The new directory to use for the live directory.
	 */
	public static void changeLiveDirectory(Path newDirectory)
	{
		// Set the new directory in the database
		Main.liveDirectory = newDirectory;
		DbManager db = DbManager.getInstance();
		db.setConfig("liveDirectory", Main.liveDirectory.toString());

		// Remove the old watcher
		try
		{
			JNotify.removeWatch(Main.watchId);
		}
		catch(JNotifyException e)
		{
			Errors.fatalError(
					"Could not remove old watcher. This problem might be fixed by a restart.", e);
		}

		// Empty the lists, since those files aren't the live directory, anymore
		synchronized(Main.deletedFiles)
		{
			synchronized(Main.createdFiles)
			{
				synchronized(Main.modifiedFiles)
				{
					synchronized(Main.renamedFiles)
					{
						Main.deletedFiles.clear();
						Main.createdFiles.clear();
						Main.modifiedFiles.clear();
						Main.renamedFiles.clear();
					}
				}
			}
		}

		// Copy any existing files in the live directory to the backup directory.
		new Startup().startupScan(Main.liveDirectory);

		// And add a new watcher
		try
		{
			Main.watchId = JNotify.addWatch(Main.liveDirectory.toString(), JNotify.FILE_CREATED
					| JNotify.FILE_DELETED | JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED, true,
					new Watcher());
		}
		catch(JNotifyException e)
		{
			Errors.fatalError("Could not add a file watcher.", e);
		}
	}

	/**
	 * Changes the backup directory path. The contents of the existing backup directory will be
	 * copied to the new directory, and the file pointing to the location of the backup directory
	 * will be updated.
	 * 
	 * The previous backup directory is NOT deleted, but will be left as a dangling folder. Perhaps
	 * the user should be prompted if they want to delete that old backup directory.
	 * 
	 * This does not check if the backup directory is valid (eg, not a child of the live directory).
	 * That is a pre-condition.
	 * 
	 * As of the time of implementation, this is currently not called from anywhere. It is expected
	 * to be added to future versions of the FrontEnd.
	 * 
	 * @param newDirectory
	 */
	public static void changeBackupDirectory(Path newDirectory)
	{
		// Iterate through all files in the backup folder, copying them to the new backup directory
		for(File child : Main.backupDirectory.toFile().listFiles())
		{
			if(child.isDirectory())
			{
				FileOp.copy(child.toPath(), newDirectory.resolve(child.getName()));
			}
			else
			{
				FileOp.copy(child.toPath(), newDirectory);
			}
		}

		Main.backupDirectory = newDirectory;

		// Write the backup path to the disk
		FileOutputStream out;
		try
		{
			out = new FileOutputStream("backup_location");
			out.write(Main.backupDirectory.toString().getBytes());
			out.close();

			Main.logger.info("Backup location file modified, set to: "
					+ Main.backupDirectory.toString());
		}
		catch(IOException e)
		{
			Errors.fatalError(
					"Could not write backup path to disk. Is the program folder writeable?", e);
		}
	}
}
