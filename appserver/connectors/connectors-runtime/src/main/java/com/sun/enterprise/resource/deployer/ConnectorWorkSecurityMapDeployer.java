/*
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

import org.glassfish.connectors.config.WorkSecurityMap;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Singleton;

@Service
@ResourceDeployerInfo(WorkSecurityMap.class)
@Singleton
public class ConnectorWorkSecurityMapDeployer  extends AbstractConnectorResourceDeployer {

    public synchronized void deployResource(Object resource, String applicationName, String moduleName)
            throws Exception {
        //no-op
    }

    public void deployResource(Object resoure) throws Exception {
        //no-op
    }

    public void undeployResource(Object resoure) throws Exception {
        //no-op
    }

    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception{
        //no-op
    }

    public void redeployResource(Object resource) throws Exception {
        //no-op
    }

    public void enableResource(Object resoure) throws Exception {
        //no-op
    }

    public void disableResource(Object resoure) throws Exception {
        //no-op
    }

    public boolean handles(Object resource) {
        return resource instanceof WorkSecurityMap;
    }

    /**
     * @inheritDoc
     */
    public boolean supportsDynamicReconfiguration() {
        return false;
    }

    /**
     * @inheritDoc
     */
    public Class[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
    }
}
