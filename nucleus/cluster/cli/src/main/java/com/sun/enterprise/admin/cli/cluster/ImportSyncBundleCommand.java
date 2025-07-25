/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.admin.cli.remote.RemoteCLICommand;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.glassfish.admin.payload.PayloadFilesManager.Perm;
import org.glassfish.admin.payload.PayloadImpl;
import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.Payload;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.admin.cli.CLIConstants.K_DAS_HOST;
import static com.sun.enterprise.admin.cli.CLIConstants.K_DAS_IS_SECURE;
import static com.sun.enterprise.admin.cli.CLIConstants.K_DAS_PORT;
import static com.sun.enterprise.admin.cli.CLIConstants.K_DAS_PROTOCOL;

/**
 * This is a local command that unbundles the bundle generated by export-sync-bundle.
 * import-sync-bundle applies the content under ${com.sun.aas.instanceRoot}/
 * directory. Synchronization cookie with DAS's timestamp should be created.
 * It also creates das.properties (if not present) under agent dir (ex.
 * installRoot/glassfish7/glassfish/nodes/<host-name>/agent/config/das.properties).
 *
 * Before running this command the instance should already have been registered in
 * DAS (server element created in DAS domain.xml) by running create-instance.
 * This command does not validate --node or instance_name.
 *
 * For upgrade - this command creates a new instance filesystem if it does not exist.
 * and completes DAS registration by setting rendezvousOccurred=true.
 *
 * For manual sync - this command creates a new instance filesystem or updates the
 * directories of an existing instance (remove existing application, generated,
 * config, docroot, lib dir first and explode the zip) and completes registration with DAS
 * by setting rendezvousOccurred=true.
 *
 * If setting of rendezvousOccurred=true with DAS fails, the command does not
 * not fail. Only a warning is printed out in the command output. We provide the
 * exact "asadmin set command" in this warning so that user can run
 * that command on DAS to change the rendezvousOccurred property for the server instance.
 *
 * Usage:
 *
 * import-sync-bundle [--node node_name] [--nodedir node_path] --file
 * xyz-sync-bundle.zip instance_name
 *
 * --node         name of the node; this is optional. The command fails if there
 * is more than one node under the default location
 * (installRoot/glassfish7/glassfish/nodes/<host-name>/)
 *
 * --nodedir      parent dir where node is created; this is optional. Default
 * location is installRoot/glassfish7/glassfish/nodes/
 *
 * --file         sync bundle created by export-sync-bundle
 *
 * instance_name  name of the server instance
 */
@Service(name = "import-sync-bundle")
@PerLookup
public class ImportSyncBundleCommand extends LocalInstanceCommand {
    @Param(name = "file_name", primary = true)
    private String syncBundle;

    @Param(name = "instance")
    private String instanceName0;

    @Param(name = "node", optional = true, alias = "nodeagent")
    protected String _node;

    String DASHost;
    int DASPort = -1;
    String DASProtocol;
    boolean dasIsSecure;

    private File dasPropsFile;
    private Properties dasProperties;

    private File syncBundleFile = null;
    private File agentConfigDir;
    private File backupDir;

    private static final String RENDEZVOUS_PROPERTY_NAME = "rendezvousOccurred";
    private String INSTANCE_DOTTED_NAME;
    private String RENDEZVOUS_DOTTED_NAME;
    //private String RENDEZVOUS_DOTTED_NAME_VALUE;
    //private boolean isDasRunning;

    /**
     */
    @Override
    protected void validate()
            throws CommandException {

        if(ok(instanceName0)) {
            instanceName = instanceName0;
        } else {
            throw new CommandException(Strings.get("Instance.badInstanceName"));
        }

        syncBundleFile = new File(syncBundle);
        if (!syncBundleFile.isFile()) {
            throw new CommandException(Strings.get("noFile", syncBundle));
        }

        if (!isRegisteredToDAS()) {
            throw new CommandException(Strings.get("import.sync.bundle.invalidInstance", instanceName));
        }
        node = _node;

        super.validate(); // set ServerDirs
        init();
    }

    private void init() throws CommandException {
        agentConfigDir = new File(nodeDirChild, "agent" + File.separator + "config");
        dasPropsFile = new File(agentConfigDir, "das.properties");

        if (dasPropsFile.isFile()) {
            setDasDefaults(dasPropsFile);
        }

        DASHost = programOpts.getHost();
        DASPort = programOpts.getPort();
        dasIsSecure = programOpts.isSecure();
        DASProtocol = "http";

        INSTANCE_DOTTED_NAME = "servers.server." + instanceName;
        RENDEZVOUS_DOTTED_NAME = INSTANCE_DOTTED_NAME + ".property." + RENDEZVOUS_PROPERTY_NAME;
        //RENDEZVOUS_DOTTED_NAME_VALUE = RENDEZVOUS_DOTTED_NAME + "=true";
    }

    private boolean isRegisteredToDAS() throws CommandException {
        boolean isRegisteredOnDAS = false;
        InputStream input = null;
        XMLStreamReader reader = null;
        ZipFile zip = null;
        try {
            //find node from domain.xml
            zip = new ZipFile(syncBundleFile);
            ZipEntry entry = zip.getEntry("config/domain.xml");
            if (entry != null) {
                input = zip.getInputStream(entry);

                reader = XMLInputFactory.newFactory().createXMLStreamReader(input);
                while (!isRegisteredOnDAS) {
                    int event = reader.next();

                    if (event == XMLStreamConstants.END_DOCUMENT) {
                        break;
                    }

                    if (event == XMLStreamConstants.START_ELEMENT && "server".equals(reader.getLocalName())) {
                        // get the attributes for this <server>
                        int num = reader.getAttributeCount();
                        Map<String, String> map = new HashMap<>();
                        for (int i = 0; i < num; i++) {
                            map.put(reader.getAttributeName(i).getLocalPart(), reader.getAttributeValue(i));
                        }
                        String thisName = map.get("name");
                        if (instanceName != null && instanceName.equals(thisName)) {
                            isRegisteredOnDAS = true;
                            if (_node == null) {  // if node not specified
                                _node = map.get("node"); // find it in domain.xml
                            }
                        }
                    }
                }
            } else {
                throw new CommandException(Strings.get("import.sync.bundle.domainXmlNotFound",
                    syncBundle));
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, Strings.get("import.sync.bundle.inboundPayloadFailed",
                    syncBundle, ex.getLocalizedMessage()), ex);
            throw new CommandException(Strings.get("import.sync.bundle.inboundPayloadFailed",
                    syncBundle, ex.getLocalizedMessage()), ex);
        } catch (XMLStreamException xe) {
            logger.log(Level.SEVERE, Strings.get("import.sync.bundle.inboundPayloadFailed",
                    syncBundle, xe.getLocalizedMessage()), xe);
            throw new CommandException(Strings.get("import.sync.bundle.inboundPayloadFailed",
                    syncBundle, xe.getLocalizedMessage()), xe);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    // ignored
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ex) {
                    // ignored
                }
            }
            if (zip != null) {
                try {
                    zip.close();
                } catch (Exception ex) {
                    // ignored
                }
            }
        }

        return isRegisteredOnDAS;
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException {
            int exitCode = createDirectories();
            if (exitCode == SUCCESS) {
                setRendezvousOccurred("true");
            }
            return exitCode;
    }

    private int createDirectories() throws CommandException {
        if (!agentConfigDir.isDirectory()) {
            if (!agentConfigDir.mkdirs()) {
                throw new CommandException(Strings.get("import.sync.bundle.createDirectoryFailed", agentConfigDir.getPath()));
            }
        }

        writeProperties();

        FileInputStream in = null;
        Payload.Inbound payload = null;
        try {
            in = new FileInputStream(syncBundle);
            payload = PayloadImpl.Inbound.newInstance("application/zip", in);
        } catch (IOException ex) {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioe) {
                logger.warning(Strings.get("import.sync.bundle.closeStreamFailed",
                            syncBundle, ioe.getLocalizedMessage()));
            }
            throw new CommandException(Strings.get("import.sync.bundle.inboundPayloadFailed",
                    syncBundle, ex.getLocalizedMessage()), ex);
        }
        backupInstanceDir();
        File targetDir = this.getServerDirs().getServerDir();
        if (!targetDir.mkdirs()) {
            restoreInstanceDir();
            throw new CommandException(Strings.get("import.sync.bundle.createDirectoryFailed", targetDir.getPath()));

        }
        Perm perm = new Perm(targetDir, null);

        try {
            perm.processParts(payload);
        } catch (Exception ex) {
            restoreInstanceDir();
            String msg = Strings.get("import.sync.bundle.extractBundleFailed",
                    syncBundle, targetDir.getAbsolutePath());
            if (ex.getLocalizedMessage() != null) {
                msg = msg + "\n" + ex.getLocalizedMessage();
            }
            throw new CommandException(msg, ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                logger.warning(Strings.get("import.sync.bundle.closeStreamFailed",
                            syncBundle, ex.getLocalizedMessage()));
            }
        }

        deleteBackupDir();
        return SUCCESS;
    }

    private void writeProperties() throws CommandException {
        try {
            if (!dasPropsFile.isFile()) {
                writeDasProperties();
            }
        } catch (IOException ex) {
            throw new CommandException(Strings.get("Instance.cantWriteProperties", "das.properties", ex.getLocalizedMessage()), ex);
        }
    }

    private void writeDasProperties() throws IOException {
        if (dasPropsFile.createNewFile()) {
            dasProperties = new Properties();
            dasProperties.setProperty(K_DAS_HOST, DASHost);
            dasProperties.setProperty(K_DAS_PORT, String.valueOf(DASPort));
            dasProperties.setProperty(K_DAS_IS_SECURE, String.valueOf(dasIsSecure));
            dasProperties.setProperty(K_DAS_PROTOCOL, DASProtocol);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(dasPropsFile);
                dasProperties.store(fos, Strings.get("Instance.dasPropertyComment"));
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }

    private void backupInstanceDir() throws CommandException {
        File f = getServerDirs().getServerDir();
        if (f != null && f.isDirectory()) {
            SecureRandom r = new SecureRandom();
            setBackupDir(r.nextInt());
            File backup = getBackupDir();
            if (!f.renameTo(backup)) {
                logger.warning(Strings.get("import.sync.bundle.backupInstanceDirFailed", f.getAbsolutePath(), backup.getAbsolutePath()));
                if (FileUtils.whack(f)) { //Ask user first before deleting?
                    logger.warning(Strings.get("import.sync.bundle.deletedInstanceDir", f.getAbsolutePath()));
                }
            }

        }
    }

    private void setBackupDir(int i) {
        File f = getServerDirs().getServerDir();
        backupDir = new File(getServerDirs().getServerParentDir(), f.getName() + "_backup" + i);
    }

    private File getBackupDir() {
        return backupDir;
    }

    private void restoreInstanceDir() {
        File backup = getBackupDir();
        if (backup != null && backup.isDirectory()) {
            File dir = getServerDirs().getServerDir();
            boolean gone = ! FileUtils.deleteFileMaybe(getServerDirs().getServerDir());
            if (!gone || !backup.renameTo(dir)) {
                logger.warning(Strings.get("import.sync.bundle.restoreInstanceDirFailed", backup.getAbsolutePath(), getServerDirs().getServerDir().getAbsolutePath()));
            }
        }
    }

    private void deleteBackupDir() {
        File backup = getBackupDir();
        if (backup != null && backup.isDirectory()) {
            FileUtils.whack(backup);
        }
    }

    private void setRendezvousOccurred(String rendezVal) {
        String dottedName = RENDEZVOUS_DOTTED_NAME + "=" + rendezVal;
        try {
            RemoteCLICommand rc = new RemoteCLICommand("set", this.programOpts, this.env);
            rc.executeAndReturnOutput("set", dottedName);
        } catch (CommandException ex) {
            logger.warning(Strings.get("import.sync.bundle.completeRegistrationFailed", dottedName));
        }
    }



}
