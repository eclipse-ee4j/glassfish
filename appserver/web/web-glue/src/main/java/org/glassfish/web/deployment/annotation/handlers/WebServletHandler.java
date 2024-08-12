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

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.annotation.context.WebBundleContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentContext;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.net.URLPattern;

import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

import java.util.Arrays;
import java.util.logging.Level;

import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.apf.ResultType;
import org.glassfish.web.deployment.descriptor.WebComponentDescriptorImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible in handling
 * jakarta.servlet.annotation.WebServlet.
 *
 * @author Shing Wai Chan
 */
@Service
@AnnotationHandlerFor(WebServlet.class)
public class WebServletHandler extends AbstractWebHandler {
    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(WebFilterHandler.class);

    public WebServletHandler() {
    }

    @Override
    public HandlerProcessingResult processAnnotation(AnnotationInfo ainfo)
            throws AnnotationProcessorException {

        AnnotatedElementHandler aeHandler = ainfo.getProcessingContext().getHandler();
        if (aeHandler instanceof WebBundleContext) {
            WebBundleContext webBundleContext = (WebBundleContext)aeHandler;
            WebServlet webServletAn = (WebServlet)ainfo.getAnnotation();
            Class webCompClass = (Class)ainfo.getAnnotatedElement();
            String servletName = getServletName(webServletAn, webCompClass);

            // create a WebComponentDescriptor if there is none
            WebComponentDescriptor webCompDesc =
                webBundleContext.getDescriptor().getWebComponentByCanonicalName(servletName);
            if (webCompDesc == null) {
                createWebComponentDescriptor(servletName, webCompClass,
                        webBundleContext.getDescriptor());
            }
        }

        return super.processAnnotation(ainfo);
    }

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            WebComponentContext[] webCompContexts)
            throws AnnotationProcessorException {

        HandlerProcessingResult result = null;
        for (WebComponentContext webCompContext : webCompContexts) {
            result = processAnnotation(ainfo,
                    webCompContext.getDescriptor());
            if (result.getOverallResult() == ResultType.FAILED) {
                break;
            }
        }
        return result;
    }

    @Override
    protected HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, WebBundleContext webBundleContext)
            throws AnnotationProcessorException {

        WebServlet webServletAn = (WebServlet)ainfo.getAnnotation();

        Class webCompClass = (Class)ainfo.getAnnotatedElement();
        String servletName = getServletName(webServletAn, webCompClass);

        WebComponentDescriptor webCompDesc =
            webBundleContext.getDescriptor().getWebComponentByCanonicalName(servletName);
        if (webCompDesc == null) {
            webCompDesc = createWebComponentDescriptor(servletName, webCompClass,
                    webBundleContext.getDescriptor());
        }

        HandlerProcessingResult result = processAnnotation(ainfo, webCompDesc);
        if (result.getOverallResult() == ResultType.PROCESSED) {
            WebComponentContext webCompContext = new WebComponentContext(webCompDesc);
            // we push the new context on the stack...
            webBundleContext.getProcessingContext().pushHandler(webCompContext);
        }

        return result;
    }

    private HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, WebComponentDescriptor webCompDesc)
            throws AnnotationProcessorException {

        Class webCompClass = (Class)ainfo.getAnnotatedElement();
        if (!HttpServlet.class.isAssignableFrom(webCompClass)) {
            log(Level.SEVERE, ainfo,
                localStrings.getLocalString(
                "web.deployment.annotation.handlers.needtoextend",
                "The Class {0} having annotation {1} need to be a derived class of {2}.",
                new Object[] { webCompClass.getName(), WebServlet.class.getName(), HttpServlet.class.getName() }));
            return getDefaultFailedResult();
        }

        WebServlet webServletAn = (WebServlet)ainfo.getAnnotation();
        String servletName = getServletName(webServletAn, webCompClass);
        if (!servletName.equals(webCompDesc.getCanonicalName())) {
            // skip the processing as it is not for given webCompDesc
            return getDefaultProcessedResult();
        }

        String webCompImpl = webCompDesc.getWebComponentImplementation();
        if (webCompImpl != null && webCompImpl.length() > 0 &&
                (!webCompImpl.equals(webCompClass.getName()) || !webCompDesc.isServlet())) {

            String messageKey = null;
            String defaultMessage = null;

            if (webCompDesc.isServlet()) {
                messageKey = "web.deployment.annotation.handlers.servletimpldontmatch";
                defaultMessage = "The servlet ''{0}'' has implementation ''{1}'' in xml. It does not match with ''{2}'' from annotation @{3}.";
            } else {
                messageKey = "web.deployment.annotation.handlers.servletimpljspdontmatch";
                defaultMessage = "The servlet ''{0}'' is a jsp ''{1}'' in xml. It does not match with ''{2}'' from annotation @{3}.";
            }

            log(Level.SEVERE, ainfo,
                localStrings.getLocalString(messageKey, defaultMessage,
                new Object[] { webCompDesc.getCanonicalName(), webCompImpl, webCompClass.getName(),
                WebServlet.class.getName() }));
            return getDefaultFailedResult();
        }
        webCompDesc.setServlet(true);
        webCompDesc.setWebComponentImplementation(webCompClass.getName());

        if (webCompDesc.getUrlPatternsSet().size() == 0) {
            String[] urlPatterns = webServletAn.urlPatterns();
            if (urlPatterns == null || urlPatterns.length == 0) {
                urlPatterns = webServletAn.value();
            }

            // no url patterns is accepted as it may be defined in top level xml
            boolean validUrlPatterns = true;
            if (urlPatterns != null && urlPatterns.length > 0) {
                for (String up : urlPatterns) {
                    if (!URLPattern.isValid(up)) {
                        validUrlPatterns = false;
                        break;
                    }
                    webCompDesc.addUrlPattern(up);
                }
            }

            if (!validUrlPatterns) {
                String urlPatternString =
                    (urlPatterns != null) ? Arrays.toString(urlPatterns) : "";

                throw new IllegalArgumentException(localStrings.getLocalString(
                        "web.deployment.annotation.handlers.invalidUrlPatterns",
                        "Invalid url patterns for {0}: {1}.",
                        new Object[] { webCompClass, urlPatternString }));
            }
        }

        if (webCompDesc.getLoadOnStartUp() == null) {
            webCompDesc.setLoadOnStartUp(webServletAn.loadOnStartup());
        }

        WebInitParam[] initParams = webServletAn.initParams();
        if (initParams != null && initParams.length > 0) {
            for (WebInitParam initParam : initParams) {
                webCompDesc.addInitializationParameter(
                        new EnvironmentProperty(
                            initParam.name(), initParam.value(),
                            initParam.description()));
            }
        }

        if (webCompDesc.getSmallIconUri() == null) {
            webCompDesc.setSmallIconUri(webServletAn.smallIcon());
        }
        if (webCompDesc.getLargeIconUri() == null) {
            webCompDesc.setLargeIconUri(webServletAn.largeIcon());
        }

        if (webCompDesc.getDescription() == null ||
                webCompDesc.getDescription().length() == 0) {
            webCompDesc.setDescription(webServletAn.description());
        }

        if (webCompDesc.getDisplayName() == null ||
                webCompDesc.getDisplayName().length() == 0) {
            webCompDesc.setDisplayName(webServletAn.displayName());
        }

        if (webCompDesc.isAsyncSupported() == null) {
            webCompDesc.setAsyncSupported(webServletAn.asyncSupported());
        }

        return getDefaultProcessedResult();
    }

    private String getServletName(WebServlet webServletAn, Class<?> webCompClass) {
        String servletName = webServletAn.name();
        if (servletName == null || servletName.length() == 0) {
            servletName = webCompClass.getName();
        }
        return servletName;
    }

    private WebComponentDescriptor createWebComponentDescriptor(String servletName,
            Class<?> webCompClass, WebBundleDescriptor webBundleDescriptor) {

        WebComponentDescriptor webCompDesc = new WebComponentDescriptorImpl();
        webCompDesc.setName(servletName);
        webCompDesc.setCanonicalName(servletName);
        webCompDesc.setServlet(true);
        webCompDesc.setWebComponentImplementation(webCompClass.getName());
        webBundleDescriptor.addWebComponentDescriptor(webCompDesc);
        return webCompDesc;
    }
}
