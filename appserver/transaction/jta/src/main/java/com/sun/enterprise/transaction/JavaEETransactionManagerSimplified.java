/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.transaction;

import com.sun.appserv.util.cache.BaseCache;
import com.sun.appserv.util.cache.Cache;
import com.sun.enterprise.config.serverbeans.ModuleMonitoringLevels;
import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.api.TransactionAdminBean;
import com.sun.enterprise.transaction.api.XAResourceWrapper;
import com.sun.enterprise.transaction.config.TransactionService;
import com.sun.enterprise.transaction.monitoring.TransactionServiceProbeProvider;
import com.sun.enterprise.transaction.monitoring.TransactionServiceStatsProvider;
import com.sun.enterprise.transaction.spi.JavaEETransactionManagerDelegate;
import com.sun.enterprise.transaction.spi.TransactionInternal;
import com.sun.enterprise.transaction.spi.TransactionalResource;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.resource.spi.XATerminator;
import jakarta.resource.spi.work.WorkException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationException;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.invocation.ResourceHandler;
import org.glassfish.common.util.Constants;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

/**
 * Implementation of jakarta.transaction.TransactionManager interface. This class provides non-XA local transaction
 * support and delegates to implementation of the JavaEETransactionManagerDelegate for XA or LAO optimization, and
 * complete JTS implementation.
 *
 * @author Tony Ng
 * @author Marina Vatkina
 */
@Service
@ContractsProvided({ TransactionManager.class, JavaEETransactionManager.class })
@Rank(Constants.DEFAULT_IMPLEMENTATION_RANK) // This should be the default impl if it is available
public class JavaEETransactionManagerSimplified implements JavaEETransactionManager, PostConstruct {

    private static final Logger LOG = LogDomains.getLogger(JavaEETransactionManagerSimplified.class, LogDomains.JTA_LOGGER);

    @Inject
    private ServiceLocator habitat;

    @Inject
    protected InvocationManager invMgr;

    private JavaEETransactionManagerDelegate delegate;

    // Sting Manager for Localization
    private static StringManager sm = StringManager.getManager(JavaEETransactionManagerSimplified.class);

    // Note: this is not inheritable because we dont want transactions
    // to be inherited by child threads.
    private final ThreadLocal<JavaEETransaction> transactions;

    private final ThreadLocal localCallCounter;
    private final ThreadLocal<JavaEETransactionManagerDelegate> delegates;

    // If multipleEnlistDelists is set to true, with in the transaction, for the same
    // - connection multiple enlistments and delistments might happen
    // - By setting the System property ALLOW_MULTIPLE_ENLISTS_DELISTS to true
    // - multipleEnlistDelists can be enabled
    private boolean multipleEnlistDelists = false;

    private int transactionTimeout;
    private final ThreadLocal<Integer> txnTmout = new ThreadLocal<>();

    private int purgeCancelledTtransactions = 0;

    // admin and monitoring related parameters
    private static final Hashtable statusMap = new Hashtable();
    private final List activeTransactions = Collections.synchronizedList(new ArrayList());
    private boolean monitoringEnabled = false;

    private TransactionServiceProbeProvider monitor;
    private Hashtable txnTable = null;

    private Cache resourceTable;

    private final Timer _timer = new Timer("transaction-manager", true);

    static {
        statusMap.put(Status.STATUS_ACTIVE, "Active");
        statusMap.put(Status.STATUS_MARKED_ROLLBACK, "MarkedRollback");
        statusMap.put(Status.STATUS_PREPARED, "Prepared");
        statusMap.put(Status.STATUS_COMMITTED, "Committed");
        statusMap.put(Status.STATUS_ROLLEDBACK, "RolledBack");
        statusMap.put(Status.STATUS_UNKNOWN, "UnKnown");
        statusMap.put(Status.STATUS_NO_TRANSACTION, "NoTransaction");
        statusMap.put(Status.STATUS_PREPARING, "Preparing");
        statusMap.put(Status.STATUS_COMMITTING, "Committing");
        statusMap.put(Status.STATUS_ROLLING_BACK, "RollingBack");

    }

    public JavaEETransactionManagerSimplified() {
        transactions = new ThreadLocal<>();
        localCallCounter = new ThreadLocal();
        delegates = new ThreadLocal<>();
    }

    @Override
    public void postConstruct() {
        initDelegates();
        initProperties();
    }

    private void initProperties() {
        int maxEntries = 8192; // FIXME: this maxEntry should be a config
        float loadFactor = 0.75f; // FIXME: this loadFactor should be a config
        // for now, let's get it from system prop
        try {
            String mEnlistDelists = System.getProperty("ALLOW_MULTIPLE_ENLISTS_DELISTS");
            if ("true".equals(mEnlistDelists)) {
                multipleEnlistDelists = true;
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "TM: multiple enlists, delists are enabled");
                }
            }
            String maxEntriesValue = System.getProperty("JTA_RESOURCE_TABLE_MAX_ENTRIES");
            if (maxEntriesValue != null) {
                int temp = Integer.parseInt(maxEntriesValue);
                if (temp > 0) {
                    maxEntries = temp;
                }
            }
            String loadFactorValue = System.getProperty("JTA_RESOURCE_TABLE_DEFAULT_LOAD_FACTOR");
            if (loadFactorValue != null) {
                float f = Float.parseFloat(loadFactorValue);
                if (f > 0) {
                    loadFactor = f;
                }
            }
        } catch (Exception ex) {
            // ignore
        }

        resourceTable = new BaseCache();
        ((BaseCache) resourceTable).init(maxEntries, loadFactor, null);
        // END IASRI 4705808 TTT001

        if (habitat != null) {
            TransactionService txnService = habitat.getService(TransactionService.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
            // running on the server side ?
            if (txnService != null) {
                transactionTimeout = Integer.parseInt(txnService.getTimeoutInSeconds());
                // the delegates will do the rest if they support it

                String v = txnService.getPropertyValue("purge-cancelled-transactions-after");
                if (v != null && v.length() > 0) {
                    purgeCancelledTtransactions = Integer.parseInt(v);
                }

                TransactionServiceConfigListener listener = habitat.getService(TransactionServiceConfigListener.class);
                listener.setTM(this);
            }
            ModuleMonitoringLevels levels = habitat.getService(ModuleMonitoringLevels.class);
            // running on the server side ?
            if (levels != null) {
                String level = levels.getTransactionService();
                if (!("OFF".equals(level))) {
                    monitoringEnabled = true;
                }
            }
        }

        // ENF OF BUG 4665539
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "TM: Tx Timeout = " + transactionTimeout);
        }

        // START IASRI 4705808 TTT004 -- monitor resource table stats
        try {
            // XXX TODO:
            if (Boolean.getBoolean("MONITOR_JTA_RESOURCE_TABLE_STATISTICS")) {
                registerStatisticMonitorTask();
            }

            StatsProviderManager.register("transaction-service", // element in domain.xml <monitoring-service>/<monitoring-level>
                    PluginPoint.SERVER, "transaction-service", // server.transaction-service node in asadmin get
                    new TransactionServiceStatsProvider(this, LOG));
        } catch (Exception ex) {
            // ignore
        }

        monitor = new TransactionServiceProbeProvider();
    }

    /**
     * Clears the transaction associated with the caller thread
     */
    @Override
    public void clearThreadTx() {
        setCurrentTransaction(null);
        delegates.set(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTxLogLocation() {
        return getDelegate().getTxLogLocation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerRecoveryResourceHandler(XAResource xaResource) {
        getDelegate().registerRecoveryResourceHandler(xaResource);
    }

    /****************************************************************************/
    /** Implementations of JavaEETransactionManager APIs **************************/
    /****************************************************************************/

    /**
     * Return true if a "null transaction context" was received from the client. See EJB2.0 spec section 19.6.2.1. A null tx
     * context has no Coordinator objref. It indicates that the client had an active tx but the client container did not
     * support tx interop.
     */
    @Override
    public boolean isNullTransaction() {
        return getDelegate().isNullTransaction();
    }

    @Override
    public void shutdown() {
        _timer.cancel();
    }

    @Override
    public void initRecovery(boolean force) {
        getDelegate().initRecovery(force);
    }

    @Override
    public void recover(XAResource[] resourceList) {
        getDelegate().recover(resourceList);
    }

    @Override
    public boolean enlistResource(Transaction tran, TransactionalResource h)
            throws RollbackException, IllegalStateException, SystemException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "\n\nIn JavaEETransactionManagerSimplified.enlistResource, h=" + h + " h.xares=" + h.getXAResource()
            /** +" h.alloc=" +h.getResourceAllocator() **/
                    + " tran=" + tran);
        }

        if (!h.isTransactional()) {
            return true;
        }

        // If LazyEnlistment is suspended, do not enlist resource.
        if (h.isEnlistmentSuspended()) {
            return false;
        }

        if (monitoringEnabled) {
            JavaEETransaction tx = getDelegate().getJavaEETransaction(tran);
            if (tx != null) {
                ((JavaEETransactionImpl) tx).addResourceName(h.getName());
            }
        }

        if (!(tran instanceof JavaEETransaction)) {
            return enlistXAResource(tran, h);
        }

        JavaEETransactionImpl tx = (JavaEETransactionImpl) tran;

        JavaEETransactionManagerDelegate d = setDelegate();
        boolean useLAO = d.useLAO();

        if ((tx.getNonXAResource() != null) && (!useLAO || !h.supportsXA())) {
            boolean isSameRM = false;
            try {
                isSameRM = h.getXAResource().isSameRM(tx.getNonXAResource().getXAResource());
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "\n\nIn JavaEETransactionManagerSimplified.enlistResource, isSameRM? " + isSameRM);
                }
            } catch (XAException xex) {
                throw new SystemException(sm.getString("enterprise_distributedtx.samerm_excep", xex));
            } catch (Exception ex) {
                throw new SystemException(sm.getString("enterprise_distributedtx.samerm_excep", ex));
            }
            if (!isSameRM) {
                throw new IllegalStateException(sm.getString("enterprise_distributedtx.already_has_nonxa"));
            }
        }

        if (h.supportsXA()) {
            if (!d.supportsXAResource()) {
                throw new IllegalStateException(sm.getString("enterprise_distributedtx.xaresource_not_supported"));
            }

            if (tx.isLocalTx()) {
                d.enlistLAOResource(tx, tx.getNonXAResource());

                /**
                 * XXX TO BE MOVED TO XA DELEGATE XXX ** startJTSTx(tx);
                 *
                 * //If transaction conatains a NonXA and no LAO, convert the existing //Non XA to LAO if(useLAO) {
                 * if(tx.getNonXAResource()!=null && (tx.getLAOResource()==null) ) { tx.setLAOResource(tx.getNonXAResource()); // XXX
                 * super.enlistLAOResource(tx, tx.getNonXAResource()); } } XXX TO BE MOVED TO XA DELEGATE XXX
                 **/
            }
            return enlistXAResource(tx, h);
        } else { // non-XA resource
            if (tx.isImportedTransaction()) {
                throw new IllegalStateException(sm.getString("enterprise_distributedtx.nonxa_usein_jts"));
            }
            if (tx.getNonXAResource() == null) {
                tx.setNonXAResource(h);
            }
            if (tx.isLocalTx()) {
                // notify resource that it is being used for tx,
                // e.g. this allows the correct physical connection to be
                // swapped in for the logical connection.
                // The flags parameter can be 0 because the flags are not
                // used by the XAResource implementation for non-XA resources.
                try {
                    h.getXAResource().start(tx.getLocalXid(), 0);
                } catch (XAException ex) {
                    throw new RuntimeException(sm.getString("enterprise_distributedtx.xaresource_start_excep"), ex);
                }

                h.enlistedInTransaction(tx);
                return true;
            } else {
                return d.enlistDistributedNonXAResource(tx, h);
                /**
                 * XXX TO BE MOVED TO XA DELEGATE? XXX if(useLAO) { return super.enlistResource(tx, h); } else { throw new
                 * IllegalStateException( sm.getString("enterprise_distributedtx.nonxa_usein_jts")); } XXX TO BE MOVED TO XA DELEGATE?
                 * XXX
                 **/
            }
        }
    }

    @Override
    public void unregisterComponentResource(TransactionalResource h) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE,
                    "\n\nIn JavaEETransactionManagerSimplified.unregisterComponentResource, h=" + h + " h.xares=" + h.getXAResource());
        }

        Object instance = h.getComponentInstance();
        if (instance == null) {
            return;
        }
        h.setComponentInstance(null);
        ComponentInvocation inv = invMgr.getCurrentInvocation();
        List l = getExistingResourceList(instance, inv);

        if (l != null) {
            l.remove(h);
        }
    }

    public void startJTSTx(JavaEETransaction t) throws RollbackException, IllegalStateException, SystemException {

        JavaEETransactionImpl tx = (JavaEETransactionImpl) t;
        TransactionInternal jtsTx = getDelegate().startJTSTx(tx, tx.isAssociatedTimeout());

        // The local Transaction was promoted to global Transaction
        if (monitoringEnabled) {
            if (activeTransactions.remove(tx)) {
                monitor.transactionDeactivatedEvent();
            }
        }

        tx.setJTSTx(jtsTx);
        jtsTx.registerSynchronization(new JTSSynchronization(jtsTx, this));
    }

    /**
     * get the resources being used in the calling component's invocation context
     *
     * @param instance Calling component instance
     * @param inv Calling component's invocation information
     * @return List of resources
     */
    @Override
    public List getResourceList(Object instance, ComponentInvocation inv) {
        if (inv == null) {
            return new ArrayList(0);
        }
        List l = null;

        /**
         * XXX EJB CONTAINER ONLY XXX -- NEED TO CHECK THE NEW CODE BELOW ** if (inv.getInvocationType() ==
         * ComponentInvocation.ComponentInvocationType.EJB_INVOCATION) { ComponentContext ctx = inv.context; if (ctx != null) l
         * = ctx.getResourceList(); else { l = new ArrayList(0); } } XXX EJB CONTAINER ONLY XXX
         **/

        ResourceHandler rh = inv.getResourceHandler();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "\n\nIn JavaEETransactionManagerSimplified.getResourceList, "
                    + ((rh == null) ? "" : (" ResourceHandler type: " + rh.getClass().getName())) + " ResourceHandler: " + rh);
        }

        if (rh != null) {
            l = rh.getResourceList();
            if (l == null) {
                l = new ArrayList(0);
            }
        } else {
            Object key = getResourceTableKey(instance, inv);
            if (key == null) {
                return new ArrayList(0);
            }
            l = (List) resourceTable.get(key);
            if (l == null) {
                l = new ArrayList(); // FIXME: use an optimum size?
                resourceTable.put(key, l);
            }
        }
        return l;
    }

    @Override
    public void enlistComponentResources() throws RemoteException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "TM: enlistComponentResources");
        }

        ComponentInvocation inv = invMgr.getCurrentInvocation();
        if (inv == null) {
            return;
        }
        try {
            Transaction tran = getTransaction();
            inv.setTransaction(tran);
            enlistComponentResources(inv);
        } catch (InvocationException ex) {
            LOG.log(Level.SEVERE, "enterprise_distributedtx.excep_in_enlist", ex);
            throw new RemoteException(ex.getMessage(), ex.getNestedException());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "enterprise_distributedtx.excep_in_enlist", ex);
            throw new RemoteException(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean delistResource(Transaction tran, TransactionalResource h, int flag) throws IllegalStateException, SystemException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE,
                    "\n\nIn JavaEETransactionManagerSimplified.delistResource, h=" + h + " h.xares=" + h.getXAResource() + " tran=" + tran);
        }

        if (!h.isTransactional()) {
            return true;
        }

        if (!(tran instanceof JavaEETransaction)) {
            return delistJTSResource(tran, h, flag);
        }

        JavaEETransactionImpl tx = (JavaEETransactionImpl) tran;
        if (tx.isLocalTx()) {
            // dissociate resource from tx
            try {
                h.getXAResource().end(tx.getLocalXid(), flag);
            } catch (XAException ex) {
                throw new RuntimeException(sm.getString("enterprise_distributedtx.xaresource_end_excep", ex));
            }
            return true;
        } else {
            return delistJTSResource(tran, h, flag);
        }
    }

    @Override
    public void delistComponentResources(boolean suspend) throws RemoteException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "TM: delistComponentResources");
        }
        ComponentInvocation inv = invMgr.getCurrentInvocation();
        // BEGIN IASRI# 4646060
        if (inv == null) {
            return;
        }
        // END IASRI# 4646060
        try {
            delistComponentResources(inv, suspend);
        } catch (InvocationException ex) {
            LOG.log(Level.SEVERE, "enterprise_distributedtx.excep_in_delist", ex);
            throw new RemoteException("", ex.getNestedException());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "enterprise_distributedtx.excep_in_delist", ex);
            throw new RemoteException("", ex);
        }
    }

    @Override
    public void registerComponentResource(TransactionalResource h) {
        ComponentInvocation inv = invMgr.getCurrentInvocation();
        if (inv != null) {
            Object instance = inv.getInstance();
            if (instance == null) {
                return;
            }
            h.setComponentInstance(instance);
            List l = getResourceList(instance, inv);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE,
                        "\n\nIn JavaEETransactionManagerSimplified.registerComponentResource, h=" + h + " h.xares=" + h.getXAResource());
            }

            l.add(h);
        }
    }

    private JavaEETransactionImpl initJavaEETransaction(int timeout) {
        JavaEETransactionImpl tx = null;
        // Do not need to use injection.
        if (timeout > 0) {
            tx = new JavaEETransactionImpl(timeout, this);
        } else {
            tx = new JavaEETransactionImpl(this);
        }

        setCurrentTransaction(tx);
        return tx;
    }

    @Override
    public List getExistingResourceList(Object instance, ComponentInvocation inv) {
        if (inv == null) {
            return null;
        }
        List l = null;

        /**
         * XXX EJB CONTAINER ONLY XXX -- NEED TO CHECK THE NEW CODE BELOW ** if (inv.getInvocationType() ==
         * ComponentInvocation.ComponentInvocationType.EJB_INVOCATION) { ComponentContext ctx = inv.context; if (ctx != null) l
         * = ctx.getResourceList(); return l; } XXX EJB CONTAINER ONLY XXX
         **/

        ResourceHandler rh = inv.getResourceHandler();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "\n\nIn JavaEETransactionManagerSimplified.getExistingResourceList, "
                    + ((rh == null) ? "" : (" ResourceHandler type: " + rh.getClass().getName())) + " ResourceHandler: " + rh);
        }

        if (rh != null) {
            l = rh.getResourceList();
        } else {
            Object key = getResourceTableKey(instance, inv);
            if (key != null) {
                l = (List) resourceTable.get(key);
            }
        }
        return l;
    }

    @Override
    public void preInvoke(ComponentInvocation prev) throws InvocationException {
        if (prev != null && prev.getTransaction() != null && prev.isTransactionCompleting() == false) {
            // do not worry about delisting previous invocation resources
            // if transaction is being completed
            delistComponentResources(prev, true); // delist with TMSUSPEND
        }

    }

    @Override
    public void postInvoke(ComponentInvocation curr, ComponentInvocation prev) throws InvocationException {

        if (curr != null && curr.getTransaction() != null)
         {
            delistComponentResources(curr, false); // delist with TMSUCCESS
        }
        if (prev != null && prev.getTransaction() != null && prev.isTransactionCompleting() == false) {
            // do not worry about re-enlisting previous invocation resources
            // if transaction is being completed
            enlistComponentResources(prev);
        }

    }

    @Override
    public void componentDestroyed(Object instance) {
        componentDestroyed(instance, null);
    }

    @Override
    public void componentDestroyed(Object instance, ComponentInvocation inv) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "TM: componentDestroyed" + instance);
            LOG.log(Level.FINE, "TM: resourceTable before: " + resourceTable.getEntryCount());
        }

        // Access resourceTable directly to avoid adding an empty list then removing it
        List l = (List) resourceTable.remove(getResourceTableKey(instance, inv));
        processResourceList(l);

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "TM: resourceTable after: " + resourceTable.getEntryCount());
        }
    }

    @Override
    public void componentDestroyed(ResourceHandler rh) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, " componentDestroyed: " + rh);
        }

        if (rh != null) {
            processResourceList(rh.getResourceList());
        }
    }

    @Override
    public boolean isTimedOut() {
        JavaEETransaction tx = transactions.get();
        if (tx != null) {
            return tx.isTimedOut();
        } else {
            return false;
        }
    }

    /**
     * Called from the CORBA Interceptors on the server-side when the server is replying to the client (local + remote
     * client). Check if there is an active transaction and remove it from TLS.
     */
    @Override
    public void checkTransactionImport() {
        // First check if this is a local call
        int[] count = (int[]) localCallCounter.get();
        if (count != null && count[0] > 0) {
            count[0]--;
            return;
        } else {
            // A remote call, clear TLS so that if this thread is reused
            // later, the current tx doesnt hang around.
            clearThreadTx();
        }
    }

    /**
     * Called from the CORBA Interceptors on the client-side when a client makes a call to a remote object (not in the same
     * JVM). Check if there is an active, exportable transaction.
     *
     * @exception RuntimeException if the transaction is not exportable
     */
    @Override
    public void checkTransactionExport(boolean isLocal) {

        if (isLocal) {
            // Put a counter in TLS indicating this is a local call.
            // Use int[1] as a mutable java.lang.Integer!
            int[] count = (int[]) localCallCounter.get();
            if (count == null) {
                count = new int[1];
                localCallCounter.set(count);
            }
            count[0]++;
            return;
        }

        JavaEETransaction tx = transactions.get();
        if (tx == null) {
            return;
        }

        if (!tx.isLocalTx()) { // a JTS tx, can be exported
            return;
        }

        // Check if a local tx with non-XA resource is being exported.
        // XXX what if this is a call on a non-transactional remote object ?
        if (tx.getNonXAResource() != null) {
            throw new RuntimeException(sm.getString("enterprise_distributedtx.cannot_export_transaction_having_nonxa"));
        }

        // If we came here, it means we have a local tx with no registered
        // resources, so start a JTS tx which can be exported.
        try {
            startJTSTx(tx);
        } catch (RollbackException rlex) {
            throw new RuntimeException(sm.getString("enterprise_distributedtx.unable_tostart_JTSTransaction"), rlex);
        } catch (IllegalStateException isex) {
            throw new RuntimeException(sm.getString("enterprise_distributedtx.unable_tostart_JTSTransaction"), isex);
        } catch (SystemException ex) {
            throw new RuntimeException(sm.getString("enterprise_distributedtx.unable_tostart_JTSTransaction"), ex);
        } catch (Exception excep) {
            throw new RuntimeException(sm.getString("enterprise_distributedtx.unable_tostart_JTSTransaction"), excep);
        }
    }

    /**
     * This is used by importing transactions via the Connector contract. Should not be called
     *
     * @return a <code>XATerminator</code> instance.
     * @throws UnsupportedOperationException
     */
    @Override
    public XATerminator getXATerminator() {
        return getDelegate().getXATerminator();
    }

    /**
     * Release a transaction. This call causes the calling thread to be dissociated from the specified transaction.
     * <p>
     * This is used by importing transactions via the Connector contract.
     *
     * @param xid the Xid object representing a transaction.
     */
    @Override
    public void release(Xid xid) throws WorkException {
        getDelegate().release(xid);
    }

    /**
     * Recreate a transaction based on the Xid. This call causes the calling thread to be associated with the specified
     * transaction.
     * <p>
     * This is used by importing transactions via the Connector contract.
     *
     * @param xid the Xid object representing a transaction.
     */
    @Override
    public void recreate(Xid xid, long timeout) throws WorkException {
        getDelegate().recreate(xid, timeout);
    }

    /****************************************************************************/
    /** Implementations of JTA TransactionManager APIs **************************/
    /****************************************************************************/

    @Override
    public void registerSynchronization(Synchronization sync) throws IllegalStateException, SystemException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "TM: registerSynchronization");
        }

        try {
            Transaction tran = getTransaction();
            if (tran != null) {
                tran.registerSynchronization(sync);
            }
        } catch (RollbackException ex) {
            LOG.log(Level.SEVERE, "enterprise_distributedtx.rollbackexcep_in_regsynch", ex);
            throw new IllegalStateException();
        }
    }

    // Implementation of begin() is moved to begin(int timeout)
    @Override
    public void begin() throws NotSupportedException, SystemException {
        begin(getEffectiveTimeout());
    }

    /**
     * This method is introduced as part of implementing the local transaction timeout capability. Implementation of begin()
     * moved here. Previpusly there is no timeout infrastructure for local txns, so when ever a timeout required for local
     * txn, it uses the globaltxn timeout infrastructure by doing an XA simulation.
     **/
    @Override
    public void begin(int timeout) throws NotSupportedException, SystemException {
        // Check if tx already exists
        if (transactions.get() != null) {
            throw new NotSupportedException(sm.getString("enterprise_distributedtx.notsupported_nested_transaction"));
        }

        setDelegate();

        // Check if JTS tx exists, without starting JTS tx.
        // This is needed in case the JTS tx was imported from a client.
        if (getStatus() != Status.STATUS_NO_TRANSACTION) {
            throw new NotSupportedException(sm.getString("enterprise_distributedtx.notsupported_nested_transaction"));
        }

        // START IASRI 4662745
        if (monitoringEnabled) {
            getDelegate().getReadLock().lock(); // XXX acquireReadLock();
            try {
                JavaEETransactionImpl tx = initJavaEETransaction(timeout);
                activeTransactions.add(tx);
                monitor.transactionActivatedEvent();
                ComponentInvocation inv = invMgr.getCurrentInvocation();
                if (inv != null && inv.getInstance() != null) {
                    tx.setComponentName(inv.getInstance().getClass().getName());
                }
            } finally {
                getDelegate().getReadLock().unlock(); // XXX releaseReadLock();
            }
        } else {
            initJavaEETransaction(timeout);
        }
        // START IASRI 4662745
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException,
            IllegalStateException, SystemException {

        boolean acquiredlock = false;
        try {
            JavaEETransaction tx = transactions.get();
            if (tx != null && tx.isLocalTx()) {
                if (monitoringEnabled) {
                    getDelegate().getReadLock().lock(); // XXX acquireReadLock();
                    acquiredlock = true;
                }
                tx.commit(); // commit local tx
            } else {
                try {
                    // an XA transaction
                    getDelegate().commitDistributedTransaction();
                } finally {
                    if (tx != null) {
                        ((JavaEETransactionImpl) tx).onTxCompletion(true);
                    }
                }
            }

        } finally {
            setCurrentTransaction(null); // clear current thread's tx
            delegates.set(null);
            if (acquiredlock) {
                getDelegate().getReadLock().unlock(); // XXX releaseReadLock();
            }
        }
        // END IASRI 4662745
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        boolean acquiredlock = false;
        try {
            JavaEETransaction tx = transactions.get();
            if (tx != null && tx.isLocalTx()) {
                if (monitoringEnabled) {
                    getDelegate().getReadLock().lock(); // XXX acquireReadLock();
                    acquiredlock = true;
                }
                tx.rollback(); // rollback local tx
            } else {
                try {
                    // an XA transaction
                    getDelegate().rollbackDistributedTransaction();
                } finally {
                    if (tx != null) {
                        ((JavaEETransactionImpl) tx).onTxCompletion(false);
                    }
                }
            }

        } finally {
            setCurrentTransaction(null); // clear current thread's tx
            delegates.set(null);
            if (acquiredlock) {
                getDelegate().getReadLock().unlock(); // XXX releaseReadLock();
            }
        }
    }

    @Override
    public int getStatus() throws SystemException {
        return getDelegate().getStatus();
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        return getDelegate().getTransaction();

        /**
         * XXX CHECK WHAT'S NEEDED FOR XA DELEGATE XXX ** TransactionInternal jtsTx = super.getTransaction(); if ( jtsTx == null
         * ) return null; else { // check if this JTS Transaction was previously active // in this JVM (possible for distributed
         * loopbacks). tx = (JavaEETransaction)globalTransactions.get(jtsTx); if ( tx == null ) { tx = new
         * JavaEETransaction(jtsTx, this); try { jtsTx.registerSynchronization( new JTSSynchronization(jtsTx, this)); } catch (
         * RollbackException rlex ) { throw new SystemException(rlex.toString()); } catch ( IllegalStateException isex ) { throw
         * new SystemException(isex.toString()); } catch ( Exception ex ) { throw new SystemException(ex.toString()); }
         *
         * globalTransactions.put(jtsTx, tx); } setCurrentTransaction(tx); // associate tx with thread return tx; } XXX CHECK
         * WHAT'S NEEDED FOR XA DELEGATE XXX
         **/
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {

        JavaEETransaction tx = transactions.get();
        // START IASRI 4662745
        if (tx == null || !tx.isLocalTx()) {
            getDelegate().setRollbackOnlyDistributedTransaction(); // probably a JTS imported tx
        } else {
            if (monitoringEnabled) {
                getDelegate().getReadLock().lock(); // XXX acquireReadLock();
                try {
                    tx.setRollbackOnly();
                } finally {
                    getDelegate().getReadLock().unlock(); // XXX releaseReadLock();
                }
            } else {
                tx.setRollbackOnly();
            }
        }
    }

    @Override
    public Transaction suspend() throws SystemException {
        return getDelegate().suspend(transactions.get());

        /**
         * XXX TO BE MOVED TO DELEGATES XXX ** if ( tx != null ) { if ( !tx.isLocalTx() ) super.suspend();
         *
         * setCurrentTransaction(null); return tx; } else { return super.suspend(); // probably a JTS imported tx } XXX TO BE
         * MOVED TO DELEGATES XXX
         **/
    }

    @Override
    public void resume(Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException {

        JavaEETransaction tx = transactions.get();
        if (tx != null) {
            throw new IllegalStateException(sm.getString("enterprise_distributedtx.transaction_exist_on_currentThread"));
        }

        if (tobj != null) {
            int status = tobj.getStatus();
            if (status == Status.STATUS_ROLLEDBACK || status == Status.STATUS_COMMITTED || status == Status.STATUS_NO_TRANSACTION
                    || status == Status.STATUS_UNKNOWN) {
                throw new InvalidTransactionException(sm.getString("enterprise_distributedtx.resume_invalid_transaction", tobj));
            }
        } else {
            throw new InvalidTransactionException(sm.getString("enterprise_distributedtx.resume_invalid_transaction", "null"));
        }

        if (tobj instanceof JavaEETransactionImpl) {
            JavaEETransactionImpl javaEETx = (JavaEETransactionImpl) tobj;
            if (!javaEETx.isLocalTx()) {
                getDelegate().resume(javaEETx.getJTSTx());
            }

            setCurrentTransaction(javaEETx);
        } else {
            getDelegate().resume(tobj); // probably a JTS imported tx
        }
    }

    /**
     * Modify the value of the timeout value that is associated with the transactions started by the current thread with the
     * begin method.
     *
     * <p>
     * If an application has not called this method, the transaction service uses some default value for the transaction
     * timeout.
     *
     * @exception SystemException Thrown if the transaction manager encounters an unexpected error condition
     *
     */
    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
        if (seconds < 0) {
            throw new SystemException(sm.getString("enterprise_distributedtx.invalid_timeout"));
        }

        txnTmout.set(seconds);
        // transactionTimeout = seconds;
    }

    /**
     * Modify the value to be used to purge transaction tasks after the specified number of cancelled tasks.
     */
    @Override
    public void setPurgeCancelledTtransactionsAfter(int num) {
        purgeCancelledTtransactions = num;
    }

    /**
     * Returns the value to be used to purge transaction tasks after the specified number of cancelled tasks.
     */
    @Override
    public int getPurgeCancelledTtransactionsAfter() {
        return purgeCancelledTtransactions;
    }

    @Override
    public JavaEETransaction getCurrentTransaction() {
        return transactions.get();
    }

    @Override
    public void setCurrentTransaction(JavaEETransaction t) {
        transactions.set(t);
    }

    @Override
    public XAResourceWrapper getXAResourceWrapper(String clName) {
        return getDelegate().getXAResourceWrapper(clName);
    }

    @Override
    public void handlePropertyUpdate(String name, Object value) {
        delegate.handlePropertyUpdate(name, value);
        // XXX Check if the current delegate needs to be called as well.
    }

    @Override
    public boolean recoverIncompleteTx(boolean delegated, String logPath, XAResource[] xaresArray) throws Exception {
        return delegate.recoverIncompleteTx(delegated, logPath, xaresArray);
    }

    /****************************************************************************/
    /*********************** Called by Admin Framework **************************/
    /****************************************************************************/
    /*
     * Called by Admin Framework to freeze the transactions.
     */
    @Override
    public synchronized void freeze() {
        getDelegate().acquireWriteLock();
        monitor.freezeEvent(true);
    }

    /*
     * Called by Admin Framework to freeze the transactions. These undoes the work done by the freeze.
     */
    @Override
    public synchronized void unfreeze() {
        getDelegate().releaseWriteLock();
        monitor.freezeEvent(false);
    }

    /**
     * XXX ???
     */
    @Override
    public boolean isFrozen() {
        return getDelegate().isWriteLocked();
    }

    @Override
    public void cleanTxnTimeout() {
        txnTmout.set(null);
    }

    public int getEffectiveTimeout() {
        Integer tmout = txnTmout.get();
        if (tmout == null) {
            return transactionTimeout;
        } else {
            return tmout;
        }
    }

    @Override
    public void setDefaultTransactionTimeout(int seconds) {
        if (seconds < 0) {
            seconds = 0;
        }
        transactionTimeout = seconds;
    }

    /*
     * This method returns the details of the Currently Active Transactions Called by Admin Framework when transaction
     * monitoring is enabled
     *
     * @return ArrayList of TransactionAdminBean
     *
     * @see TransactionAdminBean
     */
    @Override
    public ArrayList getActiveTransactions() {
        ArrayList tranBeans = new ArrayList();
        txnTable = new Hashtable();
        Object[] activeCopy = activeTransactions.toArray(); // get the clone of the active transactions
        for (Object element : activeCopy) {
            try {
                Transaction tran = (Transaction) element;
                TransactionAdminBean tBean = getDelegate().getTransactionAdminBean(tran);
                if (tBean == null) {
                    // Shouldn't happen
                    LOG.warning("enterprise_distributedtx.txbean_null" + tran);
                } else {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "TM: Adding txnId " + tBean.getId() + " to txnTable");
                    }

                    txnTable.put(tBean.getId(), tran);
                    tranBeans.add(tBean);
                }
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "transaction.monitor.error_while_getting_monitor_attr", ex);
            }
        }
        return tranBeans;
    }

    public TransactionAdminBean getTransactionAdminBean(Transaction tran) throws jakarta.transaction.SystemException {

        TransactionAdminBean tBean = null;
        if (tran instanceof JavaEETransaction) {
            JavaEETransactionImpl tran1 = (JavaEETransactionImpl) tran;
            String id = tran1.getTransactionId();
            long startTime = tran1.getStartTime();
            String componentName = tran1.getComponentName();
            ArrayList<String> resourceNames = tran1.getResourceNames();
            long elapsedTime = System.currentTimeMillis() - startTime;
            String status = getStatusAsString(tran.getStatus());

            tBean = new TransactionAdminBean(tran, id, status, elapsedTime, componentName, resourceNames);
        }

        return tBean;
    }

    /*
     * Called by Admin Framework when transaction monitoring is enabled
     */
    @Override
    public void forceRollback(String txnId) throws IllegalStateException, SystemException {
        // XXX - WORK AROUND MONITORING BUG
        if (txnTable == null || txnTable.size() == 0)
         {
            getActiveTransactions();
        // XXX - WORK AROUND MONITORING BUG
        }

        if (txnTable == null || txnTable.get(txnId) == null) {
            String result = sm.getString("transaction.monitor.rollback_invalid_id");
            throw new IllegalStateException(result);
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "TM: Marking txnId " + txnId + " for rollback");
            }

            ((Transaction) txnTable.get(txnId)).setRollbackOnly();
        }

    }

    @Override
    public void setMonitoringEnabled(boolean enabled) {
        monitoringEnabled = enabled;
        // reset the variables
        activeTransactions.clear();
    }

    private void _monitorTxCompleted(Object obj, boolean committed) {
        if (obj != null) {
            if (obj instanceof JavaEETransactionImpl) {
                JavaEETransactionImpl t = (JavaEETransactionImpl) obj;
                if (!t.isLocalTx()) {
                    obj = t.getJTSTx();
                }
            }
            if (activeTransactions.remove(obj)) {
                if (committed) {
                    monitor.transactionCommittedEvent();
                } else {
                    monitor.transactionRolledbackEvent();
                }
            } else {
                // WARN ???
            }
        }
    }

    // Mods: Adding method for statistic dumps using TimerTask
    private void registerStatisticMonitorTask() {
        TimerTask task = new StatisticMonitorTask();
        // for now, get monitoring interval from system prop
        int statInterval = 2 * 60 * 1000;
        try {
            String interval = System.getProperty("MONITOR_JTA_RESOURCE_TABLE_SECONDS");
            int temp = Integer.parseInt(interval);
            if (temp > 0) {
                statInterval = temp;
            }
        } catch (Exception ex) {
            // ignore
        }

        _timer.scheduleAtFixedRate(task, 0, statInterval);
    }

    // Mods: Adding TimerTask class for statistic dumps
    class StatisticMonitorTask extends TimerTask {
        @Override
        public void run() {
            if (resourceTable != null) {
                Map stats = resourceTable.getStats();
                Iterator it = stats.entrySet().iterator();
                LOG.log(Level.INFO, "********** JavaEETransactionManager resourceTable stats *****");
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    LOG.log(Level.INFO, (String) entry.getKey() + ": " + entry.getValue());
                }
            }
        }
    }

    /****************************************************************************/
    /************************* Helper Methods ***********************************/
    /****************************************************************************/
    public static String getStatusAsString(int status) {
        return (String) statusMap.get(status);
    }

    private void delistComponentResources(ComponentInvocation inv, boolean suspend) throws InvocationException {

        try {
            Transaction tran = (Transaction) inv.getTransaction();
            if (isTransactionActive(tran)) {
                List l = getExistingResourceList(inv.getInstance(), inv);
                if (l == null || l.size() == 0) {
                    return;
                }

                int flag = (suspend) ? XAResource.TMSUSPEND : XAResource.TMSUCCESS;
                Iterator it = l.iterator();
                while (it.hasNext()) {
                    TransactionalResource h = (TransactionalResource) it.next();
                    try {
                        if (h.isEnlisted()) {
                            delistResource(tran, h, flag);
                        }
                    } catch (IllegalStateException ex) {
                        if (LOG.isLoggable(Level.FINE))
                         {
                            LOG.log(Level.FINE, "TM: Exception in delistResource", ex);
                        // ignore error due to tx time out
                        }
                    } catch (Exception ex) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "TM: Exception in delistResource", ex);
                        }
                        it.remove();
                        handleResourceError(h, ex, tran);
                    }
                }
                // END OF IASRI 4658504
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "enterprise_distributedtx.excep_in_delist", ex);
        }
    }

    protected boolean enlistXAResource(Transaction tran, TransactionalResource h)
            throws RollbackException, IllegalStateException, SystemException {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "\n\nIn JavaEETransactionManagerSimplified.enlistXAResource, h=" + h + " h.xares=" + h.getXAResource()
                    + " tran=" + tran);
        }

        if (resourceEnlistable(h)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "\n\nIn JavaEETransactionManagerSimplified.enlistXAResource - enlistable");
            }

            XAResource res = h.getXAResource();
            boolean result = tran.enlistResource(res);
            if (!h.isEnlisted()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "\n\nIn JavaEETransactionManagerSimplified.enlistXAResource - enlist");
                }
            }

            h.enlistedInTransaction(tran);
            return result;
        } else {
            return true;
        }
    }

    private void enlistComponentResources(ComponentInvocation inv) throws InvocationException {

        try {
            Transaction tran = (Transaction) inv.getTransaction();
            if (isTransactionActive(tran)) {
                List l = getExistingResourceList(inv.getInstance(), inv);
                if (l == null || l.size() == 0) {
                    return;
                }
                Iterator it = l.iterator();
                // END IASRI 4705808 TTT002
                while (it.hasNext()) {
                    TransactionalResource h = (TransactionalResource) it.next();
                    try {
                        enlistResource(tran, h);
                    } catch (Exception ex) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.WARNING, "enterprise_distributedtx.pooling_excep", ex);
                        }

                        it.remove();
                        handleResourceError(h, ex, tran);
                    }
                }
                // END OF IASRI 4658504
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "enterprise_distributedtx.excep_in_enlist", ex);
        }
    }

    /**
     * Called by #componentDestroyed()
     */
    private void processResourceList(List l) {
        if (l != null && l.size() > 0) {
            Iterator it = l.iterator();
            while (it.hasNext()) {
                TransactionalResource h = (TransactionalResource) it.next();
                try {
                    h.closeUserConnection();
                } catch (Exception ex) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.WARNING, "enterprise_distributedtx.pooling_excep", ex);
                    }
                }
            }
            l.clear();
        }
    }

    private void handleResourceError(TransactionalResource h, Exception ex, Transaction tran) {

        if (LOG.isLoggable(Level.FINE)) {
            if (h.isTransactional()) {
                LOG.log(Level.FINE, "TM: HandleResourceError " + h.getXAResource() + ", " + ex);
            }
        }
        try {
            if (tran != null && h.isTransactional() && h.isEnlisted()) {
                tran.delistResource(h.getXAResource(), XAResource.TMSUCCESS);
            }
        } catch (Exception ex2) {
            // ignore
        }

        if (ex instanceof RollbackException) {
            // transaction marked as rollback
            return;
        } else if (ex instanceof IllegalStateException) {
            // transaction aborted by time out
            // close resource
            try {
                h.closeUserConnection();
            } catch (Exception ex2) {
                // Log.err.println(ex2);
            }
        }
    }

    private Object getResourceTableKey(Object instance, ComponentInvocation inv) {
        Object key = null;
        if (inv != null) {
            key = inv.getResourceTableKey();
        }

        // If ComponentInvocation is null or doesn't hold the key,
        // use instance as the key.
        if (key == null) {
            key = instance;
        }
        return key;
    }

    private boolean isTransactionActive(Transaction tran) {
        return (tran != null);
    }

    /**
     * JTS version of the #delistResource
     *
     * @param suspend true if the transaction association should be suspended rather than ended.
     */
    private boolean delistJTSResource(Transaction tran, TransactionalResource h, int flag) throws IllegalStateException, SystemException {

// ** XXX Throw an exception instead ??? XXX **
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "\n\nIn JavaEETransactionManagerSimplified.delistJTSResource, h=" + h + " h.xares=" + h.getXAResource()
                    + " tran=" + tran + " flag=" + flag);
        }

        if (!h.isShareable() || multipleEnlistDelists) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE,
                        "\n\nIn JavaEETransactionManagerSimplified.delistJTSResource " + "- !h.isShareable() || multipleEnlistDelists");
            }

            if (h.isTransactional() && h.isEnlisted()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "\n\nIn JavaEETransactionManagerSimplified.delistJTSResource - delist");
                }

                return tran.delistResource(h.getXAResource(), flag);
            } else {
                return true;
            }
        }
        return true;
    }

    private void remove(Transaction tx) {
        getDelegate().removeTransaction(tx);

        /**
         * XXX TO BE MOVED TO XA DELEGATE XXX javaEETM.globalTransactions.remove(jtsTx); XXX TO BE MOVED TO XA DELEGATE XXX
         **/
    }

    /**
     * Called by JavaEETransactionImpl also
     */
    synchronized JavaEETransactionManagerDelegate getDelegate() {
        JavaEETransactionManagerDelegate d = delegates.get();
        return (d == null) ? delegate : d;
    }

    private JavaEETransactionManagerDelegate setDelegate() {
        JavaEETransactionManagerDelegate d = delegates.get();
        if (d == null) {
            d = delegate;
            delegates.set(d);
        }

        return d;
    }

    public boolean isDelegate(JavaEETransactionManagerDelegate d) {
        if (delegate == null) {
            return false;
        }

        return (d.getClass().getName().equals(delegate.getClass().getName()));
    }

    private void initDelegates() {
        if (habitat == null)
         {
            return; // the delegate will be set explicitly
        }

        for (JavaEETransactionManagerDelegate d : habitat
                .<JavaEETransactionManagerDelegate>getAllServices(JavaEETransactionManagerDelegate.class)) {
            setDelegate(d);
        }

        if (delegate != null && LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.INFO, "enterprise_used_delegate_name", delegate.getClass().getName());
        }

    }

    @Override
    public synchronized void setDelegate(JavaEETransactionManagerDelegate d) {
        // XXX Check if it's valid to set or if we need to remember all that asked.

        int curr = 0;
        if (delegate != null) {
            curr = delegate.getOrder();
        }
        if (d.getOrder() > curr) {
            delegate = d;

            // XXX Hk2 work around XXX
            delegate.setTransactionManager(this);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Replaced delegate with " + d.getClass().getName());
            }
        }
    }

    public Logger getLogger() {
        return LOG;
    }

    public void monitorTxCompleted(Object obj, boolean b) {
        if (monitoringEnabled) {
            _monitorTxCompleted(obj, b);
        }
    }

    public void monitorTxBegin(Transaction tx) {
        if (monitoringEnabled) {
            activeTransactions.add(tx);
            monitor.transactionActivatedEvent();
        }
    }

    public boolean resourceEnlistable(TransactionalResource h) {
        return (h.isTransactional() && (!h.isEnlisted() || !h.isShareable() || multipleEnlistDelists));
    }

    public boolean isInvocationStackEmpty() {
        return (invMgr == null || invMgr.isInvocationStackEmpty());
    }

    public void setTransactionCompeting(boolean b) {
        ComponentInvocation curr = invMgr.getCurrentInvocation();
        if (curr != null) {
            curr.setTransactionCompeting(b);
        }
    }

    public JavaEETransaction createImportedTransaction(TransactionInternal jtsTx) throws SystemException {
        JavaEETransactionImpl tx = new JavaEETransactionImpl(jtsTx, this);
        try {
            jtsTx.registerSynchronization(new JTSSynchronization(jtsTx, this));
        } catch (RollbackException rlex) {
            throw new SystemException(rlex.toString());
        } catch (IllegalStateException isex) {
            throw new SystemException(isex.toString());
        } catch (Exception ex) {
            throw new SystemException(ex.toString());
        }

        return tx;
    }

    /****************************************************************************/
    /** Implementation of jakarta.transaction.Synchronization *********************/
    /****************************************************************************/
    private static class JTSSynchronization implements Synchronization {
        private final TransactionInternal jtsTx;
        private final JavaEETransactionManagerSimplified javaEETM;

        JTSSynchronization(TransactionInternal jtsTx, JavaEETransactionManagerSimplified javaEETM) {
            this.jtsTx = jtsTx;
            this.javaEETM = javaEETM;
        }

        @Override
        public void beforeCompletion() {
        }

        @Override
        public void afterCompletion(int status) {
            javaEETM.remove(jtsTx);
        }
    }
}
