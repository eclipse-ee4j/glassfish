/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.web;

import java.io.IOException;
import javax.annotation.Resource;
import javax.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import test.ejb.Sless;

@WebServlet(name = "TestServlet", urlPatterns = {"/TestServlet"})
public class TestServlet extends HttpServlet {
    // Portable JNDI names
    // Testing mapped name using java:global and java:app JNDI names
    @EJB(mappedName="java:global/deployment-ejb31-jndivalidation-EJBRef-RemoteRef-app/deployment-ejb31-jndivalidation-EJBRef-RemoteRef-ejb/SlessEJB")
    Sless ejb1;

    @EJB(mappedName="java:app/deployment-ejb31-jndivalidation-EJBRef-RemoteRef-ejb/SlessEJB")
    Sless ejb2;

    @EJB(mappedName="java:global/deployment-ejb31-jndivalidation-EJBRef-RemoteRef-app/deployment-ejb31-jndivalidation-EJBRef-RemoteRef-ejb/SlessEJB!test.ejb.Sless")
    Sless ejb3;

    @EJB(mappedName="java:app/deployment-ejb31-jndivalidation-EJBRef-RemoteRef-ejb/SlessEJB!test.ejb.Sless")
    Sless ejb4;

    // Non-portable jndi names
    // This should work since we have only one remote business interface
    @EJB(lookup="sless_ejb")
    Sless ejb5;

    @EJB(lookup="sless_ejb#test.ejb.Sless")
    Sless ejb6;

    // Auto-linking
    @EJB
    Sless ejb7;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }
}
