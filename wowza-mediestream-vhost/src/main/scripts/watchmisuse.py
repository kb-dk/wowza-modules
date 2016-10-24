#!/usr/bin/python2.6

# Script that parses a log file from wowza and reports by mail users having streamed more than a given amount of streams
# Usage:
#   watchmisuse.py <logfile> <maxallowedstreams> <reportrecepient> <product>
# Example:
#   watchmisuse.py /home/wowza/logs/StreamingStat-$(date -d 'yesterday' +'%Y-%m-%d').log 20 kw@statsbiblioteket.dk,mvk@statsbiblioteket.dk "Mediestream STAGE"

import json
import sys
import smtplib
from email.mime.text import MIMEText

count = {}
maxcount = {}
report = ''
try:
    with open(sys.argv[1]) as f:
        firstline = 1
        for line in f:
            if firstline:
                firstline = 0
                continue
            parts = line.split(';',3)
            action = parts[1]
            attributes = json.loads(parts[3])
            if attributes is None:
                continue
            user = attributes.get('eduPersonPrincipalName')
            if user:
                user = str(user)
                if action == 'PLAY':
                    count.setdefault(user, 0)
                    count[user] = count[user]+1;
                    maxcount.setdefault(user, 0)
                    maxcount[user] = max(count[user], maxcount[user])
                elif action == 'STOP':
                    count[user] = count[user]-1;
except IOError:
    exit
for c in maxcount:
    if maxcount[c] >= int(sys.argv[2]):
        report += 'User {0} has streamed {1} simultaneous streams.\n'.format(c, maxcount[c])
if report:
    msg = MIMEText(report)
    msg['Subject'] = sys.argv[4] + ' misuse report'
    msg['To'] = sys.argv[3];
    msg['From'] = 'statsbiblioteket@statsbiblioteket.dk'
    s = smtplib.SMTP('post.statsbiblioteket.dk')
    s.sendmail('statsbiblioteket@statsbiblioteket.dk', sys.argv[3], msg.as_string())
    s.quit()
