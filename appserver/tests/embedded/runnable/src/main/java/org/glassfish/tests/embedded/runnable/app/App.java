/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.tests.embedded.runnable.app;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.core.Application;

import java.util.logging.Logger;

import org.glassfish.tests.embedded.runnable.library.MockExecutorService;

/**
 *
 * @author Ondro Mihalyi
 */
@ApplicationScoped
public class App extends Application {

    private static final Logger LOG = Logger.getLogger(App.class.getName());

    public void init(@Observes @Initialized(ApplicationScoped.class) Object event) {
        new MockExecutorService().submit(() -> {
            LOG.info("App initialized");
        });
    }
}
