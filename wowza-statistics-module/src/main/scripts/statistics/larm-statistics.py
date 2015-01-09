#!/usr/bin/env python2.4

# NO-272 streamingstatistik for larm.fm.

#from lxml import etree as ET
import ConfigParser
import psycopg2
import csv
import datetime
#import simplejson
#import os
import re
import sys
import time
import cgi
import cgitb
import urllib2

config_file_name = "../../larm-statistics.py.cfg"

cgitb.enable()  # web page feedback in case of problems
parameters = cgi.FieldStorage()

encoding = "utf-8"  # What to convert non-ASCII chars to.

config = ConfigParser.SafeConfigParser()
config.read(config_file_name)

doms_url = config.get("cgi", "doms_url")  # .../fedora/

# Example: d68a0380-012a-4cd8-8e5b-37adf6c2d47f (optionally trailed by a ".fileending")
re_doms_id_from_url = re.compile("([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})(\.[a-zA-Z0-9]*)?$")

log_file_pattern = config.get("cgi", "log_file_pattern")
if "fromDate" in parameters:
    start_str = parameters["fromDate"].value  # "2013-06-15"
else:
    start_str = "2014-09-01"

if "toDate" in parameters:
    end_str = parameters["toDate"].value
else:
    end_str = "2014-12-01"

# http://stackoverflow.com/a/2997846/53897 - 10:00 is to avoid timezone issues in general.
start_date = datetime.datetime.fromtimestamp(time.mktime(time.strptime(start_str + " 10:00", '%Y-%m-%d %H:%M')))
end_date = datetime.datetime.fromtimestamp(time.mktime(time.strptime(end_str + " 10:00", '%Y-%m-%d %H:%M')))

# generate dates. note:  range(0,1) -> [0] hence the +1
dates = [start_date + datetime.timedelta(days=x) for x in range(0, (end_date - start_date).days + 1)]

# prepare urllib2
username = config.get("cgi", "username")
password = config.get("cgi", "password")

# https://docs.python.org/2/howto/urllib2.html#id6
password_mgr = urllib2.HTTPPasswordMgrWithDefaultRealm()
top_level_url = doms_url
password_mgr.add_password(None, top_level_url, username, password)

handler = urllib2.HTTPBasicAuthHandler(password_mgr)
opener = urllib2.build_opener(handler)

# Prepare output CSV:
fieldnames = ["Timestamp", "Type", "Filename", "Userid", "UUID"]
# fieldnames = ["Timestamp", "Type", "Titel (radio/tv)", "Kanal", "Udsendelsestidspunkt",
# "Genre", "Titel (reklamefilm)", "Alternativ titel", "Dato", "Reklamefilmstype",
# "Udgiver", "Klient", "schacHomeOrganization", "eduPersonPrimaryAffiliation",
#              "eduPersonScopedAffiliation", "eduPersonPrincipalName", "eduPersonTargetedID",
#              "SBIPRoleMapper", "MediestreamFullAccess", "UUID", "URL"]

print "Content-type: text/csv"
print "Content-disposition: attachment; filename=stat-" + start_str + "-" + end_str + ".csv"
print

result_file = sys.stdout;  # open("out.csv", "wb")
#result_file = open("out.csv", "wb")

result_dict_writer = csv.DictWriter(result_file, fieldnames, delimiter="\t")
# Inlined result_dict_writer.writeheader() - not present in 2.4.
# Writes out a row where each column name has been put in the corresponding column 
header = dict(zip(result_dict_writer.fieldnames, result_dict_writer.fieldnames))
result_dict_writer.writerow(header)

doms_ids_seen = {}  # DOMS lookup cache, id is key
urls_seen = {}  # PLAY event seen yet for this URL? (value is not important)

conn = psycopg2.connect("dbname=larm-prod user=larm-ro password=2ko6ghphBm host=hyperion")
cur = conn.cursor()
query = "SELECT * FROM events WHERE event_type = 'PLAY' AND timestamp >= '%s' AND timestamp < '%s';" % (
    start_date, end_date)
cur.execute(query)

ids_seen = {}  # PLAY event seen yet for this URL? (value is not important)

for record in cur:
    ts = record[1]
    filename = record[2]
    event = record[3]
    userid = record[4]

    if (event != "PLAY"):
        continue

    id = str(userid) + "#" + filename

    if id in ids_seen:
        continue
    else:
        ids_seen[id] = event  # only key matters.
        out = {"Timestamp": ts, "Filename": filename, "Userid": userid}
        regexp_match = re_doms_id_from_url.search(filename)
        if regexp_match != None:
            doms_id = regexp_match.group(1)
            # big sister probes this, skip (Mogens: if anybody wants to view it, we'll live with it)
            if doms_id == "d68a0380-012a-4cd8-8e5b-37adf6c2d47f":
                continue

            out["UUID"] = doms_id

        #     if doms_id in doms_ids_seen:
        #         (ext_body_text, core_body_text) = doms_ids_seen[doms_id]
        #     else:
        #         url_core = doms_url + "objects/uuid%3A" + doms_id + "/datastreams/PBCORE/content"
        #         url_ext = doms_url + "objects/uuid%3A" + doms_id + "/datastreams/RELS-EXT/content"
        #
        #         ext_body = opener.open(url_ext)
        #         ext_body_text = ext_body.read()
        #     ext_body.close()
        #
        #     core_body = opener.open(url_core)
        #     core_body_text = core_body.read()
        # core_body.close()
        #
        # doms_ids_seen[doms_id] = (ext_body_text, core_body_text)
        #
        # namespaces = {"pb": "http://www.pbcore.org/PBCore/PBCoreNamespace.html",
        #               "rdf": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
        #               "sb": "http://doms.statsbiblioteket.dk/relations/default/0/1/#"}

#        ext = ET.fromstring(ext_body_text)

        # The (get_list() or [""])[0] construct returns the empty string if the first list is empty

#        out["Type"] = (ext.xpath("./rdf:Description/sb:isPartOfCollection/@rdf:resource", namespaces=namespaces) or [""])[0]

#        core = ET.fromstring(core_body_text)
    result_dict_writer.writerow(out)

conn.close()
