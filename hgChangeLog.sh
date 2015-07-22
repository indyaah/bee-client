#!/bin/bash -e
scalabin=$(type -p scala)
if [ -z "$scalabin" ]; then
  echo Scala not found.
  exit 1
fi

export SCALA_HOME=$(dirname $(dirname $scalabin))
if [ ! -d "$SCALA_HOME" ]; then
  echo Scala home dir missing.
  exit 2
fi

#PATH=$SCALA_HOME/bin:$PATH
exec scala -cp $SCALA_HOME/lib/scala-library.jar "$0" "$@"
!#
val filteredLines = io.Source.stdin.getLines.
  filterNot(_ == "description:").
  filterNot(_.startsWith("files:")).
  filterNot(_.startsWith("user:")).
  filterNot(_.startsWith("changeset:")).
  filterNot(_.startsWith("tag:"))

val lines = filteredLines.map {
  line =>
    if (line.startsWith("date:"))
      line.replaceAll("date:\\s*([A-Za-z]{3})\\s+([A-Za-z]{3})\\s+([0-9]{2})\\s+([0-9:]+)\\s+([0-9]+).*",
        "#### $1, $3 $2 $5")
    else if (line.startsWith("* ")) line
    else if (line.trim.length > 0) "* " + line
    else ""
}

lines foreach { println }
