/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.util.OS;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;


/**
 * Starts the server after it is completely turned off.
 * <p>
 * Shutdown hooks are asynchronously executed - we need to wait for the end
 * all GlassFish shutdown hooks, at least of those we know.
 * <p>
 * All shutdown hooks are executed in parallel including this thread.
 * As we don't have any direct reference to other hooks, but we need to
 * shut GlassFish down before we can start it, we postpone our part
 * of the job as much as possible.
 */
class StartServerShutdownHook extends Thread {

    private static final Logger LOG = System.getLogger(StartServerShutdownHook.class.getName());
    private ProcessBuilder builder;

    StartServerShutdownHook(String classpath, String[] sysProps, String classname, String[] args) {
        super("GlassFish Restart Shutdown Hook");
        if (classname == null || classname.isBlank()) {
            throw new IllegalArgumentException("classname was null");
        }
        this.builder = prepareStartup(classpath, sysProps, classname, args);
    }


    @Override
    public void run() {
        try {
            blockUntilGlassFishIsOff();
            startGlassFishInstance();
        } catch (Exception e) {
            LOG.log(ERROR, "Failed to execute shutdown hook for server start. Restart failed.", e);
        }
    }


    private void startGlassFishInstance() {
        LOG.log(INFO, "Starting process {0} in directory {1}", this.builder.command(), this.builder.directory());
        Process process;
        try {
            process = this.builder.start();
        } catch (Exception e) {
            throw new IllegalStateException("GlassFish instance startup failed.", e);
        }
        boolean fail;
        try {
            fail = process.waitFor(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Waiting for GlassFish instance restart was interrupted.", e);
        }
        if (fail) {
            throw new IllegalStateException(
                "GlassFish instance startup failed - the process stopped with error code " + process.exitValue());
        }
        // Final note: at this point we must not make any output to logging, because
        // log files are now used by the new process.
    }


    private void blockUntilGlassFishIsOff() {
        Thread.onSpinWait();
        Thread[] threads = new Thread[Thread.activeCount() + 10];
        Thread.enumerate(threads);
        LOG.log(DEBUG, () -> "Found threads: " + Arrays.toString(threads));
        List<Thread> shutdownThreads = Stream.of(threads).filter(Objects::nonNull).filter(t -> t != this)
            .filter(t -> t.getName().startsWith("GlassFish") && t.getName().endsWith("Shutdown Hook"))
            .collect(Collectors.toList());
        while(true) {
            if (isGlassFishTurnedOff(shutdownThreads)) {
                break;
            }
            Thread.onSpinWait();
        }
    }


    private boolean isGlassFishTurnedOff(List<Thread> shutdown) {
        for (Thread thread : shutdown) {
            if (thread.isAlive()) {
                LOG.log(TRACE, "Waiting for the death of " + thread);
                return false;
            }
        }
        return true;
    }


    private static ProcessBuilder prepareStartup(String classpath, String[] sysprops, String classname, String[] args) {
        Path javaExecutable = detectJavaExecutable();
        final List<String> cmdline = new LinkedList<>();
        if (!OS.isWindows()) {
            cmdline.add("nohup");
        }
        cmdline.add(javaExecutable.toString());
        cmdline.add("-cp");
        cmdline.add(classpath);
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

        ProcessBuilder builder = new ProcessBuilder(cmdline);
        final File workDir = new File(System.getProperty("user.dir"));
        // We start and abandon the process.
        builder.redirectError(Redirect.DISCARD).redirectOutput(Redirect.DISCARD).redirectInput(Redirect.DISCARD);
        builder.directory(workDir);
        return builder;
    }


    private static Path detectJavaExecutable() {
        final String javaName;
        if (OS.isWindows()) {
            javaName = "java.exe";
        } else {
            javaName = "java";
        }
        final Path javaroot = Path.of(System.getProperty("java.home"));
        return javaroot.resolve("bin").resolve(javaName);
    }
}
