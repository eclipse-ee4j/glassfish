/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.service;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorDescriptorInfo;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.ResourcePrincipalDescriptor;

import java.util.Locale;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.GenericResourceInfo;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceConstants;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_MODULE;

/**
 * Util classes common to all connector Services
 *
 * @author Srikanth P
 */
public class ConnectorAdminServiceUtils implements ConnectorConstants {

    private ConnectorAdminServiceUtils() {
        // Private Constructor, to prevent initialising this class
    }

    /**
     * Returns a ResourcePrincipalDescriptor object populated with a pool's
     * default USERNAME and PASSWORD
     *
     * @throws NamingException if poolname lookup fails
     */
    public static ResourcePrincipalDescriptor getDefaultResourcePrincipal(PoolInfo poolInfo) throws NamingException {
        // All this to get the default user name and principal
        SimpleJndiName jndiNameForPool = getReservePrefixedJNDINameForPool(poolInfo);
        Context ic = ConnectorRuntime.getRuntime().getNamingManager().getInitialContext();
        ConnectorConnectionPool connectorConnectionPool = (ConnectorConnectionPool) ic.lookup(jndiNameForPool.toString());
        ConnectorDescriptorInfo cdi = connectorConnectionPool.getConnectorDescriptorInfo();

        Set<ConnectorConfigProperty> mcfConfigProperties = cdi.getMCFConfigProperties();
        String userName = "";
        String password = "";
        for (ConnectorConfigProperty prop : mcfConfigProperties) {
            if (prop.getName().toUpperCase(Locale.getDefault()).equals("USERNAME")
                || prop.getName().toUpperCase(Locale.getDefault()).equals("USER")) {
                userName = prop.getValue();
            } else if (prop.getName().toUpperCase(Locale.getDefault()).equals("PASSWORD")) {
                password = prop.getValue();
            }
        }

        //Now return the ResourcePrincipalDescriptor
        return new ResourcePrincipalDescriptor(userName, password);

    }

    public static SimpleJndiName getReservePrefixedJNDINameForPool(PoolInfo poolInfo) {
        SimpleJndiName jndiName = poolInfo.getName().removePrefix();
        SimpleJndiName name = getReservePrefixedJNDIName(POOLS_JNDINAME_PREFIX, jndiName);
        return getScopedName(poolInfo, name);
    }

    private static SimpleJndiName getScopedName(GenericResourceInfo resourceInfo, SimpleJndiName name){
        if (resourceInfo.getName().isJavaApp()) {
            if (!name.isJavaApp()) {
                return new SimpleJndiName(JNDI_CTX_JAVA_APP + name);
            }
        } else if (resourceInfo.getName().isJavaModule()) {
            if (!name.isJavaModule()) {
                return new SimpleJndiName(JNDI_CTX_JAVA_MODULE + name);
            }
        }
        return name;
    }

    public static SimpleJndiName getReservePrefixedJNDINameForDescriptor(String moduleName) {
        return getReservePrefixedJNDIName(DD_PREFIX, moduleName);
    }

    public static SimpleJndiName getReservePrefixedJNDINameForResource(String moduleName) {
        return getReservePrefixedJNDIName(RESOURCE_JNDINAME_PREFIX, moduleName);
    }

    private static SimpleJndiName getReservePrefixedJNDIName(String prefix, Comparable<?> resourceName) {
        return new SimpleJndiName(prefix + resourceName);
    }

    //TODO V3 is this right approach ? (just checking '#') ?
    public static boolean isEmbeddedConnectorModule(String moduleName) {
        return moduleName.indexOf(ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER) != -1;
    }


    public static String getApplicationName(String moduleName) {
        if (isEmbeddedConnectorModule(moduleName)) {
            int idx = moduleName.indexOf(ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER);
            return moduleName.substring(0, idx);
        }
        return null;
    }


    public static String getConnectorModuleName(String moduleName) {
        if (isEmbeddedConnectorModule(moduleName)) {
            int idx = moduleName.indexOf(ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER);
            return moduleName.substring(idx + 1);
        }
        return moduleName;
    }

    public static boolean isJMSRA(String moduleName) {
        return moduleName.equalsIgnoreCase(ConnectorConstants.DEFAULT_JMS_ADAPTER);
    }
}
