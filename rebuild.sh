#!/bin/bash
set -e
sbt clean test package package-doc package-src
upsync vm04 --delete
