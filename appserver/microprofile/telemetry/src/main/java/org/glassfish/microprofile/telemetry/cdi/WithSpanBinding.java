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

import io.opentelemetry.instrumentation.annotations.WithSpan;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Interceptor binding added by {@link TelemetryCdiExtension} to every method annotated with
 * {@link WithSpan}, which is not an interceptor binding itself.
 */
@InterceptorBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface WithSpanBinding {

    /**
     * Annotation literal of {@link WithSpanBinding}.
     */
    final class Literal extends AnnotationLiteral<WithSpanBinding> implements WithSpanBinding {

        private static final long serialVersionUID = 1L;

        /** The singleton instance. */
        public static final Literal INSTANCE = new Literal();
    }
}
