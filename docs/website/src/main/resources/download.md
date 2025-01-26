# Eclipse GlassFish Downloads

## Eclipse GlassFish 7.x

Eclipse GlassFish is an application server, implementing Jakarta EE. This release is corresponding with the Jakarta EE 10 specification, which is a major new feature release. Jakarta EE 10 requires JDK 11 as a minimum, but also works on JDK 17 and JDK 21.

GlassFish 7.0.21 is a final release, containing final Jakarta EE 10 APIs. It compiles and runs on JDK 11 to JDK 23. MicroProfile support requires JDK 17 or higher.

This release was all about hunting down a few nasty bugs reported by GlassFish users: restart hanging on fast machines (including ephemeral ports appearing when stopping GlassFish), random 403 responses for authenticated sessions and Faces that was failing to initialize on GlassFish embedded. We're proud to announce that after a lot of research and a lot of work, we were able to squash them all. A special thanks to our users for reporting these and helping us to narrow them down!

Download:

* [Eclipse GlassFish 7.0.21, Jakarta EE Platform, 10](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-7.0.21.zip)
* [Eclipse GlassFish 7.0.21, Jakarta EE Web Profile, 10](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/web-7.0.21.zip)
* [Eclipse GlassFish Embedded 7.0.21, Jakarta EE Platform, 10](https://search.maven.org/artifact/org.glassfish.main.extras/glassfish-embedded-all/7.0.21/jar)
* [Eclipse GlassFish Embedded 7.0.21, Jakarta EE Web Profile, 10](https://search.maven.org/artifact/org.glassfish.main.extras/glassfish-embedded-web/7.0.21/jar)

More details:

* [Eclipse GlassFish 7.0.21 Release Notes](https://github.com/eclipse-ee4j/glassfish/releases/tag/7.0.21)
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
