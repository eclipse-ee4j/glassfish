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
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Java adapter to call the keytool command.
 * Will be deprecated once JDK would support that in Java.
 *
 * @see <a href="https://bugs.openjdk.org/browse/JDK-8304556">JDK-8304556</a>
 * @see <a href="https://bugs.openjdk.org/browse/JDK-8058778">JDK-8058778</a>
 */
public class KeyTool {

    private static final Logger LOG = System.getLogger(KeyTool.class.getName());
    private static final long EXEC_TIMEOUT = Integer.getInteger("org.glassfish.main.keytool.timeout", 60);
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
    private final String keyStoreType;
    private char[] password;

    /**
     * Creates a new instance of KeyTool managing the keystore file.
     * The file may not exist yet.
     * The type is detected automatically from the file extension.
     *
     * @param keyStore the file representing the keystore
     * @param password keystore and key password, must have at least 6 characters
     */
    public KeyTool(File keyStore, char[] password) {
        this(keyStore, guessKeyStoreType(keyStore), password);
    }


    /**
     * Creates a new instance of KeyTool managing the keystore file.
     * The file may not exist yet.
     *
     * @param keyStore the file representing the keystore
     * @param keyStoreType the type of the keystore, e.g. "PKCS12", "JKS"
     * @param password keystore and key password, must have at least 6 characters
     */
    public KeyTool(File keyStore, String keyStoreType, char[] password) {
        this.keyStore = keyStore;
        this.password = password;
        this.keyStoreType = keyStoreType;
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
            "-storetype", keyStoreType
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
     * @param destKeyStoreFile the destination key store file
     * @param destKeyStorePassword the password for the destination key store
     * @throws IOException if an error occurs during the process
     */
    public void copyCertificate(String alias, File destKeyStoreFile, char[] destKeyStorePassword) throws IOException {
        final KeyStore ks = loadKeyStore();
        final Certificate certificate;
        try {
            certificate = ks.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new IOException(
                "Failed to get the certificate under alias " + alias + " from the keystore " + keyStore);
        }
        if (certificate == null) {
            throw new IOException("Alias " + alias + " not found in the key store: " + keyStore);
        }
        try {
            final KeyTool destKeyStoreTool;
            if (destKeyStoreFile.exists()) {
                destKeyStoreTool = new KeyTool(destKeyStoreFile, destKeyStorePassword);
            } else {
                destKeyStoreTool = createEmptyKeyStore(destKeyStoreFile, destKeyStorePassword);
            }
            final KeyStore destKeyStore = destKeyStoreTool.loadKeyStore();
            destKeyStore.setCertificateEntry(alias, certificate);
            try (FileOutputStream output = new FileOutputStream(destKeyStoreFile)) {
                destKeyStore.store(output, destKeyStorePassword);
            }
        } catch (GeneralSecurityException e) {
            throw new IOException(
                "Could not copy certificate with alias: " + alias + " to key store: " + destKeyStoreFile, e);
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
     * Changes also passwords of all keys in the key store which use the same password.
     *
     * @param newPassword the new key store password
     * @throws IOException
     */
    public void changeKeyStorePassword(char[] newPassword) throws IOException {
        // We grab the current key store, so everything is done in memory until the end.
        final KeyStore ks = loadKeyStore();
        final char[] oldPassword = password;
        this.password = newPassword;
        final List<String> aliases;
        try {
            aliases = Collections.list(ks.aliases());
        } catch (KeyStoreException e) {
            throw new IOException("Could not list aliases in keystore: " + keyStore, e);
        }
        for (String alias : aliases) {
            try {
                if (ks.isKeyEntry(alias)) {
                    changeKeyPassword(ks, alias, oldPassword, newPassword);
                }
            } catch (IOException | KeyStoreException e) {
                LOG.log(WARNING, "Could not change key password for alias: {0}, it may use different password.", alias);
            }
        }
        try (FileOutputStream output = new FileOutputStream(keyStore)) {
            ks.store(output, password);
        } catch (GeneralSecurityException e) {
            throw new IOException(
                "Keystore password successfuly changed, however failed changing key passwords: " + keyStore, e);
        }
    }


    /**
     * Changes the key password
     * <p>
     * WARNING: This is not required for the PKCS12 key store type, as it changes passwords of keys
     * together with the key store password.
     *
     * @param alias the alias of the key whose password should be changed
     * @param oldPassword the current key entry password
     * @param newPassword the new key entry password
     * @throws IOException
     */
    public void changeKeyPassword(String alias, char[] oldPassword, char[] newPassword) throws IOException {
        try {
            KeyStore sourceStore = loadKeyStore();
            Certificate[] chain = sourceStore.getCertificateChain(alias);
            PrivateKey key = (PrivateKey) sourceStore.getKey(alias, oldPassword);
            sourceStore.setKeyEntry(alias, key, newPassword, chain);
            try (FileOutputStream output = new FileOutputStream(keyStore)) {
                sourceStore.store(output, password);
            }
        } catch (GeneralSecurityException e) {
            throw new IOException("Could not change key password for alias: " + alias, e);
        }
    }


    private void execute(final List<String> command, char[]... stdinLines) throws IOException {
        execute(keyStore, command, stdinLines);
    }


    /**
     * Creates an empty key store file with the specified password.
     * The type is detected from the file extension.
     *
     * @param file
     * @param password
     * @return KeyTool suitable to manage the newly created key store
     * @throws IOException
     */
    public static KeyTool createEmptyKeyStore(File file, char[] password) throws IOException {
        return createEmptyKeyStore(file, guessKeyStoreType(file), password);
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


    private static String guessKeyStoreType(File keyStore) {
        String filename = keyStore.getName();
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0) {
            throw new IllegalArgumentException(
                "Key store file name must have an extension to guess the key store type: " + keyStore);
        }
        String suffix = filename.substring(lastDot + 1).toUpperCase();
        switch (suffix) {
            case "JKS":
                return "JKS";
            case "P12":
            case "PFX":
                return "PKCS12";
            case "JCEKS":
                return "JCEKS";
            default:
                LOG.log(WARNING, "Unknown key store type for file {0}, using its suffix as a keystore type.", keyStore);
                return suffix;
        }
    }


    private static void changeKeyPassword(KeyStore keyStore, String alias, char[] oldPassword, char[] newPassword)
        throws IOException {
        try {
            Certificate[] chain = keyStore.getCertificateChain(alias);
            PrivateKey key = (PrivateKey) keyStore.getKey(alias, oldPassword);
            keyStore.setKeyEntry(alias, key, newPassword, chain);
        } catch (GeneralSecurityException e) {
            throw new IOException("Could not change key password for alias: " + alias, e);
        }
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
                throw new IOException(
                    "KeyTool command timed out after " + EXEC_TIMEOUT + " seconds. Output: " + getOutput(process));
            }
            final int exitCode = process.exitValue();
            final String output = getOutput(process);
            LOG.log(DEBUG, () -> "Command output: " + output);
            if (exitCode != 0) {
                throw new IOException("KeyTool command failed with exit code: " + exitCode + " and output: " + output);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted", e);
        }
    }


    private static String getOutput(final Process process) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output);
        process.getErrorStream().transferTo(output);
        return output.toString(Charset.defaultCharset());
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
