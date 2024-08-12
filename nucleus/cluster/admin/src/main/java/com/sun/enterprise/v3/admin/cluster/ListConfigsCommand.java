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

package com.sun.enterprise.v3.admin.cluster;



import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.StringUtils;

import jakarta.inject.Inject;

import java.util.LinkedList;
import java.util.List;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.config.ReferenceContainer;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 *  This is a remote command that lists the configs.
 * Usage: list-config

 * @author Bhakti Mehta
 */
@Service(name = "list-configs")
@I18n("list.configs.command")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn({RuntimeType.DAS})
@TargetType(value={CommandTarget.CLUSTER,
    CommandTarget.CONFIG, CommandTarget.DAS, CommandTarget.DOMAIN, CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTERED_INSTANCE})
@RestEndpoints({
    @RestEndpoint(configBean=Configs.class,
        opType=RestEndpoint.OpType.GET,
        path="list-configs",
        description="list-configs")
})
public final class ListConfigsCommand implements AdminCommand {

    @Inject
    private Domain domain;

    @Param(optional = true, primary = true, defaultValue = "domain")
    private String target;

    @Inject
    private Configs allConfigs;

    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        List<Config> configList = null;
        //Fix for issue 13356 list-configs doesn't take an operand
        //defaults to domain
        if (target.equals("domain" )) {
            Configs configs = domain.getConfigs();
            configList = configs.getConfig();
        } else {
            configList = createConfigList();

            if (configList == null) {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(Strings.get("list.instances.badTarget", target));
                return;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Config config : configList) {
            sb.append(config.getName()).append('\n');
        }
        String output = sb.toString();
        //Fix for isue 12885
        report.addSubActionsReport().setMessage(output.substring(0,output.length()-1 ));
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    /*
    * if target was junk then return all the configs
    */
    private List<Config> createConfigList() {
        // 1. no target specified
        if (!StringUtils.ok(target))
            return allConfigs.getConfig();

        Config c = domain.getConfigNamed(target);
        if (c != null) {
            List<Config> cl = new LinkedList<Config>();
            cl.add(c);
            return cl;
        }

        ReferenceContainer rc = domain.getReferenceContainerNamed(target);
        if (rc == null) return null;

        if (rc.isServer()) {
            Server s =((Server) rc);
            List<Config> cl = new LinkedList<Config>();
            cl.add(s.getConfig());
            return  cl;
        }
        else if (rc.isCluster()) {
            Cluster cluster = (Cluster) rc;
            List<Config> cl = new LinkedList<Config>();
            cl.add(domain.getConfigNamed(cluster.getConfigRef()));
            return cl;
        }
        else return null;
    }

}
