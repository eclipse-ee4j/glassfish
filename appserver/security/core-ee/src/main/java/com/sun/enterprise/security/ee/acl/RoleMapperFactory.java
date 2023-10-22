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

package com.sun.enterprise.security.ee.acl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.deployment.common.SecurityRoleMapper;
import org.glassfish.deployment.common.SecurityRoleMapperFactory;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Singleton;

/**
 *
 * @author Jerome Dochez
 */
@Service
@Singleton
public class RoleMapperFactory implements SecurityRoleMapperFactory {

    private Map<String, String> contextToAppName = new ConcurrentHashMap<>();
    private Map<String, SecurityRoleMapper> appNameToRoleMapper = new ConcurrentHashMap<>();


    /**
     * Returns a RoleMapper corresponding to the AppName.
     *
     * @param The Application Name of this RoleMapper.
     *
     */
    @Override
    public SecurityRoleMapper getRoleMapper(String appName) {
        // If the appName is not appname but contextid for
        // web apps then get the appname
        String contextId = appName;
        String appname = getAppNameForContext(appName);

        SecurityRoleMapper securityRoleMapper = null;
        if (appname != null) {
            securityRoleMapper = getRoleMapper(appname, this);
        }
        if (securityRoleMapper == null) {
            securityRoleMapper = getRoleMapper(contextId, this);
        }

        return securityRoleMapper;
    }

    @Override
    public String getAppNameForContext(String contextId) {
        return contextToAppName.get(contextId);
    }

    @Override
    public void setAppNameForContext(String appName, String contextId) {
        contextToAppName.put(contextId, appName);
    }

    @Override
    public void removeAppNameForContext(String contextId) {
        contextToAppName.remove(contextId);
    }

    /**
     * Returns a RoleMapper corresponding to the AppName.
     *
     * @param appName Application Name of this RoleMapper.
     * @return SecurityRoleMapper for the application
     */
    public SecurityRoleMapper getRoleMapper(String appName, SecurityRoleMapperFactory securityRoleMapperFactory) {
        return appNameToRoleMapper.computeIfAbsent(appName, RoleMapper::new);
    }

    /**
     * Set a RoleMapper for the application
     *
     * @param appName Application or module name
     * @param securityRoleMapper <I>SecurityRoleMapper</I> for the application or the module
     */
    @Override
    public void setRoleMapper(String appName, SecurityRoleMapper securityRoleMapper) {
        appNameToRoleMapper.put(appName, securityRoleMapper);
    }

    /**
     * @param appName Application/module name.
     */
    @Override
    public void removeRoleMapper(String appName) {
        if (appNameToRoleMapper.containsKey(appName)) {
            appNameToRoleMapper.remove(appName);
        }
    }
}
