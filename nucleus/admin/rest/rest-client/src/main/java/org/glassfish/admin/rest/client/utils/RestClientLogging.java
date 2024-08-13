/*
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

package org.glassfish.admin.rest.client.utils;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 *
 * @author jdlee
 */
public class RestClientLogging {
    @LogMessagesResourceBundle
    public static final String SHARED_LOGMESSAGE_RESOURCE = "org.glassfish.admin.rest.client.utils.LogMessages";
    @LoggerInfo(subsystem = "REST", description = "REST Client Logger", publish = true)
    public static final String REST_CLIENT_LOGGER = "jakarta.enterprise.admin.rest.client";
    public static final Logger logger = Logger.getLogger(REST_CLIENT_LOGGER, SHARED_LOGMESSAGE_RESOURCE);
    @LogMessageInfo(message = "An unsupported encoding was requested: {0}.", cause = "The input supplied can not be encoded in the requested encoding.", action = "Verify that the input is valid.", level = "SEVERE")
    public static final String REST_CLIENT_ENCODING_ERROR = "NCLS-RSCL-00001";
    @LogMessageInfo(message = "An error occurred while processing an XML document.", cause = "The input provided could not be read as an XML document.", action = "Verify that the document provided is a valid XML document.", level = "SEVERE")
    public static final String REST_CLIENT_XML_STREAM_ERROR = "NCLS-RSCL-00002";
    @LogMessageInfo(message = "An I/O exception occurred.", cause = "An error occured while closing an InputStream.", action = "The error is not recoverable.", level = "SEVERE")
    public static final String REST_CLIENT_IO_ERROR = "NCLS-RSCL-00003";
    @LogMessageInfo(message = "An error occurred while processing a JSON object.", cause = "An invalid JSON string was provided and could not be read.", action = "Verify that the JSON string is valid and retry the request.", level = "SEVERE")
    public static final String REST_CLIENT_JSON_ERROR = "NCLS-RSCL-00004";
}
