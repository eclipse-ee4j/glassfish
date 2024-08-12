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

import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.annotation.context.WebBundleContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentContext;

import jakarta.servlet.annotation.MultipartConfig;

import java.lang.annotation.Annotation;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.apf.ResultType;
import org.glassfish.web.deployment.descriptor.MultipartConfigDescriptor;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible in handling
 * jakarta.servlet.annotation.MultipartConfig.
 *
 * @author Shing Wai Chan
 */
@Service
@AnnotationHandlerFor(MultipartConfig.class)
public class MultipartConfigHandler extends AbstractWebHandler {
    public MultipartConfigHandler() {
    }

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

    protected HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, WebBundleContext webBundleContext)
            throws AnnotationProcessorException {

        // this is not a web component
        return getInvalidAnnotatedElementHandlerResult(webBundleContext, ainfo);
    }

    private HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, WebComponentDescriptor webCompDesc)
            throws AnnotationProcessorException {

        MultipartConfig multipartConfigAn = (MultipartConfig)ainfo.getAnnotation();
        com.sun.enterprise.deployment.web.MultipartConfig multipartConfig = webCompDesc.getMultipartConfig();
        if (multipartConfig == null) {
            multipartConfig = new MultipartConfigDescriptor();
            webCompDesc.setMultipartConfig(multipartConfig);
        }

        if (multipartConfig.getLocation() == null) {
            multipartConfig.setLocation(multipartConfigAn.location());
        }
        if (multipartConfig.getMaxFileSize() == null) {
            multipartConfig.setMaxFileSize(multipartConfigAn.maxFileSize());
        }
        if (multipartConfig.getMaxRequestSize() == null) {
            multipartConfig.setMaxRequestSize(multipartConfigAn.maxRequestSize());
        }
        if (multipartConfig.getFileSizeThreshold() == null) {
            multipartConfig.setFileSizeThreshold(multipartConfigAn.fileSizeThreshold());
        }

        return getDefaultProcessedResult();
    }

    /**
     * @return an array of annotation types this annotation handler would
     * require to be processed (if present) before it processes it's own
     * annotation type.
     */
    @Override
    public Class<? extends Annotation>[] getTypeDependencies() {
        return getWebAnnotationTypes();
    }
}
