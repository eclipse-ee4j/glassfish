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

import java.io.IOException;

import org.apache.catalina.Container;
import org.apache.catalina.Realm;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.core.StandardPipeline;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Pipeline whose invoke logic checks if a given request path represents an ad-hoc path: If so, this pipeline delegates
 * the request to the ad-hoc pipeline of its associated web module. Otherwise, this pipeline processes the request.
 */
public class WebPipeline extends StandardPipeline {

    private WebModule webModule;

    /**
     * creates an instance of WebPipeline
     *
     * @param container
     */
    public WebPipeline(Container container) {
        super(container);
        if (container instanceof WebModule) {
            this.webModule = (WebModule) container;
        }
    }

    /**
     * Processes the specified request, and produces the appropriate response, by invoking the first valve (if any) of this
     * pipeline, or the pipeline's basic valve.
     *
     * If the request path to process identifies an ad-hoc path, the web module's ad-hoc pipeline is invoked.
     *
     * @param request The request to process
     * @param response The response to return
     */
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request.getRequest();

        if (webModule != null && webModule.getAdHocServletName(httpServletRequest.getServletPath()) != null) {
            webModule.getAdHocPipeline().invoke(request, response);
        } else if (webModule != null) {
            final Realm realm = webModule.getRealm();

            if (realm != null && realm.isSecurityExtensionEnabled(httpServletRequest.getServletContext())) {
                super.doChainInvoke(request, response);
            } else {
                super.invoke(request, response);
            }
        }
    }

}
