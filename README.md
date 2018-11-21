GlassFish Server
=================

GlassFish is the reference implementation of Java EE.

Building
--------

Prerequisites:

* JDK8+
* Maven 3.0.3+

Currently in the EE4J_8 branch artifacts are being pulled from OSSRH staging.

Run the full build:

`mvn -Pstaging install`

Locate the Zip distributions:
- appserver/distributions/glassfish/target/glassfish.zip
- appserver/distributions/web/target/web.zip

Locate staged distributions:
- appserver/distributions/glassfish/target/stage
- appserver/distributions/web/target/stage

Testing
--------
Running GlassFish QuickLook tests:

`mvn -f appserver/tests/quicklook/pom.xml test -Dglassfish.home=appserver/distributions/glassfish/target/stage/glassfish5/glassfish`

For more details, see [QuickLook_Test_Instructions](https://github.com/eclipse-ee4j/glassfish/blob/master/appserver/tests/quicklook/QuickLook_Test_Instructions.html)

Starting GlassFish
------------------

`glassfish5/bin/asadmin start-domain`

Stopping GlassFish
------------------

`glassfish5/bin/asadmin stop-domain`
