/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.main.itest.tools.asadmin;

import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.universal.process.ProcessManagerTimeoutException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.glassfish.embeddable.GlassFishVariable.JAVA_HOME;
import static org.glassfish.embeddable.GlassFishVariable.JAVA_ROOT;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tool for executing asadmin/asadmin.bat commands. The tool is stateless.
 *
 * @author David Matejcek
 */
public class Asadmin {

    private static final Logger LOG = Logger.getLogger(Asadmin.class.getName());

    private static final int DEFAULT_TIMEOUT_MSEC = 30 * 1000;
    private static final Function<String, KeyAndValue<String>> KEYVAL_SPLITTER = s -> {
        int equalSignPos = s.indexOf('=');
        if (equalSignPos <= 0 || equalSignPos == s.length() - 1) {
            return null;
        }
        return new KeyAndValue<>(s.substring(0, equalSignPos), s.substring(equalSignPos + 1, s.length()));
    };

    private final File asadmin;
    private final String adminUser;
    private final File adminPasswordFile;
    private final boolean terse;
    private final Map<String, String> environment = new HashMap<>();
    private final Map<String, String> additionalPasswords = new HashMap<>();

    /**
     * Creates a stateless instance of the tool.
     *
     * @param asadmin - executable file
     * @param adminUser - username authorized to use the domain
     * @param adminPasswordFile - a file containing admin's password set as
     * <code>AS_ADMIN_PASSWORD=...</code>
     */
    public Asadmin(final File asadmin, final String adminUser, final File adminPasswordFile) {
        this(asadmin, adminUser, adminPasswordFile, false);
    }

    /**
     * Creates a stateless instance of the tool.
     *
     * @param asadmin - executable file
     * @param adminUser - username authorized to use the domain
     * @param adminPasswordFile - a file containing admin's password set as
     * <code>AS_ADMIN_PASSWORD=...</code>
     * @param terse - to produce output, minimized and suitable for parsing.
     */
    public Asadmin(final File asadmin, final String adminUser, final File adminPasswordFile, final boolean terse) {
        this.asadmin = asadmin;
        this.adminUser = adminUser;
        this.adminPasswordFile = adminPasswordFile;
        this.terse = terse;
    }

    /**
     * Adds environment property set for the asadmin execution.
     *
     * @param name
     * @param value
     * @return this
     */
    public Asadmin withEnv(final String name, final String value) {
        this.environment.put(name, value);
        return this;
    }

    /**
     * Adds a password to the password file.
     *
     * @param name Name in the password file
     * @param secretValue Value in the password file
     * @return this
     */
    public Asadmin withPassword(final String name, final String secretValue) {
        this.additionalPasswords.put(name, secretValue);
        return this;
    }

    /**
     * Removes all custom passwords.
     *
     * @return this
     */
    public Asadmin resetPasswords() {
        this.additionalPasswords.clear();
        return this;
    }

    /**
     * @return asadmin command file name
     */
    public String getCommandName() {
        return asadmin.getName();
    }

    public <T> KeyAndValue<T> getValue(final String key, final Function<String, T> transformer) {
        List<KeyAndValue<T>> result = get(key, transformer);
        if (result.isEmpty()) {
            return null;
        }
        if (result.size() > 1) {
            throw new IllegalArgumentException("The key is not concrete enough to get a single value: " + key);
        }
        return result.get(0);
    }

    public <T> List<KeyAndValue<T>> get(final String key, final Function<String, T> transformer) {
        AsadminResult result = exec("get", key);
        assertThat(result, asadminOK());
        return Arrays.stream(result.getStdOut().split(System.lineSeparator())).map(KEYVAL_SPLITTER)
                .filter(Objects::nonNull).map(kv -> new KeyAndValue<>(kv.getKey(), transformer.apply(kv.getValue())))
                .collect(Collectors.toList());
    }


    /**
     * Executes the command with arguments asynchronously with {@value #DEFAULT_TIMEOUT_MSEC} ms
     * timeout. The command can be attached by the attach command. You should find the job id in
     * the {@link AsadminResult#getStdOut()} as <code>Job ID: [0-9]+</code>
     *
     * @param args
     * @return {@link AsadminResult} never null.
     */
    public DetachedTerseAsadminResult execDetached(final String... args) {
        return (DetachedTerseAsadminResult) exec(DEFAULT_TIMEOUT_MSEC, true, args);
    }


    /**
     * Executes the command with arguments synchronously with {@value #DEFAULT_TIMEOUT_MSEC} ms
     * timeout.
     *
     * @param args
     * @return {@link AsadminResult} never null.
     */
    public AsadminResult exec(final String... args) {
        return exec(DEFAULT_TIMEOUT_MSEC, false, args);
    }

    /**
     * Executes the command with arguments synchronously with given timeout in millis.
     *
     * @param timeout timeout in millis
     * @param args command and arguments.
     * @return {@link AsadminResult} never null.
     */
    public AsadminResult exec(final int timeout, final String... args) {
        return exec(timeout, false, args);
    }

    private File getPasswordFile() {
        if (!additionalPasswords.isEmpty()) {
            Objects.requireNonNull(adminPasswordFile, "The admin password file is not set.");
            try {
                final Path tempPasswordFile = Files.createTempFile("pwd", "txt");
                Files.copy(adminPasswordFile.toPath(), tempPasswordFile, StandardCopyOption.REPLACE_EXISTING);
                String additionalContent = additionalPasswords.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("\n"));
                Files.writeString(tempPasswordFile, "\n" + additionalContent, StandardOpenOption.APPEND);
                return tempPasswordFile.toFile();
            } catch (IOException e) {
                throw new IllegalStateException("Could not create the temporary password file.", e);
            }
        }
        return adminPasswordFile;
    }

    /**
     * Executes the command with arguments.
     *
     * @param timeout timeout in millis
     * @param detachedAndTerse detached command is executed asynchronously, can
     * be attached later by the attach command.
     * @param args command and arguments.
     * @return {@link AsadminResult} never null.
     */
    private AsadminResult exec(final int timeout, final boolean detachedAndTerse, final String... args) {
        final List<String> parameters = Arrays.asList(args);
        LOG.log(Level.INFO, "exec(timeout={0}, detached={1}, args={2})",
                new Object[]{timeout, detachedAndTerse, parameters});
        final List<String> command = new ArrayList<>();
        command.add(asadmin.getAbsolutePath());
        command.add("--user");
        command.add(adminUser);
        if (getPasswordFile() != null) {
            command.add("--passwordfile");
            command.add(getPasswordFile().getAbsolutePath());
        }
        if (detachedAndTerse) {
            command.add("--terse=true");
            command.add("--detach");
        } else {
            command.add("--terse=" + terse);
        }
        command.addAll(parameters);

        final ProcessManager processManager = new ProcessManager(command);
        processManager.setTimeoutMsec(timeout);
        processManager.setEcho(false);
        for (Entry<String, String> env : this.environment.entrySet()) {
            processManager.setEnvironment(env.getKey(), env.getValue());
        }
        if (System.getenv("AS_TRACE") == null && LOG.isLoggable(Level.FINEST)) {
            processManager.setEnvironment("AS_TRACE", "true");
        }
        // override any env property to what is used by tests
        processManager.setEnvironment(JAVA_HOME.getEnvName(), System.getProperty(JAVA_HOME.getSystemPropertyName()));
        processManager.setEnvironment(JAVA_ROOT.getEnvName(), System.getProperty(JAVA_HOME.getSystemPropertyName()));

        int exitCode;
        String asadminErrorMessage = "";
        try {
            exitCode = processManager.execute();
        } catch (final ProcessManagerTimeoutException e) {
            asadminErrorMessage = e.getMessage();
            exitCode = 1;
        } catch (final ProcessManagerException e) {
            LOG.log(Level.SEVERE, "The execution failed.", e);
            asadminErrorMessage = e.getMessage();
            exitCode = 1;
        }

        final String stdErr = processManager.getStderr() + '\n' + asadminErrorMessage;
        final AsadminResult result;
        if (detachedAndTerse) {
            result = new DetachedTerseAsadminResult(args[0], exitCode, processManager.getStdout(), stdErr);
        } else {
            result = new AsadminResult(args[0], exitCode, processManager.getStdout(), stdErr);
        }
        if (!result.getStdOut().isEmpty()) {
            System.out.println(result.getStdOut());
        }
        if (!result.getStdErr().isEmpty()) {
            System.err.println(result.getStdErr());
        }
        return result;
    }

}
