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

import com.sun.enterprise.config.serverbeans.Server;

import java.util.List;
import java.util.concurrent.Future;

import org.glassfish.api.admin.InstanceCommand;
import org.glassfish.api.admin.InstanceCommandResult;
import org.glassfish.api.admin.InstanceState;
import org.glassfish.api.admin.ParameterMap;
import org.jvnet.hk2.annotations.Contract;

/**
 * Interface for service that parses the instance state file.
 *
 * @author Vijay Ramachandran
 */
@Contract
public interface InstanceStateService {

    public void addServerToStateService(String instanceName);

    public void addFailedCommandToInstance(String instance, String cmd, ParameterMap params);

    public void removeFailedCommandsForInstance(String instance);

    public InstanceState.StateType getState(String instanceName);

    public List<String> getFailedCommands(String instanceName);

    public InstanceState.StateType setState(String name, InstanceState.StateType newState, boolean force);

    public void removeInstanceFromStateService(String name);

    public Future<InstanceCommandResult> submitJob(Server server, InstanceCommand ice, InstanceCommandResult r);
}
