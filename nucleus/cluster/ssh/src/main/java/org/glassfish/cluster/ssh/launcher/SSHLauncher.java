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
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.SshAuth;
import com.sun.enterprise.config.serverbeans.SshConnector;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.lang.Runtime.Version;
import java.lang.System.Logger;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.glassfish.cluster.ssh.sftp.SFTPPath;
import org.glassfish.cluster.ssh.util.SSHUtil;
import org.glassfish.internal.api.RelativePathResolver;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.glassfish.cluster.ssh.launcher.JavaSystemJschLogger.maskForLogging;
import static org.glassfish.cluster.ssh.launcher.OperatingSystem.GENERIC;

/**
 * @author Rajiv Mordani, Krishna Deepak
 */
public class SSHLauncher {

    static final String SSH_DIR_NAME = ".ssh";
    private static final int SSH_PORT_DEFAULT = 22;

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
        this.capabilities = analyzeRemote(this.host, this.port, this.userName, this.password, this.keyFile,
            this.keyPassPhrase);
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
        this.capabilities = analyzeRemote(this.host, this.port, this.userName, this.password, this.keyFile,
            this.keyPassPhrase);
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
     * @return ssh login
     */
    public String getUserName() {
        return this.userName;
    }


    File getKeyFile() {
        return this.keyFile;
    }


    String getKeyFilePassphrase() {
        return this.keyPassPhrase;
    }


    /**
     * @return preloaded {@link RemoteSystemCapabilities}
     */
    public RemoteSystemCapabilities getCapabilities() {
        return this.capabilities;
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
            sess.setConfig("StrictHostKeyChecking", "accept-new");
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
            sess.setConfig("StrictHostKeyChecking", "accept-new");
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


    /**
     * Open the {@link SSHSession}.
     *
     * @return open {@link SSHSession}
     * @throws SSHException
     */
    public SSHSession openSession() throws SSHException {
        return openSession(getCapabilities(), host, port, userName, password, keyFile, keyPassPhrase);
    }


    /**
     * Open the {@link SSHSession}.
     *
     * @return open {@link SSHSession}
     * @throws SSHException
     */
    private static SSHSession openSession(
        RemoteSystemCapabilities capabilities,
        String host,
        int port,
        String userName,
        String password,
        File keyFile,
        String keyPassPhrase) throws SSHException {
        JSch jsch = new JSch();
        String message = "";
        boolean triedAuthentication = false;
        // Private key file is provided - Public Key Authentication
        if (keyFile != null) {
            LOG.log(DEBUG, () -> "Specified key file is " + keyFile);
            if (keyFile.exists()) {
                triedAuthentication = true;
                LOG.log(DEBUG, () -> "Adding identity for private key at " + keyFile);
                addIdentity(jsch, keyFile, keyPassPhrase);
            } else {
                message = "Specified key file does not exist \n";
            }
        } else if (password == null || password.isEmpty()) {
            message += "No key or password specified - trying default keys \n";
            LOG.log(DEBUG, "keyfile and password are null. Will try to authenticate with default key file if available");
            // check the default key locations if no authentication
            // method is explicitly configured.
            Path home = FileUtils.USER_HOME.toPath();
            for (String keyName : SSHUtil.SSH_KEY_FILE_NAMES) {
                message += "Tried to authenticate using " + keyName + "\n";
                File key = home.resolve(Path.of(SSH_DIR_NAME, keyName)).toFile();
                if (key.exists()) {
                    triedAuthentication = true;
                    addIdentity(jsch, key, keyPassPhrase);
                }
            }
        }

        final Session session = openSession(jsch, host, port, userName);
        try {
            // TODO: Insecure, maybe we could create an input field and allow user to check the host key?
            session.setConfig("StrictHostKeyChecking", "accept-new");
            session.setUserInfo(new GlassFishSshUserInfo());
            if (password != null && !password.isEmpty()) {
                LOG.log(DEBUG, () -> "Authenticating with password " + maskForLogging(password));
                triedAuthentication = true;
                session.setPassword(password);
            }
            if (!triedAuthentication) {
                throw new SSHException("Could not authenticate: " + message + '.');
            }
            session.connect();
            return new SSHSession(session, capabilities);
        } catch (SSHException e) {
            session.disconnect();
            throw e;
        } catch (JSchException e) {
            session.disconnect();
            throw new SSHException("Could not authenticate: " + message + '.', e);
        }
    }


    /**
     * Opens the SSH session using user password.
     * The resulting {@link SSHSession} is very limited, doesn't distinguish between operating
     * system capabilities, etc.
     *
     * @param passwordParam
     * @return {@link SSHSession}
     * @throws SSHException if the connection attempt failed.
     */
    public SSHSession openSession(String passwordParam) throws SSHException {
        JSch jsch = new JSch();
        Session session = openSession(jsch, host, port, userName);
        try {
            session.setConfig("StrictHostKeyChecking", "accept-new");
            session.setPassword(passwordParam);
            session.connect();
            return new SSHSession(session, getCapabilities());
        } catch (JSchException e) {
            throw new SSHException("Failed to connect.", e);
        }
    }


    /**
     * Open and close the session.
     *
     * @throws SSHException
     */
    public void pingConnection() throws SSHException {
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
    public boolean exists(SFTPPath path) throws SSHException {
        try (SSHSession session = openSession(); SFTPClient sftpClient = session.createSFTPClient()) {
            return sftpClient.exists(path);
        }
    }


    @Override
    public String toString() {
        String displayPassword = maskForLogging(passwordParameter);
        String displayKeyPassPhrase = maskForLogging(keyPassPhraseParameter);
        return String.format("host=%s port=%d user=%s password=%s keyFile=%s keyPassPhrase=%s, capabilities=%s", host, port, userName,
            displayPassword, keyFile, displayKeyPassPhrase, capabilities);
    }


    /**
     * Connects to the remote SSH server and does some simple analysis to be able to work both
     * with Linux or Windows based operating systems.
     *
     * @return {@link RemoteSystemCapabilities}
     * @throws SSHException
     */
    private static RemoteSystemCapabilities analyzeRemote(String host, int port, String userName, String password,
        File keyFile, String keyPassPhrase) {
        final String[] sysPropOutputLines;
        final RemoteSystemCapabilities capabilities = new RemoteSystemCapabilities(null, null, GENERIC, UTF_8);
        try (SSHSession session = openSession(capabilities, host, port, userName, password, keyFile, keyPassPhrase)) {
            if (LOG.isLoggable(DEBUG)) {
                Map<String, String> env = session.detectShellEnv();
                LOG.log(DEBUG, "Environment of the operating system obtained for the SSH client: {0}", env);
            }
            sysPropOutputLines = loadRemoteJavaSystemProperties(session);
        } catch (SSHException e) {
            String msg = "Failed to analyze the remote system. Some commands probably are not supported.";
            LOG.log(WARNING, msg, e);
            return new RemoteSystemCapabilities(null, null, GENERIC, UTF_8);
        }

        String javaHome = getValue("java.home", sysPropOutputLines);
        Version javaVersion = getProperty("java.version", sysPropOutputLines, Version::parse);
        OperatingSystem os = getProperty("os.name", sysPropOutputLines, OperatingSystem::parse);
        Charset charset = getProperty("file.encoding", sysPropOutputLines, Charset::forName);
        return new RemoteSystemCapabilities(javaHome, javaVersion, os, charset);
    }


    private static String[] loadRemoteJavaSystemProperties(SSHSession session) throws SSHException {
        StringBuilder outputBuilder = new StringBuilder();
        // java must be available in the environment.
        // If you use docker java images, check UsePam=yes and /etc/environment
        // By default images configure ENV properties just for the container app, not for ssh clients.
        final int code = session.exec(Arrays.asList("java", "-XshowSettings:properties", "-version"), null,
            outputBuilder);
        if (code != 0) {
            throw new SSHException("Java command on the remote host failed. Output: " + outputBuilder + '.');
        }

        return outputBuilder.toString().split("\\R");
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


    private static void addIdentity(JSch jsch, File identityFile, String keyPassPhrase) throws SSHException {
        try {
            jsch.addIdentity(identityFile.getAbsolutePath(), keyPassPhrase);
        } catch (JSchException e) {
            throw new SSHException("Invalid key passphrase for key: " + identityFile.getAbsolutePath() + ".", e);
        }
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


    private static Session openSession(JSch jsch, String host, int port, String userName) throws SSHException {
        try {
            return jsch.getSession(userName, host, port);
        } catch (JSchException e) {
            throw new SSHException("Could not authenticate user " + userName + " to " + host + ':' + port + '.', e);
        }
    }
}
