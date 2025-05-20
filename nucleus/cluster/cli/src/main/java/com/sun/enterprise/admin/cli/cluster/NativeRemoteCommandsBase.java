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

package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.admin.cli.CLICommand;
import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.universal.glassfish.TokenResolver;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.io.DomainDirs;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.internal.api.RelativePathResolver;

import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;

/**
 *  Base class for SSH provisioning commands.
 *
 *  Byron Nevins Aug 2011.  SSH was hard-coded in.  Now we
 *  want to use jcifs (SAMBA) for Windows.
 */
abstract class NativeRemoteCommandsBase extends CLICommand {
    @Param(optional = false, primary = true, multiple = true)
    String[] hosts;
    String sshpassword;
    String sshkeypassphrase = null;
    boolean promptPass = false;
    TokenResolver resolver = null;
    private String remoteUser;
    private int remotePort;

    NativeRemoteCommandsBase() {
        // Create a resolver that can replace system properties in strings
        resolver = new TokenResolver();
    }

    // all of this rigamarole is to get the right names for parameters in front
    // of user eyeballs
    abstract String getRawRemoteUser();
    abstract int getRawRemotePort();
    abstract String getSshKeyFile();

    @Override
    protected void validate() throws CommandException {
        remoteUser = resolver.resolve(getRawRemoteUser());
    }

    final String getRemoteUser() {
        return remoteUser;
    }
    final int getRemotePort() {
        return remotePort;
    }
    final void setRemotePort(int newPort) {
        remotePort = newPort;
    }

    /**
     * Get SSH password from password file or user.
     */
    String getSSHPassword(String node) throws CommandException {
        return getRemotePassword(node, "AS_ADMIN_SSHPASSWORD");
    }

    /**
     * Get SSH password from password file or user.
     */
    private String getRemotePassword(String node, String key) throws CommandException {
        String password = getFromPasswordFile(key);

        if (password != null) {
            String alias = RelativePathResolver.getAlias(password);
            if (alias != null) {
                password = expandPasswordAlias(node, alias, true);
            }
        }

        //get password from user if not found in password file
        if (password == null) {
            if (programOpts.isInteractive()) {
                char[] pArr = readPassword(Strings.get("SSHPasswordPrompt", getRemoteUser(), node));
                password = pArr != null ? new String(pArr) : null;
            }
            else {
                throw new CommandException(Strings.get("SSHPasswordNotFound"));
            }
        }
        return password;
    }

    /**
     * Get SSH key passphrase from password file or user.
     */
    String getSSHPassphrase(boolean verifyConn) {
        String passphrase = getFromPasswordFile("AS_ADMIN_SSHKEYPASSPHRASE");

        if (passphrase != null) {
            String alias = RelativePathResolver.getAlias(passphrase);

            if (alias != null) {
                passphrase = expandPasswordAlias(null, alias, verifyConn);
            }
        }

        //get password from user if not found in password file
        if (passphrase == null) {
            if (programOpts.isInteractive()) {
                //i18n
                char[] pArr = readPassword(Strings.get("SSHPassphrasePrompt", getSshKeyFile()));
                passphrase = pArr != null ? new String(pArr) : null;
            }
            else {
                passphrase = ""; //empty passphrase
            }
        }
        return passphrase;
    }

    /**
     * Get domain master password from password file or user.
     */
    String getMasterPassword(String domain) {
        String masterPass = getFromPasswordFile("AS_ADMIN_MASTERPASSWORD");

        //get password from user if not found in password file
        if (masterPass == null) {
            if (programOpts.isInteractive()) {
                //i18n
                char[] mpArr = readPassword(Strings.get("DomainMasterPasswordPrompt", domain));
                masterPass = mpArr != null ? new String(mpArr) : null;
            }
            else {
                masterPass = "changeit"; //default
            }
        }
        return masterPass;
    }

    private String getFromPasswordFile(String name) {
        return passwords.get(name);
    }

    boolean isValidAnswer(String val) {
        return val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("no")
                || val.equalsIgnoreCase("y") || val.equalsIgnoreCase("n");
    }


    /**
     * Obtains the real password from the domain specific keystore given an alias
     * @param host host that we are connecting to
     * @param alias password alias of form ${ALIAS=xxx}
     * @return real password of ssh user, null if not found
     */
    String expandPasswordAlias(String host, String alias, boolean verifyConn) {
        String expandedPassword = null;
        boolean connStatus = false;

        try {
            File domainsDirFile = DomainDirs.getDefaultDomainsDir();

            // get the list of domains
            File[] files = domainsDirFile.listFiles(File::isDirectory);
            for (File f : files) {
                //the following property is required for initializing the password helper
                System.setProperty(INSTANCE_ROOT.getSystemPropertyName(), f.getAbsolutePath());
                try {
                    final PasswordAdapter pa = new PasswordAdapter(null);
                    final boolean exists = pa.aliasExists(alias);
                    if (exists) {
                        String mPass = getMasterPassword(f.getName());
                        expandedPassword = new PasswordAdapter(mPass.toCharArray()).getPasswordForAlias(alias);
                    }
                }
                catch (Exception e) {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer(StringUtils.cat(": ", alias, e.getMessage()));
                    }
                    logger.warning(Strings.get("GetPasswordFailure", f.getName()));
                    continue;
                }

                if (expandedPassword != null) {
                    SSHLauncher sshL;
                    if (host != null) {
                        sshpassword = expandedPassword;
                        sshL = new SSHLauncher(getRemoteUser(), host, getRemotePort(), sshpassword, null, null);
                        connStatus = sshL.checkPasswordAuth();
                        if (!connStatus) {
                            logger.warning(Strings.get("PasswordAuthFailure", f.getName()));
                        }
                    } else {
                        sshkeypassphrase = expandedPassword;
                        if (verifyConn) {
                            File keyFile = getSshKeyFile() == null ? null : new File(getSshKeyFile());
                            sshL = new SSHLauncher(getRemoteUser(), hosts[0], getRemotePort(), sshpassword, keyFile, sshkeypassphrase);
                            connStatus = sshL.checkConnection();
                            if (!connStatus) {
                                logger.warning(Strings.get("PasswordAuthFailure", f.getName()));
                            }
                        }
                    }

                    if (connStatus) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, e.getMessage(), e);
            }
        }
        return expandedPassword;
    }

    /**
     * This method first obtains a list of files under the product installation
     * directory. It then modifies each path by prepending it with remote install dir path.
     * For ex. glassfish/lib/appserv-rt.jar becomes
     * <remote-install-path>/glassfish/lib/appserv-rt.jar
     * @return List of files and directories
     * @throws IOException
     */
    List<Path> getListOfInstallFiles(Path installDir) throws IOException {
        String ins = resolver.resolve("${com.sun.aas.productRoot}");
        Set<File> files = FileUtils.getAllFilesAndDirectoriesUnder(new File(ins));
        logger.finer(() -> "Total number of files under " + ins + " = " + files.size());
        List<Path> modList = new ArrayList<>();
        for (File f : files) {
            modList.add(installDir.resolve(f.toPath()));
        }
        return modList;
    }

    /**
     * Check for existence of key file.
     * @param file
     * @throws CommandException
     */
    void validateKey(String file) throws CommandException {
        File f = new File(file);
        if (!f.exists()) {
            throw new CommandException(Strings.get("KeyDoesNotExist", file));
        }
    }
}
