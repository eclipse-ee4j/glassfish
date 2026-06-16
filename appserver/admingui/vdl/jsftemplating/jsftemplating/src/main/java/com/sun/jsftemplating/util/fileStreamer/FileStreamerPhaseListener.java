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

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.util.LogUtil;

import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * <code>FileStreamerPhaseListener</code> provides a <code>PhaseListener</code> to wrap JSFTemplating's
 * {@link FileStreamer} utility, a utility method to construct a <code>FileStreamPhaseListener</code>-compatible URL,
 * and an event {@link Handler} to expose the utility method to the PDL.
 * </p>
 *
 * @author Jason CTR Lee
 */
public class FileStreamerPhaseListener implements PhaseListener {

    private static final long serialVersionUID = 1;
    private static final String INVOCATION_PATH = "com.sun.jsftemplating.INVOCATION_PATH";
    public static final String STATIC_RESOURCE_IDENTIFIER = "/jsft_resource";

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
            FacesContext context = event.getFacesContext();
            ExternalContext extContext = context.getExternalContext();

            String pathInfo = extContext.getRequestPathInfo();
            if (pathInfo == null) {
                pathInfo = extContext.getRequestServletPath();
            }
            if (pathInfo != null && pathInfo.indexOf(STATIC_RESOURCE_IDENTIFIER) != -1) {
                String path = null;
                Context fsContext = new FacesStreamerContext(context);

                // Mark the response complete to ensure no further JSF
                // processing will occur for this request. We will then
                // continue to serve the response for this resource request.
                context.responseComplete();

                // Get the HttpServletResponse
                Object obj = extContext.getResponse();
                HttpServletResponse resp = null;
                if (obj instanceof HttpServletResponse) {
                    resp = (HttpServletResponse) obj;
                    path = extContext.getRequestParameterMap().get(Context.CONTENT_FILENAME);

                    fsContext.setAttribute(Context.FILE_PATH, path);

                    // We have an HttpServlet response, do some extra stuff...
                    // Check the last modified time to see if we need to serve the resource
// FIXME: Not sure why this is not part of the FacesStreamerContext...  investigate more later.
                    long mod = fsContext.getContentSource().getLastModified(fsContext);
                    if (mod != -1) {
                        HttpServletRequest req = (HttpServletRequest) extContext.getRequest();
                        long ifModifiedSince = req.getDateHeader("If-Modified-Since");
                        // Round down to the nearest second for a proper compare
                        if (ifModifiedSince < mod / 1000 * 1000) {
                            // A ifModifiedSince of -1 will always be less
                            resp.setDateHeader("Last-Modified", mod);
                            resp.setDateHeader("Expires", new java.util.Date().getTime() + Context.EXPIRY_TIME);
                        } else {
                            // Set not modified header and complete response
                            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                        }
                    }
                }

                // Stream the content
                try {
                    FileStreamer.getFileStreamer(context).streamContent(fsContext);
                } catch (FileNotFoundException ex) {
                    if (LogUtil.infoEnabled()) {
                        LogUtil.info("JSFT0004", (Object) path);
                    }
                    if (resp != null) {
                        try {
                            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                        } catch (IOException ioEx) {
                            // Ignore
                        }
                    }
                } catch (IOException ex) {
                    if (LogUtil.infoEnabled()) {
                        LogUtil.info("JSFT0004", (Object) path);
                        if (LogUtil.fineEnabled()) {
                            LogUtil.fine("Resource (" + path + ") not available!", ex);
                        }
                    }
                }
            }
        }
    }

    /**
     * This utility method will create a URL referencing the specifed resource, using the <code>contentSourceId</code>
     * specified. If the <code>contentSourceId</code> is null, the default {@link ContentSource}
     * ({@link Context#DEFAULT_CONTENT_SOURCE_ID Context.DEFAULT_CONTENT_SOURCE_ID}) will be used instead. The resulting URL
     * will be recognizable by <code>FileStreamerPhaseListener</code>. It will begin at the context root, leaving off the
     * protocol, host, port, etc., expecting the browser to complete the URL. This will allow this method to work in
     * situations where the server sits on a private network, with network parameters that differ from the public-facing
     * server which took and forwarded the initial request.
     *
     * @param context The <code>FacesContext</code> of the current request
     * @param contentSourceId The ID of the {@link ContentSource} which should be used to resolve the resource
     * @param path The path to the desired resource
     * @return The URL representing the resource
     */
    public static String createResourceUrl(FacesContext context, String contentSourceId, String path) {
        if (context == null) {
            // Likely performance hit with this ThreadLocal look up. DON'T DO THIS! :)
            context = FacesContext.getCurrentInstance();
        }
        StringBuilder sb = new StringBuilder(64);
        // sb.append(context.getExternalContext().getRequestContextPath());
        String mapping = getFacesMapping(context);
        if (mapping.charAt(0) == '/') { // prefix mapping
            sb.append(mapping).append(STATIC_RESOURCE_IDENTIFIER);
        } else {
            sb.append(STATIC_RESOURCE_IDENTIFIER).append(mapping);
        }

        sb.append("?").append(Context.CONTENT_SOURCE_ID).append("=")
                .append(contentSourceId == null ? Context.DEFAULT_CONTENT_SOURCE_ID : contentSourceId).append("&")
                .append(Context.CONTENT_FILENAME).append("=").append(path);

        String url = null;
        try {
            String encoding;
            String contentType;
            ResponseWriter writer = context.getResponseWriter();
            if (writer != null) {
                encoding = writer.getCharacterEncoding();
                contentType = writer.getContentType();
            } else {
                ExternalContext ec = context.getExternalContext();
                encoding = ec.getRequestCharacterEncoding();
                contentType = ec.getRequestContentType();
            }
            url = writeURL(sb.toString(), encoding, contentType);
            // sb.toString();
        } catch (IOException ex) {
            Logger.getLogger(FileStreamerPhaseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        // sb.toString();

        return url;
    }

    /**
     * <p>
     * Returns the URL pattern of the {@link jakarta.faces.webapp.FacesServlet} that is executing the current request. If
     * there are multiple URL patterns, the value returned by <code>HttpServletRequest.getServletPath()</code> and
     * <code>HttpServletRequest.getPathInfo()</code> is used to determine which mapping to return.
     * </p>
     * If no mapping can be determined, it most likely means that this particular request wasn't dispatched through the
     * {@link jakarta.faces.webapp.FacesServlet}.
     *
     * @param context the {@link FacesContext} of the current request
     *
     * @return the URL pattern of the {@link jakarta.faces.webapp.FacesServlet} or <code>null</code> if no mapping can be
     * determined
     *
     * @throws NullPointerException if <code>context</code> is null
     */
    private static String getFacesMapping(FacesContext context) {

        if (context == null) {
            throw new NullPointerException("The FacesContext was null.");
        }

        // Check for a previously stored mapping
        ExternalContext extContext = context.getExternalContext();
        String mapping = (String) extContext.getRequestMap().get(INVOCATION_PATH);

        if (mapping == null) {

            Object request = extContext.getRequest();
            String servletPath = null;
            String pathInfo = null;

            // first check for jakarta.servlet.forward.servlet_path
            // and jakarta.servlet.forward.path_info for non-null
            // values. if either is non-null, use this
            // information to generate determine the mapping.

            if (request instanceof HttpServletRequest) {
                servletPath = extContext.getRequestServletPath();
                pathInfo = extContext.getRequestPathInfo();
            }

            mapping = getMappingForRequest(servletPath, pathInfo);
        }

        // if the FacesServlet is mapped to /* throw an
        // Exception in order to prevent an endless
        // RequestDispatcher loop
        if ("/*".equals(mapping)) {
            throw new FacesException("The FacesServlet was configured incorrectly");
        }

        if (mapping != null) {
            extContext.getRequestMap().put(INVOCATION_PATH, mapping);
        }
        return mapping;
    }

    /**
     * <p>
     * Return the appropriate {@link jakarta.faces.webapp.FacesServlet} mapping based on the servlet path of the current
     * request.
     * </p>
     *
     * @param servletPath the servlet path of the request
     * @param pathInfo the path info of the request
     *
     * @return the appropriate mapping based on the current request
     *
     * @see HttpServletRequest#getServletPath()
     */
    private static String getMappingForRequest(String servletPath, String pathInfo) {

        if (servletPath == null) {
            return null;
        }

        // If the path returned by HttpServletRequest.getServletPath()
        // returns a zero-length String, then the FacesServlet has
        // been mapped to '/*'.
        if (servletPath.length() == 0) {
            return "/*";
        }

        // presence of path info means we were invoked
        // using a prefix path mapping
        if ((pathInfo != null) || (servletPath.indexOf('.') < 0)) {
            return servletPath;
        } else {
            // Servlet invoked using extension mapping
            return servletPath.substring(servletPath.lastIndexOf('.'));
        }
    }

    @Override
    public void afterPhase(PhaseEvent arg0) {
        // no op
    }

    /**
     * This handler will create a <code>FileStreamPhaseListener</code> resource URL for the specified resource, using the
     * <code>contentSourceId</code> provided, by calling {@link FileStreamerPhaseListener#createResourceUrl
     * FileStreamerPhaseListener.createResourceUrl()}.<br />
     * Inputs:
     * <ul>
     * <li><code>path</code>: the path to the resource</li>
     * <li><code>contentSourceId</code>: the ID of the ContentSource to use in the URL. If this is not provided
     * {@link FileStreamerPhaseListener#createResourceUrl FileStreamerPhaseListener.createResourceUrl()} will use the
     * default {@link ContentSource}.</li>
     * </ul>
     * Output:
     * <ul>
     * <li>url: the <code>FileStreamerPhaseListener</code>-compatible resource URL</li>
     * </ul>
     *
     * @param hc The {@link HandlerContext}.
     */
    @Handler(id = "fileStreamer.getResourceUrl", input = { @HandlerInput(name = "path", type = String.class, required = true),
            @HandlerInput(name = "contentSourceId", type = String.class) }, output = { @HandlerOutput(name = "url", type = String.class) })
    public static void getResourceUrl(HandlerContext hc) {
        hc.setOutputValue("url",
                createResourceUrl(hc.getFacesContext(), (String) hc.getInputValue("contentSourceId"), (String) hc.getInputValue("path")));
    }

    // Private helper methods stolen from Mojarra's c.s.f.u.HtmlUtil
    // WOW! This got away from me. :P

    private static String writeURL(String text, String queryEncoding, String contentType) throws IOException {
        StringBuilder sb = new StringBuilder();
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char ch = text.charAt(i);

            if (ch < 33 || ch > 126) {
                if (ch == ' ') {
                    sb.append('+');
                } else if (ch != ':') {
                    // ISO-8859-1. Blindly assume the character will be < 255.
                    // Not much we can do if it isn't.
                    sb.append('%');
                    sb.append(intToHex((i >> 4) % 0x10));
                    sb.append(intToHex(i % 0x10));

                }
            }
            // DO NOT encode '%'. If you do, then for starters,
            // we'll double-encode anything that's pre-encoded.
            // And, what's worse, there becomes no way to use
            // characters that must be encoded if you
            // don't want them to be interpreted, like '?' or '&'.
            // else if('%' == ch)
            // {
            // writeURIDoubleHex(out, ch);
            // }
            else if (ch == '"') {
                sb.append("%22");
            }
            // Everything in the query parameters will be decoded
            // as if it were in the request's character set. So use
            // the real encoding for those!
            else if (ch == '?') {
                sb.append('?');
                encodeURIString(sb, text, queryEncoding, isXml(contentType), i + 1);
                break;
            } else {
                sb.append(ch);
            }
        }

        return sb.toString();
    }

    // This method is obviously limited, but it works for Mojarra from which
    // I lifted it, so I'm not too worried about it... :)
    private static char intToHex(int i) {
        if (i < 10) {
            return (char) ('0' + i);
        } else {
            return (char) ('A' + (i - 10));
        }
    }

    private static boolean isXml(String contentType) {
        return XHTML_CONTENT_TYPE.equals(contentType) || APPLICATION_XML_CONTENT_TYPE.equals(contentType)
                || TEXT_XML_CONTENT_TYPE.equals(contentType);
    }

    // NOTE: Any changes made to this method should be made
    // in the associated method that accepts a char[] instead
    // of String
    private static boolean isAmpEscaped(String text, int idx) {
        for (int i = 1, ix = idx; i < AMP_CHARS.length; i++, ix++) {
            if (text.charAt(ix) == AMP_CHARS[i]) {
                continue;
            }
            return false;
        }
        return true;
    }

    // Encode a String into URI-encoded form. This code will
    // appear rather (ahem) similar to java.net.URLEncoder
    // This is duplicated below accepting a char[] for the content
    // to write. Any changes here, should be made there as well.
    private static void encodeURIString(StringBuilder out, String text, String encoding, boolean isXml, int start) throws IOException {
        StringBuilder buf = new StringBuilder();

        int length = text.length();
        for (int i = start; i < length; i++) {
            char ch = text.charAt(i);
            if (DONT_ENCODE_SET.get(ch)) {
                if (isXml && ch == '&') {
                    if (i + 1 < length && isAmpEscaped(text, i + 1)) {
                        out.append(ch);
                        continue;
                    }
                    out.append(AMP_CHARS);
                } else {
                    out.append(ch);
                }
            } else {
                // convert to external encoding before hex conversion
                buf.append(ch);

                for (int j = 0, size = buf.length(); j < size; j++) {
                    out.append('%');
                    out.append(intToHex((buf.charAt(j) + 256 >> 4) % 0x10));
                    out.append(intToHex(buf.charAt(j) + 256 % 0x10));
                }

                buf = new StringBuilder();
            }
        }
    }

    private static final BitSet DONT_ENCODE_SET = new BitSet(256);
    private static final String XHTML_CONTENT_TYPE = "application/xhtml+xml";
    private static final String APPLICATION_XML_CONTENT_TYPE = "application/xml";
    private static final String TEXT_XML_CONTENT_TYPE = "text/xml";
    private static final char[] AMP_CHARS = "&amp;".toCharArray();

    // See: http://www.ietf.org/rfc/rfc2396.txt
    // We're not fully along for that ride either, but we do encode
    // ' ' as '%20', and don't bother encoding '~' or '/'
    static {
        for (int i = 'a'; i <= 'z'; i++) {
            DONT_ENCODE_SET.set(i);
        }

        for (int i = 'A'; i <= 'Z'; i++) {
            DONT_ENCODE_SET.set(i);
        }

        for (int i = '0'; i <= '9'; i++) {
            DONT_ENCODE_SET.set(i);
        }

        // Don't encode '%' - we don't want to double encode anything.
        DONT_ENCODE_SET.set('%');
        // Ditto for '+', which is an encoded space
        DONT_ENCODE_SET.set('+');

        DONT_ENCODE_SET.set('#');
        DONT_ENCODE_SET.set('&');
        DONT_ENCODE_SET.set('=');
        DONT_ENCODE_SET.set('-');
        DONT_ENCODE_SET.set('_');
        DONT_ENCODE_SET.set('.');
        DONT_ENCODE_SET.set('*');
        DONT_ENCODE_SET.set('~');
        DONT_ENCODE_SET.set('/');
        DONT_ENCODE_SET.set('\'');
        DONT_ENCODE_SET.set('!');
        DONT_ENCODE_SET.set('(');
        DONT_ENCODE_SET.set(')');
        DONT_ENCODE_SET.set(';');
        DONT_ENCODE_SET.set(':');
    }
}
