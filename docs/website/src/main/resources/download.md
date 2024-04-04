# Eclipse GlassFish Downloads

## Eclipse GlassFish 7.x

GlassFish 7.0.14 is a final release, containing final Jakarta EE 10 APIs. It compiles and runs on JDK 11 to JDK 21. MicroProfile support requires JDK 17 or higher.

GlassFish 7.0.14 compiles and passes all internal tests using OpenJDK 21. Several Jakarta EE 10 TCKs passed on JDK 21 as well. The Jakarta EE 10 TCK as a whole is not JDK 21 compatible and it's not possible to run all the Jakarta EE TCK tests. 

This release features among others an important NPE fix for the SSHLauncher and is highly recommended for our users who make use of this launcher.

The thread safety of transactions was improved, and the JDBC connection pool sizing logic was also improved, fixing an annoying bug.

To maintain future quality of GlassFish, many fixes to the internal TCKs tests were done.

As a new feature, command completion is now enabled in the OSGi interactive console.

Download:

* [Eclipse GlassFish 7.0.14, Jakarta EE Platform, 10](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-7.0.14.zip)
* [Eclipse GlassFish 7.0.14, Jakarta EE Web Profile, 10](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/web-7.0.14.zip)
* [Eclipse GlassFish Embedded 7.0.14, Jakarta EE Full Profile, 10](https://search.maven.org/artifact/org.glassfish.main.extras/glassfish-embedded-all/7.0.14/jar)
* [Eclipse GlassFish Embedded 7.0.14, Jakarta EE Web Profile, 10](https://search.maven.org/artifact/org.glassfish.main.extras/glassfish-embedded-web/7.0.14/jar)

More details:

* [Eclipse GlassFish 7.0.14 Release Notes](https://github.com/eclipse-ee4j/glassfish/releases/tag/7.0.14)
* [Jakarte EE Platform Specification Project](https://jakartaee.github.io/jakartaee-platform/) for more info about Jakarta EE 10


### All Eclipse GlassFish 7.x Downloads

Download all GlassFish 7.x releases at the [Eclipse GlassFish 7.x Downloads](download_gf7.md) page.

----

## Eclipse GlassFish Milestone & Nightly Downloads

You can download the latest Eclipse GlassFish development milestone or nightly version in the [Eclipse Foundation Download portal](https://download.eclipse.org/ee4j/glassfish/).

----

## Eclipse GlassFish 6.x

GlassFish 6.2.5 updates and reenables a lot of tests that were disabled in previous versions (most after the GF 5 to 6 transition), once again improves JDK 17 compatibility (cases found by the new tests), fixes several bugs, and contains new versions of Hibernate Validator, Jackson and others.

For more details on Jakarta EE 9.1, please see the [Jakarte EE Platform Specification Project](https://eclipse-ee4j.github.io/jakartaee-platform/).

* [Eclipse GlassFish ${glassfish.version.6x}, Jakarta EE Platform, 9.1](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-${glassfish.version.6x}.zip)
* [Eclipse GlassFish ${glassfish.version.6x}, Jakarta EE Web Profile, 9.1](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/web-${glassfish.version.6x}.zip)

### All Eclipse GlassFish 6.x Downloads

Download all GlassFish 6.x releases at the [Eclipse GlassFish 6.x Downloads](download_gf6.md) page.

----

## Eclipse GlassFish 5.x

The latest stable releases of Eclipse GlassFish 5.1. This version is compatible with Jakarta EE 8 Specification.

* [Eclipse GlassFish ${glassfish.version.5x} - Jakarta EE Platform, 8](https://www.eclipse.org/downloads/download.php?file=/glassfish/glassfish-${glassfish.version.5x}.zip)
* [Eclipse GlassFish ${glassfish.version.5x} - Jakarta EE Web Profile, 8](https://www.eclipse.org/downloads/download.php?file=/glassfish/web-${glassfish.version.5x}.zip)


----

## Looking for Java EE 8?

Java EE has been contributed to the Eclipse Foundation.
The Jakarta EE community is responsible for all evolution of the
project formerly known as Java EE.
If you are looking for details of the archived Java EE GlassFish project, you are welcome to
[browse here](https://javaee.github.io/glassfish). 
