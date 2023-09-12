/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.grizzly.samples.websockets;

import org.glassfish.grizzly.websockets.WebSocketEngine;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import java.util.logging.Logger;

public class WebSocketsServlet extends HttpServlet {
    static final Logger logger = Logger.getLogger(WebSocketsServlet.class.getName());
    private final ChatApplication app = new ChatApplication();
    @Override
    public void init(ServletConfig config) throws ServletException {
        WebSocketEngine.getEngine().register(new ChatApplication());
    }
}
