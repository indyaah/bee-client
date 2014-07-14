#!/bin/bash -e
V=0.13.5

cd $(dirname $0)
if [ ! -f xsbt/bin/sbt-launch.jar ]; then
  mkdir t && cd t
  echo Getting sbt $V...
  curl -LSf http://dl.bintray.com/sbt/native-packages/sbt/$V/sbt-$V.tgz | tar -zx
  mv sbt ../xsbt
  cd .. && rmdir t
fi

OPTS="-Dfile.encoding=UTF8 -Xmx1024M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M -XX:ReservedCodeCacheSize=128M"
java $OPTS -jar xsbt/bin/sbt-launch.jar "$@"
