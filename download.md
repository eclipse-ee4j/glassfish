# Eclipse GlassFish Downloads

## Eclipse GlassFish 7.x

GlassFish 7.0.16 is a final release, containing final Jakarta EE 10 APIs. It compiles and runs on JDK 11 to JDK 21. MicroProfile support requires JDK 17 or higher.

GlassFish 7.0.16 compiles and passes all internal tests using OpenJDK 21. The Jakarta EE 10 TCKs passed on JDK 21 as well. The Jakarta EE 10 TCK as a whole is now also JDK 21 compatible and it's possible to run all the Jakarta EE TCK tests. 

In this summer 2024 release of GlassFish we added several new features. Specifically new is an admin command logger, which logs graphical interactions with the admin UI for usage in scripts. GlassFish now also allows resource references in persistence.xml, and we added a great new feature where we are creating temporary snapshots of the external application libraries during application startup, so any update of these is not longer system dependent. We also did a major refactoring of the aging TLS code and optimized the GJULE logging.

Download:

* [Eclipse GlassFish 7.0.16, Jakarta EE Platform, 10](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-7.0.16.zip)
* [Eclipse GlassFish 7.0.16, Jakarta EE Web Profile, 10](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/web-7.0.16.zip)
* [Eclipse GlassFish Embedded 7.0.16, Jakarta EE Platform, 10](https://search.maven.org/artifact/org.glassfish.main.extras/glassfish-embedded-all/7.0.16/jar)
* [Eclipse GlassFish Embedded 7.0.16, Jakarta EE Web Profile, 10](https://search.maven.org/artifact/org.glassfish.main.extras/glassfish-embedded-web/7.0.16/jar)

More details:

* [Eclipse GlassFish 7.0.16 Release Notes](https://github.com/eclipse-ee4j/glassfish/releases/tag/7.0.16)
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

* [Eclipse GlassFish 6.2.5, Jakarta EE Platform, 9.1](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-6.2.5.zip)
* [Eclipse GlassFish 6.2.5, Jakarta EE Web Profile, 9.1](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/web-6.2.5.zip)

### All Eclipse GlassFish 6.x Downloads

Download all GlassFish 6.x releases at the [Eclipse GlassFish 6.x Downloads](download_gf6.md) page.

----

## Eclipse GlassFish 5.x

The latest stable releases of Eclipse GlassFish 5.1. This version is compatible with Jakarta EE 8 Specification.

* [Eclipse GlassFish 5.1.0 - Jakarta EE Platform, 8](https://www.eclipse.org/downloads/download.php?file=/glassfish/glassfish-5.1.0.zip)
* [Eclipse GlassFish 5.1.0 - Jakarta EE Web Profile, 8](https://www.eclipse.org/downloads/download.php?file=/glassfish/web-5.1.0.zip)


----

## Looking for Java EE 8?

Java EE has been contributed to the Eclipse Foundation.
The Jakarta EE community is responsible for all evolution of the
project formerly known as Java EE.
If you are looking for details of the archived Java EE GlassFish project, you are welcome to
[browse here](https://javaee.github.io/glassfish). 
