/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.admin;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.deployment.xml.ConcurrencyTagNames;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.admin.cli.ResourceConstants;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;


/**
 * Create Managed Thread Factory Command
 */
@TargetType(value={CommandTarget.DAS, CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTERED_INSTANCE, CommandTarget.CONFIG,  })
@ExecuteOn(RuntimeType.ALL)
@Service(name="create-managed-thread-factory")
@PerLookup
@I18n("create.managed.thread.factory")
public class CreateManagedThreadFactory implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateManagedThreadFactory.class);

    @Param(name="jndi_name", primary=true)
    private String jndiName;

    @Param(optional=true, defaultValue="true")
    private Boolean enabled;

    @Param(name="contextinfoenabled", alias="contextInfoEnabled", defaultValue="true", optional=true)
    private Boolean contextinfoenabled;

    @Param(name="contextinfo", alias="contextInfo", defaultValue=ConcurrencyTagNames.CONTEXT_INFO_DEFAULT_VALUE, optional=true)
    private String contextinfo;

    @Param(name="threadpriority", alias="threadPriority", defaultValue=""+Thread.NORM_PRIORITY, optional=true)
    private Integer threadpriority;

    @Param(optional=true)
    private String description;

    @Param(name="property", optional=true, separator=':')
    private Properties properties;

    @Param(optional=true)
    private String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Inject
    private Domain domain;

    @Inject
    private ManagedThreadFactoryManager managedThreadFactoryMgr;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        ResourceAttributes attrList = new ResourceAttributes();
        attrList.set(ResourceConstants.JNDI_NAME, jndiName);
        attrList.set(ConcurrencyTagNames.CONTEXT_INFO_ENABLED, contextinfoenabled.toString());
        attrList.set(ConcurrencyTagNames.CONTEXT_INFO, contextinfo);
        attrList.set(ConcurrencyTagNames.THREAD_PRIORITY, threadpriority.toString());
        attrList.set(ServerTags.DESCRIPTION, description);
        attrList.set(ResourceConstants.ENABLED, enabled.toString());
        ResourceStatus rs;

        try {
            rs = managedThreadFactoryMgr.create(domain.getResources(), attrList, properties, target);
        } catch(Exception e) {
            report.setMessage(localStrings.getLocalString("create.managed.thread.factory.failed", "Managed thread factory {0} creation failed", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        if (rs.getMessage() != null){
             report.setMessage(rs.getMessage());
        }
        if (rs.getStatus() == ResourceStatus.FAILURE) {
            ec = ActionReport.ExitCode.FAILURE;
            if (rs.getException() != null) {
                report.setFailureCause(rs.getException());
            }
        }
        report.setActionExitCode(ec);
    }
}
