/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.itest.tools;

import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.universal.process.ProcessManagerTimeoutException;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tool for executing keytool/keytool.bat commands.
 * The tool is stateless.
 */
public class KeyTool {

    private static final Logger LOG = System.getLogger(KeyTool.class.getName());

    private final File keytool;

    /**
     * @param keytool
     */
    public KeyTool(File keytool) {
        this.keytool = keytool;
    }


    /**
     * Executes the command with arguments.
     *
     * @param args
     */
    public void exec(final String... args) {
        final List<String> parameters = Arrays.asList(args);
        LOG.log(Level.INFO, "exec(args={0})", parameters);
        final List<String> command = new ArrayList<>();
        command.add(keytool.getAbsolutePath());
        command.add("-noprompt");
        command.addAll(parameters);

        final ProcessManager processManager = new ProcessManager(command);
        processManager.setTimeout(60_000);
        processManager.setEcho(true);

        int exitCode;
        String asadminErrorMessage = "";
        try {
            exitCode = processManager.execute();
        } catch (final ProcessManagerTimeoutException e) {
            asadminErrorMessage = e.getMessage();
            exitCode = 1;
        } catch (final ProcessManagerException e) {
            LOG.log(Level.ERROR, "The execution failed.", e);
            asadminErrorMessage = e.getMessage();
            exitCode = 1;
        }

        final String stdErr = processManager.getStderr() + '\n' + asadminErrorMessage;
        if (!processManager.getStdout().isEmpty()) {
            System.out.println(processManager.getStdout());
        }
        if (!processManager.getStderr().isEmpty()) {
            System.err.println(processManager.getStderr());
        }
        if (exitCode != 0) {
            throw new RuntimeException(stdErr);
        }
    }


    /**
     * Loads the {@link KeyStore} from a file.
     * @param inputFile
     * @param password
     * @return {@link KeyStore}
     */
    public static KeyStore loadKeyStore(File inputFile, char[] password) {
        try {
            return KeyStore.getInstance(inputFile, password);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new IllegalStateException("Could not load the keystore from " + inputFile, e);
        }
    }

}
