/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.main.boot.osgi;

import com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys;
import com.sun.enterprise.glassfish.bootstrap.cfg.OsgiPlatform;

import java.util.Hashtable;

import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.spi.RuntimeBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;

/**
 * This {@link org.glassfish.embeddable.spi.RuntimeBuilder} is responsible for setting up a {@link GlassFishRuntime}
 * when user has a regular installation of GlassFish and they want to embed GlassFish in an existing OSGi runtime.
 * <p>
 * It sets up the runtime like this:
 * <ol>
 * <li>Installs GlassFish modules.
 * <li>Starts a list of GlassFish bundles.
 * <li>Registers an instance of GlassFishRuntime as service.
 * </ol>
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @see #build(org.glassfish.embeddable.BootstrapProperties)
 * @see #handles(org.glassfish.embeddable.BootstrapProperties)
 */
// Note: Used in a service file!
public class EmbeddedOSGiGlassFishRuntimeBuilder implements RuntimeBuilder {

    @Override
    public boolean handles(BootstrapProperties bsProps) {
        return EmbeddedOSGiGlassFishRuntimeBuilder.class.getName().
                equals(bsProps.getProperties().getProperty(BootstrapKeys.BUILDER_NAME_PROPERTY));
    }

    @Override
    public GlassFishRuntime build(BootstrapProperties bsProps, ClassLoader classloader) throws GlassFishException {
        configureBundles(bsProps);
        provisionBundles(bsProps, classloader);
        BundleContext bundleContext = getBundleContext(classloader);
        GlassFishRuntime gfr = new EmbeddedOSGiGlassFishRuntime(bundleContext);
        bundleContext.registerService(GlassFishRuntime.class.getName(), gfr, (Hashtable) bsProps.getProperties());
        return gfr;
    }

    private void configureBundles(BootstrapProperties bsProps) {
        if (System.getProperty(BootstrapKeys.PLATFORM_PROPERTY_KEY) == null) {
            System.setProperty(BootstrapKeys.PLATFORM_PROPERTY_KEY, OsgiPlatform.Embedded.name());
        }
    }

    private BundleContext getBundleContext(ClassLoader classloader) {
        return BundleReference.class.cast(classloader).getBundle().getBundleContext();
    }

    private void provisionBundles(BootstrapProperties bsProps, ClassLoader classloader) {
        BundleProvisioner bundleProvisioner = new BundleProvisioner(getBundleContext(classloader), bsProps.getProperties());
        bundleProvisioner.installBundles();
        bundleProvisioner.startBundles();
    }

}
