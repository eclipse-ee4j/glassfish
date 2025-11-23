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

import com.sun.enterprise.admin.servermgmt.NodeKeystoreManager;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.admin.util.CommandModelData.ParamModelData;
import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import com.sun.enterprise.util.HostAndPort;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_ALIAS;
import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_FILENAME;
import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_PASSWORD;
import static com.sun.enterprise.util.SystemPropertyConstants.TRUSTSTORE_FILENAME_DEFAULT;

/**
 * The change-master-password command for a node.
 * It takes in a nodeDir and node name
 *
 * @author Bhakti Mehta
 */
@Service(name = "_change-master-password-node")
@PerLookup
public  class ChangeNodeMasterPasswordCommand extends LocalInstanceCommand {

    @Param(name = "nodedir", optional = true)
    protected String nodeDir0;           // nodeDirRoot

    @Param(name = "node", primary = true)
    protected String node0;

    @Param(name = "savemasterpassword", optional = true, defaultValue = "false")
    protected boolean savemp;


    private static final LocalStringsImpl strings =
            new LocalStringsImpl(ChangeNodeMasterPasswordCommand.class);

    private String newPassword;

    private String oldPassword;


    @Override
    protected int executeCommand() throws CommandException {

        try {
            nodeDir = nodeDir0;
            node = node0;
            File serverDir = new File(nodeDir,node);

            if (!serverDir.isDirectory()) {
                throw new CommandException(strings.get("bad.node.dir",serverDir));
            }

            ArrayList<String> serverNames = getInstanceDirs(serverDir);
            for (String serverName: serverNames) {
                if (isRunning(serverDir, serverName)) {
                    throw new CommandException(strings.get("instance.is.running",
                            serverName));
                }
            }

            oldPassword = passwords.get("AS_ADMIN_MASTERPASSWORD");
            if (oldPassword == null) {
                char[] opArr = super.readPassword(strings.get("old.mp"));
                oldPassword = opArr != null ? new String(opArr) : null;
            }
            if (oldPassword == null) {
                throw new CommandException(strings.get("no.console"));
            }

            // for each instance iterate through the instances first,
            // read each keystore with the old password,
            // only then should it save the new master password.
            boolean valid = true;
            for(String instanceDir0: getInstanceDirs(nodeDirChild)) {
               valid &= verifyInstancePassword(new File(nodeDirChild,instanceDir0));
           }
           if (!valid) {
               throw new CommandException(strings.get("incorrect.old.mp"));
           }
            ParamModelData nmpo = new ParamModelData("AS_ADMIN_NEWMASTERPASSWORD",
                    String.class, false, null);
            nmpo.prompt = strings.get("new.mp");
            nmpo.promptAgain = strings.get("new.mp.again");
            nmpo.param._password = true;
            char[] npArr = super.getPassword(nmpo, null, true);
            newPassword = npArr != null ? new String(npArr) : null;

            // for each instance encrypt the keystore
            for(String instanceDir2: getInstanceDirs(nodeDirChild)) {
               encryptKeystore(instanceDir2);
           }
            if (savemp) {
                createMasterPasswordFile();
            }
            return 0;
        } catch(Exception e) {
            throw new CommandException(e.getMessage(),e);
        }
    }

    /**
     * This will load and verify the keystore for each of the instances
     * in a node
     * @param instanceDir0 The instance directory
     * @return  if the password is valid for the instance keystore
     */
    private boolean verifyInstancePassword(File instanceDir) {

        File mp = new File(new File(instanceDir, "config"), TRUSTSTORE_FILENAME_DEFAULT);
        return loadAndVerifyKeystore(mp,oldPassword);
    }



    @Override
    public int execute(String... argv) throws CommandException {
        // We iterate through all the instances and so it should relax this requirement
        // that there is only 1 instance in a node .
        checkOneAndOnly = false;
        return super.execute(argv);
    }

    /**
     * Create the master password keystore. This routine can also modify the master password
     * if the keystore already exists
     *
     * @throws CommandException
     */
    protected void createMasterPasswordFile() throws CommandException {
        final File pwdFile = new File(this.getServerDirs().getAgentDir(), MASTER_PASSWORD_FILENAME);
        try {
            PasswordAdapter p = new PasswordAdapter(pwdFile.getAbsolutePath(),
                MASTER_PASSWORD_PASSWORD.toCharArray());
            p.setPasswordForAlias(MASTER_PASSWORD_ALIAS, newPassword.getBytes());
            pwdFile.setReadable(true);
            pwdFile.setWritable(true);
        } catch (Exception ex) {
            throw new CommandException(strings.get("masterPasswordFileNotCreated", pwdFile), ex);
        }
    }


    /**
     * This will encrypt the keystore
     */
    public void encryptKeystore(String f) throws CommandException {

        RepositoryConfig nodeConfig = new RepositoryConfig(f,
                new File(nodeDir, node).toString(), f);
        NodeKeystoreManager km = new NodeKeystoreManager();
        try {
            km.encryptKeystore(nodeConfig,oldPassword,newPassword);

        } catch (Exception e) {
             throw new CommandException(strings.get("Keystore.not.encrypted"),
                e);
        }

    }

    /**
     * This will get all the instances for a given node
     * @param parent  node
     * @return   The list of instances for a node
     * @throws CommandException
     */
    private ArrayList<String> getInstanceDirs(File parent) throws CommandException {

        ArrayList<String> instancesList = new ArrayList<>();
        File[] files = parent.listFiles(File::isDirectory);
        if (files == null || files.length == 0) {
            throw new CommandException(strings.get("Instance.noInstanceDirs", parent));
        }

        for (File f : files) {
            if (!f.getName().equals("agent")) {
                instancesList.add(f.getName());
            }
        }
        return instancesList;

    }


    private boolean isRunning(File nodeDirChild, String serverName) throws CommandException {
        File serverDir = new File(nodeDirChild, serverName);
        File configDir = new File(serverDir, "config");
        File domainXml = new File(configDir, "domain.xml");
        try {
            if (!domainXml.exists()) {
                return false;
            }
            MiniXmlParser parser = new MiniXmlParser(domainXml, serverName);
            List<HostAndPort> addrSet = parser.getAdminAddresses();
            if (addrSet.isEmpty()) {
                throw new CommandException("Cannot find admin port in domain.xml file");
            }
            HostAndPort addr = addrSet.get(0);
            return ProcessUtils.isListening(addr);
        } catch (MiniXmlParserException e) {
            throw new CommandException("Invalid XML: " + domainXml, e);
        }
    }

}
