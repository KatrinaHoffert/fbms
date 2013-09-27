:: This is for running the program after ant has already built it with
:: "ant compile". See run.sh for the Linux version.
@echo off
cd build
:: Extend the classpath as necessary. Note that Windows separates paths with a
:: semicolon, different from Linux, which uses a colon
java -classpath ../lib/jnotify-0.94.jar;../lib/sqlite-jdbc-3.7.15-M1.jar;../lib/diffutils-1.2.1.jar;../lib/log4j-1.2.17.jar;. DemoMain
cd ../