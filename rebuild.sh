#!/bin/bash -e

find . -maxdepth 4 -type d -name build | xargs rm -rf

./mime-types.sh

function doBuild
{
  [ -x /usr/bin/figlet ] && figlet Scala $1
  ./switch.sh $1
  ./gradlew clean build jacocoTestReport -i
}

case "$1" in
  2.*)  doBuild $1
        ;;
  *)
        #doBuild 2.9.0
        #doBuild 2.9.1
        #doBuild 2.9.2
        #doBuild 2.9.3
        doBuild 2.10.4
        doBuild 2.11.7
        ;;
esac

./updateChangelog.sh
