TCK Results
===========

As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta Server Pages.

# Eclipse WaSP 3.1.0, Certification Summary

- [x] Organization Name ("Organization") and, if applicable, URL: <br/>
  Eclipse Foundation
  
- [x] Product Name, Version and download URL (if applicable): <br/>
  Eclipse WaSP 3.1.0-M3 provides Jakarta Server Pages 3.1
  [Eclipse WaSP Project](https://github.com/eclipse-ee4j/wasp)
  [Eclipse Glassfish 7.0.0-28-04-2022](https://github.com/eclipse-ee4j/glassfish/releases/download/7.0.0-28-04-2022/glassfish.zip) 
- [x] Specification Name, Version and download URL: <br/>
  [Jakarta Server Pages 3.1](https://jakarta.ee/specifications/pages/3.1/)
- [x] TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Server Pages TCK 3.1](https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee10/staged/eftl/jakarta-pages-tck-3.1.0.zip),  SHA-256: `e9f33d463ca35bdf8a9985cc8871858d7e6ea8a6e90adb00a91a001b1e43e025`
- [x] Public URL of TCK Results Summary: <br/>
  [TCK results summary](./TCK-Results)
- [x] Any Additional Specification Certification Requirements: <br/>
  None
- [x] Java runtime used to run the implementation: <br/>
  OpenJDK 11.0.2, 17.0.1, 18-ea
- [x] Summary of the information for the certification environment, operating system, cloud, ...: <br/>
  Debian GNU/Linux 10
  

Test results:

```
[javatest.batch] Number of Tests Passed      = 707
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0

-----
SHA256_API=
SHA256_IMPL=119d3e9c7237a54226723faa90ab087b0eb8c0ad755ce84e5c5032a9abe24e73
SHA256_TCK=e9f33d463ca35bdf8a9985cc8871858d7e6ea8a6e90adb00a91a001b1e43e025
-----
API_download=https://jakarta.oss.sonatype.org/content/repositories/staging/jakarta/servlet/jsp/jakarta.servlet.jsp-api/3.1.0/jakarta.servlet.jsp-api-3.1.0.jar
IMPL_download=https://jakarta.oss.sonatype.org/content/repositories/staging/org/glassfish/wasp/wasp/3.1.0-M3/wasp-3.1.0-M3.jar
Product_download=https://github.com/eclipse-ee4j/glassfish/releases/download/7.0.0-28-04-2022/glassfish.zip
Product_download_orig=https://ci.eclipse.org/glassfish/view/GlassFish/job/glassfish_build-and-test-using-jenkinsfile/job/master/697/artifact/bundles/glassfish.zip
TCK_download=https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee10/staged/eftl/jakarta-pages-tck-3.1.0.zip
-----
OS2=Debian GNU/Linux 10
OS3=10.12
OS4=PRETTY_NAME="Debian GNU/Linux 10 (buster)" NAME="Debian GNU/Linux" VERSION_ID="10" VERSION="10 (buster)" VERSION_CODENAME=buster ID=debian HOME_URL="https://www.debian.org/" SUPPORT_URL="https://www.debian.org/support" BUG_REPORT_URL="https://bugs.debian.org/"
JDK_VERSION=openjdk version "11.0.2" 2019-01-15 OpenJDK Runtime Environment 18.9 (build 11.0.2+9) OpenJDK 64-Bit Server VM 18.9 (build 11.0.2+9, mixed mode)
JDK_VERSION=openjdk version "17.0.1" 2021-10-19 OpenJDK Runtime Environment (build 17.0.1+12-39)  OpenJDK 64-Bit Server VM (build 17.0.1+12-39,  mixed mode, sharing)
JDK_VERSION=openjdk version "18-ea"  2022-03-22 OpenJDK Runtime Environment (build 18-ea+27-1924) OpenJDK 64-Bit Server VM (build 18-ea+27-1924, mixed mode, sharing)

```
