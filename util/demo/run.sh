## ==============================================================
##	This file is part of FBMS (https://code.google.com/p/fbms)
##
##	Copyright (C) 2013 Group 06
##
##	You can redistribute this code and/or modify it under
##	the terms of the GNU General Public License as published
##	by the Free Software Foundation; either version 3 of the
##	License, or (at your option) any later version
##	==============================================================

# This is for running the program after ant has already built it with
# "ant compile". See run.bat for the Windows version.
cd build
# Extend the classpath as necessary. Note that Linux separates paths with a
# colon, different from Windows, which uses a semicolon. Also note that for
# whatever reason, linux requires that we specify the path to the library
# (.so) file with -Djava.library.path
java -classpath ../lib/jnotify-0.94.jar:../lib/sqlite-jdbc-3.7.15-M1.jar:../lib/java-diff-utils-1.3.0.jar:../lib/log4j-1.2.17.jar:. -Djava.library.path=. DemoMain
cd ../