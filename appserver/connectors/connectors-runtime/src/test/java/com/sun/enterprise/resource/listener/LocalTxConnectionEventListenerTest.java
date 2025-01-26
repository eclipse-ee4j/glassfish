/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.resource.listener;

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnection;

import java.util.Map;

import org.glassfish.api.naming.SimpleJndiName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LocalTxConnectionEventListenerTest {

    @BeforeEach
    public void setup() throws PoolingException, ResourceException {
        // Make sure ConnectorRuntime singleton is initialized
        new ConnectorRuntime();
    }

    @Test
    public void associateHandleTest() throws ResourceException {
        ResourceHandle mainResourceHandle = createResourceHandle(1);
        LocalTxConnectionEventListener localTxConnectionEventListener = new LocalTxConnectionEventListener(mainResourceHandle);

        // Associate a new handle association
        ResourceHandle associatedResourceHandle = createResourceHandle(2);
        final Object userHandle = new Object();
        localTxConnectionEventListener.associateHandle(userHandle, associatedResourceHandle);

        // Remove the new handle association
        ResourceHandle removeAssociation = localTxConnectionEventListener.removeAssociation(userHandle);
        assertEquals(associatedResourceHandle, removeAssociation);

        // Check the remove did work in the previous call
        removeAssociation = localTxConnectionEventListener.removeAssociation(userHandle);
        assertNull(removeAssociation);
    }

    @Test
    public void getAssociatedHandlesAndClearMapTest() throws ResourceException {
        ResourceHandle mainResourceHandle = createResourceHandle(1);
        LocalTxConnectionEventListener localTxConnectionEventListener = new LocalTxConnectionEventListener(mainResourceHandle);

        localTxConnectionEventListener.associateHandle(new Object(), createResourceHandle(2));
        localTxConnectionEventListener.associateHandle(new Object(), createResourceHandle(3));

        // Check the clone works
        Map<Object, ResourceHandle> associatedHandlesAndClearMap = localTxConnectionEventListener.getAssociatedHandlesAndClearMap();
        assertEquals(2, associatedHandlesAndClearMap.size());

        // Check the clear did work in the previous call
        associatedHandlesAndClearMap = localTxConnectionEventListener.getAssociatedHandlesAndClearMap();
        assertEquals(0, associatedHandlesAndClearMap.size());
    }

    private ResourceHandle createResourceHandle(int i) throws ResourceException {
        ManagedConnection managedConnection = createNiceMock(ManagedConnection.class);
        replay();
        return new ResourceHandle(managedConnection, new ResourceSpec(new SimpleJndiName("testResource" + i), 0), null);
    }
}
