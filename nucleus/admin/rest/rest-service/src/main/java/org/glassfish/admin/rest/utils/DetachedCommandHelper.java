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

package org.glassfish.admin.rest.utils;

import com.sun.enterprise.admin.remote.AdminCommandStateImpl;
import com.sun.enterprise.v3.admin.JobManagerService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.glassfish.admin.rest.RestLogging;
import org.glassfish.api.admin.AdminCommandEventBroker;
import org.glassfish.api.admin.AdminCommandState;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.internal.api.Globals;

/**
 *
 * @author jdlee
 */
public class DetachedCommandHelper implements Runnable, AdminCommandEventBroker.AdminCommandListener {

    private final CommandRunner.CommandInvocation commandInvocation;
    private CountDownLatch latch;
    private String jobId;
    private AdminCommandEventBroker broker;

    private DetachedCommandHelper(final CommandRunner.CommandInvocation commandInvocation, CountDownLatch latch) {
        this.commandInvocation = commandInvocation;
        this.latch = latch;
    }

    @Override
    public void run() {
        commandInvocation.execute();
    }

    public static String invokeAsync(CommandRunner.CommandInvocation commandInvocation) {
        if (commandInvocation == null) {
            throw new IllegalArgumentException("commandInvocation");
        }
        CountDownLatch latch = new CountDownLatch(1);
        DetachedCommandHelper helper = new DetachedCommandHelper(commandInvocation, latch);
        commandInvocation.listener(".*", helper);
        JobManagerService jobManagerService = Globals.getDefaultHabitat().getService(JobManagerService.class);
        jobManagerService.getThreadPool().execute(helper);
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                RestLogging.restLogger.log(Level.FINE, "latch.await() returned false");
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        return helper.jobId;
    }

    @Override
    public void onAdminCommandEvent(final String name, Object event) {
        if (name == null || event == null) {
            return;
        }
        if (AdminCommandEventBroker.BrokerListenerRegEvent.EVENT_NAME_LISTENER_REG.equals(name)) {
            AdminCommandEventBroker.BrokerListenerRegEvent blre = (AdminCommandEventBroker.BrokerListenerRegEvent) event;
            broker = blre.getBroker();
            return;
        }
        if (name.startsWith(AdminCommandEventBroker.LOCAL_EVENT_PREFIX)) {
            return; //Prevent events from client to be send back to client
        }

        if (AdminCommandStateImpl.EVENT_STATE_CHANGED.equals(name)) {
            unregister();
            AdminCommandState acs = (AdminCommandState) event;
            jobId = acs.getId();
            latch.countDown();
        }
    }

    private void unregister() {
        if (broker != null) {
            broker.unregisterListener(this);
        }
    }
}
