#!/bin/bash

if [ $# -ne 1 ]
then
    echo "Invalid number of arguments in $0"
    echo "Syntax: $0 <environment>"
    echo "Example: $0 DEVEL"
    echo
    echo Valid environment values are:
    echo   LOCAL_heb
    echo   DEVEL_test
    echo   DEVEL
    echo   STAGE
    echo 
    exit
fi

ENVIRONMENT=$1

# ---===<<< Environment dependent settings >>>===---

if [ "$ENVIRONMENT" = "LOCAL_heb" ]
then
	# --- LOCAL-environment wowza_vhost_kultur:
	STREAMING_SERVER=rtmp://localhost:1937
	TICKET_SERVER=http://alhena:7880/authchecker-service/tickets
	USERNAME=0:0:0:0:0:0:0:1%0
	RESOURCE=http://www.statsbiblioteket.dk/doms/shard/uuid:a0639529-124a-453f-b4ea-59f833b47333
fi


if [ "$ENVIRONMENT" = "DEVEL_test" ]
then
	# --- DEVEL-environment wowza_vhost_kultur_test:
	STREAMING_SERVER=rtmp://iapetus:1937
	TICKET_SERVER=http://alhena:7880/authchecker-service/tickets
	USERNAME=172.18.224.234
	RESOURCE=http://www.statsbiblioteket.dk/doms/shard/uuid:a0639529-124a-453f-b4ea-59f833b47333
fi

if [ "$ENVIRONMENT" = "DEVEL" ]
then
	# --- DEVEL-environment wowza_vhost_kultur:
	STREAMING_SERVER=rtmp://iapetus:1935
	TICKET_SERVER=http://alhena:7880/authchecker-service/tickets
	USERNAME=172.18.224.234
	RESOURCE=http://www.statsbiblioteket.dk/doms/shard/uuid:a0639529-124a-453f-b4ea-59f833b47333
fi

if [ "$ENVIRONMENT" = "STAGE" ]
then
	# --- STAGE-environment wowza_vhost_kultur:
	STREAMING_SERVER=rtmp://adrasthea:1935
	TICKET_SERVER=http://carme:7880/authchecker-service/tickets
	USERNAME=172.18.224.234
	RESOURCE=http://www.statsbiblioteket.dk/doms/shard/uuid:6e99d50c-734c-4b66-aabb-3a1f02b02b35
fi

if [ "$STREAMING_SERVER" = "" ]
then
	echo Unkown environment $1
	exit
fi

# ---===<<< Global settings >>>===---

ORGANIZATION_ID=SB-test-org
USER_ID=SB-test-user
CHANNEL_ID=SB-test-channel
PROGRAM_TITLE=SB-test-program
PROGRAM_START=2007-03-04T00:00:00+0100

FILENAME=SB-test-news.flv

PREVIEW_FILENAME=0/d/0/c/0d0cb165-7469-4456-8f1e-06c79d026d40.preview.flv

java -cp ../target/lib/*:../build-libs/wowza/jars-wowza-version-2.2.3/*:../lib/jersey-1.3/jars/*:../lib/sbutil-0.5.2/jars/* dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketTool $TICKET_SERVER $USERNAME $RESOURCE $ORGANIZATION_ID $USER_ID $CHANNEL_ID $PROGRAM_TITLE $PROGRAM_START $STREAMING_SERVER $FILENAME $PREVIEW_FILENAME