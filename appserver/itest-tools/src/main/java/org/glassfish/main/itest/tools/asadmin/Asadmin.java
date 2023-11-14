/*
 * Copyright (c) 2022, 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tool for executing asadmin/asadmin.bat commands.
 * The tool is stateless.
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
    private final Map<String, String> environment = new HashMap<>();


    /**
     * Creates a stateless instance of the tool.
     *
     * @param asadmin - executable file
     * @param adminUser - username authorized to use the domain
     * @param adminPasswordFile - a file containing admin's password set as <code>AS_ADMIN_PASSWORD=...</code>
     */
    public Asadmin(final File asadmin, final String adminUser, final File adminPasswordFile) {
        this.asadmin = asadmin;
        this.adminUser = adminUser;
        this.adminPasswordFile = adminPasswordFile;
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
     * Executes the command with arguments asynchronously with {@value #DEFAULT_TIMEOUT_MSEC} ms timeout.
     * The command can be attached by the attach command.
     * You should find the job id in the {@link AsadminResult#getStdOut()} as <code>Job ID: [0-9]+</code>
     *
     * @param args
     * @return {@link AsadminResult} never null.
     */
    public DetachedTerseAsadminResult execDetached(final String... args) {
        return (DetachedTerseAsadminResult) exec(DEFAULT_TIMEOUT_MSEC, true, args);
    }

    /**
     * Executes the command with arguments synchronously with {@value #DEFAULT_TIMEOUT_MSEC} ms timeout.
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

    /**
     * Executes the command with arguments.
     *
     * @param timeout timeout in millis
     * @param detachedAndTerse detached command is executed asynchronously, can be attached later by the attach command.
     * @param args command and arguments.
     * @return {@link AsadminResult} never null.
     */
    private AsadminResult exec(final int timeout, final boolean detachedAndTerse, final String... args) {
        final List<String> parameters = Arrays.asList(args);
        LOG.log(Level.INFO, "exec(timeout={0}, detached={1}, args={2})",
            new Object[] {timeout, detachedAndTerse, parameters});
        final List<String> command = new ArrayList<>();
        command.add(asadmin.getAbsolutePath());
        command.add("--user");
        command.add(adminUser);
        command.add("--passwordfile");
        command.add(adminPasswordFile.getAbsolutePath());
        if (detachedAndTerse) {
            command.add("--terse");
            command.add("--detach");
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
        processManager.setEnvironment("JAVA_HOME", System.getProperty("java.home"));
        processManager.setEnvironment("AS_JAVA", System.getProperty("java.home"));

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
