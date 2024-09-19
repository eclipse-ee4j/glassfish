/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.core.MediaType;

/**
 * REST Interface Constants
 *
 * @author Rajeshwar Paitl
 */
public interface Constants {
    String INDENT = "  ";
    String JAVA_STRING_TYPE = "java.lang.String";
    String JAVA_BOOLEAN_TYPE = "java.lang.Boolean";
    String JAVA_INT_TYPE = "java.lang.Integer";
    String JAVA_PROPERTIES_TYPE = "java.util.Properties";
    String XSD_STRING_TYPE = "string";
    String XSD_BOOLEAN_TYPE = "boolean";
    String XSD_INT_TYPE = "int";
    String XSD_PROPERTIES_TYPE = "string"; //?
    String TYPE = "type";
    String KEY = "key";
    String OPTIONAL = "optional";
    String DEFAULT_VALUE = "defaultValue";
    String ACCEPTABLE_VALUES = "acceptableValues";
    String DEPRECATED = "deprecated";

    String VAR_PARENT = "$parent";
    String VAR_GRANDPARENT = "$grandparent";

    String CLIENT_JAVA_PACKAGE = "org.glassfish.admin.rest.client";
    String CLIENT_JAVA_PACKAGE_DIR = CLIENT_JAVA_PACKAGE.replace(".", System.getProperty("file.separator"));

    String CLIENT_PYTHON_PACKAGE = "glassfih.rest";
    String CLIENT_PYTHON_PACKAGE_DIR = CLIENT_PYTHON_PACKAGE.replace(".", System.getProperty("file.separator"));

    String REQ_ATTR_SUBJECT = "SUBJECT";

    String HEADER_LEGACY_FORMAT = "X-GlassFish-3";

    String MEDIA_TYPE = "application";
    String MEDIA_SUB_TYPE = "vnd.oracle.glassfish";
    String MEDIA_TYPE_BASE = MEDIA_TYPE + "/" + MEDIA_SUB_TYPE;
    String MEDIA_TYPE_JSON = MEDIA_TYPE_BASE + "+json";
    MediaType MEDIA_TYPE_JSON_TYPE = new MediaType(MEDIA_TYPE, MEDIA_SUB_TYPE + "+json");
    String MEDIA_TYPE_SSE = MEDIA_TYPE_BASE + "+sse";
}
