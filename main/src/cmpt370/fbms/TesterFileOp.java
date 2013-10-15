package cmpt370.fbms;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Test case for file. This test case
 */

public class TesterFileOp
{
	private FileOp mFileOp;

	/**
	 * Prepare Test Environment: All temporary item will be created under folder TestFileOp.
	 * Temporary item: File(s) for fileSize(Path), fileValid(Path), Copy(Path, Path),
	 * copy(List<Path>) Folder(s) for delete(Path)
	 * 
	 */
	protected void setUp()
	{
		File mFile = new File("TestFileOp");
		File mFile2 = new File("TestFileOp\\ZeroSize.txt");
		File mFile3 = new File("TestFileOp\\SmallSize.txt");
		File mFile4 = new File("TestFileOp\\LargeSize.txt");
		File mFile5 = new File("TestFileOp\\BinaryFile.bin");
		mFile.mkdir();
		try
		{
			Files.createFile(mFile2.toPath());
			OutputStream smallFile = Files.newOutputStream(mFile2.toPath());
			OutputStream LargeFile = Files.newOutputStream(mFile3.toPath());
			Files.createFile(mFile5.toPath());
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void TestDelete()
	{

	}

}
