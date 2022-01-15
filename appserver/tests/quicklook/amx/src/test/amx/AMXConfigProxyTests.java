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

package amxtest;

import org.testng.annotations.*;
import org.testng.Assert;

import javax.management.ObjectName;
import javax.management.AttributeList;
import javax.management.Attribute;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import org.glassfish.admin.amx.core.*;
import org.glassfish.admin.amx.base.*;
import org.glassfish.admin.amx.config.*;
import org.glassfish.admin.amx.monitoring.*;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.ListUtil;
import org.glassfish.admin.amx.util.MapUtil;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.TypeCast;
import org.glassfish.admin.amx.logging.Logging;
import org.glassfish.admin.amx.annotation.*;


/**
    Miscellaneous tests should go into this file, or another one like it.
 */
@Test(
    sequential=false, threadPoolSize=16,
    groups =
    {
        "amx"
    },
    description = "tests for AMXConfigProxy"
)
public final class AMXConfigProxyTests extends AMXTestBase
{
    public AMXConfigProxyTests()
    {
    }

     /** test all MBeans generically */
    @Test
    public void testForBogusConfigAnnotations()
    {
        final List<Class<? extends AMXProxy>> interfaces = getInterfaces().all();

        // AMXConfigProxy sub-interfaces should not use @ManagedAttribute or @ManagedOperation;
        // all such info is derived only from the ConfigBean.
        for( final Class<? extends AMXProxy>  intf : interfaces )
        {
            if ( ! AMXConfigProxy.class.isAssignableFrom(intf) ) continue;

            final Method[] methods = intf.getDeclaredMethods(); // declared methods only
            for( final Method m : methods )
            {
                final ManagedAttribute ma = m.getAnnotation(ManagedAttribute.class);
                final ManagedOperation mo = m.getAnnotation(ManagedOperation.class);
                final String desc = intf.getName() + "." + m.getName() + "()";

                assert ma == null :  "Config MBeans do not support @ManagedAttribute: " + desc;
                assert mo == null :  "Config MBeans do not support @ManagedOperation: " + desc;
            }
        }
    }


    private void _checkDefaultValues(final AMXConfigProxy amxConfig)
    {
        final String objectName = amxConfig.objectName().toString();

        // test the Map keyed by XML attribute name
        final Map<String, String> defaultValuesXML = amxConfig.getDefaultValues(false);
        for (final String attrName : defaultValuesXML.keySet())
        {
            // no default value should ever be null

            assert defaultValuesXML.get(attrName) != null :
            "null value for attribute " + attrName + " in " + objectName;
        }

        // test the Map keyed by AMXProxy attribute name
        final Map<String, String> defaultValuesAMX = amxConfig.getDefaultValues(true);

        assert defaultValuesXML.size() == defaultValuesAMX.size();
        for (final String attrName : defaultValuesAMX.keySet())
        {
            // no default value should ever be null

            assert defaultValuesAMX.get(attrName) != null :
            "null value for attribute " + attrName + " in " + objectName;
        }
    }

    private void _checkAttributeResolver(final AMXConfigProxy amxConfig)
    {
        final Set<String> attrNames = amxConfig.attributeNames();
        for (final String attrName : attrNames)
        {
            final String resolvedValue = amxConfig.resolveAttribute(attrName);
            if (resolvedValue != null)
            {
                // crude check
                assert resolvedValue.indexOf("${") < 0 :
                "Attribute " + attrName + " did not resolve: " + resolvedValue;
            }
        }

        final AttributeList attrsList = amxConfig.resolveAttributes( SetUtil.toStringArray(attrNames) );
        for (final Object o : attrsList)
        {
            final Attribute a = (Attribute) o;
            final String resolvedValue = "" + a.getValue();
            if (resolvedValue != null)
            {
                // crude check
                assert resolvedValue.indexOf("${") < 0 :
                "Attribute " + a.getName() + " did not resolve: " + resolvedValue;
            }
        }
    }


    private Map<String,Object> newPropertyMap(final String name)
    {
        final Map<String,Object>    m = MapUtil.newMap();

        m.put( "Name", name );
        m.put( "Value", name + "-value" );
        m.put( "Description", "desc.for." + name );

        return m;
    }

    private Map<String,Object>[] newPropertyMaps(final String baseName, final int count)
    {
        final Map<String,Object>[] maps = TypeCast.asArray( new Map[count] );
        for( int i = 0; i < count; ++i )
        {
            maps[i] = newPropertyMap(baseName + i);
        }
        return maps;
    }


    private void removeChildSilently( final AMXConfigProxy amx, final String type, final String name )
    {
        if ( name == null )
        {
            if ( amx.child(type) != null )
            {
                try
                {
                    final ObjectName removed = amx.removeChild( type );
                    assert removed == null : "failed (null for ObjectName) to remove child of type \"" + type + "\" from " + amx.objectName();
                    assert amx.child(type) == null : "failed to remove child of type \"" + type + "\" from " + amx.objectName();
                    System.out.println( "Removed stale test config of type " + type );
                }
                catch( final Exception e )
                {
                    e.printStackTrace();
                   assert false : "Unable to remove config of type " + type + ": " + e;
                }
            }
        }
        else if ( amx.childrenMap(type).get(name) != null )
        {
            try
            {
                amx.removeChild( type, name );
                assert amx.childrenMap(type).get(name) == null : "failed to remove child " + type + "," + name + "  from " + amx.objectName();
                System.out.println( "Removed stale test config " + name );
            }
            catch( final Exception e )
            {
                e.printStackTrace();
               assert false : "Unable to remove config " + name + ": " + e;
            }
        }
    }
}




































