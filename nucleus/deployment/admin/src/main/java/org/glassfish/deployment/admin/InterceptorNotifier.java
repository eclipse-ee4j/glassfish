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

package org.glassfish.deployment.admin;

import java.util.Arrays;
import java.util.Collection;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.deployment.ApplicationLifecycleInterceptor;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;

/**
 *
 * @author Tim Quinn
 */
public class InterceptorNotifier {

    private boolean[] isBeforeReported = initialIsReported();
    private boolean[] isAfterReported = initialIsReported();

    private Collection<ApplicationLifecycleInterceptor> interceptors;
    private ExtendedDeploymentContext dc = null;

    private static boolean[] initialIsReported() {
        final boolean[] result = new boolean[ExtendedDeploymentContext.Phase.values().length];
        Arrays.fill(result, false);
        return result;
    }

    public InterceptorNotifier(final ServiceLocator habitat, final DeploymentContext basicDC) {
        if (basicDC != null) {
            if (! (basicDC instanceof ExtendedDeploymentContext)) {
                throw new IllegalArgumentException(basicDC.getClass().getName());
            }
            dc = ExtendedDeploymentContext.class.cast(basicDC);
        }
        interceptors = habitat.getAllServices(ApplicationLifecycleInterceptor.class);
    }

    synchronized void ensureBeforeReported(final ExtendedDeploymentContext.Phase phase) {
        if (isBeforeReported[phase.ordinal()]) {
            return;
        }
        for (ApplicationLifecycleInterceptor i : interceptors) {
            i.before(phase, dc);
        }
        isBeforeReported[phase.ordinal()] = true;
    }

    synchronized void ensureAfterReported(final ExtendedDeploymentContext.Phase phase) {
        if (isAfterReported[phase.ordinal()]) {
            return;
        }

        for (ApplicationLifecycleInterceptor i : interceptors) {
            i.after(phase, dc);
        }
        isAfterReported[phase.ordinal()] = true;
    }

    ExtendedDeploymentContext dc() {
        return dc;
    }
}
