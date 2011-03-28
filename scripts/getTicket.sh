#!/bin/bash

#TICKET_SERVER=http://alhena:7880/authchecker-service/
TICKET_SERVER=http://abr-laptop.sb:7880/authchecker-service/tickets

USERNAME=127.0.0.1

RESOURCE=a/0/6/3/a0639529-124a-453f-b4ea-59f833b47333.flv

STREAMING_SERVER=rtmp://localhost:1937

#FILENAME=sample.mp4
#FILENAME=rck_10mins.flv
#FILENAME=a/0/6/3/a0639529-124a-453f-b4ea-59f833b47333.flv
FILENAME=$RESOURCE

java -cp ../target/lib/*:../lib/jersey-1.3/jars/* dk.statsbiblioteket.doms.wowza.plugin.utilities.TicketTool $TICKET_SERVER $USERNAME $RESOURCE $STREAMING_SERVER $FILENAME