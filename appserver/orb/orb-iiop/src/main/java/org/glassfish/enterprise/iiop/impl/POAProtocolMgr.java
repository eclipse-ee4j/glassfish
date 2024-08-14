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

package org.glassfish.enterprise.iiop.impl;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.ior.TaggedProfile;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactory;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactoryManager ;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.util.Utility;
import com.sun.logging.LogDomains;

import jakarta.ejb.NoSuchObjectLocalException;
import jakarta.ejb.TransactionRequiredLocalException;
import jakarta.ejb.TransactionRolledbackLocalException;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.rmi.CORBA.Tie;

import org.glassfish.enterprise.iiop.api.ProtocolManager;
import org.glassfish.enterprise.iiop.api.RemoteReferenceFactory;
import org.glassfish.enterprise.iiop.spi.EjbContainerFacade;
import org.glassfish.enterprise.iiop.spi.EjbService;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.Policy;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

/**
 * This class implements the ProtocolManager interface for the
 * RMI/IIOP ORB with POA (Portable Object Adapter).
 * Note that the POA is now accessed only through the
 * ReferenceFactoryManager for EJB.
 *
 * @author Vivek Nagar
 */
@Service
public final class POAProtocolMgr extends org.omg.CORBA.LocalObject implements ProtocolManager {

    private static final Logger _logger = LogDomains.getLogger(POAProtocolMgr.class, LogDomains.CORBA_LOGGER);

    private static final int MAPEXCEPTION_CODE = 9998;

    private ORB orb;

    private ReferenceFactoryManager rfm = null ;

    private PresentationManager presentationMgr;

    @Inject
    private ServiceLocator services;

    public POAProtocolMgr() {
    }

    @Inject
    private Provider<EjbService> ejbServiceProvider;

    @Override
    public void initialize(org.omg.CORBA.ORB o) {
        this.orb = (ORB)o;
        this.presentationMgr = ORB.getPresentationManager();
    }


    // Called in all VMs, must be called only after InitialNaming is available
    @Override
    public void initializePOAs() throws Exception {
        // NOTE:  The RootPOA manager used to be activated here.
        getRFM();
        _logger.log(Level.FINE, "POAProtocolMgr.initializePOAs: RFM resolved and activated");
    }

    private static class RemoteNamingServantLocator extends LocalObject implements ServantLocator {

        private final ORB orb;
        private final Servant servant;

        public RemoteNamingServantLocator(ORB orb, Remote impl) {
            this.orb = orb;
            Tie tie = ORB.getPresentationManager().getTie();
            tie.setTarget(impl);
            servant = Servant.class.cast(tie);
        }

        @Override
        public synchronized Servant preinvoke(byte[] oid, POA adapter, String operation, CookieHolder the_cookie)
            throws ForwardRequest {
            return servant;
        }

        @Override
        public void postinvoke(byte[] oid, POA adapter, String operation, Object the_cookie, Servant the_servant) {
        }
    }

    private synchronized ReferenceFactoryManager getRFM() {
        if (rfm == null) {
            try {
                rfm = ReferenceFactoryManager.class.cast(orb.resolve_initial_references("ReferenceFactoryManager"));
                rfm.activate();
            } catch (Exception exc) {
                // do nothing
            }
        }

        return rfm ;
    }


    private org.omg.CORBA.Object getRemoteNamingReference(Remote remoteNamingProvider) {
        final ServantLocator locator = new RemoteNamingServantLocator(orb, remoteNamingProvider);
        final PresentationManager pm = ORB.getPresentationManager();

        String repositoryId;
        try {
            repositoryId = pm.getRepositoryId(remoteNamingProvider);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

        final List<Policy> policies = new ArrayList<>();
        final ReferenceFactory rf = getRFM().create("RemoteSerialContextProvider", repositoryId, policies, locator);

        // arbitrary
        final byte[] oid = {0, 3, 5, 7, 2, 37, 42};

        final org.omg.CORBA.Object ref = rf.createReference(oid);
        return ref;
    }


    @Override
    public void initializeRemoteNaming(Remote remoteNamingProvider) throws Exception {
        try {
            org.omg.CORBA.Object provider = getRemoteNamingReference(remoteNamingProvider);

            // put object in NameService
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
            // XXX use constant for SerialContextProvider name
            NameComponent nc = new NameComponent("SerialContextProvider", "");

            NameComponent path[] = {nc};
            ncRef.rebind(path, provider);
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "enterprise_naming.excep_in_insertserialcontextprovider", ex);

            RemoteException re = new RemoteException("initSerialCtxProvider error", ex);
            throw re;
        }

    }

    // Called only in J2EE Server VM
    @Override
    public void initializeNaming() throws Exception {
        // NOTE: The TransientNameService reference is NOT HA.
        // new TransientNameService((com.sun.corba.ee.spi.orb.ORB)orb);
        // _logger.log(Level.FINE, "POAProtocolMgr.initializeNaming: complete");
    }


    /**
     * Return a factory that can be used to create/destroy remote
     * references for a particular EJB type.
     * @param container The container to use
     * @param remoteHomeView The remote home view
     * @param id The object id
     * @return the ref factory
     */
    @Override
    public RemoteReferenceFactory getRemoteReferenceFactory(
        EjbContainerFacade container, boolean remoteHomeView, String id) {
        RemoteReferenceFactory factory = new POARemoteReferenceFactory(container, this, orb, remoteHomeView, id);
        return factory;
    }


    /**
     * Connect the RMI object to the protocol.
     */
    @Override
    public void connectObject(Remote remoteObj) throws RemoteException {
        StubAdapter.connect(remoteObj, orb);
    }


    @Override
    public boolean isStub(Object obj) {
        return StubAdapter.isStub(obj);
    }


    @Override
    public boolean isLocal(Object obj) {
        return StubAdapter.isLocal(obj);
    }


    @Override
    public byte[] getObjectID(org.omg.CORBA.Object obj) {
        IOR ior = orb.getIOR(obj, false);
        java.util.Iterator iter = ior.iterator();

        byte[] oid = null;
        if (iter.hasNext()) {
            TaggedProfile profile = (TaggedProfile) iter.next();
            ObjectKey objKey = profile.getObjectKey();
            oid = objKey.getId().getId();
        }

        return oid;
    }

    /**
     * Return true if the two object references refer to the same
     * remote object.
     */
    @Override
    public boolean isIdentical(Remote obj1, Remote obj2) {
        if (obj1 instanceof org.omg.CORBA.Object && obj2 instanceof org.omg.CORBA.Object) {
            org.omg.CORBA.Object corbaObj1 = (org.omg.CORBA.Object)obj1;
            org.omg.CORBA.Object corbaObj2 = (org.omg.CORBA.Object)obj2;

            return corbaObj1._is_equivalent(corbaObj2);
        } else {
            return false;
        }
    }

    @Override
    public void validateTargetObjectInterfaces(Remote targetObj) {
        if (targetObj != null) {
            // All Remote interfaces implemented by targetObj will be
            // validated as a side-effect of calling setTarget().
            // A runtime exception will be propagated if validation fails.
            Tie tie = presentationMgr.getTie();
            tie.setTarget(targetObj);
        } else {
            throw new IllegalArgumentException("null passed to validateTargetObjectInterfaces");
        }

    }

    /**
     * Map the EJB/RMI exception to a protocol-specific (e.g. CORBA) exception
     */
    @Override
    public Throwable mapException(Throwable exception) {
        boolean initCause = true;
        Throwable mappedException = exception;

        if (exception instanceof java.rmi.NoSuchObjectException || exception instanceof NoSuchObjectLocalException) {
            mappedException = new OBJECT_NOT_EXIST(MAPEXCEPTION_CODE, CompletionStatus.COMPLETED_MAYBE);
        } else if (exception instanceof java.rmi.AccessException
            || exception instanceof jakarta.ejb.AccessLocalException) {
            mappedException = new NO_PERMISSION(MAPEXCEPTION_CODE, CompletionStatus.COMPLETED_MAYBE);
        } else if (exception instanceof java.rmi.MarshalException) {
            mappedException = new MARSHAL(MAPEXCEPTION_CODE, CompletionStatus.COMPLETED_MAYBE);
        } else if (exception instanceof jakarta.transaction.TransactionRolledbackException
            || exception instanceof TransactionRolledbackLocalException) {
            mappedException = new TRANSACTION_ROLLEDBACK(MAPEXCEPTION_CODE, CompletionStatus.COMPLETED_MAYBE);
        } else if (exception instanceof jakarta.transaction.TransactionRequiredException
            || exception instanceof TransactionRequiredLocalException) {
            mappedException = new TRANSACTION_REQUIRED(MAPEXCEPTION_CODE, CompletionStatus.COMPLETED_MAYBE);
        } else if (exception instanceof jakarta.transaction.InvalidTransactionException) {
            mappedException = new INVALID_TRANSACTION(MAPEXCEPTION_CODE, CompletionStatus.COMPLETED_MAYBE);
        } else {
            initCause = false;
        }

        if (initCause) {
            mappedException.initCause(exception);
        }

        return mappedException;
    }


    /**
     * Called from SecurityMechanismSelector for each objref creation
     */
    @Override
    public EjbDescriptor getEjbDescriptor(byte[] ejbKey) {
        EjbDescriptor result = null;

        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "POAProtocolMgr.getEjbDescriptor->: {0}", ejbKey);
            }

            if (ejbKey.length < POARemoteReferenceFactory.EJBID_OFFSET + 8) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "POAProtocolMgr.getEjbDescriptor: {0}: {1} < {2}{3}",
                        new Object[] {ejbKey, ejbKey.length, POARemoteReferenceFactory.EJBID_OFFSET, 8});
                }

                return null;
            }

            long ejbId = Utility.bytesToLong(ejbKey, POARemoteReferenceFactory.EJBID_OFFSET);

            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "POAProtocolMgr.getEjbDescriptor: {0}: ejbId: {1}",
                    new Object[] {ejbKey, ejbId});
            }

            EjbService ejbService = ejbServiceProvider.get();

            result = ejbService.ejbIdToDescriptor(ejbId);
        } finally {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "POAProtocolMgr.getEjbDescriptor<-: {0}: {1}", new Object[] {ejbKey, result});
            }
        }

        return result;
   }
}
