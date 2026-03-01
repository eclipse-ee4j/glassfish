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
package org.glassfish.main.boot.osgi;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;

import static org.osgi.framework.FrameworkEvent.ERROR;

public final class FelixErrorCollector {

    public static final BlockingQueue<FrameworkEvent> ERRORS = new LinkedBlockingQueue<>();

    public static void install(BundleContext context) {
        context.addFrameworkListener(event -> {
            if (event.getType() == ERROR && event.getThrowable() != null) {
                ERRORS.offer(event);
            }
        });
    }
}