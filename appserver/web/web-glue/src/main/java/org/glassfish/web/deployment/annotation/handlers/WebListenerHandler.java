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

package org.glassfish.web.deployment.annotation.handlers;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.annotation.context.WebBundleContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentContext;
import com.sun.enterprise.deployment.web.AppListenerDescriptor;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.web.deployment.descriptor.AppListenerDescriptorImpl;
import org.jvnet.hk2.annotations.Service;

import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.ServletRequestAttributeListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionIdListener;
import jakarta.servlet.http.HttpSessionListener;
import java.util.logging.Level;

/**
 * This handler is responsible in handling
 * jakarta.servlet.annotation.WebListener.
 *
 * @author Shing Wai Chan
 */
@Service
@AnnotationHandlerFor(WebListener.class)
public class WebListenerHandler extends AbstractWebHandler {
    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(WebFilterHandler.class);

    public WebListenerHandler() {
    }

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            WebComponentContext[] webCompContexts)
            throws AnnotationProcessorException {

        return processAnnotation(ainfo,
                webCompContexts[0].getDescriptor().getWebBundleDescriptor());
    }

    @Override
    protected HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, WebBundleContext webBundleContext)
            throws AnnotationProcessorException {

        return processAnnotation(ainfo, webBundleContext.getDescriptor());
    }

    private HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, WebBundleDescriptor webBundleDesc)
            throws AnnotationProcessorException {

        Class listenerClass = (Class)ainfo.getAnnotatedElement();
        if (!(ServletContextListener.class.isAssignableFrom(listenerClass) ||
                ServletContextAttributeListener.class.isAssignableFrom(listenerClass) ||
                ServletRequestListener.class.isAssignableFrom(listenerClass) ||
                ServletRequestAttributeListener.class.isAssignableFrom(listenerClass) ||
                HttpSessionListener.class.isAssignableFrom(listenerClass) ||
                HttpSessionAttributeListener.class.isAssignableFrom(listenerClass) ||
                HttpSessionIdListener.class.isAssignableFrom(listenerClass))) {
            log(Level.SEVERE, ainfo,
                localStrings.getLocalString(
                "web.deployment.annotation.handlers.needtoimpllistenerinterface",
                "The Class {0} having annotation jakarta.servlet.annotation.WebListener need to implement one of the following interfaces: jakarta.servlet.ServletContextLisener, jakarta.servlet.ServletContextAttributeListener, jakarta.servlet.ServletRequestListener, jakarta.servletServletRequestAttributeListener, jakarta.servlet.http.HttpSessionListener, jakarta.servlet.http.HttpSessionAttributeListener, jakarta.servlet.http.HttpSessionIdListener.",
                listenerClass.getName()));
            return getDefaultFailedResult();
        }

        WebListener listenerAn = (WebListener)ainfo.getAnnotation();
        AppListenerDescriptor appListener =
            new AppListenerDescriptorImpl(listenerClass.getName());
        appListener.setDescription(listenerAn.value());
        webBundleDesc.addAppListenerDescriptor(appListener);
        return getDefaultProcessedResult();
    }
}
