#!/bin/bash

# Author: heb
# Date:   2010-09-21
#
# Script for testing access to MCM-server. Run from within bin-folder of Wowza-installation.
#
# EXAMPLE: ./test_MCM_connection.sh
# 
# Run Java test-code that connects to the MCM server

java -classpath "../lib/*" org.junit.runner.JUnitCore dk.statsbiblioteket.larm.wowza.plugin.authentication.model.MCMSessionAndFilenameValidaterTest
