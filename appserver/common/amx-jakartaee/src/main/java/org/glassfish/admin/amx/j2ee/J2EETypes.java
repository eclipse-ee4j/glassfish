/*
 * Copyright (c) 1997, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.j2ee;

import java.util.Set;

import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.external.amx.AMX;

/**
 * See JSR 77.3-1.<br>
 */
public final class J2EETypes {

    private J2EETypes() {
    }

    /**
     * The javax.management.ObjectName property key denoting the type of the MBean.
     */
    public final static String J2EE_TYPE_KEY = "j2eeType";

    /**
     * The ObjectName property key denoting the name of the MBean.
     */
    public final static String NAME_KEY = AMX.NAME_KEY;

    public final static String J2EE_DOMAIN = "J2EEDomain";
    public final static String J2EE_SERVER = "J2EEServer";
    public final static String J2EE_APPLICATION = "J2EEApplication";
    public final static String APP_CLIENT_MODULE = "AppClientModule";
    public final static String EJB_MODULE = "EJBModule";
    public final static String WEB_MODULE = "WebModule";
    public final static String RESOURCE_ADAPTER_MODULE = "ResourceAdapterModule";
    public final static String RESOURCE_ADAPTER = "ResourceAdapter";
    public final static String ENTITY_BEAN = "EntityBean";
    public final static String STATEFUL_SESSION_BEAN = "StatefulSessionBean";
    public final static String STATELESS_SESSION_BEAN = "StatelessSessionBean";
    public final static String SINGLETON_SESSION_BEAN = "SingletonSessionBean";
    public final static String MESSAGE_DRIVEN_BEAN = "MessageDrivenBean";
    public final static String SERVLET = "Servlet";
    public final static String JAVA_MAIL_RESOURCE = "JavaMailResource";
    public final static String JCA_RESOURCE = "JCAResource";
    public final static String JCA_CONNECTION_FACTORY = "JCAConnectionFactory";
    public final static String JCA_MANAGED_CONNECTION_FACTORY = "JCAManagedConnectionFactory";
    public final static String JDBC_RESOURCE = "JDBCResource";
    public final static String JDBC_DATA_SOURCE = "JDBCDataSource";
    public final static String JDBC_DRIVER = "JDBCDriver";
    public final static String JMS_RESOURCE = "JMSResource";
    public final static String JNDI_RESOURCE = "JNDIResource";
    public final static String JTA_RESOURCE = "JTAResource";
    public final static String RMI_IIOP_RESOURCE = "RMI_IIOPResource";
    public final static String URL_RESOURCE = "URLResource";
    public final static String JVM = "JVM";


    /**
     * @since AppServer 9.0
     */
    public final static String WEB_SERVICE_ENDPOINT = "WebServiceEndpoint";

    /**
     * Set consisting of all standard JSR 77 j2eeTypes
     */
    public static final Set<String> ALL_STD =
        SetUtil.newUnmodifiableStringSet(
            J2EE_DOMAIN,
            J2EE_SERVER,
            J2EE_APPLICATION,
            APP_CLIENT_MODULE,
            EJB_MODULE,
            WEB_MODULE,
            RESOURCE_ADAPTER_MODULE,
            ENTITY_BEAN,
            STATEFUL_SESSION_BEAN,
            STATELESS_SESSION_BEAN,
            MESSAGE_DRIVEN_BEAN,
            SERVLET,
            JAVA_MAIL_RESOURCE,
            JCA_RESOURCE,
            JCA_CONNECTION_FACTORY,
            JCA_MANAGED_CONNECTION_FACTORY,
            JDBC_RESOURCE,
            JDBC_DATA_SOURCE,
            JDBC_DRIVER,
            JMS_RESOURCE,
            JNDI_RESOURCE,
            JTA_RESOURCE,
            RMI_IIOP_RESOURCE,
            URL_RESOURCE,
            JVM,
            WEB_SERVICE_ENDPOINT
        );
}
