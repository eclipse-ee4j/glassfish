/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.admin.launcher.CommandLine.CommandFormat;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import com.sun.enterprise.util.HostAndPort;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.glassfish.embeddable.GlassFishVariable.JAVA_HOME;

/**
 * @author Byron Nevins
 */
class GFEmbeddedLauncher extends GFLauncher {

    private static final String GFE_RUNSERVER_JAR = "GFE_RUNSERVER_JAR";
    private static final String GFE_RUNSERVER_CLASS = "GFE_RUNSERVER_CLASS";
    private static final String GFE_JAR = "GFE_JAR";
    private static final String INSTALL_HOME = "S1AS_HOME";
    private static final String GENERAL_MESSAGE = " *********  GENERAL MESSAGE ********\n"
        + "You must setup four different environmental variables to run embedded with asadmin."
        + " They are\n"
        + "GFE_JAR - path to the embedded jar\n"
        + "S1AS_HOME - path to installation directory. This can be empty or not exist yet.\n"
        + JAVA_HOME.getEnvName() + " - path to a JDK installation. JRE installation is generally not good enough\n"
        + "GFE_DEBUG_PORT - optional debugging port. It will start suspended.\n"
        + "\n*********  SPECIFIC MESSAGE ********\n";

    private static final String[] DERBY_FILES = { "derby.jar", "derbyclient.jar", };

    private boolean setup;
    private File gfeJar;
    private File runServerJar;
    private File installDir;
    private File javaExe;
    private File domainDir;
    private List<File> javaDbClassPath;
    private Path logFile;
    private File[] classpath;


    GFEmbeddedLauncher(GFLauncherInfo info) {
        super(info);

    }

    @Override
    public Long getPidBeforeRestart() {
        return null;
    }

    @Override
    public File getAdminRealmKeyFile() {
        return null;
    }

    @Override
    public boolean isSecureAdminEnabled() {
        return false;
    }

    @Override
    public Integer getDebugPort() {
        return null;
    }

    @Override
    public boolean isSuspendEnabled() {
        return false;
    }

    @Override
    public Path getLogFile() {
        return logFile;
    }

    @Override
    public boolean needsAutoUpgrade() {
        return false;
    }

    @Override
    public final boolean needsManualUpgrade() {
        return false;
    }

    @Override
    public void setup() throws GFLauncherException, MiniXmlParserException {
        if (setup) {
            throw new IllegalStateException("The setup() was already executed.");
        }
        setup = true;
        try {
            setupFromEnv();
        } catch (GFLauncherException e) {
            throw new GFLauncherException(GENERAL_MESSAGE + e.getMessage(), e);
        }

        final GFLauncherInfo launchParams = getParameters();
        final String instanceName = launchParams.getInstanceName() == null ? "server" : launchParams.getInstanceName();
        final File configDir = new File(domainDir, "config");
        final File domainXmlFile = new File(configDir, "domain.xml");

        launchParams.setConfigDir(configDir);
        launchParams.setDomainRootDir(new File(System.getenv(INSTALL_HOME)));
        final MiniXmlParser parser = new MiniXmlParser(domainXmlFile, instanceName);
        launchParams.setAsadminAdminAddress(new HostAndPort("localhost", 4848, false));
        launchParams.setXmlAdminAddresses(parser.getAdminAddresses());

        File logsDir = new File(domainDir, "logs");
        logFile = new File(logsDir, "server.log").toPath().toAbsolutePath();
        GFLauncherLogger.addLogFileHandler(logFile);

        setCommandLine(prepareCommandLine());
    }

    private void setClasspath() {
        List<File> cp = new ArrayList<>();
        cp.add(gfeJar);
        cp.addAll(javaDbClassPath);
        if (runServerJar != null) {
            cp.add(runServerJar);
        }
        this.classpath = cp.toArray(File[]::new);
    }

    private CommandLine prepareCommandLine() {
        CommandLine cmdLine = new CommandLine(CommandFormat.ProcessBuilder);
        cmdLine.append(javaExe.toPath());
        addThreadDump(cmdLine);
        if (classpath.length > 0) {
            cmdLine.appendClassPath(classpath);
        }
        addDebug(cmdLine);
        cmdLine.append(getMainClass());
        cmdLine.append("--installdir");
        cmdLine.append(installDir.toPath());
        cmdLine.append("--instancedir");
        cmdLine.append(domainDir.toPath());
        cmdLine.append("--autodelete");
        cmdLine.append("false");
        cmdLine.append("--autodeploy");
        return cmdLine;
    }


    private String getMainClass() {
        String className = System.getenv(GFE_RUNSERVER_CLASS);
        if (className == null) {
            // FIXME: Should not be there, it is a class from now unused tests
            return "org.glassfish.tests.embedded.EmbeddedMain";
        }
        return className;
    }

    private void addDebug(CommandLine cmdLine) {
        String suspend = System.getenv("GFE_DEBUG_SUSPEND");
        String debugPort = System.getenv("GFE_DEBUG_PORT");
        if (debugPort == null) {
            return;
        }
        String suspendOption = Boolean.valueOf(suspend).booleanValue() ?  "y" : "n";
        cmdLine.append("-Xdebug");
        cmdLine.append("-Xrunjdwp:transport=dt_socket,server=y,suspend=" + suspendOption + ",address=" + debugPort);
    }

    private void addThreadDump(CommandLine cmdLine) {
        Path jvmLog = domainDir.toPath().resolve(Path.of("logs", "jvm.log"));
        // Unlock... *must* come before the other two.
        cmdLine.appendJavaOption("-XX:+UnlockDiagnosticVMOptions");
        cmdLine.appendJavaOption("-XX:+LogVMOutput");
        cmdLine.appendJavaOption("-XX:LogFile", jvmLog);
    }

    private void setupFromEnv() throws GFLauncherException {
        // we require several env. variables to be set for embedded-cli usage
        setupEmbeddedJars();
        setupInstallationDir();
        setupJDK();
        setupDomainDir();
        setupJavaDB();
        setClasspath();
    }

    private void setupDomainDir() throws GFLauncherException {
        String domainDirName = getParameters().getDomainName();
        domainDir = getParameters().getDomainParentDir();
        domainDir = new File(domainDir, domainDirName);

        if (!FileUtils.mkdirsMaybe(domainDir)) {
            throw new GFLauncherException("Can not create directory: " + domainDir);
        }

        domainDir = SmartFile.sanitize(domainDir);
    }

    private void setupJDK() throws GFLauncherException {
        final String err = "You must set the environmental variable " + JAVA_HOME.getEnvName() + " to point"
            + " at a valid JDK.  <jdk>/bin/javac[.exe] must exist.";

        String jdkDirName = System.getenv(JAVA_HOME.getEnvName());
        if (!ok(jdkDirName)) {
            jdkDirName = System.getProperty("java.home");
        }

        File jdkDir = new File(jdkDirName);
        if (!jdkDir.isDirectory()) {
            throw new GFLauncherException(err);
        }

        if (File.separatorChar == '\\') {
            javaExe = new File(jdkDir, "bin/java.exe");
        } else {
            javaExe = new File(jdkDir, "bin/java");
        }

        if (!javaExe.isFile()) {
            throw new GFLauncherException(err);
        }

        javaExe = SmartFile.sanitize(javaExe);
    }

    private void setupInstallationDir() throws GFLauncherException {
        String err = "You must set the environmental variable S1AS_HOME to point at a GlassFish installation"
            + " or at an empty directory or at a location where an empty directory can be created.";
        String installDirName = System.getenv(INSTALL_HOME);

        if (!ok(installDirName)) {
            throw new GFLauncherException(err);
        }

        installDir = new File(installDirName);

        if (!FileUtils.mkdirsMaybe(installDir)) {
            throw new GFLauncherException(err);
        }

        installDir = SmartFile.sanitize(installDir);
    }

    private void setupEmbeddedJars() throws GFLauncherException {
        String err = "You must set the environmental variable GFE_JAR to point at the Embedded jarfile.";

        String gfeJarName = System.getenv(GFE_JAR);

        if (!ok(gfeJarName)) {
            throw new GFLauncherException(err);
        }

        gfeJar = new File(gfeJarName);

        if (!gfeJar.isFile() || gfeJar.length() < 1000000L) {
            throw new GFLauncherException(err);
        }

        gfeJar = SmartFile.sanitize(gfeJar);

        err = "You must set the environmental variable GFE_RUNSERVER_JAR to point at the server startup jar.";

        String runServerJarName = System.getenv(GFE_RUNSERVER_JAR);

        if (runServerJarName != null) {
            if (!ok(runServerJarName)) {
                throw new GFLauncherException(err);
            }
            runServerJar = new File(runServerJarName);

            if (!runServerJar.isFile()) {
                throw new GFLauncherException(err);
            }

            runServerJar = SmartFile.sanitize(runServerJar);
        }

    }

    private void setupJavaDB() throws GFLauncherException {
        // It normally will be in either:
        // * install-dir/javadb/lib
        // * install-dir/../javadb/lib

        String relPath = "javadb/lib";
        File derbyLib = new File(installDir, relPath);

        if (!derbyLib.isDirectory()) {
            derbyLib = new File(installDir.getParentFile(), relPath);
        }

        if (!derbyLib.isDirectory()) {
            throw new GFLauncherException("Could not find the JavaDB lib directory.");
        }

        // we have a valid directory. Let's verify the right jars are there!
        javaDbClassPath = new ArrayList<>();
        for (String fname : DERBY_FILES) {
            File file = new File(derbyLib, fname);
            javaDbClassPath.add(file);
            if (!file.exists()) {
                throw new GFLauncherException("Could not find the JavaDB jar: " + file);
            }
        }
    }

    private static boolean ok(String s) {
        return s != null && !s.isEmpty();
    }
}
