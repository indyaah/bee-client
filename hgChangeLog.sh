#!/bin/bash -e
export SCALA_HOME=/home/rick/sw/scala-latest
PATH=$SCALA_HOME/bin:$PATH
exec scala "$0" "$@"
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
