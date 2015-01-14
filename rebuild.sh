#!/bin/sh -e

rm -rf local/
find . -type d -name target | xargs rm -rf

./mime-types.sh

[ -f version.gradle ] || ./switch.sh

hg log --verbose | ./hgChangeLog.sh > changelog.md

for v in 2.9.0 2.9.1 2.9.2 2.9.3 2.10.1 2.11.1; do
  ./switch.sh $v
  gradle clean build
done

sh -$- ./maven-metadata.sh
