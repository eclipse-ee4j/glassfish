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

import java.io.IOException;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(asyncSupported = true, name="fa5raP", value = "/DispatchServlet", loadOnStartup = 1)
public class DispatchServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
          request.getServletContext().log(this.getClass().getSimpleName() + " "
                                          + request.getHttpServletMapping());
          HttpServletMapping forwardMapping = (HttpServletMapping) request.getAttribute(RequestDispatcher.FORWARD_MAPPING);
          request.getServletContext().log(this.getClass().getSimpleName() + " FORWARD_MAPPING attribute: "
                                          + forwardMapping);
          HttpServletMapping includeMapping = (HttpServletMapping) request.getAttribute(RequestDispatcher.INCLUDE_MAPPING);
          request.getServletContext().log(this.getClass().getSimpleName() + " INCLUDE_MAPPING attribute: "
                                          + includeMapping);
          HttpServletMapping asyncMapping = (HttpServletMapping) request.getAttribute(AsyncContext.ASYNC_MAPPING);
          request.getServletContext().log(this.getClass().getSimpleName() + " ASYNC_MAPPING attribute: "
                                          + asyncMapping);

    AsyncContext asyncContext = request.startAsync();
    asyncContext.setTimeout(0);
    asyncContext.dispatch("/ServletC");
  }
}
