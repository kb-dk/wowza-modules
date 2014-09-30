#!/usr/bin/env python2

import requests
from lxml import etree as ET
from pprint import pprint
import ConfigParser

config = ConfigParser.SafeConfigParser()
config.read("NO-154.cfg")

doms_url_prefix = config.get("cgi", "doms_url_prefix") # .../uuid%3A

# Utoya
#doms_id = "00f2f269-505d-4e59-892e-b31655c721c2"
# Avis biludlejning
#doms_id = "5c457f00-bb98-4fb3-b3c8-f2df56018130"

for doms_id in ["00f2f269-505d-4e59-892e-b31655c721c2", "5c457f00-bb98-4fb3-b3c8-f2df56018130"]:
    url_core = doms_url_prefix + doms_id + "/datastreams/PBCORE/content"
    url_ext = doms_url_prefix + doms_id + "/datastreams/RELS-EXT/content"

    username = config.get("cgi", "username")
    password = config.get("cgi", "password")

    # http://docs.python-requests.org/en/latest/
    ext_body = requests.get(url_ext, auth=(username,password))

    if ext_body.status_code != 200:
        exit(1)

    core_body = requests.get(url_core, auth=(username,password))
    if core_body.status_code != 200:
        exit(1)


    #print(ext_body.text)

    ext = ET.fromstring(ext_body.text)

    namespaces = { "pb": "http://www.pbcore.org/PBCore/PBCoreNamespace.html",
                   "rdf": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                   "sb": "http://doms.statsbiblioteket.dk/relations/default/0/1/#"}

    print(ext.xpath("./rdf:Description/sb:isPartOfCollection/@rdf:resource", namespaces=namespaces))

    #print(core_body.text)

    core = ET.fromstring(core_body.text)

    # Radio/TV collection
    print(core.xpath("./pb:pbcoreTitle[pb:titleType/text() = 'titel']/pb:title/text()", namespaces=namespaces))
    print(core.xpath("./pb:pbcorePublisher[pb:publisherRole/text() = 'kanalnavn']/pb:publisher/text()", namespaces=namespaces))
    print(core.xpath("./pb:pbcoreInstantiation/pb:pbcoreDateAvailable/pb:dateAvailableStart/text()", namespaces=namespaces))
    print(core.xpath("./pb:pbcoreGenre/pb:genre[starts-with(.,'hovedgenre')]/text()", namespaces=namespaces))
    
    # Reklamefilm
    print(core.xpath("./pb:pbcoreTitle[not(pb:titleType)]/pb:title/text()", namespaces=namespaces))
    print(core.xpath("./pb:pbcoreTitle[pb:titleType='alternative']/pb:title/text()", namespaces=namespaces))
    print(core.xpath("./pb:pbcoreInstantiation/pb:dateIssued/text()", namespaces=namespaces))
    print(core.xpath("./pb:pbcoreAssetType/text()", namespaces=namespaces))
    print(core.xpath("./pb:pbcoreCreator[pb:creatorRole='Producer']/pb:creator/text()", namespaces=namespaces))
    print(core.xpath("./pb:pbcoreCreator[pb:creatorRole='Client']/pb:creator/text()", namespaces=namespaces))
    
