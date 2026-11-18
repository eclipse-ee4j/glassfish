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
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.Prioritized;
import jakarta.interceptor.InvocationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static io.opentelemetry.semconv.CodeAttributes.CODE_FUNCTION_NAME;

/**
 * Creates a span around every invocation of a method annotated with {@link WithSpan}.
 * <p>
 * Registered by {@link TelemetryCdiExtension} as a custom interceptor bean, enabled for the
 * whole application.
 */
final class WithSpanInterceptor implements Interceptor<WithSpanInterceptor>, Prioritized {

    private static final int PRIORITY = jakarta.interceptor.Interceptor.Priority.PLATFORM_BEFORE + 100;

    private final Supplier<Tracer> tracer;

    WithSpanInterceptor(Supplier<Tracer> tracer) {
        this.tracer = tracer;
    }

    @Override
    public Object intercept(InterceptionType type, WithSpanInterceptor instance, InvocationContext invocation)
        throws Exception {
        Method method = invocation.getMethod();
        WithSpan withSpan = method == null ? null : method.getAnnotation(WithSpan.class);
        if (withSpan == null) {
            return invocation.proceed();
        }

        Class<?> declaringClass = method.getDeclaringClass();
        String spanName = withSpan.value().isEmpty()
            ? declaringClass.getSimpleName() + "." + method.getName()
            : withSpan.value();
        SpanBuilder builder = tracer.get().spanBuilder(spanName)
            .setSpanKind(withSpan.kind())
            .setParent(withSpan.inheritContext() ? Context.current() : Context.root())
            .setAttribute(CODE_FUNCTION_NAME, declaringClass.getName() + "." + method.getName());
        addSpanAttributes(builder, method.getParameters(), invocation.getParameters());

        Span span = builder.startSpan();
        boolean endInFinally = true;
        try (Scope scope = span.makeCurrent()) {
            Object result = invocation.proceed();
            if (result instanceof CompletionStage<?> stage) {
                endInFinally = false;
                return stage.whenComplete((value, error) -> end(span, error));
            }
            return result;
        } catch (Exception | Error e) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            throw e;
        } finally {
            if (endInFinally) {
                span.end();
            }
        }
    }

    @Override
    public boolean intercepts(InterceptionType type) {
        return type == InterceptionType.AROUND_INVOKE;
    }

    @Override
    public Set<Annotation> getInterceptorBindings() {
        return Set.of(WithSpanBinding.Literal.INSTANCE);
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public Class<?> getBeanClass() {
        return WithSpanInterceptor.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Set.of();
    }

    @Override
    public Set<Type> getTypes() {
        return Set.of(WithSpanInterceptor.class, Object.class);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Set.of(Default.Literal.INSTANCE, Any.Literal.INSTANCE);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Set.of();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public WithSpanInterceptor create(CreationalContext<WithSpanInterceptor> creationalContext) {
        return this;
    }

    @Override
    public void destroy(WithSpanInterceptor instance, CreationalContext<WithSpanInterceptor> creationalContext) {
    }

    private static void end(Span span, Throwable error) {
        if (error != null) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(error);
        }
        span.end();
    }

    private static void addSpanAttributes(SpanBuilder builder, Parameter[] parameters, Object[] values) {
        if (values == null) {
            return;
        }
        for (int i = 0; i < parameters.length && i < values.length; i++) {
            SpanAttribute spanAttribute = parameters[i].getAnnotation(SpanAttribute.class);
            Object value = values[i];
            if (spanAttribute == null || value == null) {
                continue;
            }
            String name = spanAttribute.value().isEmpty() ? parameters[i].getName() : spanAttribute.value();
            if (value instanceof Boolean booleanValue) {
                builder.setAttribute(AttributeKey.booleanKey(name), booleanValue);
            } else if (value instanceof Long || value instanceof Integer || value instanceof Short
                || value instanceof Byte) {
                builder.setAttribute(AttributeKey.longKey(name), ((Number) value).longValue());
            } else if (value instanceof Double || value instanceof Float) {
                builder.setAttribute(AttributeKey.doubleKey(name), ((Number) value).doubleValue());
            } else {
                builder.setAttribute(AttributeKey.stringKey(name), value.toString());
            }
        }
    }
}
