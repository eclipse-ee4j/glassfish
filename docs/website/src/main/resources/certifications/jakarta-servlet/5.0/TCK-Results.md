TCK Results
===========

As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta Servlet.

# Eclipse GlassFish 6.0, Full Profile, Certification Summary

- Product Name, Version and download URL (if applicable): <br/>
  [Eclipse Glassfish 6.0.0-M2-servlet5](https://github.com/eclipse-ee4j/glassfish/releases/tag/6.0.0-M2-servlet5)
- Specification Name, Version and download URL: <br/>
  [Jakarta Servlet 5.0](https://jakarta.ee/specifications/servlet/5.0/)
- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Servlet TCK, 5.0](https://download.eclipse.org/jakartaee/servlet/5.0/eclipse-servlet-tck-5.0.0.zip), SHA-256: `c3d2e6200b16ab2b253022a5c11030eb5fd7b6ac6d244a49c83c9ddf73fa329b`
- Public URL of TCK Results Summary: <br/>
  [TCK results summary](TCK-Results.html)
- Any Additional Specification Certification Requirements: <br/>
  None
- Java runtime used to run the implementation: <br/>
  Oracle JDK 1.8.0_202
- Summary of the information for the certification environment, operating system, cloud, ...: <br/>
  Alpine Linux v3.12

Test results:

```
[javatest.batch] ********************************************************************************
[javatest.batch] Completed running 1691 tests.
[javatest.batch] Number of Tests Passed      = 1691
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0
[javatest.batch] ********************************************************************************

BUILD SUCCESSFUL
Total time: 54 minutes 6 seconds
SHA256_GF=ab42efbbf6fa45f1db9f69cb3afa98f6f05b71113d265fc83034c31bc23d2318
SHA256_API=cbfbb73a69f9a4dbd76e69605824d60c7ef8576d3468059865e3c3b902aa200f
SHA256_TCK=c3d2e6200b16ab2b253022a5c11030eb5fd7b6ac6d244a49c83c9ddf73fa329b
Product_download=https://github.com/eclipse-ee4j/glassfish/releases/download/6.0.0-M2-servlet5/glassfish.zip
API_download=https://jakarta.oss.sonatype.org/content/repositories/staging/jakarta/servlet/jakarta.servlet-api/5.0.0/jakarta.servlet-api-5.0.0.jar
TCK_download=https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee9-eftl/staged-900/jakarta-servlet-tck-5.0.0.zip
OS4=3.12.0 NAME="Alpine Linux" ID=alpine VERSION_ID=3.12.0 PRETTY_NAME="Alpine Linux v3.12" HOME_URL="https://alpinelinux.org/" BUG_REPORT_URL="https://bugs.alpinelinux.org/"
JDK_VERSION=Picked up JAVA_TOOL_OPTIONS: -XX:+IgnoreUnrecognizedVMOptions -XX:+UnlockExperimentalVMOptions java version "1.8.0_202" Java(TM) SE Runtime Environment (build 1.8.0_202-b08) Java HotSpot(TM) 64-Bit Server VM (build 25.202-b08, mixed mode)
```