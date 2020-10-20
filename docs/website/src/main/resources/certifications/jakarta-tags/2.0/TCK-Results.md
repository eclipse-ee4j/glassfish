TCK Results
===========

As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta Standard Tag Library.

# Eclipse GlassFish 6.0, Full Profile, Certification Summary

- Product Name, Version and download URL (if applicable): <br/>
  [Eclipse EE4J implementation of Jakarta Standard Tag Library 2.0.0](https://github.com/eclipse-ee4j/jstl-api/releases/download/2.0.0-IMPL-RELEASE/jakarta.servlet.jsp.jstl-2.0.0.jar)
  [Eclipse GlassFish 6.0](https://github.com/eclipse-ee4j/glassfish/releases/download/6.0.0-M3-2020-10-04/glassfish-6.0.0-M3-2020-10-04.zip)
- Specification Name, Version and download URL: <br/>
  [Jakarta Standard Tag Library 2.0](https://jakarta.ee/specifications/tags/2.0/)
- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Standard Tag Library 2.0.0](https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee9-eftl/staged-900/jakarta-tags-tck-2.0.0.zip), SHA-256: `98f962da1a9c86fd1463ab8e65292cec7a7594ee000e14b4330bac8a34cfd355`
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
[javatest.batch] Completed running 542 tests.
[javatest.batch] Number of Tests Passed      = 542
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0
[javatest.batch] ********************************************************************************
```

```
BUILD SUCCESSFUL
Total time: 13 minutes 43 seconds
SHA256_API=70c0b4732e539b8cdf8189942416cd5759a951b3aef3ba5a2e3d4eee42ebb605
SHA256_TCK=98f962da1a9c86fd1463ab8e65292cec7a7594ee000e14b4330bac8a34cfd355
Product_download=https://github.com/eclipse-ee4j/glassfish/releases/download/6.0.0-M3-2020-10-04/glassfish-6.0.0-M3-2020-10-04.zip
API_download=https://jakarta.oss.sonatype.org/content/repositories/staging/jakarta/servlet/jsp/jstl/jakarta.servlet.jsp.jstl-api/2.0.0/jakarta.servlet.jsp.jstl-api-2.0.0.jar
TCK_download=https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee9-eftl/staged-900/jakarta-tags-tck-2.0.0.zip
OS4=3.12.0 NAME="Alpine Linux" ID=alpine VERSION_ID=3.12.0 PRETTY_NAME="Alpine Linux v3.12" HOME_URL="https://alpinelinux.org/" BUG_REPORT_URL="https://bugs.alpinelinux.org/"
JDK_VERSION=java version "1.8.0_202" Java(TM) SE Runtime Environment (build 1.8.0_202-b08) Java HotSpot(TM) 64-Bit Server VM (build 25.202-b08, mixed mode)
```