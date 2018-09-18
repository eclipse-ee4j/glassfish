/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.node.ws;

/**
 * Constants used across weblogic webservices descriptor.
 *
 * @author Rama Pulavarthi
 */
public class WLDescriptorConstants {

    /**
     * Web Logic WebServices descriptor entry
     */
    private static final String WL_WEBSERVICES_ENTRY = "weblogic-webservices.xml";

    /**
     * Web Logic WebServices descriptor entry in a web jar
     */
    public static final String WL_WEB_WEBSERVICES_JAR_ENTRY = "WEB-INF/" + WL_WEBSERVICES_ENTRY;

    /**
     * Web Logic  WebServices descriptor entry in an ejb jar
     */
    public static final String WL_EJB_WEBSERVICES_JAR_ENTRY = "META-INF/" + WL_WEBSERVICES_ENTRY;

    public static final String WL_WEBSERVICES_XML_NS = "http://xmlns.oracle.com/weblogic/weblogic-webservices";

    public static final String WL_WEBSERVICES_XML_SCHEMA = "weblogic-webservices.xsd";

    public static final String WL_WEBSERVICES_SCHEMA_LOCATION = "http://www.oracle.com/technology/weblogic/weblogic-webservices/1.0" + "/" + WL_WEBSERVICES_XML_SCHEMA;


}
