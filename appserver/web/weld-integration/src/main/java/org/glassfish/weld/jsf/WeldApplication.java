/*
 * Copyright (c) 2021, 2023 Contributors to Eclipse Foundation.
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.el.ELManager;
import jakarta.el.ExpressionFactory;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.jsp.JspFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.wasp.runtime.JspApplicationContextImpl;

public class WeldApplication extends ApplicationWrapper {

    private ExpressionFactory expressionFactory;

    public WeldApplication(Application application) {
        super(application);

        BeanManager beanManager = getBeanManager();
        if (beanManager != null) {
            application.addELContextListener(newInstance("org.jboss.weld.module.web.el.WeldELContextListener"));
            application.addELResolver(beanManager.getELResolver());

            expressionFactory = beanManager.wrapExpressionFactory(ELManager.getExpressionFactory());

            JspApplicationContextImpl jspAppContext = (JspApplicationContextImpl)
                JspFactory.getDefaultFactory()
                          .getJspApplicationContext((ServletContext)
                              FacesContext.getCurrentInstance().getExternalContext().getContext());

            jspAppContext.setExpressionFactory(expressionFactory);
        }
    }

    @Override
    public ExpressionFactory getExpressionFactory() {
        if (expressionFactory == null) {
            BeanManager beanManager = getBeanManager();
            if (beanManager != null) {
                expressionFactory = beanManager.wrapExpressionFactory(getWrapped().getExpressionFactory());
            } else {
                expressionFactory = getWrapped().getExpressionFactory();
            }
        }

        return expressionFactory;
    }

    private BeanManager getBeanManager() {
        try {
            return InitialContext.doLookup("java:comp/BeanManager");
        } catch (NamingException e) {
            return null;
        }
    }

    private <T> T newInstance(String className) {
        try {
            return (T) classForName(className).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Cannot instantiate instance of " + className + " with no-argument constructor", e);
        }
    }

    private <T> Class<T> classForName(String name) {
        try {
            if (Thread.currentThread().getContextClassLoader() != null) {
                return (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(name);
            }
            return (Class<T>) Class.forName(name);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new IllegalArgumentException("Cannot load class for " + name, e);
        }
    }
}
