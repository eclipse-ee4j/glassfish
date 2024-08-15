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

package com.sun.enterprise.connectors.util;

import com.sun.enterprise.connectors.authentication.ConnectorSecurityMap;
import com.sun.enterprise.connectors.authentication.EisBackendPrincipal;
import com.sun.enterprise.connectors.authentication.RuntimeSecurityMap;
import com.sun.enterprise.deployment.ResourcePrincipalDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.glassfish.connectors.config.BackendPrincipal;
import org.glassfish.connectors.config.SecurityMap;

/**
 * This is class performs the task of adding/deleting and updating the
 * security maps to the connector registry.
 *
 * @author Srikanth P
 */

public class SecurityMapUtils {

    public static final String USERMAP = "usermap";
    public static final String GROUPMAP = "groupmap";

    /**
     * Updates the registry with the security map. If a security map already
     * exists it deletes that map completely before adding the mew security map.
     *
     * @param securityMaps Array of securityMaps to be updated.
     * @return Hash Map containing 1 - 1 mappings of principal and
     *         Resource Principal
     */

    public static RuntimeSecurityMap processSecurityMaps(ConnectorSecurityMap[] securityMaps) {
        if (securityMaps == null || securityMaps.length == 0) {
            return new RuntimeSecurityMap();
        }

        HashMap<String, ResourcePrincipalDescriptor> userMap = new HashMap<>();
        HashMap<String, ResourcePrincipalDescriptor> groupMap = new HashMap<>();
        // Add user-backendPrincipal mappings to Map1
        for (ConnectorSecurityMap map : securityMaps) {
            ResourcePrincipalDescriptor principal = generateResourcePrincipal(map);

            List<String> principalNames = map.getPrincipals();
            for (String principalName : principalNames) {
                userMap.put(principalName, principal);
            }

            List<String> groupNames = map.getUserGroups();
            for (String groupName : groupNames) {
                groupMap.put(groupName, principal);
            }
        }
        return new RuntimeSecurityMap(userMap, groupMap);
    }


    public static ConnectorSecurityMap[] getConnectorSecurityMaps(List<SecurityMap> securityMapList) {
        ConnectorSecurityMap[] maps = null;
        maps = new ConnectorSecurityMap[securityMapList.size()];
        for (int i = 0; i < securityMapList.size(); i++) {
            maps[i] = convertSecurityMapConfigBeanToSecurityMap(securityMapList.get(i));
        }
        return maps;
    }


    private static ConnectorSecurityMap convertSecurityMapConfigBeanToSecurityMap(SecurityMap securityMap) {
        String name = securityMap.getName();
        List<String> principalList = new ArrayList<>();
        for (String p : securityMap.getPrincipal()) {
            principalList.add(p);
        }

        List<String> userGroupList = new ArrayList<>();
        for (String g : securityMap.getUserGroup()) {
            userGroupList.add(g);
        }
        EisBackendPrincipal backendPrincipal = transformBackendPrincipal(securityMap.getBackendPrincipal());
        return new ConnectorSecurityMap(name, principalList, userGroupList, backendPrincipal);
    }


    /**
     * Creates the ResourcePrincipalDescriptor object from the given securityMap
     *
     * @param securityMap SecurityMap
     * @return created ResourcePrincipalDescriptor object
     */
    private static ResourcePrincipalDescriptor generateResourcePrincipal(ConnectorSecurityMap securityMap) {
        EisBackendPrincipal backendPrincipal = securityMap.getBackendPrincipal();
        String userName = backendPrincipal.getUserName();
        String password = backendPrincipal.getPassword();
        return new ResourcePrincipalDescriptor(userName, password);
    }


    private static EisBackendPrincipal transformBackendPrincipal(BackendPrincipal principal) {
        String userName = principal.getUserName();
        String password = principal.getPassword();
        return new EisBackendPrincipal(userName, password);
    }
}
