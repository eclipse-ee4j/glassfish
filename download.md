# Eclipse GlassFish Downloads

## Eclipse GlassFish 7.x

GlassFish 7.0.13 is a final release, containing final Jakarta EE 10 APIs. It compiles and runs on JDK 11 to JDK 21. MicroProfile support requires JDK 17 or higher.

GlassFish 7.0.13 compiles and passes all internal tests using OpenJDK 21. Several Jakarta EE 10 TCKs passed on JDK 21 as well. The Jakarta EE 10 TCK as a whole is not JDK 21 compatible and it's not possible to run all the Jakarta EE TCK tests. 

In the release for this month we replaced many synchronized blocks by reentrant locks (to accommodate JDK 21 virtual threads). 

We also looked at SSO between clusters, and fixed a long standing bug there. 

A long running investigation into potential resource leaks finally came to a conclusion, and resulted in many fixes throughout the code base. The admin console also saw various improvements, specifically with respect to loading.

Download:

* [Eclipse GlassFish 7.0.13, Jakarta EE Platform, 10](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-7.0.13.zip)
* [Eclipse GlassFish 7.0.13, Jakarta EE Web Profile, 10](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/web-7.0.13.zip)
* [Eclipse GlassFish Embedded 7.0.13, Jakarta EE Full Profile, 10](https://search.maven.org/artifact/org.glassfish.main.extras/glassfish-embedded-all/7.0.13/jar)
* [Eclipse GlassFish Embedded 7.0.13, Jakarta EE Web Profile, 10](https://search.maven.org/artifact/org.glassfish.main.extras/glassfish-embedded-web/7.0.13/jar)

More details:

* [Eclipse GlassFish 7.0.13 Release Notes](https://github.com/eclipse-ee4j/glassfish/releases/tag/7.0.13)
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
