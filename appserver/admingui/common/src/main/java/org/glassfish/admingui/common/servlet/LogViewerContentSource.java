/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.common.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.glassfish.admingui.common.util.RestUtil;

/**
 *
 * @author andriy.zhdanov
 */
public class LogViewerContentSource  implements DownloadServlet.ContentSource {

     /**
     *  <p> This method returns a unique string used to identify this
     *      {@link DownloadServlet#getContentSource(String)}.  This string must be
     *      specified in order to select the appropriate
     *      {@link DownloadServlet#getContentSource(String)} when using the
     *      {@link DownloadServlet}.</p>
     */
    public String getId() {
        return "LogViewer";                                 // NOI18N
    }

    /**
     *  <p> This method is responsible for generating the content and
     *      returning an InputStream to that content.  It is also
     *      responsible for setting any attribute values in the
     *      {@link DownloadServlet#CONTENT_SOURCES}, such as {@link DownloadServlet#EXTENSION} or
     *      {@link DownloadServlet#CONTENT_TYPE}.</p>
     */
    public InputStream getInputStream(DownloadServlet.Context ctx) {
        // Set the extension so it can be mapped to a MIME type
        ctx.setAttribute(DownloadServlet.EXTENSION, "asc");

        HttpServletRequest request = (HttpServletRequest) ctx.getServletRequest();
        //http://localhost:4848/management/domain/view-log/?start=180427&instanceName=server
        String restUrl = request.getParameter("restUrl");
        String start = request.getParameter("start");
        String instanceName = request.getParameter("instanceName");

        // Create the tmpFile
        InputStream tmpFile = null;
        try {
            String endpoint = restUrl + "/view-log/";
            Map<String, Object> attrsMap = new HashMap<String, Object>();
            attrsMap.put("start", start);
            attrsMap.put("instanceName", instanceName);
            Response cr = RestUtil.getRequestFromServlet(request, endpoint, attrsMap);
            Map<String, String> headers = new HashMap<String, String>();
            String xTextAppendNextHeader = cr.getHeaderString("X-Text-Append-Next");
            if (!xTextAppendNextHeader.isEmpty()) {
                StringTokenizer tokenizer = new StringTokenizer(xTextAppendNextHeader, ",");
                if (tokenizer.hasMoreElements()) {
                    headers.put("X-Text-Append-Next", tokenizer.nextToken());
                }
            }
            ctx.setAttribute(DownloadServlet.HEADERS, headers);
            tmpFile = cr.readEntity(InputStream.class);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        // Return an InputStream to the tmpFile
        return tmpFile;
    }

    /**
     *  <p> This method may be used to clean up any temporary resources.  It
     *      will be invoked after the <code>InputStream</code> has been
     *      completely read.</p>
     */
    public void cleanUp(DownloadServlet.Context ctx) {
        // Nothing to do
    }

    /**
     *  <p> This method is responsible for returning the last modified date of
     *      the content, or -1 if not applicable.  This information will be
     *      used for caching.  This implementation always returns -1.</p>
     *
     *  @return -1
     */
    public long getLastModified(DownloadServlet.Context context) {
        return -1;
    }
}
