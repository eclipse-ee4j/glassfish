/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.cli;

import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * author - Nithya Subramanian
 *
 * Usage: list-supported-cipher-suites [--help] [--user admin_user] [--passwordfile file_name] [target_name(default server)]
 **/

@Service(name = "list-supported-cipher-suites")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.supported.cipher.suites")
@ExecuteOn({ RuntimeType.DAS })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTERED_INSTANCE })
@RestEndpoints({
    @RestEndpoint(configBean = SecurityService.class, opType = RestEndpoint.OpType.GET, path = "list-supported-cipher-suites", description = "List Supported Cipher Suites") })
@AccessRequired(resource = "domain/security-service", action = "read")
public class ListSupportedCipherSuites implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListSupportedCipherSuites.class);
    @Inject
    SSLUtils sslutils;

    /*@Param(name = "target", optional = true, defaultValue =
    SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;*/
    @Param(optional = true, primary = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        String[] cipherSuites = sslutils.getSupportedCipherSuites();

        for (String cipherSuite : cipherSuites) {
            if (!cipherSuite.contains("_KRB5_")) {
                ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(cipherSuite);
            }
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

    }
}
