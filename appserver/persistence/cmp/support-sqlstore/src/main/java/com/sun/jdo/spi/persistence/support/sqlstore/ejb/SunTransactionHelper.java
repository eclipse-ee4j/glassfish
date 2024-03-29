/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.jdo.spi.persistence.support.sqlstore.ejb;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.connectors.internal.spi.ConnectorNamingEvent;
import com.sun.appserv.connectors.internal.spi.ConnectorNamingEventListener;
import com.sun.appserv.jdbc.DataSource;
import com.sun.ejb.containers.EjbContainerUtil;
import com.sun.jdo.api.persistence.support.JDOFatalInternalException;
import com.sun.jdo.api.persistence.support.PersistenceManagerFactory;

import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.naming.InitialContext;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.internal.api.Globals;
import org.glassfish.persistence.common.I18NHelper;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT;


/** Sun specific implementation for TransactionHelper interface.
* This class has a special implementation for
* <code>registerSynchronization</code>, because it uses a special
* object that registers Synchronization instance to be processed after
* any bean's or container beforeCompletion method, but before the corresponding
* afterCompletion.
*/
public class SunTransactionHelper extends TransactionHelperImpl implements ConnectorNamingEventListener {

    /** I18N message handler */
    private final static ResourceBundle messages = I18NHelper.loadBundle(
        "com.sun.jdo.spi.persistence.support.sqlstore.Bundle", // NOI18N
        SunTransactionHelper.class.getClassLoader());

    private static List<PersistenceManagerFactory> pmf_list;

    private static EjbContainerUtil ejbContainerUtil;

    private final static Object pmf_listSyncObject = new Object();

    /**
     * Array of registered ApplicationLifeCycleEventListener
     */
    private final List<ApplicationLifeCycleEventListener> applicationLifeCycleEventListeners = new ArrayList<>();


    /** Garantees singleton.
     * Registers itself during initial load
     */
    static {
        SunTransactionHelper helper = new SunTransactionHelper();
        EJBHelper.registerTransactionHelper (helper);
        // Register with ApplicationLoaderEventNotifier to receive Sun
        // Application Server specific lifecycle events.
//        ApplicationLoaderEventNotifier.getInstance().addListener(helper);
        ConnectorRuntime connectorRuntime = Globals.getDefaultHabitat().getService(ConnectorRuntime.class);
        connectorRuntime.registerConnectorNamingEventListener(helper);

        pmf_list = new ArrayList<>();

        ejbContainerUtil = Globals.getDefaultHabitat().getService(EjbContainerUtil.class);
    }

    /** Default constructor should not be public */
    SunTransactionHelper() { }

    // helper class for looking up the TransactionManager instances.
    static private class TransactionManagerFinder {

        // JNDI name of the TransactionManager used for managing local transactions.
        static private final String AS_TM_NAME = "java:appserver/TransactionManager"; //NOI18N

        // TransactionManager instance used for managing local transactions.
        static TransactionManager appserverTM = null;

        static {
            try {
                appserverTM = (TransactionManager) (new InitialContext()).lookup(AS_TM_NAME);
            } catch (Exception e) {
                throw new JDOFatalInternalException(e.getMessage());
            }
        }
    }

    /** SunTransactionHelper specific code */
    @Override
    public Transaction getTransaction(){
       try{
            return TransactionManagerFinder.appserverTM.getTransaction();
        } catch (Exception e) {
            throw new JDOFatalInternalException(e.getMessage());
        } catch (ExceptionInInitializerError err) {
            throw new JDOFatalInternalException(err.getMessage());
        }
    }

    /** SunTransactionHelper specific code */
    @Override
    public UserTransaction getUserTransaction() {
        try {
            return (UserTransaction) InitialContext.doLookup(JNDI_CTX_JAVA_COMPONENT + "UserTransaction");
        } catch (Exception e) {
            throw new JDOFatalInternalException(e.getMessage(), e);
        }
    }


    /** SunTransactionHelper specific code */
    @Override
    public void registerSynchronization(Transaction jta, Synchronization sync)
            throws RollbackException, SystemException {
        ejbContainerUtil.registerPMSync(jta, sync);
    }

    /** SunTransactionHelper specific code */
    @Override
    public PersistenceManagerFactory replaceInternalPersistenceManagerFactory(
    PersistenceManagerFactory pmf) {

        synchronized(pmf_listSyncObject) {
        int i = pmf_list.indexOf(pmf);
        if (i == -1) {
            // New PersistenceManagerFactory. Remember it.
            pmf_list.add(pmf);
            return pmf;
        }

        return pmf_list.get(i);
        }
    }

    /**
     * Returns name prefix for DDL files extracted from the info instance by the
     * application server specific code.
     * SunTransactionHelper specific code. Delegates the actual implementation
     * to DeploymentHelper#getDDLNamePrefix(Object);
     *
     * @param info the instance to use for the name generation.
     * @return name prefix as String.
     */
    @Override
    public String getDDLNamePrefix(Object info) {
        return DeploymentHelper.getDDLNamePrefix(info);
    }

    /** Called in a managed environment to get a Connection from the application
     * server specific resource. In a non-managed environment returns null as
     * it should not be called.
     * SunTransactionHelper specific code uses com.sun.appserv.jdbc.DataSource
     * to get a Connection.
     *
     * @param resource the application server specific resource.
     * @param username the resource username. If null, Connection is requested
     * without username and password validation.
     * @param password the password for the resource username.
     * @return a Connection.
     * @throws java.sql.SQLException
     */
    @Override
    public java.sql.Connection getNonTransactionalConnection(
            Object resource, String username, char[] password)
            throws java.sql.SQLException {

        java.sql.Connection rc = null;
        // resource is expected to be com.sun.appserv.jdbc.DataSource
        if (resource instanceof DataSource) {
            DataSource ds = (DataSource)resource;
            if (username == null) {
                rc = ds.getNonTxConnection();
            } else {
                rc = ds.getNonTxConnection(username, new String(password));
            }
        } else {
            throw new JDOFatalInternalException(I18NHelper.getMessage(
                messages, "ejb.SunTransactionHelper.wrongdatasourcetype", //NOI18N
                resource.getClass().getName()));
        }
        return rc;
    }

    /** SunTransactionHelper specific code */
    @Override
    public TransactionManager getLocalTransactionManager() {
        try {
            return TransactionManagerFinder.appserverTM;
        } catch (ExceptionInInitializerError err) {
                throw new JDOFatalInternalException(err.getMessage());
        }
    }

    @Override
    public void registerApplicationLifeCycleEventListener(
            ApplicationLifeCycleEventListener listener) {
        synchronized(applicationLifeCycleEventListeners) {
             applicationLifeCycleEventListeners.add(listener);
        }
    }
    //-------------------ApplicationLifeCycleEventListener Methods --------------//

    @Override
    public void notifyApplicationUnloaded(ClassLoader classLoader) {
        for (Object applicationLifeCycleEventListener2 : applicationLifeCycleEventListeners) {
        ApplicationLifeCycleEventListener applicationLifeCycleEventListener =
                (ApplicationLifeCycleEventListener) applicationLifeCycleEventListener2;
        applicationLifeCycleEventListener.notifyApplicationUnloaded(classLoader);
      }
    }

    @Override
    public void connectorNamingEventPerformed(ConnectorNamingEvent event){
        if(event.getEventType() == ConnectorNamingEvent.EVENT_OBJECT_REBIND){
            SimpleJndiName dsName = ConnectorsUtil.getPMJndiName(event.getJndiName());
            cleanUpResources(dsName);
        } // Ignore all other events.
    }

    /**
     * Removes all entries that correspond to the same connection factory name.
     * @param name the connection factory name.
     */
    private void cleanUpResources(SimpleJndiName name) {
        synchronized(pmf_listSyncObject) {
            for (Iterator<PersistenceManagerFactory> it = pmf_list.iterator(); it.hasNext(); ) {
                PersistenceManagerFactory pmf = it.next();
                if (pmf.getConnectionFactoryName().equals(name)) {
                    it.remove();
                }
            }
        }
    }

}
