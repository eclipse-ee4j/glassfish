/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.annotation.context;

import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.annotation.handlers.PostProcessor;
import com.sun.enterprise.deployment.types.HandlerChainContainer;
import com.sun.enterprise.deployment.util.TypeUtil;

import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.impl.ComponentDefinition;
import org.glassfish.deployment.common.Descriptor;

/**
 * Created on January 16, 2005, 5:53 PM
 * @author dochez
 */
public class EjbContext extends ResourceContainerContextImpl {
    private final Method[] methods;
    private final boolean inherited;
    private final ArrayList<PostProcessInfo> postProcessInfos = new ArrayList<>();
    private WebServiceEndpoint endpoint;

    public EjbContext(EjbDescriptor currentEjb, Class<?> ejbClass) {
        super((Descriptor) currentEjb);
        componentClassName = currentEjb.getEjbClassName();
        ComponentDefinition cdef = new ComponentDefinition(ejbClass);
        methods = cdef.getMethods();
        Class<?> superClass = ejbClass.getSuperclass();
        inherited = superClass != null && !Object.class.equals(superClass);
    }


    public EjbDescriptor getDescriptor() {
        return (EjbDescriptor) descriptor;
    }


    public void setDescriptor(EjbDescriptor currentEjb) {
        descriptor = (Descriptor) currentEjb;
    }


    public void setEndpoint(WebServiceEndpoint endpoint) {
        this.endpoint = endpoint;
    }


    public WebServiceEndpoint getEndpoint() {
        return endpoint;
    }


    @Override
    public void endElement(ElementType type, AnnotatedElement element) throws AnnotationProcessorException {
        if (ElementType.TYPE.equals(type)) {
            for (PostProcessInfo ppInfo : postProcessInfos) {
                ppInfo.postProcessor.postProcessAnnotation(ppInfo.ainfo, this);
            }

            // done with processing this class, let's pop this context
            getProcessingContext().popHandler();
        }
    }


    public Class<?> getDeclaringClass(MethodDescriptor md) {
        Method method = md.getMethod(getDescriptor());
        for (Method m : methods) {
            if (TypeUtil.sameMethodSignature(m, method)) {
                return m.getDeclaringClass();
            }
        }
        return null;
    }


    public Method[] getComponentDefinitionMethods() {
        return methods;
    }


    public boolean isInherited() {
        return inherited;
    }


    public void addPostProcessInfo(AnnotationInfo ainfo, PostProcessor<EjbContext> postProcessor) {
        PostProcessInfo ppInfo = new PostProcessInfo();
        ppInfo.ainfo = ainfo;
        ppInfo.postProcessor = postProcessor;
        postProcessInfos.add(ppInfo);
    }


    @Override
    public HandlerChainContainer[] getHandlerChainContainers(boolean serviceSideHandlerChain, Class<?> declaringClass) {
        EjbBundleDescriptor bundleDescriptor = getDescriptor().getEjbBundleDescriptor();
        if (serviceSideHandlerChain) {
            EjbDescriptor[] ejbs = bundleDescriptor.getEjbByClassName(declaringClass.getName());
            List<WebServiceEndpoint> result = new ArrayList<>();
            for (EjbDescriptor ejb : ejbs) {
                result.addAll(bundleDescriptor.getWebServices().getEndpointsImplementedBy(ejb));
            }
            return result.toArray(new HandlerChainContainer[result.size()]);
        }
        List<ServiceReferenceDescriptor> result = new ArrayList<>();
        result.addAll(bundleDescriptor.getEjbServiceReferenceDescriptors());
        return result.toArray(new HandlerChainContainer[result.size()]);
    }

    private static class PostProcessInfo {

        public AnnotationInfo ainfo;
        public PostProcessor<EjbContext> postProcessor;
    }
}
