#!/bin/bash
#
# Author: heb
# Date:   2011-04-06
#
# EXAMPLE: ./streaming_statistics.sh <logfile>
# 
java -cp ../target/lib/*:../build-libs/*:../build-libs/wowza/jars-wowza-version-2.2.3/*:../lib/jersey-1.3/jars/*:../lib/sbutil-0.5.2/jars/*  dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics.StreamingStatExtractor $*

