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

package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.Server;
import org.junit.Test;
import org.junit.Before;
import org.jvnet.hk2.config.*;
import org.glassfish.tests.utils.Utils;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Level;

/**
 * User: Jerome Dochez
 * Date: Jun 25, 2008
 * Time: 8:03:41 AM
 */
public class AttributeRemovalTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }

    @Test
    public void removeAttributeTest() throws TransactionFailure {
        HttpService httpService = Utils.instance.getHabitat(this).getService(HttpService.class);
        VirtualServer vs = httpService.getVirtualServerByName("server");
        ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {

            public Object run(VirtualServer param) throws PropertyVetoException, TransactionFailure {
                param.setDefaultWebModule("/context/bar");
                return null;
            }
        }, vs);

        // ensure it's here
        org.junit.Assert.assertNotNull(vs.getDefaultWebModule());

        ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {

            public Object run(VirtualServer param) throws PropertyVetoException, TransactionFailure {
                param.setDefaultWebModule(null);
                return null;
            }
        }, vs);

        // ensure it's removed
        org.junit.Assert.assertNull(vs.getDefaultWebModule());
    }

    @Test(expected=PropertyVetoException.class)
    public void readOnlyRemovalTest() throws TransactionFailure , PropertyVetoException{
        Server server = getHabitat().getService(Server.class);
        logger.fine("config-ref is " + server.getConfigRef());
        try {
            server.setConfigRef(null);
        } catch (PropertyVetoException e) {
            if (logger.isLoggable(Level.FINE))
                e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void deprecatedWrite() throws TransactionFailure {
        final Server server = getHabitat().getService(Server.class);
        final String value = server.getNodeRef();
        logger.fine("node-ref is " + server.getNodeRef());
        ConfigSupport.apply(new SingleConfigCode<Server>() {
            @Override
            public Object run(Server s) throws PropertyVetoException, TransactionFailure {
                s.setNodeAgentRef(value);
                return null;
            }
        }, server);
        logger.fine("node-agent-ref is " + server.getNodeAgentRef());
        // restore
        ConfigSupport.apply(new SingleConfigCode<Server>() {
            @Override
            public Object run(Server s) throws PropertyVetoException, TransactionFailure {
                s.setNodeAgentRef(null);
                return null;
            }
        }, server);
        logger.fine("after, node-agent-ref is " + server.getNodeAgentRef());

    }


}
