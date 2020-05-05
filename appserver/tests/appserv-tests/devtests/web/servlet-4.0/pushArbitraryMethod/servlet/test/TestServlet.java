/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test;

import java.io.IOException;
import java.util.Arrays;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.HttpMethodConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.PushBuilder;
import static jakarta.servlet.annotation.ServletSecurity.TransportGuarantee.CONFIDENTIAL;

@WebServlet(urlPatterns="/test")
@ServletSecurity(httpMethodConstraints={
        @HttpMethodConstraint(value="GET", transportGuarantee=CONFIDENTIAL) })
public class TestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
            /*
             * To test this in an HTTP/2 capable browser, visit
             * https://localhost:8181/web-servlet-4.0-push-arbitrary-method/test
             * and note that a push promise frame similar to the following
             * has been pushed by the server.

             * HTTP2_SESSION_RECV_PUSH_PROMISE
             * :method: FOO
             * :scheme: https
             * :authority: localhost:8181
             * :path: /web-servlet-4.0-push-arbitrary-method/foo/my.css
             * host: localhost:8181
             * cache-control: max-age=0
             * upgrade-insecure-requests: 1
             * user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36
             * accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,;q=0.8
             * accept-encoding: gzip, deflate, sdch, br
             * accept-language: en-US,en;q=0.8
             * cookie: [375 bytes were stripped]
             * referer: https://localhost:8181/web-servlet-4.0-push-arbitrary-method/test
             */
            
        PushBuilder pushBuilder = req.newPushBuilder().
            method("FOO").
            path("foo/my.css");
        pushBuilder.push();
        res.getWriter().println("<html><head><title>HTTP2 Test</title></head><body>Hello</body></html>");
        res.setStatus(HttpServletResponse.SC_OK);
        return;
    }
}
