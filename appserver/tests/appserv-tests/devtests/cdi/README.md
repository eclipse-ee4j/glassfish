CDI Developer Tests README
==========================

To CheckOut CDI devtests
-------------------------
- Checkout CDI developer tests using the following commands:
svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests
cd appserv-tests #this is the directory set later to APS_HOME
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/config
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/lib
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/util
svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests
cd devtests
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests/cdi

Test Setup
----------
- set S1AS_HOME, APS_HOME as appropriate
export APS_HOME=<appserv-tests> directory
export S1AS_HOME=<GlassFish Installation> directory
- start GlassFish
$S1AS_HOME/bin/asadmin start-domain domain1
- start Derby
$S1AS_HOME/bin/asadmin start-database

To Run All CDI Developer Tests
------------------------------
- cd $APS_HOME/devtests/cdi
- ant all
- results can be found at APS_HOME/test_results.html


Test Setup Teardown
-------------------
- stop GlassFish
$S1AS_HOME/bin/asadmin stop-domain domain1
- asadmin stop-database
$S1AS_HOME/bin/asadmin stop-database

To Run a Single CDI Developer Test
----------------------------------
- after performing tasks under "Test Setup"
- cd $APS_HOME/devtests/cdi/[test-dir]
- ant all
- perform tasks listed under "Test Setup Teardown"

To Run CDI Developer Test Suite with Security Manager On
---------------------------------------------------------
- start domain and enable security manager by 
asadmin create-jvm-options -Djava.security.manager 
- stop domain
- Add the following permission block to $S1AS_HOME/domains/domain1/config/server.policy
grant codeBase "file:${com.sun.aas.instanceRoot}/applications/-" {
    permission java.lang.reflect.ReflectPermission "suppressAccessChecks";
};
- restart domain
- run tests


