/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.server.logging.logviewer.backend;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.launcher.SSHSession;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * @author Naman Mehta
 * Date: 6 Aug, 2010
 * Time: 11:20:48 AM
 */
public class LogFilterForInstance {

    public File downloadGivenInstanceLogFile(ServiceLocator habitat, Server targetServer, Domain domain, Logger logger,
        String instanceName, String domainRoot, String logFileName, String instanceLogFileName) throws IOException {

        // method is used from logviewer back end code logfilter.
        // for Instance it's going through this loop. This will use ssh utility to get file from instance machine(remote machine) and
        // store in domains/domain1/logs/<instance name> which is used to get LogFile object.
        // Right now user needs to go through this URL to setup and configure ssh http://wikis.sun.com/display/GlassFish/3.1SSHSetup
        String sNode = targetServer.getNodeRef();
        Nodes nodes = domain.getNodes();
        Node node = nodes.getNode(sNode);
        if (!node.getType().equals("SSH")) {
            return null;
        }

        final SSHLauncher sshL = new SSHLauncher(node);
        try (SSHSession session = sshL.openSession(); SFTPClient sftpClient = session.createSFTPClient()) {
            File logFileDirectoryOnServer = makingDirectory(Path.of(domainRoot, "logs", instanceName));
            boolean noFileFound = true;
            Path loggingDir = getLoggingDirectoryForNode(instanceLogFileName, node, sNode, instanceName);
            try {
                List<String> instanceLogFileNames = sftpClient.ls(loggingDir, this::isAcceptable);
                if (!instanceLogFileNames.isEmpty()) {
                    noFileFound = false;
                }
            } catch (Exception e) {
                // if directory doesn't present or missing on remote machine. It happens due
                // to bug 16451
                noFileFound = true;
            }

            if (noFileFound) {
                // this loop is used when user has changed value for server.log but not
                // restarted the server.
                loggingDir = getLoggingDirectoryForNodeWhenNoFilesFound(instanceLogFileName, node, sNode,
                    instanceName);
            }

            Path loggingFile = loggingDir.resolve(logFileName);
            if (!sftpClient.exists(loggingFile)) {
                loggingFile = loggingDir.resolve("server.log");
            } else if (!sftpClient.exists(loggingFile)) {
                loggingFile = Path.of(instanceLogFileName);
            }

            // creating local file name on DAS
            long instanceLogFileSize = 0;
            File instanceLogFile = logFileDirectoryOnServer.toPath().resolve(loggingFile.getFileName()).toFile();

            // getting size of the file on DAS
            if (instanceLogFile.exists()) {
                instanceLogFileSize = instanceLogFile.length();
            }

            // getting size of the file on instance machine
            SftpATTRS sftpFileAttributes = sftpClient.stat(loggingFile);
            long fileSizeOnNode = sftpFileAttributes.getSize();

            // if differ both size then downloading
            if (instanceLogFileSize != fileSizeOnNode) {
                sftpClient.download(loggingFile, instanceLogFile.toPath());
            }
            return instanceLogFile;
        } catch (JSchException ex) {
            throw new IOException("Unable to download instance log file from SSH Node", ex);
        } catch (SftpException ex) {
            throw new IOException("Unable to download instance log file from SSH Node", ex);
        }
    }


    public void downloadAllInstanceLogFiles(ServiceLocator habitat, Server targetServer, Domain domain, Logger logger,
                                            String instanceName, Path tempDirectoryOnServer, String instanceLogFileDirectory)
            throws IOException {

        // method is used from collect-log-files command
        // for Instance it's going through this loop. This will use ssh utility to get file from instance machine(remote machine) and
        // store in  tempDirectoryOnServer which is used to create zip file.
        // Right now user needs to go through this URL to setup and configure ssh http://wikis.sun.com/display/GlassFish/3.1SSHSetup
        String sNode = targetServer.getNodeRef();
        Nodes nodes = domain.getNodes();
        Node node = nodes.getNode(sNode);

        if (node.getType().equals("SSH")) {
            try {
                List<String> allInstanceLogFileNames = getInstanceLogFileNames(habitat, targetServer, domain, logger,
                    instanceName, instanceLogFileDirectory);

                boolean noFileFound = true;
                Path sourceDir = getLoggingDirectoryForNode(instanceLogFileDirectory, node, sNode, instanceName);
                final SSHLauncher sshL = new SSHLauncher(node);
                try (SSHSession session = sshL.openSession(); SFTPClient sftpClient = session.createSFTPClient()) {

                    try {
                        List<String> instanceLogFileNames = sftpClient.ls(sourceDir, this::isAcceptable);
                        if (!instanceLogFileNames.isEmpty()) {
                            noFileFound = false;
                        }
                    } catch (Exception e) {
                        // if directory doesn't present or missing on remote machine.
                        // It happens due to bug 16451
                        noFileFound = true;
                    }

                    if (noFileFound) {
                        // this loop is used when user has changed value for server.log but not
                        // restarted the server.
                        sourceDir = getLoggingDirectoryForNodeWhenNoFilesFound(instanceLogFileDirectory, node, sNode,
                            instanceName);
                    }

                    for (String fileName : allInstanceLogFileNames) {
                        sftpClient.download(sourceDir.resolve(fileName), tempDirectoryOnServer.resolve(fileName));
                    }
                }
            } catch (JSchException ex) {
                throw new IOException("Unable to download instance log file from SSH Node", ex);
            } catch (SftpException ex) {
                throw new IOException("Unable to download instance log file from SSH Node", ex);
            }
        }
    }

    public List<String> getInstanceLogFileNames(ServiceLocator habitat, Server targetServer, Domain domain, Logger logger,
                                          String instanceName, String instanceLogFileDetails) throws IOException {

        // helper method to get all log file names for given instance
        String sNode = targetServer.getNodeRef();
        Node node = domain.getNodes().getNode(sNode);
        List<String> instanceLogFileNamesAsString = new ArrayList<>();

        // this code is used when DAS and instances are running on the same machine
        if (node.isLocal()) {
            Path loggingDir = getLoggingDirectoryForNode(instanceLogFileDetails, node, sNode, instanceName);

            File logsDir = loggingDir.toFile();
            File allLogFileNames[] = logsDir.listFiles();

            boolean noFileFound = true;

            if (allLogFileNames != null) { // This check for,  if directory doesn't present or missing on machine. It happens due to bug 16451
                for (File file : allLogFileNames) {
                    String fileName = file.getName();
                    // code to remove . and .. file which is return
                    if (file.isFile() && !fileName.equals(".") && !fileName.equals("..") && fileName.contains(".log")
                            && !fileName.contains(".log.")) {
                        instanceLogFileNamesAsString.add(fileName);
                        noFileFound = false;
                    }
                }
            }

            if (noFileFound) {
                // this loop is used when user has changed value for server.log but not restarted the server.
                loggingDir = getLoggingDirectoryForNodeWhenNoFilesFound(instanceLogFileDetails, node, sNode, instanceName);
                logsDir = loggingDir.toFile();
                allLogFileNames = logsDir.listFiles();

                for (File file : allLogFileNames) {
                    String fileName = file.getName();
                    // code to remove . and .. file which is return
                    if (file.isFile() && !fileName.equals(".") && !fileName.equals("..") && fileName.contains(".log")
                            && !fileName.contains(".log.")) {
                        instanceLogFileNamesAsString.add(fileName);
                    }
                }
            }
        } else if (node.getType().equals("SSH")) {
                // this code is used if DAS and instance are running on different machine
            SSHLauncher sshL = new SSHLauncher(node);
            try (SSHSession session = sshL.openSession(); SFTPClient sftpClient = session.createSFTPClient()) {
                boolean noFileFound = true;
                Path loggingDir = getLoggingDirectoryForNode(instanceLogFileDetails, node, sNode, instanceName);
                try {
                    List<String> instanceLogFileNames = sftpClient.ls(loggingDir, this::isAcceptable);
                    for (String file : instanceLogFileNames) {
                        instanceLogFileNamesAsString.add(file);
                        noFileFound = false;
                    }
                } catch (Exception ex) {
                    // if directory doesn't present or missing on remote machine. It happens due
                    // to bug 16451
                    noFileFound = true;
                }

                if (noFileFound) {
                    // this loop is used when user has changed value for server.log but not
                    // restarted the server.
                    loggingDir = getLoggingDirectoryForNodeWhenNoFilesFound(instanceLogFileDetails, node, sNode,
                        instanceName);
                    List<String> instanceLogFileNames = sftpClient.ls(loggingDir, this::isAcceptable);
                    instanceLogFileNamesAsString.addAll(instanceLogFileNames);
                }
            } catch (JSchException ex) {
                throw new IOException("Unable to download instance log file from SSH Node", ex);
            } catch (SftpException ex) {
                throw new IOException("Unable to download instance log file from SSH Node", ex);
            }
        }
        return instanceLogFileNamesAsString;
    }

    private File makingDirectory(Path path) {
        File targetDir = path.toFile();
        if (targetDir.exists()) {
            if (!targetDir.delete()) {
                return targetDir;
            }
        }
        if (targetDir.mkdir()) {
            return targetDir;
        }
        return null;
    }

    public Path getLoggingDirectoryForNode(String instanceLogFileDirectory, Node node, String sNode, String instanceName) {
        if (instanceLogFileDirectory.contains("${com.sun.aas.instanceRoot}/logs") && node.getNodeDir() != null) {
            // this code is used if no changes made in logging.properties file
            return new File(node.getNodeDir()).toPath().resolve(Path.of(sNode, instanceName, "logs"));
        } else if (instanceLogFileDirectory.contains("${com.sun.aas.instanceRoot}/logs")
            && node.getInstallDir() != null) {
            return new File(node.getInstallDir()).toPath()
                .resolve(Path.of("glassfish", "nodes", sNode, instanceName, "logs"));
        } else {
            return new File(instanceLogFileDirectory).toPath();
        }
    }

    public Path getLoggingDirectoryForNodeWhenNoFilesFound(String instanceLogFileDirectory, Node node, String sNode, String instanceName) {
        if (node.getNodeDir() != null) {
            // this code is used if no changes made in logging.properties file
            return new File(node.getNodeDir()).toPath().resolve(Path.of(sNode, instanceName, "logs"));
        } else if (node.getInstallDir() != null) {
            return new File(node.getInstallDir()).toPath().resolve(Path.of("glassfish", "nodes", sNode, instanceName, "logs"));
        } else {
            return new File(instanceLogFileDirectory).toPath();
        }
    }


    private boolean isAcceptable(LsEntry file) {
        if (file.getAttrs().isDir()) {
            return false;
        }
        String fileName = file.getFilename();
        return fileName.contains(".log") //
            && !fileName.equals(".") && !fileName.equals("..") && !fileName.contains(".log.");
    }
}
