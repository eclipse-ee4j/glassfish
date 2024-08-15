/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import com.jcraft.jsch.Session;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.glassfish.api.admin.CommandException;

/**
 * @author Rajiv Mordani
 */
public class SSHUtil {

    private static final List<Session> activeConnections = new ArrayList<>();
    private static final String NL = System.lineSeparator();

    /**
     * Registers a connection for cleanup when the plugin is stopped.
     *
     * @param session The connection.
     */
    public static synchronized void register(Session session) {
        if (!activeConnections.contains(session)) {
            activeConnections.add(session);
        }
    }


    /**
     * Unregisters a connection for cleanup when the plugin is stopped.
     *
     * @param session The connection.
     */
    public static synchronized void unregister(Session session) {
        session.disconnect();
        activeConnections.remove(session);
    }


    /**
     * Convert empty string to null.
     */
    public static String checkString(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return s;
    }


    /**
     * @return null or id_rsa/id_dsa/identity at user's home directory
     */
    public static File getExistingKeyFile() {
        Path h = FileUtils.USER_HOME.toPath();
        for (String keyName : Arrays.asList("id_rsa", "id_dsa", "identity")) {
            File f = h.resolve(Path.of(".ssh", keyName)).toFile();
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }


    public static File getDefaultKeyFile() {
        return FileUtils.USER_HOME.toPath().resolve(Path.of(".ssh", "id_rsa")).toFile();
    }

    /**
     * Simple method to validate an encrypted key file
     * @return true|false
     * @throws CommandException
     */
    public static boolean isEncryptedKey(File keyFile) throws CommandException {
        boolean res = false;
        try {
            String f = FileUtils.readSmallFile(keyFile);
            if (f.startsWith("-----BEGIN ") && f.contains("ENCRYPTED")
                    && f.endsWith(" PRIVATE KEY-----" + NL)) {
                res=true;
            }
        }
        catch (IOException ioe) {
            throw new CommandException(Strings.get("error.parsing.key", keyFile, ioe.getMessage()), ioe);
        }
        return res;
    }


    /**
     * This method validates either private or public key file. In case of private
     * key, it parses the key file contents to verify if it indeed contains a key
     * @param  file the key file
     * @return success if file exists, false otherwise
     */
    public static boolean validateKeyFile(File file) throws CommandException {
        if (!file.exists()) {
            throw new CommandException(Strings.get("key.does.not.exist", file));
        }
        if (!file.getName().endsWith(".pub")) {
            String key = null;
            try {
                key = FileUtils.readSmallFile(file);
            } catch (IOException ioe) {
                throw new CommandException(Strings.get("unable.to.read.key", file, ioe.getMessage()));
            }
            if (!key.startsWith("-----BEGIN ") && !key.endsWith(" PRIVATE KEY-----" + NL)) {
                throw new CommandException(Strings.get("invalid.key.file", file));
            }
        }
        return true;
    }
}
