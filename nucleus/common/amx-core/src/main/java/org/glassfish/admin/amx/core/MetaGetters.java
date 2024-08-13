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

import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;

import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;


/**
 * @deprecated Convenience getters for Descriptor values and other metadata from the MBeanInfo.
 *             These operations do not make a trip to the server.
 *             See {@link AMXProxy#extra}.
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@Deprecated
public interface MetaGetters {

    MBeanInfo mbeanInfo();


    /**
     * From Descriptor: get the Java classname of the interface for this MBean.
     * If it has not been specified, a default generic interface is returned eg 'AMX'.
     * There is no guarantee that the interface exists on the client.
     */
    String interfaceName();


    /**
     * From Descriptor: get the generic interface for this MBean
     * eg AMXProxy or AMXConfigProxy or (possibly) something else.
     * The generic interface is always part of amx-core.
     */
    Class<? extends AMXProxy> genericInterface();


    /** From Descriptor: true if the MBeanInfo is invariant ("immutable") */
    boolean isInvariantMBeanInfo();


    /** From Descriptor: true if this MBean is a singleton (1 instance within its parent scope) */
    boolean singleton();


    /**
     * From Descriptor: true if this MBean is a global singleton (1 instance within entire domain)
     */
    boolean globalSingleton();


    /** From Descriptor: Get the <b>potential</b> sub types this MBean expects to have */
    String[] subTypes();


    /** From Descriptor: return true if new children are allowed by external subsystems */
    boolean supportsAdoption();


    /** From Descriptor: return the group value */
    String group();


    /** MBeanInfo descriptor */
    Descriptor descriptor();


    /** Get MBeanOperationInfo for specified attribute name. */
    MBeanAttributeInfo attributeInfo(String attrName);


    /** Get MBeanOperationInfo for specified operation. */
    MBeanOperationInfo operationInfo(String operationName);
}
