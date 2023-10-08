/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.ha.commands;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.ha.store.spi.BackingStoreFactoryRegistry;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * The list-persistence-types command lists different kinds of persistence options for session data
 * when high availability is enabled for an application deployed to a cluster.
 */
@Service(name="list-persistence-types")
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.persistence.types.command")
@PerLookup
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET,
        path="list-persistence-types",
        description="list-persistence-types")
})
public class ListPersistenceTypesCommand implements AdminCommand {

    private static final String CONTAINER_TYPES = "(ejb|web)";

    @Param(name = "type", optional = false, primary = false)
    @I18n("list.persistence.types.container")
    @Pattern(regexp = CONTAINER_TYPES, message = "Valid values: " + CONTAINER_TYPES)
    private String containerType = "";

    private Logger logger;
    private static final String EOL = "\n";
    private static final String SEPARATOR=EOL;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        logger = context.getLogger();
        if (!checkEnvAndParams(report)) {
            return;
        }
        if (logger.isLoggable(Level.FINE)){
            logger.log(Level.FINE, Strings.get("list.persistence.types.called", containerType));
        }

        Set<String> allPersistenceTypes = BackingStoreFactoryRegistry.getRegisteredTypes();
        allPersistenceTypes.remove("noop"); // implementation detail.  do not expose to users.
                                            // "noop" is functionally equivalent to "memory".
        if (containerType.equals("ejb") ) {
            allPersistenceTypes.remove("memory");  // ejb did not have "memory" in glassfish v2.x.
        }

        StringBuilder sb = new StringBuilder("");
        boolean removeTrailingSeparator = false;
        for (String type : allPersistenceTypes) {
            sb.append(type).append(SEPARATOR);
            removeTrailingSeparator = true;
        }
        String output = sb.toString();
        if (removeTrailingSeparator) {
            output = output.substring(0, output.length()-1);
        }
        Properties extraProperties = new Properties();
        extraProperties.put("types", new ArrayList<>(allPersistenceTypes));

        report.setExtraProperties(extraProperties);
        report.setMessage(output);
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    // return false for any failures
    private boolean checkEnvAndParams(ActionReport report) {
        if (containerType == null) {
            return fail(report, Strings.get("list.persistence.types.null.parameter"));
        }
        if (!containerType.equals("ejb") && !containerType.equals("web")) {
            return fail(report, Strings.get("list.persistence.types.invalid.parameter", containerType));
        }

        // ok to go
        return true;
    }

    private boolean fail(ActionReport report, String s) {
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        report.setMessage(s);
        return false;
    }
}
