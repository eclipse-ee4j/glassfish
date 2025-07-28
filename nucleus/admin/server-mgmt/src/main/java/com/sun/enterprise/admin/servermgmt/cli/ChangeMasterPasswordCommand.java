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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.enterprise.admin.cli.CLICommand;
import com.sun.enterprise.util.io.DomainDirs;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * The change-master-password command. This is a command which can operate on both the DAS and the node The master
 * password is the password that is used to encrypt the DAS (and instances) keystore. Therefore the DAS and associated
 * server instances need the password to open the keystore at startup. The master password is the same for the DAS and
 * all instances in the domain The default master password is "changeit"and can be saved in a master-password file:
 *
 * 1. DAS: domains/domainname/master-password 2. Instance: nodes/node-name/master-password The master-password may be
 * changed on the DAS by running change-master-password. The DAS must be down to run this command.
 * change-master-password supports the --savemasterpassword option. To change the master-password file on a node you run
 * change-master-password with --nodedir and the node name. The instances must be down to run this command on a node
 *
 * If --nodedir is not specified it will look in the default location of nodes folder and find the node
 *
 * If the domain and node have the same name it will execute the command for the domain. Incase you want the command to
 * be executed for a node when the domain and node name is same you will need to specify the --nodedir option
 *
 * @author Bhakti Mehta
 */
@Service(name = "change-master-password")
@PerLookup
public class ChangeMasterPasswordCommand extends CLICommand {

    @Inject
    private ServiceLocator habitat;

    @Param(name = "savemasterpassword", optional = true, defaultValue = "false")
    private boolean savemp;

    @Param(name = "domain_name_or_node_name", primary = true, optional = true)
    private String domainNameOrNodeName;

    @Param(name = "nodedir", optional = true)
    protected String nodeDir;

    @Param(name = "domaindir", optional = true)
    protected String domainDirParam = null;

    private final String CHANGE_MASTER_PASSWORD_DAS = "_change-master-password-das";

    private final String CHANGE_MASTER_PASSWORD_NODE = "_change-master-password-node";

    private static final LocalStringsImpl strings = new LocalStringsImpl(ChangeMasterPasswordCommand.class);

    @Override
    protected int executeCommand() throws CommandException {
        CLICommand command = null;

        if (domainDirParam != null && nodeDir != null) {
            throw new CommandException(strings.get("both.domaindir.nodedir.not.allowed"));
        }
        try {
            if (isDomain()) { // is it domain
                command = CLICommand.getCommand(habitat, CHANGE_MASTER_PASSWORD_DAS);
                return command.execute(argv);
            }

            if (nodeDir != null) {
                command = CLICommand.getCommand(habitat, CHANGE_MASTER_PASSWORD_NODE);
                return command.execute(argv);
            } else {

                // nodeDir is not specified and domainNameOrNodeName is not a domain.
                // It could be a node
                // We add defaultNodeDir parameter to args
                ArrayList arguments = new ArrayList<String>(Arrays.asList(argv));
                arguments.remove(argv.length - 1);
                arguments.add("--nodedir");
                arguments.add(getDefaultNodesDirs().getAbsolutePath());
                arguments.add(domainNameOrNodeName);
                String[] newargs = (String[]) arguments.toArray(new String[arguments.size()]);

                command = CLICommand.getCommand(habitat, CHANGE_MASTER_PASSWORD_NODE);
                return command.execute(newargs);
            }
        } catch (IOException e) {
            throw new CommandException(e.getMessage(), e);
        }
    }

    @Override
    public int execute(String... args) throws CommandException {

        //This will parse the args and call executeCommand
        super.execute(args);
        return 0;

    }

    private boolean isDomain() throws IOException {
        DomainDirs domainDirs = null;
        //if both domainDir and domainNameOrNodeName are null get default domaindir
        if (domainDirParam == null && domainNameOrNodeName == null) {
            domainDirs = new DomainDirs(DomainDirs.getDefaultDomainsDir());
        } else {
            if (domainDirParam != null) {
                domainDirs = new DomainDirs(new File(domainDirParam), domainNameOrNodeName);
                return domainDirs.isValid();
            }
            if (domainNameOrNodeName != null) {
                return new File(DomainDirs.getDefaultDomainsDir(), domainNameOrNodeName).isDirectory();
            }
        }
        //It can be null in the case when this is not a domain but a node
        if (domainDirs != null) {
            return domainDirs.getDomainsDir().isDirectory();
        }
        return false;

    }

    private File getDefaultNodesDirs() throws IOException {
        return new File(DomainDirs.getDefaultDomainsDir().getParent(), "nodes");
    }

}
