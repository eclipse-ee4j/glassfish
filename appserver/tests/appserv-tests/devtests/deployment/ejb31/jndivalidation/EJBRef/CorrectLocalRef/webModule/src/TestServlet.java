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
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import test.ejb.Sless;

@WebServlet(name = "TestServlet", urlPatterns = {"/TestServlet"})
public class TestServlet extends HttpServlet {
    // Testing lookup using java:global and java:app JNDI names
    @EJB(lookup="java:global/deployment-ejb31-jndivalidation-EJBRef-CorrectLocalRef-app/deployment-ejb31-jndivalidation-EJBRef-CorrectLocalRef-ejb/SlessEJB")
    Sless ejb1;

    @EJB(lookup="java:app/deployment-ejb31-jndivalidation-EJBRef-CorrectLocalRef-ejb/SlessEJB")
    Sless ejb2;

    @EJB(lookup="java:global/deployment-ejb31-jndivalidation-EJBRef-CorrectLocalRef-app/deployment-ejb31-jndivalidation-EJBRef-CorrectLocalRef-ejb/SlessEJB!test.ejb.Sless")
    Sless ejb3;

    @EJB(lookup="java:app/deployment-ejb31-jndivalidation-EJBRef-CorrectLocalRef-ejb/SlessEJB!test.ejb.Sless")
    Sless ejb4;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ejb1.sayHello();
        ejb2.sayHello();
        ejb3.sayHello();
        ejb4.sayHello();
    }
}
