#!/usr/bin/env python2

from lxml import etree as ET
import ConfigParser
import csv
import datetime
import io
import simplejson
import os
import re
import sys
import cgi
#import cgitb
import urllib2

#cgitb.enable() # web page feedback in case of problems

encoding = "latin-1" # What to convert non-ASCII chars to.

config = ConfigParser.SafeConfigParser()
config.read("NO-154.cfg")

doms_url = config.get("cgi", "doms_url") # .../fedora/

re_doms_id_from_url = re.compile("([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})$")

log_file_pattern = config.get("cgi", "log_file_pattern")
start_str = "2013-06-15"
end_str = "2013-07-01"

# http://stackoverflow.com/a/24637447/53897 - 10:00 is to be far away from midnight
start_date = datetime.datetime.strptime(start_str + ' 10:00', '%Y-%m-%d %H:%M')
end_date = datetime.datetime.strptime(end_str + ' 10:00', '%Y-%m-%d %H:%M')

# generate dates. note:  range(0,1) -> [0] hence the +1
dates = [start_date + datetime.timedelta(days = x) for x in range(0,(end_date - start_date).days + 1)]

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
fieldnames = ["Timestamp", "Type", "Titel (radio/tv)", "Kanal", "Udsendelsestidspunkt",
              "Genre", "Titel (reklamefilm)", "Alternativ titel", "Dato", "Reklamefilmstype",
              "Udgiver", "Klient", "schacHomeOrganization", "eduPersonPrimaryAffiliation",
              "eduPersonScopedAffiliation", "eduPersonPrincipalName", "eduPersonTargetedID",
              "SBIPRoleMapper", "MediestreamFullAccess", "UUID", "URL"]

result_file = sys.stdout; # open("out.csv", "wb")
result_dict_writer = csv.DictWriter(result_file, fieldnames, delimiter="\t")
result_dict_writer.writeheader()

doms_ids_seen = {} # DOMS lookup cache, id is key
urls_seen = {} # PLAY event seen for this URL?

for date in dates:
    log_file_name = log_file_pattern % date.strftime("%Y-%m-%d")
    
    # Silently skip non-existing logfiles.
    if os.path.isfile(log_file_name) == False:
        continue

    log_file = open(log_file_name, "rb")

    log_file_dict_reader = csv.DictReader(log_file, delimiter=";")

    for line in log_file_dict_reader:
        url = line["Streaming URL"]
        event = line["Event"]
        attr = line["User attributes"]
        ts = line["Timestamp"]

        # Ditte + Mogens - only give first PLAY event for each URL.
        
        if (event != "PLAY"):
            continue

        if url in urls_seen:
            continue

        urls_seen[url] = ts # only key matters.
            
        out = { "Timestamp": ts, "URL": url}
        
        m = re_doms_id_from_url.search(url)
        if m == None:
            print "No UUID in URL"
            continue

        doms_id = m.group(0)

        out["UUID"] = doms_id
        
        if doms_id in doms_ids_seen:
            (ext_body, core_body) = doms_ids_seen[doms_id]
        else:
            url_core = doms_url + "objects/uuid%3A" + doms_id + "/datastreams/PBCORE/content"
            url_ext = doms_url + "objects/uuid%3A" + doms_id + "/datastreams/RELS-EXT/content"
            
            ext_body = opener.open(url_ext)
            ext_body_text = ext_body.read()

            core_body = opener.open(url_core)
            core_body_text = core_body.read()
            
            doms_ids_seen[doms_id] = (ext_body, core_body)

        #print(ext_body.text)

        namespaces = { "pb": "http://www.pbcore.org/PBCore/PBCoreNamespace.html",
                       "rdf": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                       "sb": "http://doms.statsbiblioteket.dk/relations/default/0/1/#"}

        ext = ET.fromstring(ext_body_text)

        # The (get_list() or [""])[0] returns the empty string if the first list is empty

        out["Type"] = (ext.xpath("./rdf:Description/sb:isPartOfCollection/@rdf:resource", namespaces=namespaces) or [""])[0]

        # print(core_body_text)

        core = ET.fromstring(core_body_text)

        # Radio/TV collection
        out["Titel (radio/tv)"] = (core.xpath("./pb:pbcoreTitle[pb:titleType/text() = 'titel']/pb:title/text()", namespaces=namespaces) or [""])[0].encode(encoding)
        out["Kanal"] = (core.xpath("./pb:pbcorePublisher[pb:publisherRole/text() = 'kanalnavn']/pb:publisher/text()", namespaces=namespaces) or [""])[0].encode(encoding)
        out["Udsendelsestidspunkt"] = (core.xpath("./pb:pbcoreInstantiation/pb:pbcoreDateAvailable/pb:dateAvailableStart/text()", namespaces=namespaces) or [""])[0].encode(encoding)
        out["Genre"] = (core.xpath("./pb:pbcoreGenre/pb:genre[starts-with(.,'hovedgenre')]/text()", namespaces=namespaces) or [""])[0].encode(encoding)
    
        # Reklamefilm
        out["Titel (reklamefilm)"] = (core.xpath("./pb:pbcoreTitle[not(pb:titleType)]/pb:title/text()", namespaces=namespaces) or [""])[0].encode(encoding)
        out["Alternativ titel"] = (core.xpath("./pb:pbcoreTitle[pb:titleType='alternative']/pb:title/text()", namespaces=namespaces) or [""])[0].encode(encoding)
        out["Dato"] = (core.xpath("./pb:pbcoreInstantiation/pb:dateIssued/text()", namespaces=namespaces) or [""])[0].encode(encoding)
        out["Reklamefilmstype"] = (core.xpath("./pb:pbcoreAssetType/text()", namespaces=namespaces) or [""])[0].encode(encoding)
        out["Udgiver"] = (core.xpath("./pb:pbcoreCreator[pb:creatorRole='Producer']/pb:creator/text()", namespaces=namespaces) or [""])[0].encode(encoding)
        out["Klient"] =  (core.xpath("./pb:pbcoreCreator[pb:creatorRole='Client']/pb:creator/text()", namespaces=namespaces) or [""])[0].encode(encoding)

        # credentials
        creds = simplejson.loads(attr)

        for cred in ["schacHomeOrganization", "eduPersonPrimaryAffiliation",
              "eduPersonScopedAffiliation", "eduPersonPrincipalName", "eduPersonTargetedID",
              "SBIPRoleMapper", "MediestreamFullAccess"]:
            if cred in creds:
                
                out[cred] = ", ".join(e.encode(encoding) for e in creds[cred])
            else:
                out[cred] = ""

        result_dict_writer.writerow(out)
        
    log_file.close()
#    result_file.close()

