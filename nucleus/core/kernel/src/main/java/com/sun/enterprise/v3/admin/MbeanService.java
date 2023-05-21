/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import javax.management.InstanceNotFoundException;

import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.PropertyResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;

import jakarta.inject.Inject;

@Service
@RunLevel(mode = RunLevel.RUNLEVEL_MODE_NON_VALIDATING, value = StartupRunLevel.VAL)
public class MbeanService {

    @Inject
    private Domain domain;

    @Inject
    private Target tgt;

    private static ServiceLocator habitat = Globals.getDefaultHabitat();

    @Inject
    private ServerEnvironment env;

    public static MbeanService getInstance() {
        if (habitat == null) {
            return null;
        }
        return habitat.getService(MbeanService.class);
    }

    public String getHost(String instance) throws InstanceNotFoundException {
        Server s = domain.getServerNamed(instance);
        if (s == null) {
            throw new InstanceNotFoundException();
        }
        return s.getAdminHost();
    }

    public String getJMXPort(String instance) throws InstanceNotFoundException {
        Server s = domain.getServerNamed(instance);
        if (s == null) {
            throw new InstanceNotFoundException();
        }
        return new PropertyResolver(domain, instance).getPropertyValue("JMX_SYSTEM_CONNECTOR_PORT");
    }

    public boolean isDas() {
        return tgt.isThisDAS();
    }

    public boolean isValidServer(String name) {
        Server s = null;
        try {
            s = domain.getServerNamed(name);
        } catch (Throwable t) {
            return false;
        }
        return (s == null) ? false : true;
    }

    public List<String> getAllInstances() {
        return convertList(tgt.getAllInstances());
    }

    public List<String> getInstances(String name) {
        return convertList(tgt.getInstances(name));
    }

    private List<String> convertList(List<Server> servers) {
        List<String> serverStrings = new ArrayList<>();
        for (Server svr : servers) {
            serverStrings.add(svr.getName());
        }
        return serverStrings;
    }

    public boolean isInstance(String name) {
        return env.getInstanceName().equals(name);
    }

    /**
     * Returns if the SystemJMXConnector is secure or not
     *
     * @param instance
     * @return
     */
    public boolean isSecureJMX(String instance) {
        String isSecure = "false";
        if (domain.getServerNamed(instance) != null) {
            if (domain.getServerNamed(instance).getConfig().getAdminService().getSystemJmxConnector() != null) {
                isSecure = domain.getServerNamed(instance).getConfig().getAdminService().getSystemJmxConnector().getSecurityEnabled();
            }
        }
        return Boolean.parseBoolean(isSecure);

    }
}
