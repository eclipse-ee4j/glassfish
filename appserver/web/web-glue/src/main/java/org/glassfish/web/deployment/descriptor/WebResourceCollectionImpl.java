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

package org.glassfish.web.deployment.descriptor;

import com.sun.enterprise.deployment.web.WebResourceCollection;

import java.util.HashSet;
import java.util.Set;

import org.glassfish.deployment.common.Descriptor;

/**
 * This descriptor represents a description of a portion of a web app
 * in terms of a collection of url patterns and
 * a collection of http methods on this patterns.
 *
 *@author Danny Coward
 */


public class WebResourceCollectionImpl extends Descriptor
                implements WebResourceCollection
{
    private Set<String> urlPatterns;
    private Set<String> httpMethods;
    private Set<String> httpMethodOmissions;

    public WebResourceCollectionImpl() {
    }

    public WebResourceCollectionImpl(WebResourceCollectionImpl other) {
        if (other.urlPatterns != null) {
            this.urlPatterns = new HashSet<String>(other.urlPatterns);
        }
        if (other.httpMethods != null) {
            this.httpMethods = new HashSet<String>(other.httpMethods);
        }
        if (other.httpMethodOmissions != null) {
            this.httpMethodOmissions = new HashSet<String>(other.httpMethodOmissions);
        }
    }

    /**
     * Return my urls patterns (String objects)
     * @return the set of the url patterns.
     */
    public Set<String> getUrlPatterns() {
        if (this.urlPatterns == null) {
            this.urlPatterns = new HashSet<String>();
        }
        return this.urlPatterns;
    }

    /**
     * Add a URL pattern to this collection.
     * @param the url pattern to be added.
     */
    public void addUrlPattern(String urlPattern) {
        this.getUrlPatterns().add(urlPattern);
    }

    /**
     * Remove the specified url pattern from the collection.
     * @param the url pattern to be removed.
     */
    public void removeUrlPattern(String urlPattern) {
        this.getUrlPatterns().remove(urlPattern);
    }

    /**
     * Clean out the collection of URL pattern and replace
     * it with the given Set of (String) url patterns.
     * @param the url patterns to replace the current set.
     */
    public void setUrlPatterns(Set<String> urlPatterns) {
        this.urlPatterns = urlPatterns;
    }

    /**
     * Return the enumeration of HTTP methods this collection has.
     * @return the enumeration of HTTP methods.
     */
    public Set<String> getHttpMethods() {
        if (this.httpMethods == null) {
            this.httpMethods = new HashSet<String>();
        }
        return this.httpMethods;
    }

    /**
     * Returns the HttpMethods this collection has in an array of strings
     * This is added to speed up processing while creating webresource permissions
     * @return array of strings of HttpMethods
     */
    public String[] getHttpMethodsAsArray(){
        if (httpMethods == null){
            return (String[]) null;
        }
        return httpMethods.toArray(new String[httpMethods.size()]);
    }

    /**
     * Sets the set of HTTP methods this collection has.
     * @param the set of HTTP methods.
     */
    public void setHttpMethods(Set<String> httpMethods) {
        this.httpMethods = httpMethods;
    }

    /**
     * Adds the given HTTP method to the collection of http methods this
     * collection has.
     * @param the HTTP method to be added.
     */
    public void addHttpMethod(String httpMethod) {
        this.getHttpMethods().add(httpMethod);
    }

    /**
     * Removes the given HTTP method from the collection of http methods.
     * @param the HTTP method to be removed.
     */
    public void removeHttpMethod(String httpMethod) {
        this.getHttpMethods().remove(httpMethod);
    }

    /**
     * Return the set of HTTP method omissions this collection has.
     * @return the set of HTTP method omissions.
     */
    public Set<String> getHttpMethodOmissions() {
        if (this.httpMethodOmissions == null) {
            this.httpMethodOmissions = new HashSet<String>();
        }
        return this.httpMethodOmissions;
    }

    /**
     * Returns the HttpMethodOmissions this collection has in an array of strings
     * This is added to speed up processing while creating webresource permissions
     * @return array of strings of HttpMethodOmissions
     */
    public String[] getHttpMethodOmissionsAsArray(){
        if (httpMethodOmissions == null){
            return (String[]) null;
        }
        return httpMethodOmissions.toArray(new String[httpMethodOmissions.size()]);
    }
    /**
     * Sets the set of HTTP method omissions this collection has.
     * @param the set of HTTP method omissions.
     */
    public void setHttpMethodOmissions(Set<String> httpMethodOmissions) {
        this.httpMethodOmissions = httpMethodOmissions;
    }

    /**
     * Adds the given HTTP method omission to the collection of http methods this
     * collection has.
     * @param the HTTP method to be added.
     */
    public void addHttpMethodOmission(String httpMethodOmission) {
        this.getHttpMethodOmissions().add(httpMethodOmission);
    }

    /**
     * Removes the given HTTP method omission from the collection of http methods.
     * @param the HTTP method to be removed.
     */
    public void removeHttpMethodOmission(String httpMethodOmission) {
        this.getHttpMethodOmissions().remove(httpMethodOmission);
    }

    /**
     * A formatted string of the state.
     */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("WebresourceCollection: ");
        toStringBuffer.append(" urlPatterns: ").append(this.urlPatterns);
        toStringBuffer.append(" httpMethods ").append(this.httpMethods);
        toStringBuffer.append(" httpMethodOmissions ").append(this.httpMethodOmissions);
    }
}

