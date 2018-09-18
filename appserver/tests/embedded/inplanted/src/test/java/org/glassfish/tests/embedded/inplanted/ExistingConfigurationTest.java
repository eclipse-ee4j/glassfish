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
import org.glassfish.tests.embedded.utils.EmbeddedServerUtils;
import org.junit.BeforeClass;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.lang.reflect.Method;

/**
 * @author Jerome Dochez
 */
public class ExistingConfigurationTest {

    @Test
    public void setupServer() throws Exception {

        Server server=null;
        Port port = null;

        File f = EmbeddedServerUtils.getServerLocation();
        try {
            EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
            efsb.installRoot(f);
            // find the domain root.
            f = EmbeddedServerUtils.getDomainLocation(f);
            f = new File(f, "config");
            f = new File(f, "domain.xml");
            Assert.assertTrue(f.exists());
            efsb.configurationFile(f, true);
            server = EmbeddedServerUtils.createServer(efsb.build());

            ServiceLocator habitat = server.getHabitat();
            Collection<ServiceHandle<?>> vss = habitat.getAllServiceHandles(com.sun.enterprise.config.serverbeans.VirtualServer.class);
            Assert.assertTrue(vss.size()>0);
            for (ServiceHandle<?> vs : vss ) {
                Object virtualServer = vs.getService();
                Method m = virtualServer.getClass().getMethod("getId");
                Assert.assertNotNull("Object returned does not implement getId, is it a virtual server ?", m);
                String id = (String) m.invoke(virtualServer);
                System.out.println("Virtual Server " + id);
                Assert.assertNotNull("Got a null virtual server ID", id);
            }
            Collection<ServiceHandle<?>> nls = habitat.getAllServiceHandles(org.glassfish.grizzly.config.dom.NetworkListener.class);
            Assert.assertTrue(nls.size()>1);
            for (ServiceHandle<?> nl : nls) {
                Object networkListener = nl.getService();
                Method m = networkListener.getClass().getMethod("getPort");
                Assert.assertNotNull("Object returned does not implement getPort, is it a networkListener ?", m);
                String p = (String) m.invoke(networkListener);
                System.out.println("Network Listener " + p);
                Assert.assertNotNull("Got a null networkListener port", p);
            }
            server.start();
            port = server.createPort(8758);
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (port!=null) {
                port.close();
            }
            EmbeddedServerUtils.shutdownServer(server);
        }
    }
}
