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

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <p>
 * This class encapsulates servlet specific objects so that the {@link FileStreamer} class is not servlet-specific.
 * </p>
 *
 * <p>
 * This implementation will look for the following attributes ({@link BaseContext#setAttribute(String, Object)}):
 * </p>
 *
 * <ul>
 * <li>{@link Context#CONTENT_TYPE} -- The "Content-type:" of the response.</li>
 * <li>{@link Context#CONTENT_DISPOSITION} -- Disposition of the streamed content.</li>
 * <li>{@link Context#CONTENT_FILENAME} -- Filename of the streamed content.</li>
 * <li>{@link Context#EXTENSION} -- The file extension of the response.</li>
 * </ul>
 */
public class FacesStreamerContext extends BaseContext {

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public FacesStreamerContext(FacesContext ctx) {
        setFacesContext(ctx);
        init();
    }

    /**
     * <p>
     * This method initializes {@link FileStreamer} {@link ContentSource}s. It looks for the {@link Context#CONTENT_SOURCES}
     * context parameter (JSF doesn't provide a way to get to the ServletConfig init parameters, as of JSF 1.2).
     * </p>
     */
    protected synchronized void init() {
        FacesContext ctx = getFacesContext();
        if (ctx == null) {
            // Should not happen in a normal env...
            return;
        }
        ExternalContext extCtx = ctx.getExternalContext();
        boolean initDone = extCtx.getApplicationMap().containsKey(INIT_DONE);
        if (initDone) {
            return;
        }

        // Register ContentSources
        String sources = extCtx.getInitParameter(CONTENT_SOURCES);
        FileStreamer fs = getFileStreamer();
        if (sources != null && sources.trim().length() != 0) {
            StringTokenizer tokens = new StringTokenizer(sources, " \t\n\r\f,;:");
            while (tokens.hasMoreTokens()) {
                fs.registerContentSource(tokens.nextToken());
            }
        }

        // Set Valid path parameters...
        String path, end;
        String allow = extCtx.getInitParameter(ALLOW_PATHS);
        List<String> paths = new ArrayList<>();
        if (allow != null) {
            StringTokenizer tok = new StringTokenizer(allow, ",:;");
            while (tok.hasMoreTokens()) {
                path = tok.nextToken();
                end = path.endsWith("/") ? "/" : "";
                paths.add(ResourceContentSource.normalize(path) + end);
            }
        } else {
            paths.add("");
        }
        setAllowedPaths(extCtx, paths);

        // Set invalid paths...
        String deny = extCtx.getInitParameter(DENY_PATHS);
        paths = new ArrayList<>();
        if (deny != null) {
            StringTokenizer tok = new StringTokenizer(deny, ",:;");
            while (tok.hasMoreTokens()) {
                path = tok.nextToken();
                end = path.endsWith("/") ? "/" : "";
                paths.add(ResourceContentSource.normalize(path) + end);
            }
        } else {
            paths.add("WEB-INF/");
            paths.add("META-INF/");
        }
        setDeniedPaths(extCtx, paths);

        if (ctx != null) {
            // Mark initialization as complete
            extCtx.getApplicationMap().put(INIT_DONE, true);
        }
    }

    /**
     * <p>
     * Accessor to get the {@link FileStreamer} instance.
     * </p>
     */
    @Override
    public FileStreamer getFileStreamer() {
        return FileStreamer.getFileStreamer(getFacesContext());
    }

    /**
     * <p>
     * This method locates the appropriate {@link ContentSource} for this {@link Context}. It uses the
     * <code>FacesContext</code>'s <code>ExternalContext</code> to look for a <b>request parameter</b> named
     * {@link Context#CONTENT_SOURCE_ID}. This value is used as the key when looking up registered {@link ContentSource}
     * implementations.
     * </p>
     */
    @Override
    public ContentSource getContentSource() {
        ContentSource src = (ContentSource) getAttribute("_contentSource");
        if (src != null) {
            return src;
        }

        // Get the ContentSource id
        FacesContext ctx = getFacesContext();
        String id = ctx.getExternalContext().getRequestParameterMap().get(Context.CONTENT_SOURCE_ID);
        if (id == null) {
            // Use the default ContentSource
            id = Context.DEFAULT_CONTENT_SOURCE_ID;
        }

        // Get the ContentSource
        src = getFileStreamer().getContentSource(id);
        if (src == null) {
            throw new RuntimeException("The ContentSource with id '" + id + "' is not registered!");
        }

        // Return the ContentSource
        setAttribute("_contentSource", src);
        return src;
    }

    /**
     * <p>
     * This method allows the Context to restrict access to resources. It returns <code>true</code> if the user is allowed
     * to view the resource. It returns <code>false</code> if the user should not be allowed access to the resource.
     * </p>
     */
    @Override
    public boolean hasPermission(ContentSource src) {
        String filename = src.getResourcePath(this);
        ExternalContext extCtx = getFacesContext().getExternalContext();
        boolean ok = false;

        // Ensure it is in our list of OK paths...
        List<String> paths = getAllowedPaths(extCtx);
        for (String path : paths) {
            if (filename.startsWith(path)) {
                ok = true;
                break;
            }
        }

        // ...and ensure it is not in our blacklisted paths...
        if (ok) {
            // Only check if ok so far...
            paths = getDeniedPaths(extCtx);
            for (String path : paths) {
                if ((path.startsWith("*") && filename.endsWith(path.substring(1))) || filename.startsWith(path)) {
                    // Matched suffix pattern
                    ok = false;
                    break;
                }
            }
        }

        return ok;
    }

    /**
     * <p>
     * This methods sets the allowed paths for resources. Paths may be further restricted using the {@link #setDeniedPaths}
     * method.
     * </p>
     */
    public List<String> getAllowedPaths(ExternalContext extCtx) {
        return (List<String>) extCtx.getApplicationMap().get(ALLOWED_PATHS_KEY);
    }

    /**
     * <p>
     * This methods sets the allowed paths for resources. Paths may be further restricted using the {@link #setDeniedPaths}
     * method.
     * </p>
     */
    public void setAllowedPaths(ExternalContext ctx, List<String> paths) {
        ctx.getApplicationMap().put(ALLOWED_PATHS_KEY, paths);
    }

    /**
     * <p>
     * This methods sets the list of paths in which resources should not be served.
     * </p>
     */
    public void setDeniedPaths(ExternalContext ctx, List<String> paths) {
        ctx.getApplicationMap().put(DENIED_PATHS_KEY, paths);
    }

    /**
     * <p>
     * This methods sets the list of paths for resources.
     * </p>
     */
    public List<String> getDeniedPaths(ExternalContext extCtx) {
        return (List<String>) extCtx.getApplicationMap().get(DENIED_PATHS_KEY);
    }

    /**
     * <p>
     * This method is responsible for setting the response header information.
     * </p>
     */
    @Override
    public void writeHeader(ContentSource source) {
// FIXME: Portlet
        ServletResponse resp = (ServletResponse) getFacesContext().getExternalContext().getResponse();

        // Set the "Last-Modified" Header
        // First check context
        long longTime = source.getLastModified(this);
        if (longTime != -1) {
            HttpServletResponse httpResponse = (HttpServletResponse) resp;
            httpResponse.setDateHeader("Last-Modified", longTime);
            httpResponse.setDateHeader("Expires", new java.util.Date().getTime() + Context.EXPIRY_TIME);
        }

        // First check CONTENT_TYPE
        String contentType = (String) getAttribute(CONTENT_TYPE);
        if (contentType == null) {
            // Not found yet, check EXTENSION
            String ext = (String) getAttribute(EXTENSION);
            if (ext != null) {
                contentType = FileStreamer.getMimeType(ext);
            }
            if (contentType == null) {
                // Default Content-type is: application/octet-stream
                contentType = FileStreamer.getDefaultMimeType();
            }
        }
        ((HttpServletResponse) resp).setHeader("Content-type", contentType);

        // Check disposition/filename to associate a name with the stream
        String disposition = (String) getAttribute(CONTENT_DISPOSITION);
        String filename = (String) getAttribute(CONTENT_FILENAME);
        if (disposition == null) {
            // No disposition set, see if we have a filename
            if (filename != null) {
                ((HttpServletResponse) resp).setHeader("Content-Disposition", DEFAULT_DISPOSITION + ";filename=\"" + filename + "\"");
            }
        } else {
            // Disposition set, see if we also have a filename
            if (filename != null) {
                disposition += ";filename=\"" + filename + "\"";
            }
            ((HttpServletResponse) resp).setHeader("Content-Disposition", disposition);
        }
    }

    /**
     * <p>
     * This method is responsible for sending an error.
     * </p>
     */
    @Override
    public void sendError(int code, String msg) throws IOException {
// FIXME: JSF 2.0 now provides: externalContext.responseSendError(int, String)
// FIXME: Portal
        HttpServletResponse resp = (HttpServletResponse) getFacesContext().getExternalContext().getResponse();
        if (msg == null) {
            resp.sendError(code);
        } else {
            resp.sendError(code, msg);
        }
    }

    /**
     * <p>
     * This method is returns the <code>ServletOutputStream</code>.
     * </p>
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return getFacesContext().getExternalContext().getResponseOutputStream();
    }

    /**
     * <p>
     * This returns the <code>FacesContext</code>. This is the same as calling:
     * </p>
     *
     * <p>
     * <code>getAttribute({@link #FACES_CONTEXT})</code>
     * </p>
     */
    public FacesContext getFacesContext() {
        return (FacesContext) getAttribute(FACES_CONTEXT);
    }

    /**
     * <p>
     * This sets the <code>FacesContext</code>. This is the same as calling:
     * </p>
     *
     * <p>
     * <code>setAttribute({@link #FACES_CONTEXT}, ctx)</code>
     * </p>
     */
    protected void setFacesContext(FacesContext ctx) {
        setAttribute(FACES_CONTEXT, ctx);
    }

    /**
     * <p>
     * Flag indicating initialization for this class has been completed.
     * </p>
     */
    private static final String INIT_DONE = "__jsft_StreamContextInitialized";

    /**
     * <p>
     * The attribute value to access the <code>FacesContext</code>. See {@link #getFacesContext()}.
     * </p>
     */
    public static final String FACES_CONTEXT = "facesContext";

    /**
     * <p>
     * The default Content-Disposition. It is only used when a filename is provided, but a disposition is not. The default
     * is "attachment". This will normally cause a browser to prompt the user to save the file. This is the default since
     * setting a filename implies that the user may want to save this file. You must explicitly set the disposition for
     * "inline" behavior with a filename.
     * </p>
     */
    public static final String DEFAULT_DISPOSITION = "attachment";
}
