TCK Results
===========

As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta EE Enterprise Web Services 2.0.

# Jakarta EE Enterprise Web Services 2.0, Eclipse GlassFish 6.1-RC1, Certification Summary

- Product Name, Version and download URL (if applicable):<br/>
  [Eclipse GlassFish 6.1 RC1](https://download.eclipse.org/ee4j/glassfish/glassfish-6.1.0-RC1.zip)<br/>
  This specification does not define an API. Jakarta EE Platform TCK verifies required behavior.

- Specification Name, Version and download URL: <br/>
  [Jakarta Enterprise Web Services 2.0](https://jakarta.ee/specifications/enterprise-ws/2.0/)

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
    [Jakarta EE Platform TCK 9.1](http://download.eclipse.org/ee4j/jakartaee-tck/jakartaee9-eftl/promoted/jakarta-jakartaeetck-9.1.0.zip), 
	SHA-256: `2F634A52F0E994B31AAC7C2308854365417F042C956C2E6B05B088A12E9D8968
`
- Public URL of TCK Results Summary: <br/>
  There is no stand-alone TCK for this specification. See Jakarta EE Platform TCK results, below.
  [TCK results summary](./TCK-Results-6.1-RC1)

- Any Additional Specification Certification Requirements: <br/>
  None

- Java runtime used to run the implementation: <br/>
```
java version "11.0.7" 2020-04-14 LTS
Java(TM) SE Runtime Environment 18.9 (build 11.0.7+8-LTS)
Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.7+8-LTS, mixed mode)
```

- Summary of the information for the certification environment, operating system, cloud, ...: <br/>
  Linux Centos 7<br/>
  Apache Derby


Test results:
```
Test Suite Name: ejb30/webservice
********************************************************************************
Completed running 3 tests.
Number of Tests Passed      = 3
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************

Test Suite Name: webservices12
********************************************************************************
Completed running 242 tests.
Number of Tests Passed      = 242
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************

Test Suite Name: webservices13
********************************************************************************
Completed running 53 tests.
Number of Tests Passed      = 53
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
```