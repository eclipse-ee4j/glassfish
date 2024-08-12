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

package org.glassfish.uberjar.uninstaller;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * @author bhavanishankar@dev.java.net
 */

public class GlassFishOSGiModuleUninstaller implements BundleActivator, BundleListener {

    private static Logger logger = Logger.getLogger("embedded-glassfish");

    private final String uberSymbolicName = "org.glassfish.embedded.glassfish-activator";
    private Bundle myself;

    public void start(BundleContext bundleContext) throws Exception {
        myself = bundleContext.getBundle();
        bundleContext.addBundleListener(this);
        logger.info("EmbeddedGlassFishUninstaller started");
    }

    public void stop(BundleContext bundleContext) throws Exception {
        logger.info("EmbeddedGlassFishUninstaller stopped");
    }

    public void bundleChanged(BundleEvent bundleEvent) {
        if (bundleEvent.getType() == BundleEvent.UNINSTALLED) {
            String uninstalledBundle = bundleEvent.getBundle().getSymbolicName();
            if (uberSymbolicName.equals(uninstalledBundle)) {
                logger.info("Embedded GlassFish UberJar is uninstalled. " +
                        "Hence uninstalling all the GlassFish bundles.");
                // bundleEvent.getBundle().getBundleContext(0 returns null, hence use it from 'myself'
                BundleContext context = myself.getBundleContext();
                List<Bundle> uninstalled = new ArrayList();
                logger.info("BundleContext = " + context);
                for (Bundle b : context.getBundles()) {
                    if (/*!b.equals(myself) && */b.getLocation().indexOf("glassfish-embedded") != -1) {
                        try {
                            b.uninstall();
                            uninstalled.add(b);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

/*
                // Make sure the OSGI cache is cleaned up.

                // PackageAdmin throws NoClassDefFoundException possibly
                // because the felix classes are bundled in uber.jar -- check with Sahoo.

                // Workaround : type 'refresh' in the gogo shell after uninstalling.
                ServiceReference ref =
                        context.getServiceReference(PackageAdmin.class.getName());
                PackageAdmin pa = ref == null ? null : (PackageAdmin) context.getService(ref);
                logger.info("ref = " + ref + ", pa  = " + pa);
                if(pa != null) {
                    pa.refreshPackages(uninstalled.toArray(new Bundle[0]));
                }

*/


/*
                try {
                    if (myself != null) {
                        myself.uninstall();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
*/
                logger.info("Finished uninstalling all GlassFish bundles");
            }
        }
    }

}
