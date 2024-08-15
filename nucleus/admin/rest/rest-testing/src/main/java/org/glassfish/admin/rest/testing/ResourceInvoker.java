/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.testing;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

public abstract class ResourceInvoker {

    public static final String METHOD_OPTIONS = "options";
    public static final String METHOD_GET = "get";
    public static final String METHOD_POST = "post";
    public static final String METHOD_PUT = "put";
    public static final String METHOD_DELETE = "delete";
    private final Environment env;
    private String password = null;
    private String uri;
    private Map<String, String> queryParams = new HashMap<String, String>();
    private String url = null;
    private String baseUrl = null;
    private String protocol = null;
    private String host = null;
    private String port = null;
    private String username = null;
    private JSONObject body = new JSONObject();

    protected ResourceInvoker(Environment env) {
        this.env = env;
    }

    protected Environment getEnvironment() {
        return env;
    }

    protected abstract String getContextRoot();

    protected abstract String getResourceRoot();

    protected abstract String getMediaType();

    protected String getRequestBodyMediaType() {
        return getMediaType();
    }

    protected String getResponseBodyMediaType() {
        return getMediaType();
    }

    protected String getUrl() {
        return (this.url == null) ? getBaseUrl() + "/" + getUri() : this.url;
    }

    public ResourceInvoker url(String val) {
        this.url = val;
        return this;
    }

    protected String getBaseUrl() {
        if (this.baseUrl != null) {
            return this.baseUrl;
        }
        return getProtocol() + "://" + getHost() + ":" + getPort() + "/" + getContextRoot() + "/" + getResourceRoot();
    }

    public ResourceInvoker baseUrl(String val) {
        this.baseUrl = val;
        return this;
    }

    protected String getProtocol() {
        return (this.protocol == null) ? getEnvironment().getProtocol() : this.protocol;
    }

    public ResourceInvoker protocol(String val) {
        this.protocol = val;
        return this;
    }

    protected String getHost() {
        return (this.host == null) ? getEnvironment().getHost() : this.host;
    }

    public ResourceInvoker host(String val) {
        this.host = val;
        return this;
    }

    protected String getPort() {
        return (this.port == null) ? getEnvironment().getPort() : this.port;
    }

    public ResourceInvoker port(String val) {
        this.port = val;
        return this;
    }

    protected String getUserName() {
        return (this.username == null) ? getEnvironment().getUserName() : this.username;
    }

    public ResourceInvoker username(String val) {
        this.username = val;
        return this;
    }

    protected String getPassword() {
        return (this.password == null) ? getEnvironment().getPassword() : this.password;
    }

    public ResourceInvoker password(String val) {
        this.password = val;
        return this;
    }

    protected String getUri() {
        return uri;
    }

    public ResourceInvoker uri(String val) {
        this.uri = val;
        return this;
    }

    protected Map<String, String> getQueryParams() {
        return this.queryParams;
    }

    public ResourceInvoker queryParam(String name, String value) {
        this.queryParams.put(name, value);
        return this;
    }

    protected JSONObject getBody() {
        return this.body;
    }

    public ResourceInvoker body(JSONObject val) {
        this.body = val;
        return this;
    }

    public ResourceInvoker body(ObjectValue val) throws Exception {
        return body(val.toJSONObject());
    }

    public Response options() throws Exception {
        return wrapResponse(METHOD_OPTIONS, getInvocationBuilder().options());
    }

    public Response get() throws Exception {
        return wrapResponse(METHOD_GET, getInvocationBuilder().get());
    }

    public Response post() throws Exception {
        return wrapResponse(METHOD_POST, getInvocationBuilder().post(getRequestBody()));
    }

    public Response put() throws Exception {
        return wrapResponse(METHOD_PUT, getInvocationBuilder().put(getRequestBody()));
    }

    public Response delete() throws Exception {
        return wrapResponse(METHOD_DELETE, getInvocationBuilder().delete());
    }

    private Builder getInvocationBuilder() throws Exception {
        Client client = customizeClient(ClientBuilder.newClient());
        client.register(HttpAuthenticationFeature.basic(getUserName(), getPassword()));
        WebTarget target = client.target(getUrl());
        for (Map.Entry<String, String> e : getQueryParams().entrySet()) {
            target = target.queryParam(e.getKey(), e.getValue());
        }
        return target.request(getResponseBodyMediaType()).header("X-Include-Resource-Links", "true").header("X-Requested-By", "MyClient");
    }

    protected Client customizeClient(Client client) {
        return client;
    }

    protected Response wrapResponse(String method, jakarta.ws.rs.core.Response response) throws Exception {
        return new Response(method, response);
    }

    private Entity getRequestBody() throws Exception {
        return Entity.entity(getBody().toString(), getRequestBodyMediaType());
    }
}
