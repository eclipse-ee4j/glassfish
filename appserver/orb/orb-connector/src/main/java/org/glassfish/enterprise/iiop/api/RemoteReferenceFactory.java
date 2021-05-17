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

package org.glassfish.enterprise.iiop.api;


import java.rmi.Remote;

/**
 * The RemoteReferenceFactory interface provides methods to
 * create and destroy remote EJB references. Instances of the
 * RemoteReferenceFactory are obtained from the ProtocolManager.
 *
 */
public interface RemoteReferenceFactory {

    /**
     * Create a remote reference for an EJBObject which can
     * be used for performing remote invocations.
     * The key specifies the unique
     * "object-id" of the EJBObject. This operation should not
     * create any "tie" for the particular remote object instance.
     * This operation should not cause the ProtocolManager to maintain
     * any instance-specific state about the EJB instance.
     *
     * @param instanceKey a unique identifier for the EJB instance
     *          which is unique across all EJB refs created using this
     *          RemoteReferenceFactory instance.
     * @return the protocol-specific stub of the proper derived type.
     *       It should not be necessary to narrow this stub again.
     */
    Remote createRemoteReference(byte[] instanceKey);


    /**
     * Create a remote reference for an EJBHome which can
     * be used for performing remote invocations.
     * The key specifies the unique
     * "object-id" of the EJBHome. This operation should not
     * create any "tie" for the particular remote object instance.
     * This operation should not cause the ProtocolManager to maintain
     * any instance-specific state about the EJB instance.
     *
     * @param homeKey a unique identifier for the EJB instance
     *          which is unique across all EJB refs created using this
     *          RemoteReferenceFactory instance.
     * @return the protocol-specific stub of the proper derived type.
     *       It should not be necessary to narrow this stub again.
     */
    Remote createHomeReference(byte[] homeKey);

    /**
     * Destroy an EJBObject or EJBHome remote ref
     * so that it can no longer be used for remote invocations.
     * This operation should destroy any state such as "tie" objects
     * maintained by the ProtocolManager for the EJBObject or EJBHome.
     *
     * @param remoteRef the remote reference for the EJBObject/EJBHome
     * @param remoteObj the servant corresponding to the remote reference.
     */
    void destroyReference(Remote remoteRef, Remote remoteObj);

    /**
     * Destroy the factory itself. Called during shutdown / undeploy.
     * The factory is expected to release all resources in this method.
     */
    public void destroy();


    public boolean hasSameContainerID(org.omg.CORBA.Object ref)
    throws Exception;

    public void setRepositoryIds(Class homeIntf, Class remoteIntf);

    public void cleanupClass(Class clazz);

    public int getCSIv2PolicyType();

}

