# Eclipse GlassFish

[Eclipse GlassFish](https://projects.eclipse.org/projects/ee4j.glassfish) is a Jakarta EE compatible implementation
owned by the Eclipse Foundation and developed and maintained by the community.

Information about contributions to the project and partially to the whole ecosystem can be found here:
* [GlassFish Project Web Site](https://projects.eclipse.org/projects/ee4j.glassfish/who)
* [EE4J Project Group Web Site](https://projects.eclipse.org/projects/ee4j/who)
* [GitHub Insights](https://github.com/eclipse-ee4j/glassfish/graphs/contributors)

There's much more work done on project's open source dependencies too.
Please consider [contributing](CONTRIBUTING.md) or [sponsoring](https://www.eclipse.org/sponsor) of related projects,
[contributing individuals and companies](https://docs.github.com/en/sponsors/sponsoring-open-source-contributors)
or [buying their services.](#professional-services-and-enterprise-support)

## 📋 Quick Links

- [Download Links](#download-links)
- [Release Notes](https://github.com/eclipse-ee4j/glassfish/releases)
- [Building Instructions](#building)
- [Basic Usage](#basic-usage)
- [Security Considerations](#security-considerations)
- [Professional Support](#professional-services-and-enterprise-support)

## 📦 Download Links

* Maven Central:
    * [GlassFish Server Full Profile](https://central.sonatype.com/artifact/org.glassfish.main.distributions/glassfish/versions)
    * [GlassFish Server Web Profile](https://central.sonatype.com/artifact/org.glassfish.main.distributions/web/versions)
    * [Embedded GlassFish All](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-all/versions)
    * [Embedded GlassFish Web](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-web/versions)
* Docker Images on GitHub:
    * [GlassFish Server Full Profile](https://github.com/eclipse-ee4j/glassfish.docker/pkgs/container/glassfish)
    * [Embedded GlassFish All](https://github.com/eclipse-ee4j/glassfish.docker/pkgs/container/embedded-glassfish)

## 🔄 Version Compatibility

| GlassFish Version | Jakarta EE Version | Java Requirements |
|:------------------|:-------------------|:------------------|
| 8.0.0             | 11                 | 21, 25            |
| 7.1.0             | 10                 | 17, 21, 25        |
| 7.0.25            | 10                 | 11, 17, 21        |
| 6.2.5             | 9.1                | 11, 17            |
| 6.1.0             | 9.1                | 11                |
| 6.0.0             | 9                  | 8                 |
| 5.1.0             | 8 and Java EE 8    | 8                 |

## Building

### Prerequisites

* JDK21+
* Maven 3.9.0+

### Execution

* `mvn clean install` - Full build including documentation, automatic QA, checkstyle and all maven managed tests. Excludes just Ant and TCK tests. Typical time: 20 minutes.
* `mvn clean install -Pqa` - Building all distribution artifacts, running QA, checkstyle and all maven managed tests. Excludes Ant, TCK and documentation. Typical time: 10 minutes.
* `mvn clean install -Pfast` - Building all distribution artifacts, running just unit tests. Excludes QA, checkstyle, integration tests, Ant, TCK and documentation. Typical time: 7 minutes.
* `mvn clean install -Pfastest -T4C` - Building all distribution artifacts as fast as possible. Excludes everything not serving this purpose. Typical time: 1 minute.

After the build, you can run GlassFish in the following ways:
* Run GlassFish server directly: Navigate to `appserver/distributions/glassfish/target/stage/glassfish8` - it is an unpacked version of GlassFish Full. Then you can run it as usual, e.g. with `bin/startserv`
* Run GlassFish server from a ZIP: A zip file is generated either at `appserver/distributions/glassfish/target/glassfish.zip` or in your local maven repository, e.g. at `<HOME>/.m2/repository/org/glassfish/main/distributions/glassfish/<VERSION>/glassfish-<VERSION>.zip`. Unpack it and run as usual
* Run Embedded GlassFish: Find the JAR file at `appserver/extras/embedded/all/target/glassfish-embedded-all.jar` or in your local maven repository, e.g. at `<HOME>/.m2/repository/org/glassfish/main/extras/glassfish-embedded-all/<VERSION>/glassfish-embedded-all-<VERSION>.jar. Then run it with `java -jar glassfish-embedded-all.jar`

You can use also some maven optimizations, see [Maven documentation](https://maven.apache.org/ref/3.9.5/maven-embedder/cli.html).
Especially `-am`, `-amd`, `-f`, `-pl`, `-rf` and their combinations are useful.

If you use Maven 3.9+, we recommend that you copy the `.mvn/maven.config.template` file into `.mvn/maven.config` and uncomment one of the options there or customize the options as you need. Then you can just run `mvn clean install` and the options in `maven.config` will be applied automatically. Or with just `mvn`, the default goal will be run with those options. See [Configuring Maven Documentation](https://maven.apache.org/configure.html)

If you want to see more logs you can use the `-Dtest.logLevel=FINEST` option set to an appropriate log level.
Note that this applies just for tests which are executed by Maven and which use the **GlassFish Java Util Logging Extension (GJULE)**.

### 📦 Distribution

After building, find distributions at:
- **GlassFish Server Full Profile**: `appserver/distributions/glassfish/target/glassfish.zip`
- **GlassFish Server Web Profile**: `appserver/distributions/web/target/web.zip`
- **Embedded GlassFish All**:
`appserver/extras/embedded/all/target/glassfish-embedded-all.jar`
- **Embedded GlassFish Web**:
`appserver/extras/embedded/web/target/glassfish-embedded-web.jar`

### Special Profiles

* `staging` - This profile was used formerly to access the Staging Maven repository. At this time the repository is not available any more and the profile should not be used.
* `jacoco` - enables the [JaCoCo](https://www.eclemma.org/jacoco/) agent in tests, so you can import its output to your editor, i.e. Eclipse, and see the code coverage.
* `jacoco-merge` - merges all JaCoCo output files found in subdirectories and merges them into one. It is useful to see code which wasn't even touched by tests.

### Special Scripts

* `./updateVersion.sh 6.99.99.experimental` - useful for custom distributions, so you can avoid conflicts with version in master branch.
* `./runtests.sh [testBlockName] [?glassfishVersion]` - useful to run [old additional tests](#old-additional-tests) locally
* `./validateJars.sh` - uses the bnd command to check OSGI dependencies in all target directories

## Additional Testing

After building the GlassFish distribution artifacts you can execute also additional tests managed by bash scripts.
They are quite old and have high technical debt, but at this moment they still provide useful service.

### QuickLook

`mvn -f appserver/tests/quicklook/pom.xml test -Dglassfish.home=$(pwd)/appserver/distributions/glassfish/target/stage/glassfish8/glassfish`

* Usual time: 3 minutes
* see [QuickLook_Test_Instructions](https://github.com/eclipse-ee4j/glassfish/blob/master/appserver/tests/quicklook/QuickLook_Test_Instructions.html)

### Old Additional Tests

:warning: If the script fails, sometimes it doesn't stop the domain and you have to do that manually.

:warning: Some of the scripts do in-place filtering or generate other sources which remain and later affect subsequent executions. You have to remove those changes manually.

```
* ./runtests.sh batch_all # Usual time: 1 minute
* ./runtests.sh cdi_all # Usual time: 6 minutes
* ./runtests.sh connector_group_1 # Usual time: 16 minutes
* ./runtests.sh connector_group_2 # Usual time: 3 minutes
* ./runtests.sh connector_group_3 # Usual time: 4 minutes
* ./runtests.sh connector_group_4 # Usual time: 16 minutes
* ./runtests.sh deployment_all # Usual time: 8 minutes
* ./runtests.sh ejb_group_1 # Usual time: 10 minutes
* ./runtests.sh ejb_group_2 # Usual time: 7 minutes
* ./runtests.sh ejb_group_3 # Usual time: 18 minutes
* ./runtests.sh ejb_group_embedded # Usual time: 4 minutes
* ./runtests.sh jdbc_all # Usual time: 20 minutes
* ./runtests.sh naming_all # Usual time: 2 minutes
* ./runtests.sh persistence_all # Usual time: 3 minutes
* ./runtests.sh security_all # Usual time: 8 minutes
* ./runtests.sh web_jsp # Usual time: 8 minutes
* ./runtests.sh webservice_all # Usual time: 10 minutes
* ./runtests.sh ejb_web_all # Usual time: 4 minutes
* ./runtests.sh ql_gf_web_profile_all # Usual time: 2 minutes
* ./runtests.sh ql_gf_full_profile_all # Usual time: 4 minutes
```
* many tests under appserver/tests subdirectories; they are still waiting for someone's attention.

### Pull request workflow

The pull request workflow is described on [Eclipse GlassFish / workflow](https://glassfish.org/pr_workflow).

Build server results of pull requests can be found at [CI Glassfish](https://ci.eclipse.org/glassfish/).

## Basic Usage

* Starting Eclipse GlassFish: `glassfish8/bin/asadmin start-domain`
* Visit [http://localhost:4848](http://localhost:4848)
* Stopping Eclipse GlassFish: `glassfish8/bin/asadmin stop-domain`

## Security Considerations

> [!IMPORTANT]
> For production deployments, consider the following security measures:
> * **Don't use domain1**: The `domain1` can be used just in automated tests but never on production as it contains
    private keys which are implicitly compromised. You should create a new domain for your environment.
> * **Disable the Admin Console**: The web-based Administration Console should be disabled in production environments
   to reduce attack surface and prevent CSRF attacks where malicious links can execute admin commands.
   Use `asadmin set server.admin-service.property.adminConsoleStartup=never` and restart the domain.
> * **Enable Secure Admin**: Use `asadmin enable-secure-admin` to secure administrative communications.
> * **Use strong passwords**: Change default passwords and use strong authentication.

For comprehensive security guidance, see the [Security Guide](docs/latest/security-guide.html).

## Professional Services and Enterprise Support

This section is dedicated to companies offering products and services around Eclipse GlassFish.

The Eclipse GlassFish project does not endorse or recommend any of the companies on this page. We love all our supporters equally.

Professional Services and Enterprise support are available through following companies:
- [OmniFish](https://omnifish.ee/solutions/#support)
- [ManageCat](https://www.managecat.com/services-and-support)

