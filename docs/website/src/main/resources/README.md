# Eclipse GlassFish

## About

Eclipse GlassFish is a [Jakarta EE compatible implementation](compatibility)
sponsored by the Eclipse Foundation.



## Latest News

## May 31, 2024 -- Eclipse GlassFish 7.0.15 Available

We are very happy to bring you Eclipse GlassFish 7.0.15.

Download links are available from the [GlassFish Download page](download.md).

In this release we updated and thoroughly tested a lot of important components, including Exousia (Security), Mojarra (Faces), Jersey (REST) and Yasson/Parsson (JSON).

Testing has been improved by fixing a number of issues within the tests itself.

Finally, we fixed a number of errors in the documentation.

Download links are available from the [GlassFish Download page](download.md).


## March 31, 2024 -- Eclipse GlassFish 7.0.14 Available

We are very happy to bring you Eclipse GlassFish 7.0.14.

Download links are available from the [GlassFish Download page](download.md).

This release features among others an important NPE fix for the SSHLauncher and is highly recommended for our users who make use of this launcher.

The thread safety of transactions was improved, and the JDBC connection pool sizing logic was also improved, fixing an annoying bug.

To maintain future quality of GlassFish, many fixes to the internal TCKs tests were done.

As a new feature, command completion is now enabled in the OSGi interactive console.

Download links are available from the [GlassFish Download page](download.md)


## February 29, 2024 -- Eclipse GlassFish 7.0.13 Available

We are very happy to bring you Eclipse GlassFish 7.0.13. 

Download links are available from the [GlassFish Download page](download.md).

In the release for this month we replaced many synchronized blocks by reentrant locks (to accommodate JDK 21 virtual threads). 

We also looked at SSO between clusters, and fixed a long standing bug there. 

A long running investigation into potential resource leaks finally came to a conclusion, and resulted in many fixes throughout the code base. The admin console also saw various improvements, specifically with respect to loading.

Download links are available from the [GlassFish Download page](download.md)


## January 30, 2024 -- Eclipse GlassFish 7.0.12 Available

We are very happy to bring you Eclipse GlassFish 7.0.12. 

Download links are available from the [GlassFish Download page](download.md).

This release we focused on finding and fixing the root cause of several "strange" WebSocket related bugs that we witnessed in the past. We also did a similar thing related to several issues with running apps on the default context root, especially where after authentication redirects happened to another URL. 

Furthermore we looked into problems with authentication and SSO in a cluster and did some initial fixes. Handling and processing of logging was looked at once again, something we have been improving a lot step by step through various iterations of releases. 

Finally but not least a nasty ConcurrentModificationException was fixed, and a memory leak (via HK2) was solved.

Download links are available from the [GlassFish Download page](download.md).


## November 30, 2023 -- Eclipse GlassFish 7.0.11 Available

We are happy to announce the release of Eclipse GlassFish 7.0.11. 

Download links are available from the [GlassFish Download page](download.md).

This release sees an important fix where WebSockets would not work at all for applications on the default context root (e.g. https://example.com vs https://example.com/myapp). 

This month we have concentrated on the AdminGUI and fixed an assortment of small but annoying defects in it. Various major components were updated, such as Jersey (Jakarta REST), Tyrus (Jakarta WebSockets) and Mojarra (Jakarta Faces). 

To keep the project maintainable and well tested, several tests were added and improved as well.

Download links are available from the [GlassFish Download page](download.md).


## October 29, 2023 -- Eclipse GlassFish 7.0.10 Available

The GlassFish team is very happy to present you another great release of GlassFish.

Download links are available from the [GlassFish Download page](download.md).

In this release a 10 months long operation to get an internal dependency to the slf4j-api removed finally got to fruition. This involved the intense cooperation of multiple teams, and we're exceptionally happy to have finally been able to do this. 

We also did a lot of work to make our builds repeatable, and we did a ton of refactoring to the internal security packages of GlassFish, making them easier to understand and therefore easier to maintain. Any external code depending on these internal packages (such as potentially custom LoginModules/Realms) may have to update (we recommend of course not depending on internal packages and using public APIs). 

As every release, we integrated many component updates, and did a number of smaller fixes.

Download links are available from the [GlassFish Download page](download.md).

## September 29, 2023 -- Eclipse GlassFish 7.0.9 Available

We're happy to present you with the latest GlassFish release.

Download links are available from the [GlassFish Download page](download.md).

In this release the modularity of GlassFish is once again increased by moving the Jakarta Authentication implementation code to a new standalone project: [Epicyro](https://github.com/eclipse-ee4j/epicyro). We also enabled the GlassFish embedded tests again, which were dormant for a long time. Among the many updated components, Exousia was updated specifically to fix a bug with deployments on virtual servers, and the ORB was updated to fix a somewhat obscure bug where a remote EJB returned a JDK defined enum type.

Download links are available from the [GlassFish Download page](download.md).

## August 28, 2023 -- Eclipse GlassFish 7.0.8 Available

The entire GlassFish team is happy to present you another great GlassFish release.

Download links are available from the [GlassFish Download page](download.md).

This release fixes multi-jar compatibility in GlassFish and does further preparations for JDK 21. It includes various component updates among which those for CDI, Faces, and JSON. Test coverage is improved by adding the TCKs for REST Client and Connectors. Finally, various CDI extensions have been optimised to reduce excessive and unnecessary calls to them.

Download links are available from the [GlassFish Download page](download.md).

## July 29, 2023 -- Eclipse GlassFish 7.0.7 Available

Another month, another new version of Eclipse GlassFish 7.0.7 released today.

This release prepares GlassFish for the upcoming JDK 21; it compiles and passes all internal tests using OpenJDK 21ea33. Several TCKs passed on JDK 21 as well, but the TCK as a whole is not JDK 21 compatible yet. 

This release also adds support for MicroProfile JWT 2.1, and has many components updated to their latest version. Several issues have again been fixed in the Admin Console. An important bug regarding Enterprise Beans method generation has been fixed, as has a remote access issue for Enterprise Beans.

Download links are available from the [GlassFish Download page](download.md).


## June 29, 2023 -- Eclipse GlassFish 7.0.6 Available

We are very happy to present the release today of Eclipse GlassFish 7.0.6.

The main features of this release are the newly added support of the MicroProfile REST Client and a new way to start GlassFish, which is a preparation for Docker images that will run GlassFish in the foreground in a single JVM process to save memory consumed by Docker containers.

This release also contains the usual amount of fixes, and a number of important component updates. Specifically the EclipseLink and Soteria updates fix important bugs (see their release notes).

Download links are available from the [GlassFish Download page](download.md).

## May 30, 2023 -- Eclipse GlassFish 7.0.5 Available

We are pleased to announce the release today of Eclipse GlassFish 7.0.5.

The main features of this release are a number of important bug fixes such as one in deployment-time recursive bytecode, resetting the security context if a principal has not changed, and several fixes in clustering.

Jakarta EE components have been updated for Mail, JSON Processing, and REST. Auxiliary components such as Jackson and Commons IO have also been updated to their latest version.

Download links are available from the [GlassFish Download page](download.md).

## April 27, 2023 -- Eclipse GlassFish 7.0.4 Available

We are pleased to announce the release today of Eclipse GlassFish 7.0.4.

The main features of this release are important bug fixes for things like a class loader leak, and again several fixes in the admin console such as the ability to upload a war file. 

A new feature for ScatteredArchive has been added to GlassFsih Embedded.

Download links are available from the [GlassFish Download page](download.md).

## March 30, 2023 -- Eclipse GlassFish 7.0.3 Available

We are pleased to announce the release today of Eclipse GlassFish 7.0.3. 

The main features of this release are various bug fixes for things like a StackOverflow exception, a deployment error, and creating JavaMail sessions using the admin console. Additionally components have been updated for Faces, Messaging, Persistence, and (MP) Config. Support for newer JDK versions has been solidified by a new HK2 release and ASM 9.5 integration.

Eclipse GlassFish 7.0.3 compiles and runs with JDK 11 to JDK 20 releases. MicroProfile support requires JDK 17 or higher.

## February 27, 2023 -- Eclipse GlassFish 7.0.2 Available

We are happy to announce the release of Eclipse GlassFish 7.0.2. 

The main features of this release are the updates of various components, fixing a wide array of issues. Specifically with WaSP 3.2.0 a major change is that it now includes the Pages Standard Tags, so the separate jar for this is no longer present in GlassFish. We also fixed an important regression where requesting a directory from the ClassLoader failed for exploded deploys. This now works again. Additionally the version (7.0.2 now) is reported correctly again, which can be important for package managers such as brew.

Eclipse GlassFish 7.0.2 compiles and runs with JDK 11 to JDK 19 releases. MicroProfile support requires JDK 17 or higher. JDK 20ea30 has been succesfully used to compile and run GlassFish as well, but is not yet officially supported.

## January 30, 2023 -- Eclipse GlassFish 7.0.1 Available

We are pleased to announce the release of Eclipse GlassFish 7.0.1. 

The main features of this release are an overhaul of some of the class loader mechanics (speeding up various operations), and making shutdown monitoring more reliable. A new docker image has been added as well (it will be officially published on docker hub later).
Furthermore in this release a number of components have been updated to their latest version, and the code can now be build with JDK 20ea.

Eclipse GlassFish 7.0.1 compiles and runs with JDK 11 to JDK 19 releases. MicroProfile support requires JDK 17 or higher. JDK 20ea30 has been succesfully used to compile and run GlassFish as well, but is not yet officially supported.

## December 14, 2022 - The final version of Eclipse GlassFish 7 released

After huge effort by the Eclipse GlassFish team and a lot of fellow contributors, Eclipse GlassFish 7.0.0 is finally released.

Download links are available from the [GlassFish Download page](download.md).

The main new feature is [Jakarta EE 10](https://jakarta.ee/specifications/platform/10/) support, and everything that comes with that. Additionally GlassFish now provides support for the [MicroProfile Config](https://microprofile.io/microprofile-config/) and [MicroProfile JWT](https://microprofile.io/project/eclipse/microprofile-jwt-auth/) APIs, and the latest [Jakarta MVC](https://www.mvc-spec.org/) 2.0 release.

This release also features a massive overhaul and cleanup of the DOL module (Deployment Object Library), a large cleanup of how JNDI names are handled internally, and many fixes in the logging functionality and in the way how GlassFish servers start and stop.

Eclipse GlassFish 7.0.0 compiles and runs with JDK 11 to JDK 19 releases. MicroProfile support requires JDK 17 or higher.

## September 19, 2022 - Eclipse GlassFish 7.0.0-M8 certified as Jakarta EE 10 compatible

We are pleased to announce that with the milestone release 7.0.0-M8, Eclipse GlassFish is officially certified as a Jakarta EE 10 compatible implementation.

* GlassFish 7.0.0-M8 is a [Jakarta EE 10 Full Profile](https://github.com/eclipse-ee4j/jakartaee-platform/issues/514) compatible implementation
* GlassFish Web 7.0.0-M8 is a [Jakarta EE 10 Web Profile](https://github.com/eclipse-ee4j/jakartaee-platform/issues/534) compatible implementation

You can download both milestone releases from the [Eclipse Foundation Download portal](https://download.eclipse.org/ee4j/glassfish/) to try out what's new in Jakarta EE 10. 

## February 13, 2022 -- Eclipse GlassFish 6.2.5 Available

We are pleased to announce the release of Eclipse GlassFish 6.2.5. This release provides implementations
of the Jakarta EE 9.1 Platform and Web Profile specifications. Download links are available from the [GlassFish Download page](download.md). Eclipse GlassFish 6.2.5 implements the Jakarta EE 9.1 specification ([Jakarta EE 9.1 Platform](https://jakarta.ee/specifications/platform/9.1/), [Jakarta EE 9 Web Profile](https://jakarta.ee/specifications/webprofile/9.1/)). GlassFish 6.2.5 updates and reenables a lot of tests that were disabled in previous versions (most after the GF 5 to 6 transition), once again improves JDK 17 compatibility (cases found by the new tests), fixes several bugs, and contains new versions of Hibernate Validator, Jackson and others.

GlassFish 6.2.5 compiles and run with JDK 11 to JDK 18-EA releases.

Note this release requires at least JDK 11.

## January 10, 2022 -- Eclipse GlassFish 6.2.4 Available

We are pleased to announce the release of Eclipse GlassFish 6.2.4. This release provides implementations
of the Jakarta EE 9.1 Platform and Web Profile specifications. Download links are available from the [GlassFish Download page](download.md). Eclipse GlassFish 6.2.4 implements the Jakarta EE 9.1 specification ([Jakarta EE 9.1 Platform](https://jakarta.ee/specifications/platform/9.1/), [Jakarta EE 9 Web Profile](https://jakarta.ee/specifications/webprofile/9.1/)). GlassFish 6.2.4 brings initial support for JDK 18 (tested until ea29) and adds running several standalone Jakarta EE TCKs directly from the project. An import internal fix is removing a troublesome circular dependency between GlassFish and Jersey.

GlassFish 6.2.4 compiles and run with JDK 11 to JDK 18-EA releases.

Note this release requires at least JDK 11.

## November 18, 2021 -- Eclipse GlassFish 6.2.3 Available

We are pleased to announce the release of Eclipse GlassFish 6.2.3. This release provides implementations
of the Jakarta EE 9.1 Platform and Web Profile specifications. Download links are available from the [GlassFish Download page](download.md). Eclipse GlassFish 6.2.3 implements the Jakarta EE 9.1 specification ([Jakarta EE 9.1 Platform](https://jakarta.ee/specifications/platform/9.1/), [Jakarta EE 9 Web Profile](https://jakarta.ee/specifications/webprofile/9.1/)). GlassFish 6.2.3 brings Admin console fixes, build times improvement, component updates, and bug fixes.

GlassFish 6.2.3 compiles with JDK 11 to JDK 17 and runs on JDK 11 to JDK 17. GlassFish 6.2.3 also compiles and runs on JDK 18-EA releases.

Note this release requires at least JDK 11.

## October 1, 2021 -- Eclipse GlassFish 6.2.2 Available

We are pleased to announce the release of Eclipse GlassFish 6.2.2. This release provides implementations
of the Jakarta EE 9.1 Platform and Web Profile specifications. Download links are available from the [GlassFish Download page](download.md). Eclipse GlassFish 6.2.2 implements the Jakarta EE 9.1 specification ([Jakarta EE 9.1 Platform](https://jakarta.ee/specifications/platform/9.1/), [Jakarta EE 9 Web Profile](https://jakarta.ee/specifications/webprofile/9.1/)). GlassFish 6.2.2 brings GlassFish embedded back to live, and contains an import fix for a memory leak. A major behind the scenes accomplishment is that all active tests now use JUnit 5.

GlassFish 6.2.2 compiles with JDK 11 to JDK 17 and runs on JDK 11 to JDK 17. GlassFish 6.2.2 has been briefly tested with JDK 18-EA releases.

Note this release requires at least JDK 11.

## August 28, 2021 -- Eclipse GlassFish 6.2.1 Available

We are happy to announce the release of Eclipse GlassFish 6.2.1. This release provides implementations
of the Jakarta EE 9.1 Platform and Web Profile specifications. Download links are available from the [GlassFish Download page](download.md). Eclipse GlassFish 6.2.1 implements the Jakarta EE 9.1 specification ([Jakarta EE 9.1 Platform](https://jakarta.ee/specifications/platform/9.1/), [Jakarta EE 9 Web Profile](https://jakarta.ee/specifications/webprofile/9.1/)). GlassFish 6.2.1 now has much improved support for JDK 17, and includes new component Eclipse Exousia, the standalone Jakarta Authorization implementation. GlassFish 6.2.1 compiles with JDK 11 to JDK 17.

Note this release requires at least JDK 11.


## May 25, 2021 -- Eclipse GlassFish 6.1 Available

We are happy to announce the final release of Eclipse GlassFish 6.1. This release provides implementations
of the Jakarta EE 9.1 Platform and Web Profile specifications. Download links are available from the [GlassFish Download page](download.md). Eclipse GlassFish 6.1 implements the Jakarta EE 9.1 specification ([Jakarta EE 9.1 Platform](https://jakarta.ee/specifications/platform/9.1/), [Jakarta EE 9 Web Profile](https://jakarta.ee/specifications/webprofile/9.1/)).

Note this release requires JDK 11.


## April 14, 2021 -- Eclipse GlassFish 6.1 RC1 Available

This is the first release candidate of Eclipse GlassFish 6.1 and is available for [download](https://glassfish.org/download). Eclipse GlassFish 6.1 is targetted to be a Compatible Implementation of Jakarta EE 9.1. Note this release requires JDK 11. The final release is scheduled to coincide with the final release of Jakarta EE 9.1.

## December 31, 2020 -- Eclipse GlassFish 6 Stable Release

We are pleased to announce the stable release of Eclipse GlassFish 6.0. This release provides implementations
of the Jakarta EE 9 Platform and Web Profile specifications. Download links are available from the [GlassFish Download page](download.md). Eclipse GlassFish 6 implements the Jakarta EE 9 specification ([Jakarta EE 9 Platform](https://jakarta.ee/specifications/platform/9/), [Jakarta EE 9 Web Profile](https://jakarta.ee/specifications/webprofile/9/)).

### October 24, 2020 -- Eclipse GlassFish 6.0 Release Candidate 1 is released

This is the first release candidate of Eclipse GlassFish 6.0 and is available for [download](https://glassfish.org/download).
This first Release Candidate is functionally complete and is the first version to pass the in progress Jakarta EE 9 Platform CTS and standalone TCKs for both Full Platform and Web Platform. Further development will be carried out before final release but no major functinal changes will be made.

### June 23, 2020 -- Eclipse GlassFish 6.0 Milestone 1 is released

This release contains new Jakarta EE 9 compatible APIs. This is an early alpha release of Eclipse GlassFish 6. See the download page to get your copy.

### September 10, 2019 -- Eclipse GlassFish 5.1 is certified compatible with Eclipse Jakarta EE 8

Eclipse GlassFish 5.1 was certified as part of the release of the Jakarta EE 8 specification. This certification was completed without requiring any changes to Eclipse GlassFish 5.1, released in January of this same year. Eclipse GlassFish 5.1 is compatible with both Jakarta EE 8 and also Java EE 8.

### January 28, 2019 -- Eclipse GlassFish 5.1 is released

See the
[press release](https://globenewswire.com/news-release/2019/01/29/1706637/0/en/Java-EE-8-Compatible-Eclipse-GlassFish-5-1-Released.html)
and related blog posts
([here](https://dmitrykornilov.net/2019/01/29/eclipse-glassfish-5-1-is-released/)
and [here](https://blog.payara.fish/glassfish-5.1-release-marks-major-milestone-for-java-ee-transfer)).

### September 28, 2017 - Introducing Eclipse Enterprise for Java

See the [Jakarta EE home-page](https://jakarta.ee/).