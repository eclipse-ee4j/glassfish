# Eclipse GlassFish

## About

Eclipse GlassFish is a [Jakarta EE compatible implementation](compatibility)
sponsored by the Eclipse Foundation.

## Latest News

## May 27, 2025 -- Eclipse GlassFish 7.0.25 Available

This update emphasizes major cleanups, system optimizations, and prepares the codebase for easier future maintenance, notably in anticipation of the upcoming 7.1.0 release.

## April 23, 2025 -- Eclipse GlassFish 7.0.24 Available

This release marks an important step forward by introducing support for JDK 24 in GlassFish 7 — a capability that had previously only been available in milestone builds of GlassFish 8. Achieving this required new versions of CORBA, ORB, and PFL, the result of many months of dedicated work by the team.

The team also undertook a significant revision of how scripts are used across the project. All Linux scripts now consistently use Bash, and a unified method has been adopted for resolving the AS_INSTALL path across all platforms. Additionally, an issue on Windows related to the %t placeholder—used in JDK log formatting—was resolved by ensuring it is properly escaped.

Faster Deployment was achieved by improved file handling; the team replaced custom file discovery with Files.walkFileTree and switched from URI to Path, also adopting try-with-resources for better resource management. On Windows 11, deployment time for specifically selected huge applications dropped from 110s to 100s, with potential for 92s using caching. YourKit profiling showed a 19s improvement in FileSystem.hasBooleanAttributes.

## March 14, 2025 -- Eclipse GlassFish 7.0.23 Available

The key feature for this release is making SSH nodes work on the Windows operating system, and while at it improve the way they work on Linux. The team mainly focussed on this work, and a lot of effort went into it. We're really happy to have achieved the goal to have it fully working. Next to that some important fixes were done for logging and specifically a regression for the security principal was fixed.

Download links are available from the [GlassFish Download page](download.md).

## February 3, 2025 -- Eclipse GlassFish 7.0.22 Available

The first release of 2025 is here; GlassFish 7.0.22!

A smaller release while the OmniFish team is working on perfecting SSH in a separate feature branch. Still, a few important dependencies have been updated; notably Concurrō (Concurrency), EclipseLink (Persistence) and Jersey (REST). Furthermore, GlassFish has received a few security improvements: the admin flow login was improved by Alexander Pinčuk, and the OmniFish team improved the command logger, which now hides passwords in logged messages.

Download links are available from the [GlassFish Download page](download.md).

## January 3, 2024 -- Eclipse GlassFish 7.0.21 Available

We're over the moon to present you our first release of 2025: GlassFish 7.0.21!

This release was all about hunting down a few nasty bugs reported by GlassFish users: restart hanging on fast machines (including ephemeral ports appearing when stopping GlassFish), random 403 responses for authenticated sessions and Faces that was failing to initialize on GlassFish embedded. We're proud to announce that after a lot of research and a lot of work, we were able to squash them all. A special thanks to our users for reporting these and helping us to narrow them down!

Download links are available from the [GlassFish Download page](download.md).

## December 3, 2024 -- Eclipse GlassFish 7.0.20 Available

All of us from GlassFish are joyful with the release of GlassFish 7.0.20!

While working on GlassFish 7.1.0 and GlassFish 8, we didn't leave the stable 7.0.x series in the dark. In this release we updated a lot of our dependencies to their latest versions and did a large amount of testing to ensure everything worked as required. We also improved stability again by squashing a number of outstanding bugs.

Download links are available from the [GlassFish Download page](download.md).

## November 1, 2024 -- Eclipse GlassFish 7.0.19 Available

The full GlassFish team is happy to present you another great GlassFish release; Eclipse GlassFish 7.0.19!

Download links are available from the [GlassFish Download page](download.md).

This release is mostly just maintenance. It contains some updates, some code cleanup, few very important bug fixes and few new features to make your life easier.


## October 2, 2024 -- Eclipse GlassFish 7.0.18 Available

The full GlassFish team is happy to present you another great GlassFish release; Eclipse GlassFish 7.0.18!

Download links are available from the [GlassFish Download page](download.md).

This release is all about an exciting new feature that allows running GlassFish Embedded from the command line using `java -jar glassfish-embedded.jar`. It supports configuration via command line arguments and/or config files in the current directory, such as setting the HTTP port, deploying applications passed as arguments and running arbitrary AsAdmin commands at boot. Aside of that we did a large amount of maintenance for the internal modules `nucleus-admin` and `glassfish`, and fixed various issues that were reported by users.


## August 30, 2024 -- Eclipse GlassFish 7.0.17 Available

Another great release, Eclipse GlassFish 7.0.17, is here.

Download links are available from the [GlassFish Download page](download.md).

In this summer 2024 release of GlassFish we did a lot of maintenance in which we will continue in future releases. We fixed several security issues and the build now passes also in Java 23. We welcome feedback - if you notice that something doesn't work as expected, feel free to create an issue or even better create your own pull request.

## July 31, 2024 -- Eclipse GlassFish 7.0.16 Available

With much joy we present you Eclipse GlassFish 7.0.16.

Download links are available from the [GlassFish Download page](download.md).

In this summer 2024 release of GlassFish we added several new features. Specifically new is an admin command logger, which logs graphical interactions with the admin UI for usage in scripts. GlassFish now also allows resource references in persistence.xml, and we added a great new feature where we are creating temporary snapshots of the external application libraries during application startup, so any update of these is not longer system dependent. We also did a major refactoring of the aging TLS code and optimized the GJULE logging.


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


## [Older news …](older-news.md)
