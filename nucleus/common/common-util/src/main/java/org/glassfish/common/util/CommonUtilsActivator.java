/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.common.util;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * This activator is responsible for setting {@link OSGiObjectInputOutputStreamFactoryImpl}
 * as the factory in {@link ObjectInputOutputStreamFactoryFactory}
 *
 * @see ObjectInputOutputStreamFactoryFactory#setFactory
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class CommonUtilsActivator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        ObjectInputOutputStreamFactoryFactory.setFactory(new OSGiObjectInputOutputStreamFactoryImpl(context));
    }


    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
