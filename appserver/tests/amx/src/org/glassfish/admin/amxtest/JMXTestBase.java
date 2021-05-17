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

package org.glassfish.admin.amxtest;

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.stringifier.SmartStringifier;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 Base class for AMX unit tests.
 */
public class JMXTestBase
        extends junit.framework.TestCase {
    private static MBeanServerConnection _GlobalMBeanServerConnection;
    private static Map<String, Object> sEnv;
    protected final String NEWLINE;

    private static final MBeanServer TEST_MBEAN_SERVER = MBeanServerFactory.newMBeanServer("JMXTestBase_temp");
    /**
        Get an in-JVM MBeanServer for testing.
    */
        protected MBeanServer
    getTestMBeanServer()
    {
        return TEST_MBEAN_SERVER;
    }

    /**
        Set the global MBeanServerConnection.  This is to support testing to a
        remote host.
     */
    public static synchronized void setGlobalConnection(final MBeanServerConnection conn) {
        _GlobalMBeanServerConnection = conn;
    }

    public static synchronized MBeanServerConnection
    getGlobalMBeanServerConnection() {
        return _GlobalMBeanServerConnection;
    }

    public static MBeanServerConnection
    getMBeanServerConnection() {
        return getGlobalMBeanServerConnection();
    }

    protected <T> T
    newProxy(
            final ObjectName target,
            final Class<T> interfaceClass) {
        try {
            assert getGlobalMBeanServerConnection().isRegistered(target);
        }
        catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }

        return interfaceClass.cast(MBeanServerInvocationHandler.newProxyInstance(
                getGlobalMBeanServerConnection(), target, interfaceClass, true));
    }

    public static synchronized Object
    getEnvValue(final String key) {
        return (sEnv == null ? null : sEnv.get(key));
    }

    public static Integer
    getEnvInteger(
            final String key,
            Integer defaultValue) {
        final String s = getEnvString(key, null);
        Integer result = defaultValue;
        if (s != null) {
            result = new Integer(s.trim());
        }

        return (result);
    }

    public static String
    getEnvString(
            final String key,
            final String defaultValue) {
        final String s = (String) getEnvValue(key);

        return (s == null ? defaultValue : s);
    }


    public static Boolean
    getEnvBoolean(
            final String key,
            final Boolean defaultValue) {
        Boolean result = defaultValue;
        final String s = getEnvString(key, null);
        if (s != null) {
            result = Boolean.valueOf(s);
        }

        return (result);
    }


    private static synchronized void
    initEnv() {
        if (sEnv == null) {
            sEnv = new HashMap<String, Object>();
        }
    }

    public static synchronized void
    setEnvValue(
            final String key,
            final Object value) {
        initEnv();
        sEnv.put(key, value);
    }

    public static synchronized void
    setEnvValues(final Map<String, Object> m) {
        initEnv();
        sEnv.putAll(m);
    }


    public JMXTestBase() {
        super("JMXTestBase");

        NEWLINE = StringUtil.NEWLINE();

        checkAssertsOn();
    }

    public JMXTestBase(String name) {
        super(name);
        NEWLINE = StringUtil.NEWLINE();
        checkAssertsOn();
    }


    protected String
    toString(final ObjectName objectName) {
        return JMXUtil.toString(objectName);
    }

    protected String
    toString(final Object o) {
        String result = null;

        if (o instanceof Collection) {
            result = CollectionUtil.toString((Collection) o, "\n");
        } else {
            result = SmartStringifier.toString(o);
        }

        return (result);
    }


    protected static void
    trace(final Object o) {
        System.out.println(SmartStringifier.toString(o));
    }

    protected void
    println(final Object o) {
        System.out.println(SmartStringifier.toString(o));
    }

    protected long
    now() {
        return (System.currentTimeMillis());
    }

    protected final void
    printElapsed(
            final String msg,
            final long start) {
        printVerbose(msg + ": " + (now() - start) + "ms");
    }

    protected final void
    printElapsedIter(
            final String msg,
            final long start,
            final long iterations) {
        printVerbose(msg + "(" + iterations + " iterations): " + (now() - start) + "ms");
    }

    protected final void
    printElapsed(
            final String msg,
            final int numItems,
            final long start) {
        printVerbose(msg + ", " + numItems + " MBeans: " + (now() - start) + "ms");
    }


    protected final String
    quote(final Object o) {
        return (StringUtil.quote(SmartStringifier.toString(o)));
    }


        protected boolean
    getVerbose() {
        /*
        final String value = (String) getEnvValue(PropertyKeys.VERBOSE_KEY);

        return (value != null && Boolean.valueOf(value).booleanValue());
        */
        return false;
    }

    protected void
    printVerbose(final Object o) {
        if (getVerbose()) {
            trace(o);
        }
    }


    protected void
    warning(final String msg) {
        trace("\nWARNING: " + msg + "\n");
    }

    protected void
    failure(final String msg) {
        trace("\nFAILURE: " + msg + "\n");
        assert (false) : msg;
        throw new Error(msg);
    }

    protected void
    checkAssertsOn() {
        try {
            assert (false);
            throw new Error("Assertions must be enabled for unit tests");
        }
        catch (AssertionError a) {
        }
    }


    protected void
    registerMBean(
            Object mbean,
            String name)
            throws MalformedObjectNameException, InstanceAlreadyExistsException,
            NotCompliantMBeanException, MBeanRegistrationException {
           getTestMBeanServer().registerMBean(mbean, new ObjectName(name));
    }
};

