/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.tests.tck.ant;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.glassfish.main.tests.tck.ant.io.ZipResolver;
import org.glassfish.main.tests.tck.ant.junit.generated.Error;
import org.glassfish.main.tests.tck.ant.junit.generated.Failure;
import org.glassfish.main.tests.tck.ant.junit.generated.Testcase;
import org.glassfish.main.tests.tck.ant.junit.generated.Testsuite;
import org.glassfish.main.tests.tck.ant.xml.JUnitResultsParser;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author David Matejcek
 */
public class TckRunner {
    private static final Logger LOG = System.getLogger(TckRunner.class.getName());

    private final TckConfiguration cfg;
    private File glassfishZip;

    public TckRunner(final TckConfiguration cfg) {
        this.cfg = cfg;
    }


    public void prepareWorkspace() {
        LOG.log(Level.INFO, "Preparing workspace at {0}", cfg.getTargetDir());
        ZipResolver zipResolver = new ZipResolver(cfg.getTargetDir());
        zipResolver.unzipDependency("org.glassfish.main.tests.tck", "jakarta-ant-based-tck", cfg.getTckVersion());
        cfg.getJakartaeetckCommand().toFile().setExecutable(true);

        glassfishZip = zipResolver.getZipFile("org.glassfish.main.distributions", "glassfish", cfg.getGlassFishVersion());
    }


    public void start(Path modulePath) throws IOException, InterruptedException {
        LOG.log(Level.INFO, "Starting tests of module {0} ...", modulePath);
        Process process = startBash(this.cfg.getJakartaeetckCommand().toAbsolutePath() + " " + modulePath);
        if (process.waitFor() != 0) {
            throw new IllegalStateException("TCK execution ended with exit code " + process.exitValue());
        }
        final File testReport = toTestReportFile(modulePath);
        JUnitResultsParser resultsParser = new JUnitResultsParser();
        Testsuite testsuite = resultsParser.parse(testReport);
        if (testsuite.getErrors() > 0 || testsuite.getFailures() > 0) {
            throw new IllegalStateException(toReport(testsuite));
        }
    }


    /**
     * Fixes Jakarta EE issue with stopping what it started - after some types of errors the script
     * doesn't stop GlassFish and Derby instances.
     */
    public void stopServers() throws InterruptedException, IOException {
        Path riPath = cfg.getTargetDir().toPath().resolve("ri");
        Path viPath = cfg.getTargetDir().toPath().resolve("vi");

        // We don't care about the result here
        startBash(viPath.resolve(Path.of("glassfish7", "bin", "asadmin")) + " stop-database").waitFor(1, MINUTES);
        startBash(viPath.resolve(Path.of("glassfish7", "bin", "asadmin")) + " stop-domain").waitFor(1, MINUTES);
        startBash(riPath.resolve(Path.of("glassfish7", "bin", "asadmin")) + " stop-domain").waitFor(1, MINUTES);

        Path derbyDir = viPath.resolve(Path.of("glassfish7", "javadb"));
        startBash(cfg.getJdkDirectory().toPath().resolve(Path.of("bin", "java"))
            + " -Dderby.system.home=" + derbyDir.resolve("databases")
            + " -classpath " + derbyDir.resolve(Path.of("lib", "derbynet.jar"))
            + ":" + derbyDir.resolve(Path.of("lib", "derby.jar"))
            + ":" + derbyDir.resolve(Path.of("lib", "derbyshared.jar"))
            + ":" + derbyDir.resolve(Path.of("lib", "derbytools.jar"))
            + " org.apache.derby.drda.NetworkServerControl -h localhost -p 1527 shutdown")
        .waitFor(1, MINUTES);
    }


    /**
     * Results are already gzipped and we need to run another module, so we remove generated files
     * from previous executions.
     */
    public void deleteWorkspace() throws IOException {
        deleteDirectory(cfg.getTargetDir().toPath().resolve("ri"));
        deleteDirectory(cfg.getTargetDir().toPath().resolve("vi"));
        deleteDirectory(new File("/tmp/DerbyDB").toPath());
        deleteDirectory(new File(cfg.getTargetDir(), "jakartaeetck-work").toPath());
    }


    private void deleteDirectory(final Path directory) throws IOException {
        if (!directory.toFile().exists()) {
            return;
        }
        LOG.log(Level.INFO, "Deleting directory: {0}", directory);
        Files.walk(directory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    private Process startBash(final String command) throws IOException {
        ProcessBuilder bash = new ProcessBuilder("/bin/bash", "-c", command).inheritIO().directory(cfg.getTargetDir());
        configureEnvironment(bash.environment());
        return bash.start();
    }

    private void configureEnvironment(Map<String, String> env) {
        env.put("LC_ALL", "en_US.UTF-8");
        env.put("WORKSPACE", cfg.getTargetDir().getAbsolutePath());
        env.put("JAVA_HOME", cfg.getJdkDirectory().getAbsolutePath());
        env.put("JDK17_HOME", cfg.getJdkDirectory().getAbsolutePath());
        env.put("JDK", "JDK17");
        env.put("SMTP_PORT", "25");
        env.put("HARNESS_DEBUG", "false"); // TODO: configurable ... logging.
        env.put("AS_DEBUG", "false"); // TODO: configurable - logging.communicationWithServer.enabled=false

        env.put("PATH", cfg.getJdkDirectory().getAbsolutePath() + "/bin:" + cfg.getAntDirectory().getAbsolutePath()
            + "/bin/:/usr/bin");
        env.put("CTS_HOME", cfg.getTargetDir().getAbsolutePath());
        env.put("TS_HOME", cfg.getJakartaeeDir().getAbsolutePath());
        env.put("GF_BUNDLE_ZIP", this.glassfishZip.getAbsolutePath());
        env.put("GF_VI_BUNDLE_ZIP", this.glassfishZip.getAbsolutePath());
        env.put("GF_HOME_RI", cfg.getTargetDir().getAbsolutePath() + "/ri/glassfish7");
        env.put("GF_HOME_VI", cfg.getTargetDir().getAbsolutePath() + "/vi/glassfish7");
        env.put("DATABASE", "JavaDB");
        env.put("CLIENT_LOGGING_CFG", cfg.getTargetDir().getAbsolutePath() + "/test-classes/client-logging.properties");

        LOG.log(Level.DEBUG, "Configured environment: \n{0}", env);
    }


    private File toTestReportFile(Path modulePath) {
        StringBuilder name = new StringBuilder(64);
        for (Path path : modulePath) {
            if (name.length() > 0) {
                name.append('_');
            }
            name.append(path);
        }
        name.append("-junit-report.xml");
        return new File(cfg.getTargetDir().toPath().resolve(Path.of("results", "junitreports")).toFile(),
            name.toString());
    }


    private String toReport(Testsuite suite) {
        StringBuilder report = new StringBuilder();
        report.append("Test suite ").append(suite.getName());
        report.append(" failed with ").append(suite.getFailures()).append(" failures and ");
        report.append(suite.getErrors()).append(" errors.");
        List<Testcase> tests = suite.getTestcase();
        if (suite.getFailures() > 0) {
            Testcase test = tests.stream().filter(tc -> !tc.getFailure().isEmpty()).findFirst().get();
            Failure failure = test.getFailure().get(0);
            report.append("\nThe first failure is in class ").append(test.getClassname());
            report.append(" with this message: \n").append(failure.getMessage());
        }
        if (suite.getErrors() > 0) {
            Testcase test = tests.stream().filter(tc -> !tc.getError().isEmpty()).findFirst().get();
            Error error = test.getError().get(0);
            report.append("\nThe first error is in class ").append(test.getClassname());
            report.append(" with this message: \n").append(error.getMessage());
        }
        report.append("\nSee logs for more.");
        return report.toString();
    }
}
