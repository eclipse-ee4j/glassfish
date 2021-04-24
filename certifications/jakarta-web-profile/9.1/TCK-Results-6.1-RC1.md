TCK Results
===========


As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta EE Platform, Web Profile and Eclipse GlassFish Web profile.

# Jakarta EE Platform Web Profile 9.1, Eclipse GlassFish Web Profile 6.1 RC1, Certification Summary

- Product Name, Version and download URL (if applicable): <br/>
  [Eclipse GlassFish Downloads](/glassfish/download)<br/>
  [web-6.1.0-RC1.zip](https://download.eclipse.org/ee4j/glassfish/web-6.1.0-RC1.zip)

- Specification Name, Version and download URL: <br/>
  [Jakarta EE Platform, Web Profile 9.1](https://jakarta.ee/specifications/webprofile/9.1/)

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta EE Platform TCK 9.1](https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee9-eftl/promoted/jakarta-jakartaeetck-9.1.0.zip), 
  SHA-256: `16904b7ccd7ae61287b763587e8bfbff50608ab09f3876bb41af65d043263ca7`

- Public URL of TCK Results Summary: <br/>
  [TCK results summary](TCK-Results-6.1-RC1)

- Any Additional Specification Certification Requirements: <br/>
  None

- Java runtime used to run the implementation: <br/>
```
java version "11.0.7" 2020-04-14 LTS
Java(TM) SE Runtime Environment 18.9 (build 11.0.7+8-LTS)
Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.7+8-LTS, mixed mode)
```

- Summary of the information for the certification environment, operating system, cloud, ...: <br/>
  Apache Derby, <br/>
  Linux, CentOS 7.


Test results:

```
Stage Name: connector
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 252 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 252
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
   [runcts] OUT => [javatest.batch] Completed running 203 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 203
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

Stage Name: ejb32
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 537 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 537
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

Stage Name: jacc
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 24 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 24
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jaspic
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 61 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 61
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: javamail
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 56 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 56
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jaxrs
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 974 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 974
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jdbc
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 2462 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 2462
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jpa
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 1896 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1896
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
   [runcts] OUT => [javatest.batch] Completed running 532 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 532
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jsonp
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 372 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 372
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: jsp
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 720 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 720
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
   [runcts] OUT => [javatest.batch] Completed running 154 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 154
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: samples
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 5 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 5
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
   [runcts] OUT => [javatest.batch] Completed running 1643 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 1640
   [runcts] OUT => [javatest.batch] Number of Tests Failed      = 0
   [runcts] OUT => [javatest.batch] Number of Tests with Errors = 0
   [runcts] OUT => [javatest.batch] ********************************************************************************

Stage Name: signaturetest/javaee
   [runcts] OUT => [javatest.batch] ********************************************************************************
   [runcts] OUT => [javatest.batch] Completed running 2 tests.
   [runcts] OUT => [javatest.batch] Number of Tests Passed      = 2
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

```

Additionally, Jakarta EE 9 Specification requires the following TCKs:

Jakarta Dependency Injection 2.0 TCK

Download URL & SHA-256

[jakarta.inject-tck-2.0.1-bin.zip](https://download.eclipse.org/jakartaee/dependency-injection/2.0/jakarta.inject-tck-2.0.1-bin.zip), <br/>
SHA-256: `7853d02d372838f8300f5a18cfcc23011c9eb9016cf3980bba9442e4b1f8bfc6`

TCK result summary:
```
    [junit] Testsuite: org.jboss.weld.atinject.tck.AtInjectTCK
    [junit] Tests run: 50, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.099 sec
```


Jakarta Contexts and Dependency Injection 3.0 TCK

Download URL & SHA-256

[cdi-tck-3.0.1-dist.zip](http://download.eclipse.org/jakartaee/cdi/3.0/cdi-tck-3.0.1-dist.zip), <br/>
SHA-256:  `f0a3bdd81ea552ddf2c2a6cd2576f0d5ca45026665cb4a5c42606a58bf1c133d`

TCK Result Summary:
```
[mvn.test] Tests run: 1794, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6,354.02 sec
 [mvn.test] 
 [mvn.test] Results :
 [mvn.test] 
 [mvn.test] Tests run: 1794, Failures: 0, Errors: 0, Skipped: 0
```


Jakarta Bean Validation 3.0 TCK

Download URL & SHA-256

[beanvalidation-tck-dist-3.0.0.zip](https://download.eclipse.org/jakartaee/bean-validation/3.0/beanvalidation-tck-dist-3.0.0.zip), <br/>
SHA-256: `c975fd229df0c40947a9f0a69b779ec92bebb3d21e05fdc65fccc1d11ef5525b`

TCK Result Summary:
```
 [mvn.test] Tests run: 1045, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 597.125 sec - in TestSuite
 [mvn.test] 
 [mvn.test] Results :
 [mvn.test] 
 [mvn.test] Tests run: 1045, Failures: 0, Errors: 0, Skipped: 0
```


Jakarta XML Binding 3.0.1 TCK

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta XML Binding 3.0.1, TCK](https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee9-eftl/promoted/jakarta-xml-binding-tck-3.0.1.zip), <br/>
  SHA-256: `a9356a2eb989e8cb7f663ed5fd244d8e2d222e2c4108c40c65001bec90f40baf`

TCK Result Summary:
Test results: (from home/jenkins/agent/workspace/jaxb-tck_master/JAXB_REPORT/JAXB-TCK/html/report.html)

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


Jakarta Debugging Support for Other Languages 1.0 TCK

Download URL & SHA-256

[jakarta-debugging-tck-2.0.0.zip](https://download.eclipse.org/jakartaee/debugging/2.0/jakarta-debugging-tck-2.0.0.zip), <br/>
SHA-256: `71999815418799837dc6f3d0dc40c3dcc4144cd90c7cdfd06aa69270483d78bc`

TCK Result Summary:
```
+ /opt/jdk-11.0.7/bin/java VerifySMAP /home/jenkins/agent/workspace/t-for-other-languages-tck_master/vi/glassfish6/glassfish/domains/domain1/generated/jsp/testclient/org/apache/jsp/Hello_jsp.class.smap
++ grep 'is a correctly formatted SMAP' smap.log
++ wc -l
+ output=1
+ echo 1
1
+ [[ 1 < 1 ]]
+ failures=0
+ status=Passed
+ echo '<testsuite id="1" name="debugging-tck" tests="1" failures="0" errors="0" disabled="0" skipped="0">'
+ echo '<testcase name="VerifySMAP" classname="VerifySMAP" time="0" status="Passed"><system-out></system-out></testcase>'
+ echo '</testsuite>'
+ echo ''
```
