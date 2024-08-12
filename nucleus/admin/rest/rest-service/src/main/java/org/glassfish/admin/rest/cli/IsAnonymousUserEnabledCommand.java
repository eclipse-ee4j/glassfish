/*
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

package org.glassfish.admin.rest.cli;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author jasonlee
 */
@Service(name = "__anonymous-user-enabled")
@PerLookup
@RestEndpoints({ @RestEndpoint(configBean = Domain.class, path = "anonymous-user-enabled") })
public class IsAnonymousUserEnabledCommand implements AdminCommand {
    @Inject
    Domain domain;

    @Inject
    ServiceLocator habitat;

    @Override
    public void execute(AdminCommandContext context) {
        SecurityUtil su = new SecurityUtil(domain);

        String userName = su.getAnonymousUser(habitat);
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ExitCode.SUCCESS);

        Properties ep = new Properties();
        ep.put("anonymousUserEnabled", userName != null);
        report.setExtraProperties(ep);

        if (userName == null) {
            report.setMessage("The anonymous user is disabled.");
        } else {
            ep.put("anonymousUserName", userName);
            report.setMessage("The anonymous user is enabled: " + userName);
        }
    }
}
