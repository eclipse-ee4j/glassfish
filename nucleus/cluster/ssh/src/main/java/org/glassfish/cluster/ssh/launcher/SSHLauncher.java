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

package org.glassfish.cluster.ssh.launcher;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.SshAuth;
import com.sun.enterprise.config.serverbeans.SshConnector;
import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.Runtime.Version;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.glassfish.cluster.ssh.util.SSHUtil;
import org.glassfish.internal.api.RelativePathResolver;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

/**
 * @author Rajiv Mordani, Krishna Deepak
 */
public class SSHLauncher {

    private static final int SSH_PORT_DEFAULT = 22;
    private static final String SSH_DIR_NAME = ".ssh";
    private static final String AUTH_KEY_FILE = "authorized_keys";
    private static final int DEFAULT_TIMEOUT_MSEC = 120000; // 2 minutes
    private static final String SSH_KEYGEN = "ssh-keygen";

    private static final Logger LOG = System.getLogger(SSHLauncher.class.getName());

    static {
        JSch.setLogger(new JavaSystemJschLogger());
    }

    /** The host name which to connect to via ssh */
    private final String host;

    /** The port on which the ssh daemon is running */
    private final int port;

    /** The user name to use for authenticating with the ssh daemon */
    private final String userName;

    /** The name of private key file. */
    private final File keyFile;

    private final String keyPassPhraseParameter;
    private final String keyPassPhrase;

    /** Password before it has been expanded. */
    private final String passwordParameter;
    private final String password;
    private final RemoteSystemCapabilities capabilities;


    /**
     * Initialize the SSHLauncher use a {@link Node} config object
     *
     * @param node
     */
    public SSHLauncher(Node node) {
        final SshConnector connector = node.getSshConnector();
        this.host = getHost(node);
        LOG.log(DEBUG, "Connecting to host {0}", host);
        this.port = getPort(connector);

        final SshAuth sshAuth = connector.getSshAuth();
        this.userName = getUserName(sshAuth == null ? null : sshAuth.getUserName());
        this.keyFile = sshAuth == null || sshAuth.getKeyfile() == null
            ? SSHUtil.getExistingKeyFile()
            : new File(sshAuth.getKeyfile());
        this.passwordParameter = sshAuth == null ? null : sshAuth.getPassword();
        this.password = passwordParameter == null || passwordParameter.isEmpty()
            ? null
            : expandPasswordAlias(passwordParameter);
        this.keyPassPhraseParameter = sshAuth == null ? null : sshAuth.getKeyPassphrase();
        this.keyPassPhrase = keyPassPhraseParameter == null || keyPassPhraseParameter.isEmpty()
            ? null
            : expandPasswordAlias(keyPassPhraseParameter);
        this.capabilities = analyzeRemote();
        LOG.log(DEBUG, "SSH client configuration: {0}", this);
    }


    /**
     * Initialize the SSHLauncher
     *
     * @param userName
     * @param host
     * @param port
     * @param password
     * @param keyFile
     * @param keyPassPhrase
     */
    public SSHLauncher(String userName, String host, int port, String password, File keyFile, String keyPassPhrase) {
        this.host = host;
        this.port = port == 0 ? SSH_PORT_DEFAULT : port;
        this.keyFile = keyFile == null ? SSHUtil.getExistingKeyFile(): keyFile;
        this.userName = getUserName(userName);
        this.passwordParameter = password;
        this.password = password == null || password.isEmpty() ? null : expandPasswordAlias(password);
        this.keyPassPhraseParameter = keyPassPhrase;
        this.keyPassPhrase = keyPassPhrase == null || keyPassPhrase.isEmpty()
            ? null
            : expandPasswordAlias(keyPassPhrase);
        this.capabilities = analyzeRemote();
        LOG.log(DEBUG, "SSH client configuration: {0}", this);
    }


    /**
     * @return the remote host or IP address
     */
    public String getHost() {
        return this.host;
    }


    /**
     * @return the remote port supporting SSH
     */
    public int getPort() {
        return this.port;
    }


    /**
     * @return preloaded {@link RemoteSystemCapabilities}
     */
    public RemoteSystemCapabilities getCapabilities() {
        return this.capabilities;
    }


    /**
     * Open the {@link SSHSession}.
     *
     * @return open {@link SSHSession}
     * @throws JSchException
     */
    public SSHSession openSession() throws JSchException {
        JSch jsch = new JSch();

        // Client Auth
        String message = "";
        boolean triedAuthentication = false;
        // Private key file is provided - Public Key Authentication
        if (keyFile != null) {
            LOG.log(DEBUG, () -> "Specified key file is " + keyFile);
            if (keyFile.exists()) {
                triedAuthentication = true;
                LOG.log(DEBUG, () -> "Adding identity for private key at " + keyFile);
                jsch.addIdentity(keyFile.getAbsolutePath(), keyPassPhrase);
            } else {
                message = "Specified key file does not exist \n";
            }
        } else if (password == null || password.isEmpty()) {
            message += "No key or password specified - trying default keys \n";
            LOG.log(DEBUG, "keyfile and password are null. Will try to authenticate with default key file if available");
            // check the default key locations if no authentication
            // method is explicitly configured.
            Path home = FileUtils.USER_HOME.toPath();
            for (String keyName : List.of("id_rsa", "id_dsa", "id_ecdsa", "identity")) {
                message += "Tried to authenticate using " + keyName + "\n";
                File key = home.resolve(Path.of(SSH_DIR_NAME, keyName)).toFile();
                if (key.exists()) {
                    triedAuthentication = true;
                    jsch.addIdentity(key.getAbsolutePath(), keyPassPhrase);
                }
            }
        }

        final Session session = jsch.getSession(userName, host, port);
        try {
            // TODO: Insecure, maybe we could create an input field and allow user to check the host key?
            session.setConfig("StrictHostKeyChecking", "accept-new");
            session.setUserInfo(new GlassFishSshUserInfo());
            // Password Auth
            if (password != null && !password.isEmpty()) {
                LOG.log(DEBUG, () -> "Authenticating with password " + getPrintablePassword(password));
                triedAuthentication = true;
                session.setPassword(password);
            }
            if (!triedAuthentication) {
                throw new JSchException("Could not authenticate. " + message);
            }
            session.connect();
            return new SSHSession(session, getCapabilities());
        } catch (Exception e) {
            session.disconnect();
            throw e;
        }
    }


    /**
     * Open and close the session.
     *
     * @throws JSchException
     */
    public void pingConnection() throws JSchException {
        LOG.log(DEBUG, () -> "Trying to establish connection to host: " + this.host);
        try (SSHSession session = openSession()) {
            LOG.log(INFO, () -> "Establishing SSH connection to host " + this.host + " succeeded!");
        }
    }


    /**
     * Check if the remote path exists.
     * This method is a shortcut to simplify your code as it uses {@link SSHSession}
     * and {@link SFTPClient}.
     *
     * @param path absolute path
     * @return true if the path exists in the SFTP server.
     * @throws SSHException
     */
    public boolean exists(Path path) throws SSHException {
        try (SSHSession session = openSession(); SFTPClient sftpClient = session.createSFTPClient()) {
            return sftpClient.exists(path);
        } catch (JSchException | SftpException e) {
            String msg = "Failed to check if the file exists: " + path + " on host " + host;
            LOG.log(WARNING, msg, e);
            throw new SSHException(msg, e);
        }
    }


    /**
     * Connects to the remote SSH server and does some simple analysis to be able to work both
     * with Linux or Windows based operating systems.
     *
     * @return {@link RemoteSystemCapabilities}
     */
    private RemoteSystemCapabilities analyzeRemote() {
        try (SSHSession session = openSession(); SFTPClient sftpClient = session.createSFTPClient()) {
            Map<String, String> env = session.detectShellEnv();
            LOG.log(DEBUG, "Environment of the operating system obtained for the SSH client: {0}", env);

            StringBuilder outputBuilder = new StringBuilder();
            // java must be available in the environment.
            // If you use docker java images, check UsePam=yes and /etc/environment
            // By default images configure ENV properties just for the container app, not for ssh clients.
            final int code = session.exec(Arrays.asList("java", "-XshowSettings:properties", "-version"), null,
                outputBuilder);
            if (code != 0) {
                throw new IllegalStateException(
                    "Could not execute the java command on the host " + host + ". Output: " + outputBuilder);
            }

            String[] output = outputBuilder.toString().split("\\R");
            String javaHome = getValue("java.home", output);
            Version javaVersion = getProperty("java.version", output, Version::parse);
            OperatingSystem os = getProperty("os.name", output, OperatingSystem::parse);
            return new RemoteSystemCapabilities(javaHome, javaVersion, os);
        } catch (JSchException | SSHException e) {
            String msg = "Failed to analyze the remote system.";
            LOG.log(WARNING, msg, e);
            throw new IllegalStateException(msg, e);
        }
    }


    private boolean isPasswordAlias(String alias) {
        // Check if the passed string is specified using the alias syntax
        String aliasName = RelativePathResolver.getAlias(alias);
        return aliasName != null;
    }

    /**
     * Return a version of the password that is printable.
     * @param p  password string
     * @return   printable version of password
     */
    private String getPrintablePassword(String p) {
        // We only display the password if it is an alias, else
        // we display "<concealed>".
        String printable = "null";
        if (p != null) {
            if (isPasswordAlias(p)) {
                printable = p;
            } else {
                printable = "<concealed>";
            }
        }
        return printable;
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
     * @throws InterruptedException
     */
    public void setupKey(String node, String pubKeyFile, boolean generateKey, String passwd)
        throws IOException, InterruptedException {

        File key = keyFile;
        LOG.log(DEBUG, () -> "Key = " + keyFile);
        if (key.exists()) {
            if (checkConnection()) {
                throw new IOException("SSH public key authentication is already configured for " + userName + "@" + node);
            }
        } else {
            if (generateKey) {
                if(!generateKeyPair()) {
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
        try (SSHSession session = openSession(passwd)) {
            if (!session.isOpen()) {
                throw new IOException("SSH password authentication failed for user " + userName + " on host " + node);
            }
            try (SFTPClient sftp = session.createSFTPClient()) {
                if (key.exists()) {

                    // fixes .ssh file mode
                    setupSSHDir();

                    if (pubKeyFile == null) {
                        pubKeyFile = keyFile + ".pub";
                    }

                    File pubKey = new File(pubKeyFile);
                    if (!pubKey.exists()) {
                        throw new IOException("Public key file " + pubKeyFile + " does not exist.");
                    }

                    final Path remoteSshDir;
                    try {
                        remoteSshDir = sftp.getHome().resolve(SSH_DIR_NAME);
                    } catch (SftpException e) {
                        throw new IOException("Could not resolve home directory of the remote user: " + e.getMessage(),
                            e);
                    }
                    try {
                        if (!sftp.exists(remoteSshDir)) {
                            LOG.log(DEBUG, () -> SSH_DIR_NAME + " does not exist");
                            sftp.mkdirs(remoteSshDir);
                            if (getCapabilities().isChmodSupported()) {
                                sftp.chmod(remoteSshDir, 0700);
                            }
                        }
                    } catch (SftpException e) {
                        throw new IOException("Error while creating .ssh directory on remote host: " + e.getMessage(), e);
                    }

                    // copy over the public key to remote host
                    final Path remoteKeyTmp = remoteSshDir.resolve("key.tmp");
                    try {
                        sftp.put(pubKey, remoteKeyTmp);
                        if (getCapabilities().isChmodSupported()) {
                            sftp.chmod(remoteKeyTmp, 0600);
                        }
                    } catch (SftpException ex) {
                        throw new IOException("Unable to copy the public key", ex);
                    }

                    // append the public key file contents to authorized_keys file on remote host
                    final Path authKeyFile = remoteSshDir.resolve(AUTH_KEY_FILE);
                    String mergeCommand = "cat " + remoteKeyTmp + " >> " + authKeyFile;
                    LOG.log(DEBUG, () -> "mergeCommand = " + mergeCommand);
                    if (session.exec(mergeCommand) != 0) {
                        throw new IOException("Failed to propogate the public key " + pubKeyFile + " to " + host);
                    }
                    LOG.log(INFO, "Copied keyfile " + pubKeyFile + " to " + userName + "@" + host);

                    try {
                        sftp.rm(remoteKeyTmp);
                        LOG.log(DEBUG, "Removed the temporary key file on remote host");
                    } catch (SftpException e) {
                        LOG.log(WARNING, "WARNING: Failed to remove the public key file key.tmp on remote host " + host);
                    }

                    if (capabilities.isChmodSupported()) {
                        LOG.log(INFO, "Fixing file permissions for home(755), .ssh(700) and authorized_keys file(600)");
                        try {
                            sftp.cd(sftp.getHome());
                            sftp.chmod(remoteSshDir.getParent(), 0755);
                            sftp.chmod(remoteSshDir, 0700);
                            sftp.chmod(authKeyFile, 0600);
                        } catch (SftpException ex) {
                            throw new IOException("Unable to fix file permissions", ex);
                        }
                    }
                }
            }
        } catch (JSchException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Check if we can authenticate using public key auth
     * @return true|false
     */
    public boolean checkConnection() {
        LOG.log(DEBUG, "Checking connection...");
        JSch jsch = new JSch();
        Session sess = null;
        try {
            jsch.addIdentity(keyFile.getAbsolutePath(), keyPassPhrase);
            sess = jsch.getSession(userName, host, port);
            sess.setConfig("StrictHostKeyChecking", "no");
            sess.connect();
            if (sess.isConnected()) {
                LOG.log(INFO, () -> "Successfully connected to " + userName + "@" + host + " using keyfile " + keyFile);
                return true;
            }
            return false;
        } catch (JSchException ex) {
            Throwable t = ex.getCause();
            if (t != null) {
                String msg = t.getMessage();
                LOG.log(WARNING, "Failed to connect or authenticate: " + msg);
            }
            LOG.log(DEBUG, "Failed to connect or autheticate: ", ex);
            return false;
        } finally {
            if (sess != null) {
                sess.disconnect();
            }
        }
    }

    /**
     * Check if we can connect using password auth
     * @return true|false
     */
    public boolean checkPasswordAuth() {
        LOG.log(DEBUG, "Checking connection...");
        JSch jsch = new JSch();
        Session sess = null;
        try {
            sess = jsch.getSession(userName, host, port);
            sess.setConfig("StrictHostKeyChecking", "no");
            sess.setPassword(password);
            sess.connect();
            if (sess.isConnected()) {
                LOG.log(DEBUG,
                    () -> "Successfully connected to " + userName + "@" + host + " using password authentication");
                return true;
            }
            return false;
        } catch (JSchException ex) {
            LOG.log(ERROR, "Failed to connect or autheticate: ", ex);
            return false;
        } finally {
            if (sess != null) {
                sess.disconnect();
            }
        }
    }

    @Override
    public String toString() {
        String displayPassword = getPrintablePassword(passwordParameter);
        String displayKeyPassPhrase = getPrintablePassword(keyPassPhraseParameter);
        return String.format("host=%s port=%d user=%s password=%s keyFile=%s keyPassPhrase=%s, capabilities=%s", host, port, userName,
            displayPassword, keyFile, displayKeyPassPhrase, capabilities);
    }

    /**
      * Invoke ssh-keygen using ProcessManager API
      */
    private boolean generateKeyPair() throws IOException {
        String keygenCmd = findSSHKeygen();
        LOG.log(DEBUG, () -> "Using " + keygenCmd + " to generate key pair");

        if (!setupSSHDir()) {
            throw new IOException("Failed to set proper permissions on .ssh directory");
        }

        StringBuilder k = new StringBuilder();
        List<String> cmdLine = new ArrayList<>();
        cmdLine.add(keygenCmd);
        k.append(keygenCmd);
        cmdLine.add("-t");
        k.append(" ").append("-t");
        cmdLine.add("rsa");
        k.append(" ").append("rsa");

        cmdLine.add("-N");
        k.append(" ").append("-N");
        if (keyPassPhrase == null) {
            k.append(" ").append("\"\"");
            // special handling for empty passphrase on Windows
            if(OS.isWindows()) {
                cmdLine.add("\"\"");
            } else {
                cmdLine.add("");
            }
        } else {
            cmdLine.add(keyPassPhrase);
            k.append(" ").append(getPrintablePassword(keyPassPhrase));
        }
        cmdLine.add("-f");
        k.append(" ").append("-f");
        cmdLine.add(keyFile.getAbsolutePath());
        k.append(" ").append(keyFile);
        //cmdLine.add("-vvv");

        ProcessManager pm = new ProcessManager(cmdLine);

        LOG.log(DEBUG, () -> "Command = " + k);
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
     * Method to locate ssh-keygen. If found in path, return the same or else look
     * for it in a pre defined list of search paths.
     * @return ssh-keygen command
     */
    private String findSSHKeygen() {
        List<String> paths = new ArrayList<>(Arrays.asList(
                    "/usr/bin/",
                    "/usr/local/bin/"));

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
            return exe.getPath();
        }

        for (String s : paths) {
            File f = new File(s + SSH_KEYGEN);
            if (f.canExecute()) {
                return f.getAbsolutePath();
            }
        }
        return SSH_KEYGEN;
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


    private SSHSession openSession(String passwordParam) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(userName, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(passwordParam);
        session.connect();
        return new SSHSession(session);
    }


    /**
     * @param alias The alias in the format of <code>${ALIAS=aliasname}</code>
     * @return expanded password
     */
    public static String expandPasswordAlias(String alias) {
        String expandedPassword = null;
        if (alias == null) {
            return null;
        }
        try {
            expandedPassword = RelativePathResolver.getRealPasswordFromAlias(alias);
        } catch (Exception e) {
            LOG.log(WARNING, "Expansion failed for {0}: {1}", new Object[] {alias, e.getMessage()});
            return null;
        }
        return expandedPassword;
    }

    private static String getHost(Node node) {
        SshConnector sshConnector = node.getSshConnector();
        String sshHost = sshConnector.getSshHost();
        return sshHost == null || sshHost.isEmpty() ? node.getNodeHost() : sshConnector.getSshHost();
    }


    private static int getPort(final SshConnector connector) {
        try {
            int sshPort = Integer.parseInt(connector.getSshPort());
            return sshPort > 0 ? sshPort : SSH_PORT_DEFAULT;
        } catch(NumberFormatException nfe) {
            return SSH_PORT_DEFAULT;
        }
    }


    private static String getUserName(final String userName) {
        return userName == null || userName.isEmpty() ? System.getProperty("user.name") : userName;
    }


    private static <T> T getProperty(String key, String[] lines, Function<String, T> converter) {
        String value = getValue(key, lines);
        if (value == null) {
            return null;
        }
        return converter.apply(value);
    }


    private static String getValue(String keyName, String[] propertiesOutputLines) {
        for (String line : propertiesOutputLines) {
            int equalSignPosition = line.indexOf('=');
            if (equalSignPosition <= 0) {
                continue;
            }
            String key = line.substring(0, equalSignPosition).strip();
            if (keyName.equals(key)) {
                return equalSignPosition == line.length() - 1 ? "" : line.substring(equalSignPosition + 1).strip();
            }
        }
        return null;
    }
}
