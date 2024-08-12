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

package com.sun.enterprise.web;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Wrapper;
import org.glassfish.web.LogFacade;
import org.glassfish.web.valve.GlassFishValve;

/**
 * Implementation of StandardContextValve which is added as the base valve
 * to a web module's ad-hoc pipeline.
 *
 * A web module's ad-hoc pipeline is invoked for any of the web module's
 * ad-hoc paths.
 *
 * The AdHocContextValve is responsible for invoking the ad-hoc servlet
 * associated with the ad-hoc path.
 *
 * @author Jan Luehe
 */
public class AdHocContextValve implements GlassFishValve {

    private static final Logger logger = LogFacade.getLogger();

    private static final ResourceBundle rb = logger.getResourceBundle();

    private static final String VALVE_INFO =
        "com.sun.enterprise.web.AdHocContextValve";

    // The web module with which this valve is associated
    private WebModule context;


    /**
     * Constructor.
     */
    public AdHocContextValve(WebModule context) {
        this.context = context;
    }


    /**
     * Returns descriptive information about this valve.
     */
    public String getInfo() {
        return VALVE_INFO;
    }


    /**
     * Processes the given request by passing it to the ad-hoc servlet
     * associated with the request path (which has been determined, by the
     * associated web module, to be an ad-hoc path).
     *
     * @param request The request to process
     * @param response The response to return
     */
    public int invoke(Request request, Response response)
            throws IOException, ServletException {

        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        HttpServletResponse hres = (HttpServletResponse) response.getResponse();

        String adHocServletName =
            context.getAdHocServletName(hreq.getServletPath());

        Wrapper adHocWrapper = (Wrapper) context.findChild(adHocServletName);
        if (adHocWrapper != null) {
            Servlet adHocServlet = null;
            try {
                adHocServlet = adHocWrapper.allocate();
                adHocServlet.service(hreq, hres);
            } catch (Throwable t) {
                hres.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                String msg = rb.getString(LogFacade.ADHOC_SERVLET_SERVICE_ERROR);
                msg = MessageFormat.format(
                            msg,
                            new Object[] { hreq.getServletPath() });
                response.setDetailMessage(msg);
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, msg, t);
                }
                return END_PIPELINE;
            } finally {
                if (adHocServlet != null) {
                    adHocWrapper.deallocate(adHocServlet);
                }
            }
        } else {
            hres.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String msg = rb.getString(LogFacade.NO_ADHOC_SERVLET);
            msg = MessageFormat.format(
                            msg,
                            new Object[] { hreq.getServletPath() });
            response.setDetailMessage(msg);
            return END_PIPELINE;
        }

        return END_PIPELINE;
    }


    public void postInvoke(Request request, Response response)
            throws IOException, ServletException {
        // Do nothing
    }

}

