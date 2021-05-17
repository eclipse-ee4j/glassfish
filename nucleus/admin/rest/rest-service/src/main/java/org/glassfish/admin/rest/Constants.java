/*
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
    public static final String INDENT = "  ";
    public static final String JAVA_STRING_TYPE = "java.lang.String";
    public static final String JAVA_BOOLEAN_TYPE = "java.lang.Boolean";
    public static final String JAVA_INT_TYPE = "java.lang.Integer";
    public static final String JAVA_PROPERTIES_TYPE = "java.util.Properties";
    public static final String XSD_STRING_TYPE = "string";
    public static final String XSD_BOOLEAN_TYPE = "boolean";
    public static final String XSD_INT_TYPE = "int";
    public static final String XSD_PROPERTIES_TYPE = "string"; //?
    public static final String TYPE = "type";
    public static final String KEY = "key";
    public static final String OPTIONAL = "optional";
    public static final String DEFAULT_VALUE = "defaultValue";
    public static final String ACCEPTABLE_VALUES = "acceptableValues";
    public static final String DEPRECATED = "deprecated";

    public static final String VAR_PARENT = "$parent";
    public static final String VAR_GRANDPARENT = "$grandparent";

    public static final String ENCODING = "UTF-8";

    public static final String CLIENT_JAVA_PACKAGE = "org.glassfish.admin.rest.client";
    public static final String CLIENT_JAVA_PACKAGE_DIR = CLIENT_JAVA_PACKAGE.replace(".", System.getProperty("file.separator"));

    public static final String CLIENT_PYTHON_PACKAGE = "glassfih.rest";
    public static final String CLIENT_PYTHON_PACKAGE_DIR = CLIENT_PYTHON_PACKAGE.replace(".", System.getProperty("file.separator"));

    public static final String REQ_ATTR_SUBJECT = "SUBJECT";

    public static final String HEADER_LEGACY_FORMAT = "X-GlassFish-3";

    public static final String MEDIA_TYPE = "application";
    public static final String MEDIA_SUB_TYPE = "vnd.oracle.glassfish";
    public static final String MEDIA_TYPE_BASE = MEDIA_TYPE + "/" + MEDIA_SUB_TYPE;
    public static final String MEDIA_TYPE_JSON = MEDIA_TYPE_BASE + "+json";
    public static final MediaType MEDIA_TYPE_JSON_TYPE = new MediaType(MEDIA_TYPE, MEDIA_SUB_TYPE + "+json");
    public static final String MEDIA_TYPE_SSE = MEDIA_TYPE_BASE + "+sse";
}
