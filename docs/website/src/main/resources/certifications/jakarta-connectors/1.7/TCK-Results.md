TCK Results
===========

As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta Connectors.

# 1.7 Certification Request

- [x] Organization Name ("Organization") and, if applicable, URL\
  Eclipse Foundation
- [x] Product Name, Version and download URL (if applicable)\
  [GlassFish 5.1](https://www.eclipse.org/downloads/download.php?file=/glassfish/glassfish-5.1.0.zip)
- [x] Specification Name, Version and download URL\
   [Jakarta Connectors 1.7](https://jakarta.ee/specifications/concurrency/1.1/)
- [x] TCK Version, digital SHA-256 fingerprint and download URL\
  [Jakarta Connectors TCK 1.7.0](http://download.eclipse.org/ee4j/jakartaee-tck/jakartaee8-eftl/promoted/eclipse-connectors-tck-1.7.0.zip), SHA-256: 6d5fe8c0fd3d8388a6aecf5431e0c8c50391464cd4c3be94d376654f46df687c
- [x] Public URL of TCK Results Summary\
  [TCK results summary](https://eclipse-ee4j.github.io/glassfish/certifications/jakarta-connectors/1.7/TCK-Results)
- [x] Any Additional Specification Certification Requirements\
  None
- [x] Java runtime used to run the implementation\
  Oracle JDK 1.8.0_191
- [x] Summary of the information for the certification environment, operating system, cloud, ...\
  Linux
- [x] By checking this box I acknowledge that the Organization I represent accepts the terms of the [EFTL](https://www.eclipse.org/legal/tck.php).
- [x] By checking this box I attest that all TCK requirements have been met, including any compatibility rules.

Test results:

```
[javatest.batch] Number of Tests Passed      = 139
[javatest.batch] Number of Tests Failed      = 0
[javatest.batch] Number of Tests with Errors = 0
[javatest.batch] ********************************************************************************
[javatest.batch] PASSED........com/sun/ts/tests/connector/annotations/anno/annotationClient.java#testConfigPropertyAnnotation_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/annotations/anno/annotationClient.java#testConnectorAnnotation_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/annotations/anno/annotationClient.java#testRAAccessibility_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/annotations/anno/annotationClient.java#testSetterMethodConfigPropAnno_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/annotations/mdcomplete/Client.java#testMDCompleteConfigProp_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/annotations/mdcomplete/Client.java#testMDCompleteMCFAnno_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/annotations/partialanno/paClient.java#testDDOverridesAnno_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/annotations/partialanno/paClient.java#testMixedModeConfigPropertyMCF_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/annotations/partialanno/paClient.java#testMixedModeConfigPropertyNoOverride_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/annotations/partialanno/paClient.java#testMixedModeConfigPropertyOverride_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/annotations/partialanno/paClient.java#testMixedModeConfigPropertyRA_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/annotations/partialanno/paClient.java#testNoDefaultVallAnnoElement_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/connManager/connManagerClient1.java#testcheckConnectionManager_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/connManager/connManagerClient1.java#testTransactionSupportLevels_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/compat/compatClient1.java#testGetConnection1_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/compat/compatClient1.java#testgetConnectionWithParameter1_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIApplicationServerInternalException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPICommException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIEISSystemException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIHintsContext_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIIllegalStateException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIInvalidPropertyException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPILocalTransactionException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIManagedConnection_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIManagedConnectionMetaData_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPINotSupportedException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIResourceAdapterInternalException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIResourceAllocationException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIResourceException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIRetryableUnavailableException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIRetryableWorkRejectedException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPISecurityException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPISharingViolationException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIUnavailableException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIWorkCompletedException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIWorkException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testAPIWorkRejectedException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testGetConnection1_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/connection/connectionClient1.java#testgetConnectionWithParameter1_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/event/eventClient1.java#testConnectionEventListener_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/lifecycle/lifecycleClient1.java#testAssociationMCFandRA_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/lifecycle/lifecycleClient1.java#testAssociationMCFandRA2_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/lifecycle/lifecycleClient1.java#testBootstrapforNull_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/lifecycle/lifecycleClient1.java#testInstantiationOfRA_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/lifecycle/lifecycleClient1.java#testMCFcalledOnce_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/lifecycle/lifecycleClient1.java#testRAforJavaBean_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/lifecycle/lifecycleClient1.java#testRASharability_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/security/securityClient1.java#testAppEISSignon_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/security/securityClient1.java#testCBTestAllCallbacksAndPrin_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/security/securityClient1.java#testCBTestAllCallbacksNullPrin_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/security/securityClient1.java#testCBTestCPCandGPC_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/security/securityClient1.java#testCBTestCPCandNullPrin_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/security/securityClient1.java#testCBTestCPCandPrin_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/security/securityClient1.java#testCBTestCPCandPVC_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/security/securityClient1.java#testCBTestEISCPCandPrin_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/security/securityClient1.java#testCBTestGPCandCPCFail_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/security/securityClient1.java#testComponentManaged_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/security/securityClient1.java#testConnManagerAllocateConnection_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/security/securityClient1.java#testContainerManaged_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/transinflow/transinflowClient1.java#testConcurrentWorkXid_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/transinflow/transinflowClient1.java#testGetTransactionSupport_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/transinflow/transinflowClient1.java#testSetResourceAdapterMethod_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/transinflow/transinflowClient1.java#testTransactionSynchronizationRegistry_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/transinflow/transinflowClient1.java#testXATerminator_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workcontext/WorkContextClient.java#testHICNotifications_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workcontext/WorkContextClient.java#testIsContextSupported_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workcontext/WorkContextClient.java#testNestedWorkContexts_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workcontext/WorkContextClient.java#testNestedWorkContexts2_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workcontext/WorkContextClient.java#testNestedWorkContexts3_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workcontext/WorkContextClient.java#testSecurityContextCBH_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workcontext/WorkContextClient.java#testSecurityContextExecSubject_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workcontext/WorkContextClient.java#testSecurityContextServiceSubject_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workcontext/WorkContextClient.java#testWorkContextErrorCode_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workcontext/WorkContextClient.java#testWorkContextErrorCode2_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workcontext/WorkContextClient.java#testWorkContextNotifications_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workcontext/WorkContextClient.java#testWorkContextProvider_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workmgt/workmgtClient1.java#testForUnsharedTimer_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workmgt/workmgtClient1.java#testNestedWork_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workmgt/workmgtClient1.java#testScheduleWork_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workmgt/workmgtClient1.java#testScheduleWorkListener_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workmgt/workmgtClient1.java#testUnknownWorkDuration_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workmgt/workmgtClient1.java#testWorkCompletedException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workmgt/workmgtClient1.java#testWorkListenerImplementation_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/localTx/workmgt/workmgtClient1.java#testWorkManagerImplementaion_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/compat/compatClient1.java#testGetConnection1_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/compat/compatClient1.java#testgetConnectionWithParameter1_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/connection/connectionClient1.java#testGetConnection1_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/connection/connectionClient1.java#testgetConnectionWithParameter1_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/event/eventClient1.java#testConnectionEventListener_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/lifecycle/lifecycleClient1.java#testAssociationMCFandRA_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/lifecycle/lifecycleClient1.java#testAssociationMCFandRA2_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/lifecycle/lifecycleClient1.java#testBootstrapforNull_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/lifecycle/lifecycleClient1.java#testInstantiationOfRA_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/lifecycle/lifecycleClient1.java#testMCFcalledOnce_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/lifecycle/lifecycleClient1.java#testRAforJavaBean_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/lifecycle/lifecycleClient1.java#testRASharability_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/security/securityClient1.java#testAppEISSignon_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/security/securityClient1.java#testComponentManaged_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/security/securityClient1.java#testConnManagerAllocateConnection_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/security/securityClient1.java#testContainerManaged_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/workmgt/workmgtClient1.java#testForUnsharedTimer_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/workmgt/workmgtClient1.java#testNestedWork_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/workmgt/workmgtClient1.java#testScheduleWork_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/workmgt/workmgtClient1.java#testScheduleWorkListener_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/workmgt/workmgtClient1.java#testUnknownWorkDuration_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/workmgt/workmgtClient1.java#testWorkCompletedException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/workmgt/workmgtClient1.java#testWorkListenerImplementation_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/noTx/workmgt/workmgtClient1.java#testWorkManagerImplementaion_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/permissiondd/Client.java#testValidateCustomPerm_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/permissiondd/Client.java#testValidateLocalGrantForCustomPerm_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/permissiondd/Client.java#testValidateLocalPermsInvalidName_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/permissiondd/Client.java#testValidateMissingPermFails_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/permissiondd/Client.java#testValidateRequiredPermSet_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/permissiondd/Client.java#testValidateRestrictedLocalPerm_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/compat/compatClient1.java#testGetConnection1_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/compat/compatClient1.java#testgetConnectionWithParameter1_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/connection/connectionClient1.java#testGetConnection1_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/connection/connectionClient1.java#testgetConnectionWithParameter1_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/event/eventClient1.java#testConnectionEventListener_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/lifecycle/lifecycleClient1.java#testAssociationMCFandRA_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/lifecycle/lifecycleClient1.java#testAssociationMCFandRA2_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/lifecycle/lifecycleClient1.java#testBootstrapforNull_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/lifecycle/lifecycleClient1.java#testInstantiationOfRA_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/lifecycle/lifecycleClient1.java#testMCFcalledOnce_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/lifecycle/lifecycleClient1.java#testRAforJavaBean_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/lifecycle/lifecycleClient1.java#testRASharability_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/security/securityClient1.java#testAppEISSignon_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/security/securityClient1.java#testComponentManaged_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/security/securityClient1.java#testConnManagerAllocateConnection_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/security/securityClient1.java#testContainerManaged_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/workmgt/workmgtClient1.java#testForUnsharedTimer_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/workmgt/workmgtClient1.java#testNestedWork_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/workmgt/workmgtClient1.java#testScheduleWork_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/workmgt/workmgtClient1.java#testScheduleWorkListener_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/workmgt/workmgtClient1.java#testUnknownWorkDuration_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/workmgt/workmgtClient1.java#testWorkCompletedException_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/workmgt/workmgtClient1.java#testWorkListenerImplementation_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/connector/xa/workmgt/workmgtClient1.java#testWorkManagerImplementaion_from_connectorservlet
[javatest.batch] PASSED........com/sun/ts/tests/signaturetest/connector/ConnectorSigTest.java#signatureTest
[javatest.batch] 
[javatest.batch] Total time = 228s
[javatest.batch] Setup time = 0s
[javatest.batch] Cleanup time = 0s
[javatest.batch] Test results: passed: 139
