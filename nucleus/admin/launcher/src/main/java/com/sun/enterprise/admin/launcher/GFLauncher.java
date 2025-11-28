/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.admin.launcher.CommandLine.CommandFormat;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.universal.xml.MiniXmlParserException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.lang.System.Logger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.sun.enterprise.admin.launcher.GFLauncher.LaunchType.fake;
import static com.sun.enterprise.admin.launcher.GFLauncherLogger.COMMAND_LINE;
import static com.sun.enterprise.util.OS.isDarwin;
import static java.lang.System.Logger.Level.DEBUG;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * This is the main Launcher class designed for external and internal usage.
 *
 * <p>
 * Each of kinds of server, domain, node-agent and instance, need to subclass this class.
 *
 * @author bnevins
 */
public abstract class GFLauncher {
    private static final Logger LOG = System.getLogger(GFLauncher.class.getName());

    private LaunchType mode = LaunchType.normal;

    /**
     * Parameters provided by the caller of a launcher, either programmatically
     * (for GF embedded) or as commandline parameters (GF DAS or Instance).
     *
     */
    private final GFLauncherInfo parameters;

    /**
     * The full commandline string used to start GlassFish in process
     * <code<>glassFishProcess</code>
     */
    private CommandLine commandLine;

    /**
     * Time when GlassFish was launched
     */
    private long startTime;

    /**
     * The process which is running GlassFish
     */
    private Process glassFishProcess;
    private ProcessStreamDrainer processStreamDrainer;

    /**
     * Exit value of the glassFishProcess, IFF we waited on the process
     */
    private int exitValue = -1;


    protected GFLauncher(GFLauncherInfo parameters) {
        this.parameters = parameters;
    }


    /**
     * @return PID of the previous JVM process of the server, sent from that as a system property. Can be null.
     */
    public abstract Long getPidBeforeRestart();

    /**
     * Execute launch preparations. Configure the launcher, prepare the command line, some files.
     *
     * @throws GFLauncherException
     * @throws MiniXmlParserException
     */
    public abstract void setup() throws GFLauncherException, MiniXmlParserException;

    /**
     * @return the admin realm key file for the server, if the admin realm is a FileRealm.
     *         Otherwise return null. This value can be used to create a FileRealm for the server.
     */
    public abstract File getAdminRealmKeyFile();


    /**
     * @return true if secure admin is enabled
     */
    public abstract boolean isSecureAdminEnabled();

    /**
     * Get the location of the server logfile
     *
     * @return The full path of the logfile
     */
    public abstract Path getLogFile();

    /**
     * @return null or a port number
     */
    public abstract Integer getDebugPort();

    /**
     * @return true if {@link #getDebugPort()} is not null and server's JVM is set to start in suspended mode.
     */
    public abstract boolean isSuspendEnabled();

    /**
     * Does this domain need to be automatically upgraded before it can be
     * started?
     *
     * @return true if the domain needs to be upgraded first
     */
    public abstract boolean needsAutoUpgrade();

    /**
     * Does this domain need to be manually upgraded before it can be started?
     *
     * @return true if the domain needs to be upgraded first
     */
    public abstract boolean needsManualUpgrade();

    /**
     * @return The callerParameters object that contains startup caller parameters
     */
    public final GFLauncherInfo getParameters() {
        return parameters;
    }

    /**
     * Returns the exit value of the glassFishProcess.
     * This only makes sense when we ran in verbose mode and waited for the glassFishProcess to exit
     * in the {@link #waiForExit(Process)} method.
     *
     * @return the glassFishProcess' exit value if it completed and we waited. Otherwise it returns -1
     */
    public final int getExitValue() {
        return exitValue;
    }

    /**
     * You don't want to call this before calling launch because it would not
     * make sense.
     *
     * @return The Process object of the launched Server glassFishProcess. you
     * will either get a valid Process object or an Exceptio will be thrown. You
     * are guaranteed not to get a null.
     */
    public final Process getProcess() {
        if (glassFishProcess == null) {
            throw new IllegalStateException("Process was not started yet!");
        }
        return glassFishProcess;
    }

    /**
     * A ProcessStreamDrainer is always attached to every Process created here.
     * It is handy for getting the stdin and stdout as a nice String.
     *
     * @return A valid ProcessStreamDrainer. You are guaranteed to never get a
     * null.
     * @see com.sun.enterprise.universal.process.ProcessStreamDrainer
     */
    public final ProcessStreamDrainer getProcessStreamDrainer() {
        if (processStreamDrainer == null) {
            throw new IllegalStateException("Call to getProcessStreamDrainer() before it has been initialized!");
        }
        return processStreamDrainer;
    }

    /**
     * Launches the server.
     *
     * @throws GFLauncherException if launch failed.
     */
    public final void launch() throws GFLauncherException {
        logCommandLine();
        try {
            startTime = System.currentTimeMillis();
            if (isFakeLaunch()) {
                return;
            }
            launchInstance();

            // If verbose, hang around until the domain stops
            // We end otherwise, just in case server crashes, the exit value will be in the exception message.
            if (parameters.isVerboseOrWatchdog()) {
                exitValue = waiForExit(glassFishProcess);
            }
        } catch (GFLauncherException e) {
            throw e;
        } catch (Exception e) {
            throw new GFLauncherException(e);
        } finally {
            GFLauncherLogger.removeLogFileHandler();
        }
    }


    boolean isFakeLaunch() {
        return mode == fake;
    }

    public final CommandLine getCommandLine() {
        return commandLine;
    }

    protected final void setCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    void setMode(LaunchType mode) {
        this.mode = mode;
    }

    LaunchType getMode() {
        return mode;
    }

    final long getStartTime() {
        return startTime;
    }

    private void launchInstance() throws GFLauncherException {
        final List<String> securityTokens = parameters.getSecurityTokens();
        final List<String> cmds = createCommand(!securityTokens.isEmpty());

        // When calling cluster nodes, this will be visible in the server.log too.
        LOG.log(DEBUG, () -> "Executing: " + cmds.stream().collect(Collectors.joining(" ")));
        ProcessBuilder processBuilder = new ProcessBuilder(cmds);
        if (parameters.isVerboseOrWatchdog()) {
            processBuilder.redirectOutput(Redirect.INHERIT);
            processBuilder.redirectError(Redirect.INHERIT);
        }

        // Change the directory if there is one specified, o/w stick with the default.
        processBuilder.directory(parameters.getConfigDir());

        // Run the glassFishProcess and attach Stream Drainers
        try {
            // We have to abandon server.log file to avoid file locking issues on Windows.
            // From now on the server.log file is owned by the server, not by launcher.
            GFLauncherLogger.removeLogFileHandler();

            // Startup GlassFish
            glassFishProcess = processBuilder.start();

            final String name = parameters.getDomainName();

            // verbose trumps watchdog.
            if (parameters.isIgnoreOutput()) {
                processStreamDrainer = ProcessStreamDrainer.dispose(name, glassFishProcess);
            } else if (parameters.isVerbose()) {
                processStreamDrainer = ProcessStreamDrainer.redirect(name, glassFishProcess);
            } else if (parameters.isWatchdog()) {
                processStreamDrainer = ProcessStreamDrainer.dispose(name, glassFishProcess);
            } else {
                processStreamDrainer = ProcessStreamDrainer.save(name, glassFishProcess);
            }
            handleDeadProcess(glassFishProcess, processStreamDrainer);
            if (!securityTokens.isEmpty()) {
                writeSecurityTokens(glassFishProcess, processStreamDrainer, securityTokens);
            }
        } catch (Exception e) {
            throw new GFLauncherException("jvmfailure", e, e);
        }
    }

    private List<String> createCommand(final boolean securityTokensAvailable) throws GFLauncherException {
        final List<String> cmds;
        // Use launchctl bsexec on MacOS versions before 10.10
        // otherwise use regular startup.
        if (isDarwin() && useLaunchCtl(System.getProperty("os.version")) && !parameters.isVerboseOrWatchdog()) {
            cmds = new ArrayList<>();
            cmds.add("launchctl");
            cmds.add("bsexec");
            cmds.add("/");
            cmds.addAll(commandLine.toList());
        } else if (commandLine.getFormat() == CommandFormat.Script) {
            cmds = prepareWindowsEnvironment(commandLine, parameters.getConfigDir().toPath(), securityTokensAvailable);
        } else if (parameters.isVerboseOrWatchdog()) {
            cmds = new ArrayList<>();
            cmds.addAll(commandLine.toList());
        } else {
            cmds = new ArrayList<>();
            if (!isWindows()) {
                cmds.add("nohup");
            }
            cmds.addAll(commandLine.toList());
        }
        return cmds;
    }


    private void logCommandLine() {
        if (!isFakeLaunch()) {
            GFLauncherLogger.info(COMMAND_LINE, commandLine.toString("\n"));
        }
    }

    private int waiForExit(final Process p) throws GFLauncherException {
        try {
            p.waitFor();
            return p.exitValue();
        } catch (InterruptedException ex) {
            throw new GFLauncherException("verboseInterruption", ex, ex);
        }
    }

    /**
     * Checks whether to use launchctl for start up by checking if mac os
     * version < 10.10
     *
     * @return True if osversion < 10.10
     */
    private static boolean useLaunchCtl(String osversion) {

        int major = 0;
        int minor = 0;

        if (osversion == null || osversion.isEmpty()) {
            return false;
        }

        String[] split = osversion.split("[\\._\\-]+");

        try {
            if (split.length > 0 && split[0].length() > 0) {
                major = Integer.parseInt(split[0]);
            }
            if (split.length > 1 && split[1].length() > 0) {
                minor = Integer.parseInt(split[1]);
            }

            return major <= 9 || major <= 10 && minor < 10;
        } catch (NumberFormatException e) {
            // Assume version is 10.10 or later.
            return false;
        }
    }

    static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
    }

    private static List<String> prepareWindowsEnvironment(final CommandLine command, final Path configDir,
            final boolean stdinPreloaded) throws GFLauncherException {
        try {
            final Path startPsFile = createStartPsScript(command, configDir, stdinPreloaded);
            final Path schedulerPsFile = createSchedulerPsScript(startPsFile, configDir, stdinPreloaded);
            final List<String> cmds = new ArrayList<>();
            cmds.add("powershell.exe");
            if (stdinPreloaded) {
                cmds.add("-noninteractive");
            }
            cmds.add("-File");
            cmds.add("\"" + schedulerPsFile + "\"");
            return cmds;
        } catch (IOException e) {
            throw new GFLauncherException(e);
        }
    }


    private static Path createSchedulerPsScript(final Path startPsFile, final Path configDir,
        final boolean stdinPreloaded) throws IOException {
        final StringBuilder schedulerFileContent = new StringBuilder(8192);
        schedulerFileContent.append("$ErrorActionPreference = \"Stop\"\n\n");

        schedulerFileContent.append("$pidFile = \"").append(new File(configDir.toFile(), "pid").getAbsolutePath()).append("\"\n");
        schedulerFileContent.append("if (Test-Path $pidFile) {\n");
        schedulerFileContent.append("    Remove-Item $pidFile -Force\n");
        schedulerFileContent.append("}\n");
        if (stdinPreloaded) {
            schedulerFileContent.append("$stdin = [System.IO.StreamReader]::new([Console]::OpenStandardInput()).ReadToEnd()\n");
            schedulerFileContent.append("$tempFile = [System.IO.Path]::GetTempFileName()\n");
            schedulerFileContent.append("[System.IO.File]::WriteAllText($tempFile, $stdin)\n");
        }
        schedulerFileContent.append("$action = New-ScheduledTaskAction -Execute \"powershell.exe\" -Argument \"-File `\"").append(startPsFile).append("`\" `\"$tempFile`\"\"\n");
        schedulerFileContent.append("$taskName = \"GlassFishInstance_\" + [System.Guid]::NewGuid().ToString()\n");
        schedulerFileContent.append("$trigger = New-ScheduledTaskTrigger -Once -At (Get-Date).AddSeconds(1)\n");
        schedulerFileContent.append("$principal = New-ScheduledTaskPrincipal -UserId $env:USERNAME -LogonType S4U\n");
        schedulerFileContent.append("$settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -ExecutionTimeLimit (New-TimeSpan -Hours 0)\n");

        schedulerFileContent.append("Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -Principal $principal -Settings $settings\n");
        schedulerFileContent.append("Start-ScheduledTask -TaskName $taskName\n");
        schedulerFileContent.append("Unregister-ScheduledTask -TaskName $taskName -Confirm:$false\n");
        schedulerFileContent.append("$start = Get-Date\n");
        schedulerFileContent.append("$timeout = 60\n");
        schedulerFileContent.append("while (-not (Test-Path $pidFile)) {\n");
        schedulerFileContent.append("    if ((New-TimeSpan -Start $start -End (Get-Date)).TotalSeconds -gt $timeout) {\n");
        if (stdinPreloaded) {
            schedulerFileContent.append("        Remove-Item $tempFile -Force\n");
        }
        schedulerFileContent.append("        Write-Error \"Timeout waiting for GlassFish to start (pid file not created within $timeout seconds)\"\n");
        schedulerFileContent.append("        exit 1\n");
        schedulerFileContent.append("    }\n");
        schedulerFileContent.append("    Start-Sleep -Seconds 1\n");
        schedulerFileContent.append("}\n");
        if (stdinPreloaded) {
            schedulerFileContent.append("Remove-Item $tempFile -Force\n");
        }
        final Path schedulerPsFile = configDir.resolve("scheduler.ps1");
        Files.writeString(schedulerPsFile, schedulerFileContent, UTF_8, TRUNCATE_EXISTING, CREATE);
        return schedulerPsFile;
    }

    private static Path createStartPsScript(final CommandLine command, final Path configDir, final boolean stdinPreloaded)
        throws IOException {
        final List<String> commandArgs = command.toList();
        final StringBuilder scriptBlock = new StringBuilder();
        scriptBlock.append("$javaExe = ").append(commandArgs.get(0)).append('\n');
        if (stdinPreloaded) {
            scriptBlock.append("$tempFile = $args[0]\n");
        }
        scriptBlock.append("$javaArgs = @(").append(toPowerShellArgumentList(commandArgs)).append(")\n");

        scriptBlock.append("Start-Process -NoNewWindow -PassThru -FilePath ");
        scriptBlock.append(commandArgs.get(0));
        if (stdinPreloaded) {
            scriptBlock.append(" -RedirectStandardInput");
            scriptBlock.append(" \"$tempFile\"");
        }
        scriptBlock.append(" -ArgumentList $javaArgs\n");
        final Path startPsFile = configDir.resolve("start.ps1");
        Files.writeString(startPsFile, scriptBlock, UTF_8, TRUNCATE_EXISTING, CREATE);
        return startPsFile;
    }

    private static StringBuilder toPowerShellArgumentList(List<String> command) {
        StringBuilder psContent = new StringBuilder();
        for (int i = 1; i < command.size(); i++) {
            psContent.append('\n');
            psContent.append('"');
            psContent.append(command.get(i).replace("\"", "`\""));
            psContent.append('"');
        }
        return psContent;
    }


    private static void writeSecurityTokens(Process glassfishProcess, ProcessStreamDrainer drainer,
        List<String> securityTokens) throws GFLauncherException, IOException {
        OutputStream os = glassfishProcess.getOutputStream();
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            osw = new OutputStreamWriter(os, Charset.defaultCharset());
            bw = new BufferedWriter(osw);
            for (String token : securityTokens) {
                bw.write(token);
                bw.newLine();
                bw.flush();
            }
        } catch (IOException e) {
            handleDeadProcess(glassfishProcess, drainer);
            // glassFishProcess is not dead, but got some other exception, rethrow it
            throw e;
        } finally {
            if (bw != null) {
                bw.close();
            }
            if (osw != null) {
                osw.close();
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ioe) {
                    // nothing to do
                }
            }
            if (bw != null) {
                handleDeadProcess(glassfishProcess, drainer);
            }
        }
    }


    private static void handleDeadProcess(Process glassfishProcess, ProcessStreamDrainer drainer)
        throws GFLauncherException {
        String trace = getDeadProcessTrace(glassfishProcess, drainer);
        if (trace != null) {
            throw new GFLauncherException(trace);
        }
    }

    /**
     * @returns null in case the process is NOT dead or succeeded
     */
    private static String getDeadProcessTrace(Process process, ProcessStreamDrainer drainer) {
        if (process.isAlive()) {
            return null;
        }
        int exitValue = process.exitValue();
        if (exitValue == 0) {
            return null;
        }
        return "The server exited prematurely with exit code " + exitValue
            + ".\nBefore it died, it produced the following output:\n\n" + drainer.getOutErrString();
    }

    // unit tests will want 'fake' so that the glassFishProcess is not really started.
    enum LaunchType {
        normal,
        debug,
        trace,
        /**
         * Useful just for unit tests so the server will not be started.
         */
        fake
    }
}
