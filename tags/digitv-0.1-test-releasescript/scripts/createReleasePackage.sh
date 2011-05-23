#!/bin/bash

if [ $# -ne 1 ]
then
    echo "Error in $0 - Invalid Argument Count"
    echo "Syntax: $0 version"
    echo "Example: $0 1.0.4rc2"
    echo "Previous tags:"
    svn list https://merkur.statsbiblioteket.dk/svn/doms-wowza-streaming-server-plugin/tags/ 
    echo 
    exit
fi

VERSION=$1
SVN_TAG_NAME=Mediestream-Wowza-plugin-${VERSION}
PACKAGE_NAME=mediestream-wowza-release-package-${VERSION}
PACKAGE_WITH_DEPLOY_SCRIPT=${PACKAGE_NAME}_bundle

echo Prepare to:
echo " - Create tag in SVN : $SVN_TAG_NAME"
echo " - Create package    : ${PACKAGE_WITH_DEPLOY_SCRIPT}.zip"
echo 
echo -n "Do you want to continue:  (y/N) > "
read answer
echo "You entered: $answer"
if [ "$answer" != "y" ] && [ "$answer" != "Y" ]
    then
	echo "Aborting"
	exit
fi
echo
echo Tagging SVN-repository...
svn copy https://merkur.statsbiblioteket.dk/svn/doms-wowza-streaming-server-plugin/trunk https://merkur.statsbiblioteket.dk/svn/doms-wowza-streaming-server-plugin/tags/$SVN_TAG_NAME -m "Release candidate $SVN_TAG_NAME"

echo Exporting tag from SVN-repository...
svn export https://merkur.statsbiblioteket.dk/svn/doms-wowza-streaming-server-plugin/tags/$SVN_TAG_NAME ~/tmp/wdp_${VERSION}

echo Build package from export...
pushd ~/tmp/wdp_${VERSION}
ant clean package

echo Create deploy script for release package
sed "s/VERSION=\[VERSION_NUMBER\]/VERSION=${VERSION}/g" ~/tmp/wdp_${VERSION}/scripts/deploy-release-package_template.sh | sed "s/PACKAGE_NAME=\[PACKAGE_NAME\]/PACKAGE_NAME=${PACKAGE_NAME}/g" > ~/tmp/${PACKAGE_NAME}_deploy.sh
chmod +x ~/tmp/${PACKAGE_NAME}_deploy.sh

# Keep the following scripts:
# - getTicket.sh
# - stream-monitor.py
# - streaming_statistics.sh

echo Remove irrelevant shell scripts...
rm ~/tmp/wdp_${VERSION}/target/package/bin/createReleasePackage.sh
rm ~/tmp/wdp_${VERSION}/target/package/bin/deploy-*
 
echo Zip package...
cd ~/tmp/wdp_${VERSION}/target/
mv package ${PACKAGE_NAME}
zip -r ~/tmp/${PACKAGE_NAME}.zip ${PACKAGE_NAME}
cd ~/tmp/
zip -r ${PACKAGE_WITH_DEPLOY_SCRIPT}.zip ${PACKAGE_NAME}.zip ${PACKAGE_NAME}_deploy.sh

echo Cleanup...
rm ~/tmp/${PACKAGE_NAME}_deploy.sh
rm ~/tmp/${PACKAGE_NAME}.zip
rm -r ~/tmp/wdp_${VERSION}

echo Move release package to wowza@iapetus:~/releases
scp ~/tmp/${PACKAGE_WITH_DEPLOY_SCRIPT}.zip wowza@iapetus:~/releases

popd

echo Done
echo 
echo Package can be found in: ~/tmp/${PACKAGE_WITH_DEPLOY_SCRIPT}.zip
echo