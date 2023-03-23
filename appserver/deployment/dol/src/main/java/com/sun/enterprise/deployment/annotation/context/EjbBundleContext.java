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
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.types.HandlerChainContainer;
import com.sun.enterprise.deployment.types.ServiceReferenceContainer;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.context.AnnotationContext;

/**
 * This ClientContext implementation holds a top level reference
 * to the DOL EJB BundleDescriptor which will be used to populate
 * any information processed from the annotations.
 *
 * @author Jerome Dochez
 */
public class EjbBundleContext extends ResourceContainerContextImpl {

    /** Creates a new instance of EjbBundleContext */
    public EjbBundleContext(EjbBundleDescriptor descriptor) {
        super(descriptor);
    }


    public <T extends EjbBundleDescriptor> T getDescriptor() {
        return (T) descriptor;
    }


    /**
     * This methods create a context for Ejb(s) by using descriptor(s)
     * associated to given ejbClassName.
     * Return null if corresponding descriptor is not found.
     */
    public AnnotatedElementHandler createContextForEjb() {
        Class<?> ejbClass = (Class<?>) getProcessingContext().getProcessor().getLastAnnotatedElement(ElementType.TYPE);
        EjbDescriptor[] ejbDescs = null;
        String ejbClassName = null;
        if (ejbClass != null) {
            ejbClassName = ejbClass.getName();
            ejbDescs = getDescriptor().getEjbByClassName(ejbClassName);
        }

        AnnotationContext aeHandler = null;
        if (ejbDescs != null && ejbDescs.length > 1) {
            aeHandler = new EjbsContext(ejbDescs, ejbClass);
        } else if (ejbDescs != null && ejbDescs.length == 1) {
            aeHandler = new EjbContext(ejbDescs[0], ejbClass);
        }

        if (aeHandler != null) {
            // push a EjbContext to stack
            getProcessingContext().pushHandler(aeHandler);
        }
        return aeHandler;
    }


    @Override
    public HandlerChainContainer[] getHandlerChainContainers(boolean serviceSideHandlerChain, Class<?> declaringClass) {
        if (serviceSideHandlerChain) {
            EjbDescriptor[] ejbs;
            if (declaringClass.isInterface()) {
                ejbs = getDescriptor().getEjbBySEIName(declaringClass.getName());
            } else {
                ejbs = getDescriptor().getEjbByClassName(declaringClass.getName());
            }
            List<WebServiceEndpoint> result = new ArrayList<>();
            for (EjbDescriptor ejb : ejbs) {
                result.addAll(getDescriptor().getWebServices().getEndpointsImplementedBy(ejb));
            }
            return (result.toArray(new HandlerChainContainer[result.size()]));
        }
        List<ServiceReferenceDescriptor> result = new ArrayList<>();
        result.addAll(getDescriptor().getEjbServiceReferenceDescriptors());
        return result.toArray(new HandlerChainContainer[result.size()]);
    }


    @Override
    public ServiceReferenceContainer[] getServiceRefContainers() {
        return getDescriptor().getEjbs().toArray(ServiceReferenceContainer[]::new);
    }


    /**
     * This methods create a context for EjbInterceptor associated to given className.
     * Return null if corresponding descriptor is not found.
     */
    public AnnotatedElementHandler createContextForEjbInterceptor() {
        Class<?> interceptorClass = (Class<?>) getProcessingContext().getProcessor()
            .getLastAnnotatedElement(ElementType.TYPE);
        EjbInterceptor ejbInterceptor = getDescriptor().getInterceptorByClassName(interceptorClass.getName());
        if (ejbInterceptor == null) {
            return null;
        }
        EjbInterceptorContext aeHandler = new EjbInterceptorContext(ejbInterceptor);
        // push a EjbInterceptorContext to stack
        getProcessingContext().pushHandler(aeHandler);
        return aeHandler;
    }
}
