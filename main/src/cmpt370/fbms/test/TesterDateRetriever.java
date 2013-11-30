package cmpt370.fbms.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;
import cmpt370.fbms.DataRetriever;
import cmpt370.fbms.DbConnection;
import cmpt370.fbms.FileHistory;
import cmpt370.fbms.FileInfo;
import cmpt370.fbms.Main;

public class TesterDateRetriever extends TestCase
{

	Path path;
	DbConnection db;

	// Prepare DB and path.
	public void setUp()
	{
		path = Paths.get("").toAbsolutePath();
		Main.backupDirectory = path;
		Main.liveDirectory = path;
		db = DbConnection.getInstance();
		db.initConnection();
	}

	// Test getting folder contents
	public void test_GetFolderContents() throws IOException
	{

		// Get the folder contents of this directory
		DataRetriever revisionRetriever = new DataRetriever(Paths.get("").toAbsolutePath());
		List<FileInfo> list = revisionRetriever.getFolderContents();

		// Find the readme and assert that the information on it is logical
		boolean foundReadme = false;
		for(FileInfo file : list)
		{
			if(file.fileName.equals("README.txt"))
			{
				foundReadme = true;
				assertTrue(file.fileSize > 0);
				assertTrue(!file.folder);
				assertTrue(file.lastAccessedDate != 0);
				assertTrue(file.lastModifiedDate != 0);
				assertTrue(file.createdDate != 0);
			}
		}

		assertTrue(foundReadme);

	}

	public void test_GetRevisionInfo()
	{

		// Insert revisions for a file
		FileHistory fileHist = new FileHistory(path.resolve("README.txt"));
		fileHist.storeRevision(path.resolve("README.txt"), null, 100, 200);
		fileHist.storeRevision(path.resolve("license.txt"), null, 300, 400);
		fileHist.storeRevision(path.resolve("authors.txt"), null, 500, 600);

		// Get the revision table for that file
		DataRetriever dataRetriever = new DataRetriever(path.resolve("README.txt"));
		Vector<Vector<String>> tableData = dataRetriever.getRevisionInfoTable();

		// And print it out. Since it depends on time, no assertion could be used.

		System.out.println("Please check time-depend result:");
		for(Vector<String> rows : tableData)
		{
			for(String data : rows)
			{
				System.out.println(data);
			}
		}

	}

	// Close Dbconnection and clean up
	public void tearDown()
	{
		db.close();
		try
		{
			Files.deleteIfExists(Paths.get(".revisions.db"));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

}
