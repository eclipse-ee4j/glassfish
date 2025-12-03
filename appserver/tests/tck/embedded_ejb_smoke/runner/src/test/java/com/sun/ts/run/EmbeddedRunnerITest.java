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
package com.sun.ts.run;

import com.sun.ts.lib.harness.ExecTSTestCmd;
import com.sun.javatest.Status;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.fail;

public class EmbeddedRunnerITest {

    @Test
    public void test() {
        ExecTSTestCmd cmd = new ExecTSTestCmd();

        System.getProperties().entrySet().stream().forEach(entry -> System.out.println(entry.getKey() + "=" + entry.getValue()));

        List<String> arguments = new ArrayList<>();
        arguments.addAll(getEnvOptions());
        arguments.addAll(getJavaOptions());
        arguments.addAll(getTestOptions());

        PrintWriter log = new PrintWriter(new OutputStreamWriter(System.err));
        PrintWriter ref = new PrintWriter(new OutputStreamWriter(System.out));

        try {
            Status status = cmd.run(arguments.toArray(String[]::new), log, ref);
            if (!status.isPassed()) {
                fail(status.getReason());
            }

        } finally {
            log.flush();
            ref.flush();
        }
    }

    List<String> getEnvOptions() {
        return List.of(
            "-v",
            "CLASSPATH=" + getClassPath(),
            "DISPLAY=:0.0",
            "HOME=" + System.getProperty("user.home"),
            "TMP=" + System.getProperty("java.io.tmpdir"),
            "windir=",
            "SYSTEMROOT=");
    }

    List<String> getJavaOptions() {
        List<String> javaOptions = List.of(

            System.getProperty("java.home") + "/bin/java",

                "-Dcts.tmp=" + System.getProperty("java.io.tmpdir"),
                "-Djava.util.logging.config.file=" + System.getProperty("glassfish.home") + "/glassfish/domains/domain1/config/logging.properties",
                "-Dtest.ejb.stateful.timeout.wait.seconds=180 ",
                "-Ddeliverable.class=com.sun.ts.lib.deliverable.cts.CTSDeliverable",
                "--module-path", System.getProperty("glassfish.home") + "/glassfish/lib/bootstrap",
                "--add-modules", "ALL-MODULE-PATH",
                "--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED",
                "--add-opens=java.base/sun.net.www.protocol.jrt=ALL-UNNAMED",
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.util=ALL-UNNAMED",
                "--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED",
                "--add-opens=java.naming/javax.naming.spi=org.glassfish.main.jdke")
            ;

        if (System.getProperty("maven.tck.debug") != null && !System.getProperty("maven.tck.debug").isEmpty()) {
            javaOptions = new ArrayList<>(javaOptions);
            javaOptions.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=9009");
        }

        return javaOptions;
    }

    List<String> getTestOptions() {
        String basedir = System.getProperty("basedir");

        return List.of(
            "com.sun.ts.tests.common.vehicle.VehicleClient",

            "-p", basedir + "/src/test/resources/tstest.jte",
            "-t", "add",
            "-vehicle", "ejbembed"
        );
    }

    String getClassPath() {

        String glassfishVersion = System.getProperty("glassfish.version");
        String localRepository = System.getProperty("localRepository");

        Path originalEjbJar = Paths.get(localRepository + "/org/glassfish/main/tests/tck/ejb-lite-basic/" + glassfishVersion + "/ejb-lite-basic-" + glassfishVersion + ".jar");
        Path copiedEjbJar = Paths.get(System.getProperty("tck.home") + "/ejbembed_vehicle_ejb.jar");
        try {
            Files.copy(originalEjbJar, copiedEjbJar, REPLACE_EXISTING);
        } catch (IOException e) {
           throw new UncheckedIOException(e);
        }

        return String.join(":", List.of(
            localRepository + "/org/glassfish/main/tests/tck/tsharness/" + glassfishVersion + "/tsharness-" + glassfishVersion + ".jar",
            System.getProperty("glassfish.home") + "/glassfish/lib/embedded/glassfish-embedded-static-shell.jar",
            copiedEjbJar.toString()
        ));
    }
}
