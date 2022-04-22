TCK Results
===========

As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta Standard Tag Library.

# Eclipse EE4J implementation of Jakarta Standard Tag Library 3.0.0, Certification Summary

- Product Name, Version and download URL (if applicable): <br/>
  [Eclipse EE4J implementation of Jakarta Standard Tag Library 3.0.0](https://jakarta.oss.sonatype.org/content/repositories/staging/jakarta/servlet/jsp/jstl/jakarta.servlet.jsp.jstl-api/3.0.0/jakarta.servlet.jsp.jstl-api-3.0.0.jar)
  [Eclipse GlassFish 7.0.0-M4](https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/7.0.0-M4/glassfish-7.0.0-M4.zip)
- Specification Name, Version and download URL: <br/>
  [Jakarta Standard Tag Library 3.0](https://jakarta.ee/specifications/tags/3.0/)
- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Standard Tag Library 3.0.0](https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee10/staged/eftl/jakarta-tags-tck-3.0.0.zip), SHA-256: `996c9abc6561134cea986107da0648c2a33277d2f765475994ff9b16da70cb5a`
- Public URL of TCK Results Summary: <br/>
  [TCK results summary](TCK-Results.html)
- Any Additional Specification Certification Requirements: <br/>
  None
- Java runtime used to run the implementation: <br/>
  OpenJDK 11.0.2, 17.0.1, 18-ea
- Summary of the information for the certification environment, operating system, cloud, ...: <br/>
  Debian GNU/Linux 10

Test results:

```
[javatest.batch] Number of Tests Passed      = 542
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0
[javatest.batch] ********************************************************************************

BUILD SUCCESSFUL
Total time: 16 minutes 40 seconds
-----
SHA256_API=f244245440772c1502b09d6511c0e15e53d42f21a3a9dccdf96a955a758a92bd
SHA256_IMPL=b867388bea3b24ac3651b3d095d79a54e752546477693600f8776624415ed5e4
SHA256_TCK=996c9abc6561134cea986107da0648c2a33277d2f765475994ff9b16da70cb5a
-----
API_download=https://jakarta.oss.sonatype.org/content/repositories/staging/jakarta/servlet/jsp/jstl/jakarta.servlet.jsp.jstl-api/3.0.0/jakarta.servlet.jsp.jstl-api-3.0.0.jar
IMPL_download=https://jakarta.oss.sonatype.org/content/repositories/staging/org/glassfish/web/jakarta.servlet.jsp.jstl/3.0.0/jakarta.servlet.jsp.jstl-3.0.0.jar
Product_download=https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/7.0.0-M4/glassfish-7.0.0-M4.zip
TCK_download=https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee10/staged/eftl/jakarta-tags-tck-3.0.0.zip
-----
OS2=Debian GNU/Linux 10
OS3=10.12
OS4=PRETTY_NAME="Debian GNU/Linux 10 (buster)" NAME="Debian GNU/Linux" VERSION_ID="10" VERSION="10 (buster)" VERSION_CODENAME=buster ID=debian HOME_URL="https://www.debian.org/" SUPPORT_URL="https://www.debian.org/support" BUG_REPORT_URL="https://bugs.debian.org/"
JDK_VERSION=openjdk version "11.0.2" 2019-01-15 OpenJDK Runtime Environment 18.9 (build 11.0.2+9) OpenJDK 64-Bit Server VM 18.9 (build 11.0.2+9, mixed mode)
JDK_VERSION=openjdk version "17.0.1" 2021-10-19 OpenJDK Runtime Environment (build 17.0.1+12-39)  OpenJDK 64-Bit Server VM (build 17.0.1+12-39,  mixed mode, sharing)
JDK_VERSION=openjdk version "18-ea"  2022-03-22 OpenJDK Runtime Environment (build 18-ea+27-1924) OpenJDK 64-Bit Server VM (build 18-ea+27-1924, mixed mode, sharing)
```