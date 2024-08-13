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

package org.glassfish.webservices.monitoring;

import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;

import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.webservices.LogUtils;

/**
 * Log all authentication successes and failures.
 *
 * @author Jerome Dochez
 */
public class LogAuthenticationListener implements AuthenticationListener {

    private static final Logger logger = LogUtils.getLogger();


    /** Creates a new instance of LogAuthenticationListener */
    public LogAuthenticationListener() {
    }

    /**
     * notification that a user properly authenticated while making
     * a web service invocation.
     */
    @Override
    public void authSucess(BundleDescriptor bundleDesc, Endpoint endpoint, Principal principal) {
        if (DOLUtils.ejbType().equals(bundleDesc.getModuleType())) {
            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, LogUtils.AUTHENTICATION_SUCCESS,
                        new Object[] {endpoint.getEndpointSelector(),
                            bundleDesc.getModuleID(), "ejb module"});
            }
        } else {
            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, LogUtils.AUTHENTICATION_SUCCESS,
                        new Object[] {endpoint.getEndpointSelector(),
                            bundleDesc.getModuleID(), "web app"});
            }
        }
    }

    /**
     * notification that a user authentication attempt has failed.
     * @param endpoint the endpoint selector
     * @param principal Optional principal that failed
     */
    @Override
    public void authFailure(BundleDescriptor bundleDesc, Endpoint endpoint, Principal principal) {
        if (DOLUtils.ejbType().equals(bundleDesc.getModuleType())) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, LogUtils.AUTHENTICATION_FAILURE,
                        new Object[] {endpoint.getEndpointSelector(),
                            bundleDesc.getModuleID(), "ejb module"});
            }
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, LogUtils.AUTHENTICATION_FAILURE,
                        new Object[] {endpoint.getEndpointSelector(),
                            bundleDesc.getModuleID(), "web app"});
            }
        }
    }
}
