/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

 /*
 * $Id: ThemeServlet.java,v 1.1.4.2.2.2 2009-12-29 05:05:17 jyeary Exp $
 */
package com.sun.webui.theme;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code ThemeServlet} is required by the {@code com.sun.webui} components to
 * resolve references to resources that exist in a jar. This servlet
 * implementation is needed because a JSF FacesServlet cannot be extended and
 * because a {@code jakarta.servlet.ServletContext.getResourceAsStream,} does not
 * search for a resource within jars that are on the application's class path.
 * <p>
 * Not all theme resources that are referenced by a component are located in
 * jars. An application may override theme resources by defining those resources
 * in the application via a theme resource bundle.
 * <p>
 * Themes consist of both resources that are used directly by the Java classes
 * at run-time (for example property files) and resources that are requested by
 * the application users' browser (for example image files). The
 * {@code ThemeServlet} class makes the resources in the Theme jar available
 * over HTTP. This requires that the the class-path of this servlet be the same
 * as the class path of the application. The situation should not exist where a
 * different jar is used during the server side page assembly to acquire a theme
 * resource reference and then a different jar is used by this servlet to obtain
 * the actual bits of that resource when the page is rendering in a browser.
 * </p>
 *
 * <p>
 * <b>How to configure the ThemeServlet</b></p>
 *
 * <p>
 * Define an entry for the servlet in the web application's configuration file
 * (web.xml). Configure one instance of this servlet as follows:
 * <pre>
 *     &lt;servlet&gt;
 *         &lt;servlet-name&gt;ThemeServlet&lt;/servlet-name&gt;
 *         &lt;servlet-class&gt;
 *              com.sun.webui.jsf.theme.ThemeServlet
 *         &lt;/servlet-class&gt;
 *      &lt;/servlet&gt;
 *
 *     &lt;servlet-mapping&gt;
 *         &lt;servlet-name&gt;ThemeServlet&lt;/servlet-name&gt;
 *         &lt;url-pattern&gt;/theme/*&lt;/url-pattern&gt;
 *     &lt;/servlet-mapping&gt;
 * </pre>
 *
 * <p>
 * Note that the {@code url-pattern} must be specified in a slightly different
 * manner for the {@code ThemeContext} {@code context-param}
 * {@code com.sun.webui.theme.THEME_SERVLET_CONTEXT}
 * <pre>
 *     &lt;context-param&gt;
 *  &lt;param-name&gt;
 *      com.sun.webui.theme.THEME_SERVLET_CONTEXT
 *  &lt;param-name&gt;
 *  &lt;param-value&gt;theme&lt;param-value&gt;
 *    &lt;context-param&gt;
 * </pre> The actual value of the url-pattern is does not have to be
 * {@code /theme/*} it just must be the same for the servlet-mapping and the
 * {@code THEME_SERVLET_CONTEXT} {@code context-param}.
 *
 *
 * @see com.sun.webui.theme.ThemeContext
 * @see com.sun.webui.theme.Theme
 * @see com.sun.webui.theme.ThemeFactory
 */
public final class ThemeServlet extends HttpServlet {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = -8112024913542109274L;

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * The "last modified" timestamp we should broadcast for all resources
     * provided by this servlet. This will enable browsers that cache static
     * resources to send an "If-Modified-Since" header, which will allow us to
     * return a "Not Modified" response.
     */
    private final long lastModified = (new Date()).getTime();

    /**
     * Content types.
     */
    private static final Map<String, String> CONTENT_TYPES
            = new HashMap<String, String>();

    // Some mime-types... by extension
    static {
        // There is no IANA registered type for JS files. See
        // http://annevankesteren.nl/archives/2005/02/javascript-mime-type
        // for a discussion. I picked text/javascript because that's
        // what we use in the script tag. Apache defaults to
        // application/x-javascript
        CONTENT_TYPES.put("js", "text/javascript");
        CONTENT_TYPES.put("css", "text/css");
        CONTENT_TYPES.put("htm", "text/html");
        CONTENT_TYPES.put("html", "text/html");
        CONTENT_TYPES.put("wml", "text/wml");
        CONTENT_TYPES.put("txt", "text/plain");
        CONTENT_TYPES.put("xml", "text/xml");
        CONTENT_TYPES.put("jpeg", "image/jpeg");
        CONTENT_TYPES.put("jpe", "image/jpeg");
        CONTENT_TYPES.put("jpg", "image/jpeg");
        CONTENT_TYPES.put("png", "image/png");
        CONTENT_TYPES.put("tif", "image/tiff");
        CONTENT_TYPES.put("tiff", "image/tiff");
        CONTENT_TYPES.put("bmp", "image/bmp");
        CONTENT_TYPES.put("xbm", "image/xbm");
        CONTENT_TYPES.put("ico", "image/x-icon");
        CONTENT_TYPES.put("gif", "image/gif");
        CONTENT_TYPES.put("pdf", "application/pdf");
        CONTENT_TYPES.put("ps", "application/postscript");
        CONTENT_TYPES.put("mim", "application/mime");
        CONTENT_TYPES.put("mime", "application/mime");
        CONTENT_TYPES.put("mid", "application/midi");
        CONTENT_TYPES.put("midi", "application/midi");
        CONTENT_TYPES.put("wav", "audio/wav");
        CONTENT_TYPES.put("bwf", "audio/wav");
        CONTENT_TYPES.put("cpr", "image/cpr");
        CONTENT_TYPES.put("avi", "video/x-msvideo");
        CONTENT_TYPES.put("mpeg", "video/mpeg");
        CONTENT_TYPES.put("mpg", "video/mpeg");
        CONTENT_TYPES.put("mpm", "video/mpeg");
        CONTENT_TYPES.put("mpv", "video/mpeg");
        CONTENT_TYPES.put("mpa", "video/mpeg");
        CONTENT_TYPES.put("au", "audio/basic");
        CONTENT_TYPES.put("snd", "audio/basic");
        CONTENT_TYPES.put("ulw", "audio/basic");
        CONTENT_TYPES.put("aiff", "audio/x-aiff");
        CONTENT_TYPES.put("aif", "audio/x-aiff");
        CONTENT_TYPES.put("aifc", "audio/x-aiff");
        CONTENT_TYPES.put("cdda", "audio/x-aiff");
        CONTENT_TYPES.put("pict", "image/x-pict");
        CONTENT_TYPES.put("pic", "image/x-pict");
        CONTENT_TYPES.put("pct", "image/x-pict");
        CONTENT_TYPES.put("mov", "video/quicktime");
        CONTENT_TYPES.put("qt", "video/quicktime");
        CONTENT_TYPES.put("pdf", "application/pdf");
        CONTENT_TYPES.put("pdf", "application/pdf");
        CONTENT_TYPES.put("ssm", "application/smil");
        CONTENT_TYPES.put("rsml", "application/vnd.rn-rsml");
        CONTENT_TYPES.put("ra", "application/vnd.rn-realaudio");
        CONTENT_TYPES.put("rm", "application/vnd.rn-realmedia");
        CONTENT_TYPES.put("rv", "application/vnd.rn-realvideo");
        CONTENT_TYPES.put("rf", "application/vnd.rn-realflash");
        CONTENT_TYPES.put("rf", "application/vnd.rn-realflash");
        CONTENT_TYPES.put("asf", "application/x-ms-asf");
        CONTENT_TYPES.put("asx", "application/x-ms-asf");
        CONTENT_TYPES.put("wm", "application/x-ms-wm");
        CONTENT_TYPES.put("wma", "application/x-ms-wma");
        CONTENT_TYPES.put("wax", "application/x-ms-wax");
        CONTENT_TYPES.put("wmw", "application/x-ms-wmw");
        CONTENT_TYPES.put("wvx", "application/x-ms-wvx");
        CONTENT_TYPES.put("swf", "application/x-shockwave-flash");
        CONTENT_TYPES.put("spl", "application/futuresplash");
        CONTENT_TYPES.put("avi", "video/msvideo");
        CONTENT_TYPES.put("flc", "video/flc");
        CONTENT_TYPES.put("mp4", "video/mpeg4");
    }

    /**
     * This method handles the requests for the Theme files.
     *
     * @param request The Servlet Request for the theme file
     * @param response The Servlet Response
     * @throws ServletException If the Servlet fails to serve the resource file
     * @throws IOException If the Servlet cannot locate and read a requested
     * ThemeFile
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    protected void doGet(final HttpServletRequest request,
            final HttpServletResponse response)
            throws ServletException, IOException {

        if (DEBUG) {
            log("doGet()");
        }
        String resourceName = request.getPathInfo();
        InputStream inStream = null;
        OutputStream outStream = null;
        try {
            // The issue here is, do we try and get the resource
            // from the jar that defined this resource ?
            // Or hope that it is unique enough to come from the
            // jar it was defined in.
            //
            // Get InputStream
            inStream = this.getClass().getResourceAsStream(resourceName);
            if (inStream == null) {
                // Send 404 (without the original URI for XSS security reasons)
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            inStream = new BufferedInputStream(inStream, 4096);

            // Ask the container to resolve the MIME type if possible
            String type = getServletContext().getMimeType(resourceName);
            if (type == null) {
                // Otherwise, use our own hard coded list
                int lastDot = resourceName.lastIndexOf('.');
                if (lastDot != -1) {
                    String suffix = resourceName.substring(lastDot + 1);
                    type = (String) CONTENT_TYPES.get(suffix.toLowerCase());
                }
            }
            // Set the content type of this response
            if (type != null) {
                response.setContentType(type);
            }

            // Set the timestamp of the response to enable caching
            response.setDateHeader("Last-Modified", getLastModified(request));

            // Get the OutputStream
            outStream = response.getOutputStream();
            outStream = new BufferedOutputStream(outStream, 4096);

            int character;
            while ((character = inStream.read()) != -1) {
                outStream.write(character);
            }
        } catch (IOException ioex) {
            // Log an error
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException t) {
            }
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException t) {
            }
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return A String that names the Servlet
     */
    @Override
    public String getServletInfo() {
        return "Theme Servlet for Sun Web Components";
    }

    /**
     * Initializes the ThemeServlet.
     *
     * @param config The ServletConfig object
     * @throws jakarta.servlet.ServletException if an error occurs
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        // Note that there is no control exerted here to direct a
        // request to a particular theme or override a theme.
        // The assumption is that the rendered output has exerted that
        // control by obtaining a reference to the appropriate resource
        // and rendering that reference. This servlet just has the
        // opportunity of setting up the theme ASAP, before any
        // pages are rendered, and for sharing resources. The context
        // of this servlet is known by several applications or possibly
        // given to the Theme subsystem by the console. Then the
        // theme subsystem returns actual references to the appropriate
        // resource based on the "theme context", i.e. overrides etc.
        //
        // Consider the Console as implementing ThemeContext. by the
        // time this servlet's init method has been called the
        // Console will have "installed" an appropriate ThemeContext
        // instance. The call to getInstance will return that ThemeContext
        // instance and configure the ThemeFactory with it.
        //
        // Actually there is no need to set up the ThemeContext here.
        // The idea is that the application framework will have
        // implemented an "XXXThemeContext" which will have been
        // created to set up the Theme environment as necesary.
        // If fact doing this here in a JSF environment may not
        // provide enough information like the context URL which
        // doesn't appear to be available at this point.
        //
        // Forget this now and assume that JSFServletContext is
        // available.
        //
        // ThemeContext themeContext = ServletThemeContext.getInstance(
        //      getServletContext());
    }

    /**
     * <p>
     * Return the timestamp for when resources provided by this servlet were
     * last modified. By default, this will be the timestamp when this servlet
     * was first loaded at the deployment of the containing web-app, so that any
     * changes in the resources will be automatically sent to the clients who
     * might have cached earlier versions.</p>
     *
     * @param request The HttpServletRequest being processed
     * @return The date when the resource was last modified
     */
    @Override
    public long getLastModified(final HttpServletRequest request) {
        return this.lastModified;
    }
}
