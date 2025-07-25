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
import com.sun.enterprise.admin.servermgmt.services.AppserverServiceType;
import com.sun.enterprise.admin.servermgmt.services.PlatformServicesInfo;
import com.sun.enterprise.admin.servermgmt.services.Service;
import com.sun.enterprise.admin.servermgmt.services.ServiceFactory;
import com.sun.enterprise.admin.util.ServerDirsSelector;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.io.ServerDirs;

import java.io.File;
import java.util.logging.Level;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;

/**
 * Create a "service" in the operating system to start this domain automatically.
 */
@org.jvnet.hk2.annotations.Service(name = "create-service")
@PerLookup
public final class CreateServiceCommand extends CLICommand {
    @Param(name = "name", optional = true)
    private String serviceName;
    @Param(name = "serviceproperties", optional = true)
    private String serviceProperties;
    @Param(name = "dry-run", shortName = "n", optional = true, defaultValue = "false")
    private boolean dry_run;
    @Param(name = "force", optional = true, defaultValue = "false")
    private boolean force;
    @Param(name = "domaindir", optional = true)
    private File userSpecifiedDomainDirParent;
    @Param(name = "serviceuser", optional = true)
    private String serviceUser;

    /*
     * The following parameters allow an unattended start-up any number of
     * ways to tell where the domain.xml file is that should be read for
     * client/instance-side security confir.
     */
    @Param(name = "domain_or_instance_name", primary = true, optional = true, alias = "domain_name")
    private String userSpecifiedServerName;
    @Param(name = "nodedir", optional = true, alias = "agentdir")
    private String userSpecifiedNodeDir; // nodeDirRoot
    @Param(name = "node", optional = true, alias = "nodeagent")
    private String userSpecifiedNode;
    private File asadminScript;
    private static final LocalStringsImpl strings = new LocalStringsImpl(CreateServiceCommand.class);
    private ServerDirs dirs;
    private ServerDirsSelector selector = null;

    /**
     */
    @Override
    protected void validate() throws CommandException {
        try {
            super.validate(); // pointless empty method but who knows what the future holds?

            if (ok(serviceUser) && !OS.isLinux()) {
                // serviceUser is only supported on Linux
                throw new CommandException(strings.get("serviceUser_wrong_os"));
            }
            // The order that you make these calls matters!!

            selector = ServerDirsSelector.getInstance(userSpecifiedDomainDirParent, userSpecifiedServerName, userSpecifiedNodeDir,
                    userSpecifiedNode);
            dirs = selector.dirs();

            validateServiceName();
            validateAsadmin();
        } catch (CommandException e) {
            throw e;
        } catch (Exception e) {
            // plenty of RuntimeException possibilities!
            throw new CommandException(e.getMessage(), e);
        }
    }

    @Override
    protected int executeCommand() throws CommandException {
        try {
            final Service service = ServiceFactory.getService(dirs, getType());
            PlatformServicesInfo info = service.getInfo();
            info.setTrace(logger.isLoggable(Level.FINER));
            info.setDryRun(dry_run);
            info.setForce(force);
            info.setAppServerUser(getProgramOptions().getUser());
            if (ok(serviceName))
                info.setServiceName(serviceName);

            if (ok(serviceUser))
                info.setServiceUser(serviceUser);

            if (programOpts.getPasswordFile() != null)
                info.setPasswordFile(SmartFile.sanitize(new File(programOpts.getPasswordFile())));

            service.setServiceProperties(serviceProperties);
            service.createService();

            // Why the messiness?  We don't want to talk about the help
            // file inside the help file thus the complications below...
            String help = service.getSuccessMessage();
            String tellUserAboutHelp = strings.get("create.service.runtimeHelp", help,
                    new File(dirs.getServerDir(), "PlatformServices.log"));
            logger.info(tellUserAboutHelp);
            service.writeReadmeFile(help);

        } catch (Exception e) {
            // We only want to wrap the string -- not the Exception.
            // Otherwise the message that is printed out to the user will be like this:
            // java.lang.IllegalArgumentException: The passwordfile blah blah blah
            // What we want is:
            // The passwordfile blah blah blah
            // IT 8882

            String msg = e.getMessage();

            if (ok(msg))
                throw new CommandException(msg);
            else
                throw new CommandException(e);
        }
        return 0;
    }

    private void validateServiceName() throws CommandException {
        if (!ok(serviceName))
            serviceName = dirs.getServerDir().getName();

        // On Windows we need a legal filename for the service name.
        if (OS.isWindowsForSure() && !FileUtils.isFriendlyFilename(serviceName)) {
            throw new CommandException(strings.get("create.service.badServiceName", serviceName));
        }

        if (logger.isLoggable(Level.FINER))
            logger.finer("service name = " + serviceName);
    }

    private void validateAsadmin() throws CommandException {
        String s = SystemPropertyConstants.getAsAdminScriptLocation();

        if (!ok(s))
            throw new CommandException(strings.get("internal.error", "Can't get Asadmin script location"));

        asadminScript = SmartFile.sanitize(new File(s));

        if (!asadminScript.isFile()) {
            throw new CommandException(strings.get("create.service.noAsadminScript", asadminScript));
        }
    }

    private AppserverServiceType getType() {
        if (selector.isInstance())
            return AppserverServiceType.Instance;
        else
            return AppserverServiceType.Domain;
    }
}
