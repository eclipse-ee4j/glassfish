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

import java.util.Set;

import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

import org.glassfish.admin.amx.annotation.Description;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.annotation.Param;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
Supports various types of queries to find MBeans in the AMX domain only; does not
query all MBeans in all domains, only those in the AMX domain.
<p>
Note that the methods as declared return AMXProxy or collections thereof, but the
actual result consists only of ObjectName; it is the proxy code that auto-converts
to AMXProxy eg invoking with MBeanServerConnection.invoke() will return Set<ObjectName>
but using QueryMgr (as a client-side proxy) will return Set<AMXProxy>.  If ObjectNames
are desirable, use {@link Util#toObjectNames}.
 */
@Taxonomy(stability = Stability.COMMITTED)
@AMXMBeanMetadata(singleton=true, globalSingleton=true, leaf=true)
public interface Query extends AMXProxy, Utility, Singleton
{
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Return all AMX MBeans having any of the specified types")
    public Set<AMXProxy> queryTypes(@Param(name = "type") Set<String> type);

    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Return all AMX MBeans having the specified type")
    public Set<AMXProxy> queryType( @Param(name = "type") String type);

    /**
    Return all {@link AMXProxy} having the specified name.
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Return all AMX MBeans having the specified name")
    public Set<AMXProxy> queryName(  @Param(name = "name") String name);

    /**
    Return all AMX whose type and name matches. Note that the resulting items will necessarily
    have a different Parent (uniqueness invariant within any parent).
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Return all AMX MBeans having the specified type and name")
    public Set<AMXProxy> queryTypeName( @Param(name = "type") String type, @Param(name = "name") String name);

    /**
    Return all AMX whose ObjectName matches the supplied pattern, as defined by the JMX specification.

    @param pattern  an ObjectName containing a pattern as defined by JMX
    @return Set of AMX or empty Set if none
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Return all AMX MBeans matching the specfied pattern")
    public Set<AMXProxy> queryPattern( @Param(name = "pattern") ObjectName pattern);

    /**
    Return all AMX MBeans matching the specfied ObjectName properties
    @param props a String containing one or more name/value ObjectName properties
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Return all AMX MBeans matching the specfied ObjectName properties")
    public Set<AMXProxy> queryProps( @Param(name = "props") String props);

    /**
    Return all AMX MBeans whose whose ObjectName matches all property
    expressions.  Each property expression consists of a key expression, and a value
    expression; an expression which is null is considered a "*" (match all).
    <p>
    Both key and value expressions may be wildcarded with the "*" character,
    which matches 0 or more characters.
    <p>
    Each property expression is matched in turn against the ObjectName. If a match
    fails, the ObjectName is not included in the result.  If all matches succeed, then
    the ObjectName is included.
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Return all AMX MBeans matching all specified ObjectName properties, wildcarded by key and/or value")
    public Set<AMXProxy> queryWildAll( @Param(name = "wildKeys") String[] wildKeys, @Param(name = "wildValues") String[] wildValues);

    /**  Return all AMX MBeans */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    public Set<AMXProxy> queryAll();


    @Description("Return  all MBeans that are global singletons")
    @ManagedAttribute()
    public AMXProxy[] getGlobalSingletons();

    @Description("Return the global singleton of the specified type, or null if not found")
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    public AMXProxy getGlobalSingleton( @Param(name="type") String type);

    @Description("List the parent followed by all descendants, depth-first traversal")
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    public AMXProxy[] queryDescendants( @Param(name="parentObjectName") ObjectName parentObjectName);
}






