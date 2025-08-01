/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.security;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

public class SecurityLoggerInfo {

    public static final String LOGMSG_PREFIX = "NCLS-SECURITY";

    @LogMessagesResourceBundle
    public static final String SHARED_LOGMESSAGE_RESOURCE = "com.sun.enterprise.security.LogMessages";

    @LoggerInfo(subsystem = "SECURITY", description = "Core Security", publish = true)
    public static final String SECURITY_LOGGER = "jakarta.enterprise.system.core.security";

    private static final Logger securityLogger = Logger.getLogger(SECURITY_LOGGER, SHARED_LOGMESSAGE_RESOURCE);

    public static Logger getLogger() {
        return securityLogger;
    }

    // Common Security messages
    @LogMessageInfo(
            message = "Caught exception.",
            cause = "Unknown",
            action = "Check the server logs and contact support.",
            level = "WARNING")
    public static final String securityExceptionError = LOGMSG_PREFIX + "-01000";

    @LogMessageInfo(
            message = "Java security manager is enabled.",
            level = "INFO")
    public static final String secMgrEnabled = LOGMSG_PREFIX + "-01001";

    @LogMessageInfo(
            message = "Java security manager is disabled.",
            level = "INFO")
    public static final String secMgrDisabled = LOGMSG_PREFIX + "-01002";

    @LogMessageInfo(
            message = "An I/O error occurred during copying of server config files.",
            cause = "Copying server config files.",
            action = "Check the server logs and contact support.",
            level = "SEVERE")
    public static final String ioError = LOGMSG_PREFIX + "-01004";

    @LogMessageInfo(
            message = "XML processing error occurred during copying of server config files.",
            cause = "The XML file(s) may not be well formed.",
            action = "Make sure the XML file(s) are well formed.",
            level = "SEVERE")
    public static final String xmlStreamingError = LOGMSG_PREFIX + "-01005";

    // Security service messages
    @LogMessageInfo(
            message = "Entering Security Startup Service.",
            level = "INFO")
    public static final String secServiceStartupEnter = LOGMSG_PREFIX + "-01010";

    @LogMessageInfo(
            message = "Security Service(s) started successfully.",
            level = "INFO")
    public static final String secServiceStartupExit = LOGMSG_PREFIX + "-01011";

    @LogMessageInfo(
            message = "An I/O error occurred while copying the security config files.",
            cause = "Copying security files to instanceRoot/config.",
            action = "Check the server logs and contact support.",
            level = "WARNING")
    public static final String copyingSecurityConfigFilesIOError = LOGMSG_PREFIX + "-01013";

    @LogMessageInfo(
            message = "An error occurred while upgrading the security config files.",
            cause = "Upgrade security config files from a previous version.",
            action = "Check the server logs and contact support.",
            level = "SEVERE")
    public static final String securityUpgradeServiceException = LOGMSG_PREFIX + "-01014";

    @LogMessageInfo(
            message = "Upgrade from v2 EE  to v3.1 requires manual steps. Please refer to the v3.1 Upgrade Guide for details.",
            cause = "Upgrade security config files from a previous version.",
            action = "Please refer to the v3.1 Upgrade Guide for details.",
            level = "WARNING")
    public static final String securityUpgradeServiceWarning = LOGMSG_PREFIX + "-01015";

    // General login processing messages: start 1050
    @LogMessageInfo(
            message = "Certificate authentication requires certificate realm.",
            cause = "Unknown realm type.",
            action = "Check server configuration",
            level = "WARNING")
    public static final String certLoginBadRealmError = LOGMSG_PREFIX + "-01050";

    // Realms and Login Modules: start at 1100
    @LogMessageInfo(
            message = "Disabled realm [{0}] due to errors.",
            cause = "No realms available.",
            action = "Check the server logs and contact support.",
            level = "WARNING")
    public static final String realmConfigDisabledError = LOGMSG_PREFIX + "-01100";

    @LogMessageInfo(
            message = "No realms available. Authentication services disabled.",
            cause = "No realms available.",
            action = "Check the server logs and contact support.",
            level = "SEVERE")
    public static final String noRealmsError = LOGMSG_PREFIX + "-01101";

    @LogMessageInfo(
            message = "Error while obtaining private subject credentials: {0}",
            cause = "Private Credentials of Subject not available.",
            action = "Please check if the private credentials are available.",
            level = "WARNING")
    public static final String privateSubjectCredentialsError = LOGMSG_PREFIX + "-01104";

    @LogMessageInfo(
            message = "A PasswordCredential was required but not provided.",
            cause = "PasswordCredential was required, but not supplied.",
            action = "Please check if the password is provided.",
            level = "SEVERE")
    public static final String noPwdCredentialProvidedError = LOGMSG_PREFIX + "-01105";

    @LogMessageInfo(
            message = "Realm [{0}] of classtype [{1}] successfully created.",
            level = "INFO")
    public static final String realmCreated = LOGMSG_PREFIX + "-01115";

    @LogMessageInfo(
            message = "Realm [{0}] successfully updated.",
            level = "INFO")
    public static final String realmUpdated = LOGMSG_PREFIX + "-01117";

    @LogMessageInfo(
            message = "Realm [{0}] successfully deleted.",
            level = "INFO")
    public static final String realmDeleted = LOGMSG_PREFIX + "-01119";

    // JACC and policy: start at 1140
    @LogMessageInfo(
            message = "Policy provider configuration overridden by property {0} with value {1}.",
            level = "INFO")
    public static final String policyProviderConfigOverrideMsg = LOGMSG_PREFIX + "-01140";

    @LogMessageInfo(
            message = "Requested jacc-provider [{0}] is not configured in domain.xml.",
            level = "WARNING")
    public static final String policyNoSuchName = LOGMSG_PREFIX + "-01141";

    @LogMessageInfo(
            message = "Error while reading policy-provider in domain.xml.",
            level = "WARNING")
    public static final String policyReadingError = LOGMSG_PREFIX + "-01142";

    @LogMessageInfo(
            message = "Loading policy provider {0}.",
            level = "INFO")
    public static final String policyLoading = LOGMSG_PREFIX + "-01143";

    @LogMessageInfo(
            message = "Error while installing policy provider: {0}.",
            cause = "Setting the system wide policy.",
            action = "Make sure there's sufficient permission to set the policy.",
            level = "SEVERE")
    public static final String policyInstallError = LOGMSG_PREFIX + "-01144";

    @LogMessageInfo(
            message = "No policy provider defined. Will use the default JDK Policy implementation.",
            level = "WARNING")
    public static final String policyNotLoadingWarning = LOGMSG_PREFIX + "-01145";

    @LogMessageInfo(
            message = "Policy configuration factory overridden by property {0} with value {1}",
            level = "WARNING")
    public static final String policyFactoryOverride = LOGMSG_PREFIX + "-01146";

    @LogMessageInfo(
            message = "Policy configuration factory not defined.",
            level = "WARNING")
    public static final String policyConfigFactoryNotDefined = LOGMSG_PREFIX + "-01147";

    @LogMessageInfo(
            message = "Policy provider configuration overridden by property {0} with value {1}.",
            level = "WARNING")
    public static final String policyProviderConfigOverrideWarning = LOGMSG_PREFIX + "-01149";

    @LogMessageInfo(
            message = "Failed to get the group names for user {0} in realm {1}: {2}.",
            cause = "Operation now allowed.",
            action = "Check the server logs and contact support.",
            level = "WARNING")
    public static final String invalidOperationForRealmError = LOGMSG_PREFIX + "-01150";

    @LogMessageInfo(
            message = "Failed to get the group names for user {0} in realm {1}: {2}.",
            cause = "Obtain the group names for a nonexistent user.",
            action = "Make sure the user is valid.",
            level = "WARNING")
    public static final String noSuchUserInRealmError = LOGMSG_PREFIX + "-01151";

    @LogMessageInfo(
            message = "ERROR: Unknown credential provided. Class: [{0}].",
            level = "INFO")
    public static final String unknownCredentialError = LOGMSG_PREFIX + "-05019";

    @LogMessageInfo(
            message = "Exception in getting security context.",
            cause = "There was an exception obtaining the default security context.",
            action = "Check the server logs and contact support.",
            level = "SEVERE")
    public static final String defaultSecurityContextError = LOGMSG_PREFIX + "-05036";

    @LogMessageInfo(
            message = "Default user login error.",
            cause = "There was an exception while authenticating the default caller principal.",
            action = "Check the server logs and contact support.",
            level = "SEVERE")
    public static final String defaultUserLoginError = LOGMSG_PREFIX + "-05038";

    @LogMessageInfo(
            message = "Exception entering name and password for security.",
            cause = "An exception occurred while processing username and password for security.",
            action = "An exception occurred while processing username and password for security.",
            level = "SEVERE")
    public static final String usernamePasswordEnteringSecurityError = LOGMSG_PREFIX + "-05039";

    @LogMessageInfo(
            message = "Exception in security AccessController action.",
            cause = "Running a privileged action.",
            action = "Check the server logs and contact support.",
            level = "SEVERE")
    public static final String securityAccessControllerActionError = LOGMSG_PREFIX + "-05043";

    @LogMessageInfo(
            message = "Audit: Authentication refused for [{0}].",
            level = "INFO")
    public static final String auditAtnRefusedError = LOGMSG_PREFIX + "-05046";

    @LogMessageInfo(
            message = "doAsPrivileged AuthPermission required to set SecurityContext.",
            cause = "Setting the SecurityContext in the current thread.",
            action = "Check the server logs and contact support.",
            level = "SEVERE")
    public static final String securityContextPermissionError = LOGMSG_PREFIX + "-05048";

    @LogMessageInfo(
            message = "Unexpected exception while attempting to set SecurityContext.",
            cause = "There was an unexpected exception while setting the security context.",
            action = "Check the server logs and contact support.",
            level = "SEVERE")
    public static final String securityContextUnexpectedError = LOGMSG_PREFIX + "-05049";

    @LogMessageInfo(
            message = "Could not change the SecurityContext.",
            cause = "Changing the current SecurityContext.",
            action = "Check the server logs and contact support.",
            level = "SEVERE")
    public static final String securityContextNotChangedError = LOGMSG_PREFIX + "-05050";

    @LogMessageInfo(
            message = "Subject is null.",
            cause = "null Subject used in SecurityContext construction.",
            action = "Make sure the Subject is not null.",
            level = "WARNING")
    public static final String nullSubjectWarning = LOGMSG_PREFIX + "-05052";

    @LogMessageInfo(
            message = "Role mapping conflicts found in application {0}. Some roles may not be mapped.",
            level = "INFO")
    public static final String roleMappingConflictError = LOGMSG_PREFIX + "-05055";

    @LogMessageInfo(
            message = "Failed to instantiate the SecurityLifeCycle.",
            cause = "Unknown",
            action = "Check the server logs and contact support.",
            level = "SEVERE")
    public static final String secMgrInitFailure = LOGMSG_PREFIX + "-05101";

    @LogMessageInfo(
            message = "Error enabling secure admin.",
            cause = "Enable secure admin.",
            action = "Check your command usage.",
            level = "SEVERE")
    public static final String enablingSecureAdminError = LOGMSG_PREFIX + "-05200";

    @LogMessageInfo(
            message = "Error disabling secure admin.",
            cause = "Disable secure admin.",
            action = "Check your command usage.",
            level = "SEVERE")
    public static final String disablingSecureAdminError = LOGMSG_PREFIX + "-05201";

    @LogMessageInfo(
            message = "IIOP Security - error importing a name: ${0}.",
            cause = "Importing a name in IIOP.",
            action = "Check the server logs and contact support.",
            level = "SEVERE")
    public static final String iiopImportNameError = LOGMSG_PREFIX + "-05300";

    @LogMessageInfo(
            message = "IIOP Security - error creating an exported name: ${0}.",
            cause = "Creating an exported name in IIOP.",
            action = "Check the server logs and contact support.",
            level = "SEVERE")
    public static final String iiopCreateExportedNameError = LOGMSG_PREFIX + "-05301";
}
