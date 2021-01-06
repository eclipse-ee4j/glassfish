TCK Results, Eclipse XML Binding 3.0
====================================

As required by the [Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php), following is a summary of the TCK results for release of Eclipse GlassFish 6.0.

## Eclipse XML Binding Implementation, Certification Summary

- Product Name, Version and download URL (if applicable): <br/>
  Eclipse GlassFish 6, provides XML Binding 3.0, included in Eclipse GlassFish 6.0.0-RC2 <br/>
  [Eclipse GlassFish Downloads](https://eclipse-ee4j.github.io/glassfish/download)

- Specification Name, Version and download URL: <br/>
  [Jakarta XML Binding 3.0](https://jakarta.ee/specifications/xml-binding/3.0)

- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta XML Binding 3.0, TCK](https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee9-eftl/promoted/jakarta-xml-binding-tck-3.0.0.zip), SHA-256: `4ee5f5c12edf5212c7fe16f0a51e74ae48c7b7a72ffc40450dfdb07204fb5c0b`

- Public URL of TCK Results Summary: <br/>
  [TCK results summary](TCK-Results.html)

- Any Additional Specification Certification Requirements: <br/>
  None

- Java runtime used to run the implementation: <br/>
  Oracle JDK 1.8.0_191

- Summary of the information for the certification environment, operating system, cloud, ...: <br/>
  Linux Centos 7

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