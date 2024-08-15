/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.web.jsp;

import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.web.WebModule;

import jakarta.servlet.jsp.tagext.JspTag;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.internal.api.ServerContext;
import org.glassfish.jsp.api.ResourceInjector;
import org.glassfish.web.LogFacade;

/**
 * Implementation of org.glassfish.jsp.api.ResourceInjector
 *
 * @author Jan Luehe
 */
public class ResourceInjectorImpl implements ResourceInjector {

    protected static final Logger _logger = LogFacade.getLogger();

    protected static final ResourceBundle _rb = _logger.getResourceBundle();

    private InjectionManager injectionMgr;
    private JndiNameEnvironment desc;
    private WebModule webModule;

    public ResourceInjectorImpl(WebModule webModule) {
        this.webModule = webModule;
        this.desc = webModule.getWebBundleDescriptor();
        ServerContext serverContext = webModule.getServerContext();
        if (serverContext == null) {
            throw new IllegalStateException(
                    _rb.getString(LogFacade.NO_SERVERT_CONTEXT));
        }
        this.injectionMgr = serverContext.getDefaultServices().getService(
            InjectionManager.class);
    }

    /**
     * Instantiates and injects the given tag handler class.
     *
     * @param clazz the TagHandler class to be instantiated and injected
     *
     * @throws Exception if an error has occurred during instantiation or
     * injection
     */
    public <T extends JspTag> T createTagHandlerInstance(Class<T> clazz)
            throws Exception {
        return webModule.getWebContainer().createTagHandlerInstance(
            webModule, clazz);
    }

    /**
     * Invokes any @PreDestroy methods defined on the instance's class
     * (and super-classes).
     *
     * @param handler The tag handler instance whose PreDestroy-annotated
     * method to call
     */
    public void preDestroy(JspTag handler) {
        if (desc != null) {
            try {
                injectionMgr.invokeInstancePreDestroy(handler, desc);
                injectionMgr.destroyManagedObject(handler);
            } catch (Exception e) {
                String msg = _rb.getString(LogFacade.EXCEPTION_DURING_JSP_TAG_HANDLER_PREDESTROY);
                msg = MessageFormat.format(msg, handler);
                _logger.log(Level.WARNING, msg, e);
            }
        }
    }

}
