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


import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.PathnameConstants;
import org.glassfish.admin.amx.core.PathnameParser;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
    The Pathnames MBean--utilities for working with pathnames and MBeans.
    @since GlassFish V3
    @see PathnameConstants
    @see PathnameParser
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@AMXMBeanMetadata(singleton=true, globalSingleton=true, leaf=true)
public interface Pathnames extends AMXProxy, Utility, Singleton
{
    /** Resolve a path to an ObjectName.  Any aliasing, etc is dealt with.  Return null if failure. */
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public ObjectName  resolvePath( final String path );

    /** Paths that don't resolve result in a null entry */
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public ObjectName[]  resolvePaths( final String[] paths );

    /**
        An efficient way to get the list of MBeans from DomainRoot on down to the specified
        MBean.  The last entry will be the same as the parameter.
        From the ObjectNames one can obtain the path of every ancestor.
        If the MBean does not exist, null will be returned.
     */
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public ObjectName[] ancestors( final ObjectName objectName );

    /**
        Resolves the path to an ObjectName, then calls ancestors(objectName).
        Any aliasing or special handling will be dealt with.
     */
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public ObjectName[] ancestors( final String path );

    /**
        List descendant ObjectNames.
     */
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public ObjectName[]  listObjectNames( final String path, final boolean recursive);

    /**
        List descendant paths.
     */
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public String[] listPaths( final String path, boolean recursive );

    @ManagedAttribute
    public String[] getAllPathnames();

    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public String[] dump( final String path );
}







