TCK Results
===========


As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta EE Platform 11.0, certification summary.

# Jakarta EE Platform 11.0, Eclipse GlassFish 8.0.0-M10, TCK Certification Summary

- [X] Organization Name ("Organization") and, if applicable, URL:
  [Eclipse Foundation](https://eclipse.org) <br/><br/>
- [X] Product Name, Version and download URL (if applicable):
   Eclipse GlassFish Web Profile 
     - JDK 21 [web-8.0.0-M10.zip](https://download.eclipse.org/ee4j/glassfish/web-8.0.0-M10.zip) 
     - JDK 17 [web-8.0.0-JDK17-M10.zip](https://download.eclipse.org/ee4j/glassfish/web-8.0.0-JDK17-M10.zip) <br/><br/>
- [X] Specification Name, Version and download URL:
  [Jakarta EE Platform Web Profile 11](https://jakarta.ee/specifications/webprofile/11) <br/><br/>
- [X] TCK Version, digital SHA-256 fingerprint and download URL:
  [Jakarta EE Platform TCK 11.0](https://www.eclipse.org/downloads/download.php?file=/ee4j/jakartaee-tck/jakartaee11/staged/eftl/jakartaeetck-11.0.0-dist.zip)
  SHA-256: `b8ed5c1720cb10970cb179e86f7a0464ca8d354e180337d99561659fb51aa8af` <br/><br/> 
- [X] Public URL of TCK Results Summary:
  [Eclipse GlassFish Web Profile 8.0 M10 TCK Results](./TCK-Results-8.0.0-M10) <br/><br/>
- [X] Any Additional Specification Certification Requirements:

     - [Jakarta Authentication 3.1 TCK](https://www.eclipse.org/downloads/download.php?file=/jakartaee/authentication/3.1/jakarta-authentication-tck-3.1.1.zip)
     - [Jakarta Contexts and Dependency Injection 4.1 TCK](https://www.eclipse.org/downloads/download.php?file=/jakartaee/cdi/4.1/cdi-tck-4.1.0-dist.zip)

     - [Jakarta Concurrency 3.1 TCK](https://www.eclipse.org/downloads/download.php?file=/jakartaee/concurrency/3.1/concurrency-tck-3.1.1.zip)
     - [Jakarta Data 1.0 TCK](https://download.eclipse.org/jakartaee/data/1.0/data-tck-1.0.1.zip)
     - [Jakarta Debugging Support for Other Languages 2.0 TCK](https://download.eclipse.org/jakartaee/debugging/2.0/jakarta-debugging-tck-2.0.0.zip)
     - [Jakarta Dependency Injection 2.0 TCK](https://download.eclipse.org/jakartaee/dependency-injection/2.0/jakarta.inject-tck-2.0.2-bin.zip)
     - [Jakarta Faces 4.1 TCK](https://www.eclipse.org/downloads/download.php?file=/jakartaee/faces/4.1/jakarta-faces-tck-4.1.1.zip)
     - [Jakarta JSON Processing 2.1 TCK](https://download.eclipse.org/jakartaee/jsonp/2.1/jakarta-jsonp-tck-2.1.1.zip)
     - [Jakarta JSON Binding 3.0 TCK](https://download.eclipse.org/jakartaee/jsonb/3.0/jakarta-jsonb-tck-3.0.0.zip)
     - [Jakarta Pages 4.0 TCK](https://download.eclipse.org/jakartaee/pages/4.0/jakarta-pages-tck-4.0.0.zip)
     - [Jakarta REST 4.0 TCK](https://www.eclipse.org/downloads/download.php?file=/jakartaee/restful-ws/4.0/jakarta-restful-ws-tck-4.0.1.zip)
     - [Jakarta Security 4.0 TCK](https://www.eclipse.org/downloads/download.php?file=/jakartaee/security/4.0/jakarta-security-tck-4.0.0.zip)
     - [Jakarta Servlet 6.1 TCK](https://download.eclipse.org/jakartaee/servlet/6.1/jakarta-servlet-tck-6.1.1.zip)
     - [Jakarta Validation 3.1 TCK](https://www.eclipse.org/downloads/download.php?file=/jakartaee/bean-validation/3.1/validation-tck-dist-3.1.1.zip)
     - [Jakarta WebSocket 2.2 TCK](https://download.eclipse.org/jakartaee/websocket/2.2/jakarta-websocket-tck-2.2.0.zip)

<br/>

- [X] Java runtime used to run the implementation:<br/>
  - Java runtime 1 used to run the implementation: <br/>
   ```
   openjdk version "17.0.2" 2022-01-18 
   OpenJDK Runtime Environment (build 17.0.2+8-86) 
   OpenJDK 64-Bit Server VM (build 17.0.2+8-86, mixed mode, sharing)
   ```
  - Java runtime 2 used to run the implementation: <br/>
   ```
   openjdk version "21.0.2" 2024-01-16 
   OpenJDK Runtime Environment (build 21.0.2+13-58) 
   OpenJDK 64-Bit Server VM (build 21.0.2+13-58, mixed mode, sharing)
   ```
- [X] Summary of the information for the certification environment, operating system, cloud, ...:<br/>
  Apache Derby, <br/>
  Ubuntu 24.04.1 LTS<br/><br/>

Test results:

Jakarta Authentication 3.1

 - [Jakarta Authentication 3.1.1 TCK](https://www.eclipse.org/downloads/download.php?file=/jakartaee/authentication/3.1/jakarta-authentication-tck-3.1.1.zip), 
  SHA-256: `54647e8d648899383345966e01c5cc8af051b17a2c6d36566698c872d51fd5a7` <br/>  

```
********************************************************************************
Completed running 130 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
```

Job name: jakarta-cdi-extra-tck-glassfish

```
===============================================
CDI TCK
Total tests run: 1769, Passes: 1769, Failures: 0, Skips: 0
===============================================
```

Jakarta Contexts and Dependency Injection 4.1

 - [Jakarta Contexts and Dependency Injection 4.1.0 TCK](https://www.eclipse.org/downloads/download.php?file=/jakartaee/cdi/4.1/cdi-tck-4.1.0-dist.zip), 
  SHA-256: `446029ee1ce694d2a9ae8893d16be7afd7e1c0ed8705064b7095af174cf97ea0` <br/>

```
model
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

regular
===============================================
CDI TCK
Total tests run: 1334, Passes: 1334, Failures: 0, Skips: 0
===============================================

```

Jakarta Concurrency 3.1

  - [Jakarta Concurrency 3.1.1 TCK](https://www.eclipse.org/downloads/download.php?file=/jakartaee/concurrency/3.1/concurrency-tck-3.1.1.zip),
  SHA-256: `b064e189e23d755bb0eff14cc3908413776608894353254e599f0724988671fd` <br/>

```
Tests run: 197, Failures: 0, Errors: 0, Skipped: 14
```

Jakarta Data 1.0

 - [Jakarta Data 1.0.1 TCK](https://download.eclipse.org/jakartaee/data/1.0/data-tck-1.0.1.zip),
  SHA-256: `02dda1984b67f57bf6996942bea6b1234476e7d1a14bd5770f54f1264decaf35` <br/>

```
Tests run: 99, Failures: 0, Errors: 0, Skipped: 0
```


Jakarta Dependency Injection 2.0

  - [Jakarta Dependency Injection 2.0.2 TCK](https://download.eclipse.org/jakartaee/dependency-injection/2.0/jakarta.inject-tck-2.0.2-bin.zip), 
  SHA-256: `23bce4317ca061c3de648566cdf65c74b57e1264d6891f366567955d6b834972` <br/>

```
Tests run: 50, Failures: 0, Errors: 0, Skipped: 0
```



Job name: jakarta-ejb-30-lite-appexception-tck-glassfish

```
Tests run: 292, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-ejb-30-lite-env-tck-glassfish

```
Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-ejb-30-lite-rest-tck-glassfish

```
Tests run: 1177, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-ejb-30-lite-singleton-tck-glassfish

```
 Tests run: 166, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-ejb-30-lite-stateful-concurrency-tck-glassfish

```
Tests run: 61, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-ejb-30-timeout3-tck-glassfish

```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-ejb-30-timeout4-tck-glassfish

```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-ejb-32-lite-timer-basic-tck-glassfish

```
Tests run: 40, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-ejb-32-lite-timer-interceptor-tck-glassfish

```
Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-ejb-32-lite-timer-schedule-auto-tck-glassfish

```
Tests run: 36, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-ejb-32-lite-timer-schedule-descriptor-expression-lifecycle-tck-glassfish

```
Tests run: 184, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-ejb-32-lite-timer-schedule-tx-tck-glassfish

```
Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-ejb-32-lite-timer-schedule-txnonpersistent-tck-glassfish

```
Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-ejb-32-lite-timer-schedule-tz-service-timerconfig-tck-glassfish

```
Tests run: 64, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-expression-language-platform-tck-glassfish

```
********************************************************************************
Completed running 717 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
```


Jakarta Debugging Support for Other Languages 2.0 TCK

 - [Jakarta Debugging Support for Other Languages 2.0 TCK](https://download.eclipse.org/jakartaee/debugging/2.0/jakarta-debugging-tck-2.0.0.zip),
  SHA-256: `71999815418799837dc6f3d0dc40c3dcc4144cd90c7cdfd06aa69270483d78bc` <br/>

```
[INFO] TCK: Install Jakarta pages-debugging-other TCK ..... SUCCESS [  5.420 s]
[INFO] TCK: Run Jakarta pages-debugging-other TCK ......... SUCCESS [ 40.145 s]
[INFO] TCK: Jakarta pages-debugging-other TCK parent ...... SUCCESS [  0.002 s]
[INFO] ------------------------------------------------------------------------
```



 Jakarta Faces 4.1

  - [Jakarta Faces 4.1.1 TCK](https://www.eclipse.org/downloads/download.php?file=/jakartaee/faces/4.1/jakarta-faces-tck-4.1.1.zip), 
  SHA-256: `e2024aabb0c784d23c076165a4f16e03892cc68c06f67517eec5ba66b9fb17f0`<br/>

```
jakarta-faces-component-html-old-tck-glassfish
[INFO]      [exec] [javatest.batch] Number of Tests Passed      = 2449
[INFO]      [exec] [javatest.batch] Number of Tests Failed      = 0
[INFO]      [exec] [javatest.batch] Number of Tests with Errors = 0

jakarta-faces-component-non-html-old-tck-glassfish
[INFO]      [exec] [javatest.batch] Number of Tests Passed      = 1912
[INFO]      [exec] [javatest.batch] Number of Tests Failed      = 0
[INFO]      [exec] [javatest.batch] Number of Tests with Errors = 0

jakarta-faces-non-component-old-tck-glassfish
[INFO]      [exec] [javatest.batch] Number of Tests Passed      = 1030
[INFO]      [exec] [javatest.batch] Number of Tests Failed      = 0
[INFO]      [exec] [javatest.batch] Number of Tests with Errors = 0

jakarta-faces-new-tck-glassfish
********************************************************************************
Completed running 141 tests.
Number of Tests Failed      = 
Number of Tests with Errors = 
********************************************************************************

```

Job name: jakarta-jdbc-tck-glassfish

```
********************************************************************************
Completed running 2462 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
```

Job name: jakarta-jsonb-extra-tck-glassfish

```
********************************************************************************
Completed running 10 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
```

Jakarta JSON Binding 3.0 TCK

  - [Jakarta JSON Binding 3.0.0 TCK](https://download.eclipse.org/jakartaee/jsonb/3.0/jakarta-jsonb-tck-3.0.0.zip),
  SHA-256: `954fd9a3a67059ddeabe5f51462a6a3b542c94fc798094dd8c312a6a28ef2d0b` <br/>

```
Tests run: 295, Failures: 0, Errors: 0, Skipped: 5
```

Job name: jakarta-jsonp-extra-tck-glassfish

```
********************************************************************************
Completed running 38 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
```

Jakarta JSON Processing 2.1 TCK

  - [Jakarta JSON Processing 2.1.1 TCK](https://download.eclipse.org/jakartaee/jsonp/2.1/jakarta-jsonp-tck-2.1.1.zip), 
  SHA-256: `949f203de84deffa8c7892b555918e42f1dd220ccb7b6800741ea58af62737c1`<br/>
  
```
Regular
Tests run: 179, Failures: 0, Errors: 0, Skipped: 0

Plugability
Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-pages-extra-tck-glassfish

```
********************************************************************************
Completed running 18 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
```

Jakarta Pages 4.0 TCK
  - [Jakarta Pages 4.0.0 TCK](https://download.eclipse.org/jakartaee/pages/4.0/jakarta-pages-tck-4.0.0.zip),
  SHA-256: `5446aa866601a7c4c425f74054590cfc7f4fc05592a572d399ecc8694e265489` <br/>

```
********************************************************************************
Completed running 682 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
```

Job name: jakarta-persistence-platform-tck-glassfish

```
********************************************************************************
Completed running 3820 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
```

Job name: jakarta-rest-extra-tck-glassfish

```
********************************************************************************
Completed running 129 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
```

Jakarta REST 4.0 TCK
  - [Jakarta REST 4.0.1 TCK](https://www.eclipse.org/downloads/download.php?file=/jakartaee/restful-ws/4.0/jakarta-restful-ws-tck-4.0.1.zip), 
  SHA-256: `b6290c1b5b3d2fdd9cc700a999243492a7e27b94a9b6af1974ff4dc5bfbf98f2` <br/>
  
```
********************************************************************************
Completed running 2796 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
Number of Tests with Skipped = 128
********************************************************************************
```

Jakarta Security 4.0
  - [Jakarta Security 4.0.0 TCK](https://www.eclipse.org/downloads/download.php?file=/jakartaee/security/4.0/jakarta-security-tck-4.0.0.zip), 
  SHA-256: `e21a3d8251bf954c99a3f9a5f71911696509a1ed40c23673760785a3b88e4172`<br/>
  
```
New
Tests   Errors  Failures    
133 0   0   

Old
[INFO]      [exec] [javatest.batch] ********************************************************************************
[INFO]      [exec] [javatest.batch] Completed running 83 tests.
[INFO]      [exec] [javatest.batch] Number of Tests Passed      = 83
[INFO]      [exec] [javatest.batch] Number of Tests Failed      = 0
[INFO]      [exec] [javatest.batch] Number of Tests with Errors = 0
[INFO]      [exec] [javatest.batch] ********************************************************************************
```

Jakarta Servlet 6.1

  - [Jakarta Servlet 6.1.1 TCK](https://download.eclipse.org/jakartaee/servlet/6.1/jakarta-servlet-tck-6.1.1.zip),
  SHA-256: `ed876365cda298efa7f9005dff4e2e47a25db6db4734b17d74d404041a5da6da` <br/>
  
```
Tests run: 1716, Failures: 0, Errors: 0, Skipped: 7
```

Job name: jakarta-signature-tck-glassfish

```
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

Job name: jakarta-tags-tck-glassfish

```
********************************************************************************
Completed running 541 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
```

Job name: jakarta-transactions-tck-glassfish

```
********************************************************************************
Completed running 154 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
```

Jakarta Validation 3.1

  - [Jakarta Validation 3.1.1 TCK](https://www.eclipse.org/downloads/download.php?file=/jakartaee/bean-validation/3.1/validation-tck-dist-3.1.1.zip), 
  SHA-256: `bda9de47960845a7c73aa9a1616e4bbed3e346de3516f768819cf5af77bf969f` <br/>
  
```
********************************************************************************
Completed running 1049 tests.
Number of Tests Passed      = 1049
Number of Tests Failed = 0
********************************************************************************
```

Job name: jakarta-websocket-extra-tck-glassfish

```
********************************************************************************
Completed running 12 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
```

Jakarta WebSocket 2.2

  - [Jakarta WebSocket 2.2.0 TCK](https://download.eclipse.org/jakartaee/websocket/2.2/jakarta-websocket-tck-2.2.0.zip),
  SHA-256: `ff50825305bd1318a4e6bdfab21b829cfd8cf89fab8a970f007b52636d3f2629` <br/>
  
```
********************************************************************************
Completed running 737 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
```


