#!/bin/bash
if [ -f /etc/mime.types -a /etc/mime.types -nt src/main/resources/mime-types.txt ]; then
  # copy the OS mime.types and reformat and compact slightly
  egrep -v '^#' /etc/mime.types | egrep '[[:space:]]' | tr '\t' ' ' > src/main/resources/mime-types.txt
fi

sbt clean test package package-doc package-src publish-local
