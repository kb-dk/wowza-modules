#!/bin/bash

WOWZA_LIB=../build-libs/wowza/jars/*

#TICKET_SERVER=http://abr-laptop.sb:7880/authchecker-service/tickets
#TICKET_SERVER=http://alhena:7980/authchecker-service/tickets
TICKET_SERVER=http://alhena:7880/authchecker-service/tickets

#USERNAME=127.0.0.1
USERNAME=0:0:0:0:0:0:0:1%0
#USERNAME=172.18.224.234

#RESOURCE=a/0/6/3/a0639529-124a-453f-b4ea-59f833b47333.flv
RESOURCE=http://www.statsbiblioteket.dk/doms/shard/uuid:a0639529-124a-453f-b4ea-59f833b47333

# DEVEL-environment wowza_vhost_kultur:
#STREAMING_SERVER=rtmp://iapetus:1935
# DEVEL-environmentwowza_vhost_kultur_test:
#STREAMING_SERVER=rtmp://iapetus:1937
# DEVEL-environment:
STREAMING_SERVER=rtmp://localhost:1937

#FILENAME=sample.mp4
#FILENAME=rck_10mins.flv
#FILENAME=a/0/6/3/a0639529-124a-453f-b4ea-59f833b47333.flv
FILENAME=tv-a.flv

java -cp ../target/lib/*:${WOWZA_LIB}:../lib/jersey-1.3/jars/*:../lib/sbutil-0.5.2/jars/* dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketTool $TICKET_SERVER $USERNAME $RESOURCE $STREAMING_SERVER $FILENAME