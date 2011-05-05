#!/bin/bash
#
# Author: heb
# Date:   2011-04-06
#
# EXAMPLE: ./streaming_statistics.sh ~/services/wowza_vhost_kultur/logs/streamingStatistics 2011-04-01 2011-05-01
# 


#WMSAPP_HOME=/Library/WowzaMediaServer
WMSAPP_HOME=/home/wowza/wowza

echo Location of Wowza Media Server: $WMSAPP_HOME
echo Starting extractor...

java -cp ../applications/lib/*:${WMSAPP_HOME}/lib/*  dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics.StreamingStatExtractor $*

