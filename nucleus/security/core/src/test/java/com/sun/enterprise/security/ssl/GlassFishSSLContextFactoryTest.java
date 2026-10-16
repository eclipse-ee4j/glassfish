/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.ssl;

import java.io.File;
import java.io.IOException;

import javax.net.ssl.SSLContext;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.main.jdke.security.KeyTool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.glassfish.grizzly.config.ssl.SSLContextFactory.ATTR_KEYSTORE_CONFIGURED;
import static org.glassfish.grizzly.config.ssl.SSLContextFactory.ATTR_TRUSTSTORE_CONFIGURED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies that the {@code key-store} and {@code trust-store} attributes of the {@code ssl} element
 * of the domain.xml are honored instead of being silently replaced by the server stores.
 *
 * @see <a href="https://github.com/eclipse-ee4j/glassfish/issues/18175">Issue 18175</a>
 */
public class GlassFishSSLContextFactoryTest {

    private static final String LISTENER_ALIAS = "listener-cert";
    private static final String LISTENER_PASSWORD = "listenerpassword";

    /** The locator is intentionally empty - the {@link SSLUtils} service must not be needed. */
    private static final ServiceLocator EMPTY_LOCATOR = ServiceLocatorFactory.getInstance()
        .create(GlassFishSSLContextFactoryTest.class.getName());

    @TempDir
    private static File tempDir;

    private static File listenerKeyStore;
    private static File listenerTrustStore;

    @BeforeAll
    static void createListenerStores() throws Exception {
        listenerKeyStore = new File(tempDir, "listener-keystore.p12");
        KeyTool keyTool = new KeyTool(listenerKeyStore, LISTENER_PASSWORD.toCharArray());
        keyTool.generateKeyPair(LISTENER_ALIAS, "CN=listener", "RSA", 1);
        listenerTrustStore = new File(tempDir, "listener-truststore.p12");
        keyTool.copyCertificate(LISTENER_ALIAS, listenerTrustStore);
    }


    /**
     * The listener configured its own stores, therefore they must be used even if the server stores
     * managed by the {@link SSLUtils} singleton are not available at all.
     */
    @Test
    public void listenerOwnStores() throws Exception {
        GlassFishSSLContextFactory factory = createFactory(LISTENER_ALIAS);
        SSLContext context = factory.create();
        assertNotNull(context, "SSLContext created from the stores of the listener");
    }


    /**
     * Proves that the keystore of the listener is really the one which is opened - the alias comes
     * from the server keystore and does not exist in the keystore of the listener.
     */
    @Test
    public void listenerOwnKeyStoreWithoutRequestedAlias() throws Exception {
        GlassFishSSLContextFactory factory = createFactory("s1as");
        IOException e = assertThrows(IOException.class, factory::create);
        assertThat(e.getMessage(), containsString("s1as"));
    }


    private static GlassFishSSLContextFactory createFactory(String keyAlias) {
        GlassFishSSLContextFactory factory = new GlassFishSSLContextFactory(EMPTY_LOCATOR);
        factory.setAttribute("keystore", listenerKeyStore.getAbsolutePath());
        factory.setAttribute("keystoreType", "PKCS12");
        factory.setAttribute("keystorePass", LISTENER_PASSWORD);
        factory.setAttribute(ATTR_KEYSTORE_CONFIGURED, "true");
        factory.setAttribute("truststore", listenerTrustStore.getAbsolutePath());
        factory.setAttribute("truststoreType", "PKCS12");
        factory.setAttribute("truststorePass", LISTENER_PASSWORD);
        factory.setAttribute(ATTR_TRUSTSTORE_CONFIGURED, "true");
        factory.setAttribute("keyAlias", keyAlias);
        return factory;
    }
}
