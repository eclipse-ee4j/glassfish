# Eclipse GlassFish

## About

Eclipse GlassFish is a [Jakarta EE compatible implementation](compatibility)
sponsored by the Eclipse Foundation.



## Latest News

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

## How To Contribute

The Eclipse GlassFish Project is open for contributions and your help is
greatly appreciated.
The easiest way to contribute to the Eclipse GlassFish documentation is by
opening an [issue](https://github.com/eclipse-ee4j/glassfish/issues)
that contains feedback and review comments.
Contributions to the source code are also welcome.

Please review the following links:

* [Contribute](CONTRIBUTING)
* [Pull Request Acceptance Workflow](pr_workflow)
* [License](LICENSE)

## Professional Services and Enterprise Support for Eclipse GlassFish

There are companies that provide enterprise support for Eclipse GlassFish and other professional services related to Eclipse GlassFish. 

There's a list of those companies on the [Professional Services and Enterprise Support](support.md) page.