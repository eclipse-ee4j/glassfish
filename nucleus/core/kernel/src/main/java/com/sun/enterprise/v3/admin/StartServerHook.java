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
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.main.jul.GlassFishLogManager;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;


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
    private static final boolean LOG_RESTART = Boolean.parseBoolean(System.getenv("AS_RESTART_LOGFILES"));
    private static final Path LOGDIR = new File(System.getProperty("com.sun.aas.instanceRoot"), "logs").toPath()
        .toAbsolutePath();
    private static final Predicate<Thread> FILTER_OTHER_HOOKS = t -> t.getName().startsWith("GlassFish")
        && t.getName().endsWith("Shutdown Hook");

    private final PrintStream logFile;
    private final ProcessBuilder builder;
    private final Instant startTime;

    StartServerShutdownHook(String classpath, String[] sysProps, String classname, String[] args) {
        super("GlassFish Restart Shutdown Hook");
        setDaemon(false);
        if (classname == null || classname.isBlank()) {
            throw new IllegalArgumentException("classname was null");
        }
        this.startTime = Instant.now();
        this.logFile = getLogFileOld(startTime);
        this.builder = prepareStartup(startTime, classpath, sysProps, classname, args);
    }


    @Override
    public void run() {
        try {
            // We do want to be the last thread alive.
            Thread.onSpinWait();
            waitForThreads(FILTER_OTHER_HOOKS);
            // We do want this in the server.log.
            LOG.log(INFO, "Starting process {0} in directory {1}", this.builder.command(), this.builder.directory());
            stopLogging();
            // We must not make any output to logging, because
            // log files are now used by the new process.
            // The only exception is when startup failed.
            startGlassFishInstance();
        } catch (Exception e) {
            LOG.log(ERROR, "Failed to execute shutdown hook for server start. Restart failed.", e);
            log(e);
        } finally {
            if (logFile != null) {
                logFile.close();
            }
        }
    }


    private void stopLogging() {
        GlassFishLogManager logManager = GlassFishLogManager.getLogManager();
        if (logManager == null) {
            return;
        }
        logManager.getAllHandlers().forEach(Handler::close);
    }


    private void startGlassFishInstance() {
        try {
            this.builder.start();
        } catch (Exception e) {
            throw new IllegalStateException("GlassFish instance startup failed.", e);
        }
    }


    private void waitForThreads(Predicate<Thread> filter) {
        final Thread[] threads = getThreadsToDie(filter);
        log(() -> "Waiting for shutdown of threads:\n" + toString(threads));
        while(true) {
            Thread thread = getFirstAlive(threads);
            if (thread == null) {
                break;
            }
            try {
                thread.join();
                log(() -> "Joined thread " + toString(thread));
            } catch (InterruptedException e) {
                log(e);
                continue;
            }
        }
    }


    private Thread[] getThreadsToDie(Predicate<Thread> filter) {
        final Thread[] threads = new Thread[Thread.activeCount() + 10];
        Thread.enumerate(threads);
        log(() -> "Found threads:\n" + toString(threads));
        return Stream.of(threads).filter(Objects::nonNull).filter(t -> t != this).filter(t -> !t.isDaemon())
            .filter(filter).toArray(Thread[]::new);
    }


    private Thread getFirstAlive(Thread[] shutdown) {
        for (Thread thread : shutdown) {
            if (thread.isAlive()) {
                return thread;
            }
        }
        return null;
    }


    private void log(Supplier<String> message) {
        if (logFile == null) {
            return;
        }
        logFile.append(Instant.now().toString()).append(' ').append(message.get()).append('\n');
        logFile.flush();
    }


    private void log(Exception e) {
        if (logFile == null) {
            return;
        }
        logFile.append(Instant.now().toString()).append(' ').append(e.getMessage()).append('\n');
        e.printStackTrace(logFile);
        logFile.append('\n').flush();
    }


    private static PrintStream getLogFileOld(Instant startTime) {
        if (!LOG_RESTART) {
            return null;
        }
        try {
            return new PrintStream(LOGDIR.resolve("restart-" + startTime + "-old.log").toFile());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    private static ProcessBuilder prepareStartup(Instant now, String classpath, String[] sysprops, String classname,
        String[] args) {
        final Path javaExecutable = detectJavaExecutable();
        final List<String> cmdline = new ArrayList<>();
        if (!OS.isWindows()) {
            cmdline.add("nohup");
        }
        cmdline.add(javaExecutable.toFile().getAbsolutePath());
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

        final List<String> outerCommand;
        if (OS.isWindows() || OS.isDarwin()) {
            outerCommand = cmdline;
        } else {
            // To avoid conflict of the debug port used both by old and new JVM,
            // we will force waiting for the end of the old JVM.
            outerCommand = new ArrayList<>();
            outerCommand.add("bash");
            outerCommand.add("-c");
            outerCommand.add("tail --pid=" + ProcessHandle.current().pid() + " -f /dev/null && "
                + cmdline.stream().collect(Collectors.joining(" ")));
        }

        final ProcessBuilder builder = new ProcessBuilder(outerCommand);
        builder.directory(new File(System.getProperty("user.dir")));
        if (LOG_RESTART) {
            builder.redirectErrorStream(true);
            builder.redirectOutput(LOGDIR.resolve("restart-" + now + "-new.log").toFile());
        } else {
            builder.redirectError(Redirect.DISCARD);
            builder.redirectOutput(Redirect.DISCARD);
        }
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


    private static String toString(Thread[] threads) {
        return Arrays.stream(threads).filter(Objects::nonNull).map(StartServerShutdownHook::toString)
            .collect(Collectors.joining("\n"));
    }


    private static String toString(Thread thread) {
        return new StringBuilder()
            .append("Thread[")
            .append("id=").append(thread.getId())
            .append(", name=").append(thread.getName())
            .append(", daemon=").append(thread.isDaemon())
            .append(", priority=").append(thread.getPriority())
            .append(", state=").append(thread.getState())
            .append(", classloader=").append(thread.getContextClassLoader())
            .append(']').toString();
    }
}
