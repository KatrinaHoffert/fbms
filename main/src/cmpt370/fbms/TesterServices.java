package cmpt370.fbms;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

/**
 * This class runs tests that can be automated. There must not be any output that has to be examined
 * or such. This allows us to simply run the file and JUnit will report a success or failure.
 */
public class TesterServices
{
	@Test
	public void dataGetFolderContents()
	{
		List<FileInfo> list = Data.getFolderContents(Paths.get("").toAbsolutePath());

		boolean foundReadme = false;

		for(FileInfo file : list)
		{
			// Ensure we found the readme file and that its traits are logical
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

	@Test
	public void dbManagerGetSetConfig()
	{
		// Create database in current directory
		Control.backupDirectory = Paths.get("").toAbsolutePath();
		DbManager.init();

		DbManager.setConfig("liveDirectory", "/some/other/path");
		assertTrue(DbManager.getConfig("liveDirectory").equals("/some/other/path"));
		DbManager.close();

		try
		{
			Files.delete(Control.backupDirectory.resolve(".revisions.db"));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
