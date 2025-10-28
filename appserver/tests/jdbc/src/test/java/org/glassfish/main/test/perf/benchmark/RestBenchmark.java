/*
 * Copyright (c) 2025 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.perf.benchmark;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.glassfish.main.test.jdbc.pool.war.User;
import org.glassfish.main.test.perf.rest.UserRestClient;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.LIMIT_JMH_THREADS;
import static org.glassfish.main.test.perf.benchmark.RestBenchmark.RestClientProvider.SYS_PROPERTY_ENDPOINT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.openjdk.jmh.runner.options.TimeValue.seconds;

/**
 *
 */
public class RestBenchmark {

    public static Options createOptions(URI wsEndpoint, String benchmark) {
        ChainedOptionsBuilder builder = new OptionsBuilder().include(RestBenchmark.class.getName() + '.' + benchmark);
        builder.shouldFailOnError(true);
        builder.warmupIterations(1).warmupTime(seconds(5L));
        builder.timeUnit(TimeUnit.MILLISECONDS).mode(Mode.AverageTime);
        builder.detectJvmArgs().jvmArgsAppend("-D" + SYS_PROPERTY_ENDPOINT + "=" + wsEndpoint);
        builder.forks(1).threads(LIMIT_JMH_THREADS);
        builder
            .measurementIterations(2).measurementTime(seconds(10))
            .timeout(seconds(10));
        return builder.build();
    }

    // Don't forget benchmarks are executed alphabetically!

    @Benchmark
    public void createUser(RestClientProvider clientProvider) throws Exception {
        User user = new User(RandomStringUtils.insecure().nextAlphabetic(32));
        UserRestClient client = clientProvider.getClient();
        client.create(user);
    }

    @Benchmark
    public void listUsers(RestClientProvider clientProvider) throws Exception {
        UserRestClient client = clientProvider.getClient();
        List<User> users = client.list();
        assertThat(users, hasSize(equalTo(100)));
    }

    @State(Scope.Benchmark)
    public static class RestClientProvider {
        public static final String SYS_PROPERTY_ENDPOINT = "endpoint";
        private static final UserRestClient CLIENT = new UserRestClient(
            URI.create(System.getProperty(SYS_PROPERTY_ENDPOINT)), false);

        public UserRestClient getClient() {
            return CLIENT;
        }
    }
}
