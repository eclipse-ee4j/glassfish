/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.jdke.security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.System.Logger;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.System.Logger.Level.INFO;

/**
 *
 */
public class KeyTool {

    private static final Logger LOG = System.getLogger(KeyTool.class.getName());
    private static final String KEYTOOL;

    static {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null || javaHome.isEmpty()) {
            throw new IllegalStateException("java.home system property is not set. Cannot locate keytool.");
        }
        String keyToolFileName = System.getProperty("os.name").toLowerCase().contains("windows")
            ? "keytool.exe"
            : "keytool";
        KEYTOOL = new File(new File(javaHome, "bin"), keyToolFileName).getAbsolutePath();

    }

    private final File keyStore;
    private final char[] password;

    /**
     * Creates a new instance of KeyTool managing the repository.
     * The repository may not exist yet.
     *
     * @param keyStore the file representing the keystore
     */
    public KeyTool(File keyStore, char[] password) {
        this.keyStore = keyStore;
        this.password = password;
    }


    /**
     * Generates a key pair in a new keystore.
     *
     * @param alias
     * @param dn
     * @param keyAlgorithm
     * @param certValidity
     */
    public void generateKeyPair(String alias, String dn, String keyAlgorithm, int certValidity) {
        List<String> command = List.of(
            KEYTOOL,
            "-genkeypair",
            "-alias", alias,
            "-dname", dn,
            "-keyalg", keyAlgorithm,
            "-validity", Integer.toString(certValidity),
            "-keystore", keyStore.getAbsolutePath(),
            "-storetype", "JKS"
        );
        LOG.log(INFO, "Executing command: {0}", command);
        final ProcessBuilder builder = new ProcessBuilder(command).directory(keyStore.getParentFile());
        final Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new IllegalStateException("Could not execute command: " + builder.command(), e);
        }
        try (Writer stdin = new OutputStreamWriter(process.getOutputStream(), Charset.defaultCharset())) {
            // new keyStore
            writePassword(stdin);
            writePassword(stdin);
            // new key
            writePassword(stdin);
            writePassword(stdin);
            if (!process.waitFor(1, TimeUnit.MINUTES)) {
                throw new IllegalStateException("KeyTool command timed out after 60 seconds");
            }
            final int exitCode = process.exitValue();
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            process.getInputStream().transferTo(output);
            process.getErrorStream().transferTo(output);
            LOG.log(INFO, () -> "Command output: " + output.toString(Charset.defaultCharset()));
            if (exitCode != 0) {
                throw new IllegalStateException("KeyTool command failed with exit code: " + exitCode);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize keystore " + keyStore, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted", e);
        }
    }


    private void writePassword(Writer stdin) throws IOException {
        stdin.write(password);
        stdin.write(System.lineSeparator());
        stdin.flush();
    }
}
