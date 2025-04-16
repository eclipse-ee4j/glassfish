/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.FINAL_START_LEVEL_PROP;

/**
 * This is a special implementation used in non-embedded environment.
 * It assumes that it has launched the framework during bootstrap and hence can stop it upon
 * shutdown.
 * It also creates a specialized {@link GlassFish} implementation called {@link OSGiGlassFishImpl}
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiGlassFishRuntime extends GlassFishRuntime {

    private final GlassFishRuntime glassfishRuntime;
    // cache the value, because we can't use bundleContext after this bundle is stopped.
    // system bundle is the framework
    private volatile Framework framework;

    public OSGiGlassFishRuntime(GlassFishRuntime glassfishRuntime, final Framework framework) {
        this.glassfishRuntime = glassfishRuntime;
        this.framework = framework;
    }

    @Override
    public void shutdown() throws GlassFishException {
        if (framework == null) {
            // already shutdown
            return;
        }
        try {
            glassfishRuntime.shutdown();
            framework.stop();
            framework.waitForStop(0);
        } catch (InterruptedException ex) {
            throw new GlassFishException(ex);
        } catch (BundleException ex) {
            throw new GlassFishException(ex);
        } finally {
            // guard against repeated calls.
            framework = null;
        }
    }

    @Override
    public GlassFish newGlassFish(GlassFishProperties glassfishProperties) throws GlassFishException {
        GlassFish glassfish = glassfishRuntime.newGlassFish(glassfishProperties);
        int finalStartLevel = Integer
            .parseInt(glassfishProperties.getProperties().getProperty(FINAL_START_LEVEL_PROP, "2"));
        return new OSGiGlassFishImpl(glassfish, framework.getBundleContext(), finalStartLevel);
    }
}
