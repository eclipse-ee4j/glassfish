/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation
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

package com.sun.ejb;

import com.sun.ejb.codegen.EjbClassGeneratorFactory;

import java.lang.System.Logger;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import static java.lang.System.Logger.Level.INFO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The {@link #generate_benchmark()} test should be able to detect race conditions when loading
 * classes generated in {@link #generate_firstRun()} test
 *
 * @author David Matejcek
 */
@TestMethodOrder(OrderAnnotation.class)
public class EjbClassGeneratorFactoryBenchmarkTest {
    private static final Logger LOG = System.getLogger(EjbClassGeneratorFactoryBenchmarkTest.class.getName());

    /**
     * The value shall be high enough to pass on all standard environments,
     * but lower than when we are generating classes. See warmup results in logs.
     */
    private static final double MAX_TIME_PER_OPERATION = 3_000_000d;
    private static final ClassLoader loader = EjbClassGeneratorFactoryBenchmarkTest.class.getClassLoader();
    private static double firstRunScore;


    @Test
    @Order(1)
    public void generate_firstRun() throws Exception {
        Options options = createOptions(false, 20L, Mode.SingleShotTime);
        Collection<RunResult> results = new Runner(options).run();
        assertThat(results, hasSize(1));
        Result<?> primaryResult = results.iterator().next().getPrimaryResult();
        firstRunScore = primaryResult.getScore();
        assertThat(primaryResult.getScore(), lessThan(MAX_TIME_PER_OPERATION));
    }


    @Test
    @Order(2)
    public void generate_benchmark() throws Exception {
        Options options = createOptions(true, 500L, Mode.AverageTime);
        Collection<RunResult> results = new Runner(options).run();
        assertThat(results, hasSize(1));
        Result<?> primaryResult = results.iterator().next().getPrimaryResult();
        double ratio = primaryResult.getScore() / firstRunScore;
        LOG.log(INFO, "Score: {0}, firstRunScore: {1}, ratio: {2}", primaryResult.getScore(), firstRunScore, ratio);
        assertThat("Expected ratio", ratio, lessThan(1 / 3d));
    }


    @Benchmark
    public void ensureRemoteBenchmark() throws Exception {
        final Class<?> newClass;
        try (EjbClassGeneratorFactory factory = new EjbClassGeneratorFactory(loader)) {
            newClass = factory.ensureRemote(TestInterface.class.getName());
        }
        assertNotNull(newClass);
        assertEquals("com.sun.ejb._EjbClassGeneratorFactoryBenchmarkTest$TestInterface_Remote", newClass.getName());
    }


    private Options createOptions(boolean warmup, long measurementTime, Mode mode) {
        ChainedOptionsBuilder builder = new OptionsBuilder().include(getClass().getName() + ".*");
        if (warmup) {
            builder.warmupIterations(1).warmupBatchSize(1).warmupForks(0).warmupTime(TimeValue.milliseconds(100L));
        } else {
            builder.warmupIterations(0);
        }
        builder.forks(1).threads(Runtime.getRuntime().availableProcessors() * 4).shouldFailOnError(true)
            .measurementIterations(1)
            .measurementTime(TimeValue.milliseconds(measurementTime)).timeout(TimeValue.seconds(5L))
            .timeUnit(TimeUnit.MICROSECONDS).mode(mode);
        return builder.build();
    }

    interface TestInterface {
        void doSomething();
    }
}
