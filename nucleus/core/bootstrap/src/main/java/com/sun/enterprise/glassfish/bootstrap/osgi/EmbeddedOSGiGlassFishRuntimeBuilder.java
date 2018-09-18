/*
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

package com.sun.enterprise.glassfish.bootstrap.osgi;

import com.sun.enterprise.glassfish.bootstrap.Constants;
import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.spi.RuntimeBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;

import java.util.Dictionary;
import java.util.Properties;

/**
 * This {@link org.glassfish.embeddable.spi.RuntimeBuilder} is responsible for setting up a {@link GlassFishRuntime}
 * when user has a regular installation of GlassFish and they want to embed GlassFish in an existing OSGi runtime.
 * <p/>
 * It sets up the runtime like this:
 * 1. Installs GlassFish modules.
 * 2. Starts a list of GlassFish bundles.
 * 3. Registers an instance of GlassFishRuntime as service.
 * <p/>
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @see #build(org.glassfish.embeddable.BootstrapProperties)
 * @see #handles(org.glassfish.embeddable.BootstrapProperties)
 */
public class EmbeddedOSGiGlassFishRuntimeBuilder implements RuntimeBuilder {

    public boolean handles(BootstrapProperties bsProps) {
        return EmbeddedOSGiGlassFishRuntimeBuilder.class.getName().
                equals(bsProps.getProperties().getProperty(Constants.BUILDER_NAME_PROPERTY));
    }

    public GlassFishRuntime build(BootstrapProperties bsProps) throws GlassFishException {
        configureBundles(bsProps);
        provisionBundles(bsProps);
        GlassFishRuntime gfr = new EmbeddedOSGiGlassFishRuntime(getBundleContext());
        Properties props = bsProps.getProperties();
        Dictionary properties = new Properties();
        for (final String name: props.stringPropertyNames())
            properties.put(name, props.getProperty(name));
        getBundleContext().registerService(GlassFishRuntime.class.getName(), gfr, properties);
        return gfr;
    }

    private void configureBundles(BootstrapProperties bsProps) {
        if (System.getProperty(Constants.PLATFORM_PROPERTY_KEY) == null) { // See GLASSFISH-16511 for null check
            // Set this, because some stupid downstream code may be relying on this property
            System.setProperty(Constants.PLATFORM_PROPERTY_KEY, "GenericOSGi");
        }
    }

    private BundleContext getBundleContext() {
        return BundleReference.class.cast(getClass().getClassLoader()).getBundle().getBundleContext();
    }

    private void provisionBundles(BootstrapProperties bsProps) {
        BundleProvisioner bundleProvisioner = new BundleProvisioner(getBundleContext(), bsProps.getProperties());
        bundleProvisioner.installBundles();
        bundleProvisioner.startBundles();
    }

}
