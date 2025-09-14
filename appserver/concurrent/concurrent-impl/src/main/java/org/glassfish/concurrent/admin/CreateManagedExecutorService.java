/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.deployment.xml.ConcurrencyTagNames;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

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
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;


/**
 * Create Managed Executor Service Command
 *
 */
@TargetType(value={CommandTarget.DAS, CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTERED_INSTANCE, CommandTarget.CONFIG })
@ExecuteOn(RuntimeType.ALL)
@Service(name="create-managed-executor-service")
@PerLookup
@I18n("create.managed.executor.service")
public class CreateManagedExecutorService extends CreateManagedExecutorServiceBase implements AdminCommand {
    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(CreateManagedExecutorServiceBase.class);


    @Param(name="maximumpoolsize", alias="maximumPoolSize", defaultValue=""+Integer.MAX_VALUE, optional=true)
    private Integer maximumpoolsize;

    @Param(name="taskqueuecapacity", alias="taskQueueCapacity", defaultValue=""+Integer.MAX_VALUE, optional=true)
    private Integer taskqueuecapacity;

    @Inject
    private Domain domain;

    @Inject
    private ManagedExecutorServiceManager managedExecutorServiceMgr;

    @Override
    protected void setAttributeList(ResourceAttributes attrList) {
        super.setAttributeList(attrList);
        attrList.set(ConcurrencyTagNames.MAXIMUM_POOL_SIZE, maximumpoolsize.toString());
        attrList.set(ConcurrencyTagNames.TASK_QUEUE_CAPACITY, taskqueuecapacity.toString());
    }

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
        setAttributeList(attrList);

        ResourceStatus rs;

        try {
            rs = managedExecutorServiceMgr.create(domain.getResources(), attrList, properties, target);
        } catch(Exception e) {
            report.setMessage(I18N.getLocalString("create.managed.executor.service.failed",
                "Managed executor service {0} creation failed", jndiName));
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
