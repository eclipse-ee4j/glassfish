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

package org.glassfish.appclient.server.core.jws.servedcontent;

import java.io.File;
import java.io.IOException;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

/**
 * Represents all static content served for Java Web Start app client launches.
 * <p>
 * This interface exposes a File object, as opposed to a stream for example,
 * so that the Grizzly adapter that actually serves the content can use
 * sendFile for efficiency.
 *
 * @author tjquinn
 */
public interface StaticContent extends Content {

    /**
     * Returns a File object for the content.
     * @return a File object for the content
     */
    public File file() throws IOException;

    /**
     * Process the static content, adding the correct data to the response.
     * @param relativeURIString URI path by which the content was addressed
     * @param gReq the request
     * @param gResp the response
     * @throws Exception
     */
    public void process(String relativeURIString, Request gReq, Response gResp) throws Exception;
}
