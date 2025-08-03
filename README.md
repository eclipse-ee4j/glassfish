# Eclipse GlassFish

## About

[Eclipse GlassFish](https://projects.eclipse.org/projects/ee4j.glassfish) is a Jakarta EE compatible implementation
sponsored by the Eclipse Foundation.

### Release Notes
* You can read about the [release notes here](https://github.com/eclipse-ee4j/glassfish/releases) 

### Compatibility

* Eclipse GlassFish 8.0.0 is Jakarta EE 11 compatible, requires Java 21, experimentally also higher versions.
* Eclipse GlassFish 7.1.0 is Jakarta EE 10 compatible, requires Java 17 or 21, experimentally also higher versions.
* Eclipse GlassFish 7.0.25 is Jakarta EE 10 compatible, requires Java 11, 17 or 21, experimentally also higher versions.
* Eclipse GlassFish 6.2.5 is Jakarta EE 9.1 compatible, requires Java 11 or 17
* Eclipse GlassFish 6.1.0 is Jakarta EE 9.1 compatible, requires Java 11
* Eclipse GlassFish 6.0.0 is Jakarta EE 9 compatible, requires Java 8
* Eclipse GlassFish 5.1.0 is Java EE 8 and Jakarta EE 8 compatible, requires Java 8

### Distribution

The Zip distributions can be found on following paths:
* appserver/distributions/glassfish/target/glassfish.zip (Full Profile)
* appserver/distributions/web/target/web.zip (Web Profile)

## Building

### Prerequisites

* JDK17+.
* Maven 3.9.0+

### Execution

* `mvn clean install` - Full build including documentation, automatic QA, checkstyle and all maven managed tests. Excludes just Ant and TCK tests. Typical time: 15 minutes.
* `mvn clean install -Pqa` - Building all distribution artifacts, running QA, checkstyle and all maven managed tests. Excludes Ant, TCK and documentation. Typical time: 10 minutes.
* `mvn clean install -Pfast` - Building all distribution artifacts, running just unit tests. Excludes QA, checkstyle, integration tests, Ant, TCK and documentation. Typical time: 7 minutes.
* `mvn clean install -Pfastest -T4C` - Building all distribution artifacts as fast as possible. Excludes everything not serving this purpose. Typical time: 1.5 minutes.

After the build, you can run GlassFish in the following ways:
* Run GlassFish server directly: Navigate to `appserver/distributions/glassfish/target/stage/glassfish7` - it's an unpacked version of GlassFish Full. Then you can run it as usual, e.g. with `bin/startserv`
* Run GlassFish server from a ZIP: A zip file is generated either at `appserver/distributions/glassfish/target/glassfish.zip` or in your local maven repository, e.g. at `<HOME>/.m2/repository/org/glassfish/main/distributions/glassfish/<VERSION>/glassfish-<VERSION>.zip`. Unpack it and run as usual
* Run Embedded GlassFish: Find the JAR file at `appserver/extras/embedded/all/target/glassfish-embedded-all.jar` or in your local maven repository, e.g. at `<HOME>/.m2/repository/org/glassfish/main/extras/glassfish-embedded-all/<VERSION>/glassfish-embedded-all-<VERSION>.jar. Then run it with `java -jar glassfish-embedded-all.jar`

You can use also some maven optimizations, see [Maven documentation](https://maven.apache.org/ref/3.9.5/maven-embedder/cli.html).
Especially `-am`, `-amd`, `-f`, `-pl`, `-rf` and their combinations are useful.

If you use Maven 3.9+, we recommend that you copy the `.mvn/maven.config.template` file into `.mvn/maven.config` and uncomment one of the options there or customize the options as you need. Then you can just run `mvn clean install` and the options in `maven.config` will be applied automatically. Or with just `mvn`, the default goal will be run with those options. See [Configuring Maven Documentation](https://maven.apache.org/configure.html)

If you want to see more logs you can use the `-Dtest.logLevel=FINEST` option set to an appropriate log level.
Note that this applies just for tests which are executed by Maven and which use the **GlassFish Java Util Logging Extension (GJULE)**.

### Special Profiles

* `staging` - In some development stages may happen that some dependencies are available just in OSSRH staging repository.
  Then you have to use this profile, which is not enabled by default.
* `jacoco` - enables the [JaCoCo](https://www.eclemma.org/jacoco/) agent in tests, so you can import it's output to you editor, ie. Eclipse, and see the code coverage.
* `jacoco-merge` - merges all JaCoCo output files found in subdirectories and merges them into one. It is useful to see code which wasn't even touched by tests.

### Special Scripts

* `./updateVersion.sh 6.99.99.experimental` - useful for custom distributions, so you can avoid conflicts with version in master branch.
* `./runtests.sh [testBlockName] [?glassfishVersion]` - useful to run [old additional tests](#old-additional-tests) locally
* `./validateJars.sh` - uses the bnd command to check OSGI dependencies in all target directories

## Additional Testing

After building the GlassFish distribution artifacts you can execute also additional tests managed by bash scripts.
They are quite old and have high technical debt, but at this moment they still provide useful service.

### QuickLook

`mvn -f appserver/tests/quicklook/pom.xml test -Dglassfish.home=$(pwd)/appserver/distributions/glassfish/target/stage/glassfish7/glassfish`

* Usual time: 3 minutes
* see [QuickLook_Test_Instructions](https://github.com/eclipse-ee4j/glassfish/blob/master/appserver/tests/quicklook/QuickLook_Test_Instructions.html)

### Old Additional Tests

:warning: If the script fails, sometimes it doesn't stop the domain and you have to do that manually.

:warning: Some of the scripts do inplace filtering or generate other sources which remain and later affect next executions. You have to remove those changes manually.

* `./runtests.sh batch_all` - Usual time: 1 minute
* `./runtests.sh cdi_all` - Usual time: 6 minutes
* `./runtests.sh connector_group_1` - Usual time: 16 minutes
* `./runtests.sh connector_group_2` - Usual time: 3 minutes
* `./runtests.sh connector_group_3` - Usual time: 4 minutes
* `./runtests.sh connector_group_4` - Usual time: 16 minutes
* `./runtests.sh deployment_all` - Usual time: 8 minutes
* `./runtests.sh ejb_group_1` - Usual time: 10 minutes
* `./runtests.sh ejb_group_2` - Usual time: 7 minutes
* `./runtests.sh ejb_group_3` - Usual time: 18 minutes
* `./runtests.sh ejb_group_embedded` - Usual time: 4 minutes
* `./runtests.sh ejb_group_all` - Usual time: 4 minutes
* `./runtests.sh jdbc_all` - Usual time: 20 minutes
* `./runtests.sh naming_all` - Usual time: 2 minutes
* `./runtests.sh persistence_all` - Usual time: 3 minutes
* `./runtests.sh security_all` - Usual time: 8 minutes
* `./runtests.sh web_jsp` - Usual time: 8 minutes
* `./runtests.sh webservice_all` - Usual time: 10 minutes
* `./gfbuild.sh archive_bundles && ./gftest.sh ejb_web_all` - Usual time: 4 minutes
* `./gfbuild.sh archive_bundles && ./gftest.sh ql_gf_web_profile_all` - Usual time: 2 minutes
* `./gfbuild.sh archive_bundles && ./gftest.sh ql_gf_full_profile_all` - Usual time: 4 minutes

* many tests under appserver/tests subdirectories; they are still waiting for someone's attention.

### Pull request workflow

The pull request workflow is described on [Eclipse GlassFish / workflow](https://glassfish.org/pr_workflow).

Build server results of pull requests can be found at [CI Glassfish](https://ci.eclipse.org/glassfish/).

## Basic Usage

* Starting Eclipse GlassFish: `glassfish7/bin/asadmin start-domain`
* Visit [http://localhost:4848](http://localhost:4848)
* Stopping Eclipse GlassFish: `glassfish7/bin/asadmin stop-domain`

## Professional Services and Enterprise Support

This section is dedicated to companies offering products and services around Eclipse GlassFish.

The Eclipse GlassFish project does not endorse or recommend any of the companies on this page. We love all our supporters equally.

Professional Services and Enterprise support are available through following companies:
- [OmniFish](https://omnifish.ee/solutions/#support)
- [ManageCat](https://www.managecat.com/services-and-support/eclipse-glassfish-enterprise-support)
