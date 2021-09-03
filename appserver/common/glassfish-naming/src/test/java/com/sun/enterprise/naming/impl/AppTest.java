/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.naming.impl;

import com.sun.enterprise.naming.spi.NamingObjectFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.invocation.InvocationManagerImpl;
import org.glassfish.api.naming.JNDIBinding;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AppTest {

    private static final String INITIAL_CONTEXT_TEST_MODE = "com.sun.enterprise.naming.TestMode";

    @Test
    public void testCreateNewInitialContext() throws Exception {
        newInitialContext();
    }


    @Test
    public void testBind() throws Exception {
        InitialContext ic = newInitialContext();
        GlassfishNamingManagerImpl nm = new GlassfishNamingManagerImpl(ic);
        nm.publishObject("foo", "Hello: foo", false);
        nm.unpublishObject("foo");
    }


    @Test
    public void testEmptySubContext() throws Exception {
        Context ctx = newInitialContext();
        assertThrows(IllegalArgumentException.class, () -> ctx.bind("rmi://a//b/c/d/name1", "Name1"));
    }


    @Test
    public void testCachingNamingObjectFactory() throws Exception {
        InitialContext ic = newInitialContext();
        GlassfishNamingManagerImpl nm = new GlassfishNamingManagerImpl(ic);
        String nameFoo = "foo";
        String valueFoo = "Hello: foo";
        nm.publishObject(nameFoo, valueFoo, false);
        assertEquals(valueFoo, ic.lookup(nameFoo));

        NamingObjectFactory factory = new NamingObjectFactory() {

            private int counter = 1;

            @Override
            public boolean isCreateResultCacheable() {
                return true;
            }


            @Override
            public Object create(Context ic) {
                return "FACTORY_Created: " + counter++;
            }
        };
        String nameBar = "bar";
        nm.publishObject(nameBar, factory, false);
        assertEquals("FACTORY_Created: 1", ic.lookup(nameBar));
        assertEquals("FACTORY_Created: 2", ic.lookup(nameBar));
        assertEquals(valueFoo, ic.lookup(nameFoo));
        nm.unpublishObject(nameFoo);
        nm.unpublishObject(nameBar);
    }


    @Test
    public void testEmptyJavaCompEnv() throws Exception {
        InitialContext ic = newInitialContext();
        triggerLoadingNamedProxies(ic);
        GlassfishNamingManagerImpl nm = new GlassfishNamingManagerImpl(ic);
        InvocationManager im = new InvocationManagerImpl();
        nm.setInvocationManager(im);

        ComponentInvocation inv = new ComponentInvocation(
            "comp1", ComponentInvocation.ComponentInvocationType.EJB_INVOCATION, null, null, null);
        im.preInvoke(inv);
        nm.bindToComponentNamespace("app1", "mod1", "comp1", false, new ArrayList<Binding>());

        Context ctx = (Context) ic.lookup("java:comp/env");
        assertThat(ctx, instanceOf(JavaURLContext.class));
    }


    @Test
    public void testNonCachingNamingObjectFactory() throws Exception {
        InitialContext ic = newInitialContext();
        triggerLoadingNamedProxies(ic);
        InvocationManager im = new InvocationManagerImpl();
        GlassfishNamingManagerImpl nm = new GlassfishNamingManagerImpl(ic);
        nm.setInvocationManager(im);

        NamingObjectFactory intFactory = new NamingObjectFactory() {

            private int counter = 1;

            @Override
            public boolean isCreateResultCacheable() {
                return false;
            }


            @Override
            public Object create(Context ic) {
                return Integer.valueOf(++counter);
            }
        };
        List<Binding> bindings = new ArrayList<>();
        bindings.add(new Binding("conf/area", intFactory));
        String valueSantaClara = "Santa Clara";
        bindings.add(new Binding("conf/location", valueSantaClara));

        nm.bindToComponentNamespace("app1", "mod1", "comp1", false, bindings);

        ComponentInvocation inv = new ComponentInvocation(
            "comp1", ComponentInvocation.ComponentInvocationType.EJB_INVOCATION, null, null, null);
        im.preInvoke(inv);

        assertEquals(2, ic.lookup("java:comp/env/conf/area"));
        assertEquals(valueSantaClara, ic.lookup("java:comp/env/conf/location"));

        NamingObjectFactory floatFactory = new NamingObjectFactory() {

            private int counter = 1;

            @Override
            public boolean isCreateResultCacheable() {
                return false;
            }


            @Override
            public Object create(Context ic) {
                return Float.valueOf(("7" + (++counter)) + "." + 2323);
            }
        };
        List<Binding> bindings2 = new ArrayList<>();
        bindings2.add(new Binding("conf/area", floatFactory));
        String valueSantaClara14 = "Santa Clara[14]";
        bindings2.add(new Binding("conf/location", valueSantaClara14));

        nm.bindToComponentNamespace("app1", "mod1", "comp2", false, bindings2);

        inv = new ComponentInvocation(
            "comp2", ComponentInvocation.ComponentInvocationType.EJB_INVOCATION, null, null, null);
        im.preInvoke(inv);
        assertEquals(72.2323F, ic.lookup("java:comp/env/conf/area"));
        assertEquals(valueSantaClara14, ic.lookup("java:comp/env/conf/location"));

        im.postInvoke(inv);
        nm.unbindComponentObjects("comp1");
    }


    private InitialContext newInitialContext() throws NamingException {
        // Create a special InitialContext for test purposes
        // Can't just do a new no-arg InitialContext() since
        // this code runs outside a managed environment and the
        // normal behavior would be to try to contact a server.
        // Instead, create an initialcontext with a special
        // property to tell InitialContext to act as if it's
        // running in the server.
        Properties props = new Properties();
        props.setProperty(SerialContext.INITIAL_CONTEXT_TEST_MODE, "true");
        return new InitialContext(props);
    }


    /**
     * Performs an ignored test lookup to trigger the initial loading of named proxies.
     * See NamedNamingObjectManager.checkAndLoadProxies, which creates a default
     * GlassFishNamingManagerImpl. This is not what we want in this test class; we
     * want our own instance of GlassFishNamingManagerImpl that takes our own
     * InvocationManagerImpl.
     * GlassFishNamingManagerImpl(InitialContext) calls JavaURLContext.setNamingManager(this)
     * to save the GlassFishNamingManagerImpl into JavaURLContext, so the last call wins.
     * We want to make sure our test GlassFishNamingManagerImpl is instantiated after the
     * default one.
     */
    private void triggerLoadingNamedProxies(InitialContext ic) {
        try {
            ic.lookup("java:comp/env/to-be-ingored");
        } catch (Exception ignore) {
        }
    }


    private static class Binding implements JNDIBinding {

        String logicalName;
        Object value;

        public Binding(String logicalName, Object value) {
            this.logicalName = "java:comp/env/" + logicalName;
            this.value = value;
        }


        @Override
        public String getName() {
            return logicalName;
        }


        public String getJndiName() {
            return null;
        }


        @Override
        public Object getValue() {
            return value;
        }
    }
}
