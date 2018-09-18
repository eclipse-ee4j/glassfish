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

package com.sun.enterprise.web.connector.coyote;

import org.apache.catalina.connector.CoyoteWriter;
import org.apache.catalina.connector.OutputBuffer;
import org.apache.catalina.connector.Response;


/**
 * Customized version of the Tomcat 5 CoyoteResponse
 */
public class PECoyoteResponse extends Response {

    @Override
    protected CoyoteWriter createWriter(OutputBuffer outbuf) {
        return new PECoyoteWriter(outbuf);
    }

    // START GlassFish 898
    /**
     * Gets the string representation of the given cookie.
     *
     * @param cookie The cookie whose string representation to get
     *
     * @return The cookie's string representation
     */
    /*protected String getCookieString(Cookie cookie) {

        PwcWebModule wm = (PwcWebModule) getContext();
        boolean encodeCookies = false;
        if (wm != null && wm.getEncodeCookies()) {
            encodeCookies = true;
        }

        return getCookieString(cookie, encodeCookies);
    }*/
    // END GlassFish 898

    static class PECoyoteWriter
                    extends org.apache.catalina.connector.CoyoteWriter
                    implements org.glassfish.jsp.api.ByteWriter {

        public PECoyoteWriter(OutputBuffer ob) {
            super(ob);
        }
    }
}
