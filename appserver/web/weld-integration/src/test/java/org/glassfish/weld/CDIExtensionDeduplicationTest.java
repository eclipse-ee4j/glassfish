/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.weld;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.glassfish.internal.api.CompositeClassLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * Integration test to verify that CDI extensions are properly deduplicated when
 * using CompositeClassLoader in the Weld integration.
 */
class CDIExtensionDeduplicationTest {

    private ClassLoader mockClassLoader1;
    private ClassLoader mockClassLoader2;
    private CompositeClassLoader compositeClassLoader;

    @BeforeEach
    void setUp() {
        mockClassLoader1 = createMock(ClassLoader.class);
        mockClassLoader2 = createMock(ClassLoader.class);
        compositeClassLoader = new CompositeClassLoader();
    }

    @Test
    void shouldDeduplicateServiceFilesFromMultipleClassLoaders() throws Exception {
        // Simulate the same service file being found in multiple classloaders
        URL serviceUrl1 = URI.create("file:///path/to/services/jakarta.enterprise.inject.spi.Extension").toURL();
        URL serviceUrl2 = URI.create("file:///path/to/services/jakarta.enterprise.inject.spi.Extension").toURL(); // Same URL
        URL serviceUrl3 = URI.create("file:///different/path/services/jakarta.enterprise.inject.spi.Extension").toURL();

        List<URL> urls1 = new ArrayList<>();
        urls1.add(serviceUrl1);
        urls1.add(serviceUrl3);

        List<URL> urls2 = new ArrayList<>();
        urls2.add(serviceUrl2); // Duplicate of serviceUrl1
        urls2.add(serviceUrl3); // Duplicate

        expect(mockClassLoader1.getResources("META-INF/services/jakarta.enterprise.inject.spi.Extension"))
                .andReturn(Collections.enumeration(urls1));
        expect(mockClassLoader2.getResources("META-INF/services/jakarta.enterprise.inject.spi.Extension"))
                .andReturn(Collections.enumeration(urls2));

        replay(mockClassLoader1, mockClassLoader2);

        compositeClassLoader.addClassLoader(mockClassLoader1);
        compositeClassLoader.addClassLoader(mockClassLoader2);

        Enumeration<URL> resources = compositeClassLoader.getResources(
                "META-INF/services/jakarta.enterprise.inject.spi.Extension");

        // Convert to list once to avoid consuming enumeration multiple times
        List<URL> resourceList = Collections.list(resources);

        // Should only return unique URLs
        assertThat(resourceList, hasSize(2));
        assertThat(resourceList, containsInAnyOrder(serviceUrl1, serviceUrl3));

        verify(mockClassLoader1, mockClassLoader2);
    }

    @Test
    void shouldPreventDuplicateClassLoadersAutomatically() throws Exception {
        // When the same classloader instance is added multiple times (common case for web apps)
        URLClassLoader singleClassLoader = new URLClassLoader(new URL[0]);

        compositeClassLoader.addClassLoader(singleClassLoader);
        compositeClassLoader.addClassLoader(singleClassLoader); // Same instance added twice

        // Set automatically prevents duplicates
        assertThat(compositeClassLoader.getClassLoaders(), hasSize(1));
        assertThat(compositeClassLoader.getClassLoaders(), contains(singleClassLoader));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldPreventDuplicateExtensionInstances() throws Exception {
        // This test simulates the core issue: preventing duplicate CDI extension instances
        // when the same extension class is found through multiple classloaders

        String extensionClassName = "com.example.MyExtension";

        expect(mockClassLoader1.loadClass(extensionClassName))
                .andReturn((Class) String.class);
        expect(mockClassLoader2.loadClass(extensionClassName))
                .andThrow(new ClassNotFoundException()).anyTimes();

        replay(mockClassLoader1, mockClassLoader2);

        compositeClassLoader.addClassLoader(mockClassLoader1);
        compositeClassLoader.addClassLoader(mockClassLoader2);

        // Should load from first available classloader
        Class<?> loadedClass = compositeClassLoader.loadClass(extensionClassName);

        assertThat(loadedClass, is(String.class));

        verify(mockClassLoader1, mockClassLoader2);
    }
}
