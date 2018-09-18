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

import java.rmi.Remote;

import com.sun.enterprise.deployment.WebServiceEndpoint;
import org.glassfish.ejb.api.EjbEndpointFacade;
import org.glassfish.ejb.api.EJBInvocation;
import org.glassfish.internal.api.Globals;
import org.glassfish.api.invocation.ComponentInvocation;
import com.sun.xml.rpc.spi.runtime.Handler;
import com.sun.xml.rpc.spi.runtime.Tie;

/**
 * Runtime dispatch information about one ejb web service
 * endpoint.  This class must support concurrent access,
 * since a single instance will be used for all web
 * service invocations through the same ejb endpoint.
 *
 * @author Kenneth Saks
 */
public class Ejb2RuntimeEndpointInfo extends EjbRuntimeEndpointInfo {

    private Class tieClass;

    // Lazily instantiated and cached due to overhead
    // of initialization.
    private Tie tieInstance;

    private Object serverAuthConfig;

    public Ejb2RuntimeEndpointInfo(WebServiceEndpoint webServiceEndpoint,
                                  EjbEndpointFacade ejbContainer,
                                  Object servant, Class tie) {
                                  
        super(webServiceEndpoint, ejbContainer, servant);
        tieClass = tie;
        
        if (Globals.getDefaultHabitat() != null) {
            org.glassfish.webservices.SecurityService secServ = Globals.get(
                    org.glassfish.webservices.SecurityService.class);
            if (secServ != null) {
                serverAuthConfig = secServ.mergeSOAPMessageSecurityPolicies(webServiceEndpoint.getMessageSecurityBinding());
            }
        }
        
    }

    public AdapterInvocationInfo getHandlerImplementor()
        throws Exception {

        ComponentInvocation inv =  container.startInvocation();
        AdapterInvocationInfo aInfo = new AdapterInvocationInfo();
        aInfo.setInv(inv);
        synchronized(this) {
            if(tieClass == null) {
                tieClass = Thread.currentThread().getContextClassLoader().loadClass(getEndpoint().getTieClassName());
            }
            if( tieInstance == null ) {
                tieInstance = (Tie) tieClass.newInstance();
                tieInstance.setTarget((Remote) webServiceEndpointServant);
            }
        }
        if (inv instanceof EJBInvocation)
            EJBInvocation.class.cast(inv).setWebServiceTie(tieInstance);
        aInfo.setHandler((Handler)tieInstance);
        return aInfo;
    }

    /**
     * Called after attempt to handle message.  This is coded defensively
     * so we attempt to clean up no matter how much progress we made in
     * getImplementor.  One important thing is to complete the invocation
     * manager preInvoke().
     */
    @Override
    public void releaseImplementor(ComponentInvocation inv) {
        container.endInvocation(inv);
    }

    @Override
    public EjbMessageDispatcher getMessageDispatcher() {
        // message dispatcher is stateless, no need to synchronize, worse
        // case, we'll create too many.
        if (messageDispatcher==null) {
            messageDispatcher = new EjbWebServiceDispatcher();
        }
        return messageDispatcher;
    }

    public Object getServerAuthConfig() {
     return serverAuthConfig;
    }

}
