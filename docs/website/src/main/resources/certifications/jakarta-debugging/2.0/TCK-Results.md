TCK Results
===========

As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta EE Debugging Support for Other Languages.

# Eclipse GlassFish 6.0 Certification Request for Jakarta EE Debugging Support

- Product Name, Version and download URL (if applicable): <br/>
  [Eclipse GlassFish 6.0](https://eclipse-ee4j.github.io/glassfish/download)
  This specification does not define an API. The TCK verifies required behavior.
- Specification Name, Version and download URL: <br/>
  [Jakarta EE Debugging Support for Other Languages 2.0](https://jakarta.ee/specifications/debugging/2.0/)
- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta EE Debugging Support for Other Languages TCK 2.0.0](https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee9-eftl/promoted/jakarta-debugging-tck-2.0.0.zip), SHA-256: `71999815418799837dc6f3d0dc40c3dcc4144cd90c7cdfd06aa69270483d78bc`
- Public URL of TCK Results Summary: <br/>
  [TCK results summary](TCK-Results.html)
- Any Additional Specification Certification Requirements: <br/>
  None
- Java runtime used to run the implementation: <br/>
  Oracle JDK 1.8.0_191
- Summary of the information for the certification environment, operating system, cloud, ...: <br/>
  Linux Centos 7

Test results:

```
+ /opt/jdk1.8.0_191/bin/java VerifySMAP /home/jenkins/agent/workspace/t-for-other-languages-tck_master/vi/glassfish6/glassfish/domains/domain1/generated/jsp/testclient/org/apache/jsp/Hello_jsp.class.smap
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
