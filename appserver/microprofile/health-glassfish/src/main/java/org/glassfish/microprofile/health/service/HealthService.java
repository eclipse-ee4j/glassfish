/*
 * Copyright (c) 2024 Contributors to Eclipse Foundation.
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
package org.glassfish.microprofile.health.service;

import jakarta.inject.Inject;

import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.microprofile.health.HealthReporter;
import org.jvnet.hk2.annotations.Service;

@Service(name = "healthcheck-service")
@RunLevel(StartupRunLevel.VAL)
public class HealthService implements EventListener, PostConstruct {

    @Inject
    Events events;

    @Override
    public void postConstruct() {
        events.register(this);
    }

    @Override
    public void event(Event<?> event) {

        HealthReporter service = Globals.getDefaultHabitat().getService(HealthReporter.class);

        if (service == null) {
            return;
        }

        if (event.is(Deployment.APPLICATION_UNLOADED) && event.hook() instanceof ApplicationInfo appInfo) {
            service.removeAllHealthChecksFrom(appInfo.getName());
        }
    }
}
