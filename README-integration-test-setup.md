Integration Tests With Local Webserver
======================================

You can run the integration tests against your localhost webserver if you want. This is optional,
but improves the test coverage.

To set this up, you need:

* Apache, Nginx or Cherokee
* PHP interpreter configured with the webserver.

Please start your server then run the script src/test/resources/setup-symlinks.sh

This will find your webserver's root folder and make a symlink in it to this project. The
integration test will then find the source PHP script available to it.
