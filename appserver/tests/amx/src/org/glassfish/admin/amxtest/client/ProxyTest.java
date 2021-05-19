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

/*
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/client/ProxyTest.java,v 1.10 2007/05/05 05:23:54 tcfujii Exp $
* $Revision: 1.10 $
* $Date: 2007/05/05 05:23:54 $
*/
package org.glassfish.admin.amxtest.client;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import javax.management.AttributeNotFoundException;
import javax.management.ObjectName;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 */
public final class ProxyTest
        extends AMXTestBase {
    public ProxyTest() {
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(false);
    }


    public void
    checkCreateProxy(final ObjectName src)
            throws Exception {
        final AMX proxy = getProxyFactory().getProxy(src, AMX.class);

        Util.getObjectName(proxy);
        proxy.getContainer();
        proxy.getDomainRoot();
    }

    public void
    testCreateAllProxies()
            throws Exception {
        testAll("checkCreateProxy");
    }

    public void
    checkProxiesCached(final ObjectName src)
            throws Exception {
        final AMX proxy = getProxyFactory().getProxy(src, AMX.class);

        assert (proxy == getProxyFactory().getProxy(src, AMX.class));
        assert (proxy.getContainer() == proxy.getContainer());
        assert (proxy.getDomainRoot() == proxy.getDomainRoot());

        final Class interfaceClass = getInterfaceClass(proxy);
        final Method[] proxyMethods = interfaceClass.getMethods();

        for (int methodIdx = 0; methodIdx < proxyMethods.length; ++methodIdx) {
            final Method method = proxyMethods[methodIdx];
            final String methodName = method.getName();

            if (isProxyGetter(method)) {
                // invoke it twice, and verify that the 2nd call results in the same proxy
                //trace( "Invoking: " + method );
                method.invoke(proxy, (Object[]) null);
            }
        }
    }

    public void
    testProxiesCached()
            throws Exception {
        testAll("checkProxiesCached");
    }


    private boolean
    isProxyGetter(final Method method) {
        return (
                method.getName().startsWith(JMXUtil.GET) &&
                        method.getParameterTypes().length == 0 &&
                        AMX.class.isAssignableFrom(method.getReturnType()));
    }

    private boolean
    isChildProxyGetter(final Method method) {
        final Class[] paramTypes = method.getParameterTypes();

        return (
                paramTypes.length == 1 &&
                        paramTypes[0] == String.class &&
                        AMX.class.isAssignableFrom(method.getReturnType()));
    }

    private boolean
    isProxiesGetter(final Method method) {
        return (
                method.getParameterTypes().length == 0 &&
                        Set.class.isAssignableFrom(method.getReturnType()));
    }


    private String
    getProxyGetterName(final String getterName) {
        final int baseLength = getterName.length() - "ObjectName".length();
        final String baseName = getterName.substring(0, baseLength);

        return (baseName + "Proxy");
    }


    public void
    testProxyInterfaceIsAMX()
            throws Exception {/*
        final long    start    = now();
        final TypeInfos    infos    = TypeInfos.getInstance();

        final Iterator    iter    = infos.getJ2EETypes().iterator();
        while ( iter.hasNext() )
        {
            final TypeInfo    info    = infos.getInfo( (String)iter.next() );
            final Class    proxyClass    = info.getInterface();

            if ( ! AMX.class.isAssignableFrom( proxyClass ) )
            {
                warning( "Proxy interface does not extend AMX: " + proxyClass.getName() );
            }
        }
        printElapsed( "testProxyInterfaceNameConsistent", start );
        */
    }


    /**
     Verify that every getXXX() method can be called (those without parameters).
     */
    public void
    testProxyGetters(final AMX proxy)
            throws ClassNotFoundException {
        final Method[] methods = getInterfaceClass(proxy).getMethods();

        final List<Method> failedMethods = new ArrayList<Method>();
        final List<Throwable> exceptions = new ArrayList<Throwable>();

        final long start = now();

        for (int methodIdx = 0; methodIdx < methods.length; ++methodIdx) {
            final Method method = methods[methodIdx];
            final String methodName = method.getName();
            final Class[] parameterTypes = method.getParameterTypes();

            if (methodName.startsWith(JMXUtil.GET) && parameterTypes.length == 0) {
                try {
                    final Object result = method.invoke(proxy, (Object[]) null);
                    //trace( methodName + "=" + result);
                }
                catch (Throwable t) {
                    final ObjectName objectName = Util.getObjectName(proxy);
                    if (isRemoteIncomplete(objectName)) {
                        trace("remoteIncomplete: " + objectName);
                    } else {
                        trace("failure: " + methodName + " = " + t.getClass().getName() + " on MBean " + objectName );
                        failedMethods.add(method);
                        exceptions.add(t);
                    }
                }
            }
        }
        final long elapsed = now() - start;
        //printVerbose( "testProxyGetters for: " + Util.getObjectName( proxy ) + " = " + elapsed );

        if (failedMethods.size() != 0) {
            final int numFailed = failedMethods.size();

            trace("\nMBean \"" + Util.getObjectName(proxy) + "\" failed for:");
            for (int i = 0; i < numFailed; ++i) {
                final Method m = (Method) failedMethods.get(i);
                final Throwable t = (Throwable) exceptions.get(i);

                final Throwable rootCause = ExceptionUtil.getRootCause(t);
                final String rootTrace = ExceptionUtil.getStackTrace(rootCause);
                final Class rootCauseClass = rootCause.getClass();

                trace("testProxyGetters: failure from: " + m.getName() + ": " + rootCauseClass.getName());
                if (rootCauseClass != AttributeNotFoundException.class) {
                    trace(rootTrace + "\n");
                }
            }
        }
    }

    public void
    testAllGetters()
            throws Exception {
        final long start = now();

        final Set<AMX> proxies = getAllAMX();
        for (final AMX amx : proxies) {
            testProxyGetters(amx);
        }

        printElapsed("testAllGetters", start);
    }


    public void
    testQueryMgr()
            throws Exception {
        final QueryMgr proxy = (QueryMgr) getQueryMgr();
        Util.getObjectName(proxy);
        proxy.getContainer();
        proxy.getDomainRoot();
    }

    public void
    testDomainRootCachedProxies()
            throws Exception {
        final DomainRoot root = (DomainRoot) getDomainRoot();

        assert (root.getJ2EEDomain() == root.getJ2EEDomain());
        assert (root.getDomainConfig() == root.getDomainConfig());
        assert (root.getQueryMgr() == root.getQueryMgr());
        assert (root.getBulkAccess() == root.getBulkAccess());
        assert (root.getUploadDownloadMgr() == root.getUploadDownloadMgr());
        assert (root.getDottedNames() == root.getDottedNames());

        assert (root.getJ2EEDomain() == root.getJ2EEDomain());
    }

    /**
     This test is designed to check that performance is reasonable and/or
     to detect a change that slows things down drastically.
     public void
     */
    public void
    testProxyTime()
            throws Exception {
        final DomainRoot root = (DomainRoot) getDomainRoot();

        final long start = now();
        for (int i = 0; i < 5; ++i) {
            root.getContainer();
            root.getDomainRoot();

            root.getJ2EEDomain();
            root.getDomainConfig();
            root.getQueryMgr();
            root.getBulkAccess();
            root.getUploadDownloadMgr();
            root.getDottedNames();
        }
        final long elapsed = now() - start;

        // should be < 300 ms, so this is a 10X margin...
        assert (elapsed < 300 * 10 );
    }
}





