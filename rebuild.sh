#!/bin/bash -e
bash -e mime-types.sh
hg log --verbose | ./hgChangeLog.sh > changelog.md
./sbt clean test '+package' '+package-doc' '+package-src' '+publish-local'
./maven-metadata.sh

