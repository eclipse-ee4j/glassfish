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

/**
 *
 * @author Byron Nevins
 */
class GFEmbeddedLauncher extends GFLauncher {

    private boolean setup;
    private File gfeJar;
    private File runServerJar;
    private File installDir;
    private File javaExe;
    private File domainDir;
    private List<File> javaDbClassPath;
    private String logFilename;
    private static final String GFE_RUNSERVER_JAR = "GFE_RUNSERVER_JAR";
    private static final String GFE_RUNSERVER_CLASS = "GFE_RUNSERVER_CLASS";
    private static final String GFE_JAR = "GFE_JAR";
    private static final String INSTALL_HOME = "S1AS_HOME";
    private static final String JAVA_HOME = "JAVA_HOME";
    // private static final String DOMAIN_DIR = "GFE_DOMAIN";
    private static final String GENERAL_MESSAGE = " *********  GENERAL MESSAGE ********\n"
            + "You must setup four different environmental variables to run embedded" + " with asadmin.  They are\n"
            + "GFE_JAR - path to the embedded jar\n" + "S1AS_HOME - path to installation directory.  This can be empty or not exist yet.\n"
            + "JAVA_HOME - path to a JDK installation.  JRE installation is generally not good enough\n"
            + "GFE_DEBUG_PORT - optional debugging port.  It will start suspended.\n" + "\n*********  SPECIFIC MESSAGE ********\n";

    private final String[] DERBY_FILES = { "derby.jar", "derbyclient.jar", };

    GFEmbeddedLauncher(GFLauncherInfo info) {
        super(info);

    }

    @Override
    void internalLaunch() throws GFLauncherException {
        try {
            launchInstance();
        } catch (Exception ex) {
            throw new GFLauncherException(ex);
        }
    }

    @Override
    List<File> getMainClasspath() throws GFLauncherException {
        return List.of();
    }

    @Override
    String getMainClass() throws GFLauncherException {
        String className = System.getenv(GFE_RUNSERVER_CLASS);
        if (className == null) {
            // FIXME: Should not be there, it is a class from now unused tests
            return "org.glassfish.tests.embedded.EmbeddedMain";
        }
        return className;
    }

    @Override
    public void setup() throws GFLauncherException, MiniXmlParserException {
        // remember -- this is designed exclusively for SQE usage
        // don't do it mmore than once -- that would be silly!

        if (setup) {
            return;
        }
        setup = true;
        try {
            setupFromEnv();
        } catch (GFLauncherException gfle) {
            throw new GFLauncherException(GENERAL_MESSAGE + gfle.getMessage(), gfle);
        }

        initCommandLine();

        /*
         * it is NOT an error for there to be no domain.xml (yet). so eat exceptions. Also just set the default to 4848 if we
         * don't find the port...
         */

        GFLauncherInfo info = getInfo();

        try {
            File parent = info.getDomainParentDir();
            String domainName = info.getDomainName();
            String instanceName = info.getInstanceName();

            if (instanceName == null) {
                instanceName = "server";
            }

            File dom = new File(parent, domainName);
            File theConfigDir = new File(dom, "config");
            File dx = new File(theConfigDir, "domain.xml");
            info.setConfigDir(theConfigDir);

            info.setDomainRootDir(new File(System.getenv(INSTALL_HOME)));
            MiniXmlParser parser = new MiniXmlParser(dx, instanceName);
            info.setAdminAddresses(parser.getAdminAddresses());
            File logFile = new File(dom, "logs");
            logFile = new File(logFile, "server.log");
            logFilename = logFile.getAbsolutePath();

        } catch (Exception e) {
            // temp todo
            e.printStackTrace();
        }

        List<HostAndPort> adminAddresses = info.getAdminAddresses();

        if (adminAddresses == null || adminAddresses.isEmpty()) {
            adminAddresses = new ArrayList<>();
            adminAddresses.add(new HostAndPort("localhost", 4848, false));
            info.setAdminAddresses(adminAddresses);
        }
        GFLauncherLogger.addLogFileHandler(getLogFilename());

        // super.fixLogFilename();

        /*
         * String domainName = parser.getDomainName(); if(GFLauncherUtils.ok(domainName)) { info.setDomainName(domainName); }
         */

    }

    @Override
    public String getLogFilename() throws GFLauncherException {
        return logFilename;
    }

    @Override
    void setClasspath() {
        List<File> cp = new ArrayList<>();
        cp.add(gfeJar);
        cp.addAll(javaDbClassPath);
        if (runServerJar != null) {
            cp.add(runServerJar);
        }
        setClasspath(cp.toArray(File[]::new));
    }

    @Override
    void initCommandLine() throws GFLauncherException {
        CommandLine cmdLine = new CommandLine(CommandFormat.ProcessBuilder);
        cmdLine.append(javaExe.toPath());
        addThreadDump(cmdLine);
        if (getClasspath().length > 0) {
            cmdLine.appendClassPath(getClasspath());
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
        setCommandLine(cmdLine);
    }

    private void addDebug(CommandLine cmdLine) {
        String suspend;
        String debugPort = System.getenv("GFE_DEBUG_PORT");

        if (ok(debugPort)) {
            suspend = "y";
        } else {
            debugPort = "12345";
            suspend = "n";
        }

        cmdLine.append("-Xdebug");
        cmdLine.append("-Xrunjdwp:transport=dt_socket,server=y,suspend=" + suspend + ",address=" + debugPort);
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
        String domainDirName = getInfo().getDomainName();
        domainDir = getInfo().getDomainParentDir();
        domainDir = new File(domainDir, domainDirName);

        if (!FileUtils.mkdirsMaybe(domainDir)) {
            throw new GFLauncherException("Can not create directory: " + domainDir);
        }

        domainDir = SmartFile.sanitize(domainDir);
    }

    private void setupJDK() throws GFLauncherException {
        String err = "You must set the environmental variable JAVA_HOME to point"
            + " at a valid JDK. <jdk>/bin/javac[.exe] must exist.";

        String jdkDirName = System.getenv(JAVA_HOME);
        if (!ok(jdkDirName)) {
            throw new GFLauncherException(err);
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
        String err = "You must set the environmental variable S1AS_HOME to point "
                + "at a GlassFish installation or at an empty directory or at a " + "location where an empty directory can be created.";
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
        String err = "You must set the environmental variable GFE_JAR to point " + "at the Embedded jarfile.";

        String gfeJarName = System.getenv(GFE_JAR);

        if (!ok(gfeJarName)) {
            throw new GFLauncherException(err);
        }

        gfeJar = new File(gfeJarName);

        if (!gfeJar.isFile() || gfeJar.length() < 1000000L) {
            throw new GFLauncherException(err);
        }

        gfeJar = SmartFile.sanitize(gfeJar);

        err = "You must set the environmental variable GFE_RUNSERVER_JAR to point " + "at the server startup jar.";

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

    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }

}
