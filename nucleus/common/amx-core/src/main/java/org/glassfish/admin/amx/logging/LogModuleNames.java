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

package org.glassfish.admin.amx.logging;

import java.util.Collections;
import java.util.Set;

import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
        Names of log level modules.
        @see Logging
    @since AppServer 9.0
 */
@Taxonomy(stability = Stability.EXPERIMENTAL)
public class LogModuleNames
{
    protected LogModuleNames()        {}

    public static final String ROOT_KEY                                        = "Root";
    public static final String SERVER_KEY                                = "Server";
    public static final String EJB_CONTAINER_KEY                = "EJBContainer";
    public static final String CMP_CONTAINER_KEY                = "CMPContainer";
    public static final String MDB_CONTAINER_KEY                = "MDBContainer";
    public static final String WEB_CONTAINER_KEY                = "WebContainer";
    public static final String CLASSLOADER_KEY                        = "Classloader";
    public static final String CONFIGURATION_KEY                = "Configuration";
    public static final String NAMING_KEY                                = "Naming";
    public static final String SECURITY_KEY                                = "Security";
    public static final String JTS_KEY                                        = "JTS";
    public static final String JTA_KEY                                        = "JTA";
    public static final String ADMIN_KEY                                = "Admin";
    public static final String DEPLOYMENT_KEY                        = "Deployment";
    public static final String VERIFIER_KEY                                = "Verifier";
    public static final String JAXR_KEY                                        = "JAXR";
    public static final String JAXRPC_KEY                                = "JAXRPC";
    public static final String SAAJ_KEY                                        = "SAAJ";
    public static final String CORBA_KEY                                = "CORBA";
    public static final String JAVAMAIL_KEY                                = "Javamail";
    public static final String JMS_KEY                                        = "JMS";
    public static final String CONNECTOR_KEY                        = "Connector";
    public static final String JDO_KEY                                        = "JDO";
    public static final String CMP_KEY                                        = "CMP";
    public static final String UTIL_KEY                                        = "Util";
    public static final String RESOURCE_ADAPTER_KEY                = "ResourceAdapter";
    public static final String SYNCHRONIZATION_KEY                = "Synchronization";
    public static final String NODE_AGENT_KEY                        = "NodeAgent";

    /**
     */
    public static final Set<String> ALL_NAMES =
        Collections.unmodifiableSet( SetUtil.newSet( new String[]
        {
            ROOT_KEY,
            SERVER_KEY,
            EJB_CONTAINER_KEY,
            CMP_CONTAINER_KEY,
            MDB_CONTAINER_KEY,
            WEB_CONTAINER_KEY,
            CLASSLOADER_KEY,
            CONFIGURATION_KEY,
            NAMING_KEY,
            SECURITY_KEY,
            JTS_KEY,
            JTA_KEY,
            ADMIN_KEY,
            DEPLOYMENT_KEY,
            VERIFIER_KEY,
            JAXR_KEY,
            JAXRPC_KEY,
            SAAJ_KEY,
            CORBA_KEY,
            JAVAMAIL_KEY,
            JMS_KEY,
            CONNECTOR_KEY,
            JDO_KEY,
            CMP_KEY,
            UTIL_KEY,
            RESOURCE_ADAPTER_KEY,
            SYNCHRONIZATION_KEY,
            NODE_AGENT_KEY,
        }));
}

