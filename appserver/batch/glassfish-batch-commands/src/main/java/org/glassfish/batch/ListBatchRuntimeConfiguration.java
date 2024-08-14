/*
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

package org.glassfish.batch;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.ColumnFormatter;

import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.batch.spi.impl.BatchRuntimeConfiguration;
import org.glassfish.batch.spi.impl.BatchRuntimeHelper;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;

/**
 * Command to list batch jobs info
 *
 * @author Mahesh Kannan
 *
 */
@Service(name="list-batch-runtime-configuration")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.batch.runtime.configuration")
@ExecuteOn(value = {RuntimeType.DAS})
@TargetType(value = {CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@RestEndpoints({
        @RestEndpoint(configBean = Domain.class,
                opType = RestEndpoint.OpType.GET,
                path = "list-batch-runtime-configuration",
                description = "List Batch Runtime Configuration")
})
public class ListBatchRuntimeConfiguration
    extends AbstractListCommand {

    private static final String DATA_SOURCE_NAME = "dataSourceLookupName";

    private static final String EXECUTOR_SERVICE_NAME = "executorServiceLookupName";

    @Inject
    protected Target targetUtil;

    @Inject
    BatchRuntimeHelper helper;

    @Override
    protected void executeCommand(AdminCommandContext context, Properties extraProps) {

        Config config = targetUtil.getConfig(target);
        if (config == null) {
            context.getActionReport().setMessage("No such config named: " + target);
            context.getActionReport().setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        BatchRuntimeConfiguration batchRuntimeConfiguration = config.getExtensionByType(BatchRuntimeConfiguration.class);

        Map<String, Object> map = new HashMap<>();

        map.put(DATA_SOURCE_NAME, batchRuntimeConfiguration.getDataSourceLookupName());
        map.put(EXECUTOR_SERVICE_NAME, batchRuntimeConfiguration.getExecutorServiceLookupName());
        extraProps.put("listBatchRuntimeConfiguration", map);

        ColumnFormatter columnFormatter = new ColumnFormatter(getDisplayHeaders());
        Object[] data = new Object[getOutputHeaders().length];
        for (int index=0; index<getOutputHeaders().length; index++) {
            switch (getOutputHeaders()[index]) {
                case DATA_SOURCE_NAME:
                    String val = batchRuntimeConfiguration.getDataSourceLookupName();
                    data[index] = (val == null || val.trim().length() == 0)
                        ? BatchRuntimeHelper.getDefaultDataSourceLookupNameForTarget(target) : val;
                    break;
                case EXECUTOR_SERVICE_NAME:
                    data[index] = batchRuntimeConfiguration.getExecutorServiceLookupName();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown header: " + getOutputHeaders()[index]);
            }
        }
        columnFormatter.addRow(data);
        context.getActionReport().setMessage(columnFormatter.toString());
    }


    @Override
    protected final String[] getAllHeaders() {
        return new String[] {
                DATA_SOURCE_NAME, EXECUTOR_SERVICE_NAME
        };
    }

    @Override
    protected final String[] getDefaultHeaders() {
        return getAllHeaders();
    }
}
