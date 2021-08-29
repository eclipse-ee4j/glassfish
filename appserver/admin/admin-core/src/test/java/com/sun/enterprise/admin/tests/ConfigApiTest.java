/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.tests;

import org.glassfish.config.support.GlassFishDocument;
import org.glassfish.hk2.api.ServiceLocator;
import java.util.concurrent.Executors;

/**
 * User: Jerome Dochez
 * Date: Mar 25, 2008
 * Time: 12:38:30 PM
 */
public abstract class ConfigApiTest extends org.glassfish.tests.utils.ConfigApiTest {

    @Override
    public GlassFishDocument getDocument(ServiceLocator locator) {
        GlassFishDocument doc = locator.getService(GlassFishDocument.class);
        if (doc == null) {
            return new GlassFishDocument(locator, Executors.newCachedThreadPool(runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(true);
                return thread;
            }));
        }
        return doc;
    }
}
