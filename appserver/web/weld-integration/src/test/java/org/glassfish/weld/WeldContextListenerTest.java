/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld;

import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.jsp.JspApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.catalina.core.StandardContext;
import org.easymock.EasyMockSupport;
import org.glassfish.wasp.runtime.JspApplicationContextImpl;
import org.jboss.weld.module.web.el.WeldELContextListener;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:j.j.snyder@oracle.com">JJ Snyder</a>
 */
public class WeldContextListenerTest {
    @Test
    public void testcontextInitialized() throws Exception {
        EasyMockSupport mockSupport = new EasyMockSupport();

        ELResolver elResolver = mockSupport.createMock(ELResolver.class);

        ExpressionFactory expressionFactory = mockSupport.createMock(ExpressionFactory.class);
        StandardContext servletContext = new StandardContext();
        servletContext.getServletContext();

        ServletContextEvent servletContextEvent = mockSupport.createMock(ServletContextEvent.class);
        BeanManager beanManager = mockSupport.createMock(BeanManager.class);
        JspApplicationContextImpl jspApplicationContext = new JspApplicationContextImpl(servletContext);

        expect(beanManager.getELResolver()).andReturn(elResolver);
        expect(beanManager.wrapExpressionFactory(isA(ExpressionFactory.class))).andReturn(expressionFactory);

        mockSupport.replayAll();

        WeldContextListener weldContextListener = getWeldContextListener(beanManager, jspApplicationContext);
        weldContextListener.contextInitialized(servletContextEvent);

        assertSame(expressionFactory, jspApplicationContext.getExpressionFactory());
        validateJspApplicationContext(jspApplicationContext, elResolver);

        mockSupport.verifyAll();
        mockSupport.resetAll();

    }

    @Test
    public void testcontextDestroyed() throws Exception {
        EasyMockSupport mockSupport = new EasyMockSupport();

        BeanManager beanManager = mockSupport.createMock(BeanManager.class);
        mockSupport.replayAll();

        WeldContextListener weldContextListener = getWeldContextListener(beanManager, null);

        Class<?> clazz = LocalWeldContextListener.class.getSuperclass();
        Field beanManagerField = clazz.getDeclaredField("beanManager");
        beanManagerField.setAccessible(true);
        assertNotNull(beanManagerField.get(weldContextListener));

        weldContextListener.contextDestroyed(null);
        assertNull(beanManagerField.get(weldContextListener));

        mockSupport.verifyAll();
        mockSupport.resetAll();

    }

    private void validateJspApplicationContext(JspApplicationContextImpl jspApplicationContext, ELResolver elResolver) throws Exception {
        Method getELResolversMethod = JspApplicationContextImpl.class.getDeclaredMethod("getELResolvers");
        getELResolversMethod.setAccessible(true);
        Iterator iterator = (Iterator) getELResolversMethod.invoke(jspApplicationContext);
        Object elResover = iterator.next();
        assertSame(elResover, elResolver);
        assertFalse(iterator.hasNext());

        Field listenersField = JspApplicationContextImpl.class.getDeclaredField("listeners");
        listenersField.setAccessible(true);

        ArrayList listeners = (ArrayList) listenersField.get(jspApplicationContext);
        assertEquals(1, listeners.size());
        assertTrue(listeners.get(0) instanceof WeldELContextListener);
    }

    private WeldContextListener getWeldContextListener(BeanManager beanManager, JspApplicationContext jspApplicationContext)
            throws Exception {
        LocalWeldContextListener localWeldContextListener = new LocalWeldContextListener(jspApplicationContext);
        Class<?> clazz = LocalWeldContextListener.class.getSuperclass();
        Field beanManagerField = clazz.getDeclaredField("beanManager");
        beanManagerField.setAccessible(true);
        beanManagerField.set(localWeldContextListener, beanManager);
        return localWeldContextListener;
    }

    private class LocalWeldContextListener extends WeldContextListener {
        private final JspApplicationContext jspApplicationContext;

        public LocalWeldContextListener(JspApplicationContext jspApplicationContext) {
            super();
            this.jspApplicationContext = jspApplicationContext;
        }

        @Override
        protected JspApplicationContext getJspApplicationContext(ServletContextEvent servletContextEvent) {
            return jspApplicationContext;
        }
    }

}
