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

package org.glassfish.embeddable.web.config;

import java.util.Set;

import org.glassfish.embeddable.GlassFishException;

/**
 * This class represents a list of URL patterns and HTTP
 * methods that describe a set of Web resources to be protected.
 *
 * <p/> Usage example:
 *
 * <pre>
 *      WebResourceCollection webResource = new WebResourceCollection();
 *      webResource.setName("protected");
 *      webResource.setUrlPatterns("/*");
 *      Set<String> httpMethods = new HashSet<String>();
 *      httpMethods.add("GET");
 *      httpMethods.add("POST");
 *      webResource.setHttpMethods(httpMethods);
 * </pre>
 *
 * @see SecurityConfig
 *
 * @author Rajiv Mordani
 * @author Amy Roh
 */
public class WebResourceCollection {

    private String name;
    private Set<String> urlPatterns;
    private Set<String> httpMethods;
    private Set<String> httpMethodOmissions;

    /**
     * Sets the name of this collection
     *
     * @param name the name of this collection
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of this collection
     *
     * @return the name of this collection
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the url patterns that correspond to this
     * web resource
     *
     * @param urlPatterns the url patterns
     */
    public void setUrlPatterns(Set<String> urlPatterns) {
        this.urlPatterns = urlPatterns;
    }

    /**
     * Gets the url patterns that correspond to this
     * web resource
     *
     * @return the url patterns
     */
    public Set<String> getUrlPatterns() {
        return urlPatterns;
    }

    /**
     * Sets the HTTP methods that should be protected
     *
     * @param httpMethods the HTTP methods
     *
     * @throws GlassFishException if HttpMethodOmissions is already defined
     */
    public void setHttpMethods(Set<String> httpMethods)
            throws GlassFishException {
        if (httpMethodOmissions != null) {
            throw new GlassFishException(
                    "Invalid content was found starting with element 'http-method'. " +
                            "One of 'http-method' or 'http-method-omission' is expected.");
        }
        this.httpMethods = httpMethods;
    }

    /**
     * Gets the HTTP methods that should be protected
     *
     * @return the HTTP methods
     */
    public Set<String> getHttpMethods() {
        return httpMethods;
    }

    /**
     * Sets the HTTP methods to be omitted from protection
     *
     * @param httpMethodOmissions the HTTP methods to be
     * omitted from protection
     *
     * @throws GlassFishException if HttpMethods is already defined
     */
    public void setHttpMethodOmissions(Set<String> httpMethodOmissions)
            throws GlassFishException {
        if (httpMethods != null) {
            throw new GlassFishException(
                    "Invalid content was found starting with element 'http-method-omission'. " +
                            "One of 'http-method' or 'http-method-omission' is expected.");
        }
        this.httpMethodOmissions = httpMethodOmissions;
    }

    /**
     * Gets the HTTP methods to be omitted from protection
     *
     * @return the HTTP methods to be omitted from protection
     */
    public Set<String> getHttpMethodOmissions() {
        return httpMethodOmissions;
    }

    /**
     * Returns a formatted string of the state.
     */
    public String toString() {
        StringBuffer toStringBuffer = new StringBuffer();
        toStringBuffer.append("WebResourceCollection: ");
        toStringBuffer.append(" name: ").append(name);
        toStringBuffer.append(" urlPatterns: ").append(urlPatterns);
        toStringBuffer.append(" httpMethods ").append(httpMethods);
        toStringBuffer.append(" httpMethodOmissions ").append(httpMethodOmissions);
        return toStringBuffer.toString();
    }
}
