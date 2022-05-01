TCK Results
===========

As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta Authentication.

# Eclipse GlassFish 7.0 Certification Summary

- Product Name, Version and download URL (if applicable): <br/>
  [Eclipse Glassfish 7.0.0-M4](https://github.com/eclipse-ee4j/glassfish/releases/download/7.0.0-M4/glassfish-7.0.0-M4.zip)
- Specification Name, Version and download URL: <br/>
  [Jakarta Authentication 3.0](https://jakarta.ee/specifications/authentication/3.0/)
- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Authentication TCK, 3.0.0](https://download.eclipse.org/es/jakartaee10/staged/eftl/jakarta-authentication-tck-3.0.0-dist.zip), SHA-256: `f970fce5dd3883934ef4f1162c747c527f98cf88fcacc8d7af6ae4cf444c9ef8`
- Public URL of TCK Results Summary: <br/>
  [TCK results summary](TCK-Results.html)
- Any Additional Specification Certification Requirements: <br/>
  None
- Java runtime used to run the implementation: <br/>
  OpenJDK 11.0.2, 17.0.1
- Summary of the information for the certification environment, operating system, cloud, ...: <br/>
  Debian GNU/Linux 10


Test results:

```
********************************************************************************
Completed running 70 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************
     [exec] [javatest.batch] Number of Tests Passed      = 107
     [exec] [javatest.batch] Number of Tests Failed      = 0
     [exec] [javatest.batch] Number of Tests with Errors = 0
     [exec] [javatest.batch] ********************************************************************************
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/authstatus/authexception/Client.java#AuthStatusAuthException
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/authstatus/failure/Client.java#AuthStatusFailure
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/authstatus/sendfailure/Client.java#AuthStatusSendFailure
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/baseline/Client.java#ACF_getFactory_from_jaspicservlet
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/baseline/Client.java#ACFInMemoryRegisterOnlyOneACP_from_jaspicservlet
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/baseline/Client.java#ACFPersistentRegisterOnlyOneACP_from_jaspicservlet
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/baseline/Client.java#ACFRemoveRegistrationWithBadId_from_jaspicservlet
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/baseline/Client.java#ACFSwitchFactorys_from_jaspicservlet
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/baseline/Client.java#ACFUnregisterACP_from_jaspicservlet
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/baseline/Client.java#testACFComesFromSecFile_from_jaspicservlet
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#ACFDetachListener
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#ACFGetFactory
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#ACFGetRegistrationContext
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#ACFGetRegistrationIDs
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#ACFInMemoryNotifyOnUnReg
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#ACFInMemoryPrecedenceRules
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#ACFInMemoryRegisterOnlyOneACP
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#ACFPersistentNotifyOnUnReg
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#ACFPersistentPrecedenceRules
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#ACFPersistentRegisterOnlyOneACP
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#ACFRemoveRegistration
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#ACFRemoveRegistrationWithBadId
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#ACFSwitchFactorys
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#ACFUnregisterACP
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#AuthConfigFactoryGetFactory
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#AuthConfigFactoryRegistration
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#AuthConfigFactorySetFactory
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#AuthConfigFactoryVerifyPersistence
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckACFVerifyPersistence
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckACPConfigObjAppContext
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckACPContextObjAppContext
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckAuthContextId
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckAuthContextIdUsingGet
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckAuthenInValidateRequest
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckCallbackSupport
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckforNonNullAuthContext
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckforNonNullCallback
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckMessageInfo
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckMPRCallsGetAuthContext
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckMsgInfoKey
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckNoAuthReturnsValidStatusCode
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckRegistrationContextId
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#checkSACValidateRequestWithVaryingAccess
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckSecureRespForMandatoryAuth
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckSecureRespForOptionalAuth
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckServletAppContext
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckValidateReqAlwaysCalled
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#CheckValidateReqAuthException
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#GetConfigProvider
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#getRegistrationContextId
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#testACFComesFromSecFile
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#testAuthenAfterLogout
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#testAuthenIsUserInRole
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#testAuthenResultsOnHttpServlet
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#testGPCGetAuthType
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#testGPCGetRemoteUser
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#testGPCGetUserPrincipal
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#testGPCIsUserInRole
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#testGPCWithNoRequiredAuth
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#testRemoteUserCorrespondsToPrin
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#testRequestWrapper
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#testResponseWrapper
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#testSecRespCalledAfterSvcInvoc
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#VerifyClientSubjects
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#VerifyMessageInfoObjects
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#VerifyNoInvalidEntries
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#VerifyReqPolicy
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#VerifyRequestDispatchedProperly
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#verifyRuntimeCallOrder
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#VerifySAContextVerifyReqIsCalled
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/servlet/Client.java#VerifyServiceSubjects
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ACFGetFactory
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ACFInMemoryRegisterOnlyOneACP
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ACFPersistentRegisterOnlyOneACP
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ACFRemoveRegistrationWithBadId
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ACFSwitchFactorys
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ACFUnregisterACP
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ACPAuthContext
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ACPClientAuthConfig
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ACPServerAuthConfig
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#AuthConfigFactoryRegistration
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#AuthConfigFactorySetFactory
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#AuthConfigFactoryVerifyPersistence
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#CACRequestResponse
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ClientAppContextId
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ClientAuthConfig
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ClientAuthContext
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ClientRuntimeCommonCallbackSupport
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ClientRuntimeMessageInfoMap
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#GetConfigProvider
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#GetFactory
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#getRegistrationContextId
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#MessageInfo
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#NameAndPasswordCallbackSupport
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#OperationId
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#SACRequestResponse
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#SecureRequest
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#SecureResponse
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ServerAppContextId
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ServerAuthConfig
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ServerAuthContext
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ServerRuntimeCallbackSupport
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ServerRuntimeCommonCallbackSupport
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#testACFComesFromSecFile
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ValidateRequest
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/jaspic/spi/soap/Client.java#ValidateResponse
     [exec] [javatest.batch] PASSED........com/sun/ts/tests/signaturetest/jaspic/JaspicSigTest.java#signatureTest_from_standalone
     [exec] [javatest.batch] 
     [exec] [javatest.batch] Apr 30, 2022, 11:20:36 PM Finished executing all tests, wait for cleanup...
     [exec] [javatest.batch] Apr 30, 2022, 11:20:36 PM Harness done with cleanup from test run.
     [exec] [javatest.batch] Total time = 660s
     [exec] [javatest.batch] Setup time = 0s
     [exec] [javatest.batch] Cleanup time = 19s
     [exec] [javatest.batch] Test results: passed: 107
     [exec] [javatest.batch] Results written to /home/jenkins/agent/workspace/2_authentication_run_tck_against_staged_build/authentication-tck-3.0.0/tck/old-tck-runner/target/authentication-tck/authenticationtckwork/authenticationtck.
     [exec] [javatest.batch] Report written to /home/jenkins/agent/workspace/2_authentication_run_tck_against_staged_build/authentication-tck-3.0.0/tck/old-tck-runner/target/authentication-tck/authenticationtckreport/authenticationtck
     [exec] 
     [exec] BUILD SUCCESSFUL
     [exec] Total time: 11 minutes 12 seconds
     [exec] Picked up JAVA_TOOL_OPTIONS: -XX:+IgnoreUnrecognizedVMOptions -XX:+UnlockExperimentalVMOptions 
     [exec] Waiting for the domain to stop .
     [exec] Command stop-domain executed successfully.
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] Jakarta Authentication TCK - common 1.0-SNAPSHOT ... SUCCESS [ 17.185 s]
[INFO] Jakarta Authentication TCK - main 3.0.0 ............ SUCCESS [ 46.206 s]
[INFO] Jakarta Authentication TCK - basic-authentication 3.0.0 SUCCESS [ 42.773 s]
[INFO] Jakarta Authentication TCK - custom principal 3.0.0  SUCCESS [ 27.391 s]
[INFO] Jakarta Authentication TCK - programmatic-authentication 3.0.0 SUCCESS [ 22.842 s]
[INFO] Jakarta Authentication TCK - lifecycle 3.0.0 ....... SUCCESS [ 24.575 s]
[INFO] Jakarta Authentication TCK - wrapping 3.0.0 ........ SUCCESS [ 22.435 s]
[INFO] Jakarta Authentication TCK - register-session 3.0.0  SUCCESS [ 24.765 s]
[INFO] Jakarta Authentication TCK - async-authentication 3.0.0 SUCCESS [ 25.854 s]
[INFO] Jakarta Authentication TCK - status-codes 3.0.0 .... SUCCESS [ 24.159 s]
[INFO] Jakarta Authentication TCK - dispatching 3.0.0 ..... SUCCESS [ 24.694 s]
[INFO] Jakarta Authentication TCK - dispatching JSF CDI 3.0.0 SUCCESS [ 32.218 s]
[INFO] Jakarta Authentication TCK - ejb-propagation 3.0.0 . SUCCESS [ 29.495 s]
[INFO] Jakarta Authentication TCK - ejb-register-session 3.0.0 SUCCESS [ 26.872 s]
[INFO] Jakarta Authentication TCK - authorization-propagation 3.0.0 SUCCESS [ 24.041 s]
[INFO] Jakarta Authentication TCK - invoke EJB and CDI 3.0.0 SUCCESS [ 31.775 s]
[INFO] Old Jakarta Authentication TCK - run 3.0.0 ......... SUCCESS [13:38 min]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  21:10 min
[INFO] Finished at: 2022-04-30T23:20:43Z
[INFO] ------------------------------------------------------------------------
SHA256_TCK=f970fce5dd3883934ef4f1162c747c527f98cf88fcacc8d7af6ae4cf444c9ef8
TCK_download=https://download.eclipse.org/es/jakartaee10/staged/eftl/jakarta-authentication-tck-3.0.0-dist.zip
OS2=Debian GNU/Linux 10
OS3=10.12
OS4=PRETTY_NAME="Debian GNU/Linux 10 (buster)" NAME="Debian GNU/Linux" VERSION_ID="10" VERSION="10 (buster)" VERSION_CODENAME=buster ID=debian HOME_URL="https://www.debian.org/" SUPPORT_URL="https://www.debian.org/support" BUG_REPORT_URL="https://bugs.debian.org/"
JDK_VERSION=openjdk version "11.0.2" 2019-01-15 OpenJDK Runtime Environment 18.9 (build 11.0.2+9) OpenJDK 64-Bit Server VM 18.9 (build 11.0.2+9, mixed mode)
JDK_VERSION=openjdk version "17.0.1" 2021-10-19 OpenJDK Runtime Environment (build 17.0.1+12-39)  OpenJDK 64-Bit Server VM (build 17.0.1+12-39, mixed mode, sharing)
```
