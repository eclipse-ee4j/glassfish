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
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author David Matejcek
 */
@TestMethodOrder(OrderAnnotation.class)
public class EjbClassGeneratorFactoryBenchmarkTest {

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
        Options options = new OptionsBuilder()
            .include(getClass().getName() + ".*")
            .warmupIterations(0)
            .measurementIterations(1).forks(1).measurementTime(TimeValue.milliseconds(50L))
            // should be able to detect deadlocks and race conditions when generating classes
            .threads(100).timeout(TimeValue.seconds(5L))
            .timeUnit(TimeUnit.MICROSECONDS)
            .mode(Mode.SingleShotTime).shouldFailOnError(true)
            .build();

        Collection<RunResult> results = new Runner(options).run();
        assertThat(results, hasSize(1));
        Result<?> primaryResult = results.iterator().next().getPrimaryResult();
        firstRunScore = primaryResult.getScore();
        assertThat(primaryResult.getScore(), lessThan(MAX_TIME_PER_OPERATION));
    }


    @Test
    @Order(2)
    public void generate_benchmark() throws Exception {
        Options options = new OptionsBuilder()
            .include(getClass().getName() + ".*")
            .warmupBatchSize(1).warmupForks(0).warmupIterations(1).warmupTime(TimeValue.milliseconds(50L))
            .measurementIterations(1).forks(1).measurementTime(TimeValue.milliseconds(200L))
            // should be able to detect race conditions when loading classes generated in previous test
            .threads(100).timeout(TimeValue.seconds(5L))
            .timeUnit(TimeUnit.MICROSECONDS)
            .mode(Mode.AverageTime).shouldFailOnError(true)
            .build();

        Collection<RunResult> results = new Runner(options).run();
        assertThat(results, hasSize(1));
        Result<?> primaryResult = results.iterator().next().getPrimaryResult();
        assertThat(primaryResult.getScore(), lessThan(firstRunScore / 3));
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

    interface TestInterface {
        void doSomething();
    }
}
