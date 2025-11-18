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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@WebServlet("/lock")
public class LockServlet extends HttpServlet {

    private static final ConcurrentHashMap<String, AtomicBoolean> LOCKS = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String idLock = req.getParameter("idLock");
        if ("lock".equals(action)) {
            AtomicBoolean lock = new AtomicBoolean(true);
            LOCKS.put(idLock, lock);
            while (lock.get() && !Thread.currentThread().isInterrupted()) {
                Thread.onSpinWait();
            }
            sendResponse("Unlocked " + idLock + ". Still locked around " + LOCKS.size() + " requests.", resp);
        } else if ("unlock".equals(action)) {
            AtomicBoolean lock = LOCKS.remove(idLock);
            if (lock == null) {
                throw new ServletException("Unknown lock: " + lock);
            }
            // Release another thread trapped in the loop
            lock.set(false);
            sendResponse("Unlocking " + idLock + ".", resp);
        } else if ("count".equals(action)) {
            sendResponse(LOCKS.size(), resp);
        } else {
            throw new ServletException("Unknown action: " + action);
        }
    }


    private void sendResponse(String message, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        try (PrintWriter writer = resp.getWriter()) {
            writer.println(message);
            writer.println("Thread: " + Thread.currentThread().getName());
        }
    }

    private void sendResponse(int number, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        try (PrintWriter writer = resp.getWriter()) {
            writer.println(number);
        }
    }
}
