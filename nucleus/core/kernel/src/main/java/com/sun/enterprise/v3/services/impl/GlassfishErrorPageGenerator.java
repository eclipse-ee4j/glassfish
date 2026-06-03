/*
 * Copyright (c) 2023, 2026 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.services.impl;

import com.sun.appserv.server.util.Version;

import org.glassfish.grizzly.http.server.ErrorPageGenerator;
import org.glassfish.grizzly.http.server.Request;

/**
 * GlassFish default {@link ErrorPageGenerator}.
 */
public class GlassfishErrorPageGenerator implements ErrorPageGenerator {

    /**
     * @param request ignored
     * @param status response status: 200, 404, ...
     * @param reasonPhrase ignored
     * @param description ignored
     * @param exception ignored
     */
    @Override
    public String generate(final Request request, final int status, final String reasonPhrase, final String description,
        final Throwable exception) {
        String message = getMessage(status);
        return
        """
        <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
        <html xmlns="http://www.w3.org/1999/xhtml">
        <head><title>%1$s</title>
        <style type="text/css"><!--
        H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;}
        H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;}
        H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;}
        BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;}
        B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;}
        P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}
        A {color : black;}HR {color : #525D76;}
        --></style>
        </head><body>
        <h1>HTTP Status %2$s</h1>
        <hr/>
        <p><b>type:</b> Status report</p>
        <p><b>message:</b> %3$s</p>
        <hr/>
        <h3>%4$s</h3>
        </body></html>
        """.formatted(Version.getProductName(), status, message, Version.getProductId());
    }

    private static String getMessage(int status) {
        switch (status) {
            case 200:
                return "OK";
            case 404:
                return "The requested resource is not available.";
            default:
                return "The server encountered an internal error that prevented it from fulfilling this request.";
        }
    }
}
