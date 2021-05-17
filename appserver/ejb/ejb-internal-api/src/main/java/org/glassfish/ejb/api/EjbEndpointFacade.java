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

package org.glassfish.ejb.api;


import org.glassfish.api.invocation.ComponentInvocation;


/**
 * This interface provides services needed by the web services runtime
 * to flow an invocation through the ejb container to an EJB
 * web service endpoint.
 *
 * @author Kenneth Saks
 */


public interface EjbEndpointFacade {


    /**
     * Returns the application class loader associated with this
     * web service endpoint.  This class loader must be the
     * Thread's context class loader when startInvocation() is called
     * and must remain the Thread's context class loader until
     * after endInvocation() returns.
     */
    public ClassLoader getEndpointClassLoader();


    /**
     * Start an invocation for the EJB web service endpoint.
     * Once startInvocation() is called, endInvocation() must be called
     * at some later time on the same thread.  Interleaved invocations
     * on the same thread are not allowed.
     *
     * @return A component invocation for this invocation.  Must be
     *         passed to the corresponding endInvocation.
     */
    public ComponentInvocation startInvocation();


    /**
     * Perform post-processing for the web service endpoint invocation.
     * @argument inv The ComponentInvocation returned from the original
     *               startInvocation
     */
    public void endInvocation(ComponentInvocation inv);

}
