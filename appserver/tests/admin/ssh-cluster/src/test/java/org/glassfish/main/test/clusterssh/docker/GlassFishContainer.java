/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.clusterssh.docker;

import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ulimit;

import java.time.Duration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

public class GlassFishContainer extends GenericContainer<GlassFishContainer> {

    public GlassFishContainer(Network network, String hostname, String logPrefix, String command) {
        super("eclipse-temurin:21");
        withNetwork(network)
        .withEnv("TZ", "UTC").withEnv("LC_ALL", "en_US.UTF-8")
        .withStartupAttempts(1)
        .withStartupTimeout(Duration.ofSeconds(30L))
        .withCreateContainerCmdModifier(cmd -> {
            cmd.withEntrypoint("/bin/sh", "-c");
            cmd.withCmd(command);
            cmd.withHostName(hostname);
            cmd.withAttachStderr(true);
            cmd.withAttachStdout(true);
            final HostConfig hostConfig = cmd.getHostConfig();
            hostConfig.withMemory(2 * 1024 * 1024 * 1024L);
            hostConfig.withMemorySwappiness(0L);
            hostConfig.withUlimits(new Ulimit[] {new Ulimit("nofile", 4096L, 8192L)});
        })
        .withLogConsumer(o -> {
                System.err.println(logPrefix + ": " + o.getUtf8StringWithoutLineEnding());
                System.err.flush();
            }
        );
    }

    /**
     * @param enable true to enable verbose logging for the asadmin command
     * @return this
     */
    public GlassFishContainer withAsTrace(final boolean enable) {
        withEnv("AS_TRACE", Boolean.toString(enable));
        return this;
    }
}
