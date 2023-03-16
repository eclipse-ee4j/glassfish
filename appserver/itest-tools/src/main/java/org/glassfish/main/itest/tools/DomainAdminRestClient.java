/*
 * Copyright (c) 2022, 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.itest.tools;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.io.Closeable;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.glassfish.admin.rest.client.ClientWrapper;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import static jakarta.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.createClient;

/**
 * @author David Matejcek
 */
public class DomainAdminRestClient implements Closeable {

    private final ClientWrapper client;
    private final String baseUrl;
    private final String responseType;

    public DomainAdminRestClient(final String baseUrl) {
        this(baseUrl, APPLICATION_JSON);
    }

    public DomainAdminRestClient(final String baseUrl, final String responseType) {
        this(createClient(), baseUrl, responseType);
    }


    public DomainAdminRestClient(final ClientWrapper client, final String baseUrl, final String responseType) {
        this.client = client;
        this.baseUrl = baseUrl;
        this.responseType = responseType;
    }


    /**
     * @return {@code http://localhost:4848/management} or something else, see constructor.
     */
    public final String getBaseUrl() {
        return baseUrl;
    }


    public Response options(final String relativePath) {
        return getRequestBuilder(relativePath).options(Response.class);
    }


    public Response get(final String relativePath) {
        return get(relativePath, null);
    }


    public Response get(final String relativePath, final Map<String, String> queryParams) {
        final WebTarget target = getTarget(relativePath, queryParams);
        return target.request(responseType).get(Response.class);
    }

    public <T> Response put(final String relativePath, final Entity<T> entityPayload) {
        return getRequestBuilder(relativePath).put(entityPayload, Response.class);
    }


    public Response post(final String relativePath) {
        return getRequestBuilder(relativePath).post(Entity.entity(null, APPLICATION_FORM_URLENCODED), Response.class);
    }


    public Response post(final String relativePath, final Map<String, String> payload) {
        return post(relativePath, buildMultivaluedMap(payload));
    }


    public Response post(final String relativePath, final MultivaluedMap<String, String> payload) {
        return getRequestBuilder(relativePath).post(Entity.entity(payload, APPLICATION_FORM_URLENCODED), Response.class);
    }


    public <T> Response post(final String relativePath, final Entity<T> entityPayload) {
        return getRequestBuilder(relativePath).post(entityPayload, Response.class);
    }

    public Response postWithUpload(final String relativePath, final Map<String, Object> payload) {
        final FormDataMultiPart form = new FormDataMultiPart();
        for (final Map.Entry<String, Object> entry : payload.entrySet()) {
            if (entry.getValue() instanceof File) {
                form.getBodyParts().add((new FileDataBodyPart(entry.getKey(), (File) entry.getValue())));
            } else {
                form.field(entry.getKey(), entry.getValue(), TEXT_PLAIN_TYPE);
            }
        }
        return getRequestBuilder(relativePath).post(Entity.entity(form, MULTIPART_FORM_DATA), Response.class);
    }


    public Response delete(final String relativePath) {
        return delete(relativePath, null);
    }

    public Response delete(final String relativePath, final Map<String, String> queryParams) {
        final WebTarget target = getTarget(relativePath, queryParams);
        return target.request(responseType).delete(Response.class);
    }


    public Builder getRequestBuilder(final String relativePath) {
        return getRequestBuilder(relativePath, null);
    }

    public Builder getRequestBuilder(final  String relativePath, Map<String, String> queryParams) {
        return getTarget(relativePath, queryParams).request(responseType);
    }

    public WebTarget getTarget(final String relativePath, final Map<String, String> queryParams) {
        WebTarget target = client.target(baseUrl + relativePath);
        if (queryParams == null) {
            return target;
        }
        for (final Map.Entry<String, String> entry : queryParams.entrySet()) {
            target = target.queryParam(entry.getKey(), entry.getValue());
        }
        return target;
    }


    @Override
    public void close() {
        client.close();
    }


    private MultivaluedMap<String, String> buildMultivaluedMap(final Map<String, String> payload) {
        final MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        if (payload != null) {
            for (final Entry<String, String> entry : payload.entrySet()) {
                formData.add(entry.getKey(), entry.getValue());
            }
        }
        return formData;
    }
}
