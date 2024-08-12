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

package org.glassfish.ejb.spi;

import com.sun.enterprise.deployment.WebServiceEndpoint;

import org.glassfish.ejb.api.EjbEndpointFacade;
import org.jvnet.hk2.annotations.Contract;

/*
 * This interface is the contract for the service WebServiceEjbEndpointRegistry
 * which will lie in the webservices module
 * The StatelessSessionContainer will use this to lookup for the service to
 * register the endpoint
 * @author Bhakti Mehta
 */

@Contract
public interface WSEjbEndpointRegistry {


    /**
     * This method will register an endpoint the EjbEndpointRegistry
     */
    public void registerEndpoint(WebServiceEndpoint webServiceEndpoint,
                                  EjbEndpointFacade ejbContainer,
                                  Object servant, Class tieClass);

    /**
     * This method will unregister an endpoint the EjbEndpointRegistry
     */
    public void unregisterEndpoint (String addressUri) ;

}
