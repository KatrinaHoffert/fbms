/*
	FBMS: File Backup and Management System
	Copyright (C) 2013 Group 06

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
