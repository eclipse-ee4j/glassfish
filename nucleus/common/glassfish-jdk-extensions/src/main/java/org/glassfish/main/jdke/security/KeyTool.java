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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.System.Logger;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

/**
 * Java adapter to call the keytool command.
 * Will be deprecated once JDK would support that in Java.
 *
 * @see <a href="https://bugs.openjdk.org/browse/JDK-8304556">JDK-8304556</a>
 * @see <a href="https://bugs.openjdk.org/browse/JDK-8058778">JDK-8058778</a>
 */
public class KeyTool {

    private static final Logger LOG = System.getLogger(KeyTool.class.getName());
    private static final long EXEC_TIMEOUT = 10;
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
    private char[] password;

    /**
     * Creates a new instance of KeyTool managing the keystore file.
     * The file may not exist yet.
     *
     * @param keyStore the file representing the keystore
     * @param password keystore and key password, must have at least 6 characters
     */
    public KeyTool(File keyStore, char[] password) {
        this.keyStore = keyStore;
        this.password = password;
    }


    /**
     * Loads the key store from the file.
     *
     * @return {@link KeyStore}
     * @throws IOException
     */
    public KeyStore loadKeyStore() throws IOException {
        try {
            return KeyStore.getInstance(keyStore, password);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new IOException("Could not load keystore: " + keyStore, e);
        }
    }


    /**
     * Generates a key pair in a new keystore.
     *
     * @param alias certificate alias (self-signed certificate)
     * @param dn distinguished name, e.g. "CN=localhost, OU=Development, O=Example, L=City, ST=State, C=Country"
     * @param keyAlgorithm the key algorithm, e.g. "RSA", "DSA", "EC"
     * @param certValidity the validity of the certificate in days, must be positive
     * @throws IOException
     */
    public void generateKeyPair(String alias, String dn, String keyAlgorithm, int certValidity) throws IOException {
        final List<String> command = List.of(
            KEYTOOL,
            "-J-Duser.language=en",
            "-noprompt",
            "-genkeypair",
            "-alias", alias,
            "-dname", dn,
            "-keyalg", keyAlgorithm,
            "-validity", Integer.toString(certValidity),
            "-keystore", keyStore.getAbsolutePath(),
            "-storetype", "JKS"
        );
        if (keyStore.getParentFile().mkdirs()) {
            // The directory must exist, keytool will not create it
            LOG.log(DEBUG, "Created directory for keystore: {0}", keyStore.getParentFile());
        }
        // 4 times - once for key store, once for key password, each once more for confirmation
        execute(command, password, password, password, password);
    }


    /**
     * Copies a certificate from the key store to another key store.
     * The destination key store will be created if it does not exist.
     * The destination key store will use the same password as the source key store.
     *
     * @param alias the alias of the certificate to copy
     * @param destKeyStore the destination key store file
     * @throws IOException if an error occurs during the process
     */
    public void copyCertificate(String alias, File destKeyStore) throws IOException {
        copyCertificate(alias, destKeyStore, password);
    }


    /**
     * Copies a certificate from the key store to another key store.
     * The destination key store of the same type will be created if it does not exist.
     *
     * @param alias the alias of the certificate to copy
     * @param destKeyStore the destination key store file
     * @param destKeyStorePassword the password for the destination key store
     * @throws IOException if an error occurs during the process
     */
    public void copyCertificate(String alias, File destKeyStore, char[] destKeyStorePassword) throws IOException {
        final File certFile = File.createTempFile(alias, ".cer");
        try {
            certFile.delete();
            exportCertificate(alias, certFile);
            final List<String> importCommand = List.of(
                KEYTOOL,
                "-J-Duser.language=en",
                "-noprompt",
                "-importcert",
                "-alias", alias,
                "-trustcacerts",
                "-keystore", destKeyStore.getAbsolutePath(),
                "-file", certFile.getAbsolutePath()
                );
            // 3 times - once for key store, once for key password, once more for confirmation
            // 1 times if the key store does not exist.
            execute(importCommand, destKeyStorePassword, destKeyStorePassword, destKeyStorePassword);
        } finally {
            if (certFile.exists() && !certFile.delete()) {
                LOG.log(ERROR, "Failed to delete temporary certificate file: {0}", certFile);
            }
        }
    }


    /**
     * Exports a certificate from the key store to a file.
     *
     * @param alias the alias of the certificate to export
     * @param outputFile the file to write the certificate to. It must not exist yet.
     * @throws IOException if an error occurs during the process
     */
    public void exportCertificate(String alias, final File outputFile) throws IOException {
        final List<String> exportCommand = List.of(
            KEYTOOL,
            "-J-Duser.language=en",
            "-noprompt",
            "-exportcert",
            "-alias", alias,
            "-keystore", keyStore.getAbsolutePath(),
            "-file", outputFile.getAbsolutePath()
            );
        execute(exportCommand, password);
    }


    /**
     * Changes the key store password and remembers it.
     *
     * @param newPassword the new key store password
     * @throws IOException
     */
    public void changeKeyStorePassword(char[] newPassword) throws IOException {
        List<String> command = List.of(
            KEYTOOL,
            "-J-Duser.language=en",
            "-noprompt",
            "-storepasswd",
            "-keystore", this.keyStore.getAbsolutePath()
        );
        execute(command, password, newPassword, newPassword, newPassword);
        this.password = newPassword;
    }


    /**
     * Changes the key password
     *
     * @param alias the alias of the key whose password should be changed
     * @param oldPassword the current key entry password
     * @param newPassword the new key entry password
     * @throws IOException
     */
    public void changeKeyPassword(String alias, char[] oldPassword, char[] newPassword) throws IOException {
        List<String> command = List.of(
            KEYTOOL,
            "-J-Duser.language=en",
            "-noprompt",
            "-keypasswd",
            "-alias", alias,
            "-keystore", this.keyStore.getAbsolutePath()
        );

        execute(command, password, newPassword, newPassword);
    }


    private void execute(final List<String> command, char[]... stdinLines) throws IOException {
        execute(keyStore, command, stdinLines);
    }


    /**
     * Creates an empty key store file with the specified type and password.
     *
     * @param file
     * @param keyStoreType
     * @param password
     * @return KeyTool suitable to manage the newly created key store
     * @throws IOException
     */
    public static KeyTool createEmptyKeyStore(File file, String keyStoreType, char[] password) throws IOException {
        if (file == null || password == null) {
            throw new IllegalArgumentException(
                "Key store file and password must not be null (usually must have at least 6 characters,"
                    + " depends on keystore implementation).");
        }
        try {
            KeyStore cacerts = KeyStore.getInstance(keyStoreType);
            cacerts.load(null, password);
            try (FileOutputStream output = new FileOutputStream(file)) {
                cacerts.store(output, password);
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new IOException("Could not create new keystore: " + file, e);
        }
        return new KeyTool(file, password);
    }


    private static void execute(final File keyStore, final List<String> command, final char[]... stdinLines) throws IOException {
        LOG.log(INFO, () -> "Executing command: " + command.stream().collect(Collectors.joining(" ")));
        final ProcessBuilder builder = new ProcessBuilder(command).directory(keyStore.getParentFile());
        final Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new IOException("Could not execute command: " + builder.command(), e);
        }

        try (Writer stdin = new OutputStreamWriter(process.getOutputStream(), Charset.defaultCharset())) {
            if (stdinLines != null && stdinLines.length > 0) {
                writeStdIn(stdinLines, stdin);
            }

            if (!process.waitFor(EXEC_TIMEOUT, TimeUnit.SECONDS)) {
                throw new IOException("KeyTool command timed out after " + EXEC_TIMEOUT + " seconds");
            }
            final int exitCode = process.exitValue();
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            process.getInputStream().transferTo(output);
            process.getErrorStream().transferTo(output);
            LOG.log(DEBUG, () -> "Command output: " + output.toString(Charset.defaultCharset()));
            if (exitCode != 0) {
                throw new IOException("KeyTool command failed with exit code: " + exitCode + " and output:"
                    + output.toString(Charset.defaultCharset()));
            }
        } catch (IOException e) {
            throw new IOException("Failed to initialize keystore " + keyStore, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted", e);
        }
    }


    private static void writeStdIn(char[][] stdinLines, Writer stdin) throws IOException {
        for (char[] line : stdinLines) {
            writeLine(line, stdin);
        }
    }


    /**
     * @param content line without line ending
     * @param stdin target writer to write the line to
     * @throws IOException
     */
    private static void writeLine(char[] content, Writer stdin) throws IOException {
        stdin.write(content);
        stdin.write(System.lineSeparator());
        stdin.flush();
    }
}
