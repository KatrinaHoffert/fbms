:: ==============================================================
::	This file is part of FBMS (https://code.google.com/p/fbms)
::
::	Copyright (C) 2013 Group 06
::
::	You can redistribute this code and/or modify it under
::	the terms of the GNU General Public License as published
::	by the Free Software Foundation; either version 3 of the
::	License, or (at your option) any later version
:: ==============================================================

:: This is for running the program after ant has already built it with
:: "ant compile". See run.sh for the Linux version.
@echo off
cd build
:: Extend the classpath as necessary. Note that Windows separates paths with a
:: semicolon, different from Linux, which uses a colon
java -classpath ../lib/jnotify-0.94.jar;../lib/sqlite-jdbc-3.7.15-M1.jar;../lib/java-diff-utils-1.3.0.jar;../lib/log4j-1.2.17.jar;. DemoMain
cd ../