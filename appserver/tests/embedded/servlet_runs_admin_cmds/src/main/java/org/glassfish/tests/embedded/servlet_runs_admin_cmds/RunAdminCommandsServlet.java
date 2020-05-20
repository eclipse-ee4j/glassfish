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

package org.glassfish.tests.embedded.servlet_runs_admin_cmds;

import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author bhavanishankar@dev.java.net
 */
@WebServlet(name = "RunAdminCommandsServlet",
        urlPatterns = "/RunAdminCommandsServlet")
public class RunAdminCommandsServlet extends HttpServlet {

    @Resource(mappedName = "org.glassfish.embeddable.CommandRunner")
    CommandRunner cr;

    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {
        PrintWriter out = httpServletResponse.getWriter();
        out.println("Inside RunAdminCommandsServlet...");
        out.println("CommandRunner = " + cr);
        if (cr != null) {
            CommandResult result = cr.run("create-jdbc-connection-pool",
                    "--datasourceclassname=org.apache.derby.jdbc.ClientDataSource",
                    "--restype=javax.sql.XADataSource",
                    "--property=portNumber=1527:password=APP:user=APP:serverName=localhost:databaseName=sun-appserv-samples:connectionAttributes=create\\=true",
                    "sample_derby_pool");
            out.println("Ran create-jdbc-connection-pool command. Output = [ " +
                    result.getOutput() + "]");
            result = cr.run("version");
            out.println("Ran version command. Output = [" + result.getOutput() + "]");
        }
        out.flush();
        out.close();
    }
}

