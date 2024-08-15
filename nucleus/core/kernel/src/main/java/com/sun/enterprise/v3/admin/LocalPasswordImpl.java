/*
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

package com.sun.enterprise.v3.admin;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.InitRunLevel;
import org.glassfish.internal.api.LocalPassword;
import org.glassfish.kernel.KernelLoggerInfo;
import org.jvnet.hk2.annotations.Service;

/**
 * Manage a local password, which is a cryptographically secure random number stored in a file with permissions that
 * only allow the owner to read it. A new local password is generated each time the server starts. The asadmin client
 * can use it to authenticate when executing local commands, such as stop-domain, without the user needing to supply a
 * password.
 *
 * @author Bill Shannon
 */
@Service
@RunLevel(InitRunLevel.VAL)
public class LocalPasswordImpl implements PostConstruct, LocalPassword {

    @Inject
    ServerEnvironment env;

    private String password;

    private static final String LOCAL_PASSWORD_FILE = "local-password";
    private static final int PASSWORD_BYTES = 20;
    private static final char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static final Logger logger = KernelLoggerInfo.getLogger();

    /**
     * Generate a local password and save it in the local-password file.
     */
    @Override
    public void postConstruct() {
        logger.fine("Generating local password");
        SecureRandom random = new SecureRandom();
        byte[] pwd = new byte[PASSWORD_BYTES];
        random.nextBytes(pwd);
        password = toHex(pwd);
        File localPasswordFile = new File(env.getConfigDirPath(), LOCAL_PASSWORD_FILE);
        PrintWriter w = null;
        try {
            if (localPasswordFile.exists()) {
                if (!localPasswordFile.delete()) {
                    logger.log(Level.WARNING, KernelLoggerInfo.cantDeletePasswordFile, localPasswordFile.toString());
                    // if we can't make sure it's our file, don't write it
                    return;
                }
            }
            if (!localPasswordFile.createNewFile()) {
                logger.log(Level.WARNING, KernelLoggerInfo.cantCreatePasswordFile, localPasswordFile.toString());
                // if we can't make sure it's our file, don't write it
                return;
            }

            /*
             * XXX - There's a security hole here. Between the time the file is created and the permissions are changed to prevent
             * others from opening it, someone else could open it and wait for the data to be written. Java needs the ability to
             * create a file that's readable only by the owner; coming in JDK 7.
             *
             * The setReadable(false, false) call will fail on Windows. we ignore the failures on all platforms - this is a best
             * effort. The above calls ensured that the file is our file, so the following is the best we can do on all operating
             * systems.
             */
            localPasswordFile.setWritable(false, false); // take from all
            localPasswordFile.setWritable(true, true); // owner only
            localPasswordFile.setReadable(false, false); // take from all
            localPasswordFile.setReadable(true, true); // owner only

            w = new PrintWriter(localPasswordFile);
            w.println(password);
        } catch (IOException ex) {
            // ignore errors
            logger.log(Level.FINE, "Exception writing local password file", ex);
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    /**
     * Is the given password the local password?
     */
    @Override
    public boolean isLocalPassword(String p) {
        return password != null && password.equals(p);
    }

    /**
     * Get the local password.
     */
    @Override
    public String getLocalPassword() {
        return password;
    }

    /**
     * Convert the byte array to a hex string.
     */
    private static String toHex(byte[] b) {
        char[] bc = new char[b.length * 2];
        for (int i = 0, j = 0; i < b.length; i++) {
            byte bb = b[i];
            bc[j++] = hex[(bb >> 4) & 0xF];
            bc[j++] = hex[bb & 0xF];
        }
        return new String(bc);
    }
}
