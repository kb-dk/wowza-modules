#!/bin/bash

# Expect the deploy package to be in the <Wowza-plugin-dev-dir>/target folder
# The script is supposed to run from the local developer machine in the target folder

PACKAGE_NAME=doms-wowza-install-package.zip
SERVER_DEPLOY_SCRIPT=deploy-on-server-from-server.sh

echo Deploying Wowza plugin from developer machine

echo - Copying wowza plugin package to server...
scp $PACKAGE_NAME larm@iapetus:~/temp/wowza-plugin

echo - Extracting server deploy script from package...
unzip -q $PACKAGE_NAME bin/$SERVER_DEPLOY_SCRIPT -d $SERVER_DEPLOY_SCRIPT

echo - Copying deploy script to server...
scp package/bin/$SERVER_DEPLOY_SCRIPT larm@iapetus:~/temp/wowza-plugin

echo === Executing deploy script on server ===
ssh -t larm@iapetus source /home/larm/temp/wowza-plugin/$SERVER_DEPLOY_SCRIPT
echo === Finished deploy script on server ===

echo Finished deploying Wowza plugin from developer machine

