#!/usr/bin/python
# vim: set fileencoding=utf-8 :
# the above line is also used by Python
# TODO Must be updated to new log file

import datetime
import os.path
import smtplib

alertLevel = 10
pathToLogDirectory = "./"

# Mail konfiguration
SERVER = "post"
FROM = "modtager@statsbiblioteket.dk"
TO = ["modtager@statsbiblioteket.dk"] # must be a list
SUBJECT = "Advarsel om potentielt misbrug af Mediestream"


# calculate a list of log file names for the last week. The list will only
# contain names for log files that actually exist.
today = datetime.datetime.now()
aWeekAgo = today - datetime.timedelta(weeks=1)

logFileNames = filter(lambda n: os.path.isfile(n),
		map(lambda d: d.strftime(pathToLogDirectory + "StreamingStat-%Y-%m-%d.log"), 
			map(lambda i: aWeekAgo + datetime.timedelta(days=i), range(7))))

# read the content of all the log files in the list
rawData = map(lambda filename: 
	open(filename,'r').readlines(), logFileNames)

# apparently Python does not have a flatten built-in?
# this function flattens the first level in a list: [[a,b],[c,d],[e]] => [a,b,c,d,e]
flatten = lambda it: (y for x in it for y in x)

# flatten the data list and split each element on ';' to get a list of lists
data = map(lambda e: e.split(';'), list(flatten(rawData)))

# Get a list of user names and remove the system names
userIds = filter(lambda name: name != 'User ID' and name != 'no user info',
		map(lambda e: e[3],data))
# make a list of unique user names
uniqueUserIds = set(userIds)

# Create a list of (user id, no. of initiated streams for user with id) where no. of
# initiated streams exceeds the threshold.
alertList = filter(lambda e: e[1] > alertLevel,
		map(lambda userId: [userId, userIds.count(userId)],uniqueUserIds))

if len(alertList) > 0:
	body= """\
Overågningen af Mediestream viser at følgende brugere har overskredet
den definerede kvota på %s påbegyndte streams indenfor løbende syv dage.

Den enkelte brugers påbegyndte antal stream vises i parentes.\n\n""" % (alertLevel)
	for a in alertList:
	  body += a[0] + ': ('+str(a[1])+')\n'

	# Prepare actual message
	message = """\
From: %s
To: %s
Subject: %s

%s
""" % (FROM, ", ".join(TO), SUBJECT + ' - ' + str(len(alertList)) + ' brugere over kvota', body)
	#print body
	#raise SystemExit
	# Send the mail

	server = smtplib.SMTP(SERVER)
	server.sendmail(FROM, TO, message)
	server.quit()

