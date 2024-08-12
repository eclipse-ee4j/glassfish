/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.authentication;

import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.ResourcePrincipalDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.logging.LogDomains;

import jakarta.ejb.EJBContext;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.deployment.common.SecurityRoleMapper;
import org.glassfish.deployment.common.SecurityRoleMapperFactory;
import org.glassfish.ejb.api.EJBInvocation;
import org.glassfish.resourcebase.resources.api.PoolInfo;

import static com.sun.appserv.connectors.internal.api.ConnectorConstants.SECURITYMAPMETACHAR;
import static com.sun.logging.LogDomains.RSR_LOGGER;
import static java.util.logging.Level.FINE;
import static org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType.EJB_INVOCATION;
import static org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType.SERVLET_INVOCATION;

/**
 * This class does the functionality of security mapping of the principal and userGroup to the backendPrincipal.
 *
 * @author Srikanth P
 */
public class BasicPasswordAuthenticationService implements AuthenticationService {

    private static final Logger LOG = LogDomains.getLogger(BasicPasswordAuthenticationService.class, RSR_LOGGER, false);

    private final String rarName;
    private final PoolInfo poolInfo;
    private final ConnectorRegistry connectorRegistry = ConnectorRegistry.getInstance();

    /**
     * Constructor
     *
     * @param rarName Name of the rar
     * @param poolInfo Name of the pool.
     */
    public BasicPasswordAuthenticationService(String rarName, PoolInfo poolInfo) {
        this.rarName = rarName;
        this.poolInfo = poolInfo;
        LOG.log(FINE, "Constructor:BasicPasswordAuthenticationService");
    }

    /**
     * Maps the principal to the backendPrincipal
     *
     * @param callerPrincipal Name of the principal to be mapped.
     * @return Mapped Backendprincipal
     */
    @Override
    public ResourcePrincipalDescriptor mapPrincipal(Principal callerPrincipal, Set<Principal> principals) {

        // If no security maps are associated with this pool, return empty
        RuntimeSecurityMap runtimeSecurityMap = connectorRegistry.getRuntimeSecurityMap(poolInfo);
        if (runtimeSecurityMap == null) {
            return null;
        }

        String principalName = callerPrincipal.getName();

        // Create a list of Group Names from group Set
        List<String> groupNames = new ArrayList<>();
        for (Principal principal : principals) {
            // Remove the caller principal (calling user) from the Set.
            if (principal.equals(callerPrincipal)) {
                continue;
            }

            groupNames.add(principal.getName());
        }

        // If webmodule get roles from WebBundle Descriptor
        if (isContainerContextAWebModuleObject()) {
            String roleName = getRoleName(callerPrincipal);
            return doMap(principalName, groupNames, roleName, runtimeSecurityMap);
        }

        return doMap(principalName, groupNames, null, runtimeSecurityMap);
    }

    /**
     * Performs the actual mapping of the principal/userGroup to the backendPrincipal by checking at the connector registry
     * for all the existing mapping. If a map is found the backendPrincipal is returned else null is returned .
     */
    private ResourcePrincipalDescriptor doMap(String principalName, List<String> groupNames, String roleName,  RuntimeSecurityMap runtimeSecurityMap) {

        // Policy:
        // user_1, user_2, ... user_n
        // group_1/role_1, group_2/role_2, ... group_n/role_n
        // user contains *
        // role/group contains *

        Map<String, ResourcePrincipalDescriptor> userNameSecurityMap = runtimeSecurityMap.getUserMap();
        Map<String, ResourcePrincipalDescriptor> groupNameSecurityMap = runtimeSecurityMap.getGroupMap();

        // Check if caller's user-name is preset in the User Map
        if (userNameSecurityMap.containsKey(principalName)) {
            return userNameSecurityMap.get(principalName);
        }

        // Check if caller's role is present in the Group Map
        if (isContainerContextAWebModuleObject() && roleName != null) {
            if (groupNameSecurityMap.containsKey(roleName)) {
                return groupNameSecurityMap.get(roleName);
            }
        }

        // If ejb, use isCallerInRole
        if (isContainerContextAEJBContainerObject() && roleName == null) {
            ComponentInvocation componentInvocation = ConnectorRuntime.getRuntime().getInvocationManager().getCurrentInvocation();
            EJBInvocation ejbInvocation = (EJBInvocation) componentInvocation;
            EJBContext ejbcontext = ejbInvocation.getEJBContext();
            Set<Entry<String, ResourcePrincipalDescriptor>> s = groupNameSecurityMap.entrySet();
            for (Entry<String, ResourcePrincipalDescriptor> mapEntry : s) {
                String key = mapEntry.getKey();
                ResourcePrincipalDescriptor entry = mapEntry.getValue();
                boolean isInRole = false;
                try {
                    isInRole = ejbcontext.isCallerInRole(key);
                } catch (Exception ex) {
                    LOG.log(FINE, "BasicPasswordAuthentication::caller not in role {0}", key);
                }
                if (isInRole) {
                    return entry;
                }
            }
        }

        // Check if caller's group(s) is/are present in the Group Map
        for (String groupName : groupNames) {
            if (groupNameSecurityMap.containsKey(groupName)) {
                return groupNameSecurityMap.get(groupName);
            }
        }

        // Check if user name is * in Security Map
        if (userNameSecurityMap.containsKey(SECURITYMAPMETACHAR)) {
            return userNameSecurityMap.get(SECURITYMAPMETACHAR);
        }

        // Check if role/group name is * in Security Map
        if (groupNameSecurityMap.containsKey(SECURITYMAPMETACHAR)) {
            return groupNameSecurityMap.get(SECURITYMAPMETACHAR);
        }

        return null;
    }

    private String getRoleName(Principal callerPrincipal) {
        WebBundleDescriptor webBundleDescriptor = (WebBundleDescriptor) getComponentEnvManager().getCurrentJndiNameEnvironment();
        SecurityRoleMapperFactory securityRoleMapperFactory = getSecurityRoleMapperFactory();
        SecurityRoleMapper securityRoleMapper = securityRoleMapperFactory.getRoleMapper(webBundleDescriptor.getModuleID());

        Map<String, Subject> map = securityRoleMapper.getRoleToSubjectMapping();
        for (Map.Entry<String, Subject> entry : map.entrySet()) {
            String roleName = entry.getKey();
            Subject subject = entry.getValue();
            Set<Principal> principalSet = subject.getPrincipals();

            if (principalSet.contains(callerPrincipal)) {
                return roleName;
            }
        }

        return "";
    }

    private ComponentEnvManager getComponentEnvManager() {
        return ConnectorRuntime.getRuntime().getComponentEnvManager();
    }

    private ComponentInvocation getCurrentComponentInvocation() {
        return ConnectorRuntime.getRuntime().getInvocationManager().getCurrentInvocation();
    }

    private ComponentInvocation.ComponentInvocationType getCurrentComponentType() {
        return getCurrentComponentInvocation().getInvocationType();
    }

    private boolean isContainerContextAWebModuleObject() {
        return SERVLET_INVOCATION.equals(getCurrentComponentType());
    }

    // TODO V3 use this instead of isContainerContextAContainerObject
    private boolean isContainerContextAEJBContainerObject() {
        return EJB_INVOCATION.equals(getCurrentComponentType());
    }

    public SecurityRoleMapperFactory getSecurityRoleMapperFactory() {
        return ConnectorRuntime.getRuntime().getSecurityRoleMapperFactory();
    }

    @Override
    public String toString() {
        return super.toString() + "[" + rarName + "]";
    }
}
