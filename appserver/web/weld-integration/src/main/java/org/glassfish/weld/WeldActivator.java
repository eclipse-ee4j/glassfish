/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld;

import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.bootstrap.api.helpers.TCCLSingletonProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * This is a bundle activator which is responsible for configuring Weld bundle to be used in GlassFish.
 *
 * As part of configuration, it configures the the SingletonProvider in Weld.
 * It sets different SingletonProvider for different profiles. e.g., in WebProfile,
 * it sets {@link org.jboss.weld.bootstrap.api.helpers.TCCLSingletonProvider}, where
 * as for full-jakartaee profile, it uses {@link org.glassfish.weld.ACLSingletonProvider}.
 * It tests profile by testing existence of
 * {@link org.glassfish.javaee.full.deployment.EarClassLoader}.
 * <p>
 * Since Weld 1.1, an implementation of the {@link org.jboss.weld.serialization.spi.ProxyServices}
 * SPI is used to provide a classloader to load javassist defined proxies.
 * This classloader ensures that they can load not only application defined classes but also classes
 * exported by any OSGi bundle as long as the operation is happening in the context of a Jakarta EE app.
 * <p>
 * The bundle activator resets the SingletonProvicer in stop().
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class WeldActivator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        ACLSingletonProvider.initializeSingletonProvider();
    }


    @Override
    public void stop(BundleContext context) throws Exception {
        SingletonProvider.reset();
    }
}
