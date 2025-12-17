/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.lang.System.Logger;
import java.net.URI;
import java.util.Properties;

import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.AUTO_INSTALL_PROP;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.AUTO_START_PROP;
import static java.lang.System.Logger.Level.DEBUG;
import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;

/**
 * This activator is used when glassfish.jar is installed and started
 * in an existing OSGi runtime. It expects install root and instance root
 * to be set via framework context properties called com.sun.aas.installRoot and com.sun.aas.instanceRoot
 * respectively. The former one refers to the directory where glassfish is installed.
 * (e.g., /tmp/glassfish8/glassfish)
 * The latter one refers to the domain directory - this is a directory containing
 * configuration information and deployed applications, etc.
 * If instance root is not set, it defaults to $installRoot/domains/domain1/.
 * <p/>
 * Depending on the two context, it either builds a new GlassFishRuntime from scratch (i.e., by installing
 * necessary GlassFish bundles) or it just makes a new GlassFishRuntime and registers it in service registry.
 * Former is the case when user installs and starts just this bundle, where as latter is the case when this
 * bundle is invoked as part of GlassFish launching code as found in {@link OSGiGlassFishRuntimeBuilder}.
 * The reason for registering a GlassFishRuntime service is that the service has dependency on things like HK2
 * which are not available via parent loader, so unless we register here, we will have ClassCastException.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @see #prepareStartupContext(org.osgi.framework.BundleContext)
 */
public class GlassFishMainActivator implements BundleActivator {

    private static final Logger LOG = System.getLogger(GlassFishMainActivator.class.getName());

    private static final String[] DEFAULT_INSTALLATION_LOCATIONS_RELATIVE = new String[]{
        // Order is important.
        "modules/",
        "modules/autostart/"
    };

    private static final String[] DEFAULT_START_LOCATIONS_RELATIVE = new String[]{
        // Order is important.
        // osgi-resource-locator must come ahead of osgi-adapter
        "modules/osgi-resource-locator.jar",
        "modules/osgi-adapter.jar",
        "modules/autostart/"
    };


    /**
     * GlassFishRuntime being a singleton can't be bootstrapped more than once.
     * We can't maintain a static variable & perform a null check to detect whether it has been bootstrapped
     * or not, because this class being a non-exported class will get replaced when this bundle is updated.
     * Nor can we assume that GlassFishRuntime.class is loaded by this bundle, for we actually expect/recommend
     * users to have embeddable packages be part of system bundle. In such a case, we can't bootstrap it
     * more than once in the same framework without shutting down GlassFishRuntime.
     * So, we make use of the service registry to help us detect if we have bootstrapped the runtime
     * or someone else has. When we bootstrap, we note it by storing it in a field and accordingly shutdown in our
     * stop().
     *
     * @see #start
     */
    private GlassFishRuntime gfr;

    /**
     * The created GlassFish instance - can be null. We currently create GlassFish only in embedded mode.
     * In non-embedde mode, we let it get created by the launcher.
     */
    private GlassFish gf;

    /**
     * Are in activated in embedded or non-embedded mode?
     * Depending on the mode, either we bootstrap the runtime or just register the runtime as a service.
     */
    private boolean embedded;
    private String installRoot;


    @Override
    public void start(BundleContext context) throws Exception {
        embedded = context.getProperty(BootstrapKeys.BUILDER_NAME_PROPERTY) == null;
        LOG.log(DEBUG, "Starting {0}; embedded: {1}", context.getBundle(), embedded);
        if (embedded) {
            startEmbedded(context);
        } else {
            startNonEmbedded(context);
        }
    }


    @Override
    public void stop(BundleContext context) throws Exception {
        if (!embedded) {
            LOG.log(DEBUG, "We are in non-embedded mode, so {0} has nothing to do.", context.getBundle());
            return;
        }
        try {
            // gf can be null - especially in non-embedded mode.
            if (gf != null && gf.getStatus() != GlassFish.Status.DISPOSED) {
                // dispose calls stop
                gf.dispose();
            }
        } finally {
            gf = null;
        }

        if (gfr != null) {
            // gfr is non-null only if this activator has bootstrapped, else it's null.
            gfr.shutdown();
            gfr = null;
        }
    }


    private void startNonEmbedded(BundleContext context) {
        GlassFishRuntime embeddedGfr = new EmbeddedOSGiGlassFishRuntime(context);
        context.registerService(GlassFishRuntime.class.getName(), embeddedGfr, null);
        LOG.log(DEBUG, "Registered {0} in service registry.", embeddedGfr);
    }


    private void startEmbedded(BundleContext context) throws GlassFishException {
        final Properties properties = prepareStartupContext(context);
        final BootstrapProperties bsProperties = new BootstrapProperties(properties);
        System.out.println(GlassFishRuntime.class + " is loaded by [" + GlassFishRuntime.class.getClassLoader() + "]");
        GlassFishRuntime existingGfr = lookupGfr(context);
        if (existingGfr == null) {
            LOG.log(DEBUG, "Bootstrapping a new GlassFishRuntime");
            // Should we do the following in a separate thread?
            gfr = GlassFishRuntime.bootstrap(bsProperties, getClass().getClassLoader());
            existingGfr = gfr;
        } else {
            LOG.log(DEBUG, "Starting existing GlassFishRuntime: [{0}]", existingGfr);
        }
        gf = existingGfr.newGlassFish(new GlassFishProperties(properties));
        gf.start();
    }


    /**
     * @return an already bootstrapped GlassFishRuntime or null if no such runtime is bootstrapped
     */
    private GlassFishRuntime lookupGfr(BundleContext context) {
        if (context == null) {
            return null;
        }
        final ServiceReference<GlassFishRuntime> serviceReference = context.getServiceReference(GlassFishRuntime.class);
        return serviceReference == null ? null : context.getService(serviceReference);
    }


    private Properties prepareStartupContext(final BundleContext context) {
        Properties properties = new Properties();
        properties.putAll(System.getProperties());

        // override by what's defined in BundleContext,
        for (String key : properties.stringPropertyNames()) {
            String value = context.getProperty(key);
            if (value != null && !value.equals(System.getProperty(key))) {
                properties.setProperty(key, value);
            }
        }

        installRoot = context.getProperty(INSTALL_ROOT.getPropertyName());
        properties.setProperty(INSTALL_ROOT.getPropertyName(), installRoot);
        String instanceRoot = context.getProperty(INSTANCE_ROOT.getPropertyName());
        properties.setProperty(INSTANCE_ROOT.getPropertyName(), instanceRoot);
        properties.putAll(makeProvisioningOptions(context));

        // This property is understood by our corresponding builder.
        properties.setProperty(BootstrapKeys.BUILDER_NAME_PROPERTY, EmbeddedOSGiGlassFishRuntimeBuilder.class.getName());
        return properties;
    }

    private Properties makeProvisioningOptions(BundleContext context) {
        Properties provisioningOptions = new Properties();
        URI installURI = new File(installRoot).toURI();
        String installLocations = context.getProperty(AUTO_INSTALL_PROP);
        if (installLocations == null) {
            StringBuilder defaultInstallLocations = new StringBuilder();
            for (String entry : DEFAULT_INSTALLATION_LOCATIONS_RELATIVE) {
                defaultInstallLocations.append(installURI.resolve(entry)).append(' ');
            }
            installLocations = defaultInstallLocations.toString();
        }
        provisioningOptions.setProperty(AUTO_INSTALL_PROP, installLocations);
        String startLocations = context.getProperty(AUTO_START_PROP);
        if (startLocations == null) {
            StringBuilder defaultStartLocations = new StringBuilder();
            for (String entry : DEFAULT_START_LOCATIONS_RELATIVE) {
                defaultStartLocations.append(installURI.resolve(entry)).append(' ');
            }
            startLocations = defaultStartLocations.toString();
        }
        provisioningOptions.setProperty(AUTO_START_PROP, startLocations);
        LOG.log(DEBUG, "Provisioning options are {0}", provisioningOptions);
        return provisioningOptions;
    }
}
