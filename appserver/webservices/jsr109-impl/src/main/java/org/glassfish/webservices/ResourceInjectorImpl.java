/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.webservices;

import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.xml.ws.api.server.ResourceInjector;
import com.sun.xml.ws.api.server.WSWebServiceContext;

import jakarta.xml.ws.WebServiceException;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT_ENV;


/**
 * JAXWS Container call back to inject servlet endpoints
 */

public class ResourceInjectorImpl extends ResourceInjector {

    private final WebServiceEndpoint endpoint;
    private final ComponentInvocation inv;
    private final InvocationManager invMgr;
    private static final Logger logger = LogUtils.getLogger();

    public ResourceInjectorImpl(WebServiceEndpoint ep) {

        WebServiceContractImpl    wscImpl = WebServiceContractImpl.getInstance();
        invMgr =  wscImpl.getInvocationManager();
        inv = invMgr.getCurrentInvocation();
        endpoint = ep;

    }

    @Override
    public void inject(WSWebServiceContext context, Object instance)
                    throws WebServiceException {

        try {
            // Set proper component context
            invMgr.preInvoke(inv);
            // Injection first
            InjectionManager injManager = WebServiceContractImpl.getInstance().getInjectionManager();
            injManager.injectInstance(instance);

            // Set webservice context here
            // If the endpoint has a WebServiceContext with @Resource then
            // that has to be used
            WebServiceContextImpl wsc = null;
            WebBundleDescriptor bundle = (WebBundleDescriptor)endpoint.getBundleDescriptor();
            for (ResourceReferenceDescriptor r : bundle.getResourceReferenceDescriptors()) {
                if(r.isWebServiceContext()) {
                    boolean matchingClassFound = false;
                    for (InjectionTarget target : r.getInjectionTargets()) {
                        if(endpoint.getServletImplClass().equals(target.getClassName())) {
                            matchingClassFound = true;
                            break;
                        }
                    }
                    if(!matchingClassFound) {
                        continue;
                    }
                    try {
                        javax.naming.InitialContext ic = new javax.naming.InitialContext();
                        wsc = (WebServiceContextImpl) ic.lookup(JNDI_CTX_JAVA_COMPONENT_ENV + r.getName());
                    } catch (Throwable t) {
                        // Do something here
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, LogUtils.EXCEPTION_THROWN, t);
                        }
                    }
                    if(wsc != null) {
                        wsc.setContextDelegate(context);
                        //needed to support isUserInRole() on WSC;
                        wsc.setServletName(bundle.getWebComponentDescriptors());

                    }
                }
            }
        } catch (InjectionException ie) {
            throw new WebServiceException(ie);
        } finally {
            invMgr.postInvoke(inv);
        }
    }
}
