TCK Results
===========

As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta Security.

# Eclipse Soteria 2.0.0, Certification Summary

- Product Name, Version and download URL (if applicable): <br/>
  Eclipse Soteria 2.0.0 provides Jakarta Security API 2.0
  [Eclipse Soteria Project](https://github.com/eclipse-ee4j/soteria)
- Specification Name, Version and download URL: <br/>
  [Jakarta Security 2.0](https://jakarta.ee/specifications/security/2.0)
- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Security 2.0, TCK](https://download.eclipse.org/jakartaee/security/2.0/eclipse-security-tck-2.0.0.zip), SHA-256: `63563b05a7b09d7d840395570f4466e129026dfd071bd037ef159a9f817bf338`
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
[javatest.batch] Completed running 85 tests.
[javatest.batch] Number of Tests Passed      = 85
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0
[javatest.batch] ********************************************************************************

[javatest.batch] Total time = 175s
[javatest.batch] Setup time = 0s
[javatest.batch] Cleanup time = 0s
[javatest.batch] Test results: passed: 85

SHA256_API=4543f3b397ef9d88c8d915cd398424670b19df8bf97d3927efcaed6f5eb9415a
SHA256_IMPL=c2fef7dacd12de3506ee24311ea9653102faf2645dbde9f966385dfbcddd10fc
SHA256_GF=f69a582e2ee15f49afb48adb2393d42351256ea070a60513e1ae17afe973e9c3
SHA256_TCK=63563b05a7b09d7d840395570f4466e129026dfd071bd037ef159a9f817bf338

Product_download=https://download.eclipse.org/ee4j/glassfish/weekly/glassfish-6.0.0-SNAPSHOT-2020-10-04.zip
IMPL_download=https://jakarta.oss.sonatype.org/content/repositories/staging/org/glassfish/soteria/jakarta.security.enterprise/2.0.0-M3/jakarta.security.enterprise-2.0.0-M3.jar
TCK_download=https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee9-eftl/staged-900/jakarta-security-tck-2.0.0.zip
OS4=3.12.0 NAME="Alpine Linux" ID=alpine VERSION_ID=3.12.0 PRETTY_NAME="Alpine Linux v3.12" HOME_URL="https://alpinelinux.org/" BUG_REPORT_URL="https://bugs.alpinelinux.org/"
JDK_VERSION=java version "1.8.0_202" Java(TM) SE Runtime Environment (build 1.8.0_202-b08) Java HotSpot(TM) 64-Bit Server VM (build 25.202-b08, mixed mode)
```
