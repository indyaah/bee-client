#!/bin/bash -e
# This script provides an interim solution for generating maven-metadata.xml files.
# This bypasses the need to install Sonatype Nexus on the repo server (which may have
# quite limited resources, not enough for Nexus).
# Note that this is limited to work with the latest Scala only for now.

. metadata.info

# 1. Update the versions.txt to include the current version (once only)
cp versions.txt versions.$$.txt
LATEST=$(grep '^version\b' build.sbt | sed 's/^version\s*:=\s*//' | tr -d '"')
echo $LATEST >> versions.$$.txt
sort -r versions.$$.txt | uniq > versions.txt
rm -f versions.$$.txt

# 2. Write the metadata file
DATE=$(date '+%Y%m%d%H%M%S')
for target in target/scala-*; do
  scala=$(basename $target)
  v=${scala#scala-}
  F=$target/maven-metadata.xml
  echo "<metadata>" > $F
  echo " <groupId>${GROUPID}</groupId>" >> $F
  echo " <artifactId>${PROJECT}_${v}</artifactId>" >> $F
  echo " <versioning>" >> $F
  echo "  <latest>${VNUM}</latest>" >> $F
  echo "  <release>${VNUM}</release>" >> $F
  echo "  <versions>" >> $F
  for V in $(cat versions.txt); do
    echo "   <version>${V}</version>" >> $F
  done
  echo "  </versions>" >> $F
  echo "  <lastUpdated>${DATE}</lastUpdated>" >> $F
  echo " </versioning>" >> $F
  echo "</metadata>" >> $F
done
