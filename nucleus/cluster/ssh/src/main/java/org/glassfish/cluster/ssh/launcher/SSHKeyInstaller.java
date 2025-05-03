/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.cluster.ssh.launcher;

import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.glassfish.cluster.ssh.sftp.SFTPPath;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.cluster.ssh.launcher.JavaSystemJschLogger.maskForLogging;
import static org.glassfish.cluster.ssh.launcher.SSHLauncher.SSH_DIR_NAME;

/**
 * This class serves to generate a private key and then upload it to the remote host using password
 * authentication.
 */
public class SSHKeyInstaller {
    private static final Logger LOG = System.getLogger(SSHKeyInstaller.class.getName());

    private static final String AUTH_KEY_FILE = "authorized_keys";
    private static final int DEFAULT_TIMEOUT_MSEC = 120000; // 2 minutes
    private static final String SSH_KEYGEN = "ssh-keygen";

    private final SSHLauncher ssh;


    /**
     * Creates this instance.
     *
     * @param ssh settings used to establish the connection. Note that after the installation it
     *            should not be used any more as this class serves for enabling the key
     *            authentication.
     */
    public SSHKeyInstaller(SSHLauncher ssh) {
        this.ssh = ssh;
    }


    /**
     * Setting up the key involves the following steps:
     * -If a key exists and we can connect using the key, do nothing.
     * -Generate a key pair if there isn't one
     * -Connect to remote host using password auth and do the following:
     *  1. create .ssh directory if it doesn't exist
     *  2. copy over the key as key.tmp
     *  3. Append the key to authorized_keys file
     *  4. Remove the temporary key file key.tmp
     *  5. Fix permissions for home, .ssh and authorized_keys
     * @param node        - remote host
     * @param pubKeyFile  - .pub file
     * @param generateKey - flag to indicate if key needs to be generated or not
     * @param passwd      - ssh user password
     * @throws IOException
     */
    public void setupKey(String node, File pubKeyFile, boolean generateKey, String passwd) throws IOException {
        File keyFile = ssh.getKeyFile();
        String userName = ssh.getUserName();
        LOG.log(DEBUG, () -> "Key = " + keyFile);
        if (keyFile.exists()) {
            if (ssh.checkConnection()) {
                throw new IOException("SSH public key authentication is already configured for " + userName + "@" + node);
            }
        } else {
            if (generateKey) {
                if (!generateKeyPair(keyFile)) {
                    throw new IOException("SSH key pair generation failed. Please generate key manually.");
                }
            } else {
                throw new IOException("SSH key pair not present. Please generate a key pair manually or specify an existing one and re-run the command.");
            }
        }

        //password is must for key distribution
        if (passwd == null) {
            throw new IOException("SSH password is required for distributing the public key. You can specify the SSH password in a password file and pass it through --passwordfile option.");
        }
        try (SSHSession session = ssh.openSession(passwd); SFTPClient sftp = session.createSFTPClient()) {

            // fixes .ssh file mode
            setupSSHDir();

            final File pubKey;
            if (pubKeyFile == null) {
                pubKey = new File(keyFile.getParent(), keyFile.getName() + ".pub");
            } else {
                pubKey = pubKeyFile;
            }
            if (!pubKey.exists()) {
                throw new IOException("Public key file " + pubKey + " does not exist.");
            }

            final SFTPPath remoteSshDir;
            try {
                remoteSshDir = sftp.getHome().resolve(SSH_DIR_NAME);
            } catch (InvalidPathException e) {
                throw new SSHException("Could not resolve ssh home directory of the remote user.", e);
            }
            final RemoteSystemCapabilities capabilities = ssh.getCapabilities();
            if (!sftp.exists(remoteSshDir)) {
                LOG.log(DEBUG, () -> SSH_DIR_NAME + " does not exist");
                sftp.mkdirs(remoteSshDir);
                if (capabilities.isChmodSupported()) {
                    sftp.chmod(remoteSshDir, 0700);
                }
            }

            // copy over the public key to remote host
            final SFTPPath remoteKeyTmp = remoteSshDir.resolve("key.tmp");
            sftp.put(pubKey, remoteKeyTmp);
            if (capabilities.isChmodSupported()) {
                sftp.chmod(remoteKeyTmp, 0600);
            }

            // append the public key file contents to authorized_keys file on remote host
            final SFTPPath authKeyFile = remoteSshDir.resolve(AUTH_KEY_FILE);
            String mergeCommand = "cat \"" + remoteKeyTmp + "\" >> \"" + authKeyFile + "\"";
            LOG.log(DEBUG, () -> "mergeCommand = " + mergeCommand);
            if (session.exec(mergeCommand) != 0) {
                throw new IOException("Failed to propogate the public key " + pubKey + " to " + ssh.getHost());
            }
            LOG.log(INFO, "Copied keyfile " + pubKey + " to " + userName + "@" + ssh.getHost());

            try {
                sftp.rm(remoteKeyTmp);
                LOG.log(DEBUG, "Removed the temporary key file on remote host");
            } catch (SSHException e) {
                LOG.log(WARNING, "Failed to remove the public key file key.tmp on remote host " + ssh.getHost());
            }

            if (capabilities.isChmodSupported()) {
                LOG.log(INFO, "Fixing file permissions for home(755), .ssh(700) and authorized_keys file(600)");
                sftp.cd(sftp.getHome());
                sftp.chmod(remoteSshDir.getParent(), 0755);
                sftp.chmod(remoteSshDir, 0700);
                sftp.chmod(authKeyFile, 0600);
            }
        }
    }


    /**
      * Invoke ssh-keygen using ProcessManager API
      */
    private boolean generateKeyPair(File keyFile) throws IOException {
        File keygenCmd = findSSHKeygen();
        LOG.log(DEBUG, () -> "Using " + keygenCmd + " to generate key pair");

        if (!setupSSHDir()) {
            throw new IOException("Failed to set proper permissions on .ssh directory");
        }

        StringBuilder log = new StringBuilder();
        List<String> cmdLine = new ArrayList<>();
        cmdLine.add(keygenCmd.getAbsolutePath());
        log.append(keygenCmd);
        cmdLine.add("-t");
        log.append(" ").append("-t");
        cmdLine.add("rsa");
        log.append(" ").append("rsa");

        cmdLine.add("-N");
        log.append(" ").append("-N");
        if (ssh.getKeyFilePassphrase() == null) {
            log.append(" ").append("\"\"");
            // special handling for empty passphrase on Windows
            if(OS.isWindows()) {
                cmdLine.add("\"\"");
            } else {
                cmdLine.add("");
            }
        } else {
            cmdLine.add(ssh.getKeyFilePassphrase());
            log.append(" ").append(maskForLogging(ssh.getKeyFilePassphrase()));
        }
        cmdLine.add("-f");
        log.append(" ").append("-f");
        cmdLine.add(keyFile.getAbsolutePath());
        log.append(" ").append(keyFile);
        //cmdLine.add("-vvv");

        ProcessManager pm = new ProcessManager(cmdLine);

        LOG.log(DEBUG, () -> "Command = " + log);
        pm.setTimeoutMsec(DEFAULT_TIMEOUT_MSEC);

        if (LOG.isLoggable(DEBUG)) {
            pm.setEcho(true);
        } else {
            pm.setEcho(false);
        }

        int exit;
        try {
            exit = pm.execute();
        } catch (ProcessManagerException ex) {
            LOG.log(DEBUG, () -> "Error while executing ssh-keygen.", ex);
            exit = 1;
        }
        if (exit == 0) {
            LOG.log(INFO, () -> keygenCmd + " successfully generated the identification " + keyFile);
        } else {
            LOG.log(WARNING, () -> keygenCmd + " failed. It produced standard error output:\n" + pm.getStderr()
                + "\n and standard output:\n" + pm.getStdout());
        }
        return exit == 0;
    }


    /**
      * Create .ssh directory on this host and set the permissions correctly
      */
    private boolean setupSSHDir() throws IOException {
        boolean ret = true;
        File f = new File(FileUtils.USER_HOME, SSH_DIR_NAME);
        if (!FileUtils.safeIsDirectory(f)) {
            if (!f.mkdirs()) {
                throw new IOException("Failed to create " + f.getPath());
            }
            LOG.log(INFO, "Created directory {0}", f);
        }

        if (!f.setReadable(false, false) || !f.setReadable(true)) {
            ret = false;
        }

        if (!f.setWritable(false,false) || !f.setWritable(true)) {
            ret = false;
        }

        if (!f.setExecutable(false, false) || !f.setExecutable(true)) {
            ret = false;
        }

        LOG.log(DEBUG, "Fixed the .ssh directory permissions to 0700");
        return ret;
    }


    /**
     * Method to locate ssh-keygen. If found in path, return the same or else look
     * for it in a pre defined list of search paths.
     * @return ssh-keygen command
     */
    private File findSSHKeygen() {
        List<String> paths = new ArrayList<>(List.of("/usr/bin/", "/usr/local/bin/"));
        if (OS.isWindows()) {
            paths.add("C:/cygwin/bin/");
            //Windows MKS Toolkit install path
            String mks = System.getenv("ROOTDIR");
            if (mks != null) {
                paths.add(mks + "/bin/");
            }
        }

        LOG.log(DEBUG, () -> "Paths = " + paths);

        File exe = ProcessUtils.getExe(SSH_KEYGEN);
        if( exe != null){
            return exe;
        }

        for (String path : paths) {
            File f = new File(path, SSH_KEYGEN);
            if (f.canExecute()) {
                return f.getAbsoluteFile();
            }
        }
        return new File(SSH_KEYGEN);
    }

}
