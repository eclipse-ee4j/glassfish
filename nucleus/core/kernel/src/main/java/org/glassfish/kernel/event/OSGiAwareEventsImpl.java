/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.kernel.event;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

import static org.osgi.framework.FrameworkEvent.PACKAGES_REFRESHED;
import static org.osgi.framework.FrameworkUtil.getBundle;

/**
 * OSGi-aware implementation of the events dispatching facility.
 */
public class OSGiAwareEventsImpl extends EventsImpl implements FrameworkListener {

    @PostConstruct
    public void addFrameworkListener() {
        BundleContext bundleContext = getBundleContext();
        if (bundleContext != null) {
            bundleContext.addFrameworkListener(this);
        }
    }

    @PreDestroy
    public void removeFrameworkListener() {
        BundleContext bundleContext = getBundleContext();
        if (bundleContext != null) {
            bundleContext.removeFrameworkListener(this);
        }
    }

    @Override
    public void frameworkEvent(FrameworkEvent frameworkEvent) {
        if (frameworkEvent.getType() == PACKAGES_REFRESHED) {

            // Get System Bundle context
            BundleContext bundleContext = frameworkEvent.getBundle().getBundleContext();

            // Uninstalled bundles has been removed when framework has been refreshed.
            // Remove listeners registered by this uninstalled bundles.
            listenersBySequence.keySet().removeIf(listener -> {

                // Get bundle for event listener
                Bundle bundle = getBundle(listener.unwrap().getClass());
                if (bundle == null) {
                    return true;
                }

                // Even if Bundle has been removed, it must preserve bundle_id.
                return bundleContext.getBundle(bundle.getBundleId()) == null;
            });
        }
    }

    private BundleContext getBundleContext() {
        Bundle bundle = getBundle(getClass());
        if (bundle == null) {
            return null;
        }

        return bundle.getBundleContext();
    }
}
