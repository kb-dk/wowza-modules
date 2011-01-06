@echo off

REM Author: heb
REM Date:   2010-09-21
REM
REM Script for testing access to MCM-server. Run from within bin-folder of Wowza-installation.
REM
REM EXAMPLE: test_MCM_connection.bat
REM 
REM Run Java test-code that connects to the MCM server

java -classpath "../lib/*" org.junit.runner.JUnitCore dk.statsbiblioteket.larm.wowza.plugin.authentication.model.MCMSessionAndFilenameValidaterTest
