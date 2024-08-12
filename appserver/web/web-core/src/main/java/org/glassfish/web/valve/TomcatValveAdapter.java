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

import org.apache.catalina.CometEvent;
import org.apache.catalina.Valve;

/**
 * Tomcat-style wrapper valve around GlassFish-style valve.
 *
 * This allows Tomcat- and GlassFish-style valves to be added to a
 * pipeline in arbitrary order.
 *
 * @author jluehe
 */
public class TomcatValveAdapter implements Valve {

    // The next valve in the pipeline to be invoked
    private Valve next = null;

    // The wrapped GlassFish-style valve to which to delegate
    private GlassFishValve gfValve;

    /**
     * Constructor.
     *
     * @param gfValve The GlassFish-style valve to wrap
     */
    public TomcatValveAdapter(GlassFishValve gfValve) {
        this.gfValve = gfValve;
    }

    public String getInfo() {
        return gfValve.getInfo();
    }

    public Valve getNext() {
        return next;
    }

    public void setNext(Valve valve) {
        this.next = valve;
    }

    public void backgroundProcess() {
        // Deliberate no-op
    }

    /**
     * Delegates to the invoke() and postInvoke() methods of the wrapped
     * GlassFish-style valve.
     */
    public void invoke(org.apache.catalina.connector.Request request,
                       org.apache.catalina.connector.Response response)
            throws IOException, ServletException {
        int rc = gfValve.invoke(request, response);
        if (rc != GlassFishValve.INVOKE_NEXT) {
            return;
        }
        getNext().invoke(request, response);
        gfValve.postInvoke(request, response);
    }

    public void event(org.apache.catalina.connector.Request request,
                      org.apache.catalina.connector.Response response,
                      CometEvent event)
            throws IOException, ServletException {
        // Deliberate no-op
    }

}
