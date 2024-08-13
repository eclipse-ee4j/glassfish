/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.extras.osgicontainer;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

public class OSGiDeployedBundle implements ApplicationContainer<OSGiContainer> {

    private Bundle bundle;

    public OSGiDeployedBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public OSGiContainer getDescriptor() {
        return null;
    }

    public boolean start(ApplicationContext startupContext) throws Exception {
        return resume();
    }

    public boolean stop(ApplicationContext stopContext) {
        return suspend();
    }

    public boolean suspend() {
        if (!isFragment(bundle)) {
            stopBundle();
        }
        return true;
    }

    public boolean resume() throws Exception {
        if (!isFragment(bundle)) {
            startBundle();
        }
        return true;
    }

    public ClassLoader getClassLoader() {
        // return a non-null class loader. This will be set as TCL before the bundle is started or stopped
        // so that operations like JNDI lookup can be successful, as those operations in GlassFish requires
        // a non-null class loader.
        return new BundleClassLoader(bundle);
    }

    private static boolean isFragment(Bundle b) {
        return b.getHeaders().get(Constants.FRAGMENT_HOST) != null;
    }

    private void startBundle() throws BundleException {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            // Some operations like JNDI lookup requires a non-null context class loader, so
            // we need to set a non-null class loader.
            final ClassLoader cl1 = getClassLoader();
            assert(cl1 != null);
            Thread.currentThread().setContextClassLoader(cl1);
            bundle.start(Bundle.START_TRANSIENT | Bundle.START_ACTIVATION_POLICY);
            System.out.println("Started " + bundle);
        } catch (BundleException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    private void stopBundle() {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            // Some operations like JNDI lookup requires a non-null context class loader, so
            // we need to set a non-null class loader.
            final ClassLoader cl1 = getClassLoader();
            assert(cl1 != null);
            Thread.currentThread().setContextClassLoader(cl1);
            bundle.stop(Bundle.STOP_TRANSIENT);
            System.out.println("Stopped " + bundle);
        } catch (BundleException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

}

class BundleClassLoader extends ClassLoader
{
    private Bundle bundle;

    public BundleClassLoader(Bundle b)
    {
        super(Bundle.class.getClassLoader());
        this.bundle = b;
    }

    @Override
    public synchronized Class<?> loadClass(final String name, boolean resolve) throws ClassNotFoundException
    {
        return bundle.loadClass(name);
    }

    @Override
    public URL getResource(String name)
    {
        return bundle.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException
    {
        Enumeration<URL> resources = bundle.getResources(name);
        if (resources == null)
        {
            // This check is needed, because ClassLoader.getResources()
            // expects us to return an empty enumeration.
            resources = new Enumeration<URL>()
            {

                public boolean hasMoreElements()
                {
                    return false;
                }

                public URL nextElement()
                {
                    throw new NoSuchElementException();
                }
            };
        }
        return resources;
    }
}
