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

package org.glassfish.tests.embedded.scatteredarchive;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;

import java.util.logging.Logger;

import javax.transaction.xa.XAResource;

/**
 * @author bhavanishankar@java.net
 */

public class ScatteredArchiveTestRA implements ResourceAdapter {

    private static final Logger logger = Logger.getAnonymousLogger();

    public void start(BootstrapContext bootstrapContext)
            throws ResourceAdapterInternalException {
        logger.info("ScatteredArchiveTestRA start has been called");
    }

    public void stop() {
        logger.info("ScatteredArchiveTestRA stop has been called");
    }

    public void endpointActivation(MessageEndpointFactory messageEndpointFactory,
                                   ActivationSpec activationSpec) throws ResourceException {
        logger.info("ScatteredArchiveTestRA endpointActivation has been called");
    }

    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory,
                                     ActivationSpec activationSpec) {
        logger.info("ScatteredArchiveTestRA endpointDeactivation has been called");
    }

    public XAResource[] getXAResources(ActivationSpec[] activationSpecs)
            throws ResourceException {
        logger.info("ScatteredArchiveTestRA getXAResources has been called");
        return new XAResource[0];
    }
}
