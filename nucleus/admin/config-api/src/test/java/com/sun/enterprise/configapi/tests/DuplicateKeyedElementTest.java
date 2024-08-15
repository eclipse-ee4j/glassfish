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
import com.sun.enterprise.config.serverbeans.VirtualServer;

import jakarta.inject.Inject;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for invalid duplicate keyed entries
 *
 * @author Jerome Dochez
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class DuplicateKeyedElementTest {

    @Inject
    private ServiceLocator locator;

    @Test
    public void duplicateKeyTest() throws TransactionFailure {
        final VirtualServer target = findVirtualServerWithProperties();
        assertNotNull(target);
        final Property prop = target.getProperty().get(0);
        final SingleConfigCode<VirtualServer> configCode = virtualServer -> {
            // first one is fine...
            Property dupProp = virtualServer.createChild(Property.class);
            dupProp.setName(prop.getName());
            dupProp.setValue(prop.getValue().toUpperCase());
            // this should fail...
            throw assertThrows(IllegalArgumentException.class, () -> virtualServer.getProperty().add(dupProp));
        };
        assertThrows(TransactionFailure.class, () -> ConfigSupport.apply(configCode, target));
    }

    @Test
    public void identicalKeyTest() throws TransactionFailure {
        final VirtualServer target = findVirtualServerWithProperties();
        assertNotNull(target);
        final SingleConfigCode<VirtualServer> configCode = virtualServer -> {
            // first one is fine...
            Property firstProp = virtualServer.createChild(Property.class);
            firstProp.setName("foo");
            firstProp.setValue("bar");
            virtualServer.getProperty().add(firstProp);
            // this should fail...
            Property secondProp = virtualServer.createChild(Property.class);
            secondProp.setName("foo");
            secondProp.setValue("bar");
            throw assertThrows(IllegalArgumentException.class, () -> virtualServer.getProperty().add(secondProp));
        };
        assertThrows(TransactionFailure.class, () -> ConfigSupport.apply(configCode, target));
    }

    private VirtualServer findVirtualServerWithProperties() {
        HttpService httpService = locator.getService(HttpService.class);
        assertNotNull(httpService);
        for (VirtualServer vs : httpService.getVirtualServer()) {
            if (!vs.getProperty().isEmpty()) {
                return vs;
            }
        }
        return null;
    }
}
