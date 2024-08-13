/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.servermgmt;

import com.sun.enterprise.config.serverbeans.JavaConfig;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

@Service(name = "test-upgrade", metadata = "mode=debug")
@PerLookup
public class V2ToV3ConfigUpgradeTester implements AdminCommand, AdminCommandSecurity.AccessCheckProvider {
    @Inject
    ServiceLocator habitat;

    @Inject
    V2ToV3ConfigUpgrade up;

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final Collection<AccessCheck> accessChecks = new ArrayList<AccessCheck>();
        for (JavaConfig jc : up.getJavaConfigs()) {
            accessChecks.add(new AccessCheck(jc, "update", true /* isFailureFatal */));
        }
        return accessChecks;
    }

    @Override
    public void execute(AdminCommandContext context) {
        up.postConstruct();
        String msg = "Testing upgrade!";
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ExitCode.SUCCESS);
        report.setMessage(msg);
    }
}
