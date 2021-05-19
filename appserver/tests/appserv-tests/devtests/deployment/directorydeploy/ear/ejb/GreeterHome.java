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
 * Home interface for the GreeterEJB. Clients generally use home interface
 * to obtain references to the bean's remote interface.
 *
 */
public interface GreeterHome extends jakarta.ejb.EJBHome {
    /**
     * Gets a reference to the remote interface to the Greeter bean.
     * @exception throws CreateException and RemoteException.
     *
     */
    public Greeter create() throws java.rmi.RemoteException, jakarta.ejb.CreateException;
}
