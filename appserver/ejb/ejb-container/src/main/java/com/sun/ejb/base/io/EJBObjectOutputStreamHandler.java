/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.ejb.base.io;

import com.sun.ejb.EJBUtils;
import com.sun.ejb.codegen.RemoteGenerator;
import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.ejb.containers.RemoteBusinessWrapperBase;
import com.sun.enterprise.container.common.spi.util.GlassFishOutputStreamHandler;
import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;
import com.sun.enterprise.container.common.spi.util.SerializableObjectFactory;
import com.sun.enterprise.util.Utility;
import com.sun.logging.LogDomains;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.enterprise.iiop.api.GlassFishORBLocator;
import org.glassfish.enterprise.iiop.api.ProtocolManager;
import org.glassfish.internal.api.Globals;

import static com.sun.logging.LogDomains.EJB_LOGGER;

/**
 * A class that is used to passivate SFSB conversational state
 *
 * @author Mahesh Kannan
 */
public class EJBObjectOutputStreamHandler implements GlassFishOutputStreamHandler {

    static JavaEEIOUtils _javaEEIOUtils;

    private static final Logger LOG = LogDomains.getLogger(EJBObjectOutputStreamHandler.class, EJB_LOGGER);

    static final int EJBID_OFFSET = 0;
    static final int INSTANCEKEYLEN_OFFSET = 8;
    static final int INSTANCEKEY_OFFSET = 12;

    private static final byte HOME_KEY = (byte) 0xff;

    // FIXME: @JavaEEIOUtils is a Service, but this is really a bad thing!
    public static final void setJavaEEIOUtils(JavaEEIOUtils javaEEIOUtils) {
        _javaEEIOUtils = javaEEIOUtils;
    }

    /**
     * This code is needed to serialize non-Serializable objects that
     * can be part of a bean's state. See EJB2.0 section 7.4.1.
     */
    @Override
    public Object replaceObject(Object obj) throws IOException {
        Object result = obj;

        // Until we've identified a remote object, we can't assume the orb is
        // available in the container. If the orb is not present, this will be null.
        ProtocolManager protocolMgr = getProtocolManager();

        if (obj instanceof RemoteBusinessWrapperBase) {
            result = getRemoteBusinessObjectFactory((RemoteBusinessWrapperBase) obj);
        } else if ((protocolMgr != null) && protocolMgr.isStub(obj) && protocolMgr.isLocal(obj)) {
            org.omg.CORBA.Object target = (org.omg.CORBA.Object) obj;
            // If we're here, it's always for the 2.x RemoteHome view.
            // There is no remote business wrapper class.
            result = getSerializableEJBReference(target, protocolMgr, null);
        }
        return result;
    }


    private ProtocolManager getProtocolManager() {
        GlassFishORBLocator orbLocator = Globals.getDefaultHabitat().getService(GlassFishORBLocator.class);
        return orbLocator.getProtocolManager();
    }


    private Serializable getRemoteBusinessObjectFactory(RemoteBusinessWrapperBase remoteBusinessWrapper)
        throws IOException {
        // Create a serializable object with the remote delegate and
        // the name of the client wrapper class.
        org.omg.CORBA.Object target = (org.omg.CORBA.Object) remoteBusinessWrapper.getStub();
        return getSerializableEJBReference(target, getProtocolManager(), remoteBusinessWrapper.getBusinessInterfaceName());
    }


    private Serializable getSerializableEJBReference(
        org.omg.CORBA.Object obj, ProtocolManager protocolMgr, String remoteBusinessInterface) throws IOException {
        try {
            byte[] oid = protocolMgr.getObjectID(obj);
            if (oid != null && oid.length > INSTANCEKEY_OFFSET) {
                long containerId = Utility.bytesToLong(oid, EJBID_OFFSET);
                // To be really sure that is indeed a ref generated
                //  by our container we do the following checks
                int keyLength = Utility.bytesToInt(oid, INSTANCEKEYLEN_OFFSET);
                if (oid.length == keyLength + INSTANCEKEY_OFFSET) {
                    boolean isHomeReference = keyLength == 1 && oid[INSTANCEKEY_OFFSET] == HOME_KEY;
                    if (isHomeReference) {
                        return new SerializableS1ASEJBHomeReference(containerId);
                    }
                    return new SerializableS1ASEJBObjectReference(containerId, oid, keyLength, remoteBusinessInterface);
                }
            }
            return (Serializable) obj;
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Exception while getting serializable object", ex);
            throw new IOException("Exception during extraction of instance key", ex);
        }
    }
}


final class SerializableJNDIContext implements SerializableObjectFactory {

    private SimpleJndiName name;

    SerializableJNDIContext(Context ctx) throws IOException {
        try {
            // Serialize state for a jndi context.  The spec only requires
            // support for serializing contexts pointing to java:comp/env
            // or one of its subcontexts.  We also support serializing the
            // references to the the default no-arg InitialContext, as well
            // as references to the the contexts java: and java:comp. All
            // other contexts will either not serialize correctly or will
            // throw an exception during deserialization.
            String nsName = ctx.getNameInNamespace();
            this.name = nsName.isEmpty() ? null : new SimpleJndiName(nsName);
        } catch (NamingException ex) {
            throw new IOException(ex);
        }
    }


    @Override
    public Object createObject() throws IOException {
        try {
            if (name == null) {
                return new InitialContext();
            }
            return Globals.getDefaultHabitat().<GlassfishNamingManager> getService(GlassfishNamingManager.class)
                .restoreJavaCompEnvContext(name);
        } catch (NamingException e) {
            throw new IOException(e);
        }
    }
}


abstract class AbstractSerializableS1ASEJBReference implements SerializableObjectFactory {

    protected static final Logger LOG = LogDomains.getLogger(AbstractSerializableS1ASEJBReference.class, EJB_LOGGER);
    protected long containerId;


    AbstractSerializableS1ASEJBReference(long containerId) {
        this.containerId = containerId;
        BaseContainer container = EjbContainerUtilImpl.getInstance().getContainer(containerId);
        // container can be null if the app has been undeployed after this was serialized
        if (container == null) {
            LOG.log(Level.WARNING, "ejb.base.io.null_container", containerId);
        }
    }


    protected static java.rmi.Remote doRemoteRefClassLoaderConversion(final java.rmi.Remote reference) throws IOException {
        ClassLoader contextClassLoader =  Thread.currentThread().getContextClassLoader();
        if (contextClassLoader == reference.getClass().getClassLoader()) {
            return reference;
        }
        try {
            byte[] serializedRef = EJBObjectOutputStreamHandler._javaEEIOUtils.serializeObject(reference, false);
            Remote returnReference = (java.rmi.Remote) EJBObjectOutputStreamHandler._javaEEIOUtils
                .deserializeObject(serializedRef, false, contextClassLoader);
            GlassFishORBLocator orbHelper = EjbContainerUtilImpl.getInstance().getOrbLocator();
            ProtocolManager protocolMgr = orbHelper.getProtocolManager();
            protocolMgr.connectObject(returnReference);
            return returnReference;
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}


final class SerializableS1ASEJBHomeReference extends AbstractSerializableS1ASEJBReference {

    private static final long serialVersionUID = 1L;

    SerializableS1ASEJBHomeReference(long containerId) {
        super(containerId);
    }

    @Override
    public Object createObject() throws IOException {
        BaseContainer container = EjbContainerUtilImpl.getInstance().getContainer(containerId);
        //container can be null if the app has been undeployed
        //  after this was serialized
        if (container == null) {
            LOG.log(Level.WARNING, "ejb.base.io.null_container", containerId);
            return null;
        }
        // Note that we can assume it's a RemoteHome stub because an
        // application never sees a reference to the internal
        // Home for the Remote Business view.
        return AbstractSerializableS1ASEJBReference.doRemoteRefClassLoaderConversion(container.getEJBHomeStub());
    }
}


final class SerializableS1ASEJBObjectReference extends AbstractSerializableS1ASEJBReference {
    private static final long serialVersionUID = 1L;
    private final byte[] instanceKey;
    private Object sfsbKey;
    private long sfsbClientVersion;
    private boolean haEnabled;

    // If 3.0 Remote business view, the name of the remote business
    // interface to which this stub corresponds.
    private final String remoteBusinessInterface;

    SerializableS1ASEJBObjectReference(long containerId, byte[] objKey,
            int keySize, String remoteBusinessInterfaceName) {
        super(containerId);
        BaseContainer container = EjbContainerUtilImpl.getInstance().getContainer(containerId);
        if (container != null) {
            this.haEnabled = container.isHAEnabled();
        }
        remoteBusinessInterface = remoteBusinessInterfaceName;
        instanceKey = new byte[keySize];
        System.arraycopy(objKey, EJBObjectOutputStreamHandler.INSTANCEKEY_OFFSET, instanceKey, 0, keySize);
    }

    void setSFSBClientVersion(Object key, long val) {
        this.sfsbKey = key;
        this.sfsbClientVersion = val;
    }

    boolean isHAEnabled() {
        return haEnabled;
    }

    @Override
    public Object createObject() throws IOException {
        BaseContainer container = EjbContainerUtilImpl.getInstance().getContainer(containerId);
        //container can be null if the app has been undeployed after this was serialized
        if (container == null) {
            LOG.log(Level.WARNING, "ejb.base.io.null_container", containerId);
            return null;
        }
        try {
            if (remoteBusinessInterface == null) {
                java.rmi.Remote reference = container.createRemoteReferenceWithId(instanceKey, null);
                return AbstractSerializableS1ASEJBReference.doRemoteRefClassLoaderConversion(reference);
            }
            String generatedRemoteIntfName = RemoteGenerator.getGeneratedRemoteIntfName(remoteBusinessInterface);
            java.rmi.Remote remoteRef = container.createRemoteReferenceWithId(instanceKey, generatedRemoteIntfName);
            java.rmi.Remote newRemoteRef = AbstractSerializableS1ASEJBReference
                .doRemoteRefClassLoaderConversion(remoteRef);

            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            return EJBUtils.createRemoteBusinessObject(contextClassLoader, remoteBusinessInterface, newRemoteRef);
        } catch (Exception e) {
            IOException ioex = new IOException("remote ref create error");
            ioex.initCause(e);
            throw ioex;
        }
    }
}

