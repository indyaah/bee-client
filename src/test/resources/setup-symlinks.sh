#!/bin/bash
# The integration test requires access to the resources here via your localhost webserver. This script
# will help set up the symlinks, provided you have administrative access to your PC.
#
# It is also necessary to set up PHP support in your webserver. This script does not do this for you.
#
# This is Linux only and has only been tested on Ubuntu.

if [ "$UID" -ne 0 ]; then
    echo Must be root to run $0.
    sudo $0
else
    cd $(dirname $0)
    TGT=$PWD

    WEBSERVER=$(netstat -ltp | fgrep ' *:http ')
    if [ -z "$WEBSERVER" ]; then
        WEBSERVER=$(netstat -ltp | fgrep ' *:www ')
    fi

    # Fallback - error case
    WWWDIR="/NO ROOT DEFINED"
    # Most common case
    [ -d /var/www ] && WWWDIR=/var/www

    case "$WEBSERVER" in
        */apache2* | */cherokee*)
            ;;
        */nginx*)
            [ -d /usr/share/nginx/www ] && WWWDIR=/usr/share/nginx/www
            ;;
        "")
            echo "Your webserver doesn't appear to be running. Please start it and try again."
            exit 1
            ;;
        *)
            echo "Sorry, your webserver is not supported yet."
            echo $WEBSERVER
            echo "Please create a symlink in its root content folder to"
            echo $TGT
            exit 2
            ;;
    esac

    if [ -d $WWWDIR ]; then
        cd $WWWDIR
        ln -fs $TGT lighthttpclient
        ls -l
        echo Set up $WWWDIR ok.
    fi
fi
