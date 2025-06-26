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
import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import com.sun.enterprise.util.HostAndPort;
import com.sun.enterprise.util.io.ServerDirs;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.CommandException;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.glassfish.main.jdke.security.KeyTool;

import static com.sun.enterprise.admin.cli.CLIConstants.DEFAULT_ADMIN_PORT;
import static com.sun.enterprise.admin.cli.CLIConstants.DEFAULT_HOSTNAME;
import static com.sun.enterprise.admin.cli.ProgramOptions.PasswordLocation.LOCAL_PASSWORD;
import static java.util.logging.Level.CONFIG;
import static java.util.logging.Level.FINER;

/**
 * A class that's supposed to capture all the behavior common to operation on a "local" server.
 *
 * @author Byron Nevins
 */
public abstract class LocalServerCommand extends CLICommand {

    private static final LocalStringsImpl I18N = new LocalStringsImpl(LocalDomainCommand.class);
    private ServerDirs serverDirs;

    ////////////////////////////////////////////////////////////////
    /// Section:  protected methods that are OK to override
    ////////////////////////////////////////////////////////////////
    /**
     * Override this method and return false to turn-off the file validation. E.g. it demands that config/domain.xml be
     * present. In special cases like Synchronization -- this is how you turn off the testing.
     *
     * @return true - do the checks, false - don't do the checks
     */
    protected boolean checkForSpecialFiles() {
        return true;
    }

    ////////////////////////////////////////////////////////////////
    /// Section:  protected methods that are notOK to override.
    ////////////////////////////////////////////////////////////////
    /**
     * Returns the admin address.
     * <p>
     * The address atributes can be overriden from the command line by specifying
     * --host, --port and --secure arguments. If some is missing, it is loaded
     * from domain.xml or set to default.
     * <p>
     * For remote access it uses just command line arguments and defaults.
     *
     * @return HostAndPort object with admin server address.
     * @throws CommandException in case of parsing errors
     */
    protected final HostAndPort getAdminAddress() throws CommandException {
        String hostArg = programOpts.getPlainOption(ProgramOptions.HOST);
        String portArg = programOpts.getPlainOption(ProgramOptions.PORT);
        String secureArg = programOpts.getPlainOption(ProgramOptions.SECURE);
        // default: DAS always has the name "server"
        final HostAndPort xml = getAdminAddress("server");
        final String host;
        if (hostArg == null) {
            host = xml == null ? DEFAULT_HOSTNAME : xml.getHost();
        } else {
            host = hostArg;
        }
        final int port;
        if (portArg == null) {
            port = xml == null ? DEFAULT_ADMIN_PORT : xml.getPort();
        } else {
            port = Integer.parseInt(portArg);
        }
        final boolean secure;
        if (secureArg == null) {
            secure = xml == null ? false : xml.isSecure();
        } else {
            secure = Boolean.parseBoolean(secureArg);
        }
        return new HostAndPort(host, port, secure);
    }

    /**
     * Returns the admin address of a particular server parsed from the domain.xml.
     * For remote access it uses command line arguments and defaults.
     *
     * @return HostAndPort object with admin server address.
     * @throws CommandException in case of parsing errors
     */
    protected final HostAndPort getAdminAddress(String serverName) throws CommandException {
        if (!isLocal()) {
            // We don't have any access to changes.
            return new HostAndPort(programOpts.getHost(), programOpts.getPort(), programOpts.isSecure());
        }
        try {
            MiniXmlParser parser = new MiniXmlParser(getDomainXml(), serverName);
            List<HostAndPort> addrSet = parser.getAdminAddresses();
            if (addrSet.isEmpty()) {
                throw new CommandException(I18N.get("NoAdminPort"));
            }
            return addrSet.get(0);
        } catch (MiniXmlParserException ex) {
            throw new CommandException(I18N.get("NoAdminPortEx", ex), ex);
        }
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
        logger.finer(ok(pw) ? "Using local password" : "Not using local password");
    }

    protected final void unsetLocalPassword() {
        programOpts.setPassword(null, LOCAL_PASSWORD);
    }

    protected final void resetServerDirs() throws IOException {
        if (serverDirs == null) {
            throw new RuntimeException(Strings.get("NoServerDirs"));
        }

        serverDirs = serverDirs.refresh();
    }

    protected final ServerDirs getServerDirs() {
        return serverDirs;
    }

    protected final File getDomainXml() {
        if (serverDirs == null) {
            throw new RuntimeException(Strings.get("NoServerDirs"));
        }

        return serverDirs.getDomainXml();
    }

    /**
     * Checks if the create-domain was created using --savemasterpassword flag which obtains security by obfuscation!
     * Returns null in case of failure of any kind.
     *
     * @return String representing the password from the JCEKS store named master-password in domain folder
     */
    protected final String readFromMasterPasswordFile() {
        File mpf = getMasterPasswordFile();
        if (mpf == null)
         {
            return null; // no master password  saved
        }
        try {
            PasswordAdapter pw = new PasswordAdapter(mpf.getAbsolutePath(), "master-password".toCharArray()); // fixed key
            return pw.getPasswordForAlias("master-password");
        } catch (Exception e) {
            logger.log(FINER, "master password file reading error: {0}", e.getMessage());
            return null;
        }
    }

    protected final boolean verifyMasterPassword(String mpv) {
        return loadAndVerifyKeystore(getJKS(), mpv);
    }

    protected boolean loadAndVerifyKeystore(File jks, String mpv) {
        try {
            new KeyTool(jks, mpv.toCharArray()).loadKeyStore();
            return true;
        } catch (Exception e) {
            logger.log(FINER, e.getMessage(), e);
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
        String mpv = passwords.get(CLIConstants.MASTER_PASSWORD);
        if (mpv == null) {
            // not specified in the password file
            // optimization for the default case
            mpv = "changeit";
            if (!verifyMasterPassword(mpv)) {
                mpv = readFromMasterPasswordFile();
                if (!verifyMasterPassword(mpv)) {
                    mpv = retry(countOfRetries);
                }
            }
        } else {
            // the passwordfile contains AS_ADMIN_MASTERPASSWORD, use it
            if (!verifyMasterPassword(mpv)) {
                mpv = retry(countOfRetries);
            }
        }
        logger.log(FINER, "Time spent in master password extraction: {0} ms", (System.currentTimeMillis() - start));
        return mpv;
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
        logger.log(FINER, "Check if server is at location {0}", ourDir);

        try {
            RemoteCLICommand cmd = new RemoteCLICommand("__locations", programOpts, env);
            ActionReport report = cmd.executeAndReturnActionReport(new String[] { "__locations" });
            String theirDirPath = report.findProperty(directoryKey);
            logger.log(FINER, "Remote server has root directory {0}", theirDirPath);

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
        Long pidFromFile = ProcessUtils.loadPid(getServerDirs().getPidFile());
        try {
            RemoteCLICommand command = new RemoteCLICommand("__locations", programOpts, env);
            ActionReport report = command.executeAndReturnActionReport("__locations");
            if (report.getActionExitCode() == ExitCode.SUCCESS) {
                long pidFromAdmin = Long.parseLong(report.findProperty("Pid"));
                if (pidFromFile == null || !pidFromFile.equals(pidFromAdmin)) {
                    logger.log(Level.SEVERE,
                        "PID should be the same: PID from file = " + pidFromFile + ", pidFromAdmin = " + pidFromAdmin);
                }
                return pidFromAdmin;
            }
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "The server PID could not be resolved, sending PID from file: " + pidFromFile, e);
            return pidFromFile;
        }
    }


    /**
     * Waits until server stops and starts
     *
     * @param oldPid
     * @param oldAdminAddress
     * @param newAdminAddress new admin endpoint - usually same as old, but it could change with restart.
     * @param timeout can be null
     * @throws CommandException if we time out.
     */
    protected final void waitForRestart(final Long oldPid, final HostAndPort oldAdminAddress,
        final HostAndPort newAdminAddress, Duration timeout) throws CommandException {
        logger.log(Level.FINEST, "waitForRestart(oldPid={0}, oldAdminAddress={1}, newAdminAddress={2}, timeout={3})",
            new Object[] {oldPid, oldAdminAddress, newAdminAddress, timeout});

        final boolean printDots = !programOpts.isTerse();
        final boolean stopped = oldPid == null || ProcessUtils.waitWhileIsAlive(oldPid, timeout, printDots);
        if (!stopped) {
            throw new CommandException(I18N.get("restartDomain.noGFStart"));
        }
        logger.log(CONFIG, "Server instance is stopped, now we wait for the start on {0}", newAdminAddress);
        // Could change
        programOpts.setHostAndPort(newAdminAddress);
        final Supplier<Boolean> signStart = () -> {
            if (!ProcessUtils.isListening(newAdminAddress)) {
                // nobody is listening
                return false;
            }
            try {
                resetServerDirs();
                setLocalPassword();
            } catch (Exception e) {
                logger.log(Level.FINEST, "The endpoint is alive, but we failed to reset the local password.", e);
                return false;
            }
            Long newPid = ProcessUtils.loadPid(getServerDirs().getPidFile());
            if (newPid == null) {
                return false;
            }
            logger.log(Level.FINEST, "The server pid is {0}", newPid);
            return ProcessUtils.isAlive(newPid);
        };
        if (!ProcessUtils.waitFor(signStart, timeout, printDots)) {
            throw new CommandException(I18N.get("restartDomain.noGFStart"));
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
            throw new CommandException(I18N.get("restart.dasNotRunning"));
        }

        logger.log(FINER, "server uptime: {0}", up_ms);
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

        File mp = new File(new File(serverDirs.getServerDir(), "config"), "cacerts.jks");
        if (!mp.canRead()) {
            return null;
        }
        return mp;
    }

    protected File getMasterPasswordFile() {

        if (serverDirs == null) {
            return null;
        }

        File mp = new File(serverDirs.getServerDir(), "master-password");
        if (!mp.canRead()) {
            return null;
        }

        return mp;
    }

    private String retry(int times) throws CommandException {
        String mpv;
        // prompt times times
        for (int i = 0; i < times; i++) {
            // XXX - I18N
            String prompt = I18N.get("mp.prompt", (times - i));
            char[] mpvArr = super.readPassword(prompt);
            mpv = mpvArr != null ? new String(mpvArr) : null;
            if (mpv == null) {
                throw new CommandException(I18N.get("no.console"));
            }
            // ignore retries :)
            if (verifyMasterPassword(mpv)) {
                return mpv;
            }
            if (i < (times - 1)) {
                logger.info(I18N.get("retry.mp"));
            // make them pay for typos?
            //Thread.currentThread().sleep((i+1)*10000);
            }
        }
        throw new CommandException(I18N.get("mp.giveup", times));
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
