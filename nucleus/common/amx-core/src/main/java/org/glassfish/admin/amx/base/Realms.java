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

package org.glassfish.admin.amx.base;

import java.util.Map;

import javax.management.MBeanOperationInfo;

import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;



/**
    @since GlassFish V3
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@AMXMBeanMetadata(singleton=true, globalSingleton=true, leaf=true)
public interface Realms extends AMXProxy, Utility, Singleton
{
    /** get the names of all realms */
    @ManagedAttribute
    public String[] getRealmNames();
    @ManagedAttribute
    public String[] getPredefinedAuthRealmClassNames();

    @ManagedAttribute
    public String getDefaultRealmName();
    @ManagedAttribute
    public void   setDefaultRealmName(String realmName);

    @ManagedOperation(impact=MBeanOperationInfo.ACTION)
    public void addUser( String realm, String user, String password, String[] groupList );
    @ManagedOperation(impact=MBeanOperationInfo.ACTION)
    public void updateUser( String realm, String user, String newUser, String password, String[] groupList );
    @ManagedOperation(impact=MBeanOperationInfo.ACTION)
    public void removeUser(String realm, String user);

    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public String[] getUserNames(String realm);
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public String[] getGroupNames(String realm);

    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public Map<String,Object> getUserAttributes(final String realm, final String user);

    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public String[] getGroupNames(String realm, String user);

    /** @return true if the realm implementation support User Management (add,remove,update user) */
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public boolean supportsUserManagement(final String realmName);

    /** @return the username of any user that uses an empty password */
    @ManagedAttribute
    public String getAnonymousUser();
}










