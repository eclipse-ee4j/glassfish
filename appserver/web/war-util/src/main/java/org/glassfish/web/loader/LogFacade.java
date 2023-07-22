/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.glassfish.web.loader;

import java.text.MessageFormat;
import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 * Provides the logging facilities.
 *
 * @author Shing Wai Chan
 */
public class LogFacade {
    @LogMessagesResourceBundle
    private static final String SHARED_LOGMESSAGE_RESOURCE = "org.glassfish.web.loader.LogMessages";

    @LoggerInfo(subsystem = "WEB", description = "WEB Util Logger", publish = true)
    private static final String WEB_UTIL_LOGGER = "jakarta.enterprise.web.util";

    private static final Logger LOGGER =
            Logger.getLogger(WEB_UTIL_LOGGER, SHARED_LOGMESSAGE_RESOURCE);

    private LogFacade() {}

    public static Logger getLogger() {
        return LOGGER;
    }

    public static System.Logger getSysLogger(Class<?> clazz) {
        return System.getLogger(clazz.getName(), LOGGER.getResourceBundle());
    }

    public static String getString(String key, Object... objects) {
        return MessageFormat.format(LOGGER.getResourceBundle().getString(key), objects);
    }


    private static final String PREFIX = "AS-WEB-UTIL-";

    @LogMessageInfo(
            message = "Resource ''{0}'' is missing",
            level = "SEVERE",
            cause = "A naming exception is encountered",
            action = "Check the list of resources")
    public static final String MISSING_RESOURCE = PREFIX + "00001";

    @LogMessageInfo(
            message = "Failed tracking modifications of ''{0}'' : {1}",
            level = "SEVERE",
            cause = "A ClassCastException is encountered",
            action = "Check if the object is an instance of the class")
    public static final String FAILED_TRACKING_MODIFICATIONS = PREFIX + "00002";

    @LogMessageInfo(
            message = "Security Violation, attempt to use Restricted Class: {0}",
            level = "INFO")
    public static final String SECURITY_EXCEPTION = PREFIX + "00004";

    @LogMessageInfo(
            message = "Class {0} has unsupported major or minor version numbers, which are greater "
                + "than those found in the Java Runtime Environment version {1}",
            level = "WARNING")
    public static final String UNSUPPORTED_VERSION = PREFIX + "00005";

    @LogMessageInfo(
            message = "Unable to load class with name [{0}], reason: {1}",
            level = "WARNING")
    public static final String UNABLE_TO_LOAD_CLASS = PREFIX + "00006";

    @LogMessageInfo(
            message = "The web application [{0}] registered the JDBC driver [{1}] but failed to unregister it"
                + " when the web application was stopped. To prevent a memory leak, the JDBC Driver"
                + " has been forcibly unregistered.",
            level = "WARNING")
    public static final String CLEAR_JDBC = PREFIX + "00007";

    @LogMessageInfo(
            message = "JDBC driver de-registration failed for web application [{0}]",
            level = "WARNING")
    public static final String JDBC_REMOVE_FAILED = PREFIX + "00008";

    @LogMessageInfo(
            message = "Failed to check for ThreadLocal references for web application [{0}]",
            level = "WARNING")
    public static final String CHECK_THREAD_LOCALS_FOR_LEAKS_FAIL = PREFIX + "00011";

    @LogMessageInfo(
            message = "The web application [{0}] created a ThreadLocal with key of [{1}]"
                + " but failed to remove it when the web application was stopped."
                + " Threads are going to be renewed over time to try and avoid a probable memory leak.",
            level = "SEVERE",
            cause = "Failed to remove a ThreadLocal when the web application was stopped",
            action = "Threads are going to be renewed over time to try and avoid a probable memory leak.")
    public static final String CHECK_THREAD_LOCALS_FOR_LEAKS_KEY = PREFIX + "00015";

    @LogMessageInfo(
            message = "The web application [{0}] created a ThreadLocal with key of [{1}] and a value of [{2}]"
                + " but failed to remove it when the web application was stopped."
                + " Threads are going to be renewed over time to try and avoid a probable memory leak.",
            level = "SEVERE",
            cause = "Failed to remove a ThreadLocal when the web application was stopped",
            action = "Threads are going to be renewed over time to try and avoid a probable memory leak.")
    public static final String CHECK_THREAD_LOCALS_FOR_LEAKS = PREFIX + "00016";

    @LogMessageInfo(
            message = "Illegal JAR entry detected with name {0}",
            level = "INFO")
    public static final String ILLEGAL_JAR_PATH = PREFIX + "00021";

    @LogMessageInfo(
            message = "Unable to validate JAR entry with name {0}",
            level = "INFO")
    public static final String VALIDATION_ERROR_JAR_PATH = PREFIX + "00022";

    @LogMessageInfo(
            message = "Unable to create {0}",
            level = "WARNING")
    public static final String UNABLE_TO_CREATE = PREFIX + "00023";

    @LogMessageInfo(
            message = "Unable to delete {0}",
            level = "WARNING")
    public static final String UNABLE_TO_DELETE = PREFIX + "00024";

    @LogMessageInfo(
            message = "extra-class-path component [{0}] is not a valid pathname",
            level = "SEVERE",
            cause = "A naming exception is encountered",
            action = "Check the list of resources")
    public static final String CLASSPATH_ERROR = PREFIX + "00027";

    @LogMessageInfo(
            message = "The clearReferencesStatic is not consistent in context.xml for virtual servers",
            level = "WARNING")
    public static final String INCONSISTENT_CLEAR_REFERENCE_STATIC = PREFIX + "00028";

    @LogMessageInfo(
            message = "class-loader attribute dynamic-reload-interval in sun-web.xml not supported",
            level = "WARNING")
    public static final String DYNAMIC_RELOAD_INTERVAL = PREFIX + "00029";

    @LogMessageInfo(
            message = "Property element in sun-web.xml has null 'name' or 'value'",
            level = "WARNING")
    public static final String NULL_WEB_PROPERTY = PREFIX + "00030";

    @LogMessageInfo(
            message = "Ignoring invalid property [{0}] = [{1}]",
            level = "WARNING")
    public static final String INVALID_PROPERTY = PREFIX + "00031";

    @LogMessageInfo(
            message = "The xml element should be [{0}] rather than [{1}]",
            level = "INFO")
    public static final String UNEXPECTED_XML_ELEMENT = PREFIX + "00032";

    @LogMessageInfo(
            message = "This is an unexpected end of document",
            level = "WARNING")
    public static final String UNEXPECTED_END_DOCUMENT = PREFIX + "00033";

    @LogMessageInfo(
            message = "Unexpected type of ClassLoader. Expected: java.net.URLClassLoader, got: {0}",
            level = "WARNING")
    public static final String WRONG_CLASSLOADER_TYPE = PREFIX + "00034";

    @LogMessageInfo(
            message = "Unable to load class {0}, reason: {1}",
            level = "FINE")
    public static final String CLASS_LOADING_ERROR = PREFIX + "00035";

    @LogMessageInfo(
            message = "Invalid URLClassLoader path component: [{0}] is neither a JAR file nor a directory",
            level = "WARNING")
    public static final String INVALID_URL_CLASS_LOADER_PATH = PREFIX + "00036";

    @LogMessageInfo(
            message = "Error trying to scan the classes at {0} for annotations in which"
                + " a ServletContainerInitializer has expressed interest",
            level = "SEVERE",
            cause = "An IOException is encountered",
            action = "Verify if the path is correct")
    public static final String IO_ERROR = PREFIX + "00037";

    @LogMessageInfo(
            message = "Ignoring [{0}] during Tag Library Descriptor (TLD) processing",
            level = "WARNING")
    public static final String TLD_PROVIDER_IGNORE_URL = PREFIX + "00038";

    @LogMessageInfo(
            message = "Unable to determine TLD resources for [{0}] tag library, because class loader"
                + " [{1}] for [{2}] is not an instance of java.net.URLClassLoader",
            level = "WARNING")
    public static final String UNABLE_TO_DETERMINE_TLD_RESOURCES = PREFIX + "00039";
}
