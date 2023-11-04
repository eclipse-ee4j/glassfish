# Eclipse GlassFish Downloads

## Eclipse GlassFish 7.x

GlassFish 7.0.10 is a final release, containing final Jakarta EE 10 APIs. It compiles and runs on JDK 11 to JDK 21. MicroProfile support requires JDK 17 or higher.

This release is the second GlassFish release after OpenJDK 21 was released. GlassFish 7.0.10 compiles and passes all internal tests using OpenJDK 21. Several Jakarta EE 10 TCKs passed on JDK 21 as well. The Jakarta EE 10 TCK as a whole is not JDK 21 compatible and it's not possible to run all the Jakarta EE TCK tests. 

In this release a 10 months long operation to get an internal dependency to the slf4j-api removed finally got to fruition. This involved the intense cooperation of multiple teams, and we're exceptionally happy to have finally been able to do this. 

We also did a lot of work to make our builds repeatable, and we did a ton of refactoring to the internal security packages of GlassFish, making them easier to understand and therefore easier to maintain. Any external code depending on these internal packages (such as potentially custom LoginModules/Realms) may have to update (we recommend of course not depending on internal packages and using public APIs). 

As every release, we integrated many component updates, and did a number of smaller fixes.

Download:

* [Eclipse GlassFish 7.0.10, Jakarta EE Platform, 10](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/glassfish-7.0.10.zip)
* [Eclipse GlassFish 7.0.10, Jakarta EE Web Profile, 10](https://www.eclipse.org/downloads/download.php?file=/ee4j/glassfish/web-7.0.10.zip)
* [Eclipse GlassFish Embedded 7.0.10, Jakarta EE Full Profile, 10](https://search.maven.org/artifact/org.glassfish.main.extras/glassfish-embedded-all/7.0.10/jar)
* [Eclipse GlassFish Embedded 7.0.10, Jakarta EE Web Profile, 10](https://search.maven.org/artifact/org.glassfish.main.extras/glassfish-embedded-web/7.0.10/jar)

More details:

* [Eclipse GlassFish 7.0.10 Release Notes](https://github.com/eclipse-ee4j/glassfish/releases/tag/7.0.10)
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
