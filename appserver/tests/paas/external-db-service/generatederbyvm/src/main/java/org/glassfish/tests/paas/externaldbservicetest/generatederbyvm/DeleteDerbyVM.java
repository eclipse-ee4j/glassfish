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

package org.glassfish.tests.paas.externaldbservicetest.generatederbyvm;

import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.virtualization.runtime.VirtualClusters;
import org.glassfish.virtualization.runtime.VirtualMachineLifecycle;
import org.glassfish.virtualization.spi.VirtualCluster;
import org.glassfish.virtualization.spi.VirtualMachine;
import javax.inject.Inject;
import org.jvnet.hk2.annotations.Optional;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

/**
 * @author Shalini M
 */
@Service(name = "delete-derby-vm")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
public class DeleteDerbyVM implements AdminCommand {

    @Inject @Optional
    private VirtualClusters virtualClusters;

    @Inject
    VirtualMachineLifecycle vmLifecycle;

    @Param(name = "virtualcluster", optional = true, defaultValue = "db-external-service-test-cluster")
    private String virtualClusterName;

    public void execute(AdminCommandContext context) {
        try {
            VirtualCluster vCluster = virtualClusters.byName(virtualClusterName);
            VirtualMachine vm = vCluster.getVMs().get(0);
            vmLifecycle.delete(vm);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

    }
}
