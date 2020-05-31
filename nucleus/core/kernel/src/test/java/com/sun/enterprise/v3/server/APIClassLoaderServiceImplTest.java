/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sun.enterprise.module.ModuleLifecycleListener;
import com.sun.enterprise.module.single.SingleModulesRegistry;

public class APIClassLoaderServiceImplTest {
    int loadClassCalls;
    int getResourceCalls;

    @Before
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
        try {
            APIClassLoaderServiceImpl apiClassLoaderService = new APIClassLoaderServiceImpl();

            // Set up a fake ModulesRegistry to exercise ModulesRegistry lifecycle events
            FakeClassLoader classLoader = new FakeClassLoader(getClass().getClassLoader());
            FakeModulesRegistry fakeModulesRegistry = new FakeModulesRegistry(classLoader);

            apiClassLoaderService.modulesRegistry = fakeModulesRegistry;

            assertEquals(0, fakeModulesRegistry.getLifecycleListeners().size());

            apiClassLoaderService.postConstruct();

            List<ModuleLifecycleListener> lifecycleListeners = fakeModulesRegistry.getLifecycleListeners();

            assertEquals("apiClassLoaderService should have registered a lifecycle listener", 1, fakeModulesRegistry.getLifecycleListeners().size());

            ModuleLifecycleListener lifecycleListener = lifecycleListeners.iterator().next();

            
            // assert that the classloader isn't called on to load the same bad
            // class twice
            assertEquals(0, loadClassCalls);

            final String BAD_CLASSNAME = "BADCLASS";

            try {
                apiClassLoaderService.getAPIClassLoader().loadClass(BAD_CLASSNAME);
            } catch (ClassNotFoundException e) {
                // ignore
            }

            assertEquals("Classloader.loadClass not called at all", 1, loadClassCalls);

            try {
                apiClassLoaderService.getAPIClassLoader().loadClass(BAD_CLASSNAME);
            } catch (ClassNotFoundException e) {
                // ignore
            }

            assertEquals("blacklist not honored, excessive call to classloader.load", 1, loadClassCalls);

            
            // try same thing with resources

            assertEquals(0, getResourceCalls); // sanity

            final String BAD_RESOURCE = "BADRESOURCE";

            apiClassLoaderService.getAPIClassLoader().getResource(BAD_RESOURCE);

            assertEquals("Classloader.findResource not called at all", 1, getResourceCalls);

            apiClassLoaderService.getAPIClassLoader().getResource(BAD_RESOURCE);

            assertEquals("blacklist not honored, excessive call to classloader.getResource", 1, getResourceCalls);

            
            //
            // Now signal that a new module has been loaded, clearing the blacklist
            //

            lifecycleListener.moduleInstalled(null);

            apiClassLoaderService.getAPIClassLoader().getResource(BAD_RESOURCE);
            assertEquals("blacklist did not clear after a module was installed", 2, getResourceCalls);

            try {
                apiClassLoaderService.getAPIClassLoader().loadClass(BAD_CLASSNAME);
            } catch (ClassNotFoundException e) {
                // ignore
            }

            assertEquals("blacklist did not clear after a module was installed", 2, loadClassCalls);

            
            //
            // Now signal that a new module has been updated, clearing the blacklist
            //

            lifecycleListener.moduleUpdated(null);

            apiClassLoaderService.getAPIClassLoader().getResource(BAD_RESOURCE);
            assertEquals("blacklist did not clear after a module was updated", 3, getResourceCalls);

            try {
                apiClassLoaderService.getAPIClassLoader().loadClass(BAD_CLASSNAME);
            } catch (ClassNotFoundException e) {
                // ignore
            }

            assertEquals("blacklist did not clear after a module was updated", 3, loadClassCalls);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

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
