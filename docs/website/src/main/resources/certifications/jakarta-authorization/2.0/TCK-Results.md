TCK Results
===========

As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta Authorization.

# Eclipse GlassFish 6.0 Certification Request

- Product Name, Version and download URL (if applicable): <br/>
  [Eclipse Glassfish 6.0.0-M2-servlet5](https://github.com/eclipse-ee4j/glassfish/releases/tag/6.0.0-M2-servlet5)
- Specification Name, Version and download URL: <br/>
  [Jakarta Authorization 2.0](https://jakarta.ee/specifications/authorization/2.0/)
- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Authorization TCK 2.0.0](https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee9-eftl/staged-900/jakarta-authorization-tck-2.0.0.zip), SHA-256: `01bbb9b8a906c06c4c66674178381c0f1ccc8091e86063b89435c27263e88eef`
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
[javatest.batch] Completed running 34 tests.
[javatest.batch] Number of Tests Passed      = 34
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0
[javatest.batch] ********************************************************************************

BUILD SUCCESSFUL
Total time: 1 minute 4 seconds
SHA256_API=03d6f53c6273aac7c908cd398505eaa719a918a04aa9ebb40d7775cc8a1d08a9
SHA256_TCK=01bbb9b8a906c06c4c66674178381c0f1ccc8091e86063b89435c27263e88eef
Product_download=https://github.com/eclipse-ee4j/glassfish/releases/download/6.0.0-M2-servlet5/glassfish.zip
API_download=https://jakarta.oss.sonatype.org/content/repositories/staging/jakarta/authorization/jakarta.authorization-api/2.0.0/jakarta.authorization-api-2.0.0.jar
TCK_download=https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee9-eftl/staged-900/jakarta-authorization-tck-2.0.0.zip
OS4=3.12.0 NAME="Alpine Linux" ID=alpine VERSION_ID=3.12.0 PRETTY_NAME="Alpine Linux v3.12" HOME_URL="https://alpinelinux.org/" BUG_REPORT_URL="https://bugs.alpinelinux.org/"
JDK_VERSION=java version "1.8.0_202" Java(TM) SE Runtime Environment (build 1.8.0_202-b08) Java HotSpot(TM) 64-Bit Server VM (build 25.202-b08, mixed mode)
```
