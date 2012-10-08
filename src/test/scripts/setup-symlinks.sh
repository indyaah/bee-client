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

    WEBSERVER=$(netstat -ltp | fgrep ' *:http ')
    if [ -z "$WEBSERVER" ]; then
        WEBSERVER=$(netstat -ltp | fgrep ' *:www ')
    fi

    # Fallback - error case
    WWWDIR=None
    ETCDIR=None
    CFG=None

    # Most common case
    [ -d /var/www ] && WWWDIR=/var/www
    [ -d /etc/apache2/sites-enabled ] && ETCDIR=/etc/apache2/sites-enabled

    case "$WEBSERVER" in
        # TODO
        #*/apache2* | */cherokee*)
        #    SVR=apache2
        #    ;;
        #*/cherokee*)
        #    SVR=cherokee
        #    ;;
        */nginx*)
            SVR=nginx
            [ -d /usr/share/nginx/www ] && WWWDIR=/usr/share/nginx/www
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
        rm -f bee-client.cfg
        ln -fs $TGT/scripts/$SVR.cfg bee-client.cfg
        ln -fs $TGT/scripts/$SVR.ssl.cfg bee-client-ssl.cfg
        ls -l
        echo Set up $ETCDIR ok.
        service $SVR reload
    fi
fi
