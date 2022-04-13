/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.osgi.cli.remote.impl;

import org.glassfish.api.ActionReport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Detects OSGI shell impl available on the classpath and provides one of them.
 *
 * @author David Matejcek
 */
public class OsgiShellServiceProvider {


    /**
     * Effectively masks classes as used dependencies are optional and might not be on the classpath
     *
     * @param ctx - used to detect and get the service impl
     * @param sessionId - used to attach to an existing session with this id.
     * @param sessionOperation - used to start/attach/stop
     * @param report
     * @return {@link OsgiShellService} or null if there is no shell impl available.
     */
    public static OsgiShellService detectService(final BundleContext ctx, final SessionOperation sessionOperation,
        final String sessionId, final ActionReport report) {
        // warning: don't replace strings by classes, classes might not be on the classpath.
        final ServiceReference<?> processor = ctx
            .getServiceReference("org.apache.felix.service.command.CommandProcessor");
        if (processor != null) {
            return new GogoOsgiShellService(ctx.getService(processor), sessionOperation, sessionId, report);
        }
        final ServiceReference<?> shell = ctx.getServiceReference("org.apache.felix.shell.ShellService");
        if (shell != null) {
            return new FelixOsgiShellService(ctx.getService(shell), report);
        }
        return null;
    }
}
