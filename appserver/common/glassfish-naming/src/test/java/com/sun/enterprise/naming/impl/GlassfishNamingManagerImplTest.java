/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.naming.impl.test.ServerExtension;
import com.sun.enterprise.naming.spi.NamingObjectFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.easymock.EasyMock;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.invocation.InvocationManagerImpl;
import org.glassfish.api.naming.JNDIBinding;
import org.glassfish.api.naming.SimpleJndiName;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContext;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType.EJB_INVOCATION;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(ServerExtension.class)
public class GlassfishNamingManagerImplTest {

    private static InitialContext ctx;

    @BeforeAll
    public static void testCreateNewInitialContext() throws Exception {
        ctx = new InitialContext();
    }


    @AfterAll
    public static void closeCtx() throws Exception {
        if (ctx != null) {
            ctx.close();
        }
        ProviderManager.getProviderManager().getTransientContext().close();
    }


    @Test
    public void unmodifiable() throws Exception {
        GlassfishNamingManagerImpl manager = new GlassfishNamingManagerImpl(ctx);
        assertThrows(NamingException.class,
            () -> manager.publishObject(SimpleJndiName.of("java:app/message"), "JNDI is funny", true));
    }


    @Test
    public void bindToEmptySubcontext() throws Exception {
        // a//b is the problem!
        assertThrows(IllegalArgumentException.class, () -> ctx.bind("rmi://a//b/c/d/name1", "Name1"));
    }


    @Test
    public void lookupsInEmpty() throws Exception {
        GlassfishNamingManagerImpl manager = new GlassfishNamingManagerImpl(ctx);
        InvocationManager invManager = createMock(InvocationManager.class);
        ComponentInvocation invocation = createMock(ComponentInvocation.class);
        expect(invManager.getCurrentInvocation()).andReturn(invocation).anyTimes();
        expect(invocation.getAppName()).andReturn("GFNamingMgrTest").anyTimes();
        expect(invocation.getComponentId()).andReturn("component1234").anyTimes();
        replay(invManager, invocation);
        manager.setInvocationManager(invManager);

        assertNotNull(manager.getInitialContext(), "getInitialContext()");
        assertThat(manager.getNameParser(), instanceOf(SerialNameParser.class));

        assertThat(Collections.list(manager.list(SimpleJndiName.of("java:"))), hasSize(0));

        JavaURLContext java = manager.lookupFromComponentNamespace(SimpleJndiName.of("java:"));
        assertThat(java.toString(), endsWith("java:]"));

        JavaURLContext java2 = manager.lookup("component1234", SimpleJndiName.of("java:"));
        assertThat(java2.toString(), endsWith("java:]"));

     // FIXME: added and commented out dmatej
//        SimpleJndiName jdbcJndiName = SimpleJndiName.of("jdbc:derby://localhost:1527/derbyDB;create=true");
//        // class instead of an instance for simplicity.
//        manager.publishObject(jdbcJndiName, DataSource.class, true);
//        assertAll(
//            () -> assertEquals(DataSource.class, ctx.lookup(jdbcJndiName.toName())),
//            () -> assertEquals(DataSource.class, manager.lookupFromComponentNamespace(jdbcJndiName))
//        );

        assertThrows(NamingException.class,
            () -> manager.lookupFromAppNamespace("GFNamingMgrTest", SimpleJndiName.of("doesNotExist"), null));
        assertThrows(NamingException.class, () -> manager.lookupFromModuleNamespace("GFNamingMgrTest", "Module007",
            SimpleJndiName.of("doesNotExist"), null));
    }


    @Test
    public void publishAndUnpublishFoo() throws Exception {
        GlassfishNamingManagerImpl manager = new GlassfishNamingManagerImpl(ctx);
        manager.publishObject(new SimpleJndiName("foo"), "Hello: foo", false);
        manager.unpublishObject(new SimpleJndiName("foo"));
    }


    @Test
    public void bindEmptyInvocationToComponentNamespace() throws Exception {
        GlassfishNamingManagerImpl manager = new GlassfishNamingManagerImpl(ctx);
        InvocationManager invocationManager = new InvocationManagerImpl();
        manager.setInvocationManager(invocationManager);
        ComponentInvocation invocation = new ComponentInvocation("comp1", EJB_INVOCATION, null, null, null);
        invocationManager.preInvoke(invocation);
        manager.bindToComponentNamespace("app1", "mod1", "comp1", false, Collections.emptyList());

        Context subctx = (Context) ctx.lookup("java:comp/env");
        assertThat(subctx, instanceOf(JavaURLContext.class));
    }


    @Test
    public void bindToComponentNamespace() throws Exception {
        GlassfishNamingManagerImpl manager = new GlassfishNamingManagerImpl(ctx);
        InvocationManager invocationManager = new InvocationManagerImpl();
        manager.setInvocationManager(invocationManager);
        ComponentInvocation invocation = new ComponentInvocation("componentIdTest", EJB_INVOCATION, null, "appTest",
            "modTest");
        invocationManager.preInvoke(invocation);

        List<JNDIBinding> bindings = List.of(new Binding("java:comp/env/mmm/MMM", "BindingModuleValue"),
            new Binding("java:app/aaa/AAA", "BindingAppValue"));
        manager.bindToComponentNamespace("appTest", "modTest", "componentIdTest", true, bindings);
        assertEquals("BindingModuleValue", manager.lookupFromComponentNamespace(SimpleJndiName.of("java:comp/env/mmm/MMM")));
        assertEquals("BindingAppValue", manager.lookupFromComponentNamespace(SimpleJndiName.of("java:app/aaa/AAA")));
    }


    @Test
    public void initializeRemoteNamingSupport() throws Exception {
        ORB orb = createMock(ORB.class);
        NamingContext orbNamingContext = createMock(NamingContext.class);
        expect(orb.resolve_initial_references(EasyMock.anyString())).andReturn(orbNamingContext).anyTimes();
        expect(orbNamingContext.resolve(anyObject())).andReturn(null).anyTimes();
        expect(orbNamingContext.bind_new_context(anyObject())).andReturn(orbNamingContext).anyTimes();
        orbNamingContext.bind(anyObject(), anyObject());
        expectLastCall().anyTimes();
        replay(orb, orbNamingContext);
        GlassfishNamingManagerImpl manager = new GlassfishNamingManagerImpl();
        InvocationManager invocationManager = new InvocationManagerImpl();
        manager.setInvocationManager(invocationManager);
        ComponentInvocation invocation = new ComponentInvocation("compCorba", EJB_INVOCATION, null, "appCorba",
            "moduleCorba");
        invocationManager.preInvoke(invocation);
        manager.initializeRemoteNamingSupport(orb);

    }


    /**
     * Tests that we really work with the same instance which supports caching.
     */
    @Test
    public void cachingNamingObjectFactory() throws Exception {
        GlassfishNamingManagerImpl manager = new GlassfishNamingManagerImpl(ctx);
        SimpleJndiName nameFoo = new SimpleJndiName("foo");
        String valueFoo = "Hello: foo";
        manager.publishObject(nameFoo, valueFoo, false);
        assertEquals(valueFoo, ctx.lookup(nameFoo.toString()));

        NamingObjectFactory factory = new NamingObjectFactory() {

            private int counter = 1;

            @Override
            public boolean isCreateResultCacheable() {
                return true;
            }


            @Override
            public String create(Context ic) {
                return "FACTORY_Created: " + counter++;
            }
        };
        SimpleJndiName nameBar = new SimpleJndiName("bar");
        manager.publishObject(nameBar, factory, false);
        assertEquals("FACTORY_Created: 1", ctx.lookup(nameBar.toString()));
        assertEquals("FACTORY_Created: 2", ctx.lookup(nameBar.toString()));
        assertEquals(valueFoo, ctx.lookup(nameFoo.toString()));
        assertAll(
            () -> manager.unpublishObject(nameFoo),
            () -> manager.unpublishObject(nameBar)
        );
    }


    @Test
    public void nonCachingNamingObjectFactory() throws Exception {
        InvocationManager im = new InvocationManagerImpl();
        GlassfishNamingManagerImpl nm = new GlassfishNamingManagerImpl(ctx);
        nm.setInvocationManager(im);

        NamingObjectFactory intFactory = new NamingObjectFactory() {

            private int counter = 1;

            @Override
            public boolean isCreateResultCacheable() {
                return false;
            }


            @Override
            public Integer create(Context ic) {
                return Integer.valueOf(++counter);
            }
        };
        List<Binding> bindings = new ArrayList<>();
        bindings.add(new Binding("java:comp/env/conf/area", intFactory));
        String valueSantaClara = "Santa Clara";
        bindings.add(new Binding("java:comp/env/conf/location", valueSantaClara));

        nm.bindToComponentNamespace("app1", "mod1", "comp1", false, bindings);

        ComponentInvocation inv = new ComponentInvocation("comp1", EJB_INVOCATION, null, null, null);
        im.preInvoke(inv);

        assertEquals(2, ctx.lookup("java:comp/env/conf/area"));
        assertEquals(valueSantaClara, ctx.lookup("java:comp/env/conf/location"));

        NamingObjectFactory floatFactory = new NamingObjectFactory() {

            private int counter = 1;

            @Override
            public boolean isCreateResultCacheable() {
                return false;
            }


            @Override
            public Float create(Context ic) {
                return Float.valueOf(("7" + (++counter)) + "." + 2323);
            }
        };
        List<Binding> bindings2 = new ArrayList<>();
        bindings2.add(new Binding("java:comp/env/conf/area", floatFactory));
        String valueSantaClara14 = "Santa Clara[14]";
        bindings2.add(new Binding("java:comp/env/conf/location", valueSantaClara14));

        nm.bindToComponentNamespace("app1", "mod1", "comp2", false, bindings2);

        inv = new ComponentInvocation("comp2", EJB_INVOCATION, null, null, null);
        im.preInvoke(inv);
        assertEquals(72.2323F, ctx.lookup("java:comp/env/conf/area"));
        assertEquals(valueSantaClara14, ctx.lookup("java:comp/env/conf/location"));

        im.postInvoke(inv);
        nm.unbindComponentObjects("comp1");
    }


    private static class Binding implements JNDIBinding {

        SimpleJndiName logicalName;
        Object value;

        public Binding(String logicalName, Object value) {
            this.logicalName = SimpleJndiName.of(logicalName);
            this.value = value;
        }


        @Override
        public SimpleJndiName getName() {
            return logicalName;
        }


        @Override
        public Object getValue() {
            return value;
        }
    }
}
