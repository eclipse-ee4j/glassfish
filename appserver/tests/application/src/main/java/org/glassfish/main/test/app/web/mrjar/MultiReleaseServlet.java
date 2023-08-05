/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.web.mrjar;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class MultiReleaseServlet extends HttpServlet {

    private final Class<?> versionClass;

    public MultiReleaseServlet(Class<?> versionClass) {
        this.versionClass = versionClass;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Version version;
        try {
            version = (Version) versionClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new ServletException(e);
        }
        resp.setContentType("text/plain");
        try (PrintWriter out = resp.getWriter()) {
            out.println(version.getVersion());
        }
    }
}
