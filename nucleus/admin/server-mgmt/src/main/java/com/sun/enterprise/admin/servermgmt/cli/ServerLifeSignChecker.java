/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.enterprise.admin.servermgmt.util.CommandAction;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.HostAndPort;

import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import org.glassfish.api.admin.CommandException;

import static com.sun.enterprise.admin.servermgmt.util.CommandAction.step;

public class ServerLifeSignChecker {
    private static final Logger LOG = System.getLogger(ServerLifeSignChecker.class.getName());

    private final ServerLifeSignCheck checks;
    private final File pidFile;
    private final Supplier<List<HostAndPort>> adminEndpointsSupplier;
    private final boolean verbose;


    public ServerLifeSignChecker(ServerLifeSignCheck checks, File pidFile, Supplier<List<HostAndPort>> adminEndpointsSupplier, boolean verbose) {
        this.checks = checks;
        this.pidFile = pidFile;
        this.adminEndpointsSupplier = adminEndpointsSupplier;
        this.verbose = verbose;
    }

    /**
     * Sign of the finished startup means that the starting process finished but it doesn't tell us
     * the result. When it returns true it means that we can evaluate the final result which cannot
     * change any more.
     *
     * @param process
     * @param timeout
     * @return true if we can make the final decision.
     * @throws CommandException
     */
    public ServerLifeSigns watchStartup(GlassFishProcess process, Duration timeout) throws CommandException {
        final ServerLifeSigns signs = new ServerLifeSigns();
        final CommandAction action = ()  -> {
            if (timeout != null && timeout.isNegative()) {
                signs.situationReport = createSituationReport(process);
                createTimeoutReport(signs);
                return;
            }
            if (!checks.isPidFile() && !checks.isProcessAlive() && !checks.isAdminEndpoint() && !checks.isCustomEndpoints()) {
                signs.summary = "All checks of the server state were disabled. Assuming the server is running.";
                signs.situationReport = createSituationReport(process);
                signs.suggestion = getSuggestions();
                return;
            }
            final boolean wasTimeout = !waitFor(process, timeout);
            signs.situationReport = createSituationReport(process);
            if (wasTimeout) {
                createTimeoutReport(signs);
                return;
            }
            if (process.isAlive()) {
                signs.summary = "Successfully started the " + checks.getServerTitleAndName() + ".";
                return;
            }
            signs.error = true;
            signs.suggestion = getSuggestions();
            final Integer exitCode = process.exitCode();
            if (exitCode == null) {
                signs.summary = "The process died.";
            } else {
                signs.summary = "The startup command return code was " + exitCode + " which means that the start ";
                if (exitCode == 0) {
                    signs.summary += "succeded, however later the process stopped for some reason.";
                } else {
                    signs.summary += "failed.";
                }
            }
        };
        step("Waiting until start of " + checks.getServerTitleAndName() + " completes.", timeout, action);
        return signs;
    }

    private boolean waitFor(GlassFishProcess process, Duration timeout) {
        // true -> in time, false -> timeout
        final Supplier<Boolean> signOfFinishedStartup = () -> {
            if (checks.isProcessAlive()) {
                // If process died, start always failed and we are done.
                // however this check can be explicitly disabled.
                if (!process.isAlive()) {
                    return true;
                }
            }
            if (checks.isCustomEndpoints()) {
                if (!isListeningOnAllEndpoints(checks.getCustomEndpoints())) {
                    return false;
                }
            }
            if (checks.isAdminEndpoint()) {
                if (!isListeningOnAnyEndpoint(adminEndpointsSupplier.get())) {
                    return false;
                }
            }
            if (checks.isPidFile()) {
                if (ProcessUtils.loadPid(pidFile) == null) {
                    return false;
                }
            }
            return true;
        };
        return ProcessUtils.waitFor(signOfFinishedStartup, timeout, verbose);
    }

    private ServerLifeSigns createTimeoutReport(final ServerLifeSigns signs) {
        signs.error = true;
        signs.summary = "Failed to confirm that the server is running - timed out."
            + " The command is either taking too long to complete"
            + " or the startup has failed"
            + " or we are not permitted to complete all checks.";
        signs.suggestion = getSuggestions();
        return signs;
    }

    private String createSituationReport(GlassFishProcess process) {
        if (!verbose) {
            return null;
        }
        // Add always, even when we don't use it in checks.
        final StringBuilder report = new StringBuilder(4096);
        if (checks.isPidFile()) {
            report.append("\n  The pid file ").append(pidFile.getAbsolutePath());
            if (pidFile.exists()) {
                final Long pid = ProcessUtils.loadPid(pidFile);
                if (pid == null) {
                    report.append(" exists but does not contain parseable pid.");
                } else {
                    report.append(" contains pid ").append(pid).append('.');
                    if (pid.longValue() != process.pid()) {
                        report.append(" WARNING: The process we started has different pid!");
                        report.append(" The process with the pid ").append(pid);
                        report.append(ProcessUtils.isAlive(pid) ? " is" : " is not").append(" alive.");
                    }
                }
            } else {
                report.append(" does not exist.");
            }
        }
        report.append("\n  Process with pid ").append(process.pid()).append(" is ");
        report.append(process.isAlive() ? "alive" : "dead");

        if (checks.isAdminEndpoint()) {
            List<HostAndPort> adminEndpoints = adminEndpointsSupplier.get();
            if (!adminEndpoints.isEmpty()) {
                report.append("\n  Admin Endpoints:");
                appendEndpoints(adminEndpoints, report);
            }
        }

        if (!checks.getCustomEndpoints().isEmpty()) {
            report.append("\n  Custom Endpoints:");
            appendEndpoints(checks.getCustomEndpoints(), report);
        }
        return report.toString();
    }

    private void appendEndpoints(List<HostAndPort> endpoints, final StringBuilder report) {
        for (HostAndPort endpoint : endpoints) {
            final boolean listening = ProcessUtils.isListening(endpoint);
            report.append("\n    ").append(endpoint.isSecure() ? "https://" : "http://").append(endpoint.getHost())
                .append(':').append(endpoint.getPort());
            report.append(' ').append(listening ? "is" : "is not").append(" reachable.");
        }
    }

    private String getSuggestions() {
        return "Please see the server log files for command status.\n"
            + "You can also start with the --verbose option in order to see early messages in this output.";
    }

    /**
     * Any - some endpoints might not be accessible from this host.
     *
     * @return true if found endpoint which seems working.
     */
    private boolean isListeningOnAnyEndpoint(List<HostAndPort> endpoints) {
        for (HostAndPort endpoint : endpoints) {
            if (ProcessUtils.isListening(endpoint)) {
                LOG.log(Level.TRACE, "Server is listening on {0}.", endpoint);
                return true;
            }
        }
        return false;
    }

    /**
     * All - user provided what to check.
     *
     * @return true if found endpoint which seems working.
     */
    private boolean isListeningOnAllEndpoints(List<HostAndPort> endpoints) {
        for (HostAndPort endpoint : endpoints) {
            if (!ProcessUtils.isListening(endpoint)) {
                LOG.log(Level.TRACE, "Server is not listening on {0}.", endpoint);
                return false;
            }
        }
        return true;
    }

    public static final class ServerLifeSigns {
        private boolean error;
        private String summary;
        private String situationReport;
        private String suggestion;

        public boolean isError() {
            return error;
        }

        public String getSummary() {
            return summary;
        }

        public String getSituationReport() {
            return situationReport;
        }

        public String getSuggestion() {
            return suggestion;
        }
    }


    public interface GlassFishProcess {
        boolean isAlive();
        long pid();
        Integer exitCode();

        static GlassFishProcess of(Process process) {
            return new GlassFishProcessInstance(process);
        }
        static GlassFishProcess of(long pid) {
            return new GlassFishProcessHandle(pid);
        }
    }


    private static final class GlassFishProcessInstance implements GlassFishProcess {

        private final Process process;

        private GlassFishProcessInstance(Process process) {
            this.process = process;
        }

        @Override
        public boolean isAlive() {
            return process.isAlive();
        }

        @Override
        public long pid() {
            return process.pid();
        }

        @Override
        public Integer exitCode() {
            return process.exitValue();
        }
    }

    private static final class GlassFishProcessHandle implements GlassFishProcess {
        private final long pid;

        private GlassFishProcessHandle(long pid) {
            this.pid = pid;
        }

        @Override
        public boolean isAlive() {
            return ProcessUtils.isAlive(pid);
        }

        @Override
        public long pid() {
            return pid;
        }

        @Override
        public Integer exitCode() {
            return null;
        }
    }
}
