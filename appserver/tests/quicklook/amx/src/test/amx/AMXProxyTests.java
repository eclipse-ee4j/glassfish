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

import java.lang.reflect.Method;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;

import org.glassfish.admin.amx.core.*;
import org.glassfish.admin.amx.base.*;
import org.glassfish.admin.amx.config.*;
//import org.glassfish.admin.amx.j2ee.*;
import org.glassfish.admin.amx.monitoring.*;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.logging.Logging;
import org.glassfish.admin.amx.annotation.*;


/** 
    These tests are designed to exercise the AMXProxyHandler code.
 */
//@Test(groups={"amx"}, description="AMXProxy tests", sequential=false, threadPoolSize=5)
@Test(
    sequential=false, threadPoolSize=10,
    groups =
    {
        "amx"
    },
    description = "test the functionality of AMX dymamic proxy eg AMXProxy"
)
public final class AMXProxyTests extends AMXTestBase
{
    public AMXProxyTests()
    {
    }
    
    private String attrName( final Method m )
    {
        if ( ! JMXUtil.isIsOrGetter(m) ) return null;
        
        return JMXUtil.getAttributeName(m);
    }
    
    
    private <T extends AMXProxy> void testProxyInterface(final AMXProxy amx, Class<T> clazz)
    {
        final List<String> problems = _testProxyInterface(amx, clazz);
        assert problems.size() == 0 : CollectionUtil.toString( problems, "\n" );
    }
    
    
    private <T extends AMXProxy> List<String> _testProxyInterface(final AMXProxy amxIn, Class<T> clazz)
    {
        assert amxIn != null : "_testProxyInterface(): null proxy for class " + clazz.getName();
        
        final List<String> problems = new ArrayList<String>();
        
        final T amx = amxIn.as(clazz);
        
        final String nameProp = amx.nameProp();
        assert Util.getNameProp(amx.objectName()) == nameProp;
        
        assert amx.parentPath() != null;
        assert amx.type() != null;
        assert amx.valid();
        assert amx.childrenSet() != null;
        assert amx.childrenMaps() != null;
        assert amx.attributesMap() != null;
        final Set<String> attrNames = amx.attributeNames();
        assert attrNames != null;
        final ObjectName objectName = amx.objectName();
        assert objectName != null;
        assert amx.extra() != null;
        
        final Extra extra = amx.extra();
        //assert extra.mbeanServerConnection() == getMBeanServerConnection();
        assert extra.proxyFactory() == getProxyFactory();
        assert extra.java().length() >= 100;
        
        assert extra.mbeanInfo() != null;
        assert extra.interfaceName() != null;
        assert extra.genericInterface() != null;
        assert extra.group() != null;
        assert extra.descriptor() != null;
        extra.isInvariantMBeanInfo();   // just call it
        extra.subTypes();   // just call it
        extra.supportsAdoption();   // just call it
        if ( extra.globalSingleton() )
        {
            assert extra.singleton();
        }
            
        
        final Method[] methods = clazz.getMethods();
        final Set<String> attrNamesFromMethods = new HashSet<String>();
        for( final Method m : methods )
        {
            if ( JMXUtil.isIsOrGetter(m) )
            {
                final String attrName = attrName(m);
                final ChildGetter childGetter = m.getAnnotation(ChildGetter.class);
                if ( attrNames.contains(attrName) && childGetter != null )
                {
                    println( "Warning: Attribute " + attrName +
                        " exists in " + objectName + ", but has superfluous @ChildGetter annotation" );
                }
                
                try
                {
                    final Object result = m.invoke( amx, (Object[])null);
                    
                    attrNamesFromMethods.add( attrName(m) );
                }
                catch( final Exception e )
                {
                    e.printStackTrace();
                    problems.add( "Error invoking " + m.getName() + "() on " + objectName + " = " + e );
                }
            }
        }
        if ( clazz != AMXProxy.class && clazz != AMXConfigProxy.class )
        {
            // see whether the interface is missing any getters
            final Set<String> missing = new HashSet<String>(attrNames);
            missing.removeAll(attrNamesFromMethods);
            if ( missing.size() != 0 )
            {
                //println( clazz.getName() + " missing getters attributes: " + missing );
            }
        }
        return problems;
    }

    @Test
    public void testDomainRoot()
    {
        final DomainRoot dr = getDomainRootProxy();
        testProxyInterface( dr, DomainRoot.class );
        
        // sanity check:  see that the various attributes are reachable through its proxy
        assert dr.getAMXReady();
        assert dr.getDebugPort() != null;
        assert dr.getApplicationServerFullVersion() != null;
        assert dr.getInstanceRoot() != null;
        assert dr.getDomainDir() != null;
        assert dr.getConfigDir() != null;
        assert dr.getInstallDir() != null;
        assert dr.getUptimeMillis() != null;

    }

    @Test
    public void testExt()
    {
        testProxyInterface( getExt(), Ext.class );
    }

    @Test
    public void testQuery()
    {
        testProxyInterface( getQueryMgr(), Query.class );
    }

    @Test
    public void testBulkAccess()
    {
        testProxyInterface( getDomainRootProxy().getBulkAccess(), BulkAccess.class );
    }

    @Test
    public void testTools()
    {
        testProxyInterface( getDomainRootProxy().getTools(), Tools.class );
    }

    @Test
    public void testMonitoringRoot()
    {
        testProxyInterface( getDomainRootProxy().getMonitoringRoot(), MonitoringRoot.class );
    }

    @Test
    public void testRuntimeRoot()
    {
        testProxyInterface( getDomainRootProxy().getRuntime(), RuntimeRoot.class );
    }

    @Test
    public void testServerRuntime()
    {
        final RuntimeRoot runtime = getDomainRootProxy().getRuntime();
        final Map<String,ServerRuntime>  serverRuntimes = runtime.getServerRuntime();
        assert serverRuntimes.keySet().size() != 0;
        for( final ServerRuntime sr : serverRuntimes.values() )
        {
            testProxyInterface( sr, ServerRuntime.class );
        }
    }

    @Test
    public void testSystemInfo()
    {
        testProxyInterface( getDomainRootProxy().getSystemInfo(), SystemInfo.class );
    }

    @Test
    public void testLogging()
    {
        testProxyInterface( getDomainRootProxy().getLogging(), Logging.class );
    }

    @Test
    public void testPathnames()
    {
        testProxyInterface( getDomainRootProxy().getPathnames(), Pathnames.class );
    }

    /** test all MBeans generically */
    @Test
    public void testForBogusAnnotations()
    {
        final List<Class<? extends AMXProxy>> interfaces = getInterfaces().all();
        
        for( final Class<? extends AMXProxy>  intf : interfaces )
        {
            final Method[] methods = intf.getMethods();
            for( final Method m : methods )
            {
                final ChildGetter cg = m.getAnnotation(ChildGetter.class);
                final ManagedAttribute ma = m.getAnnotation(ManagedAttribute.class);
                final ManagedOperation mo = m.getAnnotation(ManagedOperation.class);
                final String desc = intf.getName() + "." + m.getName() + "()";
                final int numArgs = m.getParameterTypes().length;
                
                assert ma == null || mo == null :
                    "Can't have both @ManagedAttribute and @ManagedOperation: " + desc;

                if ( cg != null )
                {
                    assert numArgs == 0 :
                        "@ChildGetter cannot be applied to method with arguments: " + desc;
                        
                    assert ma == null && mo == null :
                        "@ManagedAttribute/@ManagedOperation not applicable with @ChildGetter: " + desc;
                }
                
                if ( mo != null )
                {
                    // operations that mimic getters are bad. We can't prevent all such things
                    // but we can object to such oddball usage in an AMX interface
                    if ( numArgs == 0 && m.getName().startsWith("get") )
                    {
                        assert false : "testForBogusAnnotations: @ManagedOperation should be @ManagedAttribute: " + desc;
                    }
                }
            }
        }
    }
    

    
    /** test all MBeans generically */
    @Test(/* GLASSFISH-21214 */enabled=false)
    public void testAllGenerically()
    {
        final Interfaces interfaces = getInterfaces();
        final List<String> problems = new ArrayList<String>();

        for( final AMXProxy amx : getQueryMgr().queryAll() )
        {
            assert amx != null : "testAllGenerically(): null proxy in query list";
            try
            {
                final List<String> p = _testProxyInterface( amx, interfaces.get(amx.type()) );
                problems.addAll(p);
            }
            catch( final Throwable t )
            {
                final Throwable rootCause = ExceptionUtil.getRootCause(t);
                problems.add( rootCause.getMessage() );
            }
        }

        if ( problems.size() != 0 )
        {
            System.out.println( "\nPROBLEMS:\n" + CollectionUtil.toString(problems, "\n\n") );
            assert false : "" + problems;
        }
    }
    
    @Test
    public void testJ2EEDomain() throws ClassNotFoundException
    {
        if ( haveJSR77() )
        {
            testProxyInterface( getDomainRootProxy().getJ2EEDomain(), getJ2EEDomainClass() );
        }
    }
    
}




































