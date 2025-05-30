/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.glassfish.grizzly.Transport;
import org.glassfish.grizzly.config.dom.NetworkAddressValidator;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.ThreadPool;
import org.glassfish.grizzly.config.test.GrizzlyConfigTestHelper;
import org.glassfish.grizzly.config.test.example.DummySelectionKeyHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.memory.ByteBufferManager;
import org.glassfish.grizzly.memory.HeapMemoryManager;
import org.glassfish.grizzly.memory.MemoryManager;
import org.glassfish.grizzly.nio.NIOTransport;
import org.glassfish.grizzly.strategies.SameThreadIOStrategy;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.main.jdke.security.KeyTool;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_FILENAME_DEFAULT;
import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_PASSWORD_DEFAULT;
import static com.sun.enterprise.util.SystemPropertyConstants.TRUSTSTORE_FILENAME_DEFAULT;
import static org.glassfish.embeddable.GlassFishVariable.KEYSTORE_FILE;
import static org.glassfish.embeddable.GlassFishVariable.KEYSTORE_PASSWORD;
import static org.glassfish.embeddable.GlassFishVariable.TRUSTSTORE_FILE;
import static org.glassfish.embeddable.GlassFishVariable.TRUSTSTORE_PASSWORD;
import static org.glassfish.main.jdke.props.SystemProperties.setProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Created Jan 5, 2009
 *
 * @author <a href="mailto:justin.d.lee@oracle.com">Justin Lee</a>
 */
public class GrizzlyConfigTest {

    private static final GrizzlyConfigTestHelper helper = new GrizzlyConfigTestHelper(GrizzlyConfigTest.class);
    @TempDir
    private static File tempDir;

    @Test
    public void processConfig() throws IOException, InstantiationException {
        GrizzlyConfig grizzlyConfig = null;
        try {
            grizzlyConfig = new GrizzlyConfig("grizzly-config.xml");
            grizzlyConfig.setupNetwork();
            int count = 0;
            for (GrizzlyListener listener : grizzlyConfig.getListeners()) {
                helper.addStaticHttpHandler((GenericGrizzlyListener) listener, count++);
            }
            final String content = helper.getContent(new URL("http://localhost:38082").openConnection());
            final String content2 = helper.getContent(new URL("http://localhost:38083").openConnection());
            final String content3 = helper.getContent(new URL("http://localhost:38084").openConnection());
            assertEquals("<html><body>You've found the server on port 38082</body></html>", content);
            assertEquals("<html><body>You've found the server on port 38083</body></html>", content2);
            assertEquals("<html><body>You've found the server on port 38084</body></html>", content3);
        } finally {
            if (grizzlyConfig != null) {
                grizzlyConfig.shutdown();
            }
        }
    }

    @Test
    public void references() throws IOException {
        GrizzlyConfig grizzlyConfig = null;
        try {
            grizzlyConfig = new GrizzlyConfig("grizzly-config.xml");
            final List<NetworkListener> list = grizzlyConfig.getConfig().getNetworkListeners().getNetworkListener();
            final NetworkListener listener = list.get(0);
            boolean found = false;
            for (NetworkListener ref : listener.findProtocol().findNetworkListeners()) {
                found |= ref.getName().equals(listener.getName());
            }
            assertTrue(found, "Should find the NetworkListener in the list of references from Protocol");
            found = false;
            for (NetworkListener ref : listener.findTransport().findNetworkListeners()) {
                found |= ref.getName().equals(listener.getName());
            }
            assertTrue(found, "Should find the NetworkListener in the list of references from Transport");
            found = false;
            for (NetworkListener ref : listener.findThreadPool().findNetworkListeners()) {
                found |= ref.getName().equals(listener.getName());
            }
            assertTrue(found, "Should find the NetworkListener in the list of references from ThreadPool");
        } finally {
            if (grizzlyConfig != null) {
                grizzlyConfig.shutdown();
            }
        }
    }

    @Test
    public void defaults() throws IOException {
        GrizzlyConfig grizzlyConfig = null;
        try {
            grizzlyConfig = new GrizzlyConfig("grizzly-config.xml");
            final ThreadPool threadPool = grizzlyConfig.getConfig().getNetworkListeners().getThreadPool().get(0);
            assertEquals("5", threadPool.getMaxThreadPoolSize());
        } finally {
            if (grizzlyConfig != null) {
                grizzlyConfig.shutdown();
            }
        }
    }

    @Test
    public void testDefaultBufferConfiguration() throws Exception {
        GrizzlyConfig grizzlyConfig = null;
        try {
            configure();
            grizzlyConfig = new GrizzlyConfig("grizzly-config.xml");
            grizzlyConfig.setupNetwork();
            final String bufferType = grizzlyConfig.getConfig().getNetworkListeners().getNetworkListener().get(0).findTransport().getByteBufferType();
            assertEquals("heap", bufferType);
            GenericGrizzlyListener genericGrizzlyListener =
                    (GenericGrizzlyListener) getListener(grizzlyConfig, "http-listener-1");
            MemoryManager<?> mm = genericGrizzlyListener.getTransport().getMemoryManager();
            assertEquals(HeapMemoryManager.class.getName(), mm.getClass().getName());
        } finally {
            if (grizzlyConfig != null) {
                grizzlyConfig.shutdownNetwork();
                grizzlyConfig.shutdown();
            }
        }
    }

    @Test
    public void testSelectionKeyHandlerConfiguration() throws Exception {
        GrizzlyConfig grizzlyConfig = null;
        try {
            configure();
            grizzlyConfig = new GrizzlyConfig("grizzly-config-skh.xml");
            grizzlyConfig.setupNetwork();
            final String bufferType = grizzlyConfig.getConfig().getNetworkListeners().getNetworkListener().get(0).findTransport().getByteBufferType();
            GenericGrizzlyListener genericGrizzlyListener =
                    (GenericGrizzlyListener) getListener(grizzlyConfig, "http-listener-1");
            NIOTransport transport = (NIOTransport) genericGrizzlyListener.getTransport();
            assertNotSame(DummySelectionKeyHandler.class.getName(), transport.getSelectionKeyHandler().getClass().getName());
        } finally {
            if (grizzlyConfig != null) {
                grizzlyConfig.shutdownNetwork();
                grizzlyConfig.shutdown();
            }
        }
    }

    @Test
    public void testDirectBufferConfiguration() throws Exception {
        GrizzlyConfig grizzlyConfig = null;
        try {
            configure();
            grizzlyConfig = new GrizzlyConfig("grizzly-direct-buffer.xml");
            grizzlyConfig.setupNetwork();
            final String bufferType = grizzlyConfig.getConfig().getNetworkListeners().getNetworkListener().get(0).findTransport().getByteBufferType();
            assertEquals("direct", bufferType);
            GenericGrizzlyListener genericGrizzlyListener =
                                           (GenericGrizzlyListener) getListener(grizzlyConfig, "http-listener-1");
            MemoryManager<?> mm = genericGrizzlyListener.getTransport().getMemoryManager();
            assertEquals(ByteBufferManager.class.getName(), mm.getClass().getName());
            assertTrue(((ByteBufferManager) mm).isDirect());
        } finally {
            if (grizzlyConfig != null) {
                grizzlyConfig.shutdownNetwork();
                grizzlyConfig.shutdown();
            }
        }
    }

    @Test
    public void testSocketBufferConfiguration() throws Exception {
        GrizzlyConfig grizzlyConfig = null;
        try {
            configure();
            grizzlyConfig = new GrizzlyConfig("grizzly-config-socket.xml");
            grizzlyConfig.setupNetwork();
            GenericGrizzlyListener genericGrizzlyListener = (GenericGrizzlyListener) getListener(grizzlyConfig,
                "http-listener-1");
            Transport t = genericGrizzlyListener.getTransport();

            assertEquals(-1, t.getReadBufferSize());
            assertEquals(-1, t.getWriteBufferSize());

            genericGrizzlyListener = (GenericGrizzlyListener) getListener(grizzlyConfig, "http-listener-2");
            t = genericGrizzlyListener.getTransport();
            assertEquals(8192, t.getReadBufferSize());
            assertEquals(-1, t.getWriteBufferSize());

            genericGrizzlyListener = (GenericGrizzlyListener) getListener(grizzlyConfig, "http-listener-3");
            t = genericGrizzlyListener.getTransport();
            assertEquals(-1, t.getReadBufferSize());
            assertEquals(8000, t.getWriteBufferSize());

            genericGrizzlyListener = (GenericGrizzlyListener) getListener(grizzlyConfig, "http-listener-4");
            t = genericGrizzlyListener.getTransport();
            assertEquals(6000, t.getReadBufferSize());
            assertEquals(5000, t.getWriteBufferSize());
        } finally {
            if (grizzlyConfig != null) {
                grizzlyConfig.shutdownNetwork();
                grizzlyConfig.shutdown();
            }
        }
    }

    @Test
    public void ssl() throws URISyntaxException, IOException {
        GrizzlyConfig grizzlyConfig = null;
        try {
            configure();
            grizzlyConfig = new GrizzlyConfig("grizzly-config-ssl.xml");
            grizzlyConfig.setupNetwork();
            int count = 0;
            for (GrizzlyListener listener : grizzlyConfig.getListeners()) {
                helper.addStaticHttpHandler((GenericGrizzlyListener) listener, count++);
            }

            assertEquals("<html><body>You've found the server on port 38082</body></html>",
                helper.getContent(new URL("https://localhost:38082").openConnection()));
            assertEquals("<html><body>You've found the server on port 38083</body></html>",
                helper.getContent(new URL("https://localhost:38083").openConnection()));
            assertEquals("<html><body>You've found the server on port 38084</body></html>",
                helper.getContent(new URL("https://localhost:38084").openConnection()));
            assertEquals("<html><body>You've found the server on port 38085</body></html>",
                helper.getContent(new URL("https://localhost:38085").openConnection()));
        } finally {
            if (grizzlyConfig != null) {
                grizzlyConfig.shutdown();
            }
        }
    }

    private void configure() throws IOException {
        File keyStoreFile = new File(tempDir, KEYSTORE_FILENAME_DEFAULT);
        if (keyStoreFile.exists()) {
            return;
        }
        File trustStoreFile = new File(tempDir, TRUSTSTORE_FILENAME_DEFAULT);
        KeyTool keyTool = new KeyTool(keyStoreFile, KEYSTORE_PASSWORD_DEFAULT.toCharArray());
        keyTool.generateKeyPair("s1as", "CN=localhost", "RSA", 1);
        keyTool.copyCertificate("s1as", trustStoreFile);
        setProperty(TRUSTSTORE_FILE.getSystemPropertyName(), trustStoreFile.getAbsolutePath(), true);
        setProperty(TRUSTSTORE_PASSWORD.getSystemPropertyName(), KEYSTORE_PASSWORD_DEFAULT, true);
        setProperty(KEYSTORE_FILE.getSystemPropertyName(), keyStoreFile.getAbsolutePath(), true);
        setProperty(KEYSTORE_PASSWORD.getSystemPropertyName(), KEYSTORE_PASSWORD_DEFAULT, true);
    }

    @Test
    @Disabled(
        "Fails with UnknownHostException, but should probably result in GrizzlyConfigException."
        + " Or it should not try to resolve the address and simply respect it."
        + " That may be useful for environments with strict networking, where admin instance"
        + " doesn't have access to a network used by worker instances."
    )
    public void badConfig() throws IOException {
        assertThrows(GrizzlyConfigException.class, () -> {
            GrizzlyConfig grizzlyConfig = null;
            try {
                grizzlyConfig = new GrizzlyConfig("grizzly-config-bad.xml");
                grizzlyConfig.setupNetwork();
            } finally {
                if (grizzlyConfig != null) {
                    grizzlyConfig.shutdown();
                }
            }
        });
    }

    @Test
    public void timeoutDisabled() throws IOException, InstantiationException {
        GrizzlyConfig grizzlyConfig = null;
        try {
            grizzlyConfig = new GrizzlyConfig("grizzly-config-timeout-disabled.xml");
            grizzlyConfig.setupNetwork();
        } finally {
            if (grizzlyConfig != null) {
                grizzlyConfig.shutdown();
            }
        }
    }

    @Test
    public void testNetworkAddressValidator() {
        NetworkAddressValidator validator = new NetworkAddressValidator();
        assertTrue(validator.isValid("${SOME_PROP}", null));
        assertFalse(validator.isValid("$SOME_PROP}", null));
        assertFalse(validator.isValid("${SOME_PROP", null));
        assertFalse(validator.isValid("{SOME_PROP}", null));
        assertTrue(validator.isValid("127.0.0.1", null));
        assertFalse(validator.isValid("1271.2.1.3", null));
        assertTrue(validator.isValid("::1", null));
        assertFalse(validator.isValid(":1", null));
    }

    @Test
    public void ioStrategySet() throws IOException, InstantiationException {
        GrizzlyConfig grizzlyConfig = null;
        try {
            grizzlyConfig = new GrizzlyConfig("grizzly-config-io-strategies.xml");
            grizzlyConfig.setupNetwork();

            GenericGrizzlyListener genericGrizzlyListener1 = (GenericGrizzlyListener) getListener(grizzlyConfig, "http-listener-1");
            assertEquals(SameThreadIOStrategy.class, genericGrizzlyListener1.getTransport().getIOStrategy().getClass());

            GenericGrizzlyListener genericGrizzlyListener2 = (GenericGrizzlyListener) getListener(grizzlyConfig, "http-listener-2");
            assertEquals(WorkerThreadIOStrategy.class, genericGrizzlyListener2.getTransport().getIOStrategy().getClass());
        } finally {
            if (grizzlyConfig != null) {
                grizzlyConfig.shutdown();
            }
        }
    }

    @Test
    public void schemeOverride() throws IOException, InstantiationException {
        GrizzlyConfig grizzlyConfig = null;
        try {
            grizzlyConfig = new GrizzlyConfig("grizzly-config-scheme-override.xml");
            grizzlyConfig.setupNetwork();
            for (GrizzlyListener listener : grizzlyConfig.getListeners()) {
                helper.setHttpHandler((GenericGrizzlyListener) listener, new HttpHandler() {

                    @Override
                    public void service(Request request, Response response) throws Exception {
                        response.getWriter().write(request.getScheme());
                    }
                });
            }

            final String content = helper.getContent(new URL("http://localhost:38082").openConnection());
            final String content2 = helper.getContent(new URL("http://localhost:38083").openConnection());

            assertEquals("http", content);
            assertEquals("https", content2);
        } finally {
            if (grizzlyConfig != null) {
                grizzlyConfig.shutdown();
            }
        }
    }


    private static GrizzlyListener getListener(GrizzlyConfig grizzlyConfig, String listenerName) {
        for (GrizzlyListener listener : grizzlyConfig.getListeners()) {
            GenericGrizzlyListener genericGrizzlyListener = (GenericGrizzlyListener) listener;
            if (listenerName.equals(genericGrizzlyListener.getName())) {
                return listener;
            }
        }

        return null;
    }
}
