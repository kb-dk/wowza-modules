#!/bin/bash

TICKETCHECKERSERVICE=http://deneb.statsbiblioteket.dk:9651/ticket-system-service
WOWZAURL=rtmp://thalassa:1935/mediestream
PROGRAM=d68a0380-012a-4cd8-8e5b-37adf6c2d47f
IPADDR=$(hostname -i)
TICKET=$(curl "$TICKETCHECKERSERVICE/tickets/issueTicket?id=doms_radioTVCollection:uuid:$PROGRAM&ipAddress=$IPADDR&type=Stream&SBIPRoleMapper=inhouse" 2> /dev/null|cut -d\" -f4)
rtmpdump -q --stop=.1  -o /dev/null -r $WOWZAURL?ticket=$TICKET/flv:$PROGRAM.flv
EXITCODE=$?
if [ $EXITCODE -eq 2 ]; then
  exit 0
else
  exit $EXITCODE
fi
