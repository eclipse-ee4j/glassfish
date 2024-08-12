/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.deployer;

import jakarta.inject.Singleton;

import org.glassfish.connectors.config.WorkSecurityMap;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.jvnet.hk2.annotations.Service;

@Service
@Singleton
@ResourceDeployerInfo(WorkSecurityMap.class)
public class ConnectorWorkSecurityMapDeployer  extends AbstractConnectorResourceDeployer<WorkSecurityMap> {

    @Override
    public synchronized void deployResource(WorkSecurityMap resource, String applicationName, String moduleName)
            throws Exception {
        //no-op
    }

    @Override
    public void deployResource(WorkSecurityMap resoure) throws Exception {
        //no-op
    }

    @Override
    public void undeployResource(WorkSecurityMap resoure) throws Exception {
        //no-op
    }

    @Override
    public void undeployResource(WorkSecurityMap resource, String applicationName, String moduleName) throws Exception{
        //no-op
    }

    @Override
    public void redeployResource(WorkSecurityMap resource) throws Exception {
        //no-op
    }

    @Override
    public void enableResource(WorkSecurityMap resoure) throws Exception {
        //no-op
    }

    @Override
    public void disableResource(WorkSecurityMap resoure) throws Exception {
        //no-op
    }

    @Override
    public boolean handles(Object resource) {
        return resource instanceof WorkSecurityMap;
    }
}
