/*
 * Copyright (c) 2008, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security;

import com.sun.enterprise.security.audit.AuditManager;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.security.auth.callback.CallbackHandler;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;

@Service
@Singleton
public class SecurityServicesUtil {

    private static ServiceLocator serviceLocator = Globals.getDefaultHabitat();

    @Inject
    private ProcessEnvironment processEnv;

    @Inject
    private ServerEnvironment env;

    @Inject
    private AuditManager auditManager;

    //the appclient CBH
    private CallbackHandler callbackHandler;

    public ServiceLocator getHabitat() {
        return serviceLocator;
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public AuditManager getAuditManager() {
        return auditManager;
    }

    public static SecurityServicesUtil getInstance() {
        // return my singleton service
        if (serviceLocator == null) {
            return null;
        }

        return serviceLocator.getService(SecurityServicesUtil.class);
    }

    public ProcessEnvironment getProcessEnv() {
        return processEnv;
    }

    public boolean isACC() {
        return processEnv.getProcessType().equals(ProcessType.ACC);
    }

    public boolean isServer() {
        return processEnv.getProcessType().isServer();
    }

    public boolean isNotServerOrACC() {
        return processEnv.getProcessType().equals(ProcessType.Other);
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

}
