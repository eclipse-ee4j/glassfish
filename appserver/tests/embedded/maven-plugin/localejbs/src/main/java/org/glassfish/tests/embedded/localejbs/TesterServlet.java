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

package org.glassfish.tests.embedded.localejbs;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * @author bhavanishankar@dev.java.net
 */
@WebServlet(name="TesterServlet", urlPatterns="/TesterServlet")
public class TesterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {
        System.out.println("TesterServlet invoked");
        Result res = JUnitCore.runClasses(LocalEjbTest.class);
        if(res.getFailureCount() == 0) {
            httpServletResponse.setStatus(200, "All tests passed");
        } else {
            httpServletResponse.sendError(500, "One or more tests failed");
        }
    }
}
