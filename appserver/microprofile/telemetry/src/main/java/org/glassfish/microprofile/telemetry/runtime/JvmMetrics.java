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
package org.glassfish.microprofile.telemetry.runtime;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;

import java.lang.System.Logger;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import static io.opentelemetry.semconv.JvmAttributes.JVM_GC_ACTION;
import static io.opentelemetry.semconv.JvmAttributes.JVM_GC_NAME;
import static io.opentelemetry.semconv.JvmAttributes.JVM_MEMORY_POOL_NAME;
import static io.opentelemetry.semconv.JvmAttributes.JVM_MEMORY_TYPE;
import static io.opentelemetry.semconv.JvmAttributes.JVM_THREAD_DAEMON;
import static io.opentelemetry.semconv.JvmAttributes.JVM_THREAD_STATE;
import static java.lang.System.Logger.Level.DEBUG;

/**
 * The JVM metrics required by MicroProfile Telemetry, as defined by the OpenTelemetry semantic
 * conventions for JVM runtime metrics.
 * <p>
 * Only {@code java.management} types are used: the {@code com.sun.management} extensions are
 * read through the platform MBean server, so that no JDK internal package has to be imported.
 */
final class JvmMetrics implements AutoCloseable {

    private static final Logger LOG = System.getLogger(JvmMetrics.class.getName());

    private static final String SCOPE_NAME = "org.glassfish.microprofile.telemetry.jvm";
    private static final String GC_NOTIFICATION = "com.sun.management.gc.notification";
    private static final List<Double> GC_DURATION_BUCKETS = List.of(0.01, 0.1, 1.0, 10.0);
    private static final double NANOS_PER_SECOND = 1_000_000_000d;
    private static final double MILLIS_PER_SECOND = 1_000d;

    private final List<AutoCloseable> registrations = new ArrayList<>();

    private JvmMetrics() {
    }

    /**
     * Registers the JVM metrics in the meter provider of the instance.
     *
     * @param openTelemetry the runtime instance
     * @return the registration, closing it unregisters all the metrics.
     */
    static JvmMetrics register(OpenTelemetry openTelemetry) {
        JvmMetrics metrics = new JvmMetrics();
        Meter meter = openTelemetry.getMeter(SCOPE_NAME);
        metrics.registerMemory(meter);
        metrics.registerGarbageCollection(meter);
        metrics.registerThreads(meter);
        metrics.registerClasses(meter);
        metrics.registerCpu(meter);
        return metrics;
    }

    @Override
    public void close() {
        for (AutoCloseable registration : registrations) {
            try {
                registration.close();
            } catch (Exception e) {
                LOG.log(DEBUG, () -> "Failed to unregister a JVM metric.", e);
            }
        }
        registrations.clear();
    }

    private void registerMemory(Meter meter) {
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        registerMemoryPools(meter, pools, "jvm.memory.used", "Measure of memory used.", MemoryPoolMXBean::getUsage,
            MemoryUsage::getUsed);
        registerMemoryPools(meter, pools, "jvm.memory.committed", "Measure of memory committed.",
            MemoryPoolMXBean::getUsage, MemoryUsage::getCommitted);
        registerMemoryPools(meter, pools, "jvm.memory.limit", "Measure of max obtainable memory.",
            MemoryPoolMXBean::getUsage, MemoryUsage::getMax);
        registerMemoryPools(meter, pools, "jvm.memory.used_after_last_gc",
            "Measure of memory used, as measured after the most recent garbage collection event on this pool.",
            MemoryPoolMXBean::getCollectionUsage, MemoryUsage::getUsed);
    }

    private void registerMemoryPools(Meter meter, List<MemoryPoolMXBean> pools, String name, String description,
        Function<MemoryPoolMXBean, MemoryUsage> usage, Function<MemoryUsage, Long> value) {
        Map<MemoryPoolMXBean, Attributes> attributes = new HashMap<>();
        for (MemoryPoolMXBean pool : pools) {
            attributes.put(pool, Attributes.of(JVM_MEMORY_POOL_NAME, pool.getName(), JVM_MEMORY_TYPE,
                pool.getType() == MemoryType.HEAP ? "heap" : "non_heap"));
        }
        registrations.add(meter.upDownCounterBuilder(name).setDescription(description).setUnit("By")
            .buildWithCallback(measurement -> {
                for (MemoryPoolMXBean pool : pools) {
                    MemoryUsage memoryUsage = usage.apply(pool);
                    if (memoryUsage != null) {
                        long measured = value.apply(memoryUsage);
                        if (measured >= 0) {
                            measurement.record(measured, attributes.get(pool));
                        }
                    }
                }
            }));
    }

    private void registerGarbageCollection(Meter meter) {
        DoubleHistogram duration = meter.histogramBuilder("jvm.gc.duration")
            .setDescription("Duration of JVM garbage collection actions.")
            .setUnit("s")
            .setExplicitBucketBoundariesAdvice(GC_DURATION_BUCKETS)
            .build();
        NotificationListener listener = (notification, handback) -> recordGc(duration, notification);
        for (GarbageCollectorMXBean collector : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (collector instanceof NotificationEmitter emitter) {
                emitter.addNotificationListener(listener, notification -> GC_NOTIFICATION.equals(notification.getType()),
                    null);
                registrations.add(() -> emitter.removeNotificationListener(listener));
            }
        }
    }

    private static void recordGc(DoubleHistogram duration, Notification notification) {
        if (!(notification.getUserData() instanceof CompositeData data)) {
            return;
        }
        if (!(data.get("gcInfo") instanceof CompositeData gcInfo) || !(gcInfo.get("duration") instanceof Long millis)) {
            return;
        }
        duration.record(millis / MILLIS_PER_SECOND,
            Attributes.of(JVM_GC_NAME, (String) data.get("gcName"), JVM_GC_ACTION, (String) data.get("gcAction")));
    }

    private void registerThreads(Meter meter) {
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        registrations.add(meter.upDownCounterBuilder("jvm.thread.count")
            .setDescription("Number of executing platform threads.")
            .setUnit("{thread}")
            .buildWithCallback(measurement -> recordThreads(threads, measurement)));
    }

    private static void recordThreads(ThreadMXBean threads, ObservableLongMeasurement measurement) {
        Map<Attributes, Long> counts = new HashMap<>();
        for (ThreadInfo info : threads.getThreadInfo(threads.getAllThreadIds())) {
            if (info != null) {
                Attributes attributes = Attributes.of(JVM_THREAD_DAEMON, info.isDaemon(), JVM_THREAD_STATE,
                    threadState(info.getThreadState()));
                counts.merge(attributes, 1L, Long::sum);
            }
        }
        counts.forEach((attributes, count) -> measurement.record(count, attributes));
    }

    private static String threadState(Thread.State state) {
        return switch (state) {
            case NEW -> "new";
            case RUNNABLE -> "runnable";
            case BLOCKED -> "blocked";
            case WAITING -> "waiting";
            case TIMED_WAITING -> "timed_waiting";
            case TERMINATED -> "terminated";
        };
    }

    private void registerClasses(Meter meter) {
        ClassLoadingMXBean classes = ManagementFactory.getClassLoadingMXBean();
        registrations.add(meter.counterBuilder("jvm.class.loaded")
            .setDescription("Number of classes loaded since JVM start.")
            .setUnit("{class}")
            .buildWithCallback(measurement -> measurement.record(classes.getTotalLoadedClassCount())));
        registrations.add(meter.counterBuilder("jvm.class.unloaded")
            .setDescription("Number of classes unloaded since JVM start.")
            .setUnit("{class}")
            .buildWithCallback(measurement -> measurement.record(classes.getUnloadedClassCount())));
        registrations.add(meter.upDownCounterBuilder("jvm.class.count")
            .setDescription("Number of classes currently loaded.")
            .setUnit("{class}")
            .buildWithCallback(measurement -> measurement.record(classes.getLoadedClassCount())));
    }

    private void registerCpu(Meter meter) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName os = toObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
        registrations.add(meter.counterBuilder("jvm.cpu.time")
            .ofDoubles()
            .setDescription("CPU time used by the process as reported by the JVM.")
            .setUnit("s")
            .buildWithCallback(measurement -> {
                Number nanos = readNumber(server, os, "ProcessCpuTime");
                if (nanos != null && nanos.longValue() >= 0) {
                    measurement.record(nanos.longValue() / NANOS_PER_SECOND);
                }
            }));
        registrations.add(meter.upDownCounterBuilder("jvm.cpu.count")
            .setDescription("Number of processors available to the Java virtual machine.")
            .setUnit("{cpu}")
            .buildWithCallback(measurement -> measurement.record(Runtime.getRuntime().availableProcessors())));
        registrations.add(meter.gaugeBuilder("jvm.cpu.recent_utilization")
            .setDescription("Recent CPU utilization for the process as reported by the JVM.")
            .setUnit("1")
            .buildWithCallback(measurement -> {
                Number load = readNumber(server, os, "ProcessCpuLoad");
                if (load != null && load.doubleValue() >= 0) {
                    measurement.record(load.doubleValue());
                }
            }));
    }

    private static Number readNumber(MBeanServer server, ObjectName name, String attribute) {
        try {
            return server.getAttribute(name, attribute) instanceof Number number ? number : null;
        } catch (Exception e) {
            // Not available on this JVM
            return null;
        }
    }

    private static ObjectName toObjectName(String name) {
        try {
            return new ObjectName(name);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid MBean name " + name, e);
        }
    }
}
