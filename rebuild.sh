#!/bin/sh -e

find . -type d -name build | xargs rm -rf

./mime-types.sh

[ -f gradle.properties ] || ./switch.sh

for v in 2.10.4 2.11.7; do
  [ -x /usr/bin/figlet ] && figlet Scala $v
  ./switch.sh $v
  ./gradlew clean build
done

./updateChangelog.sh
#sh -$- ./maven-metadata.sh
