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

package org.apache.catalina.core;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletSecurityElement;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.catalina.LogFacade;


public class DynamicServletRegistrationImpl
    extends ServletRegistrationImpl
    implements ServletRegistration.Dynamic {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    /**
     * Constructor
     */
    public DynamicServletRegistrationImpl(StandardWrapper wrapper,
            StandardContext ctx) {
        super(wrapper, ctx);
    }

    public void setLoadOnStartup(int loadOnStartup) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(LogFacade.DYNAMIC_SERVLET_REGISTRATION_ALREADY_INIT),
                                              new Object[] {"load-on-startup", wrapper.getName(), ctx.getName()});
            throw new IllegalStateException(msg);
        }

        wrapper.setLoadOnStartup(loadOnStartup);
    }

    public void setAsyncSupported(boolean isAsyncSupported) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(LogFacade.DYNAMIC_SERVLET_REGISTRATION_ALREADY_INIT),
                                              new Object[] {"load-on-startup", wrapper.getName(), ctx.getName()});
            throw new IllegalStateException(msg);
        }

        wrapper.setIsAsyncSupported(isAsyncSupported);
    }

    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        Set<String> emptySet = Collections.emptySet();
        return Collections.unmodifiableSet(emptySet);
    }

    public void setMultipartConfig(MultipartConfigElement mpConfig) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(LogFacade.DYNAMIC_SERVLET_REGISTRATION_ALREADY_INIT),
                    new Object[] {"multipart-config", wrapper.getName(), ctx.getName()});
            throw new IllegalStateException(msg);
        }

        wrapper.setMultipartLocation(mpConfig.getLocation());
        wrapper.setMultipartMaxFileSize(mpConfig.getMaxFileSize());
        wrapper.setMultipartMaxRequestSize(mpConfig.getMaxRequestSize());
        wrapper.setMultipartFileSizeThreshold(
            mpConfig.getFileSizeThreshold());
    }

    public void setRunAsRole(String roleName) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(LogFacade.DYNAMIC_SERVLET_REGISTRATION_ALREADY_INIT),
                    new Object[] {"run-as", wrapper.getName(), ctx.getName()});
            throw new IllegalStateException(msg);
        }

        wrapper.setRunAs(roleName);
    }

    protected void setServletClassName(String className) {
        wrapper.setServletClassName(className);
    }

    protected void setServletClass(Class <? extends Servlet> clazz) {
        wrapper.setServletClass(clazz);
    }

}

