/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * TCK Runner prepares or resets the workspace, runs the test and stops all servers after
 * the test.
 *
 * @author David Matejcek
 */
public class TckRunner {

    private static final Logger LOG = System.getLogger(TckRunner.class.getName());
    private static final int MAIL_SMTP_PORT = 3025;
    private static final int MAIL_IMAP_PORT = 3143;
    private static final String JPMS_ADDITIONS = "--add-opens=java.naming/javax.naming.spi=org.glassfish.main.jdke"
        + " --add-opens=java.base/java.io=ALL-UNNAMED"
        + " --add-opens=java.base/java.lang=ALL-UNNAMED"
        + " --add-opens=java.base/java.util=ALL-UNNAMED"
        + " --add-opens=java.base/sun.net.www.protocol.jrt=ALL-UNNAMED"
        + " --add-opens=java.naming/javax.naming.spi=ALL-UNNAMED"
        + " --add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED"
        + " --add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED"
        + " --add-exports=java.naming/com.sun.jndi.ldap=ALL-UNNAMED";

    private final TckConfiguration cfg;
    private GenericContainer<?> mailServer;
    private File glassfishZip;

    /**
     * Just creates this instance.
     *
     * @param cfg
     */
    public TckRunner(final TckConfiguration cfg) {
        this.cfg = cfg;
        LOG.log(Level.INFO, "TckRunner configuration: \n{0}", cfg);
    }


    /**
     * Unpacks the TCK and resolves glassFish zip  as maven dependencies.
     */
    public void prepareWorkspace() {
        LOG.log(Level.INFO, "Preparing workspace at {0}", cfg.getTargetDir());
        ZipResolver zipResolver = new ZipResolver(cfg.getTargetDir(), cfg.getSettingsXml());
        if (cfg.getJakartaeeDir().exists()) {
            LOG.log(Level.INFO, "Jakarta EE was already installed, unzipping to {0} skipped.", cfg.getJakartaeeDir());
        } else {
            zipResolver.unzipDependency("org.glassfish.main.tests.tck", "jakarta-platform-tck", cfg.getTckVersion());
            File command = cfg.getJakartaeetckCommand().toFile();
            if (command.exists()) {
                command.setExecutable(true);
            } else {
                throw new IllegalStateException("The TCK runnable doesn't exist: " + command);
            }
            fixTsJteFiles();
        }
        glassfishZip = zipResolver.getZipFile("org.glassfish.main.distributions", "glassfish", cfg.getGlassFishVersion());
    }


    /**
     * Starts the TestContainer's docker container providing a mail server for tests.
     * The server can be stopped with other servers by the {@link #stopServers()} method.
     */
    // stopped in #stopServers
    @SuppressWarnings("resource")
    public void startMailServer() {
        mailServer = new GenericContainer<>(DockerImageName.parse("greenmail/standalone:1.6.10"))
        .waitingFor(Wait.forLogMessage(".*Starting GreenMail standalone.*", 1))
        .withEnv("GREENMAIL_OPTS", "-Dgreenmail.setup.test.all -Dgreenmail.hostname=0.0.0.0 -Dgreenmail.auth.disabled")
        .withExposedPorts(MAIL_SMTP_PORT, 3110, MAIL_IMAP_PORT, 3465, 3993, 3995, 8080);

        mailServer.start();
    }


    /**
     * Starts tests. Expects that the workspace was initialized by the {@link #prepareWorkspace()}
     * method or reset by {@link #deleteGeneratedWorkspaceDirs()}, all servers managed by the TCK
     * are stopped, mail server was started if it is required by the test by the
     * {@link #startMailServer()} method.
     * <p>
     * Call of this method may take minutes to hours, so be patient.
     * <p>
     * You should stop all servers by the {@link #stopServers()} method after the test.
     *
     * @param modulePath path to selected tests
     */
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
     * <p>
     * Also stops the mailserver if it was started by the {@link #startMailServer()} method.
     */
    public void stopServers() {
        if (mailServer != null) {
            mailServer.stop();
            mailServer.close();
            mailServer = null;
        }
    }


    /**
     * Results are already gzipped and we need to run another module, so we remove generated files
     * from previous executions.
     * @throws IOException on failure
     */
    public void deleteGeneratedWorkspaceDirs() throws IOException {
        deleteDirectory(cfg.getTargetDir().toPath().resolve("ri"));
        deleteDirectory(cfg.getTargetDir().toPath().resolve("vi"));
        deleteDirectory(new File("/tmp/DerbyDB").toPath());
        deleteDirectory(new File(cfg.getTargetDir(), "jakartaeetck-work").toPath());
    }

    private void fixTsJteFiles() {
        try {
            Files.walk(cfg.getJakartaeeDir().toPath())
                .filter(path -> path.getFileName().toString().equals("ts.jte"))
                .forEach(this::fixContent);
        } catch (IOException e) {
            throw new IllegalStateException("Could not fix ts.jte files!", e);
        }
    }

    private void fixContent(Path file) {
        try {
            String content = Files.readString(file);
            content = content.replace("s1as.truststore=${s1as.domain}/config/cacerts.jks",
                "s1as.truststore=${s1as.domain}/config/cacerts.p12");
            content = content.replace("ri.truststore=${ri.domain}/config/cacerts.jks",
                "ri.truststore=${ri.domain}/config/cacerts.p12");

            content = content.replace("${JVMOPTS_RUNTESTCOMMAND}", JPMS_ADDITIONS);
            Files.writeString(file, content);
            LOG.log(Level.INFO, "Fixed file: {0}", file);
        } catch (IOException e) {
            throw new IllegalStateException("Could not fix file: " + file, e);
        }
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
        env.put("LC_ALL", "C");
        env.put("LANG", "en");
        env.put("WORKSPACE", cfg.getTargetDir().getAbsolutePath());
        env.put("JAVA_HOME", cfg.getJdkDirectory().getAbsolutePath());
        env.put("JDK17_HOME", cfg.getJdkDirectory().getAbsolutePath());
        env.put("JDK", "JDK17");
        if (mailServer != null) {
            env.put("MAIL_HOST", mailServer.getHost());
            env.put("SMTP_PORT", String.valueOf(mailServer.getMappedPort(MAIL_SMTP_PORT)));
            env.put("IMAP_PORT", String.valueOf(mailServer.getMappedPort(MAIL_IMAP_PORT)));
        }
        env.put("HARNESS_DEBUG", String.valueOf(cfg.isHarnessLoggingEnabled()));
        env.put("AS_DEBUG", String.valueOf(cfg.isAsadminLoggingEnabled()));

        env.put("PATH", String.join(":",
            cfg.getJdkDirectory().getAbsolutePath() + "/bin",
            cfg.getAntDirectory().getAbsolutePath() + "/bin",
            "/opt/homebrew/bin", // For macOs, to override BSD tar with GNU Tar etc
            "/usr/bin",
            "/bin"));
        env.put("CTS_HOME", cfg.getTargetDir().getAbsolutePath());
        env.put("TS_HOME", cfg.getJakartaeeDir().getAbsolutePath());
        env.put("GF_BUNDLE_ZIP", this.glassfishZip.getAbsolutePath());
        env.put("GF_VI_BUNDLE_ZIP", this.glassfishZip.getAbsolutePath());
        env.put("GF_HOME_RI", cfg.getTargetDir().getAbsolutePath() + "/ri/glassfish8");
        env.put("GF_HOME_VI", cfg.getTargetDir().getAbsolutePath() + "/vi/glassfish8");
        env.put("GF_LOGGING_CFG_RI", cfg.getServerLoggingProperties().getAbsolutePath());
        env.put("GF_LOGGING_CFG_VI", cfg.getServerLoggingProperties().getAbsolutePath());
        env.put("DATABASE", "JavaDB");
        env.put("CLIENT_LOGGING_CFG", cfg.getClientLoggingProperties().getAbsolutePath());
        env.put("ENABLE_RERUN", "false");

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
        report.append(" finished at ").append(suite.getTimestamp());
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
