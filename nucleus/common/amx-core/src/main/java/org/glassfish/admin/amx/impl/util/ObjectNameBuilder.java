/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.admin.amx.impl.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import static org.glassfish.external.amx.AMX.*;

/**
Class used to build ObjectNameBuilder for AMX MBeans.
 */
public final class ObjectNameBuilder {

    private final MBeanServer mMBeanServer;
    private final String mJMXDomain;
    private final ObjectName mParent;

    public ObjectNameBuilder(final MBeanServer mbeanServer, final String jmxDomain) {
        mMBeanServer = mbeanServer;
        mJMXDomain = jmxDomain;
        mParent = null;
    }

    public ObjectNameBuilder(final MBeanServer mbeanServer, final ObjectName parent) {
        mMBeanServer = mbeanServer;
        if (parent == null) {
            throw new IllegalArgumentException("null ObjecName for parent");
        }

        mParent = parent;
        mJMXDomain = parent.getDomain();
    }

    public String getJMXDomain() {
        return (mJMXDomain);
    }

    public static String makeWild(String props) {
        return (Util.concatenateProps(props, JMXUtil.WILD_PROP));
    }

    /**
    Return a list of ancestors, with the child itself last in the list.
     */
    public static List<ObjectName> getAncestors(
            final MBeanServer server,
            final ObjectName start) {
        //debug( "ObjectNameBuilder.getAncestors(): type = " + start );
        AMXProxy amx = ProxyFactory.getInstance(server).getProxy(start, AMXProxy.class);
        final List<ObjectName> ancestors = new ArrayList<ObjectName>();

        AMXProxy parent = null;
        while ((parent = amx.parent()) != null) {
            ancestors.add(parent.extra().objectName());
            amx = parent;
        }

        Collections.reverse(ancestors);

        ancestors.add(start);

        return ancestors;
    }

    public ObjectName buildChildObjectName(
            final ObjectName parent,
            final String type,
            final String childName) {
        return buildChildObjectName(mMBeanServer, parent, type, childName);
    }

    public ObjectName buildChildObjectName(
            final String type,
            final String childName) {
        return buildChildObjectName(mMBeanServer, mParent, type, childName);
    }

    public ObjectName buildChildObjectName(final Class<?> intf) {
        return buildChildObjectName(mMBeanServer, mParent, intf);
    }

    public ObjectName buildChildObjectName(final Class<?> intf, final String name) {
        return buildChildObjectName(mMBeanServer, mParent, intf, name);
    }

    /**
    Build an ObjectName for an MBean logically contained within the parent MBean.
    The child may be a true child (a subtype), or simply logically contained
    within the parent.

    @param parent
    @param type  type to be used in the ObjectName
    @param pathType   type to be used in the path, null if to be the same as type
    @return ObjectName
     */
    public static ObjectName buildChildObjectName(
            final MBeanServer server,
            final ObjectName parent,
            final String type,
            final String childName) {
        //debug( "ObjectNameBuilder.buildChildObjectName(): type = " + type + ", name = " + childName + ", parent = " + parent );
        String props = Util.makeRequiredProps(type, childName);

        /*
        final String parentPath = PathnameParser.path( parent);
        final String path = PathnameParser.path(parentPath, type, childName);
        final String pathProp = Util.makeProp(PATH_KEY,path);
        props = Util.concatenateProps(pathProp, props);
         */
        final AMXProxy parentProxy = ProxyFactory.getInstance(server).getProxy(parent, AMXProxy.class);
        final String parentPath = parentProxy.path();
        final String parentPathProp = Util.makeProp(PARENT_PATH_KEY, Util.quoteIfNeeded(parentPath));
        props = Util.concatenateProps(parentPathProp, props);

        return JMXUtil.newObjectName(parent.getDomain(), props);
    }

    public static ObjectName buildChildObjectName(
            final MBeanServer server,
            final ObjectName parent,
            final Class<?> intf,
            final String name) {
        final String type = Util.deduceType(intf);
        //final String pathType = Util.getPathType(intf);

        return buildChildObjectName(server, parent, type, name);
    }

    public static ObjectName buildChildObjectName(
            final MBeanServer server,
            final ObjectName parent,
            final Class<?> intf) {
        return buildChildObjectName(server, parent, intf, null);
    }
}









