/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.jsp.JspApplicationContext;
import jakarta.servlet.jsp.JspFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jasper.runtime.JspApplicationContextImpl;
import org.glassfish.cdi.CDILoggerInfo;
import org.jboss.weld.module.web.el.WeldELContextListener;

/**
 * ServletContextListener implementation that ensures (for Weld applications)
 * the correct Weld EL Resolver and Weld EL Context Listener is used for JSP(s).
 */  
public class WeldContextListener implements ServletContextListener {

    private Logger logger = Logger.getLogger(WeldContextListener.class.getName());

    @Inject
    private BeanManager beanManager;

    /**
     * Stash the Weld EL Resolver and Weld EL Context Listener so it is recognized by JSP.
     */
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        if (null != beanManager) {
             JspApplicationContext jspAppContext = getJspApplicationContext(servletContextEvent);
             jspAppContext.addELResolver(beanManager.getELResolver());

             try {
                 Class weldClass = Class.forName("org.jboss.weld.module.web.el.WeldELContextListener");
                 WeldELContextListener welcl = ( WeldELContextListener ) weldClass.newInstance();
                 jspAppContext.addELContextListener(welcl);
             } catch (Exception e) {
                 logger.log(Level.WARNING,
                            CDILoggerInfo.CDI_COULD_NOT_CREATE_WELDELCONTEXTlISTENER,
                            new Object [] {e});
             }

            ( ( JspApplicationContextImpl ) jspAppContext ).setExpressionFactory(
                beanManager.wrapExpressionFactory(jspAppContext.getExpressionFactory()));
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        if (null != beanManager) {
            beanManager = null;
        }
    }

    protected JspApplicationContext getJspApplicationContext(ServletContextEvent servletContextEvent) {
        return JspFactory.getDefaultFactory().getJspApplicationContext(servletContextEvent.getServletContext());
    }
}
