/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package samples.ejb.stateless.simple.ejb;

/**
 * Remote interface for the GreeterEJB. The remote interface defines all possible
 * business methods for the bean. These are the methods going to be invoked remotely
 * by clients, once they have a reference to the remote interface.
 *
 * Clients (GreeterServlet, in this case), generally take the help of JNDI to lookup
 * the bean's home interface (GreeterHome, in this case) and then use the home interface
 * to obtain references to the bean's remote interface (Greeter, in this case).
 *
 */
public interface Greeter extends jakarta.ejb.EJBObject {
    /**
     * Returns a greeting.
     * @return returns a greeting as a string.
     * @exception throws a RemoteException.
     *
     */
    public String getGreeting() throws java.rmi.RemoteException;
}
