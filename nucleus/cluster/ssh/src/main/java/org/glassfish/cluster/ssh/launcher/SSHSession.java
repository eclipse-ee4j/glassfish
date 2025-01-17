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

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sun.enterprise.util.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.glassfish.cluster.ssh.sftp.SFTPClient;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Bridge for the Jsch {@link Session}.
 */
public class SSHSession implements AutoCloseable {

    private static final Logger LOG = System.getLogger(SSHSession.class.getName());

    /** The connection object that represents the connection to the host via ssh */
    private final Session session;
    private final RemoteSystemCapabilities capabilities;

    /**
     * This constructor uses GENERIC operating system. Suitable just for operations where
     * you don't care about commands available on the operating system.
     *
     * @param session
     */
    SSHSession(Session session) {
        this.session = session;
        this.capabilities = new RemoteSystemCapabilities(null, null, OperatingSystem.GENERIC);
    }


    /**
     * This constructor should be preferred to provide the full service - respects the operating
     * system capabilities.
     *
     * @param session
     * @param capabilities
     */
    SSHSession(Session session, RemoteSystemCapabilities capabilities) {
        this.session = session;
        this.capabilities = capabilities;
    }


    /**
     * @return true if connected
     */
    public boolean isOpen() {
        return session.isConnected();
    }


    /**
     * Detects environment variables configured in the remote shell.
     *
     * @return map of environment variables
     * @throws SSHException
     */
    public Map<String, String> detectShellEnv() throws SSHException {
        ChannelShell shell;
        try {
            shell = (ChannelShell) session.openChannel("shell");
        } catch (JSchException e) {
            throw new SSHException("Could not open the shell session.", e);
        }
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8192);
        try {
            // Command is executable both in Linux and Windows os
            shell.setInputStream(listInputStream(List.of("env || set")));
            shell.setPty(false);
            InputStream in = shell.getInputStream();
            PumpThread t1 = new PumpThread(in, outputStream);
            t1.start();
            shell.connect();
            t1.join();
            // Don't check the exit code, returns -1 on windows.
            String output = outputStream.toString(UTF_8);
            LOG.log(DEBUG, () -> "Environment options - command output: \n" + output);
            return parseProperties(output);
        } catch (Exception e) {
            throw new SSHException(
                "Could not detect shell environment options. Output: " + outputStream.toString(UTF_8), e);
        } finally {
            shell.disconnect();
        }
    }


    /**
     * Detects default system properties of the remote java command.
     *
     * @return map of system properties
     * @throws SSHException
     */
    public Map<String, String> detectJavaSystemProperties() throws SSHException {
        ChannelShell shell;
        try {
            shell = (ChannelShell) session.openChannel("shell");
        } catch (JSchException e) {
            throw new SSHException("Could not open the shell session.", e);
        }
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8192);
        try {
            // Command is executable both in Linux and Windows os
            shell.setInputStream(listInputStream(List.of("env || set")));
            shell.setPty(false);
            InputStream in = shell.getInputStream();
            PumpThread t1 = new PumpThread(in, outputStream);
            t1.start();
            shell.connect();
            t1.join();
            // Don't check the exit code, returns -1 on windows.
            String output = outputStream.toString(UTF_8);
            LOG.log(DEBUG, () -> "Environment options - command output: \n" + output);
            return parseProperties(output);
        } catch (Exception e) {
            throw new SSHException(
                "Could not detect shell environment options. Output: " + outputStream.toString(UTF_8), e);
        } finally {
            shell.disconnect();
        }
    }


    /**
     * Unpacks the zip file to the target directory.
     * <p>
     * On Linux it uses <code>cd</code> and <code>jar</code> commands.<br>
     * On Windows it uses PowerShell commands.
     *
     * @param remoteZipFile
     * @param remoteDir
     * @throws SSHException
     */
    public void unzip(Path remoteZipFile, Path remoteDir) throws SSHException {
        final String unzipCommand;
        if (capabilities.getOperatingSystem() == OperatingSystem.WINDOWS) {
              unzipCommand = "PowerShell.exe -Command \"Expand-Archive -LiteralPath '" + remoteZipFile
                  + "' -DestinationPath '" + remoteDir + "'\"";
        } else {
            unzipCommand = "cd \"" + remoteDir + "\"; jar -xvf \"" + remoteZipFile + "\"";
        }

        final StringBuilder output = new StringBuilder();
        try {
            int status = exec(unzipCommand, null, output);
            if (status != 0) {
                throw new SSHException("Failed unpacking glassfish zip file. Output: " + output + ".");
            }
            LOG.log(DEBUG, () -> "Unpacked " + remoteZipFile + " to directory " + remoteDir);
        } catch (JSchException e) {
            throw new SSHException("Failed unpacking glassfish zip file. Output: " + output + ".", e);
        }
    }


    /**
     * SFTP exec command.
     * Executes a command on the remote system via ssh, optionally sending
     * lines of data to the remote process's System.in.
     *
     * @param command - command line parts
     * @param stdinLines - lines used to fake standard input in STDIN stream. Can be null.
     * @param output - empty collector of the output. Can be null.
     * @return exit code
     * @throws SSHException
     * @throws JSchException
     */
    public int exec(List<String> command, List<String> stdinLines, StringBuilder output)
        throws SSHException, JSchException {
        return exec(commandListToQuotedString(command), listInputStream(stdinLines), output);
    }


    /**
     * SFTP exec command.
     * Executes a command on the remote system via ssh, optionally sending
     * lines of data to the remote process's System.in.
     *
     * @param command - command line parts
     * @param stdinLines - lines used to fake standard input in STDIN stream. Can be null.
     * @return exit code
     * @throws SSHException
     */
    public int exec(List<String> command, List<String> stdinLines) throws JSchException, SSHException {
        return exec(commandListToQuotedString(command), stdinLines);
    }


    /**
     * SFTP exec command.
     * Executes a command on the remote system via ssh, optionally sending
     * lines of data to the remote process's System.in.
     *
     * @param command - command to execute. If it has arguments, better use {@link #exec(List, List)}.
     * @param stdinLines - lines used to fake standard input in STDIN stream. Can be null.
     * @return exit code
     * @throws SSHException
     */
    public int exec(String command, List<String> stdinLines) throws JSchException, SSHException {
        return exec(command, listInputStream(stdinLines));
    }


    /**
     * SFTP exec command without STDIN and without reading the output.
     * Executes a command on the remote system via ssh.
     *
     * @param command - command to execute. If it has arguments, better use {@link #exec(List)}.
     * @return exit code
     * @throws SSHException
     */
    public int exec(final String command)
        throws JSchException, SSHException {
        return exec(command, (InputStream) null);
    }


    /**
     * SFTP exec command.
     * Executes a command on the remote system via ssh, optionally sending
     * lines of data to the remote process's System.in.
     *
     * @param command - command to execute. If it has arguments, better use {@link #exec(List, List)}.
     * @param stdin - stream used to fake standard input in STDIN stream. Can be null.
     * @return exit code
     * @throws SSHException
     */
    public int exec(final String command, final InputStream stdin) throws JSchException, SSHException {
        return exec(command, stdin, null);
    }


    /**
     * SFTP exec command.
     * Executes a command on the remote system via ssh, optionally sending
     * lines of data to the remote process's System.in.
     *
     * @param command - command to execute. If it has arguments, better use {@link #exec(List, List, StringBuilder)}.
     * @param stdin - stream used to fake standard input in STDIN stream. Can be null.
     * @return exit code
     * @throws SSHException
     */
    public int exec(final String command, final InputStream stdin, final StringBuilder output)
        throws JSchException, SSHException {
        LOG.log(INFO, () -> "Executing command " + command + " on host: " + session.getHost());
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8192);
        final ChannelExec execChannel = (ChannelExec) session.openChannel("exec");
        try {
            execChannel.setInputStream(stdin);
            execChannel.setCommand(command);
            InputStream in = execChannel.getInputStream();
            PumpThread t1 = new PumpThread(in, outputStream);
            t1.start();
            PumpThread t2 = new PumpThread(execChannel.getErrStream(), outputStream);
            t2.start();
            execChannel.connect();

            t1.join();
            t2.join();
            if (output != null || LOG.isLoggable(DEBUG)) {
                String commandOutput = outputStream.toString(UTF_8);
                LOG.log(DEBUG, () -> "Command output: \n" + commandOutput);
                if (output != null) {
                    output.append(commandOutput);
                }
            }
            if (execChannel.isClosed()) {
                return execChannel.getExitStatus();
            }
            return -1;
        } catch (Exception e) {
            throw new SSHException("Command " + command + " failed. Output: " + outputStream.toString(UTF_8), e);
        } finally {
            execChannel.disconnect();
        }
    }


    /**
     * @return new {@link SFTPClient}
     * @throws JSchException
     */
    public SFTPClient createSFTPClient() throws JSchException {
        return new SFTPClient((ChannelSftp) session.openChannel("sftp"));
    }


    @Override
    public void close() {
        if (session.isConnected()) {
            session.disconnect();
        }
    }


    /**
     * Take a command in the form of a list and convert it to a command string.
     * If any string in the list has spaces then the string is quoted before
     * being added to the final command string.
     *
     * @param command
     * @return
     */
    private static String commandListToQuotedString(List<String> command) {
        if (command.size() == 1) {
            return command.get(0);
        }
        StringBuilder commandBuilder  = new StringBuilder();
        boolean first = true;

        for (String s : command) {
            if (!first) {
                commandBuilder.append(" ");
            } else {
                first = false;
            }
            if (s.contains(" ")) {
                // Quote parts of the command that contain a space
                commandBuilder.append(FileUtils.quoteString(s));
            } else {
                commandBuilder.append(s);
            }
        }
        return commandBuilder.toString();
    }


    private static InputStream listInputStream(final List<String> stdinLines) {
        if (stdinLines == null) {
            return null;
        }
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (String line : stdinLines) {
                baos.write(line.getBytes(UTF_8));
                baos.write('\n');
            }
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot copy the input to UTF-8 byte array input stream.", e);
        }
    }


    private static Map<String, String> parseProperties(String output) {
        String[] lines = output.split("\\R");
        Map<String, String> properties = new TreeMap<>();
        for (String line : lines) {
            int equalSignPosition = line.indexOf('=');
            if (equalSignPosition <= 0) {
                continue;
            }
            String key = line.substring(0, equalSignPosition);
            String value = equalSignPosition == line.length() - 1 ? "" : line.substring(equalSignPosition + 1);
            properties.put(key.strip(), value.strip());
        }
        return properties;
    }


    /**
     * Pumps {@link InputStream} to {@link OutputStream}.
     *
     * @author Kohsuke Kawaguchi
     */
    private static final class PumpThread extends Thread {
        private final InputStream in;
        private final OutputStream out;

        public PumpThread(InputStream in, OutputStream out) {
            super("pump thread");
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            byte[] buf = new byte[8192];
            try {
                while(true) {
                    int len = in.read(buf);
                    if(len<0) {
                        in.close();
                        return;
                    }
                    out.write(buf,0,len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
