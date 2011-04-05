#!/bin/bash

# Expect the deploy package to be in the ~/tmp/wowza-plugin folder.
# The script is supposed to run on the server where Wowza resides as the wowza-user.

VHOST_LOCATION=~/services/wowza_vhost_kultur_test
PACKAGE_NAME=doms-wowza-install-package.zip

echo Deploying Wowza plugin - VHost location: $VHOST_LOCATION

echo - Removing previous deploy-setup folder...
rm -r ~/tmp/wowza-plugin_deploy-setup

echo - Creating new deploy-setup folder...
mkdir ~/tmp/wowza-plugin_deploy-setup 

echo - Extracting install package...
unzip -q $PACKAGE_NAME -d ~/tmp/wowza-plugin_deploy-setup

echo - Remove old plugin
rm -r $VHOST_LOCATION

echo - Copy deploy-setup virtual host location
cp -r ~/tmp/wowza-plugin_deploy-setup $VHOST_LOCATION

echo - Create content folder with content file
ln -s ~larm/streamingContent $VHOST_LOCATION/streamingContent
ln -s ~larm/previewDirectory $VHOST_LOCATION/streamingContentPreview
mkdir $VHOST_LOCATION/streamingContentLive
