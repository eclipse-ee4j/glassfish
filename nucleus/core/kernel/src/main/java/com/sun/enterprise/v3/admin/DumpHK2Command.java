/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.universal.collections.ManifestUtils;
import com.sun.enterprise.v3.common.PropsFileActionReporter;

import jakarta.inject.Inject;

/**
 * Dumps the currently configured HK2 modules and their contents.
 *
 * <p>
 * Useful for debugging classloader related issues.
 *
 * @author Kohsuke Kawaguchi
 */
@PerLookup
@Service(name = "_dump-hk2")
@RestEndpoints({
        @RestEndpoint(
            configBean = Domain.class,
            opType = RestEndpoint.OpType.POST,
            path = "_dump-hk2",
            description = "_dump-hk2") })
@AccessRequired(resource = "domain", action = "dump")
public class DumpHK2Command implements AdminCommand {

    @Inject
    ModulesRegistry modulesRegistry;

    @Override
    public void execute(AdminCommandContext context) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        modulesRegistry.dumpState(new PrintStream(baos));

        ActionReport report = context.getActionReport();
        report.setActionExitCode(ExitCode.SUCCESS);
        String msg = baos.toString();

        // the proper way to do this is to check the user-agent of the caller,
        // but I can't access that -- so I'll just check the type of the
        // ActionReport. If we are sending back to CLI then linefeeds will
        // cause problems. Manifest.write() is OK but Manifest.read() explodes!
        if (report instanceof PropsFileActionReporter) {
            msg = ManifestUtils.encode(msg);
        }
        report.setMessage(msg);
    }
}
