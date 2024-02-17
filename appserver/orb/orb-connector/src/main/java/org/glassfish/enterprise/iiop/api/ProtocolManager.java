/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.enterprise.iiop.api;

import com.sun.enterprise.deployment.EjbDescriptor;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.glassfish.enterprise.iiop.spi.EjbContainerFacade;
import org.jvnet.hk2.annotations.Contract;
import org.omg.CORBA.ORB;


/**
 * The ProtocolManager interface specifies the functionality of the remote communication layer,
 * which provides the support for distribution described in Chapter 13 of the EJB spec.
 * Possible implementations of the ProtocolManager include RMI/IIOP, RMI/JRMP, RMI/DCOM, RMI/HTTP,
 * etc.
 *
 * @author Vivek Nagar
 */
@Contract
public interface ProtocolManager {

    void initialize(ORB o);

    void initializePOAs() throws Exception;

    void initializeNaming() throws Exception;

    void initializeRemoteNaming(Remote remoteNamingProvider) throws Exception;

    /**
     * Return a factory that can be used to create/destroy remote
     * references for a particular EJB type.
     */
    RemoteReferenceFactory getRemoteReferenceFactory(EjbContainerFacade container, boolean remoteHomeView, String id);

    /**
     * Return true if the two object references refer to the same
     * remote object.
     */
    boolean isIdentical(Remote obj1, Remote obj2);

    /**
     * Check that all Remote interfaces implemented by target object
     * conform to the rules for valid RMI-IIOP interfaces.  Throws
     * runtime exception if validation fails.
     */
    void validateTargetObjectInterfaces(Remote targetObj);

    /**
     * Map the RMI exception to a protocol-specific (e.g. CORBA) exception
     */
    Throwable mapException(Throwable exception);

    /**
     * True if object is a corba stub
     */
    boolean isStub(Object obj);

    boolean isLocal(Object obj);

    byte[] getObjectID(org.omg.CORBA.Object obj);

    /**
     * Connect the RMI object to the protocol.
     */
    void connectObject(Remote remoteObj) throws RemoteException;

    EjbDescriptor getEjbDescriptor(byte[] ejbKey);
}
