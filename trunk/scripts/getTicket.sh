#!/bin/bash

TICKET_SERVER=http://alhena:7880/authchecker-service/
USERNAME=bent
URL=http://www.test.dk/test
STREAMING_SERVER=rtmp://localhost:1937
#FILENAME=sample.mp4
FILENAME=rck_10mins.flv

java -cp ../target/lib/*:../lib/jersey-1.3/jars/* dk.statsbiblioteket.doms.wowza.plugin.utilities.TicketTool $TICKET_SERVER $USERNAME $URL $STREAMING_SERVER $FILENAME