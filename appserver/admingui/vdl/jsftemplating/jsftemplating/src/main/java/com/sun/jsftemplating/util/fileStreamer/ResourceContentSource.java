/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.util.fileStreamer;

import com.sun.jsftemplating.layout.LayoutDefinitionManager;
import com.sun.jsftemplating.util.FileUtil;
import com.sun.jsftemplating.util.LogUtil;

import jakarta.faces.context.FacesContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * <p>
 * This class implements {@link ContentSource}. It retreives it content by looking in the "docroot" of the application,
 * and if not found, it will check the classpath.
 * </p>
 */
public class ResourceContentSource implements ContentSource {

    /**
     * <p>
     * This method returns a unique string used to identify this {@link ContentSource}. This string must be specified in
     * order to select the appropriate {@link ContentSource} when using the {@link FileStreamer}.
     * </p>
     */
    @Override
    public String getId() {
        return ID;
    }

    /**
     * <p>
     * This method is responsible for generating the content and returning an <code>InputStream</code> for that content. It
     * is also responsible for setting any attribute values in the {@link Context}, such as {@link Context#EXTENSION} or
     * {@link Context#CONTENT_TYPE}.
     * </p>
     */
    @Override
    public InputStream getInputStream(Context ctx) throws IOException {
        // See if we already have it.
        InputStream in = (InputStream) ctx.getAttribute("inputStream");
        if (in != null) {
            return in;
        }

        // Get the path...
        String path = getResourcePath(ctx);

        // Find the file...
        URL url = FileUtil.searchForFile(path, null);
        if (url == null) {
            return null;
        }

        // Set the extension so it can be mapped to a MIME type
        int index = path.lastIndexOf('.');
        if (index > 0) {
            ctx.setAttribute(Context.EXTENSION, path.substring(index + 1));
        }

        // Open the InputStream
        in = url.openStream();
        ctx.setAttribute("inputStream", in);

        // Return an InputStream to the file
        return in;
    }

    /**
     * <p>
     * This method returns the path of the resource that was requested.
     * </p>
     */
    @Override
    public String getResourcePath(Context ctx) {
        // Check the ctx for the path...
        String path = (String) ctx.getAttribute(Context.FILE_PATH + "norm");
        if (path != null) {
            return path;
        }

        // Not yet calculated, calculate it...
        String origPath = (String) ctx.getAttribute(Context.FILE_PATH);

        // Store for next time... and return
        path = normalize(origPath);
        ctx.setAttribute(Context.FILE_PATH + "norm", path);
        return path;
    }

    /**
     * <p>
     * This method attempts to normalize the paths that we are using for comparison purposes and to ensure illegal paths are
     * prevented for security reasons.
     * </p>
     */
    public static String normalize(String origPath) {
        String path = origPath;

        // Normalize it...
        if (path != null && path.length() > 0) {
            path = path.replace('\\', '/');

            if (path.charAt(0) != '/') {
                path = "/".concat(path);
            }
            // Replace all double "//" with "/"
            if (path.contains("//")) {
                path = path.replaceAll("//", "/");
            }
            for (int idx = path.indexOf("/../"); idx != -1; idx = path.indexOf("/../")) {
                if (idx == 0) {
                    // Make sure we're not trying to go before the context root
                    LogUtil.info("JSFT0010", origPath);
                    throw new IllegalArgumentException("Invalid Resource Path: '" + origPath + "'");
                }
                // Create new path after evaluating ".."
                int prevPathIdx = path.lastIndexOf('/', idx - 2) + 1;
                path = path.substring(0, prevPathIdx) // before x/../
                        + path.substring(idx + 4); // after x/../
            }

            // Remove leading '/' chars
            while (path.length() > 0 && path.charAt(0) == '/') {
                path = path.substring(1);
            }
            // We check for "../" so ".." at the end of a path could occur,
            // which is fine, unless it is also at the beginning...
            if (path.equals("..")) {
                path = null;
            }

            // Last ensure path does not end in a '/'
            if (path != null && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

    /**
     * <p>
     * This method may be used to clean up any temporary resources. It will be invoked after the <code>InputStream</code>
     * has been completely read.
     * </p>
     */
    @Override
    public void cleanUp(Context ctx) {
        // Get the File information
        InputStream is = (InputStream) ctx.getAttribute("inputStream");

        // Close the InputStream
        if (is != null) {
            try {
                is.close();
            } catch (Exception ex) {
                // Ignore...
            }
        }
        ctx.removeAttribute("inputStream");
    }

    /**
     * <p>
     * This method is responsible for returning the last modified date of the content, or -1 if not applicable. This
     * information will be used for caching.
     * </p>
     */
    @Override
    public long getLastModified(Context context) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (LayoutDefinitionManager.isDebug(fc)) {
            // When in debug mode, don't cache resources... otherwise always
            // allow browser to always cache them
            if (fc != null) {
                // Check to see if this resource exists in the FileSystem, if
                // it doesn't we will let the browser cache it b/c it can't be
                // changed.
                String path = getResourcePath(context);
                path = FileUtil.getRealPath(fc.getExternalContext().getContext(), path);
                if (path != null) {
                    long time = new File(path).lastModified();
                    if (time > 0) {
                        // Send the timestamp so it will only be cached by
                        // the browser if it hasn't changed
                        return time;
                    }
                }
            }
        }

        // This will enable caching on all files served through this code path
        return DEFAULT_MODIFIED_DATE;
    }

    /**
     * This is the default "Last-Modified" Date. Its value is the <code>Long</code> representing the initialization time of
     * this class.
     */
    protected static final long DEFAULT_MODIFIED_DATE = new Date().getTime();

    /**
     * <p>
     * This is the ID for this {@link ContentSource}.
     * </p>
     */
    public static final String ID = "resourceCS";
}
