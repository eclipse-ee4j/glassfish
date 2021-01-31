/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin;

import java.util.logging.Logger;
import javax.security.auth.Subject;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.Payload.Inbound;
import org.glassfish.api.admin.Payload.Outbound;

/**
 * Most of {@link AdminCommandContext} attributes are used in any phase of command execution (supplemental commands,
 * replication) but some of them must be different for every instance. This wrapper provides such staff.
 *
 * @author mmares
 */
public class AdminCommandContextForInstance implements AdminCommandContext {

    private AdminCommandContext wrapped;
    private ProgressStatus progressStatus;

    public AdminCommandContextForInstance(AdminCommandContext wrapped, ProgressStatus progressStatus) {
        if (wrapped == null) {
            throw new IllegalArgumentException("Argument wrapped can not be null");
        }
        this.wrapped = wrapped;
        this.progressStatus = progressStatus;
    }

    @Override
    public ActionReport getActionReport() {
        return wrapped.getActionReport();
    }

    @Override
    public void setActionReport(ActionReport newReport) {
        wrapped.setActionReport(newReport);
    }

    @Override
    public Logger getLogger() {
        return wrapped.getLogger();
    }

    @Override
    public Inbound getInboundPayload() {
        return wrapped.getInboundPayload();
    }

    @Override
    public void setInboundPayload(Inbound newInboundPayload) {
        wrapped.setInboundPayload(newInboundPayload);
    }

    @Override
    public Outbound getOutboundPayload() {
        return wrapped.getOutboundPayload();
    }

    @Override
    public void setOutboundPayload(Outbound newOutboundPayload) {
        wrapped.setOutboundPayload(newOutboundPayload);
    }

    @Override
    public Subject getSubject() {
        return wrapped.getSubject();
    }

    @Override
    public void setSubject(Subject subject) {
        wrapped.setSubject(subject);
    }

    @Override
    public ProgressStatus getProgressStatus() {
        if (progressStatus == null) {
            return wrapped.getProgressStatus();
        } else {
            return progressStatus;
        }
    }

    @Override
    public AdminCommandEventBroker getEventBroker() {
        return wrapped.getEventBroker();
    }

    @Override
    public String getJobId() {
        return wrapped.getJobId();
    }

}
