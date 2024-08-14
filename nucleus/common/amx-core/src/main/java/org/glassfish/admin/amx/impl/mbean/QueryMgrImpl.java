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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.MBeanInfo;
import javax.management.ObjectName;

import org.glassfish.admin.amx.base.Query;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.core.proxy.AMXProxyHandler;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.RegexUtil;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.jmx.ObjectNameQueryImpl;

/**
 */
public class QueryMgrImpl extends AMXImplBase // implements Query
{

    public QueryMgrImpl(final ObjectName parentObjectName) {
        super(parentObjectName, Query.class);
    }

    public ObjectName[] queryProps(final String props) {
        return queryPattern(Util.newObjectNamePattern(getJMXDomain(), props));
    }

    public ObjectName[] queryTypes(final Set<String> types)
            throws IOException {
        final Set<ObjectName> result = new HashSet<ObjectName>();

        for (final ObjectName objectName : queryAll()) {
            if (types.contains(Util.getTypeProp(objectName))) {
                result.add(objectName);
            }
        }

        return asArray(result);
    }

    public ObjectName[] queryType(final String type) {
        return queryProps(Util.makeTypeProp(type));
    }

    public ObjectName[] queryName(final String name) {
        return queryProps(Util.makeNameProp(name));
    }

    public ObjectName[] queryPattern(final ObjectName pattern) {
        return asArray(JMXUtil.queryNames(getMBeanServer(), pattern, null));
    }

    /**
    @return Set<ObjectName> containing all items that have the matching type and name
     */
    public ObjectName[] queryTypeName(
            final String type,
            final String name) {
        return queryProps(Util.makeRequiredProps(type, name));
    }

    private static String[] convertToRegex(String[] wildExprs) {
        String[] regexExprs = null;

        if (wildExprs != null) {
            regexExprs = new String[wildExprs.length];

            for (int i = 0; i < wildExprs.length; ++i) {
                final String expr = wildExprs[i];

                final String regex = expr == null ? null : RegexUtil.wildcardToJavaRegex(expr);

                regexExprs[i] = regex;
            }
        }
        return (regexExprs);
    }

    private Set<ObjectName> matchWild(
            final Set<ObjectName> candidates,
            final String[] wildKeys,
            final String[] wildValues) {
        final String[] regexNames = convertToRegex(wildKeys);
        final String[] regexValues = convertToRegex(wildValues);

        final ObjectNameQueryImpl query = new ObjectNameQueryImpl();
        final Set<ObjectName> resultSet = query.matchAll(candidates, regexNames, regexValues);

        return resultSet;
    }

    public ObjectName[] queryWildAll(
            final String[] wildKeys,
            final String[] wildValues) {
        final ObjectName[] candidates = queryAll();
        final Set<ObjectName> candidatesSet = SetUtil.newSet(candidates);

        return asArray(matchWild(candidatesSet, wildKeys, wildValues));
    }

    public ObjectName[] queryAll() {
        final ObjectName pat = Util.newObjectNamePattern(getJMXDomain(), "");

        final Set<ObjectName> names = JMXUtil.queryNames(getMBeanServer(), pat, null);

        return asArray(names);
    }

    private final ObjectName[] asArray(final Set<ObjectName> items) {
        return CollectionUtil.toArray(items, ObjectName.class);
    }

    public ObjectName[] getGlobalSingletons() {
        final ObjectName[] all = queryAll();
        final List<ObjectName> globalSingletons = new ArrayList<ObjectName>();

        final ProxyFactory proxyFactory = getProxyFactory();
        for (final ObjectName candidate : all) {
            final MBeanInfo mbeanInfo = proxyFactory.getMBeanInfo(candidate);
            if (mbeanInfo != null && AMXProxyHandler.globalSingleton(mbeanInfo)) {
                globalSingletons.add(candidate);
            }
        }

        return CollectionUtil.toArray(globalSingletons, ObjectName.class);
    }

    public ObjectName getGlobalSingleton(final String type) {
        final ObjectName[] gs = getGlobalSingletons();
        for (final ObjectName objectName : gs) {
            if (Util.getTypeProp(objectName).equals(type)) {
                return objectName;
            }
        }
        return null;
    }

    public ObjectName[] queryDescendants(final ObjectName parentObjectName) {
        final AMXProxy parent = getProxyFactory().getProxy(parentObjectName);

        final List<AMXProxy> items = ParentChildren.hierarchy(parent).asList();

        return Util.toObjectNamesArray(items);
    }
}













