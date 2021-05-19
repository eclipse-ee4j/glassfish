/*
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

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.glassfish.grizzly.comet.CometContext;
import org.glassfish.grizzly.comet.CometEngine;
import org.glassfish.grizzly.comet.CometEvent;
import org.glassfish.grizzly.comet.CometHandler;
import org.glassfish.grizzly.comet.DefaultCometHandler;

public class CometEchoServlet extends HttpServlet {
    private String contextPath;

    public class ChatListnerHandler extends DefaultCometHandler<PrintWriter> {

        private PrintWriter writer;

        public void attach(PrintWriter writer) {
            this.writer = writer;
        }

        public void onEvent(CometEvent event) throws IOException {
            if (event.getType() == CometEvent.Type.NOTIFY) {
                String output = (String) event.attachment();

                writer.println(output);
                writer.flush();
            }
        }

        public void onInitialize(CometEvent event) throws IOException {
        }

        public void onInterrupt(CometEvent event) throws IOException {
            removeThisFromContext();
        }

        public void onTerminate(CometEvent event) throws IOException {
            removeThisFromContext();
        }

        private void removeThisFromContext() {
            writer.close();

            CometContext context = CometEngine.getEngine().getCometContext(contextPath);
            context.removeCometHandler(this);
        }
    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        contextPath = config.getServletContext().getContextPath() + "/echo";

        CometContext context = CometEngine.getEngine().register(contextPath);
        context.setExpirationDelay(5 * 60 * 1000);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        res.setHeader("Cache-Control", "private");
        res.setHeader("Pragma", "no-cache");

        PrintWriter writer = res.getWriter();
        ChatListnerHandler handler = new ChatListnerHandler();
        handler.attach(writer);

        CometContext context = CometEngine.getEngine().register(contextPath);
        context.addCometHandler(handler);
        writer.println("OK");
        writer.flush();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        res.setHeader("Cache-Control", "private");
        res.setHeader("Pragma", "no-cache");

        req.setCharacterEncoding("UTF-8");
        String message = req.getParameter("msg");

        CometContext context = CometEngine.getEngine().register(contextPath);
        context.notify(message);
        PrintWriter writer = res.getWriter();
        writer.println("OK");
        writer.flush();
    }
}
