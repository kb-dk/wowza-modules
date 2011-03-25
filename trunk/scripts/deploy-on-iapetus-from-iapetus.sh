#!/bin/bash

# Expect the deploy package to be in the ~/temp/wowza-plugin folder.
# The script is supposed to run on the server where Wowza resides as the wowza-user.

PACKAGE_NAME=doms-wowza-install-package.zip

echo Deploying Wowza plugin on server

cd ~/temp/wowza-plugin

echo - Removing previous deploy-setup folder...
rm -r deploy-setup

echo - Creating new deploy-setup folder...
mkdir deploy-setup

echo - Extracting install package...
unzip -q $PACKAGE_NAME -d deploy-setup

echo - Copy deploy-setup to Wowza install dir...
cp -r deploy-setup/* /usr/local/WowzaMediaServer/

# Wowza restart script cannot be called remotely.
# echo - Restarting server...
# echo ====== Server log : Start ======
# sudo /usr/local/sbin/wowzainit.sh restart
# echo ====== Server log : End ======
echo - Restart server locally on the machine using the command: "sudo /usr/local/sbin/wowzainit.sh restart"

echo Finished deploying Wowza plugin on server
