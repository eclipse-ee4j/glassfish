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

package org.glassfish.admin.amx.impl.mbean;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanInfo;
import javax.management.ObjectName;

import org.glassfish.admin.amx.base.BulkAccess;
import org.glassfish.admin.amx.util.jmx.JMXUtil;

/**
 */
public class BulkAccessImpl extends AMXImplBase // implements BulkAccess
{

    public BulkAccessImpl(final ObjectName parentObjectName) {
        super(parentObjectName, BulkAccess.class);
    }

    public Object[] bulkGetMBeanInfo(final ObjectName[] objectNames) {
        final Object[] infos = new Object[objectNames.length];

        for (int i = 0; i < infos.length; ++i) {
            try {
                infos[i] = getMBeanServer().getMBeanInfo(objectNames[i]);
            } catch (Throwable t) {
                infos[i] = t;
            }
        }
        return (infos);
    }

    public Object[] bulkGetMBeanAttributeInfo(final ObjectName[] objectNames) {
        final Object[] results = new Object[objectNames.length];
        final Object[] mbeanInfos = bulkGetMBeanInfo(objectNames);

        for (int i = 0; i < results.length; ++i) {
            if (mbeanInfos[i] instanceof MBeanInfo) {
                results[i] = ((MBeanInfo) mbeanInfos[i]).getAttributes();
            } else {
                results[i] = mbeanInfos[i];
            }
        }
        return (results);
    }

    public Object[] bulkGetAttributeNames(final ObjectName[] objectNames) {
        final Object[] results = new Object[objectNames.length];
        final Object[] mbeanInfos = bulkGetMBeanInfo(objectNames);

        for (int i = 0; i < results.length; ++i) {
            if (mbeanInfos[i] instanceof MBeanInfo) {
                final MBeanInfo info = (MBeanInfo) mbeanInfos[i];

                results[i] = JMXUtil.getAttributeNames(info.getAttributes());
            } else {
                results[i] = mbeanInfos[i];
            }
        }
        return (results);
    }

    public Object[] bulkGetMBeanOperationInfo(final ObjectName[] objectNames) {
        final Object[] results = new Object[objectNames.length];
        final Object[] mbeanInfos = bulkGetMBeanInfo(objectNames);

        for (int i = 0; i < results.length; ++i) {
            if (mbeanInfos[i] instanceof MBeanInfo) {
                final MBeanInfo info = (MBeanInfo) mbeanInfos[i];

                results[i] = info.getOperations();
            } else {
                results[i] = mbeanInfos[i];
            }
        }
        return (results);
    }

    public Object[] bulkGetAttribute(
            final ObjectName[] objectNames,
            final String attributeName) {
        final Object[] results = new Object[objectNames.length];

        for (int i = 0; i < objectNames.length; ++i) {
            try {
                results[i] = getMBeanServer().getAttribute(objectNames[i], attributeName);
            } catch (Throwable t) {
                results[i] = t;
            }
        }
        return (results);
    }

    public Object[] bulkSetAttribute(
            final ObjectName[] objectNames,
            final Attribute attr) {
        final Object[] results = new Object[objectNames.length];

        for (int i = 0; i < objectNames.length; ++i) {
            try {
                results[i] = null;
                getMBeanServer().setAttribute(objectNames[i], attr);
            } catch (Throwable t) {
                results[i] = t;
            }
        }
        return (results);
    }

    public Object[] bulkGetAttributes(
            final ObjectName[] objectNames,
            final String[] attributeNames) {
        final Object[] results = new Object[objectNames.length];

        // check for empty list; this occurs occassionally and not all MBeans
        // are well-behaved if one asks for an empty list
        if (attributeNames.length != 0) {
            for (int i = 0; i < objectNames.length; ++i) {
                // copy names, in case an MBean messes with the array
                final String[] attributesCopy = attributeNames.clone();

                try {
                    results[i] = getMBeanServer().getAttributes(objectNames[i], attributesCopy);
                } catch (Throwable t) {
                    results[i] = t;
                }
            }
        }
        return (results);
    }

    public Object[] bulkSetAttributes(
            final ObjectName[] objectNames,
            final AttributeList attrs) {
        final Object[] results = new Object[objectNames.length];

        for (int i = 0; i < objectNames.length; ++i) {
            try {
                // avoid alterations to original copy
                final AttributeList attrsCopy = (AttributeList) attrs.clone();

                results[i] = getMBeanServer().setAttributes(objectNames[i], attrsCopy);
            } catch (Throwable t) {
                results[i] = t;
            }
        }
        return (results);
    }

    public Object[] bulkInvoke(
            final ObjectName[] objectNames,
            final String operationName,
            final Object[] args,
            final String[] types) {
        final Object[] results = new Object[objectNames.length];

        for (int i = 0; i < objectNames.length; ++i) {
            try {
                // hopefully the MBean won't alter the args or types
                results[i] = getMBeanServer().invoke(objectNames[i],
                        operationName, args, types);
            } catch (Throwable t) {
                results[i] = t;
            }
        }
        return (results);
    }
}











