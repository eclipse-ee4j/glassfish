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

package org.glassfish.main.test.app.mrjar.servlet;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.annotation.HandlesTypes;

import java.util.Set;

import org.glassfish.main.test.app.mrjar.Version;

@HandlesTypes({Version.class})
public class MultiReleaseServletContainerInitializer implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
        if (classes == null) {
            throw new ServletException();
        }

        Class<?> versionClass = null;
        for (Class<?> c : classes) {
            if (Version.class.isAssignableFrom(c)) {
                versionClass = c;
                break;
            }
        }

        if (versionClass == null) {
            throw new ServletException();
        }

        ServletRegistration.Dynamic servletRegistration =
                servletContext.addServlet("Multi-Release Servlet", new MultiReleaseServlet(versionClass));
        servletRegistration.addMapping("/");
    }
}
