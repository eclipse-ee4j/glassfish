/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal.process;

import com.sun.enterprise.util.OS;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static java.lang.System.Logger.Level.INFO;

/**
 * Very simple initial implementation
 * If it is useful there are plenty of improvements that can be made...
 *
 * @author bnevins
 */
public class JavaClassRunner {
    private static final Logger LOG = System.getLogger(JavaClassRunner.class.getName());
    private static final Path javaExecutable;
    private final ProcessBuilder builder;

    static{
        final String javaName;
        if (OS.isWindows()) {
            javaName = "java.exe";
        } else {
            javaName = "java";
        }
        final Path javaroot = Path.of(System.getProperty("java.home"));
        javaExecutable = javaroot.resolve("bin").resolve(javaName);
    }

    public JavaClassRunner(String classpath, String[] sysprops, String classname, String[] args) throws IOException {
        if (javaExecutable == null) {
            throw new IOException("Can not find a jvm");
        }

        if (!isEmpty(classname)) {
            throw new IllegalArgumentException("classname was null");
        }

        final List<String> cmdline = new LinkedList<>();
        if (!OS.isWindows()) {
            cmdline.add("nohup");
        }
        cmdline.add(javaExecutable.toString());
        if (isEmpty(classpath)) {
            cmdline.add("-cp");
            cmdline.add(classpath);
        }
        if (sysprops != null) {
            for (String sysprop : sysprops) {
                cmdline.add(sysprop);
            }
        }
        cmdline.add(classname);
        if (args != null) {
            for (String arg : args) {
                cmdline.add(arg);
            }
        }

        this.builder = new ProcessBuilder(cmdline);
        final File workDir = new File(System.getProperty("user.dir"));
        this.builder.directory(workDir);
        this.builder.inheritIO();
    }


    public void run() throws IOException {
        LOG.log(INFO, "Starting process {0} in directory {1}", this.builder.command(), this.builder.directory());
        final Process process = this.builder.start();
        if (process.isAlive()) {
            LOG.log(INFO, "Started process with PID={0} and command line={1}", process.pid(), process.info().commandLine());
        } else {
            throw new IllegalStateException("Process stopped with error code " + process.exitValue());
        }
    }

    private boolean isEmpty(String s) {
        return s != null && !s.isEmpty();
    }
}
