TCK Results
===========

As required by the [Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php), following is a summary of the TCK results for releases of Jakarta EE WebSocket 2.0.

# Jakarta EE WebSocket 2.0, Eclipse GlassFish 6.1 RC1, Certification Summary

- Product Name, Version and download URL (if applicable): <br/>
  [Eclipse GlassFish 6.1 RC1](https://download.eclipse.org/ee4j/glassfish/glassfish-6.1.0-RC1.zip)

- Specification Name, Version and download URL: <br/>
  [Jakarta EE WebSocket, 2.0](https://jakarta.ee/specifications/websocket/2.0)

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta WebSocket TCK, 2.0.1](https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee9-eftl/promoted/jakarta-websocket-tck-2.0.1.zip, 
  SHA-256: `08c9a6ecf509c6df255b67f6662a857a1c390407cd2fe998f9bf5d81c3e0da00`

- Public URL of TCK Results Summary: <br/>
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
  Linux Centos 7

Test results:

N.B. Two tests not run via exclude list included in 2.0 TCK, see https://github.com/eclipse-ee4j/websocket-api/issues/228

```
[javatest.batch] ********************************************************************************
[javatest.batch] Number of tests completed:  732 (732 passed, 0 failed, 0 with errors)
[javatest.batch] Number of tests remaining:  2
[javatest.batch] ********************************************************************************
[javatest.batch] Completed running 732 tests.
[javatest.batch] Number of Tests Passed      = 732
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0
[javatest.batch] ********************************************************************************
```
