/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.webapi;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class TestServlet
  extends HttpServlet
{
  public String msg_listener = null;

  public void init(ServletConfig config)
    throws ServletException
  {
    super.init(config);

    this.msg_listener = TestListener.msg;
  }

  protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    throws ServletException, IOException
  {
    System.out.println("Servlet TestServlet doGet called");

    String msg_servlet = null;
    try
    {
        Class c = Class.forName("org.glassfish.tests.embedded.web.TestCacaoList");
      msg_servlet = "Class TestCacaoList loaded successfully from servlet";
      System.out.println(msg_servlet);
    }
    catch (Exception ex)
    {
      msg_servlet = "Exception while loading class TestCacaoList from servlet : " + ex.toString();
      System.out.println(msg_servlet);
    }
    PrintWriter out = httpServletResponse.getWriter();
    out.println("CACAO Glash Fish Test Servlet with logs !!");
    out.println("Context Init Msg = " + this.msg_listener);
    out.println("Servlet Msg = " + msg_servlet);
    out.flush();
    out.close();

    System.out.println("Servlet TestServlet doGet DONE");
  }
}
