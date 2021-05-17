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

package org.glassfish.deployment.common;

import org.glassfish.deployment.common.SecurityRoleMapper;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author  dochez
 */
@Contract
public interface SecurityRoleMapperFactory {

     /**
     * Returns a RoleMapper corresponding to the AppName.
     * @param The Application Name of this RoleMapper.
     */
    public SecurityRoleMapper getRoleMapper(String appName);

    /**
     * remove the RoleMapping associated with this application
     * @param the application name for this RoleMapper
     */
    public void removeRoleMapper(String appName);

    /**
     * Sets a new RoleMapper for a particular Application
     * @param the application name
     * @param the new role mapper
     */
    public void setRoleMapper(String appName, SecurityRoleMapper rmap);

    /**
     * Returns the appname for this particular context id. Used in
     * context of a web application
     */
    public String getAppNameForContext(String contextId);
    /**
     * stores the appname for this particular context id. Used in the
     * context of a web application
     */
    public void setAppNameForContext(String appName, String contextId);

    /**
     * removes the link between contextId and the appname
     */
    public void removeAppNameForContext(String contextId);
}
