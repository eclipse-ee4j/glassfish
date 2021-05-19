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

package com.sun.ejb.containers;


import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;

import com.sun.ejb.EjbInvocation;
import com.sun.ejb.Container;

import org.glassfish.ejb.api.EjbEndpointFacade;


import java.util.logging.Level;
import java.util.logging.Logger;


/*
 * @author Kenneth Saks
 */

public class EjbEndpointFacadeImpl implements EjbEndpointFacade {

    private BaseContainer container_;
    private InvocationManager invManager_;
    private static Logger logger_ = EjbContainerUtilImpl.getLogger();


    public EjbEndpointFacadeImpl(BaseContainer container, EjbContainerUtil util) {
        container_ = container;
        invManager_ = util.getInvocationManager();
    }


    public ClassLoader getEndpointClassLoader() {

        return container_.getClassLoader();

    }


    public ComponentInvocation startInvocation() {

        // We need to split the preInvoke tasks into stages since handlers
        // need access to java:comp/env and method authorization must take
        // place before handlers are run.  Note that the application
        // classloader was set much earlier when the invocation first arrived
        // so we don't need to set it here.
        EjbInvocation inv = container_.createEjbInvocation();

        // Do the portions of preInvoke that don't need a Method object.
        inv.isWebService = true;
        inv.container = container_;
        inv.transactionAttribute = Container.TX_NOT_INITIALIZED;

        // AS per latest spec change, the MessageContext object in WebSvcCtxt
        // should be the same one as used in the ejb's interceptors'
        // TODO
        // inv.setContextData(wsCtxt);

        // In all cases, the WebServiceInvocationHandler will do the
        // remaining preInvoke tasks : getContext, preInvokeTx, etc.
        invManager_.preInvoke(inv);

        return inv;

    }


    public void endInvocation(ComponentInvocation inv) {

        try {
            EjbInvocation ejbInv = (EjbInvocation) inv;

            // Only use container version of postInvoke if we got past
            // assigning an ejb instance to this invocation.  This is
            // because the web service invocation does an InvocationManager
            // preInvoke *before* assigning an ejb instance.  So, we need
            // to ensure that InvocationManager.postInvoke is always
            // called.  It was cleaner to keep this logic in this class
            // and WebServiceInvocationHandler rather than change the
            // behavior of BaseContainer.preInvoke and
            // BaseContainer.postInvoke.


            if( ejbInv.ejb != null ) {
                container_.webServicePostInvoke(ejbInv);
            } else {
                invManager_.postInvoke(inv);
            }

        } catch(Throwable t) {
            logger_.log(Level.WARNING,
                       "Unexpected error in EJB WebService endpoint post processing", t);
        }

    }

}
