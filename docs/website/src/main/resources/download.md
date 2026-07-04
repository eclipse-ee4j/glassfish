# Eclipse GlassFish Downloads

## Eclipse GlassFish 8.0.3

Eclipse GlassFish is an application server, implementing Jakarta EE. This release is corresponding with the [Jakarta EE 11](https://jakarta.ee/release/11) specification, which is a major new feature release. Eclipse GlassFish 8 requires JDK 21 or higher.

### Breaking Changes Compared to 7.1.0

* Compliance with Jakarta EE 11
* Minimal supported version is Java 21
* Removed the SecurityManager based authorization

### New Features

* Virtual thread support
* Jakarta Data support

### Download

* [Eclipse GlassFish 8.0.3, Jakarta EE Platform 11](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-8.0.3.zip) (zip)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.distributions/glassfish/8.0.3)
* [Eclipse GlassFish 8.0.3, Jakarta EE Web Profile 11](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/web-8.0.3.zip) (zip)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.distributions/web/8.0.3)
* [Eclipse GlassFish Embedded 8.0.3, Jakarta EE Platform 11](https://repo1.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-all/8.0.3/glassfish-embedded-all-8.0.3.jar) (jar) — run with `java -jar glassfish-embedded-all-8.0.3.jar`, no installation required
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-all/8.0.3)
* [Eclipse GlassFish Embedded 8.0.3, Jakarta EE Web Profile 11](https://repo1.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-web/8.0.3/glassfish-embedded-web-8.0.3.jar) (jar) — run with `java -jar glassfish-embedded-web-8.0.3.jar`, no installation required
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-web/8.0.3)

More details:

* [Eclipse GlassFish Release Notes](https://github.com/eclipse-ee4j/glassfish/releases)
* [Jakarte EE Specifications](https://jakarta.ee/specifications/) for more info about Jakarta EE

### All Eclipse GlassFish 8.x Downloads

Download all GlassFish 8.x releases at the [Eclipse GlassFish 8.x Downloads](download_gf8.md) page.

----

## Eclipse GlassFish 7.1.1

Eclipse GlassFish is an application server, implementing [Jakarta EE](https://jakarta.ee/about).
This release is corresponding with the [Jakarta EE 10](https://jakarta.ee/specifications/platform/10) specification, which is a major feature release.

GlassFish 7.1.1 is tested with Java 17, 21 and 25.

This maintenance release focuses on security hardening and production stability.
Eclipse GlassFish 7.1.1 fixes known vulnerabilities and resolves regressions introduced in 7.1.0, while backporting most applicable fixes from GlassFish 8.0.2 and selected improvements from 8.0.3, notably a Jersey memory leak fix and improved Jakarta Faces rendering performance.

Download:

* [Eclipse GlassFish 7.1.1, Jakarta EE Platform, 10](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-7.1.1.zip) (zip)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.distributions/glassfish/7.1.1)
* [Eclipse GlassFish 7.1.1, Jakarta EE Web Profile, 10](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/web-7.1.1.zip) (zip)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.distributions/web/7.1.1)
* [Eclipse GlassFish Embedded 7.1.1, Jakarta EE Platform, 10](https://repo1.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-all/7.1.1/glassfish-embedded-all-7.1.1.jar) (jar) — run with `java -jar glassfish-embedded-all-7.1.1.jar`, no installation required
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-all/7.1.1)
* [Eclipse GlassFish Embedded 7.1.1, Jakarta EE Web Profile, 10](https://repo1.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-web/7.1.1/glassfish-embedded-web-7.1.1.jar) (jar) — run with `java -jar glassfish-embedded-web-7.1.1.jar`, no installation required
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-web/7.1.1)

More details:

* [Eclipse GlassFish 7.1.1 Release Notes](https://github.com/eclipse-ee4j/glassfish/releases/tag/7.1.1)
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
