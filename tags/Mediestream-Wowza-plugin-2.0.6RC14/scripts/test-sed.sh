#!/bin/bash
#
# This is a template to be used by the script createReleasePackage.sh.
#
# The text [VERSION_NUMBER] is replaced by the correct version. 

VERSION=2.0.4RC5

sed "s/VERSION=\[VERSION_NUMBER\]/VERSION=${VERSION}/g" deploy-release-package_template.sh