#!/usr/bin/env python2.4

# NO-272 streamingstatistik for larm.fm.

from lxml import etree as ET
import ConfigParser
import psycopg2
import csv
import datetime
import re
import sys
import time
import cgi
import urllib2
import string

config_file_name = "../../larm-statistics.py.cfg"

#cgitb.enable()  # web page feedback in case of problems
parameters = cgi.FieldStorage()

encoding = "utf-8"  # What to convert non-ASCII chars to.

config = ConfigParser.SafeConfigParser()
config.read(config_file_name)

doms_url = config.get("cgi", "doms_url")  # .../fedora/

# Example: d68a0380-012a-4cd8-8e5b-37adf6c2d47f (optionally trailed by a ".fileending")
re_doms_id_from_url = re.compile("([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})(\.[a-zA-Z0-9]*)?$")

if "fromDate" in parameters:
    start_str = parameters["fromDate"].value  # "2013-06-15"
else:
    start_str = "2014-01-03"

if "toDate" in parameters:
    end_str = parameters["toDate"].value
else:
    end_str = "2014-01-06"

# http://stackoverflow.com/a/2997846/53897 - 10:00 is to avoid timezone issues in general.
start_date = datetime.datetime.fromtimestamp(time.mktime(time.strptime(start_str + " 10:00", '%Y-%m-%d %H:%M')))
end_date = datetime.datetime.fromtimestamp(time.mktime(time.strptime(end_str + " 10:00", '%Y-%m-%d %H:%M')))

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
fieldnames = ["Timestamp", "Type", "Filename", "Titel (radio/tv)", "Kanal", "Udsendelsestidspunkt", "Genre", "Userid", "Shard UUID", "PBCore UUID"]

print "Content-type: text/csv"
print "Content-disposition: attachment; filename=larm_fm_stat-" + start_str + "-" + end_str + ".csv"
print

result_file = sys.stdout;  # open("out.csv", "wb")

result_dict_writer = csv.DictWriter(result_file, fieldnames, delimiter="\t")
# Inlined result_dict_writer.writeheader() - not present in 2.4.
# Writes out a row where each column name has been put in the corresponding column 
header = dict(zip(result_dict_writer.fieldnames, result_dict_writer.fieldnames))
result_dict_writer.writerow(header)

doms_ids_seen = {}  # DOMS lookup cache, id is key

larm_db_host = config.get("cgi", "larm_db_host")
larm_db_name = config.get("cgi", "larm_db_name")
larm_db_username = config.get("cgi", "larm_db_username")
larm_db_password = config.get("cgi", "larm_db_password")

conn = psycopg2.connect(host=larm_db_host, database=larm_db_name, user=larm_db_username, password=larm_db_password)
cur = conn.cursor()
query = "SELECT * FROM events WHERE event_type = 'PLAY' AND timestamp >= '%s' AND timestamp < '%s';" % (start_date, end_date)
cur.execute(query)

for record in cur:
    timestamp = record[1]
    filename = record[2]
    event = record[3]
    userid = record[4]

    out = {"Timestamp": timestamp, "Filename": filename, "Userid": userid}
    regexp_match = re_doms_id_from_url.search(filename)
    if regexp_match != None:
        doms_id = regexp_match.group(1)
        # big sister probes this, skip (Mogens: if anybody wants to view it, we'll live with it)
        if doms_id == "d68a0380-012a-4cd8-8e5b-37adf6c2d47f":
            continue

        out["Shard UUID"] = doms_id

        if doms_id in doms_ids_seen:
            (ext_body_text, shard_metadata_text, pbcore_metadata_xml, pbcore_uuid) = doms_ids_seen[doms_id]
        else:
            url_shard_metadata = doms_url + "objects/uuid%3A" + doms_id + "/datastreams/SHARD_METADATA/content"
            url_ext = doms_url + "objects/uuid%3A" + doms_id + "/datastreams/RELS-EXT/content"

            ext_body = opener.open(url_ext)
            ext_body_text = ext_body.read()
            ext_body.close()

            shard_metadata = opener.open(url_shard_metadata)
            shard_metadata_text = shard_metadata.read()
            shard_metadata.close()

            riquery = "*+*+<info:fedora/uuid:" + doms_id + ">"
            url_risearch = doms_url + "risearch?type=triples&lang=spo&format=N-Triples&query=" + riquery
            risearch_body = opener.open(url_risearch)
            risearch_text = risearch_body.read()
            risearch_body.close()
            risearch_text_firstelement = string.split(risearch_text, ">")[0]
            pbcore_uuid = string.split(risearch_text_firstelement, ":")[2]
            url_pbcore_metadata = doms_url + "objects/uuid%3A" + pbcore_uuid + "/datastreams/PBCORE/content"
            pbcore_metadata = opener.open(url_pbcore_metadata)
            pbcore_metadata_xml = pbcore_metadata.read()
            pbcore_metadata.close

            doms_ids_seen[doms_id] = (ext_body_text, shard_metadata_text, pbcore_metadata_xml, pbcore_uuid)

        out["PBCore UUID"] = pbcore_uuid

        namespaces = {
                  "pb": "http://www.pbcore.org/PBCore/PBCoreNamespace.html",
                  "rdf": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                  "sb": "http://doms.statsbiblioteket.dk/relations/default/0/1/#"}


        # The (get_list() or [""])[0] construct returns the empty string if the first list is empty
        ext = ET.fromstring(ext_body_text)
        out["Type"] = (ext.xpath("./rdf:Description/sb:isPartOfCollection/@rdf:resource", namespaces=namespaces) or [""])[0]

        shard = ET.fromstring(shard_metadata_text)
        filename_text = shard.xpath("/shard_metadata/file/file_name/text()")[0]
        out["Filename"] = filename_text

        filename_text_entries = string.split(filename_text, "_")
        out["Kanal"] = filename_text_entries[2]
            
        timestamp_from_filename = filename_text_entries[4]  # date format 20060109040501
        timestamp_offset = int(shard.xpath("/shard_metadata/file/program_start_offset/text()")[0])
        out["Udsendelsestidspunkt"] = datetime.datetime.fromtimestamp(time.mktime(time.strptime(timestamp_from_filename, "%Y%m%d%H%M%S"))) + datetime.timedelta(0, timestamp_offset)

        pbcore = ET.fromstring(pbcore_metadata_xml)
        out["Titel (radio/tv)"] = (pbcore.xpath("./pb:pbcoreTitle[pb:titleType/text() = 'titel']/pb:title/text()", namespaces=namespaces) or [""])[0].encode(encoding)
        out["Genre"] = (pbcore.xpath("./pb:pbcoreGenre/pb:genre[starts-with(.,'hovedgenre')]/text()", namespaces=namespaces) or [""])[0].encode(encoding)

    else:
        out["Kanal"] = string.split(filename, "_")[2]
            
    result_dict_writer.writerow(out)

conn.close()
