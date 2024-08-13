/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.util;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.InstanceCommand;
import org.glassfish.api.admin.InstanceCommandResult;
import org.glassfish.api.admin.InstanceState;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 * Service that is called at startup and parses the instance state file.
 *
 * @author Vijay Ramachandran
 */
@Service
@RunLevel(value = StartupRunLevel.VAL, mode = RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
public class InstanceStateServiceImpl implements InstanceStateService {

    @Inject
    private ServerEnvironment serverEnv;

    @Inject
    private Domain domain;

    @Inject
    private CommandThreadPool cmdPool;

    private InstanceStateFileProcessor stateProcessor;
    private HashMap<String, InstanceState> instanceStates;
    private final static int MAX_RECORDED_FAILED_COMMANDS = 10;
    private final static Logger logger = AdminLoggerInfo.getLogger();

    public InstanceStateServiceImpl() {
    }

    /*
     * Perform lazy-initialization for the object, since this InstanceStateService
     * is not needed if there are not any instances.
     */
    private void init() {
        if (instanceStates != null) {
            return;
        }
        instanceStates = new HashMap<String, InstanceState>();
        File stateFile = new File(serverEnv.getConfigDirPath().getAbsolutePath(), ".instancestate");
        try {
            stateProcessor = new InstanceStateFileProcessor(instanceStates, stateFile);
        } catch (IOException ioe) {
            logger.log(Level.FINE, AdminLoggerInfo.mISScannotread, stateFile);
            instanceStates = new HashMap<String, InstanceState>();
            // Even though instances may already exist, do not populate the
            // instancesStates array because it will be repopulated as it is
            // used. Populating it early causes problems during instance
            // creation.
            try {
                stateProcessor = InstanceStateFileProcessor.createNew(instanceStates, stateFile);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, AdminLoggerInfo.mISScannotcreate, new Object[] { stateFile, ex.getLocalizedMessage() });
                stateProcessor = null;
            }
        }
    }

    @Override
    public synchronized void addServerToStateService(String instanceName) {
        init();
        instanceStates.put(instanceName, new InstanceState(InstanceState.StateType.NEVER_STARTED));
        try {
            stateProcessor.addNewServer(instanceName);
        } catch (Exception e) {
            logger.log(Level.SEVERE, AdminLoggerInfo.mISSaddstateerror, e.getLocalizedMessage());
        }
    }

    @Override
    public synchronized void addFailedCommandToInstance(String instance, String cmd, ParameterMap params) {
        init();
        String cmdDetails = cmd;
        String defArg = params.getOne("DEFAULT");
        if (defArg != null) {
            cmdDetails += " " + defArg;
        }

        try {
            InstanceState i = instanceStates.get(instance);
            if (i != null && i.getState() != InstanceState.StateType.NEVER_STARTED
                    && i.getFailedCommands().size() < MAX_RECORDED_FAILED_COMMANDS) {
                i.addFailedCommands(cmdDetails);
                stateProcessor.addFailedCommand(instance, cmdDetails);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, AdminLoggerInfo.mISSaddcmderror, e.getLocalizedMessage());
        }
    }

    @Override
    public synchronized void removeFailedCommandsForInstance(String instance) {
        init();
        try {
            InstanceState i = instanceStates.get(instance);
            if (i != null) {
                i.removeFailedCommands();
                stateProcessor.removeFailedCommands(instance);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, AdminLoggerInfo.mISSremcmderror, e.getLocalizedMessage());
        }
    }

    @Override
    public synchronized InstanceState.StateType getState(String instanceName) {
        init();
        InstanceState s = instanceStates.get(instanceName);
        if (s == null) {
            return InstanceState.StateType.NEVER_STARTED;
        }
        return s.getState();
    }

    @Override
    public synchronized List<String> getFailedCommands(String instanceName) {
        init();
        InstanceState s = instanceStates.get(instanceName);
        if (s == null) {
            return new ArrayList<String>();
        }
        return s.getFailedCommands();
    }

    @Override
    public synchronized InstanceState.StateType setState(String name, InstanceState.StateType newState, boolean force) {
        init();
        boolean updateXML = false;
        InstanceState.StateType ret = newState;
        InstanceState is = instanceStates.get(name);
        InstanceState.StateType currState;
        if (is == null || (currState = is.getState()) == null) {
            instanceStates.put(name, new InstanceState(newState));
            updateXML = true;
            ret = newState;
        } else if (!force && currState == InstanceState.StateType.RESTART_REQUIRED) {
            // If current state is RESTART_REQUIRED, no updates to state is allowed because
            // only an instance restart can move this instance out of RESTART_REQD state
            updateXML = false;
            ret = currState;
        } else if (!force && currState == InstanceState.StateType.NEVER_STARTED && (newState == InstanceState.StateType.NOT_RUNNING
                || newState == InstanceState.StateType.RESTART_REQUIRED || newState == InstanceState.StateType.NO_RESPONSE)) {
            // invalid state change
            updateXML = false;
            ret = currState;
        } else if (!currState.equals(newState)) {
            instanceStates.get(name).setState(newState);
            updateXML = true;
            ret = newState;
        }

        try {
            if (updateXML) {
                stateProcessor.updateState(name, newState.getDescription());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, AdminLoggerInfo.mISSsetstateerror, e.getLocalizedMessage());
        }
        return ret;
    }

    @Override
    public synchronized void removeInstanceFromStateService(String name) {
        init();
        instanceStates.remove(name);
        try {
            stateProcessor.removeInstanceNode(name);
        } catch (Exception e) {
            logger.log(Level.SEVERE, AdminLoggerInfo.mISSremstateerror, e.getLocalizedMessage());
        }
    }

    /*
     * For now, this just submits the job directly to the pool.  In the future
     * it might be possible to avoid submitting the job
     */
    @Override
    public Future<InstanceCommandResult> submitJob(Server server, InstanceCommand ice, InstanceCommandResult r) {
        return cmdPool.submitJob(ice, r);
    }
}
