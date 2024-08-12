/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.glassfish.tests.embedded.scatteredarchive.contextInitialized;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import static java.lang.System.Logger.Level.INFO;

@WebListener
public class DispatcherListener implements ServletContextListener {

    private static final System.Logger logger = System.getLogger(DispatcherListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.log(INFO, "Listener initialized");
        ApplicationStatus.contextInitializedCounter++;
    }
}
