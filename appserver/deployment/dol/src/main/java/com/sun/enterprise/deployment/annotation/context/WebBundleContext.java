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

package com.sun.enterprise.deployment.annotation.context;

import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.types.HandlerChainContainer;
import org.glassfish.apf.AnnotatedElementHandler;

import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

/**
 * This ClientContext implementation holds a top level reference
 * to the DOL Web BundleDescriptor which will be used to populate
 * any information processed from the annotations.
 *
 * @author Shing Wai Chan
 */
public class WebBundleContext extends ResourceContainerContextImpl {

    public WebBundleContext(WebBundleDescriptor webBundleDescriptor) {
        super(webBundleDescriptor);
    }

    public WebBundleDescriptor getDescriptor() {
        return (WebBundleDescriptor)descriptor;
    }

    /**
     * This method create a context for web component(s) by using
     * descriptor(s) associated to given webComponet impl class.
     * Return null if corresponding descriptor is not found.
     */
    public AnnotatedElementHandler createContextForWeb() {
        AnnotatedElement anTypeElement =
                this.getProcessingContext().getProcessor(
                ).getLastAnnotatedElement(ElementType.TYPE);
        WebComponentDescriptor[] webComps = null;
        if (anTypeElement != null) {
            String implClassName = ((Class)anTypeElement).getName();
            webComps = getDescriptor().getWebComponentByImplName(implClassName);
        }

        AnnotatedElementHandler aeHandler = null;
        if (webComps != null && webComps.length > 1) {
            aeHandler = new WebComponentsContext(webComps);
        } else if (webComps != null && webComps.length == 1) {
            aeHandler = new WebComponentContext(webComps[0]);
        }

        if (aeHandler != null) {
            // push a WebComponent(s)Context to stack
            this.getProcessingContext().pushHandler(aeHandler);
        }
        return aeHandler;
    }

    public HandlerChainContainer[]
            getHandlerChainContainers(boolean serviceSideHandlerChain, Class declaringClass) {
        if(serviceSideHandlerChain) {
            List<WebServiceEndpoint> result = new ArrayList<WebServiceEndpoint>();
            for (WebServiceEndpoint endpoint : getDescriptor().getWebServices().getEndpoints()) {
                if (endpoint.getWebComponentImpl().getWebComponentImplementation().equals(declaringClass.getName())) {
                    result.add(endpoint);
                }
            }
            return(result.toArray(new HandlerChainContainer[result.size()]));
        } else {
            List<ServiceReferenceDescriptor> result = new ArrayList<ServiceReferenceDescriptor>();
            result.addAll(getDescriptor().getServiceReferenceDescriptors());
            return(result.toArray(new HandlerChainContainer[result.size()]));
        }
    }
}
