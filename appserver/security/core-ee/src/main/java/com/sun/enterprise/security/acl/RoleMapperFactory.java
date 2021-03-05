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

package com.sun.enterprise.security.acl;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

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
    private Map CONTEXT_TO_APPNAME = new HashMap();
    private Hashtable ROLEMAPPER = new Hashtable();

    /** Creates a new instance of RoleMapperFactory */
    public RoleMapperFactory() {
    }

    /**
     * Returns a RoleMapper corresponding to the AppName.
     *
     * @param The Application Name of this RoleMapper.
     *
     */
    @Override
    public SecurityRoleMapper getRoleMapper(String appName) {
        // if the appName is not appname but contextid for
        // web apps then get the appname
        String contextId = appName;
        String appname = getAppNameForContext(appName);
        SecurityRoleMapper srm = null;
        if (appname != null) {
            srm = getRoleMapper(appname, this);
        }
        if (srm == null) {
            srm = getRoleMapper(contextId, this);
        }
        return srm;
    }

    /**
     * remove the RoleMapping associated with this application
     *
     * @param the application name for this RoleMapper
     *
     * public void removeRoleMapper(String appName) { RoleMapper.removeRoleMapper(appName); }
     */

    /**
     * Sets a new RoleMapper for a particular Application
     *
     * @param the application name
     * @param the new role mapper
     *
     * public void setRoleMapper(String appName, SecurityRoleMapper rmap) { RoleMapper.setRoleMapper(appName, rmap); }
     */

    @Override
    public String getAppNameForContext(String contextId) {
        return (String) CONTEXT_TO_APPNAME.get(contextId);
    }

    @Override
    public void setAppNameForContext(String appName, String contextId) {
        CONTEXT_TO_APPNAME.put(contextId, appName);
    }

    @Override
    public void removeAppNameForContext(String contextId) {
        CONTEXT_TO_APPNAME.remove(contextId);
    }

    /**
     * Returns a RoleMapper corresponding to the AppName.
     *
     * @param appName Application Name of this RoleMapper.
     * @return SecurityRoleMapper for the application
     */
    public RoleMapper getRoleMapper(String appName, SecurityRoleMapperFactory fact) {
        RoleMapper r = (RoleMapper) ROLEMAPPER.get(appName);
        if (r == null) {
            r = new RoleMapper(appName);
            ROLEMAPPER.put(appName, r);
        }
        return r;
    }

    /**
     * Set a RoleMapper for the application
     *
     * @param appName Application or module name
     * @param rmap <I>SecurityRoleMapper</I> for the application or the module
     */
    @Override
    public void setRoleMapper(String appName, SecurityRoleMapper rmap) {
        ROLEMAPPER.put(appName, rmap);
    }

    /**
     * @param appName Application/module name.
     */
    @Override
    public void removeRoleMapper(String appName) {
        if (ROLEMAPPER.containsKey(appName)) {
            ROLEMAPPER.remove(appName);
        }
    }
}
