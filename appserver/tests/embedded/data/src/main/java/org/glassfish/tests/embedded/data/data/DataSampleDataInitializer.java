/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.glassfish.tests.embedded.data.data;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.tests.embedded.data.domain.Todo;

/**
 * Seeds sample todos on application startup.
 */
@ApplicationScoped
@Transactional
public class DataSampleDataInitializer {
    private static final Logger LOG = Logger.getLogger(DataSampleDataInitializer.class.getName());

    @Inject
    DataTodoRepository todoRepository;

    public void init(@Observes Startup event) {
        LOG.log(Level.INFO, "initializing sample data: {0}", event);
        todoRepository.saveAll(List.of(
                Todo.of("Say Hello to Jakarta EE 11"),
                Todo.of("Upgrade to Jakarta EE 11")
        ));
        LOG.log(Level.INFO, "initializing sample data is done: {0}", event);
    }
}
