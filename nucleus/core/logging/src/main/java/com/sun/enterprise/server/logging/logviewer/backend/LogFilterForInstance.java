/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * @author Naman Mehta
 * Date: 6 Aug, 2010
 * Time: 11:20:48 AM
 */
public class LogFilterForInstance {

    public File downloadGivenInstanceLogFile(ServiceLocator habitat, Server targetServer, Domain domain, Logger logger,
                                             String instanceName, String domainRoot, String logFileName, String instanceLogFileName)
            throws IOException {

        File instanceLogFile = null;

        // method is used from logviewer back end code logfilter.
        // for Instance it's going through this loop. This will use ssh utility to get file from instance machine(remote machine) and
        // store in domains/domain1/logs/<instance name> which is used to get LogFile object.
        // Right now user needs to go through this URL to setup and configure ssh http://wikis.sun.com/display/GlassFish/3.1SSHSetup
        SSHLauncher sshL = getSSHL(habitat);
        String sNode = targetServer.getNodeRef();
        Nodes nodes = domain.getNodes();
        Node node = nodes.getNode(sNode);

        if (node.getType().equals("SSH")) {

            try {
                sshL.init(node, logger);

                try (SFTPClient sftpClient = sshL.getSFTPClient()) {
                    File logFileDirectoryOnServer = makingDirectory(
                        domainRoot + File.separator + "logs" + File.separator + instanceName);
                    boolean noFileFound = true;
                    String loggingDir = getLoggingDirectoryForNode(instanceLogFileName, node, sNode, instanceName);
                    try {
                        @SuppressWarnings("unchecked")
                        Vector<LsEntry> instanceLogFileNames = sftpClient.getSftpChannel().ls(loggingDir);
                        for (LsEntry file : instanceLogFileNames) {
                            // code to remove . and .. file which is return from sftpclient ls
                            // method
                            if (isAcceptable(file)) {
                                noFileFound = false;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // if directory doesn't present or missing on remote machine. It happens due
                        // to bug 16451
                        noFileFound = true;
                    }

                    if (noFileFound) {
                        // this loop is used when user has changed value for server.log but not
                        // restarted the server.
                        loggingDir = getLoggingDirectoryForNodeWhenNoFilesFound(instanceLogFileName, node, sNode, instanceName);
                    }

                    String loggingFile = loggingDir + File.separator + logFileName;
                    if (!sftpClient.exists(loggingFile)) {
                        loggingFile = loggingDir + File.separator + "server.log";
                    } else if (!sftpClient.exists(loggingFile)) {
                        loggingFile = instanceLogFileName;
                    }

                    // creating local file name on DAS
                    long instanceLogFileSize = 0;
                    instanceLogFile = new File(logFileDirectoryOnServer.getAbsolutePath() + File.separator
                        + loggingFile.substring(loggingFile.lastIndexOf(File.separator), loggingFile.length()));

                    // getting size of the file on DAS
                    if (instanceLogFile.exists()) {
                        instanceLogFileSize = instanceLogFile.length();
                    }

                    SftpATTRS sftpFileAttributes = sftpClient._stat(loggingFile);

                    // getting size of the file on instance machine
                    long fileSizeOnNode = sftpFileAttributes.getSize();

                    // if differ both size then downloading
                    if (instanceLogFileSize != fileSizeOnNode) {
                        try (InputStream inputStream = sftpClient.getSftpChannel().get(loggingFile);
                            BufferedInputStream in = new BufferedInputStream(inputStream);
                            FileOutputStream file = new FileOutputStream(instanceLogFile);
                            BufferedOutputStream out = new BufferedOutputStream(file)) {
                            int i;
                            while ((i = in.read()) != -1) {
                                out.write(i);
                            }
                            out.flush();
                        }
                    }
                }
            } catch (JSchException ex) {
                throw new IOException("Unable to download instance log file from SSH Node", ex);
            } catch (SftpException ex) {
                throw new IOException("Unable to download instance log file from SSH Node", ex);
            }
        }

        return instanceLogFile;

    }

    public void downloadAllInstanceLogFiles(ServiceLocator habitat, Server targetServer, Domain domain, Logger logger,
                                            String instanceName, String tempDirectoryOnServer, String instanceLogFileDirectory)
            throws IOException {

        // method is used from collect-log-files command
        // for Instance it's going through this loop. This will use ssh utility to get file from instance machine(remote machine) and
        // store in  tempDirectoryOnServer which is used to create zip file.
        // Right now user needs to go through this URL to setup and configure ssh http://wikis.sun.com/display/GlassFish/3.1SSHSetup
        SSHLauncher sshL = getSSHL(habitat);
        String sNode = targetServer.getNodeRef();
        Nodes nodes = domain.getNodes();
        Node node = nodes.getNode(sNode);

        if (node.getType().equals("SSH")) {
            try {
                sshL.init(node, logger);

                List<String> allInstanceLogFileName = getInstanceLogFileNames(habitat, targetServer, domain, logger,
                    instanceName, instanceLogFileDirectory);

                boolean noFileFound = true;
                String sourceDir = getLoggingDirectoryForNode(instanceLogFileDirectory, node, sNode, instanceName);
                SFTPClient sftpClient = sshL.getSFTPClient();

                try {
                    @SuppressWarnings("unchecked")
                    List<LsEntry> instanceLogFileNames = sftpClient.getSftpChannel().ls(sourceDir);
                    for (LsEntry file : instanceLogFileNames) {
                        // code to remove . and .. file which is return from sftpclient ls method
                        if (isAcceptable(file)) {
                            noFileFound = false;
                            break;
                        }
                    }
                } catch (Exception e) {
                    // if directory doesn't present or missing on remote machine. It happens due to bug 16451
                    noFileFound = true;
                }

                if (noFileFound) {
                    // this loop is used when user has changed value for server.log but not restarted the server.
                    sourceDir = getLoggingDirectoryForNodeWhenNoFilesFound(instanceLogFileDirectory, node, sNode, instanceName);
                }

                for (Object element : allInstanceLogFileName) {
                    String remoteFileName = sourceDir + File.separator + element;
                    InputStream inputStream = sftpClient.getSftpChannel().get(remoteFileName);
                    Files.copy(inputStream, Paths.get(tempDirectoryOnServer));
                }
                sftpClient.close();
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
            String loggingDir = getLoggingDirectoryForNode(instanceLogFileDetails, node, sNode, instanceName);

            File logsDir = new File(loggingDir);
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
                logsDir = new File(loggingDir);
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
            try {
                // this code is used if DAS and instance are running on different machine
                SSHLauncher sshL = getSSHL(habitat);
                sshL.init(node, logger);
                try (SFTPClient sftpClient = sshL.getSFTPClient()) {
                    boolean noFileFound = true;
                    String loggingDir = getLoggingDirectoryForNode(instanceLogFileDetails, node, sNode, instanceName);
                    try {
                        @SuppressWarnings("unchecked")
                        Vector<LsEntry> instanceLogFileNames = sftpClient.getSftpChannel().ls(loggingDir);
                        for (LsEntry file : instanceLogFileNames) {
                            // code to remove . and .. file which is return from sftpclient ls method
                            if (isAcceptable(file)) {
                                instanceLogFileNamesAsString.add(file.getFilename());
                                noFileFound = false;
                            }
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
                        @SuppressWarnings("unchecked")
                        Vector<LsEntry> instanceLogFileNames = sftpClient.getSftpChannel().ls(loggingDir);
                        for (LsEntry file : instanceLogFileNames) {
                            // code to remove . and .. file which is return from sftpclient ls
                            // method
                            if (isAcceptable(file)) {
                                instanceLogFileNamesAsString.add(file.getFilename());
                            }
                        }
                    }

                }
            } catch (JSchException ex) {
                throw new IOException("Unable to download instance log file from SSH Node", ex);
            } catch (SftpException ex) {
                throw new IOException("Unable to download instance log file from SSH Node", ex);
            }
        }
        return instanceLogFileNamesAsString;
    }

    private SSHLauncher getSSHL(ServiceLocator habitat) {
        return habitat.getService(SSHLauncher.class);
    }

    private File makingDirectory(String path) {
        File targetDir = new File(path);
        boolean created = false;
        boolean deleted = false;
        if (targetDir.exists()) {
            deleted = targetDir.delete();
            if (!deleted) {
                return targetDir;
            }

        }
        created = targetDir.mkdir();
        if (created) {
            return targetDir;
        }
        return null;

    }

    public String getLoggingDirectoryForNode(String instanceLogFileDirectory, Node node, String sNode, String instanceName) {
        String loggingDir = "";

        if (instanceLogFileDirectory.contains("${com.sun.aas.instanceRoot}/logs") && node.getNodeDir() != null) {
            // this code is used if no changes made in logging.properties file
            loggingDir = node.getNodeDir() + File.separator + sNode
                    + File.separator + instanceName + File.separator + "logs";
        } else if (instanceLogFileDirectory.contains("${com.sun.aas.instanceRoot}/logs") && node.getInstallDir() != null) {
            loggingDir = node.getInstallDir() + File.separator + "glassfish" + File.separator + "nodes"
                    + File.separator + sNode + File.separator + instanceName + File.separator + "logs";
        } else {
            // this code is used when user changes the attributes value(com.sun.enterprise.server.logging.GFFileHandler.file) in
            // logging.properties file to something else.
            loggingDir = instanceLogFileDirectory.substring(0, instanceLogFileDirectory.lastIndexOf(File.separator));
        }

        return loggingDir;
    }

    public String getLoggingDirectoryForNodeWhenNoFilesFound(String instanceLogFileDirectory, Node node, String sNode, String instanceName) {
        String loggingDir = "";

        if (node.getNodeDir() != null) {
            // this code is used if no changes made in logging.properties file
            loggingDir = node.getNodeDir() + File.separator + sNode
                    + File.separator + instanceName + File.separator + "logs";
        } else if (node.getInstallDir() != null) {
            loggingDir = node.getInstallDir() + File.separator + "glassfish" + File.separator + "nodes"
                    + File.separator + sNode + File.separator + instanceName + File.separator + "logs";
        } else {
            // this code is used when user changes the attributes value(com.sun.enterprise.server.logging.GFFileHandler.file) in
            // logging.properties file to something else.
            loggingDir = instanceLogFileDirectory.substring(0, instanceLogFileDirectory.lastIndexOf(File.separator));
        }

        return loggingDir;

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
