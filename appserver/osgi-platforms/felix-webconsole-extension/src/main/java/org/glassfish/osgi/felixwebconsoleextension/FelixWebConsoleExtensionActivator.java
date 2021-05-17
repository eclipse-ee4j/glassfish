/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.osgi.felixwebconsoleextension;

import org.apache.felix.webconsole.BrandingPlugin;
import org.apache.felix.webconsole.WebConsoleSecurityProvider;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * This activator servers following purposes:
 * a) Registers a BrandingPlugin service to customize the look and feel.
 * See http://felix.apache.org/site/branding-the-web-console.html for more details.
 * b) Registers configuration object to select the right HttpService.
 * c) Registers a SecurityProvider to integrate with GlassFish security service.
 *
 * @author sanjeeb.sahoo@oracle.com
 * @author tangyong@cn.fujitsu.com
 */
public class FelixWebConsoleExtensionActivator implements BundleActivator {

    private Logger logger = Logger.getLogger(getClass().getPackage().getName());
    private BundleContext context;
    private static final String WEBCONSOLE_PID = "org.apache.felix.webconsole.internal.servlet.OsgiManager";
    private static final String PROP_HTTP_SERVICE_SELECTOR = "http.service.filter";
    private static final String PROP_REALM = "realm";
    private static final String REALM="GlassFish Server";
    private static final String HTTP_SERVICE_SELECTOR = "VirtualServer=server"; // We bind to default virtual host
    private ServiceTracker tracker;

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        registerBrandingPlugin();
        configureConsole();
        registerWebConsoleSecurityProvider(); // GLASSFISH-12975
    }

    private void registerWebConsoleSecurityProvider() {
         final GlassFishSecurityProvider secprovider = new GlassFishSecurityProvider();
         secprovider.setBundleContext(context);
        context.registerService(WebConsoleSecurityProvider.class.getName(), secprovider, null);
         logger.logp(Level.INFO, "FelixWebConsoleExtensionActivator", "start", "Registered {0}", new Object[]{secprovider});
    }

    private void configureConsole() {
        tracker = new ServiceTracker(context, ConfigurationAdmin.class.getName(), null) {
            @Override
            public Object addingService(ServiceReference reference) {
                try {
                    ConfigurationAdmin ca = ConfigurationAdmin.class.cast(context.getService(reference));
                    org.osgi.service.cm.Configuration config = null;
                    config = ca.getConfiguration(WEBCONSOLE_PID, null);
                    Dictionary old = config.getProperties();
                    Dictionary newProps = new Hashtable();
                    newProps.put(PROP_HTTP_SERVICE_SELECTOR, HTTP_SERVICE_SELECTOR);
                    newProps.put(PROP_REALM, REALM);
                    if (old != null) {
                        old.remove( Constants.SERVICE_PID );
                    }

                    if( !newProps.equals( old ) )
                    {
                        if (config.getBundleLocation() != null)
                        {
                            config.setBundleLocation(null);
                        }
                        config.update(newProps);
                    }
                } catch (IOException e) {
                    logger.logp(Level.INFO, "FelixWebConsoleExtensionActivator", "addingService",
                            "Failed to update webconsole configuration", e);
                }
                return null;
            }
        };
        tracker.open();
    }

    private void registerBrandingPlugin() {
        final GlassFishBrandingPlugin service = new GlassFishBrandingPlugin();
        context.registerService(BrandingPlugin.class.getName(), service, null);
        logger.logp(Level.INFO, "FelixWebConsoleExtensionActivator", "start", "Registered {0}", new Object[]{service});
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (tracker != null) {
            tracker.close();
        }
    }
}
