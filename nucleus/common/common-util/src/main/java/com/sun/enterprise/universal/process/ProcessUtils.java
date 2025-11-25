/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.HostAndPort;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessHandle.Info;
import java.lang.System.Logger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

import static com.sun.enterprise.util.StringUtils.ok;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * Includes a somewhat kludgy way to get the pid for "me". Another casualty of
 * the JDK catering to the LEAST common denominator. Some obscure OS might not
 * have a pid! The name returned from the JMX method is like so: 12345
 *
 * @author bnevins
 * @author David Matejcek
 */
public final class ProcessUtils {

    private static final Logger LOG = System.getLogger(ProcessUtils.class.getName());

    private static final int SOCKET_TIMEOUT = 5000;
    private static final String[] PATH = getSystemPath();

    private ProcessUtils() {
        // all static class -- no instances allowed!!
    }


    /**
     * Blocks until the pid's handle exits or timeout comes first.
     *
     * @param pid process identifier
     * @param timeout
     * @param printDots true to print dots to STDOUT while waiting. One dot per second.
     * @return true if the handle was not found or exited before timeout. False otherwise.
     */
    public static boolean waitWhileIsAlive(final long pid, Duration timeout, boolean printDots) {
        return waitFor(() -> !isAlive(pid), timeout, printDots);
    }

    /**
     * Blocks until the pid file contains a new PID or timeout comes first.
     * Doesn't check if the state of the process.
     *
     * @param oldPid process identifier
     * @param pidFile file which will contain the new PID at some point
     * @param timeout
     * @param printDots true to print dots to STDOUT while waiting. One dot per second.
     * @return true if the new PID was detected before timeout. False otherwise.
     */
    public static boolean waitForNewPid(final long oldPid, final File pidFile, Duration timeout, boolean printDots) {
        Supplier<Boolean> predicate = () -> {
            final Long newPid = loadPid(pidFile);
            return newPid != null && newPid.longValue() != oldPid;
        };
        return waitFor(predicate, timeout, printDots);
    }


    /**
     * @param pidFile
     * @return true if the pid file exists and the process with the pid inside is alive.
     */
    public static boolean isAlive(final File pidFile) {
        final Long pid = loadPid(pidFile);
        if (pid == null) {
            return false;
        }
        return isAlive(pid);
    }


    /**
     * @param pid
     * @return true if the process with is alive.
     */
    public static boolean isAlive(final long pid) {
        Optional<ProcessHandle> handle = ProcessHandle.of(pid);
        return handle.isPresent() ? isAlive(handle.get()) : false;
    }


    /**
     * The {@link Process#isAlive()} returns true even for zombies so we implemented
     * this method which considers zombies as dead.
     *
     * @param process
     * @return true if the process with is alive.
     */
    public static boolean isAlive(final ProcessHandle process) {
        if (!process.isAlive()) {
            return false;
        }
        // This is a trick to avoid zombies on some systems (ie containers on Jenkins)
        // Operating system there does the cleanup much later, so we can still access
        // zombies to process their output despite for us would be better we would
        // not see them any more.
        // The ProcessHandle.onExit blocks forever for zombies in docker containers
        // without proper process reaper.
        final Info info = process.info();
        if (info.commandLine().isEmpty() && !(OS.isWindowsForSure() && info.command().isPresent())) {
            LOG.log(TRACE, "Could not retrieve command line for the pid {0},"
                + " therefore we assume that the process stopped.", process.pid());
            return false;
        }
        return true;
    }

    /**
     * @param endpoint endpoint host and port to use.
     * @return true if the endpoint is listening on socket
     */
    public static boolean isListening(HostAndPort endpoint) {
        try (Socket server = new Socket()) {
            server.setReuseAddress(false);
            // Max 5 seconds to connect. It is an extreme value for local endpoint.
            server.connect(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()), SOCKET_TIMEOUT);
            return true;
        } catch (Exception ex) {
            LOG.log(TRACE, "An attempt to open a socket to " + endpoint
                + " resulted in exception. Therefore we assume the server has stopped.", ex);
            return false;
        }
    }


    /**
     * Kill the process with the given Process ID and wait until it's gone - that means
     * that the watchedPidFile is deleted OR the process is not resolved as alive by
     * the {@link ProcessHandle#isAlive()} OR we cannot retrieve the command line of
     * the process via {@link Info#commandLine()}.
     *
     * @param pidFile - used to load pid
     * @param timeout - timeout to wait until to meet conditions meaning that the process stopped
     * @param printDots - print one dot per second when waiting.
     * @throws KillNotPossibleException It wasn't possible to send the kill signal to the process.
     * @throws KillTimeoutException Signal was sent, but process is still alive after the timeout.
     */
    public static void kill(File pidFile, Duration timeout, boolean printDots)
        throws KillNotPossibleException, KillTimeoutException {
        LOG.log(DEBUG, "kill(pidFile={0}, timeout={1}, printDots={2})", pidFile, timeout, printDots);
        final Long pid = loadPid(pidFile);
        if (pid == null) {
            return;
        }
        if (!isAlive(pid)) {
            LOG.log(INFO, "Process with pid {0} has already stopped.", pid);
            return;
        }
        final Optional<ProcessHandle> handleOptional = ProcessHandle.of(pid);
        final Optional<String> commandLine = handleOptional.get().info().commandLine();
        LOG.log(INFO, "Killing process with pid {0} and command line {1}", pid, commandLine);
        if (!handleOptional.get().destroyForcibly()) {
            // Maybe the process died in between?
            if (isAlive(pid)) {
                // ... no, it did not.
                throw new KillNotPossibleException(
                    "It wasn't possible to destroy the process with pid=" + pid + ". Check your system permissions.");
            }
            return;
        }
        // This is because File.exists() can cache file attributes
        if (!waitWhileIsAlive(pid, timeout, printDots)) {
            throw new KillTimeoutException(MessageFormat.format(
                "The process {0} was killed, but it is still alive after timeout {1} s.", pid, timeout.toSeconds()));
        }
    }


    /**
     * @param sign logic defining what we are waiting for.
     * @param timeout can be null to wait indefinitely.
     * @param printDots print dot each second and new line in the end.
     * @return true if the sign returned true before timeout.
     */
    public static boolean waitFor(Supplier<Boolean> sign, Duration timeout, boolean printDots) {
        LOG.log(DEBUG, "waitFor(sign={0}, timeout={1}, printDots={2})", sign, timeout, printDots);
        final DotPrinter dotPrinter = DotPrinter.startWaiting(printDots);
        final Instant start = Instant.now();
        try {
            final Instant deadline = timeout == null ? null : start.plus(timeout);
            while (deadline == null || Instant.now().isBefore(deadline)) {
                if (sign.get()) {
                    return true;
                }
                Thread.onSpinWait();
            }
            return false;
        } finally {
            DotPrinter.stopWaiting(dotPrinter);
            LOG.log(INFO, "Waiting finished after {0} ms.", Duration.between(start, Instant.now()).toMillis());
        }
    }


    /**
     * Look for <strong>name</strong> in the Path. If it is found and if it is
     * executable then return a File object pointing to it. Otherwise return nu
     *
     * @param name the name of the file with no path
     * @return the File object or null
     */
    public static File getExe(String name) {
        for (String path : PATH) {
            File f = new File(path + "/" + name);

            if (f.canExecute()) {
                return SmartFile.sanitize(f);
            }
        }
        return null;
    }


    /**
     * Saves current pid file to the file.
     *
     * @param pidFile
     * @throws IOException
     */
    public static void saveCurrentPid(final File pidFile) throws IOException {
        FileUtils.writeStringToFile(Long.toString(ProcessHandle.current().pid()), pidFile, ISO_8859_1);
    }


    /**
     * @param pidFile file containing pid.
     * @return pid from the file or null if the file does not exist or is null
     * @throws IllegalArgumentException for unparseable file
     */
    public static Long loadPid(final File pidFile) throws IllegalArgumentException {
        if (pidFile == null || !pidFile.exists()) {
            return null;
        }
        try {
            // Usually the process is concurrent to the process writing to the file.
            // Reproduced by tests at least once that the file can be created but empty
            // while we are already reading it.
            final String fileContent = FileUtils.readSmallFile(pidFile, ISO_8859_1).trim();
            return fileContent.isEmpty() ? null : Long.valueOf(fileContent);
        } catch (NumberFormatException | IOException e) {
            throw new IllegalArgumentException("Could not parse the PID file: " + pidFile, e);
        }
    }


    private static String[] getSystemPath() {
        String tempPaths;
        if (OS.isWindows()) {
            tempPaths = System.getenv("Path");
            if (!ok(tempPaths)) {
                tempPaths = System.getenv("PATH");
            }
        } else {
            tempPaths = System.getenv("PATH");
        }

        if (ok(tempPaths)) {
            return tempPaths.split(File.pathSeparator);
        }
        return new String[0];
    }


    private static class DotPrinter extends Thread {

        public DotPrinter() {
            super("DotPrinter");
            setDaemon(true);
        }


        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(1000L);
                    System.out.print(".");
                    System.out.flush();
                }
            } catch (InterruptedException e) {
                System.out.println();
                Thread.currentThread().interrupt();
            }
        }


        public static DotPrinter startWaiting(boolean printDots) {
            if (printDots) {
                DotPrinter dotPrinter = new DotPrinter();
                dotPrinter.start();
                return dotPrinter;
            }
            return null;
        }


        public static void stopWaiting(DotPrinter dotPrinter) {
            if (dotPrinter != null) {
                dotPrinter.interrupt();
            }
        }
    }
}
