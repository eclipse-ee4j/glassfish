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

package com.sun.enterprise.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.core.StandardPipeline;
import org.glassfish.common.util.InputValidationUtil;
import org.glassfish.grizzly.http.util.CharChunk;
import org.glassfish.web.LogFacade;

import static com.sun.logging.LogCleanerUtil.neutralizeForLog;

/**
 * Pipeline associated with a virtual server.
 *
 * This pipeline inherits the state (off/disabled) of its associated
 * virtual server, and will abort execution and return an appropriate response
 * error code if its associated virtual server is off or disabled.
 */
public class VirtualServerPipeline extends StandardPipeline {

    private static final Logger logger = LogFacade.getLogger();

    private static final ResourceBundle rb = logger.getResourceBundle();

    private VirtualServer vs;

    private boolean isOff;
    private boolean isDisabled;

    private ArrayList<RedirectParameters> redirects;

    private ConcurrentLinkedQueue<CharChunk> locations;

    /**
     * Constructor.
     *
     * @param vs Virtual server with which this VirtualServerPipeline is being
     * associated
     */
    public VirtualServerPipeline(VirtualServer vs) {
        super(vs);
        this.vs = vs;
        locations = new ConcurrentLinkedQueue<CharChunk>();
    }

    /**
     * Processes the specified request, and produces the appropriate
     * response, by invoking the first valve (if any) of this pipeline, or
     * the pipeline's basic valve.
     *
     * @param request The request to process
     * @param response The response to return
     */
    public void invoke(Request request, Response response)
            throws IOException, ServletException {

        if (isOff) {
            String msg = rb.getString(LogFacade.VS_VALVE_OFF);
            msg = MessageFormat.format(msg, new Object[] { vs.getName() });
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, neutralizeForLog(msg));
            }
            ((HttpServletResponse) response.getResponse()).sendError(
                                            HttpServletResponse.SC_NOT_FOUND,
                                            msg);
        } else if (isDisabled) {
            String msg = rb.getString(LogFacade.VS_VALVE_DISABLED);
            msg = MessageFormat.format(msg, new Object[] { vs.getName() });
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, neutralizeForLog(msg));
            }
            ((HttpServletResponse) response.getResponse()).sendError(
                                            HttpServletResponse.SC_FORBIDDEN,
                                            msg);
        } else {
            boolean redirect = false;
            if (redirects != null) {
                redirect = redirectIfNecessary(request, response);
            }
            if (!redirect) {
                super.invoke(request, response);
            }
        }
    }


    /**
     * Sets the <code>disabled</code> state of this VirtualServerPipeline.
     *
     * @param isDisabled true if the associated virtual server has been
     * disabled, false otherwise
     */
    void setIsDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }


    /**
     * Sets the <code>off</code> state of this VirtualServerPipeline.
     *
     * @param isOff true if the associated virtual server is <code>off</code>,
     * false otherwise
     */
    void setIsOff(boolean isOff) {
        this.isOff = isOff;
    }


    /**
     * Adds the given redirect instruction to this VirtualServerPipeline.
     *
     * @param from URI prefix to match
     * @param url Redirect URL to return to the client
     * @param urlPrefix New URL prefix to return to the client
     * @param escape true if redirect URL returned to the client is to be
     * escaped, false otherwise
     */
    void addRedirect(String from, String url, String urlPrefix,
                     boolean escape) {

        if (redirects == null) {
            redirects = new ArrayList<RedirectParameters>();
        }

        redirects.add(new RedirectParameters(from, url, urlPrefix, escape));
    }


    /**
     * @return true if this VirtualServerPipeline has any redirects
     * configured, and false otherwise.
     */
    boolean hasRedirects() {
        return ((redirects != null) && (redirects.size() > 0));
    }


    /**
     * Clears all redirects.
     */
    void clearRedirects() {
        if (redirects != null) {
            redirects.clear();
        }
    }


    /**
     * Checks to see if the given request needs to be redirected.
     *
     * @param request The request to process
     * @param response The response to return
     *
     * @return true if redirect has occurred, false otherwise
     */
    private boolean redirectIfNecessary(Request request, Response response)
            throws IOException {

        if (redirects == null) {
            return false;
        }

        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        HttpServletResponse hres = (HttpServletResponse) request.getResponse();
        String requestURI = hreq.getRequestURI();
        RedirectParameters redirectMatch = null;

        // Determine the longest 'from' URI prefix match
        int size = redirects.size();
        for (int i=0; i<size; i++) {
            RedirectParameters elem = redirects.get(i);
            String elemFromWithTrailingSlash = elem.from;
            if (!elemFromWithTrailingSlash.endsWith("/")) {
                elemFromWithTrailingSlash += "/";
            }
            if (requestURI.equals(elem.from) ||
                    requestURI.startsWith(elemFromWithTrailingSlash)) {
                if (redirectMatch != null) {
                    if (elem.from.length() > redirectMatch.from.length()) {
                        redirectMatch = elem;
                    }
                } else {
                    redirectMatch = elem;
                }
            }
        }

        if (redirectMatch != null) {
            // Redirect prefix match found, need to redirect
            String location = null;
            String uriSuffix = requestURI.substring(
                            redirectMatch.from.length());
            if ("/".equals(redirectMatch.from)) {
                uriSuffix = "/" + uriSuffix;
                // START 6810361
                if (redirectMatch.urlPrefixPath != null &&
                        uriSuffix.startsWith(redirectMatch.urlPrefixPath)) {
                    return false;
                }
                // END 6810361
            }
            // START 6810361
            // Implements welcome page only redirection
            if ("".equals(redirectMatch.from)) {
                if (!("/".equals(requestURI))) return false;
            }
            // END 6810361
            if (redirectMatch.urlPrefix != null) {
                // Replace 'from' URI prefix with URL prefix
                location = redirectMatch.urlPrefix + uriSuffix;
            } else {
                // Replace 'from' URI prefix with complete URL
                location = redirectMatch.url;
            }

            String queryString = hreq.getQueryString();
            if (queryString != null) {
                location += "?" + queryString;
            }

            CharChunk locationCC = null;

            if (redirectMatch.isEscape) {
                try {
                    URL url = new URL(location);
                    locationCC = locations.poll();
                    if (locationCC == null) {
                        locationCC = new CharChunk();
                    }
                    locationCC.append(url.getProtocol());
                    locationCC.append("://");
                    locationCC.append(url.getHost());
                    if (url.getPort() != -1) {
                        locationCC.append(":");
                        locationCC.append(String.valueOf(url.getPort()));
                    }
                    locationCC.append(response.encode(url.getPath()));
                    if (queryString != null) {
                        locationCC.append("?");
                        locationCC.append(url.getQuery());
                    }
                    location = locationCC.toString();
                } catch (MalformedURLException mue) {
                    if (redirectMatch.validURI) {
                        logger.log(Level.WARNING,
                            LogFacade.INVALID_REDIRECTION_LOCATION,
                                neutralizeForLog(location));
                    } else {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE,
                                LogFacade.INVALID_REDIRECTION_LOCATION,
                                    neutralizeForLog(location));
                        }
                    }
                } finally {
                    if (locationCC != null) {
                        locationCC.recycle();
                        locations.offer(locationCC);
                    }
                }
            }

            // Validate the URL for extra spaces before redirection.
            if(InputValidationUtil.validateStringforCRLF(location)) {
                hres.sendError(403, "Forbidden");
            } else {
                hres.sendRedirect(InputValidationUtil.removeLinearWhiteSpaces(location));
            }
            return true;
        }

        return false;
    }

    /**
     * Class representing redirect parameters
     */
    static class RedirectParameters {

        private String from;

        private String url;

        private String urlPrefix;

        // START 6810361
        /*
         * The path portion of the urlPrefix, in case urlPrefix is
         * specified as an absolute URL (including protocol etc.)
         */
        private String urlPrefixPath;
        // END 6810361

        private boolean validURI;

        private boolean isEscape;

        RedirectParameters(String from, String url, String urlPrefix,
                           boolean isEscape) {
            this.from = from;
            this.url = url;
            this.urlPrefix = urlPrefix;
            this.isEscape = isEscape;
            this.validURI = true;

            // START 6810361
            try {
                URL u = new URL(urlPrefix);
                urlPrefixPath = u.getPath();
            } catch (MalformedURLException e) {
                urlPrefixPath = urlPrefix;
                this.validURI = false;
            }
            // END 6810361
        }
    }

}
