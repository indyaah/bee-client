#!/bin/sh -e

rm -rf local/
find . -type d -name target | xargs rm -rf

./mime-types.sh

[ -f gradle.properties ] || ./switch.sh

hg log --verbose | ./hgChangeLog.sh > changelog.md

for v in 2.9.0 2.9.1 2.9.2 2.9.3 2.10.1 2.11.1; do
  [ -x /usr/bin/figlet ] && figlet Scala $v
  ./switch.sh $v
  ./gradlew clean build
done

sh -$- ./maven-metadata.sh
