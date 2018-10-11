#!/usr/bin/env python2.7

# Jira issue NO-154.  Enrich mediastream player log with DOMS meta data.

from lxml import etree as ET
import ConfigParser
import csv
import datetime
try:
    import simplejson
except ImportError:
    import json as simplejson
import os
import re
import sys
import time
import cgi
import cgitb
import requests

#
config_file_name = "../../statistics.py.cfg"

# -----

cgitb.enable() # web page feedback in case of problems
parameters = cgi.FieldStorage()

encoding = "utf-8" # What to convert non-ASCII chars to.

config = ConfigParser.SafeConfigParser()
config.read(config_file_name)

doms_url = config.get("cgi", "doms_url") # .../fedora/
pvica_url = config.get("cgi", "pvica_url") # kuana
solr_idx_url = config.get("cgi", "solr_idx_url") # solr

# Example: A colon, a uuid e.g. d68a0380-012a-4cd8-8e5b-37adf6c2d47f trailed by a ".fileending", a /, or EOL)
re_doms_id_from_url = re.compile("(?::)([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})((\.[a-zA-Z0-9]*)*|/|$)")
re_DelivUnitRef_from_solr = re.compile("(?::)([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$)")

log_file_pattern = config.get("cgi", "log_file_pattern")
if "fromDate" in parameters:
        start_str = parameters["fromDate"].value # "2013-06-15"
else:
        start_str = "2018-06-18"

if "toDate" in parameters:
        end_str = parameters["toDate"].value
else:
        end_str = "2018-09-20"

# http://stackoverflow.com/a/2997846/53897 - 10:00 is to avoid timezone issues in general.
start_date = datetime.datetime.fromtimestamp(time.mktime(time.strptime(start_str + " 10:00", '%Y-%m-%d %H:%M')))
end_date = datetime.datetime.fromtimestamp(time.mktime(time.strptime(end_str + " 10:00", '%Y-%m-%d %H:%M')))

# generate dates. note:  range(0,1) -> [0] hence the +1
dates = [start_date + datetime.timedelta(days = x) for x in range(0,(end_date - start_date).days + 1)]

# prepare requests
username = config.get("cgi", "username")
password = config.get("cgi", "password")
kuanausername = config.get("cgi", "kuanausername")
kuanapassword = config.get("cgi", "kuanapassword")

# Prepare output CSV:
fieldnames = ["Timestamp", "Type", "Titel (radio/tv)", "Kanal", "Udsendelsestidspunkt",
              "Genre", "Titel (reklamefilm)", "Alternativ titel", "Dato", "Reklamefilmstype",
              "Udgiver", "Klient", "schacHomeOrganization", "eduPersonPrimaryAffiliation",
              "eduPersonScopedAffiliation", "eduPersonPrincipalName", "eduPersonTargetedID",
              "SBIPRoleMapper", "MediestreamFullAccess", "UUID", "URL"]

print "Content-type: text/csv"
print "Content-disposition: attachment; filename=stat-" + start_str + "-" + end_str + ".csv"
print


result_file = sys.stdout; # open("out.csv", "wb")

result_dict_writer = csv.DictWriter(result_file, fieldnames, delimiter="\t")
# Inlined result_dict_writer.writeheader() - not present in 2.4.
# Writes out a row where each column name has been put in the corresponding column
header = dict(zip(result_dict_writer.fieldnames, result_dict_writer.fieldnames))
result_dict_writer.writerow(header)

doms_ids_seen = {} # DOMS/KUANA lookup cache, id is key
urls_seen = {} # PLAY event seen yet for this URL? (value is not important)

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

        # Ditte + Mogens rule - we only look at the very first event with the type "PLAY" for each URL.

        if (event != "PLAY"):
            continue

        if (attr == 'null'):
            continue

        regexp_match = re_doms_id_from_url.search(url)
        if regexp_match == None:
            print "No UUID in URL: " + url + ", line skipped"
            continue

        doms_id = regexp_match.group(1)
        solr_authid = regexp_match.group(1)

        # big sister probes this, skip (Mogens: if anybody wants to view it, we'll live with it)
        if doms_id == "d68a0380-012a-4cd8-8e5b-37adf6c2d47f":
            continue

        if doms_id+attr in urls_seen:
            continue
        else:
            urls_seen[doms_id+attr] = ts # only key matters.

        # Ok.  Now slowly build row to write in "out"

        out = { "Timestamp": ts, "URL": url} # add more below

        out["UUID"] = doms_id

        if doms_id in doms_ids_seen:
            (ext_body_text, core_body_text) = doms_ids_seen[doms_id]
        else:
            url_core = doms_url + "objects/uuid%3A" + doms_id + "/datastreams/PBCORE/content"
            url_ext = doms_url + "objects/uuid%3A" + doms_id + "/datastreams/RELS-EXT/content"
            notdoms = None

            try:
                ext_body = requests.get(url_ext, auth=(username, password))
                ext_body_text = ext_body.content
                ext_body.raise_for_status() # raise exception if error 4xx/5xx

                core_body = requests.get(url_core, auth=(username, password))
                core_body_text = core_body.content
            # set notdoms to something - trigger for kuana search
            except requests.exceptions.RequestException as notdoms:
                ext_body_text = None

            # If no match in doms get recordID for the corresponding UUID from solr and
            # search for kuana pbcore - use solr:recordID as kuana:DeliverableUnitRef
            if notdoms:
                url_solr = solr_idx_url + "select?indent=on&q=authID:%22" + solr_authid + "%22&wt=xml"
                solr_body_text = requests.get(url_solr).content
                solr = ET.fromstring(solr_body_text)
                recordID = solr.xpath("string(/response/result[@name = 'response'][@numFound = '1']/doc/str[@name = 'recordID'])")
                try:
                    DeliverableUnitRef = re_DelivUnitRef_from_solr.search(recordID).group(1)
                    url_core = pvica_url + "api/entity/deliverableUnits/" + DeliverableUnitRef
                    core_body_text = requests.get(url_core, auth=(kuanausername, kuanapassword)).content
                    ext_body_text = None
                except:
                    core_body_text = None

            doms_ids_seen[doms_id] = (ext_body_text, core_body_text)

        namespaces = {"pb": "http://www.pbcore.org/PBCore/PBCoreNamespace.html",
                      "rdf": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                      "sb": "http://doms.statsbiblioteket.dk/relations/default/0/1/#",
                      "pv": "http://www.tessella.com/XIP/v4"
                      }

        # if doms match is found - run xpath on found doms ext and (pb)core
        if ext_body_text:
            ext = ET.fromstring(ext_body_text)

            # The (get_list() or [""])[0] construct returns the empty string if the first list is empty

            out["Type"] = (ext.xpath("./rdf:Description/sb:isPartOfCollection/@rdf:resource", namespaces=namespaces) or [""])[0]

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

        # if ext_body_text is None, a doms match is not found - run xpath on found kuana (pb)core
        elif core_body_text:
            # Radio/TV collection
            core = ET.fromstring(core_body_text)
            out["Titel (radio/tv)"] = (core.xpath("string("
                                                  "/pv:XIP"
                                                  "/pv:DeliverableUnits"
                                                  "/pv:DeliverableUnit[@status = 'same']"
                                                  "/pv:Metadata[@schemaURI = 'http://www.pbcore.org/PBCore/PBCoreNamespace.html']"
                                                  "/pb:PBCoreDescriptionDocument"
                                                  "/pb:pbcoreTitle[pb:titleType = 'titel']"
                                                  "/pb:title"
                                                  ")", namespaces=namespaces) or "").encode(encoding)
            out["Kanal"] = (core.xpath("string("
                                      "/pv:XIP"
                                      "/pv:DeliverableUnits"
                                      "/pv:DeliverableUnit[@status = 'same']"
                                      "/pv:Metadata[@schemaURI = 'http://www.pbcore.org/PBCore/PBCoreNamespace.html']"
                                      "/pb:PBCoreDescriptionDocument"
                                      "/pb:pbcorePublisher[pb:publisherRole = 'kanalnavn']"
                                      "/pb:publisher"
                                      ")", namespaces=namespaces) or "").encode(encoding)
            out["Udsendelsestidspunkt"] = (core.xpath("string("
                                                     "/pv:XIP"
                                                     "/pv:DeliverableUnits"
                                                     "/pv:DeliverableUnit[@status = 'same']"
                                                     "/pv:Metadata[@schemaURI = 'http://www.pbcore.org/PBCore/PBCoreNamespace.html']"
                                                     "/pb:PBCoreDescriptionDocument"
                                                     "/pb:pbcoreInstantiation"
                                                     "/pb:pbcoreDateAvailable"
                                                     "/pb:dateAvailableStart"
                                                     ")", namespaces=namespaces) or "").encode(encoding)
            out["Type"] = (core.xpath("string("
                                                     "/pv:XIP"
                                                     "/pv:DeliverableUnits"
                                                     "/pv:DeliverableUnit[@status = 'same']"
                                                     "/pv:Metadata[@schemaURI = 'http://www.pbcore.org/PBCore/PBCoreNamespace.html']"
                                                     "/pb:PBCoreDescriptionDocument"
                                                     "/pb:pbcoreInstantiation"
                                                     "/pb:formatLocation"
                                                     ")", namespaces=namespaces) or "").encode(encoding)
            out["Genre"] = (core.xpath("string("
                                       "/pv:XIP"
                                       "/pv:DeliverableUnits"
                                       "/pv:DeliverableUnit[@status = 'same']"
                                       "/pv:Metadata[@schemaURI = 'http://www.pbcore.org/PBCore/PBCoreNamespace.html']"
                                       "/pb:PBCoreDescriptionDocument"
                                       "/pb:pbcoreGenre"
                                       "/pb:genre[starts-with(.,'hovedgenre')]"
                                       ")", namespaces=namespaces) or "").encode(encoding)

            # Reklamefilm (not yet in Kuana at the this time)
            # In Kuana PB core we are missing placeholders for tags:
            # pb:dateIssued, pb:pbcoreAssetType, pbcoreCreator[pb:creatorRole='Producer'], pbcoreCreator[pb:creatorRole='Client']
            out["Titel (reklamefilm)"] = (core.xpath("string("
                                                 "/pv:XIP"
                                                 "/pv:DeliverableUnits"
                                                 "/pv:DeliverableUnit[@status = 'same']"
                                                 "/pv:Metadata[@schemaURI = 'http://www.pbcore.org/PBCore/PBCoreNamespace.html']"
                                                 "/pb:PBCoreDescriptionDocument"
                                                 "/pb:pbcoreTitle[not(pb:titleType)]"
                                                 "/pb:title"
                                                 ")", namespaces=namespaces) or "").encode(encoding)
            out["Alternativ titel"] = (core.xpath("string("
                                                 "/pv:XIP"
                                                 "/pv:DeliverableUnits"
                                                 "/pv:DeliverableUnit[@status = 'same']"
                                                 "/pv:Metadata[@schemaURI = 'http://www.pbcore.org/PBCore/PBCoreNamespace.html']"
                                                 "/pb:PBCoreDescriptionDocument"
                                                 "/pb:pbcoreTitle[pb:titleType='alternative']"
                                                 "/pb:title"
                                                 ")", namespaces=namespaces) or "").encode(encoding)

        # if no uuid match is found in doms or solr/kuana give us a notice
        else:
            out["Type"] = "Not in DOMS or SOLR: "+doms_id
            # continue

        # credentials
        if attr:
            creds = simplejson.loads(attr)
        else:
            creds = []

        for cred in ["schacHomeOrganization", "eduPersonPrimaryAffiliation",
              "eduPersonScopedAffiliation", "eduPersonPrincipalName", "eduPersonTargetedID",
              "SBIPRoleMapper", "MediestreamFullAccess"]:
            if creds and cred in creds:
                # creds[cred] is list, encode each entry, and join them as a single comma-separated string.
                out[cred] = ", ".join(e.encode(encoding) for e in creds[cred])
            else:
                out[cred] = ""

        result_dict_writer.writerow(out)

    log_file.close()
