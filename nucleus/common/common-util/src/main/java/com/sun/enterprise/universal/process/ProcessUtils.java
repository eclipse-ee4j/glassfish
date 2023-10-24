/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
import java.nio.file.Files;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

import static com.sun.enterprise.util.StringUtils.ok;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;

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
        FileUtils.writeStringToFile(Long.toString(ProcessHandle.current().pid()), pidFile);
    }


    /**
     * @param pidFile
     * @return true if the pid file exists and the process with the pid inside is alive.
     */
    public static boolean isAlive(final File pidFile) {
        if (!pidFile.exists()) {
            return false;
        }
        final long pid;
        try {
            pid = loadPid(pidFile);
        } catch (Exception e) {
            LOG.log(TRACE, "Could not load the pid file " + pidFile
                + ", therefore we assume that the process stopped.", e);
            return false;
        }
        return isAlive(pid);
    }


    public static boolean isAlive(final long pid) {
        Optional<ProcessHandle> handle = ProcessHandle.of(pid);
        if (handle.isEmpty()) {
            return false;
        }
        if (!handle.get().isAlive()) {
            return false;
        }
        Info info = handle.get().info();
        if (info.commandLine().isEmpty() && !(OS.isWindowsForSure() && info.command().isPresent())) {
            LOG.log(TRACE, "Could not retrieve command line for the pid {0},"
                + " therefore we assume that the process stopped.");
            return false;
        }
        return true;
    }


    /**
     * @param pidFile existing file containing pid.
     * @return pid from the file
     * @throws IllegalArgumentException non-existing, empty or unparseable file
     */
    public static long loadPid(final File pidFile) throws IllegalArgumentException {
        try {
            return Long.parseLong(FileUtils.readSmallFile(pidFile).trim());
        } catch (NumberFormatException | IOException e) {
            throw new IllegalArgumentException("Could not parse the PID file: " + pidFile, e);
        }
    }


    /**
     * @param endpoint endpoint host and port to use.
     * @return true if the endpoint is listening on socket
     */
    public static boolean isListening(HostAndPort endpoint) {
        try (Socket server = new Socket()) {
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
     * @param watchedPidFile - if this file vanish, we expect that the process stopped.
     * @param timeout - timeout to wait until to meet conditions meaning that the process stopped
     * @param printDots - print one dot per second when waiting.
     * @throws KillNotPossibleException It wasn't possible to send the kill signal to the process.
     * @throws KillTimeoutException Signal was sent, but process is still alive after the timeout.
     */
    public static void kill(File pidFile,
        File watchedPidFile, Duration timeout, boolean printDots) throws KillNotPossibleException, KillTimeoutException {
        LOG.log(DEBUG, "kill(pidFile={0}, watchedPidFile={1}, timeout={2}, printDots={3})",
            pidFile, watchedPidFile, timeout, printDots);
        if (!pidFile.exists()) {
            return;
        }
        final long pid = loadPid(pidFile);
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
        Supplier<Boolean> deathSign = () -> !isAlive(pid) || !Files.exists(watchedPidFile.toPath());
        if (!waitFor(deathSign, timeout, printDots)) {
            throw new KillTimeoutException(MessageFormat.format(
                "The process {0} was killed, but it is still alive after timeout {1} s.", pid, timeout.getSeconds()));
        }
    }


    /**
     * @param sign logic defining what we are waiting for.
     * @param timeout
     * @param printDots print dot each second and new line in the end.
     * @return true if the sign returned true before timeout.
     */
    public static boolean waitFor(Supplier<Boolean> sign, Duration timeout, boolean printDots) {
        LOG.log(DEBUG, "waitFor(sign={0}, timeout={1}, printDots={2})", sign, timeout, printDots);
        final Instant start = Instant.now();
        try {
            final Instant deadline = start.plus(timeout);
            Instant nextDot = start;
            while (Instant.now().isBefore(deadline)) {
                if (sign.get()) {
                    return true;
                }
                if (printDots) {
                    Instant now = Instant.now();
                    if (now.isAfter(nextDot)) {
                        nextDot = now.plusSeconds(1L);
                        System.out.print(".");
                        System.out.flush();
                    }
                }
                Thread.yield();
            }
            return false;
        } finally {
            if (printDots) {
                System.out.println();
            }
            LOG.log(INFO, "Waiting finished after {0} ms.", Duration.between(start, Instant.now()).toMillis());
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
}
