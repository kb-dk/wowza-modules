#!/bin/bash

TICKETCHECKERSERVICE=http://deneb.statsbiblioteket.dk:9651/ticket-system-service
WOWZAURL=rtmp://thalassa:1935/mediestream
PROGRAM=d68a0380-012a-4cd8-8e5b-37adf6c2d47f
TICKET=$(curl "$TICKETCHECKERSERVICE/tickets/issueTicket?id=doms_radioTVCollection:uuid:d68a0380-012a-4cd8-8e5b-37adf6c2d47f&ipAddress=$(hostname -I|head -n 1|tr -d ' ')&type=Stream&SBIPRoleMapper=inhouse" 2> /dev/null|cut -d\" -f4)
ffprobe $WOWZAURL?ticket=$TICKET/flv:$PROGRAM.flv >& /dev/null
