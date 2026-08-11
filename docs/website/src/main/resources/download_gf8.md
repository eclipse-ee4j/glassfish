# Eclipse GlassFish 8.x Downloads

Eclipse GlassFish is an application server, implementing Jakarta EE.
This release is corresponding with the [Jakarta EE 11](https://jakarta.ee/release/11) specification, which is a major new feature release. Eclipse GlassFish 8 requires JDK 21 or higher.

## Eclipse GlassFish 8.0.4

This release focuses on the upgrade path from GlassFish 7, on Embedded GlassFish, and on CDI integration.  It also bring huge Jakarta Faces rendering performance improvements with an upgraded Mojarra, which now renders pages 3 times faster than before.

### Main Changes

* Domains created by GlassFish 7.0.x now have their legacy JKS/JCEKS security stores migrated to PKCS12
* Embedded GlassFish propagates deployment failures
* Jakarta REST artifacts can be injected with @Inject out of the box
* @Transactional rollbackOn/dontRollbackOn carried by CDI stereotypes are resolved
* Thread-context-classloader hack in Weld bean deployment archive replaced by per-BDA ResourceLoader SPI

### TCK results

 * [Full](./certifications/jakarta-platform/11/TCK-Results-8.0.4.md)

### Download

* [Eclipse GlassFish 8.0.4, Jakarta EE Platform 11](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-8.0.4.zip) (zip)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.distributions/glassfish/8.0.4)
* [Eclipse GlassFish 8.0.4, Jakarta EE Web Profile 11](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/web-8.0.4.zip) (zip)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.distributions/web/8.0.4)
* [Eclipse GlassFish Embedded 8.0.4, Jakarta EE Platform 11](https://repo1.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-all/8.0.4/glassfish-embedded-all-8.0.4.jar) (jar) — run with `java -jar glassfish-embedded-all-8.0.4.jar`, no installation required
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-all/8.0.4)
* [Eclipse GlassFish Embedded 8.0.4, Jakarta EE Web Profile 11](https://repo1.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-web/8.0.4/glassfish-embedded-web-8.0.4.jar) (jar) — run with `java -jar glassfish-embedded-web-8.0.4.jar`, no installation required
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-web/8.0.4)

More details:

* [Eclipse GlassFish 8.0.4 Release Notes](https://github.com/eclipse-ee4j/glassfish/releases/tag/8.0.4)
* [Jakarta EE Specifications](https://jakarta.ee/specifications/) for more info about Jakarta EE


## Eclipse GlassFish 8.0.3

This release focused on security improvements including components of GlassFish. - see [release notes](https://github.com/eclipse-ee4j/glassfish/releases/tag/8.0.3) for details.

### Main Changes

* Security improvements
* Optimized Jakarta Faces performance (rendering performance improved by more than 2 times)
* Optimized Embedded GlassFish startup time (starts approximately 10% faster)

### TCK results

 * [Full](./certifications/jakarta-platform/11/TCK-Results-8.0.3.md)

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

* [Eclipse GlassFish 8.0.3 Release Notes](https://github.com/eclipse-ee4j/glassfish/releases/tag/8.0.3)
* [Jakarta EE Specifications](https://jakarta.ee/specifications/) for more info about Jakarta EE

## Eclipse GlassFish 8.0.2

This release focused on dependencies - see [release notes](https://github.com/eclipse-ee4j/glassfish/releases/tag/8.0.2) for details.

### Main Changes

* Improved local host evaluation
* Optimized usage of resources and speed of deployment
* Security improvements

### Download

* [Eclipse GlassFish 8.0.2, Jakarta EE Platform 11](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-8.0.2.zip) (zip)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.distributions/glassfish/8.0.2)
* [Eclipse GlassFish 8.0.2, Jakarta EE Web Profile 11](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/web-8.0.2.zip) (zip)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.distributions/web/8.0.2)
* [Eclipse GlassFish Embedded 8.0.2, Jakarta EE Platform 11](https://repo1.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-all/8.0.2/glassfish-embedded-all-8.0.2.jar) (jar) — run with `java -jar glassfish-embedded-all-8.0.2.jar`, no installation required
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-all/8.0.2)
* [Eclipse GlassFish Embedded 8.0.2, Jakarta EE Web Profile 11](https://repo1.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-web/8.0.2/glassfish-embedded-web-8.0.2.jar) (jar) — run with `java -jar glassfish-embedded-web-8.0.2.jar`, no installation required
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-web/8.0.2)

More details:

* [Eclipse GlassFish 8.0.2 Release Notes](https://github.com/eclipse-ee4j/glassfish/releases/tag/8.0.2)
* [Jakarta EE Specifications](https://jakarta.ee/specifications/) for more info about Jakarta EE

## Eclipse GlassFish 8.0.1

### Main Changes

* Refactored [glassfish-itest-tools](https://central.sonatype.com/artifact/org.glassfish.main/glassfish-itest-tools) to make testing with JUnit6 easier.
* Optimized usage of resources and speed of deployment

### Download

* [Eclipse GlassFish 8.0.1, Jakarta EE Platform 11](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-8.0.1.zip) (zip)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.distributions/glassfish/8.0.1)
* [Eclipse GlassFish 8.0.1, Jakarta EE Web Profile 11](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/web-8.0.1.zip) (zip)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.distributions/web/8.0.1)
* [Eclipse GlassFish Embedded 8.0.1, Jakarta EE Platform 11](https://repo1.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-all/8.0.1/glassfish-embedded-all-8.0.1.jar) (jar) — run with `java -jar glassfish-embedded-all-8.0.1.jar`, no installation required
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-all/8.0.1)
* [Eclipse GlassFish Embedded 8.0.1, Jakarta EE Web Profile 11](https://repo1.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-web/8.0.1/glassfish-embedded-web-8.0.1.jar) (jar) — run with `java -jar glassfish-embedded-web-8.0.1.jar`, no installation required
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-web/8.0.1)

More details:

* [Eclipse GlassFish 8.0.1 Release Notes](https://github.com/eclipse-ee4j/glassfish/releases/tag/8.0.1)
* [Jakarta EE Specifications](https://jakarta.ee/specifications/) for more info about Jakarta EE

## Eclipse GlassFish 8.0.0

### Breaking Changes

* Compliance with Jakarta EE 11
* Minimal supported version is Java 21
* Removed the SecurityManager based authorization

### New Features

* Virtual thread support
* Jakarta Data support

### Download

* [Eclipse GlassFish 8.0.0, Jakarta EE Platform 11](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-8.0.0.zip) (zip)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.distributions/glassfish/8.0.0)
* [Eclipse GlassFish 8.0.0, Jakarta EE Web Profile 11](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/web-8.0.0.zip) (zip)
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.distributions/web/8.0.0)
* [Eclipse GlassFish Embedded 8.0.0, Jakarta EE Platform 11](https://repo1.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-all/8.0.0/glassfish-embedded-all-8.0.0.jar) (jar) — run with `java -jar glassfish-embedded-all-8.0.0.jar`, no installation required
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-all/8.0.0)
* [Eclipse GlassFish Embedded 8.0.0, Jakarta EE Web Profile 11](https://repo1.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-web/8.0.0/glassfish-embedded-web-8.0.0.jar) (jar) — run with `java -jar glassfish-embedded-web-8.0.0.jar`, no installation required
  * [Maven coordinates](https://central.sonatype.com/artifact/org.glassfish.main.extras/glassfish-embedded-web/8.0.0)

More details:

* [Eclipse GlassFish 8.0.0 Release Notes](https://github.com/eclipse-ee4j/glassfish/releases/tag/8.0.0)
* [Jakarta EE Specifications](https://jakarta.ee/specifications/) for more info about Jakarta EE
