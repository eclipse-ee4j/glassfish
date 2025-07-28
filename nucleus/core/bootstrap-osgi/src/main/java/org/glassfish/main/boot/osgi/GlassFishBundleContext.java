/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;

/**
 * Wrapper for {@link BundleContext}, {@link StartLevel} service and {@link PackageAdmin}.
 */
// See discussion: https://www.eclipse.org/forums/index.php/t/205719/
// "Note that PackageAdmin is deprecated, but will not be removed any time soon in Equinox.
// There is no replacement method in wiring because this method is a convenience method
// and it was decided that it should not be moved to wiring."
// However that doesn't mean that it will be available forever, so I pushed it to this special class.
@SuppressWarnings("deprecation")
class GlassFishBundleContext {

    private final BundleContext bundleContext;
    private final StartLevel startLevelService;
    private final PackageAdmin packageAdmin;


    /**
     * @param bundleContext
     */
    GlassFishBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.startLevelService = bundleContext.getService(bundleContext.getServiceReference(StartLevel.class));
        this.packageAdmin = bundleContext.getService(bundleContext.getServiceReference(PackageAdmin.class));
    }

    /**
     * @return original {@link BundleContext}
     */
    BundleContext unwrap() {
        return this.bundleContext;
    }


    void setInitialBundleStartLevel(int bundleStartLevel) {
        startLevelService.setInitialBundleStartLevel(bundleStartLevel);
    }


    /**
     * @param bundle
     * @return true if the bundle is a fragment bundle.
     */
    boolean isFragment(Bundle bundle) {
        return packageAdmin.getBundleType(bundle) == PackageAdmin.BUNDLE_TYPE_FRAGMENT;
    }


    /**
     * Refresh packages
     */
    void refresh() {
        // null to refresh any bundle that's obsolete
        packageAdmin.refreshPackages(null);
        bundleContext.ungetService(bundleContext.getServiceReference(PackageAdmin.class));
    }
}
