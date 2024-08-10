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

package org.glassfish.web.valve;

import jakarta.servlet.ServletException;

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Valve;

/**
 * Adapter valve for wrapping a GlassFish-style valve that was compiled
 * against the "old" org.apache.catalina.Valve interface from GlassFish
 * releases prior to V3 (which has been renamed to
 * org.glassfish.web.valve.GlassFishValve in GlassFish V3).
 *
 * @author jluehe
 */
public class GlassFishValveAdapter implements GlassFishValve {

    // The wrapped GlassFish-style valve to which to delegate
    private Valve gfValve;

    private Method invokeMethod;
    private Method postInvokeMethod;

    /**
     * Constructor.
     *
     * @param gfValve The GlassFish valve to which to delegate
     */
    public GlassFishValveAdapter(Valve gfValve) throws Exception {
        this.gfValve = gfValve;
        invokeMethod = gfValve.getClass().getMethod("invoke", Request.class,
                                                    Response.class);
        postInvokeMethod = gfValve.getClass().getMethod("postInvoke",
                                                        Request.class,
                                                        Response.class);
    }

    public String getInfo() {
        return gfValve.getInfo();
    }

    /**
     * Delegates to the invoke() of the wrapped GlassFish-style valve.
     */
    public int invoke(Request request,
                      Response response)
                throws IOException, ServletException {
        try {
            return ((Integer) invokeMethod.invoke(gfValve, request, response)).intValue();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Delegates to the postInvoke() of the wrapped GlassFish-style valve.
     */
    public void postInvoke(Request request, Response response)
                throws IOException, ServletException {
        try {
            postInvokeMethod.invoke(gfValve, request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
