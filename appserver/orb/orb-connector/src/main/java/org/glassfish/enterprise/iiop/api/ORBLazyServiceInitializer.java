/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.enterprise.iiop.api;

import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.nio.channels.SelectableChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.internal.grizzly.LazyServiceInitializer;
import org.jvnet.hk2.annotations.Service;

import static com.sun.logging.LogDomains.SERVER_LOGGER;

/**
 * @author Ken Saks
 */
@Service
@Named("iiop-service")
public class ORBLazyServiceInitializer implements LazyServiceInitializer {
    private static final Logger LOG = LogDomains.getLogger(ORBLazyServiceInitializer.class, SERVER_LOGGER, false);

    @Inject
    private GlassFishORBLocator orbLocator;

    @Inject
    private OrbInitializationNode evil;

    private boolean initializedSuccessfully;

    public String getServiceName() {
        return "iiop-service";
    }

    @Override
    public boolean initializeService() {
        try {
            orbLocator.getORB();
            initializedSuccessfully = orbLocator.isORBInitialized();

            // TODO add check to ensure that lazy init is enabled for the orb
            // and throw exception if not

        } catch(Exception e) {
            LOG.log(Level.WARNING, "ORB initialization failed in lazy init", e);
        }
        return initializedSuccessfully;
    }

    @Override
    public void handleRequest(SelectableChannel channel) {
        if (initializedSuccessfully) {
            evil.handleRequest(channel);
        } else {
            LOG.log(Level.WARNING, "Cannot handle SelectableChannel request in ORBLazyServiceInitializer. "
                + "ORB did not initialize successfully");
        }
    }
}
