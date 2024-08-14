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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.admingui.common.util.RestUtil;

/**
 *
 * @author andriy.zhdanov
 */
public class LogFilesContentSource  implements DownloadServlet.ContentSource {

     /**
     *  <p> This method returns a unique string used to identify this
     *      {@link DownloadServlet#ContentSource}.  This string must be
     *      specified in order to select the appropriate
     *      {@link DownloadServlet#ContentSource} when using the
     *      {@link DownloadServlet}.</p>
     */
    public String getId() {
        return "LogFiles";                                 // NOI18N
    }

    /**
     *  <p> This method is responsible for generating the content and
     *      returning an InputStream to that content.  It is also
     *      responsible for setting any attribute values in the
     *      {@link DownloadServlet#Context}, such as {@link DownloadServlet#EXTENSION} or
     *      {@link DownloadServlet#CONTENT_TYPE}.</p>
     */
    public InputStream getInputStream(DownloadServlet.Context ctx) {
        // Set the extension so it can be mapped to a MIME type
        ctx.setAttribute(DownloadServlet.EXTENSION, "CLIENT-STUBS");

        // Get appName
        HttpServletRequest request = (HttpServletRequest) ctx.getServletRequest();
        String target = request.getParameter("target");
        String restUrl = request.getParameter("restUrl");

        // Create the tmpFile
        InputStream tmpFile = null;
        try {
            String endpoint = restUrl + "/collect-log-files";
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = new Date();
            Map attrsMap = new HashMap();
            String tempDir = System.getProperty("java.io.tmpdir");
            String fileName = "log-files-" + target + "-" + dateFormat.format(date) + ".zip";
            File file = new File(tempDir, fileName);
            // retrieveFilePath
            attrsMap.put("id", file.getAbsolutePath()); // CAUTION: file instead of dir
            attrsMap.put("retrieve", "true");
            attrsMap.put("target", target);
            RestUtil.postRestRequestFromServlet(request, endpoint, attrsMap, true, true);
            tmpFile = new FileInputStream(file);
            file.delete();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        // Save some important stuff for cleanUp
        ctx.setAttribute("tmpFile", tmpFile);                   // NOI18N

        // Return an InputStream to the tmpFile
        return tmpFile;
    }

    /**
     *  <p> This method may be used to clean up any temporary resources.  It
     *      will be invoked after the <code>InputStream</code> has been
     *      completely read.</p>
     */
    public void cleanUp(DownloadServlet.Context ctx) {
        // Get the File information
        InputStream tmpFile =
            (InputStream) ctx.getAttribute("tmpFile");          // NOI18N

        // Close the InputStream
        if (tmpFile != null) {
            try {
                tmpFile.close();
            } catch (Exception ex) {
                // Ignore...
            }
        }

        ctx.removeAttribute("tmpFile");                 // NOI18N
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
