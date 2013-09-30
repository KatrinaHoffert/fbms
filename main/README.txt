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

Notes:
	- Eclipse has been configured to use the modified Java code style (demonstrated
	  in part in the demo program). Changes to this should be discussed.