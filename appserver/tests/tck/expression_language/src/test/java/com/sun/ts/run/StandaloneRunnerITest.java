/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import com.sun.javatest.Status;
import com.sun.ts.lib.harness.ExecTSTestCmd;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static java.lang.String.join;
import static java.lang.System.getProperty;
import static java.nio.file.Files.writeString;
import static java.nio.file.Paths.get;
import static org.junit.jupiter.api.Assertions.fail;

public class StandaloneRunnerITest {

    @Test
    public void test() throws IOException {
        if (getProperty("tck.standalone") == null) {
            return;
        }

        ExecTSTestCmd cmd = new ExecTSTestCmd();

        System.getProperties().entrySet().stream().forEach(entry -> System.out.println(entry.getKey() + "=" + entry.getValue()));

        writeString(
            get(getProperty("tck.home") + "/tmp/tstest.jte"),
            getTSContent());

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
            "HOME=" + getProperty("user.home"),
            "windir=",
            "SYSTEMROOT=");
    }

    List<String> getJavaOptions() {
        List<String> javaOptions = List.of(

            getProperty("java.home") + "/bin/java",

                "-Dcts.tmp=" + getProperty("java.io.tmpdir"),

                "--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED",
                "--add-opens=java.base/sun.net.www.protocol.jrt=ALL-UNNAMED",
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.util=ALL-UNNAMED",
                "--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED",
                "--add-opens=java.naming/javax.naming.spi=ALL-UNNAMED")
            ;

        if (getProperty("maven.tck.debug") != null && !getProperty("maven.tck.debug").isEmpty()) {
            javaOptions = new ArrayList<>(javaOptions);
            javaOptions.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=9009");
        }

        return javaOptions;
    }

    List<String> getTestOptions() {
        return List.of(
            "com.sun.ts.tests.common.vehicle.VehicleClient",

            "-p", getProperty("tck.home") + "/tmp/tstest.jte",
            "-t", getTest(),
            "-vehicle", "standalone"
        );
    }

    String getClassPath() {
        return join(":", List.of(
            getProperty("tck.home") + "/lib/javatest.jar",
            getProperty("tck.home") + "/lib/tsharness.jar",
            getProperty("tck.home") + "/lib/eltck.jar",
            getProperty("tck.home") + "/lib/sigtest.jar",
            getProperty("tck.home") + "/classes",
            getProperty("glassfish.home") + "/glassfish/modules/jakarta.el-api.jar",
            getProperty("glassfish.home") + "/glassfish/modules/expressly.jar"

        ));
    }

    String getTSContent() {
        return join("\n", List.of(
            "test_classname=" + getTestClass(),
            "variable.mapper=org.glassfish.expressly.lang.VariableMapperImpl",
            "porting.ts.url.class.1=com.sun.ts.lib.implementation.sun.common.SunRIURL",

            "bin.dir=" + getProperty("tck.home") + "/bin",
            "harness.temp.directory=" + getProperty("tck.home") + "/tmp",
            "jimage.dir=" + getProperty("tck.home") + "/tmp/jdk-bundles",

            "harness.socket.retry.count=10",
            "harness.log.port=2000",
            "harness.log.delayseconds=1",
            "harness.log.traceflag=false",
            "all.props=false",
            "finder=cts",
            "current.keywords=all"
        ));
    }

    String getTestClass() {
        // e.g.
        // -Drun.test="com/sun/ts/tests/el/api/jakarta_el/methodexpression/ELClient.java#methodExpressionMatchingAmbiguousTest"
        String test = getProperty("run.test");
        int dashPos = test.indexOf('#');
        if (dashPos > 0) {
            test = test.substring(0, dashPos);
        }
        int javaPos = test.indexOf(".java");
        if (javaPos > 0) {
            test = test.substring(0, javaPos);
        }

        return test.replace('/', '.');
    }

    String getTest() {
        String test = "";
        String runTest = getProperty("run.test");
        int dashPos = runTest.indexOf('#');
        if (dashPos > 0) {
            test = runTest.substring(dashPos + 1);
        }

        return test;
    }

}
