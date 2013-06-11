#!/usr/bin/python

# Script that parses a log file from wowza and reports by mail if streams have been played with different referrer than jwplayer
# Usage:
#   watchplayer.py <logfile> <reportrecepient> <product>
# Example:
#   watchplayer.py /home/wowza/wowza/logs/wowzamediaserver_access.log.$(date -d 'yesterday' +'%Y-%m-%d') kw@statsbiblioteket.dk,mvk@statsbiblioteket.dk "Mediestream STAGE"

import sys
import smtplib
from email.mime.text import MIMEText

players = {}
report = ''
try:
    with open(sys.argv[1]) as f:
        for line in f:
            parts = line.split('\t')
            if len(parts) < 18:
                continue
            action = parts[3]
            if action == 'play':
                player = parts[18]
                players.setdefault(player, 0)
                players[player] = players[player]+1
except IOError:
    exit
for p in players:
    if not(p.endswith('jwplayer.flash.swf')):
        report += 'Streams have been played with non-approved player \'{0}\' {1} times.\n'.format(p, players[p])
if report:
    msg = MIMEText(report)
    msg['Subject'] = sys.argv[3] + ' misuse report'
    msg['To'] = sys.argv[2];
    msg['From'] = 'statsbiblioteket@statsbiblioteket.dk'
    s = smtplib.SMTP('post.statsbiblioteket.dk')
    s.sendmail('statsbiblioteket@statsbiblioteket.dk', sys.argv[2], msg.as_string())
    s.quit()
