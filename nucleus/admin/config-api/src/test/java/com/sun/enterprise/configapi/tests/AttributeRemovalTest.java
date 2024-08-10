/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.VirtualServer;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.logging.Logger;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * User: Jerome Dochez
 * Date: Jun 25, 2008
 * Time: 8:03:41 AM
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class AttributeRemovalTest {
    @Inject
    private ServiceLocator locator;
    @Inject
    private Logger logger;

    @Test
    public void removeAttributeTest() throws TransactionFailure {
        HttpService httpService = locator.getService(HttpService.class);
        VirtualServer vs = httpService.getVirtualServerByName("server");
        SingleConfigCode<VirtualServer> configCodeWebModule = virtualServer -> {
            virtualServer.setDefaultWebModule("/context/bar");
            return null;
        };
        ConfigSupport.apply(configCodeWebModule, vs);
        assertNotNull(vs.getDefaultWebModule());

        SingleConfigCode<VirtualServer> configCodeNullWebModule = virtualServer -> {
            virtualServer.setDefaultWebModule(null);
            return null;
        };
        ConfigSupport.apply(configCodeNullWebModule, vs);
        assertNull(vs.getDefaultWebModule());
    }

    @Test
    public void readOnlyRemovalTest(){
        Server server = locator.getService(Server.class);
        logger.fine("config-ref is " + server.getConfigRef());
        assertThrows(PropertyVetoException.class, () -> server.setConfigRef(null));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void deprecatedWrite() throws TransactionFailure {
        final Server server = locator.getService(Server.class);
        final String value = server.getNodeRef();
        final SingleConfigCode<Server> configCode = virtualServer -> {
            virtualServer.setNodeAgentRef(null);
            return null;
        };
        ConfigSupport.apply(configCode, server);
        assertNull(server.getNodeRef());

        SingleConfigCode<Server> restoreConfig = virtualServer -> {
            virtualServer.setNodeAgentRef(value);
            return null;
        };
        ConfigSupport.apply(restoreConfig, server);
        assertEquals(value, server.getNodeAgentRef());
    }
}
