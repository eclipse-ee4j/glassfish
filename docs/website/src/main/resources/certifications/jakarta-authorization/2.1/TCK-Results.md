TCK Results
===========

As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta Authorization.

# Eclipse GlassFish 7.0 Certification Request

- Product Name, Version and download URL (if applicable): <br/>
  [Eclipse Glassfish 7.0.0-M4](https://github.com/eclipse-ee4j/glassfish/releases/download/7.0.0-M4/glassfish-7.0.0-M4.zip)
- Specification Name, Version and download URL: <br/>
  [Jakarta Authorization 2.1](https://jakarta.ee/specifications/authorization/2.1/)
- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Authorization TCK 2.1](https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee10/staged/eftl/jakarta-authorization-tck-2.1.0.zip), SHA-256: `fc35eb78bf6e2fe2df3b11f1ada02dea94d05a5505f0679241bab12cfa43d976`
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

[javatest.batch] Number of Tests Passed      = 34
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0
[javatest.batch] ********************************************************************************

BUILD SUCCESSFUL
Total time: 1 minute 29 seconds
SHA256_API=6bbd277672dc3c3133b70c489f530e798b4c6230ab8d5522170110744c1c8dd0
SHA256_TCK=fc35eb78bf6e2fe2df3b11f1ada02dea94d05a5505f0679241bab12cfa43d976
Product_download=https://github.com/eclipse-ee4j/glassfish/releases/download/7.0.0-M4/glassfish-7.0.0-M4.zip
API_download=https://jakarta.oss.sonatype.org/content/repositories/staging/jakarta/authorization/jakarta.authorization-api/2.1.0/jakarta.authorization-api-2.1.0.jar
TCK_download=https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee10/staged/eftl/jakarta-authorization-tck-2.1.0.zip
OS2=Debian GNU/Linux 10
OS3=10.12
OS4=PRETTY_NAME="Debian GNU/Linux 10 (buster)" NAME="Debian GNU/Linux" VERSION_ID="10" VERSION="10 (buster)" VERSION_CODENAME=buster ID=debian HOME_URL="https://www.debian.org/" SUPPORT_URL="https://www.debian.org/support" BUG_REPORT_URL="https://bugs.debian.org/"
JDK_VERSION=openjdk version "11.0.2" 2019-01-15 OpenJDK Runtime Environment 18.9 (build 11.0.2+9) OpenJDK 64-Bit Server VM 18.9 (build 11.0.2+9, mixed mode)
JDK_VERSION=openjdk version "17.0.1" 2021-10-19 OpenJDK Runtime Environment (build 17.0.1+12-39)  OpenJDK 64-Bit Server VM (build 17.0.1+12-39, mixed mode, sharing)
JDK_VERSION=openjdk version "18-ea"  2022-03-22 OpenJDK Runtime Environment (build 18-ea+27-1924) OpenJDK 64-Bit Server VM (build 18-ea+27-1924, mixed mode, sharing)
```
