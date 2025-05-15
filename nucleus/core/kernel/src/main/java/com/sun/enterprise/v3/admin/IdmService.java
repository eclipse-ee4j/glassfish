/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.security.store.IdentityManagement;

import jakarta.inject.Inject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.glassfish.api.admin.PasswordAliasStore;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.kernel.KernelLoggerInfo;
import org.glassfish.security.services.impl.JCEKSPasswordAliasStore;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.ARG_SEP;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.ORIGINAL_ARGS;

/**
 * An implementation of the @link {IdentityManagement} that manages the password needs of the server. This
 * implementation consults the Java KeyStore and assumes that the stores are available in server's configuration area.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
@Service(name = "jks-based")
public class IdmService implements PostConstruct, IdentityManagement {

    private static final String FIXED_KEY = "master-password"; // the fixed key for master-password file
    private static final String PASSWORDFILE_OPTION_TO_ASMAIN = "-passwordfile"; // note single hyphen, in line with other args to ASMain!
    private static final String STDIN_OPTION_TO_ASMAIN = "-read-stdin"; // note single hyphen, in line with other args to ASMain!

    private static final String MP_PROPERTY = "AS_ADMIN_MASTERPASSWORD";

    private final Logger logger = Logger.getAnonymousLogger();

    @Inject
    private volatile StartupContext sc;

    @Inject
    private volatile ServerEnvironmentImpl env;

    private char[] masterPassword;

    @Override
    public void postConstruct() {
        boolean success;
        boolean readStdin = sc.getArguments().containsKey(STDIN_OPTION_TO_ASMAIN);
        if (readStdin) {
            success = setFromStdin();
        } else {
            success = setFromMasterPasswordFile();
            if (!success) {
                success = setFromAsMainArguments();
            }
        }
        if (!success) {
            masterPassword = "changeit".toCharArray(); // the default;
        }
    }

    @Override
    public char[] getMasterPassword() {
        return Arrays.copyOf(masterPassword, masterPassword.length);
    }

    ///// All Private

    private boolean setFromMasterPasswordFile() {
        try {
            File mp = env.getMasterPasswordFile();
            if (!mp.isFile()) {
                logger.fine("The JCEKS file: " + mp.getAbsolutePath()
                        + " does not exist, master password was not saved on disk during domain creation");
                return false;
            }
            final PasswordAliasStore masterPasswordAliasStore = JCEKSPasswordAliasStore.newInstance(mp.getAbsolutePath(),
                    FIXED_KEY.toCharArray());
            char[] mpChars = masterPasswordAliasStore.get(FIXED_KEY);
            if (mpChars == null) {
                return false;
            }
            masterPassword = mpChars;
            return true;
        } catch (Exception ex) {
            logger.fine("Error in master-password processing: " + ex.getMessage());
            return false;
        }

    }

    private boolean setFromAsMainArguments() {
        File pwf = null;
        try {
            String[] args = getOriginalArguments(sc);
            int index = 0;
            for (String arg : args) {
                if (PASSWORDFILE_OPTION_TO_ASMAIN.equals(arg)) {
                    if (index == (args.length - 1)) { // -passwordfile is the last argument
                        logger.warning(KernelLoggerInfo.optionButNoArg);
                        return false;
                    }
                    pwf = new File(args[index + 1]);
                    return readPasswordFile(pwf);
                }
                index++;
            }
            // no -passwordfile found
            return false;
        } catch (Exception ex) {
            String s = "Something wrong with given password file: ";
            String msg = pwf == null ? s : s + pwf.getAbsolutePath();
            logger.fine(msg);
            return false;
        }
    }

    private boolean readPasswordFile(File pwf) {
        Properties p = new Properties();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(pwf));
            p.load(br);
            if (p.getProperty(MP_PROPERTY) == null) {
                return false;
            }
            masterPassword = p.getProperty(MP_PROPERTY).toCharArray(); // this would stay in memory, so this needs some security audit,
                                                                       // frankly
            return true;
        } catch (IOException e) {
            logger.fine("Passwordfile: " + pwf.getAbsolutePath() + " (a simple property file) could not be processed, ignoring ...");
            return false;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                // ignore, I know
            }
        }
    }

    private boolean setFromStdin() {
        logger.fine("Reading the master password from stdin> ");
        // We will close the standard input as we don't use it any more.
        // On windows it would block deletion of the temporary file otherwise.
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String s;
            while ((s = br.readLine()) != null) {
                int ind = s.indexOf(MP_PROPERTY);
                if (ind == -1) {
                    return false; // this means stdin isn't behaving. That's bad and shouldn't happen.
                }
                masterPassword = s.substring(MP_PROPERTY.length() + 1).toCharArray(); // begIndex is that of "AS_ADMIN_MASTERPASSWORD=;
                                                                                      // consider trailing '='
            }
            // We don't want reveal the master password in the logs.
            // logger.fine("******************* Password from stdin: " + new String(masterPassword));
            if (masterPassword == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.fine("Stdin isn't behaving, ignoring it ..." + e.getMessage());
            return false;
        } finally {
            try {
                System.in.close();
            } catch (IOException e) {
                logger.fine("Error closing stdin: " + e.getMessage());
            } finally {
                System.setIn(null);
            }
        }
    }

    /**
     * @param context
     * @return parsed array of arguments saved as {@link #ORIGINAL_ARGS}
     */
    private static String[] getOriginalArguments(StartupContext context) {
        Properties args = context.getArguments();
        String s = args.getProperty(ORIGINAL_ARGS);
        if (s == null) {
            return new String[0];
        }
        StringTokenizer st = new StringTokenizer(s, ARG_SEP, false);
        List<String> result = new ArrayList<>();
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result.toArray(new String[result.size()]);
    }
}
