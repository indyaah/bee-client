#!/bin/sh -e

V=${1:-2.11.2}
scalaVersion=$(echo $V |cut -d. -f-2)
scalaMicroVersion=$(echo $V |cut -d. -f3-)

echo "scalaVersion = $scalaVersion" > gradle.properties
echo "scalaMicroVersion = $scalaVersion.$scalaMicroVersion" >> gradle.properties

cat gradle.properties
