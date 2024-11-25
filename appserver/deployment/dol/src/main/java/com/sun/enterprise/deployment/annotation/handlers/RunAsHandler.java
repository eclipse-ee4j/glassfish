/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.annotation.handlers;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.RunAsIdentityDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.annotation.context.EjbContext;
import com.sun.enterprise.deployment.annotation.context.WebBundleContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentContext;

import jakarta.annotation.security.RunAs;

import java.lang.annotation.Annotation;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.security.common.Role;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the
 * jakarta.annotation.security.RunAs.
 *
 * @author Shing Wai Chan
 */
@Service
@AnnotationHandlerFor(RunAs.class)
public class RunAsHandler extends AbstractCommonAttributeHandler {

    public RunAsHandler() {
    }


    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, EjbContext[] ejbContexts)
        throws AnnotationProcessorException {

        RunAs runAsAn = (RunAs) ainfo.getAnnotation();
        for (EjbContext ejbContext : ejbContexts) {
            EjbDescriptor ejbDesc = ejbContext.getDescriptor();
            // overriden by xml
            if (ejbDesc.getUsesCallerIdentity() != null) {
                continue;
            }
            String roleName = runAsAn.value();
            Role role = new Role(roleName);
            // add Role if not exists
            ejbDesc.getEjbBundleDescriptor().addRole(role);
            RunAsIdentityDescriptor runAsDesc = new RunAsIdentityDescriptor();
            runAsDesc.setRoleName(roleName);
            ejbDesc.setUsesCallerIdentity(false);
            if (ejbDesc.getRunAsIdentity() == null) {
                ejbDesc.setRunAsIdentity(runAsDesc);
            }
        }

        return getDefaultProcessedResult();
    }


    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, WebComponentContext[] webCompContexts)
        throws AnnotationProcessorException {

        RunAs runAsAn = (RunAs) ainfo.getAnnotation();
        for (WebComponentContext webCompContext : webCompContexts) {
            WebComponentDescriptor webDesc = webCompContext.getDescriptor();
            // override by xml
            if (webDesc.getRunAsIdentity() != null) {
                continue;
            }
            String roleName = runAsAn.value();
            Role role = new Role(roleName);
            // add Role if not exists
            webDesc.getWebBundleDescriptor().addRole(role);
            RunAsIdentityDescriptor runAsDesc = new RunAsIdentityDescriptor();
            runAsDesc.setRoleName(roleName);
            webDesc.setRunAsIdentity(runAsDesc);
        }

        return getDefaultProcessedResult();
    }


    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, WebBundleContext webBundleContext)
        throws AnnotationProcessorException {
        return getInvalidAnnotatedElementHandlerResult(ainfo.getProcessingContext().getHandler(), ainfo);
    }


    @Override
    public Class<? extends Annotation>[] getTypeDependencies() {
        return getEjbAndWebAnnotationTypes();
    }
}
