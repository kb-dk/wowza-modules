#!/usr/bin/env python2

import requests
from lxml import etree as ET
from pprint import pprint
import ConfigParser
import datetime
import os
import csv
import re
import json
import io

encoding = "latin-1" # "utf-8"

config = ConfigParser.SafeConfigParser()
config.read("NO-154.cfg")

doms_url_prefix = config.get("cgi", "doms_url_prefix") # .../uuid%3A

re_doms_id_from_url = re.compile("([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})$")

# Utoya
#doms_id = "00f2f269-505d-4e59-892e-b31655c721c2"
# Avis biludlejning
#doms_id = "5c457f00-bb98-4fb3-b3c8-f2df56018130"

log_file_pattern = config.get("cgi", "log_file_pattern")
start_str = "2013-06-17"
end_str = start_str # "2013-06-01"

# http://stackoverflow.com/a/24637447/53897 - 10:00 is to be far away from midnight
start_date = datetime.datetime.strptime(start_str + ' 10:00', '%Y-%m-%d %H:%M')
end_date = datetime.datetime.strptime(end_str + ' 10:00', '%Y-%m-%d %H:%M')

# generate dates. note:  range(0,1) -> [0] hence the +1
dates = [start_date + datetime.timedelta(days = x) for x in range(0,(end_date - start_date).days + 1)]

fieldnames = ["Timestamp", "Type", "Titel (radio/tv)", "Kanal", "Udsendelsestidspunkt",
              "Genre", "Titel (reklamefilm)", "Alternativ titel", "Dato", "Reklamefilmstype",
              "Udgiver", "Klient", "schacHomeOrganization", "eduPersonPrimaryAffiliation",
              "eduPersonScopedAffiliation", "eduPersonPrincipalName", "eduPersonTargetedID",
              "SBIPRoleMapper", "MediestreamFullAccess", "Event", "UUID"]

result_file = open("out.csv", "wb")
result_dict_writer = csv.DictWriter(result_file, fieldnames, delimiter="\t")
result_dict_writer.writeheader()

seen_before = {} # cache the two lookups in the DOMS for a given id.

for date in dates:
    log_file_name = log_file_pattern % date.strftime("%Y-%m-%d")
    print log_file_name
    
    if os.path.isfile(log_file_name) == False:
        print "Not found: ", log_file_name

    log_file = open(log_file_name, "rb")

    log_file_dict_reader = csv.DictReader(log_file, delimiter=";")

    for line in log_file_dict_reader:
        url = line["Streaming URL"]
        event = line["Event"]
        attr = line["User attributes"]
        ts = line["Timestamp"]

        out = { "Timestamp": ts, "Event": event}
        
        m = re_doms_id_from_url.search(url)
        if m == None:
            print "No UUID in URL"
            continue

        doms_id = m.group(0)

        out["UUID"] = doms_id
        
        # print doms_id

        if doms_id in seen_before:
            (ext_body, core_body) = seen_before[doms_id]
        else:
            url_core = doms_url_prefix + doms_id + "/datastreams/PBCORE/content"
            url_ext = doms_url_prefix + doms_id + "/datastreams/RELS-EXT/content"
            
            username = config.get("cgi", "username")
            password = config.get("cgi", "password")
            
            ext_body = requests.get(url_ext, auth=(username,password))
            
            if ext_body.status_code != 200:
                print "status_code = " + ext_body.status_code
                continue

            core_body = requests.get(url_core, auth=(username,password))

            if core_body.status_code != 200:
                print "status_code = " + ext_core.status_code
                continue
            
            seen_before[doms_id] = (ext_body, core_body)


        #print(ext_body.text)

        namespaces = { "pb": "http://www.pbcore.org/PBCore/PBCoreNamespace.html",
                       "rdf": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                       "sb": "http://doms.statsbiblioteket.dk/relations/default/0/1/#"}

        ext = ET.fromstring(ext_body.text)

        # The (get_list() or [""])[0] returns the empty string if the first list is empty

        out["Type"] = (ext.xpath("./rdf:Description/sb:isPartOfCollection/@rdf:resource", namespaces=namespaces) or [""])[0]

        #print(core_body.text)

        core = ET.fromstring(core_body.text)

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

        creds = json.loads(attr)

        for cred in ["schacHomeOrganization", "eduPersonPrimaryAffiliation",
              "eduPersonScopedAffiliation", "eduPersonPrincipalName", "eduPersonTargetedID",
              "SBIPRoleMapper", "MediestreamFullAccess"]:
            if cred in creds:
                
                out[cred] = ", ".join(e.encode(encoding) for e in creds[cred])
            else:
                out[cred] = ""

        # print out
        result_dict_writer.writerow(out)
        
    log_file.close()
    result_file.close()

