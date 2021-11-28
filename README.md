# Eclipse GlassFish

## About

[Eclipse GlassFish](https://projects.eclipse.org/projects/ee4j.glassfish) is a Jakarta EE compatible implementation
sponsored by the Eclipse Foundation.

### Compatibility

* Eclipse GlassFish 7.0.0 is Jakarta EE 10 compatible, requires Java 11, supports Java 18
* Eclipse GlassFish 6.2.0 is Jakarta EE 9.1 compatible, requires Java 11, supports Java 17
* Eclipse GlassFish 6.1.0 is Jakarta EE 9.1 compatible, requires Java 11
* Eclipse GlassFish 6.0.0 is Jakarta EE 9 compatible, requires Java 8
* Eclipse GlassFish 5.1.0 is Java EE 8 and Jakarta EE 8 compatible, requires Java 8

### Distribution

The Zip distributions can be found on following paths:
* appserver/distributions/glassfish/target/glassfish.zip (Full Profile)
* appserver/distributions/web/target/web.zip (Web Profile)

## Building

### Prerequisites

* JDK11+
* Maven 3.5.4+

### Execution

* `mvn clean install` - Full build including automatic QA and maven managed tests. Typical time: 5 minutes.
* `mvn clean install -Pfast` - Building all distribution artifacts, running just unit tests, QA and integration tests excluded. Typical time: 3 minutes.
* `mvn clean install -Pfastest` - Building all distribution artifacts, excluded all QA and testing. Typical time: 1.5 minutes.

You can use also some maven optimizations, ie. using `-T4C` to allow parallel build.

### Special Profiles

* `staging` - In some development stages may happen that some dependencies are available just in OSSRH staging repository.
  Then you have to use this profile, which is not enabled by default.
* `jacoco` - enables the [JaCoCo](https://www.eclemma.org/jacoco/) agent in tests, so you can import it's output to you editor, ie. Eclipse, and see the code coverage.
* `jacoco-merge` - merges all JaCoCo output files found in subdirectories and merges them into one. It is useful to see code which wasn't even touched by tests.

### Special Scripts

* `./updateVersion.sh 6.99.99.experimental` - useful for custom distributions, so you can avoid conflicts with version in master branch.
* `./runTests.sh 6.2.0 cdi_all` - useful to run [additional](Additional Testing) tests locally; before that edit the script and update variables so they will reflect your environment.
* `./validateJars.sh` - uses the bnd command to check OSGI dependencies in all target directories

## Additional Testing

After building the GlassFish distribution artifacts you can execute also additional tests managed by bash scripts.
They are quite old and have high technical debt, but at this moment they still provide useful service.

### QuickLook

`mvn -f appserver/tests/quicklook/pom.xml test -Dglassfish.home=$(pwd)/appserver/distributions/glassfish/target/stage/glassfish7/glassfish`

* Usual time: 3 minutes
* see [QuickLook_Test_Instructions](https://github.com/eclipse-ee4j/glassfish/blob/master/appserver/tests/quicklook/QuickLook_Test_Instructions.html)

### Old Additional Tests

* `./runTests.sh 7.0.0 cdi_all` - Usual time: 6 minutes
* `./runTests.sh 7.0.0 ejb_group_1` - Usual time: 8 minutes
* `./runTests.sh 7.0.0 web_jsp` - Usual time: 6 minutes
* `./gfbuild.sh archive_bundles && ./gftest.sh ql_gf_web_profile_all` - Usual time: 2 minutes
* `./gfbuild.sh archive_bundles && ./gftest.sh ql_gf_full_profile_all` - Usual time: 4 minutes

### Half-Dead Additional Tests Waiting for a Rescue

First warning: if the script fails, it doesn't stop the domain and you have to do that manually.
* `./runTests.sh 7.0.0 batch_all` - Crashes, Derby uses different port than expected
* `./runTests.sh 7.0.0 connector_group_1` - Crashes, ports
* `./runTests.sh 7.0.0 connector_group_2` - Crashes, ports
* `./runTests.sh 7.0.0 connector_group_3` - Crashes, ports
* `./runTests.sh 7.0.0 connector_group_4` - Crashes, ports
* `./runTests.sh 7.0.0 deployment_all` - Usual time: 1 minute, failure: still uses javax packages.
* `./runTests.sh 7.0.0 ejb_group_2` - Usual time: 2 minutes, failure: incompatible API at TimerSessionEJB.
* `./runTests.sh 7.0.0 ejb_group_3` - Usual time: 4 minutes, failure: still uses javax packages
* `./runTests.sh 7.0.0 jdbc_all` - Crashes, Derby uses different port than expected
* `./runTests.sh 7.0.0 persistence_all` - Crashes, Derby uses different port than expected
* `./gfbuild.sh archive_bundles && ./gftest.sh ejb_web_all` - Usual time: 4 minutes, failure: could not create the derby database
* `./gfbuild.sh archive_bundles && ./gftest.sh ql_gf_nucleus_all` - Crashes
* `./gfbuild.sh archive_bundles && ./gftest.sh nucleus_admin_all` - Crashes, missing TestNG dependency
* many tests under appserver/tests subdirectories; they are still waiting for someone's attention.

## Basic Usage

* Starting Eclipse GlassFish: `glassfish7/bin/asadmin start-domain`
* Visit [http://localhost:4848](http://localhost:4848)
* Stopping Eclipse GlassFish: `glassfish7/bin/asadmin stop-domain`

## Professional Services and Enterprise Support

This section is dedicated to companies offering products and services around Eclipse GlassFish.

The Eclipse GlassFish project does not endorse or recommend any of the companies on this page. We love all our supporters equally.

Professional Services and Enterprise support are available through following companies:
- [ManageCat](https://www.managecat.com/services-and-support/eclipse-glassfish-enterprise-support).
