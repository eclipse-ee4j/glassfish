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

package org.glassfish.weld.jsf;

import jakarta.el.ELContextListener;
import jakarta.el.ExpressionFactory;
import javax.enterprise.inject.spi.BeanManager;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationWrapper;
import jakarta.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.jsp.JspApplicationContext;
import jakarta.servlet.jsp.JspFactory;


import org.glassfish.weld.util.Util;

import org.apache.jasper.runtime.JspApplicationContextImpl;

public class WeldApplication extends ApplicationWrapper {
   
    private final Application application;
    private ExpressionFactory expressionFactory;
   
    public WeldApplication(Application application) {
        this.application = application;
        BeanManager beanManager = getBeanManager();
        if (beanManager != null) {
            application.addELContextListener(Util.<ELContextListener>newInstance(
                "org.jboss.weld.module.web.el.WeldELContextListener"));
            application.addELResolver(beanManager.getELResolver());
            JspApplicationContext jspAppContext = JspFactory.getDefaultFactory().
                getJspApplicationContext((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext());
            this.expressionFactory = beanManager.wrapExpressionFactory(jspAppContext.getExpressionFactory());
            ((JspApplicationContextImpl)jspAppContext).setExpressionFactory(this.expressionFactory);
        }
    }

    public Application getWrapped() {
        return this.application;
    }

    @Override
    public ExpressionFactory getExpressionFactory() {
        if (this.expressionFactory == null) {
            BeanManager beanManager = getBeanManager();
            if (beanManager != null) {
                this.expressionFactory = beanManager.wrapExpressionFactory(getWrapped().getExpressionFactory());
          } else {
              this.expressionFactory = getWrapped().getExpressionFactory(); 
          }
        }
        return expressionFactory;
    }

    private BeanManager getBeanManager() {
        try {
            InitialContext context = new InitialContext();
            return (BeanManager) context.lookup("java:comp/BeanManager");
        } catch (NamingException e) {
            return null;
        }

    }
}
