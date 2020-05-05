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

package test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns={"/asyncdispatch"}, asyncSupported=true)
public class AsyncDispatch extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        System.out.println("AD: dispatcher type: " + req.getDispatcherType());
        boolean withArgs = Boolean.parseBoolean(req.getParameter("withargs"));
        boolean forceAsync = Boolean.parseBoolean(req.getParameter("forceasync"));
        if (!req.getDispatcherType().equals(DispatcherType.ASYNC) ||
                forceAsync) {

            final AsyncContext ac =
                ((withArgs)? req.startAsync(req, res) : req.startAsync());

            ac.addListener(new AsyncListener() {
                public void onComplete(AsyncEvent event) {
                    System.out.println("AD: AsyncListener.onComplete");
                }

                public void onError(AsyncEvent event) {
                    System.out.println("AD: AsyncListener.onError");
                }

                public void onStartAsync(AsyncEvent event) {
                    System.out.println("AD: AsyncListener.onStartAsync");
                }

                public void onTimeout(AsyncEvent event) {
                    System.out.println("AD: AsyncListener.onTimeout");
                }
            });

            Timer timer = new Timer("AsyncTimer", true);
            timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        ac.dispatch();
                    }
                },
                3000);
        } else {
            PrintWriter writer = res.getWriter();
            writer.write("Hello from AsyncDispatch\n");
        }
    }
}
