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
package org.glassfish.microprofile.telemetry;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Exporters running the wrapped exporter with the context class loader of the application.
 * <p>
 * The SDK exports from its own threads, which have no context class loader. Exporters
 * provided by applications, typically CDI beans, need it to find their application.
 */
final class ContextClassLoaderExporters {

    private ContextClassLoaderExporters() {
    }

    static SpanExporter wrap(SpanExporter exporter, ClassLoader classLoader) {
        return new SpanExporter() {

            @Override
            public CompletableResultCode export(Collection<SpanData> spans) {
                return call(classLoader, () -> exporter.export(spans));
            }

            @Override
            public CompletableResultCode flush() {
                return call(classLoader, exporter::flush);
            }

            @Override
            public CompletableResultCode shutdown() {
                return call(classLoader, exporter::shutdown);
            }

            @Override
            public String toString() {
                return exporter.toString();
            }
        };
    }

    static MetricExporter wrap(MetricExporter exporter, ClassLoader classLoader) {
        return new MetricExporter() {

            @Override
            public CompletableResultCode export(Collection<MetricData> metrics) {
                return call(classLoader, () -> exporter.export(metrics));
            }

            @Override
            public CompletableResultCode flush() {
                return call(classLoader, exporter::flush);
            }

            @Override
            public CompletableResultCode shutdown() {
                return call(classLoader, exporter::shutdown);
            }

            @Override
            public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
                return call(classLoader, () -> exporter.getAggregationTemporality(instrumentType));
            }

            @Override
            public Aggregation getDefaultAggregation(InstrumentType instrumentType) {
                return call(classLoader, () -> exporter.getDefaultAggregation(instrumentType));
            }

            @Override
            public MemoryMode getMemoryMode() {
                return call(classLoader, exporter::getMemoryMode);
            }

            @Override
            public String toString() {
                return exporter.toString();
            }
        };
    }

    static LogRecordExporter wrap(LogRecordExporter exporter, ClassLoader classLoader) {
        return new LogRecordExporter() {

            @Override
            public CompletableResultCode export(Collection<LogRecordData> logs) {
                return call(classLoader, () -> exporter.export(logs));
            }

            @Override
            public CompletableResultCode flush() {
                return call(classLoader, exporter::flush);
            }

            @Override
            public CompletableResultCode shutdown() {
                return call(classLoader, exporter::shutdown);
            }

            @Override
            public String toString() {
                return exporter.toString();
            }
        };
    }

    private static <T> T call(ClassLoader classLoader, Supplier<T> action) {
        Thread thread = Thread.currentThread();
        ClassLoader original = thread.getContextClassLoader();
        if (original == classLoader) {
            return action.get();
        }
        thread.setContextClassLoader(classLoader);
        try {
            return action.get();
        } finally {
            thread.setContextClassLoader(original);
        }
    }
}
