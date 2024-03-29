/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.corba.ee.org.omg.CORBA.SUNVMCID;
import com.sun.corba.ee.spi.extension.CopyObjectPolicy;
import com.sun.corba.ee.spi.extension.RequestPartitioningPolicy;
import com.sun.corba.ee.spi.extension.ServantCachingPolicy;
import com.sun.corba.ee.spi.extension.ZeroPortPolicy;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.ior.TaggedProfile;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactory;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactoryManager;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;
import com.sun.corba.ee.spi.threadpool.ThreadPoolManager;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.util.Utility;
import com.sun.logging.LogDomains;

import jakarta.ejb.NoSuchObjectLocalException;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.Remote;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.Util;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.enterprise.iiop.api.RemoteReferenceFactory;
import org.glassfish.enterprise.iiop.spi.EjbContainerFacade;
import org.glassfish.enterprise.iiop.util.S1ASThreadPoolManager;
import org.glassfish.pfl.dynamic.codegen.spi.Wrapper;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.Policy;
import org.omg.CORBA.portable.Delegate;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

/**
 * This class implements the RemoteReferenceFactory interface for the
 * RMI/IIOP ORB with POA (Portable Object Adapter).
 * There is one instance of the POARemoteReferenceFactory for each
 * EJB type.
 *
 * It also implements the preinvoke/postinvoke APIs in the
 * POA's ServantLocator interface, which are called before/after
 * every invocation (local or remote).
 * It creates a RMI-IIOP-POA object reference (a stub) for every EJBObject
 * and EJBHome in the EJB container.
 *
 * @author Kenneth Saks
 */

public final class POARemoteReferenceFactory extends org.omg.CORBA.LocalObject
    implements RemoteReferenceFactory, ServantLocator {

    static final int PASS_BY_VALUE_ID = 0;
    static final int PASS_BY_REFERENCE_ID = 1;

    static final int OTS_POLICY_TYPE = SUNVMCID.value + 123;
    static final int CSIv2_POLICY_TYPE = SUNVMCID.value + 124;
    static final int REQUEST_DISPATCH_POLICY_TYPE = SUNVMCID.value + 125;
    static final int SFSB_VERSION_POLICY_TYPE = SUNVMCID.value + 126;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LogDomains.CORBA_LOGGER);
    private static final int GET_TIE_EXCEPTION_CODE = 9999;

    private EjbContainerFacade container;
    private EjbDescriptor ejbDescriptor;
    private final ClassLoader appClassLoader;

    private ORB orb;
    private POAProtocolMgr protocolMgr;
    private final PresentationManager presentationMgr;

    private ReferenceFactory ejbHomeReferenceFactory ;
    private PresentationManager.StubFactory ejbHomeStubFactory;
    private String ejbHomeRepositoryId;

    private ReferenceFactory ejbObjectReferenceFactory ;
    private PresentationManager.StubFactory ejbObjectStubFactory;
    private String ejbObjectRepositoryId;

    private String remoteBusinessIntf;

    // true if remote home view.  false if remote business view.
    // Used when getting target object for an invocation.
    private final boolean isRemoteHomeView;

    private final String poaId_EJBHome;
    private final String poaId_EJBObject;

    // The EJB key format with field-name(size in bytes):
    // -----------------------------------------
    // | EJB ID(8) | INSTANCEKEY | INSTANCEKEY |
    // |           | LENGTH(4)   |   (unknown) |
    // -----------------------------------------
    // The following are the offsets for the fields in the EJB key.
    static final int EJBID_OFFSET = 0;
    private static final int INSTANCEKEYLEN_OFFSET = 8;
    private static final int INSTANCEKEY_OFFSET = 12;

    POARemoteReferenceFactory(EjbContainerFacade container, POAProtocolMgr protocolMgr,
                  ORB orb, boolean remoteHomeView, String id) {

        this.protocolMgr = protocolMgr;
        this.orb = orb;
        this.poaId_EJBHome   = id + "-EJBHome";
        this.poaId_EJBObject = id + "-EJBObject";
        this.presentationMgr = ORB.getPresentationManager();
        this.container = container;
        this.ejbDescriptor = container.getEjbDescriptor();
        this.isRemoteHomeView = remoteHomeView;

        appClassLoader = container.getClassLoader();

        // NOTE: ReferenceFactory creation happens in setRepositoryIds.
    }

    @Override
    public int getCSIv2PolicyType() {
        return CSIv2_POLICY_TYPE;
    }

    /*
    private String getRepositoryId(Class c) throws Exception {

        // Using PresentationManager to get repository ID will always work,
        // independent of whether we have generated static RMI-IIOP stubs.

        PresentationManager.ClassData cData = presentationMgr.getClassData(c);
        String[] typeIds = cData.getTypeIds();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, ".getRepositoryId: {0}", typeIds[0]);
        }

        // Repository id is always 1st element in array.
        return typeIds[0];
    }
     */


    @Override
    public void setRepositoryIds(Class homeIntf, Class remoteIntf) {
        PresentationManager.StubFactoryFactory sff = ORB.getStubFactoryFactory();

        // Home
        ejbHomeStubFactory = sff.createStubFactory(homeIntf.getName(), false, "", null, appClassLoader);
        String[] ejbHomeTypeIds = ejbHomeStubFactory.getTypeIds();
        ejbHomeRepositoryId = ejbHomeTypeIds[0];

        ejbObjectStubFactory = sff.createStubFactory(remoteIntf.getName(), false, "", null, appClassLoader);

        String[] ejbObjectTypeIds = ejbObjectStubFactory.getTypeIds();

        ejbObjectRepositoryId = ejbObjectTypeIds[0];

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, ".setRepositoryIds:" + " " + "{0} {1}",
                new Object[] {ejbHomeRepositoryId, ejbObjectRepositoryId});
        }

        try {

            ejbHomeReferenceFactory = createReferenceFactory(poaId_EJBHome, ejbHomeRepositoryId);
            ejbObjectReferenceFactory = createReferenceFactory(poaId_EJBObject, ejbObjectRepositoryId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!isRemoteHomeView) {
            remoteBusinessIntf = remoteIntf.getName();
        }
    }

    @Override
    public void cleanupClass(Class clazz) {
        try {
            presentationMgr.flushClass(clazz);
        } catch(Exception e) {
            logger.log(Level.FINE, "cleanupClass error", e);
        }
    }

    private ReferenceFactory createReferenceFactory(String poaId, String repoid ) throws Exception {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, ".createReferenceFactory->: {0} {1}", new Object[] {poaId, repoid});
            }

            ReferenceFactoryManager rfm
                = (ReferenceFactoryManager) orb.resolve_initial_references(ORBConstants.REFERENCE_FACTORY_MANAGER);

            List<Policy> policies = new ArrayList<>();

            // Servant caching for local RMI-IIOP invocation performance
            policies.add(ServantCachingPolicy.getPolicy());

            // OTS Policy
            policies.add(new OTSPolicyImpl());

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, ".createReferenceFactory: {0} {1}: {2}",
                    new Object[] {poaId, repoid, ejbDescriptor});

            }

            // CSIv2 Policy
            policies.add(new CSIv2Policy(ejbDescriptor));

            String threadPoolName = container.getUseThreadPoolId();
            int threadPoolNumericID = 0;
            boolean usePassByReference = container.getPassByReference();

            if (usePassByReference) {
                policies.add(new CopyObjectPolicy(PASS_BY_REFERENCE_ID));
            }

            if (threadPoolName != null) {
                ThreadPoolManager threadPoolManager = S1ASThreadPoolManager.getThreadPoolManager();
                try {
                    threadPoolNumericID = threadPoolManager.getThreadPoolNumericId(
                    threadPoolName);
                    policies.add(new RequestPartitioningPolicy(threadPoolNumericID));
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Not using threadpool-request-partitioning...", ex);
                }
            }

            if (logger.isLoggable(Level.FINE)) {
                SimpleJndiName jndiName = ejbDescriptor.getJndiName();
                logger.log(Level.FINE, "Using Thread-Pool: [{0} ==> {1}] for jndi name: {2}",
                    new Object[] {threadPoolName, threadPoolNumericID, jndiName});
                logger.log(Level.FINE, "Pass by reference: [{0}] for jndi name: {1}",
                    new Object[] {usePassByReference, usePassByReference});
            }

            // DisableClearTextIIOP policy which sets IIOP Profile port to 0
            // if EJB allows only SSL invocations
            if (ejbDescriptor.allMechanismsRequireSSL()) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.WARNING, ".createReferenceFactory: {0} {1}: adding ZeroPortPolicy",
                        new Object[] {poaId, repoid});
                }
                policies.add(ZeroPortPolicy.getPolicy());
            }


            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, ".createReferenceFactory: {0} {1}: policies: {2}",
                    new Object[] {poaId, repoid, policies});
            }

            ReferenceFactory rf = rfm.create(poaId, repoid, policies, this);
            return rf;
        } finally {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.WARNING, ".createReferenceFactory<-: {0} {1}", new Object[] {poaId, repoid});
            }
        }
    }


    @Override
    public java.rmi.Remote createRemoteReference(byte[] instanceKey) {
        return createRef(instanceKey, ejbObjectReferenceFactory, ejbObjectStubFactory, ejbObjectRepositoryId);
    }


    @Override
    public Remote createHomeReference(byte[] homeKey) {
        return createRef(homeKey, ejbHomeReferenceFactory, ejbHomeStubFactory, ejbHomeRepositoryId);
    }


    private void setClassLoader() {
        ClassLoader cl;
        SecurityManager sman = System.getSecurityManager();
        if (sman == null) {
            cl = this.getClass().getClassLoader();
        } else {
            cl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

                @Override
                public ClassLoader run() {
                    return this.getClass().getClassLoader();
                }
            });
        }

        Wrapper._setClassLoader(cl);
    }


    private Remote createRef(byte[] instanceKey, ReferenceFactory rf, PresentationManager.StubFactory stubFactory,
        String repoid) {
        try {
            PresentationManager.StubFactory stubFact = stubFactory;
            org.omg.CORBA.Object ref = _createRef(rf, instanceKey, repoid);

            // Set the ClassLoader to the ClassLoader for this class,
            // which is loaded by the OSGi bundle ClassLoader for the
            // orb-iiop bundle, which depends on (among others) the
            // glassfish-corba-codegen bundle, which contains the
            // CodegenProxyStub class needed inside the makeStub call.
            setClassLoader();

            org.omg.CORBA.Object stub = stubFact.makeStub();
            Delegate delegate = StubAdapter.getDelegate(ref);
            StubAdapter.setDelegate(stub, delegate);

            return (Remote) stub;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "iiop.createreference_exception", e.toString());
            throw new RuntimeException("Unable to create reference ", e);
        }
    }

    // NOTE: The repoid is only needed for logging.
    private org.omg.CORBA.Object _createRef(ReferenceFactory rf, byte[] instanceKey, String repoid) throws Exception {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "\t\tIn POARemoteReferenceFactory._createRef, repositoryId = {0}", repoid);
        }

        // Create the ejbKey using EJB's unique id + instanceKey
        byte[] ejbKey = createEJBKey(ejbDescriptor.getUniqueId(), instanceKey);

        org.omg.CORBA.Object obj = rf.createReference(ejbKey);

        return obj;
    }


    private byte[] createEJBKey(long ejbId, byte[] instanceKey) {
        byte[] ejbkey = new byte[INSTANCEKEY_OFFSET + instanceKey.length];

        Utility.longToBytes(ejbId, ejbkey, EJBID_OFFSET);
        Utility.intToBytes(instanceKey.length, ejbkey, INSTANCEKEYLEN_OFFSET);
        System.arraycopy(instanceKey, 0, ejbkey, INSTANCEKEY_OFFSET, instanceKey.length);
        return ejbkey;
    }


    /**
     * Disconnect an EJBObject or EJBHome from the ORB.
     */
    @Override
    public void destroyReference(Remote remoteRef, Remote remoteObj) {
        // Note: the POAs have the NON_RETAIN policy so they dont maintain
        // any state for objects. We only need to unexport the object from
        // the RMI/IIOP machinery.
        // The following call also does tie.deactivate() for the remoteObj's tie
        try {
            Util.unexportObject(remoteObj);
        } catch ( RuntimeException ex ) {
            // A bug in Util.unexportObject causes this exception
            // Ignore it.
        } catch ( java.lang.Exception nsoe ){
            // eat it and ignore it.
        }
    }

    /**
     * This is the implementation of ServantLocator.preinvoke()
     * It is called from the POA before every remote invocation.
     * Return a POA Servant (which is the RMI/IIOP Tie for EJBObject/EJBHome).
     * @param ejbKey
     * @param cookieHolder
     */
    @Override
    public Servant preinvoke(byte[] ejbKey, POA adapter, String operation, CookieHolder cookieHolder)
        throws org.omg.PortableServer.ForwardRequest {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "In preinvoke for operation:{0}", operation);
        }

        // get instance key
        int keyLen = Utility.bytesToInt(ejbKey, INSTANCEKEYLEN_OFFSET);
        byte[] instanceKey = new byte[keyLen];
        System.arraycopy(ejbKey, INSTANCEKEY_OFFSET, instanceKey, 0, keyLen);

        Servant servant = null;
        try {
            while ( servant == null ) {
                // get the EJBObject / EJBHome
                Remote targetObj = container.getTargetObject(instanceKey, (isRemoteHomeView ? null : remoteBusinessIntf));

                // This could be null in rare cases for sfsbs and entity
                // beans.  It would be preferable to push the retry logic
                // within the sfsb container and entity container
                // implementations of getTargetObject, but for now let's keep
                // the looping logic the same as it has always been.
                if( targetObj != null ) {
                    // get the Tie which is the POA Servant
                    //fix for bug 6484935
                    @SuppressWarnings("unchecked")
                    Tie tie = (Tie)AccessController.doPrivileged(
                        new PrivilegedAction() {
                        @Override
                            public Tie run()  {
                                return presentationMgr.getTie();
                        }
                    });

                    tie.setTarget(targetObj);
                    servant = (Servant) tie;
                }
            }
        } catch (NoSuchObjectLocalException e) {
            logger.log(Level.SEVERE, "Target object not found", e);
            throw new OBJECT_NOT_EXIST(GET_TIE_EXCEPTION_CODE, CompletionStatus.COMPLETED_NO);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE,"iiop.runtime_exception", e);
            throw e;
        }

        return servant;
    }

    @Override
    public void postinvoke(byte[] ejbKey, POA adapter, String operation, java.lang.Object cookie, Servant servant) {
        Remote target = null;
        if ( servant != null ) {
            target = ((Tie)servant).getTarget();
        }

        // Always release, since that restores previous context class loader.
        container.releaseTargetObject(target);
    }


    @Override
    public void destroy() {
        try {
            ejbHomeReferenceFactory.destroy() ;
            ejbObjectReferenceFactory.destroy() ;
            ejbHomeReferenceFactory = null ;
            ejbObjectReferenceFactory = null ;

            container = null;
            ejbDescriptor = null;

            orb = null;
            protocolMgr = null;

        } catch (Throwable th) {
            logger.log(Level.SEVERE, "Exception during " + "POARemoteRefFactory::destroy()", th);
        }
    }

    @Override
    public boolean hasSameContainerID(org.omg.CORBA.Object obj) throws Exception {
        boolean result = false;
        try {
            IOR ior = (orb).getIOR(obj, false);
            java.util.Iterator iter = ior.iterator();

            byte[] oid = null;
            if (iter.hasNext()) {
                TaggedProfile profile = (TaggedProfile) iter.next();
                ObjectKey objKey = profile.getObjectKey();
                oid = objKey.getId().getId();
            }

            if (oid != null && oid.length > INSTANCEKEY_OFFSET) {
                long cid = Utility.bytesToLong(oid, EJBID_OFFSET);
                // To be really sure that is indeed a ref generated
                //  by our container we do the following checks
                int keyLen = Utility.bytesToInt(oid, INSTANCEKEYLEN_OFFSET);
                if (oid.length == keyLen + INSTANCEKEY_OFFSET) {
                    result = (cid == ejbDescriptor.getUniqueId() );
                }
                if (logger.isLoggable(Level.FINE)) {
                    StringBuilder sbuf = new StringBuilder();
                    sbuf.append("hasSameContainerID() result: ").append(result)
                        .append("; because ==> oid.length: ").append(oid.length)
                        .append("; instance-key-length: ").append(keyLen)
                        .append("; expected oid.length: ")
                        .append(keyLen).append("+").append(INSTANCEKEY_OFFSET)
                        .append("; myContainrID: ")
                        .append(ejbDescriptor.getUniqueId())
                        .append("; obj.containerID: ")
                        .append(cid);
                    logger.log(Level.FINE, sbuf.toString());
                }
            } else {
                if (logger.isLoggable(Level.FINE)) {
                    if (oid == null) {
                        logger.log(Level.FINE, "hasSameContainerID() failed because oid=null");
                    } else {
                        logger.log(Level.FINE,
                            "hasSameContainerID() failed because oid.length={0}; but INSTANCE_KEY_OFFSET= {1}",
                            new Object[] {oid.length, INSTANCEKEY_OFFSET});
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.FINE, "Exception while checking for same containerID", ex);
            throw ex;
        }

        return result;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException();
    }

     private void readObject(ObjectInputStream in) throws IOException {
         throw new NotSerializableException();
    }
}
