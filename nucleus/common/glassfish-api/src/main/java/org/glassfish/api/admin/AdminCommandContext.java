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

package org.glassfish.api.admin;


import java.io.Serializable;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ExecutionContext;

/**
 * Useful services for administrative commands implementation
 *
 * @author Jerome Dochez
 */
public interface AdminCommandContext extends ExecutionContext, Serializable {
    
    /**
     * Returns the Reporter for this action
     * @return ActionReport implementation suitable for the client
     */
    public ActionReport getActionReport();
    /**
     * Change the Reporter for this action
     * @param newReport The ActionReport to set.
     */
    public void setActionReport(ActionReport newReport);

    /**
     * Returns the Logger
     * @return the logger
     */
    public Logger getLogger();

    /**
     * Returns the inbound payload, from the admin client, that accompanied
     * the command request.
     *
     * @return the inbound payload
     */
    public Payload.Inbound getInboundPayload();

    /**
     * Changes the inbound payload for this action.
     *
     * @param newInboundPayload inbound payload to set.
     */
    public void setInboundPayload(Payload.Inbound newInboundPayload);

    /**
     * Returns a reference to the outbound payload so a command implementation
     * can populate the payload for return to the admin client.
     *
     * @return the outbound payload
     */
    public Payload.Outbound getOutboundPayload();

    /**
     * Changes the outbound payload for this action.
     *
     * @param newOutboundPayload outbound payload to set.
     */
    public void setOutboundPayload(Payload.Outbound newOutboundPayload);

    /**
     * Returns the Subject associated with this command context.
     *
     * @return the Subject
     */
    public Subject getSubject();

    /**
     * Sets the Subject to be associated with this command context.
     *
     * @param subject
     */
    public void setSubject(Subject subject);
    
    /** 
     * ProgressStatus can be used to inform about step by step progress 
     * of the command. It is always ready to use but propagated to 
     * the client only if {@code @Progress} annotation is on the command
     * implementation.
     */
    public ProgressStatus getProgressStatus();
    
    /** Simple event broker for inter command communication mostly
     * from server to client. (Command to caller).
     */
    public AdminCommandEventBroker getEventBroker();
    
    
    /** Id of current job. Only managed commands has job id.
     */
    public String getJobId();

}
