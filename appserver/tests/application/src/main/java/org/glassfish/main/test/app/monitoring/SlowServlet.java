/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.main.test.app.monitoring;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/slow")
public class SlowServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String delayParam = req.getParameter("delay");
        int delay = delayParam != null ? Integer.parseInt(delayParam) : 5000;
        try {
            Thread.sleep(delay); // delay to keep threads busy
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        resp.setContentType("text/plain");
        try (PrintWriter writer = resp.getWriter()) {
            writer.println("Slow response completed");
            writer.println("Thread: " + Thread.currentThread().getName());
        }
    }
}
