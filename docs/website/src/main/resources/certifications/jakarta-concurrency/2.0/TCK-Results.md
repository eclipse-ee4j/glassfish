TCK Results
===========

As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta Concurrency.

# 1.1 Certification Request

- [x] Organization Name ("Organization") and, if applicable, URL: <br/>
  Eclipse Foundation
- [x] Product Name, Version and download URL (if applicable): <br/>
  [GlassFish 6.0.0-M1](http://download.eclipse.org/ee4j/glassfish/glassfish-6.0.0-M1.zip)
- [x] Specification Name, Version and download URL: <br/>
   [Jakarta Concurrency 2.0](https://jakarta.ee/specifications/concurrency/2.0/)
- [x] TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Concurrency TCK 2.0.0](https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee9-eftl/staged-900/jakarta-concurrency-tck-2.0.0.zip), SHA-256: 59662088bc0a7e66b0042015c013b1ded0cf920f5d43519914f0588390443e26
- [x] Public URL of TCK Results Summary: <br/>
  [TCK results summary](https://eclipse-ee4j.github.io/glassfish/certifications/jakarta-concurrency/2.0/TCK-Results)
- [x] Any Additional Specification Certification Requirements: <br/>
  None
- [x] Java runtime used to run the implementation: <br/>
  java version "1.8.0_202"
  Java(TM) SE Runtime Environment (build 1.8.0_202-b08)
  Java HotSpot(TM) 64-Bit Server VM (build 25.202-b08, mixed mode)
- [x] Summary of the information for the certification environment, operating system, cloud, ...: <br/>
  Linux basic-dltjh 3.10.0-1062.el7.x86_64 #1 SMP Thu Jul 18 20:25:13 UTC 2019 x86_64 GNU/Linux
- [x] By checking this box I acknowledge that the Organization I represent accepts the terms of the [EFTL](https://www.eclipse.org/legal/tck.php).
- [x] By checking this box I attest that all TCK requirements have been met, including any compatibility rules.

Test results:

```
[javatest.batch] Number of Tests Passed      = 72
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0
[javatest.batch] ********************************************************************************
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/AbortedException/Client.java#AbortedExceptionNoArgTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/AbortedException/Client.java#AbortedExceptionStringTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/AbortedException/Client.java#AbortedExceptionStringThrowableTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/AbortedException/Client.java#AbortedExceptionThrowableTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ContextService/Client.java#ContextServiceWithIntf_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ContextService/Client.java#ContextServiceWithIntfAndInstanceIsNull_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ContextService/Client.java#ContextServiceWithIntfAndIntfNoImplemented_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ContextService/Client.java#ContextServiceWithIntfAndProperties_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ContextService/Client.java#ContextServiceWithIntfAndPropertiesAndIntfNoImplemented_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ContextService/Client.java#ContextServiceWithIntfsAndPropertiesAndInstanceIsNull_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ContextService/Client.java#ContextServiceWithMultiIntfs_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ContextService/Client.java#ContextServiceWithMultiIntfsAndInstanceIsNull_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ContextService/Client.java#ContextServiceWithMultiIntfsAndIntfNoImplemented_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ContextService/Client.java#ContextServiceWithMultiIntfsAndProperties_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ContextService/Client.java#ContextServiceWithMultiIntfsAndPropertiesAndInstanceIsNull_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ContextService/Client.java#ContextServiceWithMultiIntfsAndPropertiesAndIntfNoImplemented_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ContextService/Client.java#GetExecutionProperties_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ContextService/Client.java#GetExecutionPropertiesNoProxy_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/LastExecution/Client.java#lastExecutionGetIdentityNameTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/LastExecution/Client.java#lastExecutionGetResultTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/LastExecution/Client.java#lastExecutionGetRunningTimeTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManageableThread/Client.java#isShutdown_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedExecutors/Client.java#IsCurrentThreadShutdown_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedExecutors/Client.java#IsCurrentThreadShutdown_ManageableThread_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedExecutors/Client.java#ManageCallableTaskWithMapAndNullArg_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedExecutors/Client.java#ManageCallableTaskWithNullArg_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedExecutors/Client.java#ManageCallableTaskWithTaskListener_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedExecutors/Client.java#ManageCallableTaskWithTaskListenerAndMap_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedExecutors/Client.java#ManageRunnableTaskWithMapAndNullArg_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedExecutors/Client.java#ManageRunnableTaskWithNullArg_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedExecutors/Client.java#ManageRunnableTaskWithTaskListener_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedExecutors/Client.java#ManageRunnableTaskWithTaskListenerAndMap_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedScheduledExecutorService/Client.java#normalScheduleProcess1Test_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedScheduledExecutorService/Client.java#normalScheduleProcess2Test_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedScheduledExecutorService/Client.java#nullCallableScheduleProcessTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedScheduledExecutorService/Client.java#nullCommandScheduleProcessTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedTask/Client.java#GetExecutionProperties_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedTask/Client.java#GetManagedTaskListener_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedTaskListener/Client.java#TaskAborted_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedTaskListener/Client.java#TaskDone_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedTaskListener/Client.java#TaskStarting_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/ManagedTaskListener/Client.java#TaskSubmitted_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/SkippedException/Client.java#SkippedExceptionNoArgTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/SkippedException/Client.java#SkippedExceptionStringTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/SkippedException/Client.java#SkippedExceptionStringThrowableTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/SkippedException/Client.java#SkippedExceptionThrowableTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/Trigger/Client.java#triggerGetNextRunTimeTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/api/Trigger/Client.java#triggerSkipRunTest_from_servlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ContextService/contextPropagate_servlet/Client.java#testClassloaderInServlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ContextService/contextPropagate_servlet/Client.java#testJNDIContextInServlet
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedExecutorService/inheritedapi/Client.java#testBasicManagedExecutorService
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedExecutorService/managed_servlet/forbiddenapi/Client.java#testAwaitTermination
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedExecutorService/managed_servlet/forbiddenapi/Client.java#testIsShutdown
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedExecutorService/managed_servlet/forbiddenapi/Client.java#testIsTerminated
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedExecutorService/managed_servlet/forbiddenapi/Client.java#testShutdown
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedExecutorService/managed_servlet/forbiddenapi/Client.java#testShutdownNow
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedScheduledExecutorService/inheritedapi_servlet/Client.java#testApiExecute
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedScheduledExecutorService/inheritedapi_servlet/Client.java#testApiInvokeAll
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedScheduledExecutorService/inheritedapi_servlet/Client.java#testApiInvokeAny
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedScheduledExecutorService/inheritedapi_servlet/Client.java#testApiSchedule
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedScheduledExecutorService/inheritedapi_servlet/Client.java#testApiScheduleAtFixedRate
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedScheduledExecutorService/inheritedapi_servlet/Client.java#testApiScheduleWithFixedDelay
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedScheduledExecutorService/inheritedapi_servlet/Client.java#testApiSubmit
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedScheduledExecutorService/managed/forbiddenapi_servlet/Client.java#testAwaitTermination
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedScheduledExecutorService/managed/forbiddenapi_servlet/Client.java#testIsShutdown
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedScheduledExecutorService/managed/forbiddenapi_servlet/Client.java#testIsTerminated
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedScheduledExecutorService/managed/forbiddenapi_servlet/Client.java#testShutdown
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedScheduledExecutorService/managed/forbiddenapi_servlet/Client.java#testShutdownNow
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedThreadFactory/apitests/Client.java#implementsManageableThreadInterfaceTest
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedThreadFactory/apitests/Client.java#interruptThreadApiTest
[javatest.batch] PASSED........com/sun/ts/tests/concurrency/spec/ManagedThreadFactory/context_servlet/Client.java#jndiClassloaderPropagationTest
[javatest.batch] PASSED........com/sun/ts/tests/signaturetest/concurrency/CONCURRENCYSigTest.java#signatureTest_from_standalone
[javatest.batch] 
[javatest.batch] Oct 7, 2020 8:37:40 PM Finished executing all tests, wait for cleanup...
[javatest.batch] Oct 7, 2020 8:37:40 PM Harness done with cleanup from test run.
[javatest.batch] Total time = 462s
[javatest.batch] Setup time = 0s
[javatest.batch] Cleanup time = 0s
[javatest.batch] Test results: passed: 72
[javatest.batch] Results written to /home/jenkins/agent/workspace/TCK_run_EE9/JTwork.
[javatest.batch] Report written to /home/jenkins/agent/workspace/TCK_run_EE9/JTreport

BUILD SUCCESSFUL
Total time: 7 minutes 43 seconds
```
