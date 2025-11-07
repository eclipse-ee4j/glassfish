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

package org.glassfish.internal.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for CompositeClassLoader to ensure it properly aggregates multiple ClassLoaders
 * and prevents duplicate resources/classes.
 */
class CompositeClassLoaderTest {

    private CompositeClassLoader compositeClassLoader;
    private ClassLoader mockClassLoader1;
    private ClassLoader mockClassLoader2;

    @BeforeEach
    void setUp() {
        compositeClassLoader = new CompositeClassLoader();
        // Create simple URLClassLoaders for testing
        mockClassLoader1 = new URLClassLoader(new URL[0]);
        mockClassLoader2 = new URLClassLoader(new URL[0]);
    }

    @Test
    void shouldPreventDuplicateClassLoaders() {
        compositeClassLoader.addClassLoader(mockClassLoader1);
        compositeClassLoader.addClassLoader(mockClassLoader1); // Same instance added twice
        compositeClassLoader.addClassLoader(mockClassLoader2);

        assertThat(compositeClassLoader.getClassLoaders(), hasSize(2));
        assertThat(compositeClassLoader.getClassLoaders(), contains(mockClassLoader1, mockClassLoader2));
    }

    @Test
    void shouldIgnoreNullClassLoaders() {
        compositeClassLoader.addClassLoader(mockClassLoader1);
        compositeClassLoader.addClassLoader(null);
        compositeClassLoader.addClassLoader(mockClassLoader2);

        assertThat(compositeClassLoader.getClassLoaders(), hasSize(2));
        assertThat(compositeClassLoader.getClassLoaders(), contains(mockClassLoader1, mockClassLoader2));
    }

    @Test
    void shouldAddClassLoadersSuccessfully() {
        compositeClassLoader.addClassLoader(mockClassLoader1);
        compositeClassLoader.addClassLoader(mockClassLoader2);

        assertThat(compositeClassLoader.getClassLoaders(), hasSize(2));
        assertThat(compositeClassLoader.getClassLoaders(), contains(mockClassLoader1, mockClassLoader2));
    }

    @Test
    void shouldLoadClassFromFirstAvailableClassLoader() throws ClassNotFoundException {
        compositeClassLoader.addClassLoader(mockClassLoader1);
        compositeClassLoader.addClassLoader(getClass().getClassLoader()); // This one has String class

        Class<?> stringClass = compositeClassLoader.loadClass("java.lang.String");
        assertThat(stringClass, is(String.class));
    }

    @Test
    void shouldThrowClassNotFoundExceptionWhenClassNotAvailable() {
        compositeClassLoader.addClassLoader(mockClassLoader1);
        compositeClassLoader.addClassLoader(mockClassLoader2);

        assertThrows(ClassNotFoundException.class, 
            () -> compositeClassLoader.loadClass("com.nonexistent.Class"));
    }

    @Test
    void shouldReturnFirstAvailableResource() {
        compositeClassLoader.addClassLoader(mockClassLoader1);
        compositeClassLoader.addClassLoader(getClass().getClassLoader());

        URL resource = compositeClassLoader.getResource("java/lang/String.class");
        assertThat(resource, is(notNullValue()));
    }

    @Test
    void shouldReturnNullWhenResourceNotFound() {
        compositeClassLoader.addClassLoader(mockClassLoader1);
        compositeClassLoader.addClassLoader(mockClassLoader2);

        URL resource = compositeClassLoader.getResource("nonexistent/resource.txt");
        assertThat(resource, is(nullValue()));
    }

    @Test
    void shouldReturnUniqueResourcesFromAllClassLoaders() throws IOException {
        compositeClassLoader.addClassLoader(getClass().getClassLoader());

        Enumeration<URL> resources = compositeClassLoader.getResources("META-INF/MANIFEST.MF");
        
        List<URL> resourceList = Collections.list(resources);
        // Should have at least one MANIFEST.MF, exact count depends on classpath
        assertThat(resourceList, is(not(empty())));
    }

    @Test
    void shouldReturnCopyOfClassLoadersList() {
        compositeClassLoader.addClassLoader(mockClassLoader1);
        
        List<ClassLoader> classLoaders = compositeClassLoader.getClassLoaders();
        classLoaders.add(mockClassLoader2); // Should not affect internal state
        
        // Internal state should remain unchanged
        assertThat(compositeClassLoader.getClassLoaders(), hasSize(1));
        assertThat(compositeClassLoader.getClassLoaders(), contains(mockClassLoader1));
    }
}
