/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Service;

/**
 * Convenience implementation that delegate to a provided system executor. This provider will be looked up from the
 * habitat by its type ClusterExecutor and the "target" name.
 */
@Service
public final class TargetBasedExecutor implements ClusterExecutor {

    @Inject
    @Named("GlassFishClusterExecutor")
    private ClusterExecutor delegate = null;

    @Override
    public ActionReport.ExitCode execute(String commandName, AdminCommand command, AdminCommandContext context, ParameterMap parameters) {
        return delegate.execute(commandName, command, context, parameters);
    }
}
