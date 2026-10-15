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
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;

import jakarta.ws.rs.core.HttpHeaders;

import java.net.URI;
import java.util.List;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.glassfish.jersey.uri.UriTemplate;
import org.glassfish.microprofile.telemetry.TelemetryInstances;

import static io.opentelemetry.semconv.ErrorAttributes.ERROR_TYPE;
import static io.opentelemetry.semconv.HttpAttributes.HTTP_REQUEST_METHOD;
import static io.opentelemetry.semconv.HttpAttributes.HTTP_RESPONSE_STATUS_CODE;
import static io.opentelemetry.semconv.HttpAttributes.HTTP_ROUTE;
import static io.opentelemetry.semconv.ServerAttributes.SERVER_ADDRESS;
import static io.opentelemetry.semconv.ServerAttributes.SERVER_PORT;
import static io.opentelemetry.semconv.UrlAttributes.URL_PATH;
import static io.opentelemetry.semconv.UrlAttributes.URL_QUERY;
import static io.opentelemetry.semconv.UrlAttributes.URL_SCHEME;
import static io.opentelemetry.semconv.UserAgentAttributes.USER_AGENT_ORIGINAL;
import static org.glassfish.microprofile.telemetry.TelemetryInstances.INSTRUMENTATION_NAME;

/**
 * Creates a server span and records the {@code http.server.request.duration} metric for every
 * matched Jakarta REST request.
 * <p>
 * The span becomes current when the request is matched and stays current until the resource
 * method returns, so that it is the parent of spans created by the resource. It ends when the
 * response is complete, which for asynchronous resources happens on another thread.
 */
final class ServerTelemetryListener implements ApplicationEventListener {

    private static final double NANOS_PER_SECOND = 1_000_000_000d;

    private static final List<Double> DURATION_BUCKETS = List.of(0.005, 0.01, 0.025, 0.05, 0.075, 0.1, 0.25, 0.5,
        0.75, 1.0, 2.5, 5.0, 7.5, 10.0);

    private static final TextMapGetter<ContainerRequest> HEADERS = new TextMapGetter<>() {

        @Override
        public Iterable<String> keys(ContainerRequest request) {
            return request.getHeaders().keySet();
        }

        @Override
        public String get(ContainerRequest request, String key) {
            return request == null ? null : request.getHeaderString(key);
        }
    };

    private final ClassLoader applicationClassLoader;
    private volatile Instruments instruments;

    ServerTelemetryListener(ClassLoader applicationClassLoader) {
        this.applicationClassLoader = applicationClassLoader;
    }

    @Override
    public void onEvent(ApplicationEvent event) {
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        OpenTelemetry openTelemetry = TelemetryInstances.forClassLoader(applicationClassLoader);
        if (!TelemetryInstances.isEnabled(openTelemetry)) {
            return null;
        }
        return new ServerRequestTelemetry(getInstruments(openTelemetry));
    }

    private Instruments getInstruments(OpenTelemetry openTelemetry) {
        Instruments current = instruments;
        if (current == null || current.openTelemetry != openTelemetry) {
            current = new Instruments(openTelemetry);
            instruments = current;
        }
        return current;
    }

    /**
     * Route of the request: the base URI path followed by the matched resource templates.
     */
    static String route(String basePath, List<UriTemplate> matchedTemplates) {
        StringBuilder route = new StringBuilder(basePath == null ? "" : basePath);
        // Templates are ordered from the most specific one to the root resource
        for (int i = matchedTemplates.size() - 1; i >= 0; i--) {
            appendPath(route, matchedTemplates.get(i).getTemplate());
        }
        if (route.isEmpty()) {
            route.append('/');
        }
        return route.toString();
    }

    private static void appendPath(StringBuilder route, String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return;
        }
        boolean endsWithSlash = !route.isEmpty() && route.charAt(route.length() - 1) == '/';
        boolean startsWithSlash = path.charAt(0) == '/';
        if (endsWithSlash && startsWithSlash) {
            route.append(path, 1, path.length());
        } else if (endsWithSlash || startsWithSlash) {
            route.append(path);
        } else {
            route.append('/').append(path);
        }
    }

    private static final class Instruments {

        private final OpenTelemetry openTelemetry;
        private final DoubleHistogram requestDuration;

        Instruments(OpenTelemetry openTelemetry) {
            this.openTelemetry = openTelemetry;
            this.requestDuration = openTelemetry.getMeter(INSTRUMENTATION_NAME)
                .histogramBuilder("http.server.request.duration")
                .setUnit("s")
                .setDescription("Duration of HTTP server requests.")
                .setExplicitBucketBoundariesAdvice(DURATION_BUCKETS)
                .build();
        }
    }

    private static final class ServerRequestTelemetry implements RequestEventListener {

        private final Instruments instruments;

        private Span span;
        private Scope scope;
        private Thread scopeThread;
        private long startNanos;
        private String method;
        private String scheme;
        private String route;

        ServerRequestTelemetry(Instruments instruments) {
            this.instruments = instruments;
        }

        @Override
        public void onEvent(RequestEvent event) {
            switch (event.getType()) {
                case REQUEST_MATCHED -> start(event);
                case RESOURCE_METHOD_FINISHED -> closeScope();
                case FINISHED -> finish(event);
                default -> {
                }
            }
        }

        private void start(RequestEvent event) {
            if (span != null) {
                return;
            }
            ContainerRequest request = event.getContainerRequest();
            ExtendedUriInfo uriInfo = event.getUriInfo();
            OpenTelemetry openTelemetry = instruments.openTelemetry;
            Context parent = openTelemetry.getPropagators().getTextMapPropagator()
                .extract(Context.current(), request, HEADERS);

            URI requestUri = request.getRequestUri();
            method = request.getMethod();
            scheme = requestUri.getScheme();
            route = route(request.getBaseUri().getRawPath(), uriInfo.getMatchedTemplates());

            SpanBuilder builder = openTelemetry.getTracer(INSTRUMENTATION_NAME)
                .spanBuilder(method + " " + route)
                .setParent(parent)
                .setSpanKind(SpanKind.SERVER)
                .setAttribute(HTTP_REQUEST_METHOD, method)
                .setAttribute(HTTP_ROUTE, route)
                .setAttribute(URL_SCHEME, scheme)
                .setAttribute(URL_PATH, requestUri.getRawPath());
            if (requestUri.getRawQuery() != null) {
                builder.setAttribute(URL_QUERY, requestUri.getRawQuery());
            }
            if (requestUri.getHost() != null) {
                builder.setAttribute(SERVER_ADDRESS, requestUri.getHost());
            }
            if (requestUri.getPort() > 0) {
                builder.setAttribute(SERVER_PORT, (long) requestUri.getPort());
            }
            String userAgent = request.getHeaderString(HttpHeaders.USER_AGENT);
            if (userAgent != null) {
                builder.setAttribute(USER_AGENT_ORIGINAL, userAgent);
            }

            startNanos = System.nanoTime();
            span = builder.startSpan();
            scope = parent.with(span).makeCurrent();
            scopeThread = Thread.currentThread();
        }

        private void closeScope() {
            // A scope must be closed by the thread which opened it
            if (scope != null && scopeThread == Thread.currentThread()) {
                scope.close();
                scope = null;
                scopeThread = null;
            }
        }

        private void finish(RequestEvent event) {
            if (span == null) {
                return;
            }
            closeScope();

            ContainerResponse response = event.getContainerResponse();
            Throwable exception = event.getException();
            int status = response == null ? 500 : response.getStatus();

            String errorType = null;
            if (status >= 500) {
                errorType = Integer.toString(status);
            }
            if (exception != null && response == null) {
                errorType = exception.getClass().getName();
            }

            span.setAttribute(HTTP_RESPONSE_STATUS_CODE, (long) status);
            if (errorType != null) {
                span.setAttribute(ERROR_TYPE, errorType);
                span.setStatus(StatusCode.ERROR);
            }
            if (exception != null) {
                span.recordException(exception);
            }
            span.end();

            AttributesBuilder attributes = Attributes.builder()
                .put(HTTP_REQUEST_METHOD, method)
                .put(URL_SCHEME, scheme)
                .put(HTTP_ROUTE, route)
                .put(HTTP_RESPONSE_STATUS_CODE, (long) status);
            if (errorType != null) {
                attributes.put(ERROR_TYPE, errorType);
            }
            double seconds = (System.nanoTime() - startNanos) / NANOS_PER_SECOND;
            instruments.requestDuration.record(seconds, attributes.build(), Context.root().with(span));
            span = null;
        }
    }
}
