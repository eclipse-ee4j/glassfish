/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.cluster.ssh.util;

import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.glassfish.api.admin.CommandException;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * @author Rajiv Mordani
 */
public class SSHUtil {

    /** List of supported SSH key file names */
    public static final List<String> SSH_KEY_FILE_NAMES = List.of("id_rsa", "id_dsa", "id_ecdsa", "identity");

    /**
     * @return null or one of {@link #SSH_KEY_FILE_NAMES} at user's home directory
     */
    public static File getExistingKeyFile() {
        Path h = FileUtils.USER_HOME.toPath();
        for (String keyName : SSH_KEY_FILE_NAMES) {
            File f = h.resolve(Path.of(".ssh", keyName)).toFile();
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }


    /**
     * @return .ssh/id_rsa in the current user's home directory.
     */
    public static File getDefaultKeyFile() {
        return FileUtils.USER_HOME.toPath().resolve(Path.of(".ssh", "id_rsa")).toFile();
    }

    /**
     * Simple method to validate an encrypted key file
     *
     * @param keyFile
     * @return true if the key file is encrypted using standard format
     * @throws CommandException
     */
    public static boolean isEncryptedKey(File keyFile) throws CommandException {
        try {
            String f = FileUtils.readSmallFile(keyFile, ISO_8859_1).trim();
            if (f.startsWith("-----BEGIN ") && f.contains("ENCRYPTED") && f.endsWith(" PRIVATE KEY-----")) {
                return true;
            }
            return false;
        } catch (IOException ioe) {
            throw new CommandException(Strings.get("error.parsing.key", keyFile, ioe.getMessage()), ioe);
        }
    }


    /**
     * This method validates either private or public key file.
     * In case of private key, it parses the key file contents to verify if it indeed contains a key
     *
     * @param file the key file
     * @throws CommandException
     */
    public static void validateKeyFile(File file) throws CommandException {
        if (!file.exists()) {
            throw new CommandException(Strings.get("key.does.not.exist", file));
        }
        if (!file.getName().endsWith(".pub")) {
            final String key;
            try {
                key = FileUtils.readSmallFile(file, ISO_8859_1).trim();
            } catch (IOException ioe) {
                throw new CommandException(Strings.get("unable.to.read.key", file, ioe.getMessage()));
            }
            if (!key.startsWith("-----BEGIN ") && !key.endsWith(" PRIVATE KEY-----")) {
                throw new CommandException(Strings.get("invalid.key.file", file));
            }
        }
    }
}
