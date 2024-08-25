/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util;

import java.text.MessageFormat;
import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 * Logger information for the common-util module.
 *
 * @author Tom Mueller
 */
/* Module private */
public class CULoggerInfo {

    private static final String LOGMSG_PREFIX = "NCLS-COMUTIL";

    @LogMessagesResourceBundle
    private static final String SHARED_LOGMESSAGE_RESOURCE = "com.sun.enterprise.util.LogMessages";

    @LoggerInfo(subsystem = "COMMON", description = "Common Utilities", publish = true)
    private static final String UTIL_LOGGER = "jakarta.enterprise.system.util";

    private static final Logger utilLogger = Logger.getLogger(UTIL_LOGGER, SHARED_LOGMESSAGE_RESOURCE);

    public static Logger getLogger() {
        return utilLogger;
    }

    public static String getString(String key) {
        return utilLogger.getResourceBundle().getString(key);
    }

    public static String getString(String key, Object... args) {
        return MessageFormat.format(getString(key), args);
    }

    @LogMessageInfo(
            message = "Failed to process class {0} with bytecode preprocessor {1}",
            cause = "Unknown",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String preprocessFailed = LOGMSG_PREFIX + "-00001";

    @LogMessageInfo(
            message = "Class {0} is being reset to its original state",
            cause = "Unknown",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String resettingOriginal = LOGMSG_PREFIX + "-00002";

    @LogMessageInfo(
            message = "Class {0} is being reset to the last successful preprocessor",
            cause = "Unknown",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String resettingLastGood = LOGMSG_PREFIX + "-00003";

    @LogMessageInfo(
            message = "The supplied preprocessor class {0} is not an instance of org.glassfish.api.BytecodePreprocessor",
            cause = "Unknown",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String invalidType = LOGMSG_PREFIX + "-00004";

    @LogMessageInfo(
            message = "Bytecode preprocessor disabled",
            cause = "Unknown",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String disabled = LOGMSG_PREFIX + "-00005";

    @LogMessageInfo(
            message = "Initialization failed for bytecode preprocessor {0}",
            cause = "Unknown",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String failedInit = LOGMSG_PREFIX + "-00006";

    @LogMessageInfo(
            message = "Error setting up preprocessor",
            cause = "Unknown",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String setupEx = LOGMSG_PREFIX + "-00007";

    @LogMessageInfo(message = "Illegal max-entries [{0}]; please check your cache configuration.")
    public static final String illegalMaxEntries = LOGMSG_PREFIX + "-00008";

    @LogMessageInfo(message = "Illegal MaxSize value [{0}]")
    public static final String boundedMultiLruCacheIllegalMaxSize = LOGMSG_PREFIX + "-00009";

    @LogMessageInfo(
            message = "Error closing zip file for class path entry {0}",
            level = "INFO")
    public static final String exceptionClosingURLEntry = LOGMSG_PREFIX + "-00010";

    @LogMessageInfo(
            message = "An error occurred while adding URL [{0}] to the EJB class loader. Please check the content of this URL.",
            cause = "An unexpected exception occurred while processing a URL.",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String badUrlEntry = LOGMSG_PREFIX + "-00011";

    @LogMessageInfo(
            message = "The URL entry is missing while constructing the classpath.",
            level = "INFO")
    public static final String missingURLEntry = LOGMSG_PREFIX + "-00012";

    @LogMessageInfo(
            message = "Error closing zip file for duplicate class path entry {0}",
            level = "INFO")
    public static final String exceptionClosingDupUrlEntry = LOGMSG_PREFIX + "-00013";

    @LogMessageInfo(
            message = "Exception in ASURLClassLoader",
            level = "INFO")
    public static final String exceptionInASURLClassLoader = LOGMSG_PREFIX + "-00014";

    @LogMessageInfo(
            message = "ASURLClassLoader {1} was requested to find resource {0} after done was invoked from the following stack trace",
            level = "WARNING")
    public static final String findResourceAfterDone = LOGMSG_PREFIX + "-00015";

    @LogMessageInfo(
            message = "Error: Request made to load class or resource [{0}] on an ASURLClassLoader instance that has already been shutdown. [{1}]",
            level = "WARNING")
    public static final String doneAlreadyCalled = LOGMSG_PREFIX + "-00016";

    @LogMessageInfo(
            message = "{0} actually got transformed",
            level = "INFO")
    public static final String actuallyTransformed = LOGMSG_PREFIX + "-00017";

    @LogMessageInfo(
            message = "ASURLClassLoader {1} was requested to find class {0} after done was invoked from the following stack trace",
            level = "WARNING")
    public static final String findClassAfterDone = LOGMSG_PREFIX + "-00018";

    @LogMessageInfo(
            message = "Illegal call to close() detected",
            level = "WARNING")
    public static final String illegalCloseCall = LOGMSG_PREFIX + "-00019";

    @LogMessageInfo(
            message = "Error processing file with path {0} in {1}",
            cause = "An unexpected exception occurred while processing a file.",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String exceptionProcessingFile = LOGMSG_PREFIX + "-00020";

    @LogMessageInfo(
            message = "Error checking for existing of {0} in {1}",
            cause = "An unexpected exception occurred while checking for the existence of a file.",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String exceptionCheckingFile = LOGMSG_PREFIX + "-00021";

    @LogMessageInfo(
            message = "Error closing an open stream during loader clean-up",
            level = "WARNING")
    public static final String exceptionClosingStream = LOGMSG_PREFIX + "-00022";

    @LogMessageInfo(
            message = "Input stream has been finalized or forced closed without being explicitly closed; stream instantiation reported in following stack trace",
            level = "WARNING")
    public static final String inputStreamFinalized = LOGMSG_PREFIX + "-00023";

    @LogMessageInfo(
            message = "Unable to create client data directory: {0}",
            cause = "An unexpected failure occurred while creating the directory for the file.",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String errorCreatingDirectory = LOGMSG_PREFIX + "-00024";

    @LogMessageInfo(
            message = "Exception in invokeApplicationMain [{0}].",
            cause = "An unexpected exception occurred.",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String exceptionInUtility = LOGMSG_PREFIX + "-00025";

    @LogMessageInfo(
            message = "The main method signature is invalid.",
            cause = "While invoking a main class, an invalid method was found.",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String mainNotValid = LOGMSG_PREFIX + "-00026";

    @LogMessageInfo(
            message = "Error while caching the local string manager - package name may be null.",
            cause = "An unexpected exception occurred.",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String exceptionCachingStringManager = LOGMSG_PREFIX + "-00027";

    @LogMessageInfo(
            message = "Error while constructing the local string manager object.",
            cause = "An unexpected exception occurred.",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String exceptionConstructingStringManager = LOGMSG_PREFIX + "-00028";

    @LogMessageInfo(
            message = "Error in local string manager - resource bundle is probably missing.",
            cause = "An unexpected exception occurred.",
            action = "Check the system logs and contact support.",
            level = "SEVERE")
    public static final String exceptionResourceBundle = LOGMSG_PREFIX + "-00029";

    @LogMessageInfo(
            message = "Error while formatting the local string.",
            level = "WARNING")
    public static final String exceptionWhileFormating = LOGMSG_PREFIX + "-00030";

    @LogMessageInfo(
            message = "Failed to open jar file: {0}",
            level = "WARNING")
    public static final String exceptionJarOpen = LOGMSG_PREFIX + "-00038";

    @LogMessageInfo(
            message = "Attempt to use non-existent auth token {0}",
            level = "WARNING")
    public static final String useNonexistentToken = LOGMSG_PREFIX + "-00039";

    @LogMessageInfo(
            message = "File Lock not released on {0}",
            level = "WARNING")
    public static final String fileLockNotReleased = LOGMSG_PREFIX + "-00040";

    @LogMessageInfo(
            message = "BundleTracker.removedBundle null bundleID for {0}",
            level="WARNING")
    public static final String NULL_BUNDLE = LOGMSG_PREFIX + "-00042";
}
