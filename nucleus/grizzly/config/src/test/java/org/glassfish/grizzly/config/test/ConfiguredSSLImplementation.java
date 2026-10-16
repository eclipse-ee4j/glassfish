/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.grizzly.config.test;

import java.util.concurrent.atomic.AtomicInteger;

import org.glassfish.grizzly.config.ssl.SSLContextFactory;
import org.glassfish.grizzly.config.ssl.SSLImplementation;

/**
 * Implementation used just by tests to verify that the {@code classname} attribute of the
 * {@code ssl} element is honored. It is intentionally not a HK2 service, so it can be found only
 * through the configured class name.
 */
public class ConfiguredSSLImplementation implements SSLImplementation {

    private static final AtomicInteger USAGE_COUNTER = new AtomicInteger();

    public ConfiguredSSLImplementation() {
        USAGE_COUNTER.incrementAndGet();
    }

    @Override
    public SSLContextFactory getSSLContextFactory() {
        return new SSLContextFactory();
    }

    public static int getUsageCount() {
        return USAGE_COUNTER.get();
    }

    public static void resetUsageCount() {
        USAGE_COUNTER.set(0);
    }
}
