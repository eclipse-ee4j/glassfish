/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.devtest.security.plogin.converter.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBHome;

/**
 * Home interface for the <code>ConverterBean</code>. Clients generally use home interface
 * to obtain references to the bean's remote interface, <code>Converter</code>.
 *
 * @see Converter
 * @see ConverterBean
 */
public interface ConverterRemoteHome extends EJBHome {
    /**
     * Gets a reference to the remote interface of the <code>ConverterBean</code>.
     * @exception throws CreateException and RemoteException.
     *
     */
    ConverterRemote create() throws RemoteException, CreateException;
}
