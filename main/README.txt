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

FBMS works on Windows and Linux.

For documentation on this project, see the wiki:
   https://code.google.com/p/fbms/wiki/Documentation

RUNNING THE PROGRAM (pre-built distribution only):
  - Windows users can run the "run.cmd" file (either double click it or type
    the name in the command line)
  - Linux users can run the "run.sh" file via the terminal
  - The working directory must be the directory that the JAR file is in (same
    directory as the "run" files are in)

COMPILING IT YOURSELF:
  - The pre-built distribution only includes the necessary files to run the
    program. To build it yourself, you'll need the full SVN repository.
    See: https://code.google.com/p/fbms/source/checkout
  - The "coding.txt" file in the SVN (located in trunk/doc) contains details
    about building the program from source and importing the project into
    the Eclipse IDE