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
    cd $(dirname $0)/..
    TGT=$PWD

    if [ "$1" != "" ]; then
        WEBSERVER=$1
    else
        WEBSERVER=$(lsof -i tcp:80 -s tcp:LISTEN | awk 'NR!=1 {print $1}' | uniq)
    fi

    # Fallback - error case
    WWWDIR=None
    ETCDIR=None
    CFG=None

    # Most common case
    [ -d /var/www ] && WWWDIR=/var/www
    [ -d /etc/apache2/sites-enabled ] && ETCDIR=/etc/apache2/sites-enabled

    case "$WEBSERVER" in
        apache2)
            SVR=apache2
            [ -d /var/www ] && WWWDIR=/var/www
            [ -d /etc/apache2/sites-enabled ] && ETCDIR=/etc/apache2/sites-enabled
            ;;
        nginx)
            SVR=nginx
            [ -d /usr/share/nginx ] && WWWDIR=/usr/share/nginx
            [ -d /etc/nginx/sites-enabled ] && ETCDIR=/etc/nginx/sites-enabled
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
        rm -f bee-client
        ln -fs $TGT bee-client
        ls -l
        echo Set up $WWWDIR ok.
    fi

    if [ -d $ETCDIR ]; then
        cd $ETCDIR
        rm -f bee-client.cfg bee-client-ssl.cfg
        [ -f $TGT/scripts/$SVR.cfg ] && ln -vfs $TGT/scripts/$SVR.cfg bee-client.cfg
        [ -f $TGT/scripts/$SVR.ssl.cfg ] && ln -vfs $TGT/scripts/$SVR.ssl.cfg bee-client-ssl.cfg
        ls -l
        echo Set up $ETCDIR ok.
        service $SVR reload
    fi
fi
