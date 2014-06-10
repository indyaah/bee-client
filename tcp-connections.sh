#!/bin/bash -e
# This utility is a tool for analysing open socket connections, particularly to determine
# whether they are being closed correctly and not leaking resources.
HOST=${1:-beeclient}
netstat -t tcp | grep $HOST:http | grep -v '_WAIT'
