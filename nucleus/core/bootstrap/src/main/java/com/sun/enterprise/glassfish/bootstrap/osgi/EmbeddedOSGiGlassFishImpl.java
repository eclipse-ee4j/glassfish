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

package com.sun.enterprise.glassfish.bootstrap.osgi;

import com.sun.enterprise.glassfish.bootstrap.GlassFishDecorator;
import com.sun.enterprise.glassfish.bootstrap.LogFacade;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * A specialized implementation of GlassFish which takes care of calling
 * registering & unregistering GlassFish service from service registry when GlassFish is started and stopped.
 *
 * This object is created by {@link EmbeddedOSGiGlassFishRuntime}
 *
 * @author sanjeeb.sahoo@oracle.com
 */
public class EmbeddedOSGiGlassFishImpl extends GlassFishDecorator {
    private final Logger logger = LogFacade.BOOTSTRAP_LOGGER;
    private ServiceRegistration reg;
    private final BundleContext bundleContext;

    public EmbeddedOSGiGlassFishImpl(GlassFish decoratedGf, BundleContext bundleContext) {
        super(decoratedGf);
        this.bundleContext = bundleContext;
    }

    @Override
    public void start() throws GlassFishException {
        super.start();
        registerService();
    }

    @Override
    public void stop() throws GlassFishException {
        unregisterService();
        super.stop();
    }

    private void registerService() {
        reg = getBundleContext().registerService(GlassFish.class.getName(), this, null);
        logger.log(Level.CONFIG, LogFacade.SERVICE_REGISTERED, new Object[]{this, reg});
    }

    private void unregisterService() {
        if (getBundleContext() != null) { // bundle is still active
            try {
                if (reg != null) {
                    reg.unregister();
                }
                logger.log(Level.CONFIG, LogFacade.SERVICE_UNREGISTERED, this);
            } catch (IllegalStateException e) {
                LogFacade.log(logger, Level.WARNING, LogFacade.SERVICE_UNREGISTRATION_EXCEPTION, e, e);
            }
        }
    }

    private BundleContext getBundleContext() {
        return bundleContext;
    }
}
