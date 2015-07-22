#!/bin/bash -e
scalabin=$(type -p scala)
if [ ! -f "$scalabin" ]; then
  echo Scala not found.
  exit 1
fi

hg log --verbose | ./hgChangeLog.sh > changelog.md
