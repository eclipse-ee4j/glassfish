TCK Results
===========

As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta EE Platform, Full Profile.

# Eclipse GlassFish 5.1 Full Profile Certification Summary

- Product Name, Version and download URL (if applicable): <br/>
*  [Eclipse GlassFish 5.1](https://www.eclipse.org/downloads/download.php?file=/glassfish/glassfish-5.1.0.zip)
- Specification Name, Version and download URL: <br/>
*  [Jakarta EE Platform, Full Profile 8.0](https://jakarta.ee/specifications/jakartaee-full-profile/8.0)
- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
*  [Jakarta EE Platform CTS 8.0](http://download.eclipse.org/ee4j/jakartaee-tck/jakartaee8-eftl/promoted/eclipse-jakartaeetck-8.0.0.zip), SHA-256: `847c80c9c80bea4682006f186292b68acdd0ce9b239d998856c3a379c3a7be50`
- Public URL of TCK Results Summary: <br/>
*  [TCK results summary](TCK-Results.html)
- Any Additional Specification Certification Requirements: <br/>
*  Jakarta CDI TCK; Jakarta Bean Validation TCK; Jakarta Dependency Injection TCK. TBD -- summary results, or links, here.
- Java runtime used to run the implementation: <br/>
**  Oracle JDK 1.8.0_191
- Summary of the information for the certification environment, operating system, cloud, ...: <br/>
**  Apache Derby
**  Linux, Centos 7.


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

Stage Name: compat12
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 12 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 12
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: compat13
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 15 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 15
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: concurrency
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 205 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 205
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: connector
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 495 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 495
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: ejb
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1809 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1809
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
   [runcts] OUT => [javatest.batch] Completed running 1200 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1200
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
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 10
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
   [runcts] OUT => [javatest.batch] Completed running 667 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 667
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

Stage Name: interop
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 820 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 820
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: j2eetools
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 134 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 134
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

Stage Name: jaspic
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 68 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 68
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

Stage Name: jaxr
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1372 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1372
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jaxrpc
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1478 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1478
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jaxrs
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 2803 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 2803
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jbatch
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 322 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 322
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
   [runcts] OUT => [javatest.batch] Completed running 1733 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1733
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jpa_appmanagedNoTx
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1873 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1873
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jpa_pmservlet
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1881 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1881
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jpa_puservlet
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1871 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1871
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jpa_stateful3
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1733 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1733
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jpa_stateless3
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1883 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1883
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jsf
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 5526 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 5526
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jsonb
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1082 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1082
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jsonp
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 744 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 744
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jsp
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 731 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 731
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
   [runcts] OUT => [javatest.batch] Completed running 195 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 195
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: rmiiiop
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 129 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 129
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: samples
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 13 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 13
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: securityapi
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 84 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 84
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: servlet
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1746 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1746
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: signaturetest/javaee
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 5 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 5
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: webservices
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 507 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 507
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
   [runcts] OUT => [javatest.batch] Completed running 745 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 745
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
