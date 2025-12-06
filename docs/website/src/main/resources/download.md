# Eclipse GlassFish Downloads

## Eclipse GlassFish 7.1.0

Eclipse GlassFish is an application server, implementing [Jakarta EE](https://jakarta.ee/about).
This release is corresponding with the [Jakarta EE 10](https://jakarta.ee/specifications/platform/10) specification, which is a major feature release.

GlassFish 7.1.0 is a final release, containing final Jakarta EE 10 APIs.
It is tested with Java 17, 21 and 25 experimentally can be used also with newer or non LTS version.

This is the first release after several months of intensive work on fixes and feature upgrades.

The release marks the transition to the latest version of the Java Platform, so we dropped support for Java 11, and are now supporting Java 25 instead.
GlassFish 7.1.0 introduces support for Microprofile Health, switches keystores from JKS to PKCS12, the current industry standard, and adapted server bootstrap to JPMS.

We also implemented more user friendly interactive asadmin, improved user communication on server startups, etc. The complete list of changes is available on GitHub.

This is a step of gradual modernization while it did not slow down the development of GlassFish 8, based on Jakarta EE 11, while GlassFish 7.1.0 still supports Jakarta EE 10.

Download:

* [Eclipse GlassFish 7.1.0, Jakarta EE Platform, 10](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-7.1.0.zip) (zip)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.distributions/glassfish/7.1.0)
* [Eclipse GlassFish 7.1.0, Jakarta EE Web Profile, 10](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/web-7.1.0.zip) (zip)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.distributions/web/7.1.0)
* [Eclipse GlassFish Embedded 7.1.0, Jakarta EE Platform, 10](https://repo1.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-all/7.1.0/glassfish-embedded-all-7.1.0.jar) (jar)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-all/7.1.0)
* [Eclipse GlassFish Embedded 7.1.0, Jakarta EE Web Profile, 10](https://repo1.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-web/7.1.0/glassfish-embedded-web-7.1.0.jar) (jar)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-web/7.1.0)

More details:

* [Eclipse GlassFish 7.1.0 Release Notes](https://github.com/eclipse-ee4j/glassfish/releases/tag/7.1.0)
* [Jakarte EE Specifications](https://jakarta.ee/specifications/) for more info about Jakarta EE

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
