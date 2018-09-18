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

package com.sun.enterprise.connectors.service;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorDescriptorInfo;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.ResourcePrincipal;
import org.glassfish.resourcebase.resources.api.GenericResourceInfo;
import org.glassfish.resourcebase.resources.api.PoolInfo;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * Util classes common to all connector Services
 *
 * @author Srikanth P
 */

public class ConnectorAdminServiceUtils implements ConnectorConstants {

    //Private Constructor, to prevent initialising this class
    private ConnectorAdminServiceUtils() {
    }

    /*
     * Returns a ResourcePrincipal object populated with a pool's
     * default USERNAME and PASSWORD
     *
     * @throws NamingException if poolname lookup fails
     */

    public static ResourcePrincipal getDefaultResourcePrincipal(PoolInfo poolInfo)
            throws NamingException {
        // All this to get the default user name and principal
        ConnectorConnectionPool connectorConnectionPool = null;
        try {
            String jndiNameForPool = getReservePrefixedJNDINameForPool(poolInfo);
            Context ic = ConnectorRuntime.getRuntime().getNamingManager().getInitialContext();
            connectorConnectionPool =
                    (ConnectorConnectionPool) ic.lookup(jndiNameForPool);
        } catch (NamingException ne) {
            throw ne;
        }

        ConnectorDescriptorInfo cdi = connectorConnectionPool.
                getConnectorDescriptorInfo();

        Set mcfConfigProperties = cdi.getMCFConfigProperties();
        Iterator mcfConfPropsIter = mcfConfigProperties.iterator();
        String userName = "";
        String password = "";
        while (mcfConfPropsIter.hasNext()) {
            ConnectorConfigProperty  prop =
                    (ConnectorConfigProperty) mcfConfPropsIter.next();

            if (prop.getName().toUpperCase(Locale.getDefault()).equals("USERNAME") ||
                    prop.getName().toUpperCase(Locale.getDefault()).equals("USER")) {
                userName = prop.getValue();
            } else if (prop.getName().toUpperCase(Locale.getDefault()).equals("PASSWORD")) {
                password = prop.getValue();
            }
        }

        //Now return the ResourcePrincipal
        return new ResourcePrincipal(userName, password);

    }

    private static String getReservePrefixedJNDIName(String prefix, String resourceName) {
        return prefix + resourceName;
    }

    public static String getReservePrefixedJNDINameForPool(PoolInfo poolInfo) {
        String name = getReservePrefixedJNDIName(ConnectorConstants.POOLS_JNDINAME_PREFIX, poolInfo.getName());
        return getScopedName(poolInfo, name);
    }

    private static String getScopedName(GenericResourceInfo resourceInfo, String name){
        if(resourceInfo.getName().startsWith(ConnectorConstants.JAVA_APP_SCOPE_PREFIX)){
            if(!name.startsWith(ConnectorConstants.JAVA_APP_SCOPE_PREFIX)){
                name = ConnectorConstants.JAVA_APP_SCOPE_PREFIX + name;
            }
        }else if (resourceInfo.getName().startsWith(ConnectorConstants.JAVA_MODULE_SCOPE_PREFIX)){
            if(!name.startsWith(ConnectorConstants.JAVA_MODULE_SCOPE_PREFIX)){
                name = ConnectorConstants.JAVA_MODULE_SCOPE_PREFIX + name;
            }
        }
        return name;
    }

    public static String getReservePrefixedJNDINameForDescriptor(String moduleName) {
        return getReservePrefixedJNDIName(ConnectorConstants.DD_PREFIX, moduleName);
    }

    public static String getReservePrefixedJNDINameForResource(String moduleName) {
        return getReservePrefixedJNDIName(ConnectorConstants.RESOURCE_JNDINAME_PREFIX, moduleName);
    }

    public static String getOriginalResourceName(String reservePrefixedJNDIName) {
        String prefix = null;
        if (reservePrefixedJNDIName.startsWith(ConnectorConstants.POOLS_JNDINAME_PREFIX)) {
            prefix = ConnectorConstants.POOLS_JNDINAME_PREFIX;
        } else if (reservePrefixedJNDIName.startsWith(ConnectorConstants.DD_PREFIX)) {
            prefix = ConnectorConstants.DD_PREFIX;
        } else if (reservePrefixedJNDIName.startsWith(ConnectorConstants.RESOURCE_JNDINAME_PREFIX)) {
            prefix = ConnectorConstants.RESOURCE_JNDINAME_PREFIX;
        }
        return ((prefix == null) ? reservePrefixedJNDIName : reservePrefixedJNDIName.substring(prefix.length()));
    }

    //TODO V3 is this right approach ? (just checking '#') ?
    public static boolean isEmbeddedConnectorModule(String moduleName) {
        return (moduleName.indexOf(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER) != -1);
    }

    public static String getApplicationName(String moduleName) {
        if (isEmbeddedConnectorModule(moduleName)) {
            int idx = moduleName.indexOf(
                    ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER);
            return moduleName.substring(0, idx);
        } else {
            return null;
        }
    }

    public static String getConnectorModuleName(String moduleName) {
        if (isEmbeddedConnectorModule(moduleName)) {
            int idx = moduleName.indexOf(
                    ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER);
            return moduleName.substring(idx + 1);
        } else {
            return moduleName;
        }
    }

    public static boolean isJMSRA(String moduleName) {
        return moduleName.equalsIgnoreCase(ConnectorConstants.DEFAULT_JMS_ADAPTER);
    }
}
