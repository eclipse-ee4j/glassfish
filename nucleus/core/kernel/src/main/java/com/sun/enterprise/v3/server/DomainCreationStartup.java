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

package com.sun.enterprise.v3.server;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;

import jakarta.inject.Inject;

import java.util.logging.Logger;

import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.event.Events;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.api.admin.ServerEnvironment.Status.stopped;
import static org.glassfish.api.event.EventTypes.SERVER_SHUTDOWN;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Sep 18, 2009
 * Time: 10:46:34 PM
 * To change this template use File | Settings | File Templates.
 */
@Service(name = "DomainCreation")
public class DomainCreationStartup implements ModuleStartup {

    @Inject
    Events events;

    @Inject
    ServerEnvironmentImpl serverEnvironment;

    @Override
    public void setStartupContext(StartupContext startupContext) {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        try {
            serverEnvironment.setStatus(stopped);
            events.send(new Event<>(SERVER_SHUTDOWN), false);
        } catch (Exception ex) {
            Logger.getAnonymousLogger().warning(ex.getMessage());
        }
    }
}
