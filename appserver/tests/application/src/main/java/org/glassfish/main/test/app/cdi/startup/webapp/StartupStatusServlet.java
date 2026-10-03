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

package org.glassfish.main.test.app.cdi.startup.webapp;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.glassfish.main.test.app.cdi.startup.lib.LibraryStartupObserver;

import static org.glassfish.main.test.app.cdi.startup.lib.LibraryStartupObserver.DATA_SOURCE_NAME;

@DataSourceDefinition(
    name = DATA_SOURCE_NAME,
    className = "org.apache.derby.jdbc.ClientDataSource",
    serverName = "localhost",
    portNumber = 1527,
    databaseName = "sun-appserv-samples;create=true",
    user = "APP",
    password = "APP")
@WebServlet("/status")
public class StartupStatusServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private LibraryStartupObserver libraryObserver;

    @Inject
    private WebAppStartupObserver webAppObserver;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.getWriter().println("library=" + libraryObserver.getStatus());
        response.getWriter().println("webapp=" + webAppObserver.getStatus());
    }
}
