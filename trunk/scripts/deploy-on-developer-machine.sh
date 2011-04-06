#!/bin/bash

# Expect the deploy package to be in the ~/temp/wowza-plugin folder.
# This script installs developer specific configurations, that vary 
# from the setup in production. See creation of symbolic links.

PACKAGE_NAME=doms-wowza-install-package.zip

echo Deploying Wowza plugin on server

echo - Removing previous deploy-setup folder...
rm -r ~/tmp/wowza-plugin_deploy-setup

echo - Creating new deploy-setup folder...
mkdir ~/tmp/wowza-plugin_deploy-setup

echo - Extracting install package...
unzip -q $PACKAGE_NAME -d ~/tmp/wowza-plugin_deploy-setup

echo - Remove old plugin
rm -r ~/services/wowza_vhost_kultur

echo - Copy deploy-setup virtual host location
cp -r ~/tmp/wowza-plugin_deploy-setup ~/services/wowza_vhost_kultur

echo - Create content folder with content file
ln -s ~/Downloads ~/services/wowza_vhost_kultur/streamingContent
ln -s ~/Downloads ~/services/wowza_vhost_kultur/streamingContentPreview
mkdir ~/services/wowza_vhost_kultur/streamingContentLive
#ln -s ~/services/wowza_vhost_kultur/data ~/services/wowza_vhost_kultur/streamingContent 

echo Finished deploying Wowza plugin on server
echo
echo Restart Wowza Streaming Server manually:
echo
echo ====== Mac OS X: ======
echo "cd /Library/WowzaMediaServer/bin/;./startup.sh"
echo
echo ====== Linux: ======
echo "cd /usr/local/WowzaMediaServer/bin;./startup.sh"
echo
