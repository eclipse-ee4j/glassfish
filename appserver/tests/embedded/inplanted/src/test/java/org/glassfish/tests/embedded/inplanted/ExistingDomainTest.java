/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.inplanted;

import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.embedded.*;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.admin.*;
import org.glassfish.api.container.Sniffer;
import org.glassfish.tests.embedded.utils.EmbeddedServerUtils;
import org.junit.BeforeClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.AfterClass;

import java.io.File;
import java.util.Enumeration;
import java.util.Collection;
import java.lang.reflect.Method;

/**
 * Test embedded API with an existing domain.xml
 *
 * @author Jerome Dochez
 */
public class ExistingDomainTest {
    static Server server;

    @BeforeClass
    public static void setupServer() throws Exception {
        File serverLocation = EmbeddedServerUtils.getServerLocation();
        EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
        efsb.installRoot(serverLocation).instanceRoot(EmbeddedServerUtils.getDomainLocation(serverLocation));
        server = EmbeddedServerUtils.createServer(efsb.build());
    }

    @Test
    public void Test() throws Exception {

        ServiceLocator habitat = server.getHabitat();
        System.out.println("Process type is " + habitat.<ProcessEnvironment>getService(ProcessEnvironment.class).getProcessType());
        Collection<ServiceHandle<?>> listeners = habitat.getAllServiceHandles(org.glassfish.grizzly.config.dom.NetworkListener.class);
        Assert.assertTrue(listeners.size()>1);
        for (ServiceHandle<?> s : listeners) {
            Object networkListener = s.getService();
            Method m = networkListener.getClass().getMethod("getPort");
            Assert.assertNotNull("Object returned does not implement getPort, is it a networkListener ?", m);
            String port = (String) m.invoke(networkListener);
            System.out.println("Network Listener " + port);
            Assert.assertNotNull("Got a null networkListener port", port);
        }
    }

    @AfterClass
    public static void shutdownServer() throws Exception {
        EmbeddedServerUtils.shutdownServer(server);
    }
    
}
