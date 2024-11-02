/*
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

package com.sun.enterprise.glassfish.bootstrap.osgi;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.osgi.framework.BundleContext;
import org.osgi.service.startlevel.StartLevel;

/**
 * A {@link GlassFishDecorator} which takes care of setting final start level.
 * This is done so as to avoid any impact on startup time by optional services.
 *
 * This object is used by {@link OSGiGlassFishRuntime}.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
class OSGiGlassFishImpl extends GlassFishDecorator {
    private final int finalStartLevel;
    private final BundleContext bundleContext;

    OSGiGlassFishImpl(GlassFish decoratedGf, BundleContext bundleContext, int finalStartLevel)
            throws GlassFishException {
        super(decoratedGf);
        this.bundleContext = bundleContext;
        this.finalStartLevel = finalStartLevel;
    }

    @Override
    public void start() throws GlassFishException {
        super.start();
        StartLevel sl = StartLevel.class.cast(bundleContext.getService(
                bundleContext.getServiceReference(StartLevel.class.getName())));
        sl.setStartLevel(finalStartLevel);
    }
}
