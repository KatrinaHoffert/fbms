// ==============================================================
//	This file is part of FBMS (https://code.google.com/p/fbms)
//
//	Copyright (C) 2013 Group 06
//
//	You can redistribute this code and/or modify it under
//	the terms of the GNU General Public License as published
//	by the Free Software Foundation; either version 3 of the
//	License, or (at your option) any later version
// ==============================================================

import difflib.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import org.apache.log4j.*;

class DemoDiff
{
	// Get an instance of the DemoMain logger
	static Logger logger = Logger.getLogger("DemoMain");

	/**
	 * Converts a given file into a list of Strings.
	 * @param filename Path of file to convert
	 */
	public static List<String> fileToList(Path filename)
	{
		List<String> lines = new LinkedList<>();
		String line = "";

		try
		{
			// Create a buffered reader that reads in a file, taking in the path
			// passed into the function.
			BufferedReader in = new BufferedReader(new FileReader(filename.toFile()));
			// While there are remaining lines, read them into the list
			while ((line = in.readLine()) != null)
			{
				lines.add(line);
			}
		}
		catch (IOException e)
		{
			logger.error(e);
		}

		return lines;
	}
}