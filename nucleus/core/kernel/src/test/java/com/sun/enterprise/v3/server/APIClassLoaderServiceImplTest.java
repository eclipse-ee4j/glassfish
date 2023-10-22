/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.server;

import com.sun.enterprise.module.ModuleLifecycleListener;
import com.sun.enterprise.module.single.SingleModulesRegistry;

import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class APIClassLoaderServiceImplTest {
    private int loadClassCalls;
    private int getResourceCalls;

    @BeforeEach
    public void setUp() {
        loadClassCalls = 0;
        getResourceCalls = 0;
    }

    /**
     * This test ensures that the ApiClassLoaderService will not attempt to load a class or find a resource after an initial
     * negative result until a module is installed or update.
     */
    @Test
    public void testBlackList() {
        APIClassLoaderServiceImpl apiClassLoaderService = new APIClassLoaderServiceImpl();

        // Set up a fake ModulesRegistry to exercise ModulesRegistry lifecycle events
        FakeClassLoader classLoader = new FakeClassLoader(getClass().getClassLoader());
        FakeModulesRegistry fakeModulesRegistry = new FakeModulesRegistry(classLoader);
        assertThat(fakeModulesRegistry.getLifecycleListeners(), hasSize(0));

        apiClassLoaderService.modulesRegistry = fakeModulesRegistry;
        apiClassLoaderService.postConstruct();

        List<ModuleLifecycleListener> lifecycleListeners = fakeModulesRegistry.getLifecycleListeners();
        assertThat("apiClassLoaderService should have registered a lifecycle listener", fakeModulesRegistry.getLifecycleListeners(),
                hasSize(1));

        ModuleLifecycleListener lifecycleListener = lifecycleListeners.iterator().next();

        // assert that the classloader isn't called on to load the same bad class twice
        assertEquals(0, loadClassCalls);

        final String BAD_CLASSNAME = "BADCLASS";
        assertThrows(ClassNotFoundException.class, () -> apiClassLoaderService.getAPIClassLoader().loadClass(BAD_CLASSNAME));
        assertEquals(1, loadClassCalls, "Classloader.loadClass not called at all");
        assertThrows(ClassNotFoundException.class, () -> apiClassLoaderService.getAPIClassLoader().loadClass(BAD_CLASSNAME));

        assertEquals(1, loadClassCalls, "blacklist not honored, excessive call to classloader.load");

        // try same thing with resources
        assertEquals(0, getResourceCalls); // sanity
        final String BAD_RESOURCE = "BADRESOURCE";
        apiClassLoaderService.getAPIClassLoader().getResource(BAD_RESOURCE);
        assertEquals(1, getResourceCalls, "Classloader.findResource not called at all");

        apiClassLoaderService.getAPIClassLoader().getResource(BAD_RESOURCE);
        assertEquals(1, getResourceCalls, "blacklist not honored, excessive call to classloader.getResource");

        // Now signal that a new module has been loaded, clearing the blacklist
        lifecycleListener.moduleInstalled(null);

        apiClassLoaderService.getAPIClassLoader().getResource(BAD_RESOURCE);
        assertEquals(2, getResourceCalls, "blacklist did not clear after a module was installed");

        assertThrows(ClassNotFoundException.class, () -> apiClassLoaderService.getAPIClassLoader().loadClass(BAD_CLASSNAME));

        assertEquals(2, loadClassCalls, "blacklist did not clear after a module was installed");

        // Now signal that a new module has been updated, clearing the blacklist
        lifecycleListener.moduleUpdated(null);

        apiClassLoaderService.getAPIClassLoader().getResource(BAD_RESOURCE);
        assertEquals(3, getResourceCalls, "blacklist did not clear after a module was updated");

        assertThrows(ClassNotFoundException.class, () -> apiClassLoaderService.getAPIClassLoader().loadClass(BAD_CLASSNAME));

        assertEquals(3, loadClassCalls, "blacklist did not clear after a module was updated");
    }

    class FakeModulesRegistry extends SingleModulesRegistry {
        public FakeModulesRegistry(ClassLoader cl) {
            super(cl);
        }
    }

    class FakeClassLoader extends ClassLoader {
        public FakeClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> loadClass(String arg0) throws ClassNotFoundException {
            loadClassCalls++;
            return super.loadClass(arg0);
        }

        @Override
        protected URL findResource(String arg0) {
            getResourceCalls++;
            return super.findResource(arg0);
        }

    }
}
