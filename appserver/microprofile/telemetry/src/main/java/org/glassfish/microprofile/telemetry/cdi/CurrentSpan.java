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
package org.glassfish.microprofile.telemetry.cdi;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;

import java.util.concurrent.TimeUnit;

/**
 * The injectable {@link Span}: every call goes to the span current at the time of the call,
 * as required by the specification ({@code @Inject Span} behaves like {@link Span#current()}).
 */
final class CurrentSpan implements Span {

    static final CurrentSpan INSTANCE = new CurrentSpan();

    private CurrentSpan() {
    }

    @Override
    public <T> Span setAttribute(AttributeKey<T> key, T value) {
        Span.current().setAttribute(key, value);
        return this;
    }

    @Override
    public Span addEvent(String name, Attributes attributes) {
        Span.current().addEvent(name, attributes);
        return this;
    }

    @Override
    public Span addEvent(String name, Attributes attributes, long timestamp, TimeUnit unit) {
        Span.current().addEvent(name, attributes, timestamp, unit);
        return this;
    }

    @Override
    public Span setStatus(StatusCode statusCode, String description) {
        Span.current().setStatus(statusCode, description);
        return this;
    }

    @Override
    public Span recordException(Throwable exception, Attributes additionalAttributes) {
        Span.current().recordException(exception, additionalAttributes);
        return this;
    }

    @Override
    public Span updateName(String name) {
        Span.current().updateName(name);
        return this;
    }

    @Override
    public Span addLink(SpanContext spanContext) {
        Span.current().addLink(spanContext);
        return this;
    }

    @Override
    public Span addLink(SpanContext spanContext, Attributes attributes) {
        Span.current().addLink(spanContext, attributes);
        return this;
    }

    @Override
    public void end() {
        Span.current().end();
    }

    @Override
    public void end(long timestamp, TimeUnit unit) {
        Span.current().end(timestamp, unit);
    }

    @Override
    public SpanContext getSpanContext() {
        return Span.current().getSpanContext();
    }

    @Override
    public boolean isRecording() {
        return Span.current().isRecording();
    }

    @Override
    public Context storeInContext(Context context) {
        return Span.current().storeInContext(context);
    }

    @Override
    public String toString() {
        return Span.current().toString();
    }
}
