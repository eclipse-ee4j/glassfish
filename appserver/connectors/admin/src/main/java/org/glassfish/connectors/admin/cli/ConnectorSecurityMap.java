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

package org.glassfish.connectors.admin.cli;

import org.glassfish.api.I18n;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.SecurityMap;

import org.glassfish.hk2.api.PerLookup;

import java.util.Collection;
import java.util.List;

/**
 * Create Connector SecurityMap command
 */
@PerLookup
@I18n("create.connector.security.map")
public class ConnectorSecurityMap {

    boolean doesPoolNameExist(String poolName, Collection<ConnectorConnectionPool> ccPools) {
        //check if the poolname exists.If it does not then throw an exception.
        boolean doesPoolExist = false;
        if (ccPools != null) {
            for (ConnectorConnectionPool ccp : ccPools) {
                if (ccp.getName().equals(poolName)) {
                    doesPoolExist = true;
                }
            }
        }
        return doesPoolExist;
    }

    boolean doesMapNameExist(String poolName, String mapname, Collection<ConnectorConnectionPool> ccPools) {
        //check if the mapname exists for the given pool name..
        List<SecurityMap> maps = getAllSecurityMapsForPool(poolName, ccPools);

        boolean doesMapNameExist = false;
        if (maps != null) {
            for (SecurityMap sm : maps) {
                String name = sm.getName();
                if (name.equals(mapname)) {
                    doesMapNameExist = true;
                }
            }
        }
        return doesMapNameExist;
    }

    List<SecurityMap> getAllSecurityMapsForPool(String poolName, Collection<ConnectorConnectionPool> ccPools) {
         List<SecurityMap> securityMaps = null;
         for (ConnectorConnectionPool ccp : ccPools) {
            if (ccp.getName().equals(poolName)) {
                securityMaps = ccp.getSecurityMap();
                break;
            }
         }
         return securityMaps;
    }

    ConnectorConnectionPool getPool(String poolName, Collection<ConnectorConnectionPool> ccPools) {
         ConnectorConnectionPool pool = null;
         for (ConnectorConnectionPool ccp : ccPools) {
            if (ccp.getName().equals(poolName)) {
                pool = ccp;
                break;
            }
         }
         return pool;
    }

    SecurityMap getSecurityMap(String mapName, String poolName, Collection<ConnectorConnectionPool> ccPools) {
        List<SecurityMap> maps = getAllSecurityMapsForPool(poolName, ccPools);
        SecurityMap map = null;
        if (maps != null) {
            for (SecurityMap sm : maps) {
                if (sm.getName().equals(mapName)) {
                    map = sm;
                    break;
                }
            }
        }
        return map;
    }

    boolean isPrincipalExisting(String principal, List<SecurityMap> maps) {
        boolean exists = false;
        List<String> existingPrincipals = null;

        if (maps != null) {
            for (SecurityMap sm : maps) {
                existingPrincipals = sm.getPrincipal();
                if (existingPrincipals != null && principal != null) {
                    for (String ep : existingPrincipals) {
                        if (ep.equals(principal)) {
                            exists = true;
                            break;
                        }
                    }
                }
            }
        }
        return exists;
    }

    boolean isUserGroupExisting(String usergroup, List<SecurityMap> maps) {
        boolean exists = false;
        List<String> existingUserGroups = null;
        if (maps != null) {
            for (SecurityMap sm : maps) {
                existingUserGroups = sm.getUserGroup();
                if (existingUserGroups != null && usergroup != null) {
                    for (String eug : existingUserGroups) {
                        if (eug.equals(usergroup)) {
                            exists = true;
                            break;
                        }
                    }
                }
            }
        }
        return exists;
    }
}
