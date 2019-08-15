TCK Results
===========

As required by the [Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php), the following is a summary of the TCK results for Glassfish 5.1.0 against Jakarta Transactions 1.3.

# Eclipse GlassFish 5.1 Certification Summary


- Organization Name ("Organization") and, if applicable, URL <br/>
  Eclipse Foundation
- Product Name, Version and download URL (if applicable) <br/>
  Eclipse Glassfish 5.1.0 https://www.eclipse.org/downloads/download.php?file=/glassfish/glassfish-5.1.0.zip
- Specification Name, Version and download URL <br/>
  Jakarta Transactions 1.3 https://github.com/jakartaee/specifications/pull/25
- TCK Version, digital SHA-256 fingerprint and download URL <br/>
  eclipse-transactions-tck-1.3.0 76b80f10347804a38d5f12ea09a74356f8ea26298e97c8a525d7a9e3379f1fc5 http://download.eclipse.org/ee4j/jakartaee-tck/jakartaee8-eftl/promoted/eclipse-transactions-tck-1.3.0.zip
- Public URL of TCK Results Summary <br/>
  https://eclipse-ee4j.github.io/glassfish/certifications/jakarta-transactions/1.3/TCK-Results
- Any Additional Specification Certification Requirements <br/>
  N/A
- Java runtime used to run the implementation <br/>
```
> java -version
java version "1.8.0_202"
Java(TM) SE Runtime Environment (build 1.8.0_202-b08)
Java HotSpot(TM) 64-Bit Server VM (build 25.202-b08, mixed mode)
```
- Summary of the information for the certification environment, operating system, cloud, ... <br/>
```
> uname -a
Linux default-java-mbwkj 3.10.0-957.21.3.el7.x86_64 #1 SMP Fri Jun 14 02:54:29 EDT 2019 x86_64 Linux
> cat /etc/os-release
NAME="Alpine Linux"
ID=alpine
VERSION_ID=3.8.1
PRETTY_NAME="Alpine Linux v3.8"
HOME_URL="http://alpinelinux.org"
BUG_REPORT_URL="http://bugs.alpinelinux.org"
```
- [x] By checking this box I acknowledge that the Organization I represent accepts the terms of the [EFTL](https://www.eclipse.org/legal/tck.php).
- [x] By checking this box I attest that all TCK requirements have been met, including any compatibility rules.

Test results:

```
[javatest.batch] Number of Tests Passed      = 72
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0
```
