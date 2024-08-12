/*
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

package org.glassfish.web.ha;

import com.sun.enterprise.web.PESSOFactory;
import com.sun.enterprise.web.PEWebContainerFeatureFactoryImpl;
import com.sun.enterprise.web.SSOFactory;
import com.sun.enterprise.web.ServerConfigLookup;

import jakarta.inject.Inject;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.web.ha.authenticator.HASSOFactory;
import org.jvnet.hk2.annotations.Service;

/**
 * Implementation of WebContainerFeatureFactory which returns web container
 * feature implementations for HA.
 *
 * @author Shing Wai Chan
 */
@Service(name="ha")
public class HAWebContainerFeatureFactoryImpl extends PEWebContainerFeatureFactoryImpl {
    @Inject
    private ServiceLocator services;

    @Inject
    private ServerConfigLookup serverConfigLookup;

    @Override
    public SSOFactory getSSOFactory() {
        if (isSsoFailoverEnabled()) {
            return services.getService(HASSOFactory.class);
        } else {
            return new PESSOFactory();
        }
    }

    /**
     * check sso-failover-enabled in web-container-availability
     * @return return true only if the value of sso-failover-enabled is "true"
     * and availability-enabled in web-container-availability is "true"
     * otherwise, return false.
     */
    private boolean isSsoFailoverEnabled() {
        boolean webContainerAvailabilityEnabled =
            serverConfigLookup.calculateWebAvailabilityEnabledFromConfig();
        boolean isSsoFailoverEnabled =
            serverConfigLookup.isSsoFailoverEnabledFromConfig();
        return isSsoFailoverEnabled && webContainerAvailabilityEnabled;
    }
}
