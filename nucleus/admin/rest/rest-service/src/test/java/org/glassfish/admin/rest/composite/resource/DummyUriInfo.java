/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.composite.resource;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;
import jakarta.ws.rs.core.UriInfo;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This Dummy class is meant to do the bare minimum to make the tests pass. It is not meant as a full-featured implementation
 * of <code>UriInfo</code>.  Attempts to use it as such will likely fail.
 * @author jdlee
 */
public class DummyUriInfo implements UriInfo {

    @Override
    public String getPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPath(boolean decode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<PathSegment> getPathSegments() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<PathSegment> getPathSegments(boolean decode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public URI getRequestUri() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        return new UriBuilderImpl();
    }

    @Override
    public URI getAbsolutePath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
        return new UriBuilderImpl();
    }

    @Override
    public URI getBaseUri() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
        return new UriBuilderImpl();
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getMatchedURIs() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getMatchedURIs(boolean decode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Object> getMatchedResources() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public URI resolve(URI uri) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public URI relativize(URI uri) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class UriBuilderImpl extends UriBuilder {

        public UriBuilderImpl() {
        }

        @Override
        public UriBuilder clone() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder uri(URI uri) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder uri(String uriTemplate) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder scheme(String scheme) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder schemeSpecificPart(String ssp) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder userInfo(String ui) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder host(String host) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder port(int port) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder replacePath(String path) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder path(String path) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder path(Class type) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder path(Class type, String string) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder path(Method method) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder segment(String... segments) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder replaceMatrix(String matrix) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder matrixParam(String name, Object... values) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder replaceMatrixParam(String name, Object... values) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder replaceQuery(String query) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder queryParam(String name, Object... values) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder replaceQueryParam(String name, Object... values) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder fragment(String fragment) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public URI buildFromMap(Map<String, ? extends Object> values) throws IllegalArgumentException, UriBuilderException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public URI buildFromEncodedMap(Map<String, ? extends Object> values) throws IllegalArgumentException, UriBuilderException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public URI build(Object... values) throws IllegalArgumentException, UriBuilderException {
            URI uri = null;
            try {
                uri = new URI("");
            } catch (URISyntaxException ex) {
                Logger.getLogger(DummiesResource.class.getName()).log(Level.SEVERE, null, ex);
            }
            return uri;
        }

        @Override
        public URI buildFromEncoded(Object... values) throws IllegalArgumentException, UriBuilderException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public URI buildFromMap(Map<String, ?> values, boolean encodeSlashInPath) throws IllegalArgumentException, UriBuilderException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public URI build(Object[] values, boolean encodeSlashInPath) throws IllegalArgumentException, UriBuilderException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String toTemplate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder resolveTemplate(String name, Object value) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder resolveTemplate(String name, Object value, boolean encodeSlashInPath) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder resolveTemplateFromEncoded(String name, Object value) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder resolveTemplates(Map<String, Object> templateValues) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UriBuilder resolveTemplatesFromEncoded(Map<String, Object> templateValues) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public String getMatchedResourceTemplate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
