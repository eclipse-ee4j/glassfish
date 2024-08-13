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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.admin.progress.CommandProgressImpl;
import com.sun.enterprise.admin.progress.ProgressStatusClient;

import java.util.UUID;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandContextForInstance;
import org.glassfish.api.admin.AdminCommandEventBroker;
import org.glassfish.api.admin.CommandProgress;
import org.glassfish.api.admin.Job;
import org.glassfish.api.admin.Progress;
import org.glassfish.api.admin.ProgressProvider;
import org.glassfish.api.admin.ProgressStatus;
import org.glassfish.api.admin.SupplementalCommandExecutor.SupplementalCommand;
import org.glassfish.api.admin.progress.ProgressStatusBase;
import org.glassfish.api.admin.progress.ProgressStatusEvent;
import org.glassfish.api.admin.progress.ProgressStatusMirroringImpl;
import org.glassfish.config.support.GenericCrudCommand;

/** Helper class for {@code ProgressStatus} manipulation during
 * {@code CommandRunner} execution.<br/><br/>
 * <b>Life cycle:</b><br/>
 * <ul>
 *   <li>Constructs</li>
 *   <li>setReplicationCount</li>
 *   <li>addProgressStatusToSupplementalCommand <i>(optional)</i></li>
 *   <li>wrapContext4MainCommand</li>
 *   <li><i>do replication</i></li>
 *   <li>complete</li>
 * </ul>
 *
 * @author mmares
 */
class CommandRunnerProgressHelper {

    //From constructor
    private Progress progressAnnotation;
    private CommandProgressImpl commandProgress;
    private int replicationCount = 0;

    //Changed during lifecycle
    private ProgressStatus progressForMainCommand = null;
    private ProgressStatusMirroringImpl progressMirroring = null;

    public CommandRunnerProgressHelper(AdminCommand command, String name, Job job, ProgressStatus clientProgressStatus) {
        if (command instanceof GenericCrudCommand) {
            GenericCrudCommand gcc = (GenericCrudCommand) command;
            Class decorator = gcc.getDecoratorClass();
            if (decorator != null) {
                progressAnnotation = (Progress) decorator.getAnnotation(Progress.class);
            }
        } else if (command instanceof ProgressProvider) {
            progressAnnotation = ((ProgressProvider) command).getProgress();
        } else {
            progressAnnotation = command.getClass().getAnnotation(Progress.class);
        }
        this.commandProgress = (CommandProgressImpl) job.getCommandProgress(); //Possible from checkpoint
        if (progressAnnotation != null) {
            if (commandProgress == null) {
                if (progressAnnotation.name() == null || progressAnnotation.name().isEmpty()) {
                    commandProgress = new CommandProgressImpl(name, createIdForCommandProgress(job));
                } else {
                    commandProgress = new CommandProgressImpl(progressAnnotation.name(), createIdForCommandProgress(job));
                }
            }
            connectWithClientProgressStatus(job, clientProgressStatus);
            job.setCommandProgress(commandProgress);
        }
    }

    private void connectWithClientProgressStatus(Job commandInstance, ProgressStatus clientProgressStatus) {
        if (clientProgressStatus == null) {
            return;
        }
        final ProgressStatusClient psc = new ProgressStatusClient(clientProgressStatus);
        commandInstance.getEventBroker().registerListener(CommandProgress.EVENT_PROGRESSSTATUS_STATE, new AdminCommandEventBroker.AdminCommandListener<ProgressStatusBase>() {
                    @Override
                    public void onAdminCommandEvent(String name, ProgressStatusBase event) {
                        psc.mirror(event);
                    }
                });
        commandInstance.getEventBroker().registerListener(CommandProgress.EVENT_PROGRESSSTATUS_CHANGE, new AdminCommandEventBroker.AdminCommandListener<ProgressStatusEvent>() {
                    @Override
                    public void onAdminCommandEvent(String name, ProgressStatusEvent event) {
                        psc.mirror(event);
                    }
                });
    }

    private String createIdForCommandProgress(Job commandInstance) {
        String cid = commandInstance == null ? null : commandInstance.getId();
        if (cid == null || cid.isEmpty()) {
            cid = UUID.randomUUID().toString();
        }
        return cid;
    }

    public int getReplicationCount() {
        return replicationCount;
    }

    public void setReplicationCount(int replicationCount) {
        this.replicationCount = replicationCount;
    }

    public void addProgressStatusToSupplementalCommand(SupplementalCommand supplemental) {
        if (commandProgress == null || supplemental == null) {
            return;
        }
        if (progressForMainCommand != null && progressMirroring == null) {
            throw new IllegalStateException("Suplmenetal commands must be filled with ProgressStatus before main command!");
        }
        if (replicationCount < 0) {
            throw new IllegalStateException("Replication count must be provided first");
        }
        if (supplemental.getProgressAnnotation() != null) {
            if (progressMirroring == null) {
                commandProgress.setTotalStepCount(replicationCount + 1);
                progressMirroring = commandProgress.createMirroringChild(1);
                progressForMainCommand = progressMirroring.createChild(null, 0, progressAnnotation.totalStepCount());
            }
            supplemental.setProgressStatus(progressMirroring.createChild(supplemental.getProgressAnnotation().name(),
                    0, supplemental.getProgressAnnotation().totalStepCount()));
        }
    }

    public AdminCommandContext wrapContext4MainCommand(AdminCommandContext context) {
        if (progressForMainCommand != null) {
            return new AdminCommandContextForInstance(context, progressForMainCommand);
        }

        if (commandProgress != null) {
            if (replicationCount > 0) {
                commandProgress.setTotalStepCount(replicationCount + 1);
                progressForMainCommand = commandProgress.createChild(null, 1, progressAnnotation.totalStepCount());
            } else {
                commandProgress.setTotalStepCount(progressAnnotation.totalStepCount());
                progressForMainCommand = commandProgress;
            }
            return new AdminCommandContextForInstance(context, progressForMainCommand);
        }

        return context;
    }

}
