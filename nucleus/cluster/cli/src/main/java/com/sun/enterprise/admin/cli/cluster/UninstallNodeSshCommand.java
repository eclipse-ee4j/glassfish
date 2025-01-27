/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.IOException;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.launcher.SSHSession;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.glassfish.cluster.ssh.sftp.SFTPPath;
import org.glassfish.cluster.ssh.util.SSHUtil;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Byron Nevins
 */
@Service(name = "uninstall-node-ssh")
@PerLookup
public class UninstallNodeSshCommand extends UninstallNodeBaseCommand {
    @Param(name = "sshuser", optional = true, defaultValue = "${user.name}")
    private String user;
    @Param(optional = true, defaultValue = "22", name = "sshport")
    private int port;
    @Param(optional = true)
    private String sshkeyfile;

    @Override
    String getRawRemoteUser() {
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

    @Override
    protected void validate() throws CommandException {
        super.validate();
        if (sshkeyfile == null) {
            // if user hasn't specified a key file check if key exists in
            // default location
            File existingKey = SSHUtil.getExistingKeyFile();
            if (existingKey == null) {
                promptPass = true;
            } else {
                sshkeyfile = existingKey.getAbsolutePath();
            }
        } else {
            validateKey(sshkeyfile);
        }

        // we need the key passphrase if key is encrypted
        if (sshkeyfile != null && SSHUtil.isEncryptedKey(new File(sshkeyfile))) {
            sshkeypassphrase = getSSHPassphrase(true);
        }
    }

    @Override
    void deleteFromHosts() throws CommandException {
        try {
            SFTPPath installDir = SFTPPath.of(getInstallDir());
            for (String host : hosts) {
                File keyFile = sshkeyfile == null ? null : new File(sshkeyfile);
                SSHLauncher sshLauncher = new SSHLauncher(getRemoteUser(), host, getRemotePort(), sshpassword, keyFile, sshkeypassphrase);

                if (keyFile != null && !sshLauncher.checkConnection()) {
                    //key auth failed, so use password auth
                    promptPass = true;
                }

                if (promptPass) {
                    sshpassword = getSSHPassword(host);
                    sshLauncher = new SSHLauncher(getRemoteUser(), host, getRemotePort(), sshpassword, keyFile, sshkeypassphrase);
                }

                try (SSHSession session = sshLauncher.openSession();
                    SFTPClient sftpClient = session.createSFTPClient()) {
                    if (!sftpClient.exists(installDir)) {
                        throw new IOException("Directory does not exist: " + getInstallDir());
                    }
                    sftpClient.rmDir(installDir, false);
                }
            }
        } catch (CommandException ce) {
            throw ce;
        } catch (Exception ex) {
            throw new CommandException(ex);
        }
    }
}
