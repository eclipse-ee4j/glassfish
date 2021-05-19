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

/*
 * EjbContext.java
 *
 * Created on January 16, 2005, 5:53 PM
 */

package com.sun.enterprise.deployment.annotation.context;

import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.annotation.handlers.PostProcessor;
import com.sun.enterprise.deployment.types.HandlerChainContainer;
import com.sun.enterprise.deployment.types.ServiceReferenceContainer;
import com.sun.enterprise.deployment.util.TypeUtil;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.impl.ComponentDefinition;
import org.glassfish.deployment.common.Descriptor;

/**
 *
 * @author dochez
 */
public class EjbContext extends ResourceContainerContextImpl {
    private WebServiceEndpoint endpoint;
    private Method[] methods;
    private boolean inherited;
    private ArrayList<PostProcessInfo> postProcessInfos =
            new ArrayList<PostProcessInfo>();

    public EjbContext(EjbDescriptor currentEjb, Class ejbClass) {
        super((Descriptor) currentEjb); // FIXME by srini - can we extract intf to avoid this
        componentClassName = currentEjb.getEjbClassName();
        ComponentDefinition cdef = new ComponentDefinition(ejbClass);
        methods = cdef.getMethods();
        Class superClass = ejbClass.getSuperclass();
        inherited = (superClass != null && !Object.class.equals(superClass));
    }

    public EjbDescriptor getDescriptor() {
        return (EjbDescriptor)descriptor;
    }

    public void setDescriptor(EjbDescriptor currentEjb) {
        descriptor = (Descriptor) currentEjb;  // FIXME by srini - can we extract intf to avoid this
    }

    public void setEndpoint(WebServiceEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public WebServiceEndpoint getEndpoint() {
        return endpoint;
    }

    public void endElement(ElementType type, AnnotatedElement element)
            throws AnnotationProcessorException {

        if (ElementType.TYPE.equals(type)) {
            for (PostProcessInfo ppInfo : postProcessInfos) {
                 ppInfo.postProcessor.postProcessAnnotation(
                         ppInfo.ainfo, this);
            }

            // done with processing this class, let's pop this context
            getProcessingContext().popHandler();
        }
    }

    public Class getDeclaringClass(MethodDescriptor md) {
        Method method = md.getMethod(getDescriptor());
        Class declaringClass = null;
        for (Method m : methods) {
            if (TypeUtil.sameMethodSignature(m, method)) {
                declaringClass = m.getDeclaringClass();
            }
        }
        return declaringClass;
    }

    public Method[] getComponentDefinitionMethods() {
        return methods;
    }

    public boolean isInherited() {
        return inherited;
    }

    public void addPostProcessInfo(AnnotationInfo ainfo, PostProcessor postProcessor) {
        PostProcessInfo ppInfo = new PostProcessInfo();
        ppInfo.ainfo = ainfo;
        ppInfo.postProcessor = postProcessor;
        postProcessInfos.add(ppInfo);
    }

    private static class PostProcessInfo {
        public AnnotationInfo ainfo;
        public PostProcessor postProcessor;
    }

    public ServiceReferenceContainer[] getServiceRefContainers(String implName) {
        return getDescriptor().getEjbBundleDescriptor().getEjbByClassName(implName);
    }

    public HandlerChainContainer[]
            getHandlerChainContainers(boolean serviceSideHandlerChain, Class declaringClass) {
        if(serviceSideHandlerChain) {
            EjbDescriptor[] ejbs = getDescriptor().getEjbBundleDescriptor().getEjbByClassName(declaringClass.getName());
            List<WebServiceEndpoint> result = new ArrayList<WebServiceEndpoint>();
            for (EjbDescriptor ejb : ejbs) {
                result.addAll(getDescriptor().getEjbBundleDescriptor().getWebServices().getEndpointsImplementedBy(ejb));
            }
            return(result.toArray(new HandlerChainContainer[result.size()]));
        } else {
            List<ServiceReferenceDescriptor> result = new ArrayList<ServiceReferenceDescriptor>();
            result.addAll(getDescriptor().getEjbBundleDescriptor().getEjbServiceReferenceDescriptors());
            return(result.toArray(new HandlerChainContainer[result.size()]));
        }
    }
}
