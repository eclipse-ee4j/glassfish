TCK Results
===========


As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta EE Platform 10.0, certification summary.

# Jakarta EE Platform 10.0, Eclipse GlassFish 7.0, TCK Certification Summary

- Product Name, Version and download URL (if applicable): <br/>
  [Eclipse GlassFish Downloads](/glassfish/download)<br/>
  [glassfish-7.0.zip](https://download.eclipse.org/ee4j/glassfish/glassfish-7.0.0.zip)

- Specification Name, Version and download URL: <br/>
  [Jakarta EE Platform, 10.0](https://jakarta.ee/specifications/platform/10/)

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta EE Platform TCK 10.0](https://download.eclipse.org/jakartaee/platform/10/jakarta-jakartaeetck-10.0.0.zip), 
  SHA-256: `930c1b5037cd60fb1f59c6473ad49170337fb3423c1d56bf34f9dcc1907ed06f`

- Public URL of TCK Results Summary: <br/>
  [TCK results summary](./TCK-Results-7.0)
  
- Any Additional Specification Certification Requirements: <br/>
  Jakarta Concurrency 3.0 TCK <br/>
  Jakarta JSON Processing 2.1 TCK <br/>
  Jakarta JSON Binding 3.0 TCK <br/>
  Jakarta RESTful Web Services 3.1 TCK <br/>
  Jakarta Security 3.0 TCK <br/>
  Jakarta Authentication 3.0 TCK <br/>
  Jakarta Faces 4.0 TCK <br/>
  Jakarta Contexts and Dependency Injection 4.0 TCK <br/>
  Jakarta Batch 2.1 TCK <br/>
  Jakarta Activation 2.1 TCK <br/>
  Jakarta Mail 2.1 TCK <br/>
  Jakarta XML Binding 4.0 TCK <br/>
  Jakarta Bean Validation 3.0 TCK <br/>
  Jakarta Dependency Injection 2.0 TCK <br/>
  Jakarta Debugging Support for Other Languages 2.0 TCK <br/>

- Java 1 runtime used to run the implementation: <br/>
```
openjdk version "11.0.2" 2019-01-15
OpenJDK Runtime Environment 18.9 (build 11.0.2+9)
OpenJDK 64-Bit Server VM 18.9 (build 11.0.2+9, mixed mode)
```

- Java 2 runtime used to run the implementation: <br/>
```
openjdk version "17.0.1" 2021-10-19
OpenJDK Runtime Environment (build 17.0.1+12-39)
OpenJDK 64-Bit Server VM (build 17.0.1+12-39, mixed mode, sharing)
```

- Summary of the information for the certification environment, operating system, cloud, ...: <br/>
  Apache Derby, <br/>
  Debian GNU/Linux 10 (buster)


Test results:

```
Stage Name: appclient
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 50 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 50
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: assembly
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 30 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 30
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: connector
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 477 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 477
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1793 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1793
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/assembly
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 51 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 51
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/bb
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1193 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1193
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/appexception
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 365 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 365
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/async
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 300 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 300
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/basic
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 105 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 105
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/ejbcontext
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 50 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 50
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/enventry
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 30 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 30
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/interceptor
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 175 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 175
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/lookup
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 30 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 30
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/naming
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 54 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 54
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/nointerface
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 60 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 60
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/packaging
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 211 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 211
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/singleton
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 230 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 230
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/stateful
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 129 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 129
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/tx
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 358 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 358
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/view
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 95 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 95
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/lite/xmloverride
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 30 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 30
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/misc
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 100 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 100
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/sec
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 99 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 99
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/timer
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 178 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 178
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/webservice
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 3 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 3
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb30/zombie
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb32
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 825 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 825
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: el
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 695 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 695
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: integration
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 18 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 18
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jacc
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 40 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 40
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: javaee
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 24 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 24
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: javamail
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 112 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 112
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jaxrs
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 138 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 138
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jdbc_appclient
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1231 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1231
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jdbc_ejb
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1231 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1231
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jdbc_jsp
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1231 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1231
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jdbc_servlet
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1231 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1231
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jms
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 3510 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 3510
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jpa_appmanaged
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1749 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1749
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jpa_appmanagedNoTx
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1889 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1889
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jpa_pmservlet
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1897 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1897
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jpa_puservlet
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1887 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1887
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jpa_stateful3
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1749 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1749
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jpa_stateless3
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1899 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1899
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jsp
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 735 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 735
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jstl
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 541 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 541
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jta
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 141 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 141
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: samples
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 12 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 12
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: servlet
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1739 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1739
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: signaturetest/javaee
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 4 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 4
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: webservices12
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 242 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 242
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: webservices13
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 53 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 53
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: websocket
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 748 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 748
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: xa
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 66 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 66
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************
```

Jakarta Concurrency 3.0 TCK

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Concurrency 3.0.0 TCK](https://download.eclipse.org/jakartaee/concurrency/3.0/concurrency-tck-3.0.0.zip), <br/>
  SHA-256: `2fe905b8adfab903a6c5954453e67fbc3ed583b0e313edef7f09d18b7eddd0ee`

TCK Result Summary:


===============================================
jakarta-concurrency
Total tests run: 149, Passes: 149, Failures: 0, Skips: 0
===============================================


Jakarta JSON Processing 2.1 TCK

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta JSON Processing 2.1.0 TCK](https://download.eclipse.org/jakartaee/jsonp/2.1/jakarta-jsonp-tck-2.1.0.zip), <br/>
  SHA-256: `6ee953382ff965627fe20dd7e3bfce6c968ed829d611cf4990988ab54bfe8b54`

```
[INFO] Results:
[INFO] 
[INFO] Tests run: 179, Failures: 0, Errors: 0, Skipped: 0

[INFO] Results:
[INFO] 
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
```


Jakarta JSON Binding 3.0 TCK

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta JSON Binding 3.0 TCK](https://download.eclipse.org/jakartaee/jsonb/3.0/jakarta-jsonb-tck-3.0.0.zip), <br/>
  SHA-256: `954fd9a3a67059ddeabe5f51462a6a3b542c94fc798094dd8c312a6a28ef2d0b`

```
[INFO] Results:
[INFO] 
[WARNING] Tests run: 295, Failures: 0, Errors: 0, Skipped: 5
```


Jakarta RESTful Web Services 3.1 TCK

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta RESTful Web Services 3.1.0 TCK](https://download.eclipse.org/jakartaee/restful-ws/3.1/jakarta-restful-ws-tck-3.1.0.zip), <br/>
  SHA-256: `ea8cd6cc857af55f19468bbb09e8a30f41c60e8f7413a093e7007c3902a49070`
 
```
[INFO] Results:
[INFO] 
[WARNING] Tests run: 2789, Failures: 0, Errors: 0, Skipped: 59

[INFO] Results:
[INFO] 
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```


Jakarta Security 3.0 TCK <br/>

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Security 3.0.0 TCK](https://download.eclipse.org/jakartaee/security/3.0/jakarta-security-tck-3.0.0.zip), <br/>
  SHA-256: `696776046dfeaed74266a5d1c4dac7fea5437b6f51743b7fe10962dde755ff8f`


```
********************************************************************************
Completed running 117 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
[INFO]      [exec] [javatest.batch] Number of Tests Passed      = 84
[INFO]      [exec] [javatest.batch] Number of Tests Failed      = 0
[INFO]      [exec] [javatest.batch] Number of Tests with Errors = 0
```

Jakarta Authentication 3.0 TCK <br/>

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Authentication 3.0.1 TCK](https://download.eclipse.org/jakartaee/authentication/3.0/jakarta-authentication-tck-3.0.1.zip), <br/>
  SHA-256: `8b916f1b4aed828337bd88b34bb39b133f04611c2dfe71541c2ec5d2dd22cd54`

```
********************************************************************************
Completed running 70 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
     [exec] [javatest.batch] Number of Tests Passed      = 68
     [exec] [javatest.batch] Number of Tests Failed      = 0
     [exec] [javatest.batch] Number of Tests with Errors = 0
```


Jakarta Faces 4.0 TCK <br/>

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Faces 4.0.1 TCK](https://download.eclipse.org/jakartaee/faces/4.0/jakarta-faces-tck-4.0.1.zip), <br/>
  SHA-256: `117fdbf8aee14ee162cc913ae055621f7e067b0be4dd14c4591be76b90a0dde5`

```
********************************************************************************
Completed running 261 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
[INFO]      [exec] [javatest.batch] Number of Tests Passed      = 5399
[INFO]      [exec] [javatest.batch] Number of Tests Failed      = 0
[INFO]      [exec] [javatest.batch] Number of Tests with Errors = 0
```

Jakarta Contexts and Dependency Injection 4.0 TCK <br/>

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Contexts and Dependency Injection 4.0.5 TCK](https://download.eclipse.org/jakartaee/cdi/4.0/cdi-tck-4.0.5-dist.zip), <br/>
  SHA-256: `56ce1046738f79d9bc19271bfb3fb57c667dc7b84122cfeff287f554b34b1377`

```  
===============================================
CDI TCK
Total tests run: 1831, Passes: 1831, Failures: 0, Skips: 0
===============================================
```

Jakarta Batch 2.1 TCK <br/>

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Batch 2.1.1 TCK](https://download.eclipse.org/jakartaee/batch/2.1/jakarta.batch.official.tck-2.1.1.zip), <br/>
  SHA-256: `0dd8ca0f35cc696ea86d0dffaa1301cf2786806832ea1b2a491d528eaa57b3b7`
  
```
[INFO] 
        Jakarta Batch TCK completed running 380 tests.
        Number of Tests Passed      = 380
        Number of Tests with Errors = 0
        Number of Tests Failed      = 0
        Number of Tests Skipped     = 12
[INFO] 
```

Jakarta Activation 2.1 TCK <br/>

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Activation 2.1 TCK](https://download.eclipse.org/jakartaee/activation/2.1/jakarta-activation-tck-2.1.0.zip), <br/>
  SHA-256: `6c4aad27e45761dd9f3e0f8506f37edea41f42401465db750689145718b27a0b`

```
[javatest.batch] ***************************************************************
[javatest.batch] Completed running 91 tests.
[javatest.batch] Number of Tests Passed      = 91
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0
[javatest.batch] Number of Tests Not Run     = 0
[javatest.batch] ---------------------------------------------------------------
[javatest.batch] ***************************************************************
[javatest.batch] Completed running 2 tests.
[javatest.batch] Number of Tests Passed      = 2
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0
[javatest.batch] Number of Tests Not Run     = 0
[javatest.batch] ---------------------------------------------------------------
```

Jakarta Mail 2.1 TCK <br/>

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Mail 2.1.0 TCK](https://download.eclipse.org/jakartaee/mail/2.1/jakarta-mail-tck-2.1.0.zip), <br/>
  SHA-256: `6f02a92e0a5ef60260e65f95938cc566da2f93a3d269c3b321da0d787a3448a5`
  
```
[javatest.batch] ***************************************************************
[javatest.batch] Completed running 321 tests.
[javatest.batch] Number of Tests Passed      = 321
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0
[javatest.batch] Number of Tests Not Run     = 0
[javatest.batch] ---------------------------------------------------------------
[javatest.batch] ***************************************************************
[javatest.batch] Completed running 1 tests.
[javatest.batch] Number of Tests Passed      = 1
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0
[javatest.batch] Number of Tests Not Run     = 0
[javatest.batch] ---------------------------------------------------------------
```

Jakarta XML Binding 4.0 TCK <br/>

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta XML Binding 4.0.0 TCK](https://download.eclipse.org/jakartaee/xml-binding/4.0/jakarta-xml-binding-tck-4.0.0.zip), <br/>
  SHA-256: `33fa6a39e6ac6b767316efc2f71fed3577c3d364dd1f532d410915c30a0b5b67`
  
```
| Keyword                                           | Passed    | Total     |
| ------------------------------------------------- | --------- | --------- |
| bindinfo document positive                        | 75        | 75        |
| bindinfo empty_output positive schema             | 2         | 2         |
| bindinfo negative schema                          | 11        | 11        |
| bindinfo positive schema                          | 48        | 48        |
| cttest positive runtime                           | 1         | 1         |
| document positive                                 | 5070      | 5070      |
| document positive runtime                         | 195       | 195       |
| document positive validation_checker              | 5613      | 5613      |
| empty_output java_to_schema jaxb positive runtime | 2         | 2         |
| empty_output jaxb positive rtgen runtime          | 2         | 2         |
| empty_output positive schema                      | 25        | 25        |
| java_to_schema jaxb negative runtime              | 22        | 22        |
| java_to_schema jaxb positive runtime              | 309       | 309       |
| jaxb positive rtgen runtime                       | 308       | 308       |
| jaxb positive runtime                             | 1         | 1         |
| jaxb rtgen runtime                                | 22        | 22        |
| negative schema                                   | 2678      | 2678      |
| positive runtime                                  | 16        | 16        |
| positive schema                                   | 10224     | 10224     |
| runtime                                           | 4         | 4         |
| **Total**                                         | **24628** | **24628** |
```


Jakarta Bean Validation 3.0 TCK <br/>

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Bean Validation 3.0.1 TCK](https://download.eclipse.org/jakartaee/bean-validation/3.0/beanvalidation-tck-dist-3.0.1.zip), <br/>
  SHA-256: `9da36d2d6e2eb8d413f886f15711820008419d210ce4c51af04f96e1ffd583b3`
  
```
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 1045, Failures: 0, Errors: 0, Skipped: 0
```

Jakarta Dependency Injection 2.0 TCK <br/>

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Dependency Injection 2.0.2 TCK](https://download.eclipse.org/jakartaee/dependency-injection/2.0/jakarta.inject-tck-2.0.2-bin.zip), <br/>
  SHA-256: `23bce4317ca061c3de648566cdf65c74b57e1264d6891f366567955d6b834972`

```  
Package
Duration
Fail
(diff)
Skip
(diff)
Pass
(diff)
Total
(diff)
org.atinject.tck.auto   6 ms    0       0       50      50  
```

Jakarta Debugging Support for Other Languages 2.0 TCK <br/>

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Debugging Support for Other Languages 2.0 TCK](https://download.eclipse.org/jakartaee/debugging/2.0/jakarta-debugging-tck-2.0.0.zip), <br/>
  SHA-256: `71999815418799837dc6f3d0dc40c3dcc4144cd90c7cdfd06aa69270483d78bc`
  
```
Package
Duration
Fail
(diff)
Skip
(diff)
Pass
(diff)
Total
(diff)
(root)  0 ms    0       0       1       1  
``` 

