/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.progress;

import com.sun.enterprise.util.StringUtils;

import java.io.Serializable;
import java.util.Date;

import org.glassfish.api.admin.AdminCommandEventBroker;
import org.glassfish.api.admin.CommandProgress;
import org.glassfish.api.admin.progress.ProgressStatusBase;
import org.glassfish.api.admin.progress.ProgressStatusEvent;
import org.glassfish.api.admin.progress.ProgressStatusEventCreateChild;
import org.glassfish.api.admin.progress.ProgressStatusEventProgress;
import org.glassfish.api.admin.progress.ProgressStatusImpl;
import org.glassfish.api.admin.progress.ProgressStatusMessage;
import org.glassfish.api.admin.progress.ProgressStatusMirroringImpl;

/**
 * Basic and probably only implementation of {@code CommandProgress}.
 *
 * @author mmares
 */
public class CommandProgressImpl extends ProgressStatusImpl implements CommandProgress, Serializable {

    private static final long serialVersionUID = 1;

    public class LastChangedMessage implements ProgressStatusMessage, Serializable {

        private String sourceId;
        private String message;
        private String contextString;

        private LastChangedMessage(String sourceId, String message) {
            this.sourceId = sourceId;
            if (message != null && message.isEmpty()) {
                message = null;
            }
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String getSourceId() {
            return sourceId;
        }

        public String getContextString() {
            if (contextString == null) {
                StringBuilder result = new StringBuilder();
                ProgressStatusBase fnd = findById(sourceId);
                if (StringUtils.ok(fnd.getName())) {
                    result.append(fnd.getName());
                }
                ProgressStatusBase parent;
                while ((parent = fnd.getParrent()) != null) {
                    if (StringUtils.ok(parent.getName())) {
                        if (result.length() > 0) {
                            result.insert(0, '.');
                        }
                        result.insert(0, parent.getName());
                    }
                    fnd = parent;
                }
                contextString = result.toString();
            }
            return contextString;
        }

    }

    private LastChangedMessage lastMessage;
    private Date startTime;
    private Date endTime;
    //TODO: Set after resurrection
    private transient AdminCommandEventBroker eventBroker;
    private boolean spinner = false;

    public CommandProgressImpl(String name, String id) {
        super(name, -1, null, id);
        startTime = new Date();
    }

    @Override
    protected synchronized void fireEvent(ProgressStatusEvent event) {
        if (event == null) {
            return;
        }
        if (event instanceof ProgressStatusMessage) {
            ProgressStatusMessage msgEvent = (ProgressStatusMessage) event;
            if (StringUtils.ok(msgEvent.getMessage())) {
                lastMessage = new LastChangedMessage(msgEvent.getSourceId(), msgEvent.getMessage());
            }
        }
        if (event instanceof ProgressStatusEventProgress) {
            this.spinner = ((ProgressStatusEventProgress) event).isSpinner();
        }
        if (eventBroker != null) {
            eventBroker.fireEvent(EVENT_PROGRESSSTATUS_CHANGE, event);
        }
    }

    @Override
    public void setEventBroker(AdminCommandEventBroker eventBroker) {
        this.eventBroker = eventBroker;
        if (eventBroker != null) {
            // Pass an empty progress status to prevent duplicate children creation
            // on the client side. This status is only used to send a state event.
            eventBroker.fireEvent(EVENT_PROGRESSSTATUS_STATE, new CommandProgressImpl(name, id));
        }
    }

    @Override
    public synchronized ProgressStatusMirroringImpl createMirroringChild(int allocatedSteps) {
        allocateStapsForChildProcess(allocatedSteps);
        String childId = (id == null ? "" : id) + "." + (children.size() + 1);
        ProgressStatusMirroringImpl result = new ProgressStatusMirroringImpl(null, this, childId);
        children.add(new ChildProgressStatus(allocatedSteps, result));
        fireEvent(new ProgressStatusEventCreateChild(id, null, result.getId(), allocatedSteps, -1));
        return result;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getLastMessage() {
        if (lastMessage != null) {
            StringBuilder result = new StringBuilder();
            result.append(lastMessage.getContextString());
            if (result.length() > 0) {
                result.append(": ");
            }
            result.append(lastMessage.getMessage());
            return result.toString();
        } else {
            return null;
        }
    }

    @Override
    public void complete() {
        complete(null);
    }

    @Override
    public void complete(String message) {
        this.endTime = new Date();
        super.complete(message);
    }

    @Override
    public boolean isSpinnerActive() {
        return this.spinner;
    }

}
