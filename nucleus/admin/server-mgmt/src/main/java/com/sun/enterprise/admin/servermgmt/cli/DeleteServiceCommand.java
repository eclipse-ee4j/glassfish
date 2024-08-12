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
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.io.ServerDirs;

import java.io.File;
import java.util.logging.Level;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;

/**
 * Delete a "service" in the operating system.
 *
 * @author Byron Nevins
 * @since November 18, 2010
 */
@org.jvnet.hk2.annotations.Service(name = "_delete-service")
@PerLookup
public final class DeleteServiceCommand extends CLICommand {
    @Param(name = "name", optional = true)
    private String serviceName;
    @Param(name = "domaindir", optional = true)
    private File userSpecifiedDomainDirParent;

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
    private static final LocalStringsImpl strings = new LocalStringsImpl(DeleteServiceCommand.class);
    private ServerDirs dirs;
    private ServerDirsSelector selector = null;

    /**
     */
    @Override
    protected void validate() throws CommandException {
        try {
            super.validate(); // pointless empty method but who knows what the future holds?

            // The order that you make these calls matters!!

            selector = ServerDirsSelector.getInstance(userSpecifiedDomainDirParent, userSpecifiedServerName, userSpecifiedNodeDir,
                    userSpecifiedNode);
            dirs = selector.dirs();

            validateServiceName();
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

            if (ok(serviceName))
                info.setServiceName(serviceName);

            if (programOpts.getPasswordFile() != null)
                info.setPasswordFile(SmartFile.sanitize(new File(programOpts.getPasswordFile())));

            service.deleteService();
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

        logger.finer("service name = " + serviceName);
    }

    private AppserverServiceType getType() {
        if (selector.isInstance())
            return AppserverServiceType.Instance;
        else
            return AppserverServiceType.Domain;
    }
}
