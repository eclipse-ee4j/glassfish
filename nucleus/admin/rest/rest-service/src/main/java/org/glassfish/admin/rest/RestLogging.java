/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.admin.rest;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 *
 * @author jdlee
 */
public class RestLogging {

    @LogMessagesResourceBundle
    public static final String SHARED_LOGMESSAGE_RESOURCE = "org.glassfish.admin.rest.LogMessages";
    @LoggerInfo(subsystem = "REST", description = "Main REST Logger", publish = true)
    public static final String REST_MAIN_LOGGER = "jakarta.enterprise.admin.rest";
    public static final Logger restLogger = Logger.getLogger(REST_MAIN_LOGGER, SHARED_LOGMESSAGE_RESOURCE);
    @LogMessageInfo(message = "Listening to REST requests at context: {0}/domain.", level = "INFO")
    public static final String REST_INTERFACE_INITIALIZED = "NCLS-REST-00001";
    @LogMessageInfo(message = "Incorrectly formatted entry in {0}: {1}", level = "INFO")
    public static final String INCORRECTLY_FORMATTED_ENTRY = "NCLS-REST-00002";
    @LogMessageInfo(message = "An error occurred while processing the request. Please see the server logs for details.", cause = "A runtime error occurred. Please see the log file for more details", action = "See the log file for more details", level = "SEVERE")
    public static final String SERVER_ERROR = "NCLS-REST-00003";
    @LogMessageInfo(message = "The class specified by generator does not implement DefaultsGenerator", cause = "The generator does not implement the DefaultsGenerator interface", action = "Modify the generator to implement the DefaultsGenerator interface", level = "SEVERE")
    public static final String DOESNT_IMPLEMENT_DEFAULTS_GENERATOR = "NCLS-REST-00004";
    @LogMessageInfo(message = "Unsupported fixed value.  Supported types are String, boolean, Boolean, int, Integer, long, Long, double, Double, float, and Float", cause = "The RestModel property has specified an unsupported data type", action = "Modify the model to use one of the supported types", level = "SEVERE")
    public static final String UNSUPPORTED_FIXED_VALUE = "NCLS-REST-00005";
    @LogMessageInfo(message = "Fixed value type does not match the property type", cause = "The value for the given property can not be converted to the property's type", action = "Check the input data", level = "SEVERE")
    public static final String VALUE_DOES_NOT_MATCH_TYPE = "NCLS-REST-00006";
    @LogMessageInfo(message = "Cannot marshal", cause = "The system is unable to generate XML for the given object", action = "Check the logs for more details", level = "SEVERE")
    public static final String CANNOT_MARSHAL = "NCLS-REST-00007";
    @LogMessageInfo(message = "Unable to delete directory {0}.  Will attempt deletion again upon JVM exit.", level = "WARNING")
    public static final String UNABLE_DELETE_DIRECTORY = "NCLS-REST-00009";
    @LogMessageInfo(message = "Unable to delete file %s.  Will attempt deletion again upon JVM exit.", level = "WARNING")
    public static final String UNABLE_DELETE_FILE = "NCLS-REST-00010";
    @LogMessageInfo(message = "{0}:  {1}", level = "INFO")
    public static final String TIMESTAMP_MESSAGE = "NCLS-REST-00011";
    @LogMessageInfo(message = "Compilation failed.", level = "INFO")
    public static final String COMPILATION_FAILED = "NCLS-REST-00012";
    @LogMessageInfo(message = "File creation failed: {0}", cause = "The system was unable to create the specified file.", action = "Verify that the filesystem is writable and has sufficient disk space", level = "SEVERE")
    public static final String FILE_CREATION_FAILED = "NCLS-REST-00013";
    @LogMessageInfo(message = "Directory creation failed: {0}", level = "INFO")
    public static final String DIR_CREATION_FAILED = "NCLS-REST-00014";
    @LogMessageInfo(message = "Unexpected exception during initilization.", cause = "The system is unable to init ReST interface", action = "Check the logs for more details", level = "SEVERE")
    public static final String INIT_FAILED = "NCLS-REST-00015";
    @LogMessageInfo(message = "I/O exception", cause = "See server log for details", action = "See server log for details.", level = "SEVERE")
    public static final String IO_EXCEPTION = "NCLS-REST-00016";
}
