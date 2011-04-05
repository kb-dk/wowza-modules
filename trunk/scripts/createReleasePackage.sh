#!/bin/bash

if [ $# -ne 1 ]
then
    echo "Error in $0 - Invalid Argument Count"
    echo "Syntax: $0 version"
    echo "Example: $0 1.0.4RC2"
    echo 
    exit
fi

VERSION=$1

SVN_PACKAGE_NAME=DOMS-Wowza-plugin-${VERSION}
PACKAGE_NAME=doms-wowza-install-package-${VERSION}.zip

echo Prepare to:
echo " - Create tag in SVN : $SVN_PACKAGE_NAME"
echo " - Create package    : $PACKAGE_NAME"
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
#svn copy https://merkur.statsbiblioteket.dk/svn/doms-wowza-streaming-server-plugin/trunk https://merkur.statsbiblioteket.dk/svn/doms-wowza-streaming-server-plugin/tags/DOMS-Wowza-plugin-${VERSION} -m "Release candidate ${VERSION}"

echo Exporting tag from SVN-repository...
svn export https://merkur.statsbiblioteket.dk/svn/doms-wowza-streaming-server-plugin/tags/DOMS-Wowza-plugin-${VERSION} ~/tmp/wdp_${VERSION}

echo Build package from export...
pushd ~/tmp/wdp_${VERSION}
ant clean package

