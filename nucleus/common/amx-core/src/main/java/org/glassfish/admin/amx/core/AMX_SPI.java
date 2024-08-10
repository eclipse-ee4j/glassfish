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

package org.glassfish.admin.amx.core;

import javax.management.ObjectName;

import org.glassfish.admin.amx.annotation.Description;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
 * @deprecated MBean implementations can 'implements AMX_SPI', though it is only behavior
 *             via MBeanInfo and attributes that is actually required.
 */
@Taxonomy(stability = Stability.COMMITTED)
@Deprecated
public interface AMX_SPI {

    /**
     * @return the unencoded name, which could differ from the value of the 'name' property in the ObjectName
     */
    @ManagedAttribute
    @Description("Name of this MBean, can differ from name in ObjectName")
    String getName();


    /** @return the ObjectName of the parent. Must not be null (except for DomainRoot) */
    @ManagedAttribute
    @Description("Parent of this MBean, non-null except for DomainRoot")
    ObjectName getParent();


    /**
     * If no children are possible (a leaf node), an AttributeNotFoundException should be thrown.
     */
    @ManagedAttribute
    @Description("Children of this MBean, in no particular order")
    ObjectName[] getChildren();
}

