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

package org.glassfish.webservices;

import java.util.Hashtable;
import java.util.Iterator;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

import java.rmi.Remote;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

import javax.xml.rpc.server.ServiceLifecycle;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.ejb.api.EJBInvocation;

// JAXRPC-RI classes
import com.sun.xml.rpc.spi.JaxRpcObjectFactory;
import com.sun.xml.rpc.spi.runtime.Implementor;
import com.sun.xml.rpc.spi.runtime.ImplementorCache;
import com.sun.xml.rpc.spi.runtime.ImplementorCacheDelegate;
import com.sun.xml.rpc.spi.runtime.RuntimeEndpointInfo;
import com.sun.xml.rpc.spi.runtime.Tie;

/**
 * This class extends the behavior of ImplementorCache in order to
 * interpose on lifecycle events for the creation and destruction of 
 * ties/servants for servlet web service endpoints.
 *
 * @author Kenneth Saks
 */
public class ImplementorCacheDelegateImpl extends ImplementorCacheDelegate {

    private Hashtable implementorCache_;
    private ServletContext servletContext_;
    private JaxRpcObjectFactory rpcFactory_;

    public ImplementorCacheDelegateImpl(ServletConfig servletConfig) {
        servletContext_ = servletConfig.getServletContext();
        implementorCache_ = new Hashtable();
        rpcFactory_ = JaxRpcObjectFactory.newInstance();
    }

    public Implementor getImplementorFor(RuntimeEndpointInfo targetEndpoint) {

        Implementor implementor = null;
        try {
            synchronized(targetEndpoint) {
                implementor = (Implementor) 
                    implementorCache_.get(targetEndpoint);
                if( implementor == null ) {
                    implementor = createImplementor(targetEndpoint);
                    implementorCache_.put(targetEndpoint, implementor);
                }
            }

            WebServiceContractImpl wscImpl = WebServiceContractImpl.getInstance();
            InvocationManager invManager = wscImpl.getInvocationManager();
            ComponentInvocation inv = invManager.getCurrentInvocation();
            if (inv instanceof EJBInvocation)
                ((EJBInvocation)inv).setWebServiceTie(implementor.getTie());
        } catch(Throwable t) {
            RuntimeException re = new RuntimeException();
            re.initCause(t);
            throw re;
        }

        return implementor;
    }

    public void releaseImplementor(RuntimeEndpointInfo targetEndpoint, 
                                   Implementor implementor) {
        // do nothing
    }

    public void destroy() {
        for (Iterator iter = implementorCache_.values().iterator(); 
             iter.hasNext();) {
            Implementor implementor = (Implementor) iter.next();
            try {
                implementor.destroy();
            } catch(Throwable t) {
                // @@@ log
            }
        }
        implementorCache_.clear();
    }

    private Implementor createImplementor(RuntimeEndpointInfo targetEndpoint) 
        throws Exception {

        Tie tie = (Tie) targetEndpoint.getTieClass().newInstance();

        Class seiClass  = targetEndpoint.getRemoteInterface();
        Class implClass = targetEndpoint.getImplementationClass();

        Remote servant  = null;
        if( seiClass.isAssignableFrom(implClass) ) {
            // if servlet endpoint impl is a subtype of SEI, use an
            // instance as the servant.
            servant = (Remote) implClass.newInstance();
        } else {
            // Create a dynamic proxy that implements SEI (and optionally
            // ServiceLifecycle) and delegates to an instance of the 
            // endpoint impl.
            Object implInstance = implClass.newInstance();

            InvocationHandler handler = 
                new ServletImplInvocationHandler(implInstance);
            boolean implementsLifecycle = 
                ServiceLifecycle.class.isAssignableFrom(implClass);
            Class[] proxyInterfaces = implementsLifecycle ?
                new Class[] { seiClass, ServiceLifecycle.class } :
                new Class[] { seiClass };

            servant = (Remote) Proxy.newProxyInstance
                (implClass.getClassLoader(), proxyInterfaces, handler);
        }
        tie.setTarget(servant);
        
        Implementor implementor = rpcFactory_.createImplementor(servletContext_, tie);
        implementor.init();

        return implementor;
    }
}
