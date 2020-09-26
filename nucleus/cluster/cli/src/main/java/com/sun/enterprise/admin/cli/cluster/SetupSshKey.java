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

package com.sun.enterprise.admin.cli.cluster;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;

import jakarta.inject.Inject;


import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.util.SSHUtil;

/**
 *  This is a local command that distributes the SSH public key to remote node(s)
 *
 */
@Service(name = "setup-ssh")
@PerLookup
@ExecuteOn({RuntimeType.DAS})
public final class SetupSshKey extends NativeRemoteCommandsBase {
    @Param(name = "sshuser", optional = true, defaultValue = "${user.name}")
    private String user;
    @Param(optional = true, defaultValue = "22", name = "sshport")
    int port;
    @Param(optional = true)
    String sshkeyfile;
    @Param(optional = true)
    private String sshpublickeyfile;
    @Param(optional = true, defaultValue = "false")
    private boolean generatekey;
    @Inject
    private ServiceLocator habitat;

    public SetupSshKey() {
    }

    /**
     */
    @Override
    protected void validate()
            throws CommandException {
        super.validate();
        Globals.setDefaultHabitat(habitat);

        if (sshkeyfile == null) {
            //if user hasn't specified a key file and there is no key file at default
            //location, then generate one
            String existingKey = SSHUtil.getExistingKeyFile();
            if (existingKey == null) {
                sshkeyfile = SSHUtil.getDefaultKeyFile();
                if (promptForKeyGeneration()) {
                    generatekey = true;
                }
            }
            else {
                //there is a key that requires to be distributed, hence need password
                promptPass = true;
                sshkeyfile = existingKey;

                if (SSHUtil.isEncryptedKey(sshkeyfile)) {
                    sshkeypassphrase = getSSHPassphrase(false);
                }
            }
        }
        else {
            promptPass = SSHUtil.validateKeyFile(sshkeyfile);
            if (SSHUtil.isEncryptedKey(sshkeyfile)) {
                sshkeypassphrase = getSSHPassphrase(false);
            }
        }

        if (sshpublickeyfile != null) {
            SSHUtil.validateKeyFile(sshpublickeyfile);
        }

    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException {

        SSHLauncher sshL = habitat.getService(SSHLauncher.class);

        String previousPassword = null;
        boolean status = false;
        for (String node : hosts) {
            sshL.init(getRemoteUser(), node, getRemotePort(), sshpassword, sshkeyfile, sshkeypassphrase, logger);
            if (generatekey || promptPass) {
                //prompt for password iff required
                if (sshkeyfile != null || SSHUtil.getExistingKeyFile() != null) {
                    if (sshL.checkConnection()) {
                        logger.info(Strings.get("SSHAlreadySetup", getRemoteUser(), node));
                        continue;
                    }
                }
                if (previousPassword != null) {
                    status = sshL.checkPasswordAuth();
                }
                if (!status) {
                    sshpassword = getSSHPassword(node);
                    previousPassword = sshpassword;
                }
            }

            try {
                sshL.setupKey(node, sshpublickeyfile, generatekey, sshpassword);
            }
            catch (IOException ce) {
                //logger.fine("SSH key setup failed: " + ce.getMessage());
                throw new CommandException(Strings.get("KeySetupFailed", ce.getMessage()));
            }
            catch (Exception e) {
                //handle KeyStoreException
                if (logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "Keystore error: ", e);
                }
            }

            if (!sshL.checkConnection()) {
                throw new CommandException(Strings.get("ConnFailed"));
            }
        }
        return SUCCESS;
    }

    /**
     * Prompt for key generation
     */
    private boolean promptForKeyGeneration() {
        if (generatekey)
            return true;

        if (!programOpts.isInteractive())
            return false;

        Console cons = System.console();

        if (cons != null) {
            String val = null;
            do {
                cons.printf("%s", Strings.get("GenerateKeyPairPrompt", getRemoteUser(), Arrays.toString(hosts)));
                val = cons.readLine();
                if (val != null && (val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("y"))) {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Generate key!");
                    }
                    return true;
                }
                else if (val != null && (val.equalsIgnoreCase("no") || val.equalsIgnoreCase("n"))) {
                    break;
                }
            }
            while (val != null && !isValidAnswer(val));
        }
        return false;
    }

    @Override
    final String getRawRemoteUser() {
        return user;
    }

    @Override
    int getRawRemotePort() {
        return port;
    }

    @Override
    String getSshKeyFile() {
        return sshkeyfile;
    }
}
