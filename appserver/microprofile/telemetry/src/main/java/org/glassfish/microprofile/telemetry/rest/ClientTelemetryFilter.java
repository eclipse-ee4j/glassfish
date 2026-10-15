/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.glassfish.microprofile.telemetry.rest;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;

import java.net.URI;
import java.net.URISyntaxException;

import org.glassfish.jersey.client.spi.InvocationBuilderListener;
import org.glassfish.jersey.client.spi.PostInvocationInterceptor;
import org.glassfish.microprofile.telemetry.TelemetryInstances;

import static io.opentelemetry.semconv.ErrorAttributes.ERROR_TYPE;
import static io.opentelemetry.semconv.HttpAttributes.HTTP_REQUEST_METHOD;
import static io.opentelemetry.semconv.HttpAttributes.HTTP_RESPONSE_STATUS_CODE;
import static io.opentelemetry.semconv.ServerAttributes.SERVER_ADDRESS;
import static io.opentelemetry.semconv.ServerAttributes.SERVER_PORT;
import static io.opentelemetry.semconv.UrlAttributes.URL_FULL;
import static org.glassfish.microprofile.telemetry.TelemetryInstances.INSTRUMENTATION_NAME;

/**
 * Creates a client span for every Jakarta REST client and MicroProfile REST Client request
 * and propagates the context in the request headers.
 * <p>
 * Request filters of asynchronous invocations run on a client thread, so the context of the
 * caller is captured when the invocation builder is created and used as the parent.
 */
@Priority(Priorities.USER + 1000)
final class ClientTelemetryFilter
    implements ClientRequestFilter, ClientResponseFilter, PostInvocationInterceptor, InvocationBuilderListener {

    private static final String PARENT_CONTEXT = ClientTelemetryFilter.class.getName() + ".parent";
    private static final String SPAN = ClientTelemetryFilter.class.getName() + ".span";

    private static final TextMapSetter<ClientRequestContext> HEADERS =
        (request, key, value) -> request.getHeaders().putSingle(key, value);

    private final ClassLoader applicationClassLoader;

    ClientTelemetryFilter(ClassLoader applicationClassLoader) {
        this.applicationClassLoader = applicationClassLoader;
    }

    @Override
    public void onNewBuilder(InvocationBuilderContext context) {
        context.property(PARENT_CONTEXT, Context.current());
    }

    @Override
    public void filter(ClientRequestContext request) {
        OpenTelemetry openTelemetry = TelemetryInstances.forClassLoader(applicationClassLoader);
        if (!TelemetryInstances.isEnabled(openTelemetry)) {
            return;
        }
        Context parent = request.getProperty(PARENT_CONTEXT) instanceof Context captured
            ? captured
            : Context.current();
        URI uri = request.getUri();
        String method = request.getMethod();
        SpanBuilder builder = openTelemetry.getTracer(INSTRUMENTATION_NAME)
            .spanBuilder(method)
            .setParent(parent)
            .setSpanKind(SpanKind.CLIENT)
            .setAttribute(HTTP_REQUEST_METHOD, method)
            .setAttribute(URL_FULL, withoutUserInfo(uri).toString());
        if (uri.getHost() != null) {
            builder.setAttribute(SERVER_ADDRESS, uri.getHost());
        }
        int port = port(uri);
        if (port > 0) {
            builder.setAttribute(SERVER_PORT, (long) port);
        }
        Span span = builder.startSpan();
        request.setProperty(SPAN, span);
        openTelemetry.getPropagators().getTextMapPropagator().inject(parent.with(span), request, HEADERS);
    }

    @Override
    public void filter(ClientRequestContext request, ClientResponseContext response) {
        Span span = removeSpan(request);
        if (span == null) {
            return;
        }
        int status = response.getStatus();
        span.setAttribute(HTTP_RESPONSE_STATUS_CODE, (long) status);
        if (status >= 400) {
            span.setAttribute(ERROR_TYPE, Integer.toString(status));
            span.setStatus(StatusCode.ERROR);
        }
        span.end();
    }

    @Override
    public void afterRequest(ClientRequestContext request, ClientResponseContext response) {
    }

    @Override
    public void onException(ClientRequestContext request, ExceptionContext context) {
        Span span = removeSpan(request);
        if (span == null) {
            return;
        }
        Throwable exception = context.getThrowables().peekFirst();
        span.setStatus(StatusCode.ERROR);
        if (exception != null) {
            span.setAttribute(ERROR_TYPE, exception.getClass().getName());
            span.recordException(exception);
        }
        span.end();
    }

    private static Span removeSpan(ClientRequestContext request) {
        if (request.getProperty(SPAN) instanceof Span span) {
            request.removeProperty(SPAN);
            return span;
        }
        return null;
    }

    private static int port(URI uri) {
        if (uri.getPort() > 0) {
            return uri.getPort();
        }
        if ("https".equalsIgnoreCase(uri.getScheme())) {
            return 443;
        }
        if ("http".equalsIgnoreCase(uri.getScheme())) {
            return 80;
        }
        return -1;
    }

    private static URI withoutUserInfo(URI uri) {
        if (uri.getRawUserInfo() == null) {
            return uri;
        }
        try {
            return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(),
                uri.getFragment());
        } catch (URISyntaxException e) {
            return uri;
        }
    }
}
