#!/bin/sh -e

V=${1:-2.11.1}
scalaVersion=$(echo $V |cut -d. -f-2)
scalaMicroVersion=$(echo $V |cut -d. -f3-)

echo "ext {" > version.gradle
echo " scalaVersion = \"$scalaVersion\"" >> version.gradle
echo " scalaMicroVersion = \"$scalaVersion.$scalaMicroVersion\"" >> version.gradle
echo "}" >> version.gradle

cat version.gradle
