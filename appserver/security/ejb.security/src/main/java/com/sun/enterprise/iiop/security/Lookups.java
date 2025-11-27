/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.iiop.security;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.ref.WeakReference;

import org.glassfish.gms.bootstrap.GMSAdapterService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;

/**
 * This class is a local utility class to provide for hk2 lookups during runtime.
 *
 * @author Sudarsan Sridhar
 */
public class Lookups {

    @Inject
    private Provider<SecurityMechanismSelector> securityMechanismSelectorProvider;

    @Inject
    private Provider<SecurityContextUtil> securityContextUtilProvider;

    @Inject
    private Provider<GMSAdapterService> gmsAdapterServiceProvider;

    /**
     * Static singleton {@link Habitat} instance.
     */
    private static final ServiceLocator SERVICE_LOCATOR = Globals.getDefaultHabitat();

    /**
     * Static singleton {@link Lookups} instance. Note that this is assigned lazily and may remain null if the
     * {@link Habitat} can not be obtained.
     */
    private static Lookups singleton;

    private static WeakReference<SecurityMechanismSelector> sms = new WeakReference<>(null);
    private static WeakReference<SecurityContextUtil> sc = new WeakReference<>(null);

    private Lookups() {
    }

    /**
     * Check to see if the singleton {@link Lookups} reference has been assigned. If null, then attempt to obtain and assign
     * the singleton {@link Lookups} instance.
     *
     * @return true if the singleton instance has been successfully assigned; false otherwise
     */
    private static synchronized boolean checkSingleton() {
        if (singleton == null && SERVICE_LOCATOR != null) {
            // Obtaining the singleton through the habitat will cause the injections to occur.
            singleton = SERVICE_LOCATOR.create(Lookups.class);
            SERVICE_LOCATOR.inject(singleton);
            SERVICE_LOCATOR.postConstruct(singleton);
        }
        return singleton != null;
    }

    /**
     * Get the {@link SecurityMechanismSelector}.
     *
     * @return the {@link SecurityMechanismSelector}; null if not available
     */
    static SecurityMechanismSelector getSecurityMechanismSelector() {
        if (sms.get() != null) {
            return sms.get();
        }
        return provideSecurityMechanismSelector();
    }

    private static synchronized SecurityMechanismSelector provideSecurityMechanismSelector() {
        if (sms.get() == null && checkSingleton()) {
            sms = new WeakReference<>(singleton.securityMechanismSelectorProvider.get());
        }
        return sms.get();
    }

    /**
     * Get the {@link SecurityContextUtil}.
     *
     * @return the {@link SecurityContextUtil}; null if not available
     */
    static SecurityContextUtil getSecurityContextUtil() {
        if (sc.get() != null) {
            return sc.get();
        }
        return provideSecurityContextUtil();
    }

    private static synchronized SecurityContextUtil provideSecurityContextUtil() {
        if (sc.get() == null && checkSingleton()) {
            sc = new WeakReference<>(singleton.securityContextUtilProvider.get());
        }
        return sc.get();
    }

    /**
     * Get the {@link GMSAdapterService}.
     *
     * @return the {@link GMSAdapterService}; null if not available
     */
    static GMSAdapterService getGMSAdapterService() {
        return checkSingleton() ? singleton.gmsAdapterServiceProvider.get() : null;
    }
}
