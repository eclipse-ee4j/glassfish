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

package org.glassfish.admin.amx.impl.util;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.module.ModulesRegistry;

import jakarta.inject.Inject;

import javax.management.MBeanServer;

import org.glassfish.api.Async;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.config.UnprocessedConfigListener;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;

/**
Utility class that gets various useful values injected into it for use
by other AMX facilities which don't have injection available to themselves.  This is needed
because many AMX MBeans and support code don't have any access to injection.
 */
@Service
@Async
public class InjectedValues {

    @Inject
    ServiceLocator mHabitat;
    @Inject
    private MBeanServer mMBeanServer;
    @Inject
    private ServerEnvironmentImpl mServerEnvironment;
    @Inject
    UnprocessedConfigListener mUnprocessedConfigListener;
    @Inject
    ModulesRegistry mModulesRegistry;
    @Inject
    Domain mDomain;

    public MBeanServer getMBeanServer() {
        return mMBeanServer;
    }

    public ServiceLocator getHabitat() {
        return mHabitat;
    }

    public ServerEnvironmentImpl getServerEnvironment() {
        return mServerEnvironment;
    }

    public UnprocessedConfigListener getUnprocessedConfigListener() {
        return mUnprocessedConfigListener;
    }

    public ModulesRegistry getModulesRegistry() {
        return mModulesRegistry;
    }

    public static ServiceLocator getDefaultServices() {
        return Globals.getDefaultHabitat();
    }

    public static InjectedValues getInstance() {
        return getDefaultServices().getService(InjectedValues.class);
    }

    public InjectedValues() {
    }
}













