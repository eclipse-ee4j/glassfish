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

import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.batch.spi.impl.BatchRuntimeHelper;
import org.glassfish.batch.spi.impl.GlassFishBatchSecurityHelper;

/**
 * @author Mahesh Kannan
 *
 */
public abstract class AbstractListCommand
    implements AdminCommand {

    @Inject
    BatchRuntimeHelper helper;

    @Inject
    protected Logger logger;

    @Param(name = "terse", optional=true, defaultValue="false", shortName="t")
    public boolean isTerse = false;

    @Param(name = "output", shortName = "o", optional = true)
    protected String outputHeaderList;

    @Param(name = "header", shortName = "h", optional = true)
    protected boolean header;

    @Param(name = "target", optional = true, defaultValue = "server")
    protected String target;

    protected String[] outputHeaders;

    protected String[] displayHeaders;

    @Inject
    GlassFishBatchSecurityHelper glassFishBatchSecurityHelper;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport actionReport = context.getActionReport();
        Properties extraProperties = actionReport.getExtraProperties();
        if (extraProperties == null) {
            extraProperties = new Properties();
            actionReport.setExtraProperties(extraProperties);
        }

        try {
            calculateHeaders();
            helper.checkAndInitializeBatchRuntime();
            glassFishBatchSecurityHelper.markInvocationPrivilege(true);
            executeCommand(context, extraProperties);
            actionReport.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (Exception ex) {
            logger.log(Level.FINE, "Exception during command ", ex);
            actionReport.setMessage(ex.getMessage());
            actionReport.setActionExitCode(ActionReport.ExitCode.FAILURE);
        } finally {
            glassFishBatchSecurityHelper.markInvocationPrivilege(false);
        }
    }

    private void calculateHeaders() {
        String[] headers = getDefaultHeaders();
        if (outputHeaderList != null) {
            headers = outputHeaderList.split("[,]");
            if (headers.length == 0)
                headers = getDefaultHeaders();
        } else if (supportsLongFormat())
            headers = getAllHeaders();

        Map<String, String> validHeaders = new HashMap<>();
        for (String h : getAllHeaders())
            validHeaders.put(h.toLowerCase(Locale.US), h);
        for (int i=0; i<headers.length; i++) {
            String val = validHeaders.get(headers[i].toLowerCase(Locale.US));
            if (val == null)
                throw new IllegalArgumentException("Invalid header " + headers[i]);
            headers[i] = val;
        }

        outputHeaders = headers;
        displayHeaders = new String[outputHeaders.length];
        for (int index = 0; index < displayHeaders.length; index++)
            displayHeaders[index] = isHeaderRequired() ? outputHeaders[index].toUpperCase(Locale.US) : "";

    }

    protected static JobOperator getJobOperatorFromBatchRuntime() {
        try {
            return BatchRuntime.getJobOperator();
        } catch (java.util.ServiceConfigurationError error) {
            throw new IllegalStateException("Could not get JobOperator. "
                + " Check if the Batch DataSource is configured properly and Check if the Database is up and running", error);
        } catch (Throwable ex) {
            throw new IllegalStateException("Could not get JobOperator. ", ex);
        }
    }

    protected boolean isHeaderRequired() {
        return !isTerse || header;
    }

    protected boolean supportsLongFormat() {
        return true;
    }

    protected abstract void executeCommand(AdminCommandContext context, Properties extraProps)
                throws Exception;

    protected abstract String[] getAllHeaders();

    protected abstract String[] getDefaultHeaders();

    protected String[] getOutputHeaders() {
        return outputHeaders;
    }

    protected String[] getDisplayHeaders() {
        return displayHeaders;
    }

}
