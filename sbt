#!/bin/bash -e
V=0.12.0

cd $(dirname $0)
if [ ! -f xsbt/bin/sbt-launch.jar ]; then
  mkdir t && cd t
  echo Getting sbt $V...
  curl -Sf http://scalasbt.artifactoryonline.com/scalasbt/sbt-native-packages/org/scala-sbt/sbt/$V/sbt.tgz | tar -zx
  mv sbt ../xsbt
  cd .. && rmdir t
fi

OPTS="-Dfile.encoding=UTF8 -Xmx1024M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"
java $OPTS -jar xsbt/bin/sbt-launch.jar "$@"
