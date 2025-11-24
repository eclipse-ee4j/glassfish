/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.enterprise.admin.cli.CLICommand;
import com.sun.enterprise.admin.cli.CLIConstants;
import com.sun.enterprise.admin.cli.ProgramOptions;
import com.sun.enterprise.admin.cli.remote.RemoteCLICommand;
import com.sun.enterprise.admin.servermgmt.cli.ServerLifeSignChecker.GlassFishProcess;
import com.sun.enterprise.admin.servermgmt.cli.ServerLifeSignChecker.ServerLifeSigns;
import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import com.sun.enterprise.util.HostAndPort;
import com.sun.enterprise.util.io.ServerDirs;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.CommandException;
import org.glassfish.main.jdke.security.KeyTool;

import static com.sun.enterprise.admin.cli.CLIConstants.DEFAULT_ADMIN_PORT;
import static com.sun.enterprise.admin.cli.CLIConstants.DEFAULT_HOSTNAME;
import static com.sun.enterprise.admin.cli.ProgramOptions.PasswordLocation.LOCAL_PASSWORD;
import static com.sun.enterprise.admin.servermgmt.cli.ServerLifeSignChecker.step;
import static com.sun.enterprise.universal.process.ProcessUtils.isListening;
import static com.sun.enterprise.universal.process.ProcessUtils.loadPid;
import static com.sun.enterprise.universal.process.ProcessUtils.waitFor;
import static com.sun.enterprise.universal.process.ProcessUtils.waitForNewPid;
import static com.sun.enterprise.universal.process.ProcessUtils.waitWhileIsAlive;
import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_PASSWORD_DEFAULT;
import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_ALIAS;
import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_FILENAME;
import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_PASSWORD;
import static com.sun.enterprise.util.SystemPropertyConstants.TRUSTSTORE_FILENAME_DEFAULT;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

/**
 * A class that's supposed to capture all the behavior common to operation on a "local" server.
 *
 * @author Byron Nevins
 */
public abstract class LocalServerCommand extends CLICommand {

    private static final Logger LOG = System.getLogger(LocalServerCommand.class.getName());

    private ServerDirs serverDirs;

    /**
     * Override this method and return false to turn-off the file validation. E.g. it demands that config/domain.xml be
     * present. In special cases like Synchronization -- this is how you turn off the testing.
     *
     * @return true - do the checks, false - don't do the checks
     */
    protected boolean checkForSpecialFiles() {
        return true;
    }

    /**
     * Use this just for starting
     *
     * @return HostAndPort object with admin server address.
     * @throws CommandException in case of parsing errors
     * @deprecated return all, repeat to find any listening
     */
    @Deprecated
    protected final HostAndPort getAdminAddress(String serverName) throws CommandException {
        if (isLocal()) {
            List<HostAndPort> addrSet = loadAdminAddresses(getDomainXml(), serverName);
            if (addrSet.isEmpty()) {
                throw new CommandException("Cannot find admin port in domain.xml file");
            }
            return addrSet.get(0);
        }
        // We don't have any access to changes.
        return new HostAndPort(programOpts.getHost(), programOpts.getPort(), programOpts.isSecure());
    }

    /**
     * @return admin endpoint of the instance.
     */
    protected HostAndPort getReachableAdminAddress() {
        return getReachableAdminAddress(() -> loadAdminAddresses(getDomainXml(), getServerDirs().getServerName()));
    }

    /**
     * Returns first reachable admin address.
     * <p>
     * The address attributes can be overridden from the command line by specifying
     * --host, --port and --secure arguments. If some is missing, it is loaded
     * from domain.xml or set to default.
     * <p>
     * For remote access it uses just command line arguments and defaults.
     *
     * @return HostAndPort object with reachable admin server address or null.
     */
    protected final HostAndPort getReachableAdminAddress(Supplier<List<HostAndPort>> adminEndpointCandidatesSupplier) {
        String hostArg = programOpts.getPlainOption(ProgramOptions.HOST);
        String portArg = programOpts.getPlainOption(ProgramOptions.PORT);
        Integer port = portArg == null ? null : Integer.parseInt(portArg);
        String secureArg = programOpts.getPlainOption(ProgramOptions.SECURE);
        Boolean secure = secureArg == null ? null : Boolean.valueOf(secureArg);
        return findReachableAdminAddress(hostArg, port, secure, adminEndpointCandidatesSupplier);
    }

    private HostAndPort findReachableAdminAddress(String userHost, Integer userPort, Boolean userSecure,
        Supplier<List<HostAndPort>> adminEndpointCandidatesSupplier) {
        // Respect user provided values
        if (userHost != null && userPort != null && userSecure != null) {
            HostAndPort endpoint = new HostAndPort(userHost, userPort, userSecure);
            return ProcessUtils.isListening(endpoint) ? endpoint : null;
        }
        final List<HostAndPort> adminEndpointCandidates = adminEndpointCandidatesSupplier.get();
        if (adminEndpointCandidates.isEmpty()) {
            String host = userHost == null ? DEFAULT_HOSTNAME : userHost;
            Integer port = userPort == null ? DEFAULT_ADMIN_PORT : userPort;
            boolean secure = userSecure == null ? false : userSecure;
            HostAndPort endpoint = new HostAndPort(host, port, secure);
            LOG.log(DEBUG, () -> "Checking candidate: " + endpoint);
            return ProcessUtils.isListening(endpoint) ? endpoint : null;
        }
        for (HostAndPort candidate : adminEndpointCandidates) {
            final String host = userHost == null ? candidate.getHost() : userHost;
            final int port = userPort == null ? candidate.getPort() : userPort;
            final boolean secure = userSecure == null ? candidate.isSecure() : userSecure;
            HostAndPort endpoint = new HostAndPort(host, port, secure);
            LOG.log(DEBUG, () -> "Checking candidate: " + endpoint);
            if (ProcessUtils.isListening(endpoint)) {
                return endpoint;
            }
        }
        return null;
    }


    /**
     * Loads the list of admin addresses of a particular server parsed from the domain.xml.
     *
     * @param domainXml
     * @param serverName DAS: "server", instances have different.
     * @return list of HostAndPort objects with admin server address. Never null but can be empty.
     */
    protected final List<HostAndPort> loadAdminAddresses(File domainXml, String serverName) {
        try {
            MiniXmlParser parser = new MiniXmlParser(domainXml, serverName);
            return parser.getAdminAddresses();
        } catch (MiniXmlParserException e) {
            throw new IllegalStateException("Invalid XML: " + domainXml, e);
        }
    }


    protected final HostAndPort getUserProvidedAdminAddress() {
        return new HostAndPort(programOpts.getHost(), programOpts.getPort(), programOpts.isSecure());
    }

    protected final void setServerDirs(ServerDirs sd) {
        serverDirs = sd;
    }


    /**
     * @return true if something called {@link #setServerDirs(ServerDirs)} to a non-null value and
     *         serverName is not null.
     */
    protected final boolean isLocal() {
        return serverDirs != null && serverDirs.getServerName() != null;
    }

    /**
     * Sets the local password loaded from serverDirs.
     */
    protected final void setLocalPassword() {
        String pw = serverDirs == null ? null : serverDirs.getLocalPassword();
        programOpts.setPassword(pw == null ? null : pw.toCharArray(), LOCAL_PASSWORD);
        LOG.log(DEBUG, () -> ok(pw) ? "Using local password" : "Not using local password");
    }

    protected final void unsetLocalPassword() {
        programOpts.setPassword(null, LOCAL_PASSWORD);
    }

    protected final void resetServerDirs() throws IOException {
        if (serverDirs == null) {
            throw new IllegalStateException("The setServerDirs() was never called, so the server dirs is null.");
        }
        serverDirs = serverDirs.refresh();
    }

    protected final ServerDirs getServerDirs() {
        return serverDirs;
    }

    protected final File getDomainXml() {
        if (serverDirs == null) {
            throw new IllegalStateException("The setServerDirs() was never called, so the server dirs is null.");
        }
        return serverDirs.getDomainXml();
    }

    /**
     * Checks if the create-domain was created using --savemasterpassword flag which obtains security by obfuscation!
     * Returns null in case of failure of any kind.
     *
     * @return String representing the password from the key store
     */
    protected final String readFromMasterPasswordFile() {
        File mpf = getMasterPasswordFile();
        if (mpf == null) {
            return null; // no master password saved
        }
        try {
            PasswordAdapter pw = new PasswordAdapter(mpf.getAbsolutePath(), MASTER_PASSWORD_PASSWORD.toCharArray());
            return pw.getPasswordForAlias(MASTER_PASSWORD_ALIAS);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "A master password file reading error: " + e.toString(), e);
            return null;
        }
    }

    protected final boolean verifyMasterPassword(String mpv) {
        return loadAndVerifyKeystore(getJKS(), mpv);
    }

    protected boolean loadAndVerifyKeystore(File jks, String mpv) {
        LOG.log(DEBUG, "loading keystore: " + jks);
        if (jks == null || mpv == null) {
            return false;
        }
        try {
            new KeyTool(jks, mpv.toCharArray()).loadKeyStore();
            return true;
        } catch (Exception e) {
            LOG.log(DEBUG, e.getMessage(), e);
            return false;
        }
    }

    /**
     * @return the master password, either from a password file or by asking the user.
     */
    protected final String getMasterPassword() throws CommandException {
        // Sets the password into the launcher info.
        // Yes, returning master password as a string is not right ...
        final int countOfRetries = 3;
        final long start = System.currentTimeMillis();
        String masterPassword = passwords.get(CLIConstants.MASTER_PASSWORD);
        if (masterPassword == null) {
            // not specified in the password file
            // optimization for the default case
            masterPassword = KEYSTORE_PASSWORD_DEFAULT;
            if (!verifyMasterPassword(masterPassword)) {
                masterPassword = readFromMasterPasswordFile();
                if (!verifyMasterPassword(masterPassword)) {
                    masterPassword = retry(countOfRetries);
                }
            }
        } else {
            // the passwordfile contains AS_ADMIN_MASTERPASSWORD, use it
            if (!verifyMasterPassword(masterPassword)) {
                masterPassword = retry(countOfRetries);
            }
        }
        LOG.log(DEBUG, "Time spent in master password extraction: " + (System.currentTimeMillis() - start) + " ms");
        return masterPassword;
    }

    /**
     * See if the server is alive and is the one at the specified directory.
     *
     * @return true if it's the DAS at this domain directory
     */
    protected final boolean isThisServer(File ourDir, String directoryKey) {
        if (!ok(directoryKey)) {
            throw new NullPointerException(directoryKey);
        }

        ourDir = getUniquePath(ourDir);
        LOG.log(DEBUG, "Check if server is at location {0}", ourDir);

        try {
            RemoteCLICommand cmd = new RemoteCLICommand("__locations", programOpts, env);
            ActionReport report = cmd.executeAndReturnActionReport(new String[] { "__locations" });
            String theirDirPath = report.findProperty(directoryKey);
            LOG.log(DEBUG, "Remote server has root directory {0}", theirDirPath);

            if (ok(theirDirPath)) {
                File theirDir = getUniquePath(new File(theirDirPath));
                return theirDir.equals(ourDir);
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Asks remote server for the PID and compares it with the pid file.
     * If the remote server doesn't respond, returns pid from the file.
     *
     * @return PID or null if unreachable
     */
    protected final Long getServerPid() {
        Long pidFromFile = loadPid(getServerDirs().getPidFile());
        try {
            RemoteCLICommand command = new RemoteCLICommand("__locations", programOpts, env);
            ActionReport report = command.executeAndReturnActionReport("__locations");
            if (report.getActionExitCode() == ExitCode.SUCCESS) {
                long pidFromAdmin = Long.parseLong(report.findProperty("Pid"));
                if (pidFromFile == null || !pidFromFile.equals(pidFromAdmin)) {
                    LOG.log(Level.ERROR, "PID should be the same: PID from file = " + pidFromFile
                        + ", while PID received from admin endpoint = " + pidFromAdmin);
                }
                return pidFromAdmin;
            }
            return null;
        } catch (Exception e) {
            LOG.log(Level.ERROR, "The server PID could not be resolved, sending PID from file: " + pidFromFile + ".", e);
            return pidFromFile;
        }
    }

    /**
     * Waits until server stops
     *
     * @param pid
     * @param adminAddress
     * @param timeout can be null
     * @throws CommandException if we time out.
     */
    protected final void waitForStop(final Long pid, final HostAndPort adminAddress, final Duration timeout)
        throws CommandException {
        LOG.log(DEBUG, "waitForStop(pid={0}, oldAdminAddress={1}, timeout={2})", pid, adminAddress, timeout);

        final boolean printDots = !programOpts.isTerse();
        final Duration portTimeout;
        if (pid == null) {
            portTimeout = timeout;
        } else {
            portTimeout = step("Waiting for the death of the process with pid " + pid, timeout,
                () -> waitWhileIsAlive(pid, timeout, printDots));
            if (ProcessUtils.isAlive(pid)) {
                throw new CommandException("Timed out waiting for the server process to stop.");
            }
        }
        if (adminAddress == null) {
            return;
        }
        LOG.log(INFO, "Waiting until admin endpoint {0} is free.", adminAddress);
        final boolean portIsFree = waitFor(() -> !isListening(adminAddress), portTimeout, printDots);
        LOG.log(INFO, "Admin port is {0}.", portIsFree ? "free" : "blocked");
        if (portIsFree) {
            return;
        }
        throw new CommandException("Timed out waiting for the server to stop.");
    }

    /**
     * Waits until server is running - with different pid
     *
     * @param oldPid
     * @param lifeSignCheck
     * @param adminEndpointsSupplier
     * @param timeout can be null
     * @throws CommandException if we time out.
     */
    protected final String waitForStart(final Long oldPid, final ServerLifeSignCheck lifeSignCheck,
        final Supplier<List<HostAndPort>> adminEndpointsSupplier, final Duration timeout) throws CommandException {
        LOG.log(DEBUG, "waitForStart(oldPid={0}, adminEndpoints, timeout={1})", oldPid, timeout);

        final boolean printDots = !programOpts.isTerse();
        final File pidFile = getServerDirs().getPidFile();
        final Duration startTimeout;
        if (oldPid == null) {
            startTimeout = timeout;
        } else {
            startTimeout = step("Waiting for the new PID.", timeout,
                () -> waitForNewPid(oldPid, pidFile, timeout, printDots));
        }
        if (startTimeout != null && startTimeout.isNegative()) {
            throw new CommandException(reportPidFileIssue(pidFile));
        }
        final Long pid = loadPid(getServerDirs().getPidFile());
        if (pid == null) {
            throw new CommandException(reportPidFileIssue(pidFile));
        }

        try {
            resetServerDirs();
            setLocalPassword();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "The endpoint is alive, but we failed to reset the local password.", e);
        }

        LOG.log(INFO, () -> "Waiting until start of " + lifeSignCheck.getServerTitleAndName() + " completes.");

        final ServerLifeSignChecker checker = new ServerLifeSignChecker(lifeSignCheck, pidFile, adminEndpointsSupplier, printDots);
        final GlassFishProcess process = GlassFishProcess.of(pid);
        final ServerLifeSigns signs = checker.watchStartup(process, startTimeout);
        final String report = report(signs);
        if (signs.isError()) {
            throw new CommandException(report);
        }
        return report;
    }

    private String reportPidFileIssue(final File pidFile) {
        File restartLogFile = serverDirs.getRestartLogFile();
        String error = "Could not load the PID number from file " + pidFile;
        String restartLog = loadRestartLog(restartLogFile);
        if (restartLog == null) {
            return error + "\nThe " + restartLogFile + " file could not be loaded too.";
        }
        return error + "\n" + restartLog;
    }

    private String report(ServerLifeSigns signs) {
        final StringBuilder report = new StringBuilder(2048);
        report.append('\n').append(signs.getSummary());
        if (signs.getSuggestion() != null) {
            report.append('\n').append(signs.getSuggestion());
        }
        report.append("\n  Location: ").append(getServerDirs().getServerDir());
        final String situationReport = signs.getSituationReport();
        if (situationReport != null) {
            report.append(signs.getSituationReport());
        }
        if (signs.isError()) {
            final String restartLog = loadRestartLog(serverDirs.getRestartLogFile());
            if (restartLog != null) {
                report.append('\n').append(restartLog);
            }
        }
        return report.toString();
    }

    private String loadRestartLog(final File logFile ) {
        if (logFile == null || !logFile.exists()) {
            return null;
        }
        try {
            String report = "Found restart log file content: \n##########\n"
                + Files.readString(serverDirs.getRestartLogFile().toPath()) + "\n##########\n";
            // The log is there just to diagnose failed start phase of the restart.
            // We don't want to delete it if we cannot load it.
            logFile.delete();
            return report;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to read the restart log file: " + serverDirs.getRestartLogFile(), e);
            return null;
        }
    }

    /**
     * @return uptime from the server.
     */
    protected final long getUptime() throws CommandException {
        RemoteCLICommand cmd = new RemoteCLICommand("uptime", programOpts, env);
        String up = cmd.executeAndReturnOutput("uptime", "--milliseconds").trim();
        long up_ms = parseUptime(up);

        if (up_ms <= 0) {
            throw new CommandException("Server is not running, will attempt to start it...");
        }

        LOG.log(DEBUG, () -> "Server uptime: " + up_ms + " ms");
        return up_ms;
    }

    /**
     * See if the server is restartable As of March 2011 -- this only returns false if a passwordfile argument was given
     * when the server started -- but it is no longer available - i.e. the user deleted it or made it unreadable.
     */
    protected final boolean isRestartable() throws CommandException {
        // false negative is worse than false positive.
        // there is one and only one case where we return false
        RemoteCLICommand cmd = new RemoteCLICommand("_get-runtime-info", programOpts, env);
        ActionReport report = cmd.executeAndReturnActionReport("_get-runtime-info");

        if (report != null) {
            String val = report.findProperty("restartable_value");

            if (ok(val) && val.equals("false")) {
                return false;
            }
        }
        return true;
    }

    ////////////////////////////////////////////////////////////////
    /// Section:  private methods
    ////////////////////////////////////////////////////////////////
    /**
     * The remote uptime command returns a string like: Uptime: 10 minutes, 53 seconds, Total milliseconds: 653859\n We find
     * that last number and extract it. XXX - this is pretty gross, and fragile
     */
    private long parseUptime(String up) {
        try {
            return Long.parseLong(up);
        } catch (Exception e) {
            return 0;
        }
    }

    private File getJKS() {
        if (serverDirs == null) {
            return null;
        }
        File mp = new File(new File(serverDirs.getServerDir(), "config"), TRUSTSTORE_FILENAME_DEFAULT);
        if (mp.canRead()) {
            return mp;
        }
        LOG.log(DEBUG, "File does not exist or is not readable: {0}", mp);
        return null;
    }

    protected File getMasterPasswordFile() {

        if (serverDirs == null) {
            return null;
        }

        File mp = new File(serverDirs.getServerDir(), MASTER_PASSWORD_FILENAME);
        if (!mp.canRead()) {
            return null;
        }

        return mp;
    }

    private String retry(int times) throws CommandException {
        String mpv;
        // prompt times times
        for (int i = 0; i < times; i++) {
            String prompt = "Enter master password - (" + (times - i) + ") attempt(s) remain)> ";
            char[] mpvArr = super.readPassword(prompt);
            mpv = mpvArr != null ? new String(mpvArr) : null;
            if (mpv == null) {
                throw new CommandException("The Master Password is required to start the domain.\n"
                    + "No console, no prompting possible. You should either create the domain\n"
                    + "with --savemasterpassword=true or provide a password file with the --passwordfile option.");
            }
            // ignore retries :)
            if (verifyMasterPassword(mpv)) {
                return mpv;
            }
            if (i < (times - 1)) {
                logger.info("Sorry, incorrect master password, retry");
            // make them pay for typos?
            //Thread.currentThread().sleep((i+1)*10000);
            }
        }
        throw new CommandException("umber of attempts (" + times + ") exhausted, giving up");
    }

    private File getUniquePath(File f) {
        try {
            f = f.getCanonicalFile();
        } catch (IOException ioex) {
            f = SmartFile.sanitize(f);
        }
        return f;
    }
}
