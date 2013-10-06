package cmpt370.fbms;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

// Making sure libraries are found (remove later)
import org.apache.log4j.*;
import difflib.*;
import net.contentobjects.jnotify.*;

public class Control
{
	// Various public variables shared amongst components
	public static Path liveDirectory;
	public static Path backupDirectory;
	public static List<Path> createdFiles = new LinkedList<>();
	public static List<Path> modifiedFiles = new LinkedList<>();
	public static List<RenamedFile> renamedFiles = new LinkedList<>();
	public static List<Path> deletedFiles = new LinkedList<>();
	
	// Logger object is linked to the class
	public static Logger logger = Logger.getLogger(Control.class);
	
	public static void main(String[] args)
	{
		// Load the properties file for logger settings
		PropertyConfigurator.configure("log4j.properties");

		// And print a testing message to the log
		logger.info("Program started");
	}
	
	private static void handleCreatedFiles()
	{
		
	}
	
	private static void handleModifiedFiles()
	{
		
	}

	private static void handleRenamedFiles()
	{
		
	}

	private static void handleDeletedFiles()
	{
		
	}
	
	public static void displayRevision(Path file, int timestamp)
	{
		
	}
	
	public static void displayRevisionChanges(Path file, int timestamp)
	{
		
	}
	
	public static void revertRevision(Path file, int timestampt)
	{
		
	}
	
	public static void restoreBackup()
	{
		
	}
	
	public static void copyTo(Path sourceFile, Path destFolder)
	{
		
	}
}
