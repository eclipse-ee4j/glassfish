/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.WorkContextHandler;
import com.sun.logging.LogDomains;

import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.XATerminator;
import jakarta.resource.spi.work.WorkManager;
import jakarta.resource.spi.work.WorkContext;
import jakarta.transaction.TransactionSynchronizationRegistry;
import javax.naming.InitialContext;

import java.io.Serializable;
import java.util.Timer;
import java.util.logging.Logger;
import java.util.logging.Level;


/**
 * BootstrapContext implementation.
 *
 * @author Qingqing Ouyang, Binod P.G
 */
public final class BootstrapContextImpl implements BootstrapContext, Serializable {

    public static final int MAX_INSTANCE_LENGTH=24;
    private static final long serialVersionUID = -8449694716854376406L;
    private transient WorkManager wm;
    private XATerminator xa;
    private String moduleName;
    private String threadPoolId;
    private ClassLoader rarCL;

    private static final Logger logger =
            LogDomains.getLogger(BootstrapContextImpl.class, LogDomains.RSR_LOGGER);

    /**
     * Constructs a <code>BootstrapContext</code> with default
     * thread pool for work manager.
     *
     * @param moduleName resource-adapter name
     * @throws ConnectorRuntimeException If there is a failure in
     *         retrieving WorkManager.
     */
    public BootstrapContextImpl (String moduleName) throws ConnectorRuntimeException{
        this.moduleName = moduleName;
        initializeWorkManager();
    }

    /**
     * Constructs a <code>BootstrapContext</code> with a specified
     * thread pool for work manager.
     *
     * @param poolId thread-pool-id
     * @param moduleName resource-adapter name
     * @throws ConnectorRuntimeException If there is a failure in
     *         retrieving WorkManager.
     */
    public BootstrapContextImpl (String poolId, String moduleName, ClassLoader rarCL)
                                 throws ConnectorRuntimeException{
        this.threadPoolId = poolId;
        this.moduleName = moduleName;
        this.rarCL = rarCL;
        initializeWorkManager();
    }

    /**
     * Creates a <code>java.util.Timer</code> instance.
     * This can cause a problem, since the timer threads are not actually
     * under appserver control. We should override the timer later.
     *
     * @return <code>java.util.Timer</code> object.
     */
    public Timer createTimer() {
        // set the timer as 'daemon' such that RAs that do not cancel the timer during
        // ra.stop() will not block (eg : server shutdown)
        return new Timer("connectors-runtime-context", true);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isContextSupported(Class<? extends WorkContext> aClass) {
        WorkContextHandler wch = ConnectorRuntime.getRuntime().getWorkContextHandler();
        wch.init(moduleName, rarCL);
        return wch.isContextSupported(true, aClass.getName());
    }

    /**
     * {@inheritDoc}
     */
    public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
        try{
            InitialContext ic = new InitialContext();
            return (TransactionSynchronizationRegistry)ic.lookup("java:comp/TransactionSynchronizationRegistry");
        }catch(Exception e){
            logger.log(Level.WARNING, "tx.sync.registry.lookup.failed", e);
            RuntimeException re = new RuntimeException("Transaction Synchronization Registry Unavailable");
            re.initCause(e);
            throw re;
        }
    }

    /**
     * Retrieves the work manager.
     *
     * @return <code>WorkManager</code> instance.
     * @see com.sun.enterprise.connectors.work.CommonWorkManager
     * @see com.sun.enterprise.connectors.work.WorkManagerFactoryImpl
     */
    public WorkManager getWorkManager() {
        initializeWorkManager();
        return wm;
    }

    /**
     * initialize work manager reference
     */
    private void initializeWorkManager() {
        if (wm == null) {
            try {
                wm = ConnectorRuntime.getRuntime().getWorkManagerProxy(threadPoolId, moduleName, rarCL);
            } catch(Exception e) {
                   logger.log(Level.SEVERE, "workmanager.instantiation_error", e);
            }
        }
    }


    /**
     * Retrieves the <code>XATerminator</code> object.
     */
    public XATerminator getXATerminator() {
        initializeXATerminator();
        return xa;
    }

    /**
     * initializes XATerminator reference
     */
    private void initializeXATerminator() {
        if (xa == null) {
            xa = ConnectorRuntime.getRuntime().getXATerminatorProxy(moduleName);
        }
    }
}
