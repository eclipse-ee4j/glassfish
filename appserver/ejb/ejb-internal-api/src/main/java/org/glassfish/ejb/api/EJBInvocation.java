/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ejb.EJBContext;

import java.lang.reflect.Method;

/**
 * This interface provides access to the exported portions of the ejb invocation object.
 *
 * @author Kenneth Saks
 */

public interface EJBInvocation {

    public EJBContext getEJBContext();

    /**
     * This is for EJB JAXWS only.
     *
     * @return the JAXWS message
     */
    public Object getMessage();

    /**
     * This is for EJB JAXWS only.
     *
     * @param message an unconsumed message
     */
    public <T> void setMessage(T message);

    /**
     *
     * @return true if it is a webservice invocation
     */
    public boolean isAWebService();

    /**
     * @return the Java Method object for this Invocation
     */
    public Method getMethod();

    /**
     *
     * @return the Method parameters for this Invocation
     */
    public Object[] getMethodParams();

    /**
     * Used by Jakarta Authorization implementation to get an enterprise bean instance for the EnterpriseBean policy handler.
     * The Jakarta Authorization implementation should use this method rather than directly accessing the ejb field.
     */
    public Object getJaccEjb();

    /**
     * Use the underlying container to authorize this invocation
     *
     * @return true if the invocation was authorized by the underlying container
     * @throws java.lang.Exception TODO, change this to throw some subclass
     */
    public boolean authorizeWebService(Method m) throws Exception;

    /**
     *
     * @return true if the SecurityManager reports that the caller is in role
     */
    public boolean isCallerInRole(String role);

    public void setWebServiceMethod(Method method);

    public Method getWebServiceMethod();

    public void setWebServiceContext(Object webServiceContext);
}
