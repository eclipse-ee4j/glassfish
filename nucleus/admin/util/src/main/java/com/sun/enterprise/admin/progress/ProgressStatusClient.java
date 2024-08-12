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

package com.sun.enterprise.admin.progress;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.api.admin.CommandProgress;
import org.glassfish.api.admin.ProgressStatus;
import org.glassfish.api.admin.progress.ProgressStatusBase;
import org.glassfish.api.admin.progress.ProgressStatusDTO;
import org.glassfish.api.admin.progress.ProgressStatusEvent;
import org.glassfish.api.admin.progress.ProgressStatusEventCreateChild;

/**
 * Provides mirroring of events into given ProgressStatus substructure. Never rewrites name in base ProgressStatus (i.e.
 * only children will have copied names).
 *
 * @author mmares
 */
public class ProgressStatusClient {

    private static final LocalStringsImpl strings = new LocalStringsImpl(ProgressStatusClient.class);

    private ProgressStatus status;
    private final Map<String, ProgressStatus> map = new HashMap<String, ProgressStatus>();

    /**
     * Mirror incoming events and structures into given ProgressStatus. If null, CommandProgess will be created with first
     * event or structure.
     *
     * @param status
     */
    public ProgressStatusClient(ProgressStatus status) {
        this.status = status;
    }

    private synchronized void preventNullStatus(String name, String id) {
        if (status == null) {
            status = new CommandProgressImpl(name, id);
        }
        map.put(id, status);
    }

    public synchronized void mirror(ProgressStatusDTO dto) {
        if (dto == null) {
            return;
        }
        preventNullStatus(dto.getName(), dto.getId());
        mirror(dto, status);
    }

    private void mirror(ProgressStatusDTO dto, ProgressStatus stat) {
        //TODO: copy-paste problem because of ProgressStatusDTO and ProgressStatusBase we have to create shared interface
        stat.setTotalStepCount(dto.getTotalStepCount());
        stat.setCurrentStepCount(dto.getCurrentStepCount());
        if (dto.isCompleted()) {
            stat.complete();
        }
        for (ProgressStatusDTO.ChildProgressStatusDTO chld : dto.getChildren()) {
            ProgressStatus dst = map.get(chld.getProgressStatus().getId());
            if (dst == null) {
                dst = stat.createChild(chld.getProgressStatus().getName(), chld.getAllocatedSteps());
                map.put(chld.getProgressStatus().getId(), dst);
            }
            mirror(chld.getProgressStatus(), dst);
        }
    }

    public synchronized void mirror(ProgressStatusBase source) {
        if (source == null) {
            return;
        }
        preventNullStatus(source.getName(), source.getId());
        mirror(source, status);
    }

    private void mirror(ProgressStatusBase source, ProgressStatus stat) {
        stat.setTotalStepCount(source.getTotalStepCount());
        stat.setCurrentStepCount(source.getCurrentStepCount());
        if (source.isComplete()) {
            stat.complete();
        }
        for (ProgressStatusBase.ChildProgressStatus chld : source.getChildProgressStatuses()) {
            ProgressStatus dst = map.get(chld.getProgressStatus().getId());
            if (dst == null) {
                dst = stat.createChild(chld.getProgressStatus().getName(), chld.getAllocatedSteps());
                map.put(chld.getProgressStatus().getId(), dst);
            }
            mirror(chld.getProgressStatus(), dst);
        }
    }

    /**
     * Applies event on existing structures. If not appliable do nothing.
     */
    public synchronized void mirror(ProgressStatusEvent event) {
        if (event == null) {
            return;
        }
        ProgressStatus effected = map.get(event.getSourceId());
        ProgressStatus result = event.apply(effected);
        if (event instanceof ProgressStatusEventCreateChild) {
            map.put(((ProgressStatusEventCreateChild) event).getChildId(), result);
        }
    }

    public synchronized ProgressStatus getProgressStatus() {
        return status;
    }

    public static String composeMessageForPrint(CommandProgress cp) {
        if (cp == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        synchronized (cp) {
            //Measurements
            int percentage = Math.round(cp.computeCompletePortion() * 100);
            if (percentage >= 0) {
                result.append(percentage);
                switch (result.length()) {
                case 1:
                    result.insert(0, "  ");
                    break;
                case 2:
                    result.insert(0, ' ');
                    break;
                default:
                    break;
                }
                result.append('%');
            } else {
                int sumSteps = cp.computeSumSteps();
                result.append(sumSteps);
            }
            //Message
            String message = cp.getLastMessage();
            if (!StringUtils.ok(message) && StringUtils.ok(cp.getName())) {
                message = strings.getString("progressstatus.message.starting", "Starting");
            }
            if (StringUtils.ok(message)) {
                result.append(": ");
                result.append(message);
            }
        }
        return result.toString();
    }

}
