/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.store;

import com.sun.enterprise.util.CULoggerInfo;
import com.sun.enterprise.util.io.FileUtils;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Logger;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.glassfish.main.jdke.security.KeyTool;

import static org.glassfish.embeddable.GlassFishVariable.KEYSTORE_FILE;
import static org.glassfish.embeddable.GlassFishVariable.TRUSTSTORE_PASSWORD;

/**
 * Various utility methods related to certificate-based security.
 * <p>
 * In particular, this class opens both the client-side keystore and the client-side truststore when either one is requested.
 * This allows us to prompt only once for the master password (if necessary) without storing the password the user responds with
 * which would be a security risk.
 *
 * @author Tim Quinn (with portions refactored from elsewhere)
 */
public class AsadminSecurityUtil {

    /** Existing writable GlassFish client directory. It is used for caching data, locating SSH keys, etc. */
    public static final File GF_CLIENT_DIR = initGfClientDir();

    private static final Logger logger = CULoggerInfo.getLogger();
    private static final LocalStringsImpl strmgr = new LocalStringsImpl(AsadminSecurityUtil.class);

    private static AsadminSecurityUtil instance;

    private AsadminTruststore asadminTruststore;
    private KeyStore asadminKeystore;


    private static File initGfClientDir() {
        String env = System.getenv("GF_CLIENT_DIR");
        File clientDir = env == null ? new File(FileUtils.USER_HOME, ".gfclient") : new File(env);
        FileUtils.ensureWritableDir(clientDir);
        return clientDir;
    }


    /**
     * @param host
     * @param port
     * @return $GF_CLIENT_DIR/cache/{host}_{port}/session
     */
    public static File getGfClientSessionFile(final String host, final int port) {
        Path sessionFilePath = Path.of("cache", host + "_" + port, "session");
        File file = GF_CLIENT_DIR.toPath().resolve(sessionFilePath).toFile();
        FileUtils.ensureWritableDir(file.getParentFile());
        return file;
    }


    /**
     * Returns the usable instance, creating it if needed.
     *
     * @param commandLineMasterPassword password provided via the command line
     * @param isPromptable if the command requiring the object was run by a human who is present to respond to a prompt for the
     * master password
     * @return the usable instance
     */
    public synchronized static AsadminSecurityUtil getInstance(final char[] commandLineMasterPassword, final boolean isPromptable) {
        if (instance == null) {
            instance = new AsadminSecurityUtil(commandLineMasterPassword, isPromptable);
        }

        return instance;
    }

    /**
     * Returns the usable instance, creating it if needed.
     *
     * @param isPromptable if the command requiring the object was run by a human who is present to respond to a prompt for the
     * master password
     * @return
     */
    public synchronized static AsadminSecurityUtil getInstance(final boolean isPromptable) {
        return getInstance(null, isPromptable);
    }

    private AsadminSecurityUtil(final char[] commandLineMasterPassword, final boolean isPromptable) {
        try {
            init(commandLineMasterPassword, isPromptable);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * If we fail to open the client database using the default password (changeit) or the password found in
     * "javax.net.ssl.trustStorePassword" system property, then the fallback behavior is to prompt the user for the password by
     * calling this method.
     *
     * @return the password to the client side truststore
     */
    private char[] promptForPassword() throws IOException {
        Console console = System.console();
        if (console != null) {
            return console.readPassword(strmgr.get("certificateDbPrompt"));
        }

        return null;
    }

    /**
     * Returns the opened AsadminTruststore object.
     *
     * @return the AsadminTruststore object
     */
    public AsadminTruststore getAsadminTruststore() {
        return asadminTruststore;
    }

    public KeyStore getAsadminKeystore() {
        return asadminKeystore;
    }

    private void init(final char[] commandLineMasterPassword, final boolean isPromptable)
        throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        char[] passwordToUse = chooseMasterPassword(commandLineMasterPassword);
        try {

            /*
             * Open the keystore if the user has specified one using
             * the standard system property.  That would allow users to add a
             * key to a client-side keystore and use SSL client auth from
             * asadmin to the DAS (if they have added the corresponding cert to
             * the DAS truststore).
             */
            asadminKeystore = openKeystore(passwordToUse);
            if (asadminKeystore == null) {
                logger.finer("Skipped loading keystore - location null");
            } else {
                logger.finer("Loaded keystore using command or default master password");
            }
        } catch (IOException ex) {
            if (ex.getCause() instanceof UnrecoverableKeyException) {
                // The password did not allow access to the keystore.
                // Prompt the user if possible.
                if (!isPromptable) {
                    throw ex;
                }

                passwordToUse = promptForPassword();
                if (passwordToUse == null) {
                    throw new IllegalArgumentException();
                }

                asadminKeystore = openKeystore(passwordToUse);
                logger.finer("Loaded keystore using prompted master password");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /*
         * The keystore has been opened successfully, using passwordToUse.
         * Open the truststore with that password.
         */
        asadminTruststore = openTruststore(passwordToUse);
    }

    private AsadminTruststore openTruststore(final char[] password)
        throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException {
        return new AsadminTruststore(password);
    }

    /**
     * Open the keystore, using the password provided.
     *
     * @param candidateMasterPassword password to use in opening the keystore
     * @return opened keystore
     * @throws KeyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     */
    private KeyStore openKeystore(final char[] candidateMasterPassword)
        throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        final File keystoreFile = getAsadminKeyStoreFile();
        if (keystoreFile == null) {
            return null;
        }
        return new KeyTool(keystoreFile, candidateMasterPassword).loadKeyStore();
    }

    /**
     * Returns the master password passed on the command line or, if none, the default master password.
     *
     * @param commandMasterPassword master password passed on the command line; null if none
     * @return master password to use
     */
    private char[] chooseMasterPassword(final char[] commandMasterPassword) {
        return commandMasterPassword == null ? defaultMasterPassword() : commandMasterPassword;
    }

    /**
     * Returns an open stream to the keystore.
     *
     * @return keystore file or null
     * @throws FileNotFoundException
     */
    private File getAsadminKeyStoreFile() throws FileNotFoundException {
        String location = System.getProperty(KEYSTORE_FILE.getSystemPropertyName());
        if (location == null) {
            return null;
        }
        return new File(location);
    }

    private char[] defaultMasterPassword() {
        return System.getProperty(TRUSTSTORE_PASSWORD.getSystemPropertyName(), "changeit").toCharArray();
    }

}
