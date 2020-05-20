/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package valves;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.valves.ValveBase;

/**
 * Custom GlassFish-style valve compiled against the "old"
 * org.apache.catalina.Valve interface (from GlassFish releases prior to V3),
 * which has been renamed to org.glassfish.web.valve.GlassFishValve in V3.
 */
public class GlassFishValveImpl extends ValveBase {

    public String getInfo() {
        return getClass().getName();
    }

    public int invoke(Request request, Response response)
            throws IOException, ServletException {

        ServletRequest serReq = request.getRequest();
        serReq.setAttribute("ATTR", "VALUE");

        return 1;
    }

    public void postInvoke(Request request, Response response)
            throws IOException, ServletException {
        // Deliberate no-op
    }
}

