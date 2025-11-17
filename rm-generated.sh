#!/bin/bash
rm -rf appserver/tests/appserv-tests/config/derby.properties.replaced appserver/tests/appserv-tests/devtests/ejb/ejb31/embedded/sun-appserv-samples/ appserver/tests/appserv-tests/lib/maven-ant-tasks-2.1.3.jar ejb_web_all-results.tar.gz glassfish7/ results/ tests-run.log nucleus/.gitignore appserver/.gitignore ql_gf_full_profile_all-results.tar.gz ql_gf_web_profile_all-results.tar.gz appserver/tests/appserv-tests/devtests/ejb/stubs/stubser/foohandle appserver/tests/appserv-tests/devtests/ejb/stubs/stubser/homehandle appserver/tests/embedded/web/web-api/bin/
rm -rf appserver/tests/appserv-tests/devtests/cdi/implicit/deployment-option/client/Client.java appserver/tests/appserv-tests/devtests/cdi/implicit/simple-ejb-cdi/client/Client.java appserver/tests/appserv-tests/devtests/cdi/javaee-integration/embedded-resource-adapter-as-bean-archive/ra/generic-ra.rar appserver/tests/appserv-tests/devtests/cdi/portable-extensions/new-bean-registration-extension-in-lib/lib/
rm appserver/tests/appserv-tests/devtests/cdi/javaee-integration/*/lib/*.jar
rm appserver/tests/appserv-tests/devtests/cdi/javaee-integration/*/ra/generic-ra.jar

git checkout HEAD -- appserver/tests/appserv-tests/config.properties appserver/tests/appserv-tests/config/derby.properties
git checkout HEAD -- .gitignore

git checkout HEAD -- appserver/tests/appserv-tests/devtests/ejb/ejb30/hello/session/client/Client.java
git checkout HEAD -- appserver/tests/appserv-tests/devtests/cdi/implicit/deployment-option/client/Client.java
git checkout HEAD -- appserver/tests/appserv-tests/devtests/cdi/implicit/simple-ejb-cdi/client/Client.java

git checkout HEAD -- appserver/tests/appserv-tests/devtests/cdi/javaee-integration/*/lib/*.jar
