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

import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;


/**
	@deprecated Convenience getters for Descriptor values and other metadata from the MBeanInfo.
    These operations do not make a trip to the server.
    See {@link AMXProxy#extra}.
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@Deprecated
public interface MetaGetters
{
    public MBeanInfo mbeanInfo();
    
    /**
        From Descriptor: get the Java classname of the interface for this MBean.
        If it has not been specified, a default generic interface is returned  eg 'AMX'.
		There is no guarantee that the interface exists on the client.
    */
    public String interfaceName();
    
    /**
        From Descriptor: get the generic interface for this MBean
        eg AMXProxy or AMXConfigProxy or (possibly) something else.
        The generic interface is always part of amx-core.
    */
    public Class<? extends AMXProxy> genericInterface();
    
    /** From Descriptor:  true if the MBeanInfo is invariant ("immutable") */
    public boolean isInvariantMBeanInfo();
    
    /** From Descriptor: true if this MBean is a singleton (1 instance within its parent scope) */
    public boolean singleton();
    
    /** From Descriptor: true if this MBean is a global singleton (1 instance within entire domain) */
    public boolean globalSingleton();
    
    /** From Descriptor: Get the *potential* sub types this MBean expects to have */
    public String[]  subTypes();
    
    /** From Descriptor: return true if new children are allowed by external subsystems */
    public boolean  supportsAdoption();
    
    /** From Descriptor: return the group value */
    public String  group();
    
    /** MBeanInfo descriptor */
    public Descriptor  descriptor();
    
    /** Get MBeanOperationInfo for specified attribute name. */
    public MBeanAttributeInfo attributeInfo(  final String attrName);
    
    /** Get MBeanOperationInfo for specified operation. */
    public MBeanOperationInfo operationInfo(final String operationName);
}








