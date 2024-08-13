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

import java.io.IOException;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
 * @deprecated Direct access to JMX attributes and methods,
 *             These are "straight JMX" with no intervening processing whatsoever.
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@Deprecated
public interface StdAttributesAccess {

    /** Direct access to the MBeanServer, calls conn.getAttribute(objectName, name) */
    Object getAttribute(String name)
        throws InstanceNotFoundException, ReflectionException, MBeanException, AttributeNotFoundException, IOException;


    /** Direct access to the MBeanServer, calls conn.getAttributes(objectName, names) */
    AttributeList getAttributes(String[] names)
        throws InstanceNotFoundException, ReflectionException, IOException;


    /** Direct access to the MBeanServer, calls conn.setAttribute(objectName, attr) */
    void setAttribute(Attribute attr) throws InstanceNotFoundException, ReflectionException, MBeanException,
        AttributeNotFoundException, InvalidAttributeValueException, IOException;


    /** Direct access to the MBeanServer, calls conn.setAttributes(objectName, attrs) */
    AttributeList setAttributes(AttributeList attrs)
        throws InstanceNotFoundException, ReflectionException, IOException;


    /**
     * Direct access to the MBeanServer, calls conn.invoke(objectName, methodName, params, signature)
     */
    Object invoke(String methodName, Object[] params, String[] signature)
        throws InstanceNotFoundException, MBeanException, ReflectionException, IOException;
}

