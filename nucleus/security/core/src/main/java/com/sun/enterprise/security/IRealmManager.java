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

package com.sun.enterprise.security;

/**
 * This is an remote interface provided to the RealmManager
 * This allows the realms to be updated while the server is
 * running.
 * @author Harpreet Singh (harpreet.singh@sun.com)
 */
public interface IRealmManager extends java.rmi.Remote{

    /**
     * This refreshes the realm information. This is used by
     * a client program e.g. realmtool. The client manipulates
     * the realm information and then calls this method. This then
     * updates the realm information in an already running server.
     * If the server is not running the call should not be made.
     * This interface is obtained by looking up the <i>RealmManager</>
     * from the Naming service.
     * @param String realmName
     */
    public void refreshRealms(String realmName)
        throws java.rmi.RemoteException;
}
