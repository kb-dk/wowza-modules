#!/bin/bash
#
# This is a template to be used by the script createReleasePackage.sh.
#
# The text (bracket)VERSION_NUMBER(bracket) is replaced by the correct version. 

VERSION=[VERSION_NUMBER]
VHOST_LOCATION=~/services/wowza_vhost_kultur
PACKAGE_NAME=[PACKAGE_NAME]

echo Deploying Wowza plugin - VHost location: $VHOST_LOCATION

echo - Removing previous deploy-setup folder...
rm -r ~/tmp/${PACKAGE_NAME}-deploy-folder

echo - Creating new deploy-setup folder...
mkdir ~/tmp/${PACKAGE_NAME}-deploy-folder

echo - Extracting install package...
unzip -q $PACKAGE_NAME -d ~/tmp/${PACKAGE_NAME}-deploy-folder

echo  - Backup old vhost to ~/tmp...
mv ${VHOST_LOCATION} ~/tmp/wowza_vhost_kultur_before_${PACKAGE_NAME}

echo - Copy deploy-setup virtual host location..
cp -r ~/tmp/${PACKAGE_NAME}-deploy-folder $VHOST_LOCATION

echo Done.