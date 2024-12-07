/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;

/**
 * A decorator for GlassFish
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class GlassFishDecorator implements GlassFish {
    private final GlassFish decoratedGf;

    public GlassFishDecorator(GlassFish decoratedGf) {
        this.decoratedGf = decoratedGf;
    }

    @Override
    public void start() throws GlassFishException {
        decoratedGf.start();
    }

    @Override
    public void stop() throws GlassFishException {
        decoratedGf.stop();
    }

    @Override
    public void dispose() throws GlassFishException {
        decoratedGf.dispose();
    }

    @Override
    public Status getStatus() throws GlassFishException {
        return decoratedGf.getStatus();
    }

    @Override
    public <T> T getService(Class<T> serviceType) throws GlassFishException {
        return decoratedGf.getService(serviceType);
    }

    @Override
    public <T> T getService(Class<T> serviceType, String serviceName) throws GlassFishException {
        return decoratedGf.getService(serviceType, serviceName);
    }

    @Override
    public Deployer getDeployer() throws GlassFishException {
        return decoratedGf.getDeployer();
    }

    @Override
    public CommandRunner getCommandRunner() throws GlassFishException {
        return decoratedGf.getCommandRunner();
    }
}
