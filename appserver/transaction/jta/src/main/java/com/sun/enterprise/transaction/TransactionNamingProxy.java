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

package com.sun.enterprise.transaction;

import org.glassfish.api.naming.*;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.hk2.api.ServiceLocator;

import com.sun.enterprise.transaction.spi.TransactionOperationsManager;

import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;

import javax.naming.NamingException;

/**
 * Proxy for creating JTA instances that get registered in the naming manager.
 * NamingManager will call the handle() method when the JNDI name is looked up.
 * Will return the instance that corresponds to the known name.
 *
 * @author Marina Vatkina
 */
@Service
@NamespacePrefixes({TransactionNamingProxy.USER_TX,
        TransactionNamingProxy.TRANSACTION_SYNC_REGISTRY,
        TransactionNamingProxy.APPSERVER_TRANSACTION_MGR,
        TransactionNamingProxy.APPSERVER_TRANSACTION_SYNC_REGISTRY})
public class TransactionNamingProxy
        implements NamedNamingObjectProxy {

    @Inject
    private ServiceLocator habitat;

    @SuppressWarnings("unused")
    @Inject
    private org.glassfish.api.admin.ProcessEnvironment processEnv;  // Here for ordering

    static final String USER_TX = "java:comp/UserTransaction";
    static final String USER_TX_NO_JAVA_COMP = "UserTransaction";

    static final String TRANSACTION_SYNC_REGISTRY
            = "java:comp/TransactionSynchronizationRegistry";

    static final String APPSERVER_TRANSACTION_SYNC_REGISTRY
            = "java:appserver/TransactionSynchronizationRegistry";

    static final String TRANSACTION_MGR
            = "java:pm/TransactionManager";

    static final String APPSERVER_TRANSACTION_MGR
            = "java:appserver/TransactionManager";

    public Object handle(String name) throws NamingException {

        if (USER_TX.equals(name)) {
            checkUserTransactionLookupAllowed();
            return habitat.getService(UserTransactionImpl.class);
        } else if (TRANSACTION_SYNC_REGISTRY.equals(name) || APPSERVER_TRANSACTION_SYNC_REGISTRY.equals(name)) {
            return habitat.getService(TransactionSynchronizationRegistryImpl.class);
        } else if (APPSERVER_TRANSACTION_MGR.equals(name)) {
            return habitat.getService(TransactionManagerHelper.class);
        }

        return null;
    }

    private void checkUserTransactionLookupAllowed() throws NamingException {
        InvocationManager iv = habitat.getService(InvocationManager.class);
        if (iv != null) {
            ComponentInvocation inv = iv.getCurrentInvocation();
            if (inv != null) {
                TransactionOperationsManager toMgr =
                        (TransactionOperationsManager)inv.getTransactionOperationsManager();
                if ( toMgr != null ) {
                    toMgr.userTransactionLookupAllowed();
                }
            }
        }
    }
}
