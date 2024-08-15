/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Set;

import javax.management.ObjectName;

import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.external.amx.AMXGlassfish;

/**
    MBean providing server-side support for AMX eg for efficiency or other
    reasons.
 */
@AMXMBeanMetadata(type="mbean-tracker",singleton=true, globalSingleton=true, leaf=true)
public interface MBeanTrackerMBean
{
    public static final ObjectName MBEAN_TRACKER_OBJECT_NAME = JMXUtil.newObjectName(AMXGlassfish.DEFAULT.amxSupportDomain(), "type=mbean-tracker");

    /**
        Get all children of the specified MBean.  An empty set is returned
        if no children are found.
    */
    @ManagedOperation
    public Set<ObjectName> getChildrenOf(final ObjectName parent);

    @ManagedOperation
    public ObjectName getParentOf(final ObjectName child);

    @ManagedAttribute
    public boolean getEmitMBeanStatus();

    @ManagedAttribute
    public void setEmitMBeanStatus(boolean emit);
}











