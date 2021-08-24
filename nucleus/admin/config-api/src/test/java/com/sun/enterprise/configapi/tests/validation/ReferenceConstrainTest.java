/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.configapi.tests.validation;

import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.configapi.tests.ConfigApiTest;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.TransactionFailure;

import jakarta.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author mmares
 */
public class ReferenceConstrainTest extends ConfigApiTest {

    private ServiceLocator locator;

    @Override
    public String getFileName() {
        return "DomainTest";
    }

    @Override
    public ServiceLocator getBaseServiceLocator() {
        return locator;
    }

    private ConstraintViolationException findConstrViolation(Throwable thr) {
        if (thr == null) {
            return null;
        }
        if (thr instanceof ConstraintViolationException) {
            return (ConstraintViolationException) thr;
        }
        return findConstrViolation(thr.getCause());
    }

    @BeforeEach
    public void createNewHabitat() {
        this.locator = Utils.instance.getHabitat(this);
    }

    @Test
    public void serverConfigRefValid() throws TransactionFailure {
        Server server = locator.getService(Server.class, "server");
        assertNotNull(server);
        ConfigBean serverConfig = (ConfigBean) Dom.unwrap(server);
        Map<ConfigBean, Map<String, String>> changes = new HashMap<>();
        Map<String, String> configChanges = new HashMap<>();
        configChanges.put("config-ref", "server-config");
        changes.put(serverConfig, configChanges);
        ConfigSupport cs = getHabitat().getService(ConfigSupport.class);
        cs.apply(changes);
    }

    @Test
    public void serverConfigRefInvalid() throws TransactionFailure {
        Server server = locator.getService(Server.class, "server");
        assertNotNull(server);
        ConfigBean serverConfig = (ConfigBean) Dom.unwrap(server);
        Map<ConfigBean, Map<String, String>> changes = new HashMap<>();
        Map<String, String> configChanges = new HashMap<>();
        configChanges.put("config-ref", "server-config-nonexist");
        changes.put(serverConfig, configChanges);
        try {
            ConfigSupport cs = getHabitat().getService(ConfigSupport.class);
            cs.apply(changes);
            fail("Can not reach this point");
        } catch (TransactionFailure tf) {
            ConstraintViolationException cv = findConstrViolation(tf);
            assertNotNull(cv);
        }
    }

    @Test
    public void jmxConnectorAuthRealmRefValid() throws TransactionFailure {
        JmxConnector jmxConnector = locator.getService(JmxConnector.class, "system");
        assertNotNull(jmxConnector);
        ConfigBean serverConfig = (ConfigBean) Dom.unwrap(jmxConnector);
        Map<ConfigBean, Map<String, String>> changes = new HashMap<>();
        Map<String, String> configChanges = new HashMap<>();
        configChanges.put("auth-realm-name", "file");
        changes.put(serverConfig, configChanges);
        ConfigSupport cs = getHabitat().getService(ConfigSupport.class);
        cs.apply(changes);
    }

    @Test
    public void jmxConnectorAuthRealmRefInvalid() throws TransactionFailure {
        JmxConnector jmxConnector = locator.getService(JmxConnector.class, "system");
        assertNotNull(jmxConnector);
        ConfigBean serverConfig = (ConfigBean) Dom.unwrap(jmxConnector);
        Map<ConfigBean, Map<String, String>> changes = new HashMap<>();
        Map<String, String> configChanges = new HashMap<>();
        configChanges.put("auth-realm-name", "realm-not-exist");
        changes.put(serverConfig, configChanges);
        try {
            ConfigSupport cs = getHabitat().getService(ConfigSupport.class);
            cs.apply(changes);
            fail("Can not reach this point");
        } catch (TransactionFailure tf) {
            ConstraintViolationException cv = findConstrViolation(tf);
            assertNotNull(cv);
        }
    }

}
