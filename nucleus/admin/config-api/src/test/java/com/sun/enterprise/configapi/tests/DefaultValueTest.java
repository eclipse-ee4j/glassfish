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

import jakarta.inject.Inject;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.grizzly.config.dom.Http;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Protocols;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Dom;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test attribute and raw attribute access *
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class DefaultValueTest {

    @Inject
    private ServiceLocator locator;
    private NetworkListener listener;

    @BeforeEach
    public void setup() {
        NetworkListeners httpService = locator.getService(NetworkListeners.class);
        listener = httpService.getNetworkListener().get(0);
    }

    @Test
    public void rawAttributeTest() throws NoSuchMethodException {
        String address = listener.getAddress();
        Dom raw = Dom.unwrap(listener);
        Attribute attr = raw.getProxyType().getMethod("getAddress").getAnnotation(Attribute.class);
        assertEquals(address, attr.defaultValue());
        assertEquals(address, raw.attribute("address"));
        assertEquals(address, raw.rawAttribute("address"));
    }

    @Test
    public void defaultValueTest() {
        Protocols protocols = locator.getService(Protocols.class);
        for (Protocol protocol : protocols.getProtocol()) {
            Http http = protocol.getHttp();
            assertEquals(Http.COMPRESSABLE_MIME_TYPE, http.getCompressableMimeType());
        }
    }
}
