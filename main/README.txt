-------------------------------------------------------------------------------
                FBMS: File Backup and Management System
-------------------------------------------------------------------------------
    Copyright (C) 2013 Group 06: Hoffert, Rizvi, Alsharif, Tao, and Butler

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
-------------------------------------------------------------------------------

FBMS is an automated backup system written in Java. It keeps track of file
changes and stores older revisions of files.

For documentation on this project, see the wiki:
   https://code.google.com/p/fbms/wiki/Documentation

To import the project into Eclipse:
	- Go to File > Import
	- Under "General", choose "Existing Projects into Workspace"
	- As the root directory, choose the folder containing this file, which
	  should also contain a file named ".classpath" and a file named
	  ".project"
	- Leave everything else at the default and choose "finish". The project
	  should now be imported into the package explorer.
	- To verify the importation was successful, attempt to run the program.
	  It should be able to find all the imported packages.

SVN:
	- Don't add the "bin" folder to subversion. Eclipse will make this to place
	  binary files in, but because it's almost always changed on compile, it
	  shouldn't be a part of the SVN. I added the folder to the list of ignored
	  files, so it shouldn't appear when you use "svn status". However, you must
	  be careful not to accidentally add it (eg, with "svn add *").
	- Because of this, "svn add *" should never be used. Manually specify a more
	  specific wildcard, if necessary.
	- Commit often, but never commit broken code. The program must always be able
	  to compile.
	- To make a branch, use:
	  svn copy https://fbms.googlecode.com/svn/trunk https://fbms.googlecode.com/svn/branches/BRANCH_NAME

Notes:
	- Eclipse has been configured to use the modified Java code style (demonstrated
	  in part in the demo program). Changes to this should be discussed.
	- To add files that will be ignored by SVN, use "svn propedit svn:ignore . "
	  The "bin" folder and all files in the pattern "*.log" or "*.db" are already
	  being ignored.