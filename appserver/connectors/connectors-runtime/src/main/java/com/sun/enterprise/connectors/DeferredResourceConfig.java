/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;

import org.glassfish.connectors.config.AdminObjectResource;
import org.glassfish.connectors.config.ResourceAdapterConfig;

public class DeferredResourceConfig {

    private String rarName;
    private AdminObjectResource adminObject;
    private ResourcePool resourcePool;
    private BindableResource bindableResource;
    private ResourceAdapterConfig[] resourceAdapterConfig;
    private Resource[] resourcesToLoad;

    public DeferredResourceConfig() {

    }

    public DeferredResourceConfig(String rarName, AdminObjectResource adminObject, ResourcePool resourcePool,
            BindableResource bindableResource, ResourceAdapterConfig[] resAdapterConfig) {

        this.rarName = rarName;
        this.adminObject = adminObject;
        this.resourcePool = resourcePool;
        this.bindableResource = bindableResource;
        this.resourceAdapterConfig = resAdapterConfig;

    }

    public void setRarName(String rarName) {
        this.rarName = rarName;
    }

    public String getRarName() {
        return this.rarName;
    }

    public void setAdminObject(AdminObjectResource adminObject) {
        this.adminObject = adminObject;
    }

    public AdminObjectResource getAdminObject() {
        return this.adminObject;
    }

    public void setResourcePool(ResourcePool resourcePool) {
        this.resourcePool = resourcePool;
    }

    public ResourcePool getResourcePool() {
        return this.resourcePool;
    }

    public void setBindableResource(BindableResource bindableResource) {
        this.bindableResource = bindableResource;
    }

    public BindableResource getBindableResource() {
        return this.bindableResource;
    }

    public void setResourceAdapterConfig(ResourceAdapterConfig[] resourceAdapterConfig) {
        this.resourceAdapterConfig = resourceAdapterConfig;
    }

    public ResourceAdapterConfig[] getResourceAdapterConfig() {
        return this.resourceAdapterConfig;
    }

    public void setResourcesToLoad(Resource[] resourcesToLoad) {
        this.resourcesToLoad = resourcesToLoad;
    }

    public Resource[] getResourcesToLoad() {
        return this.resourcesToLoad;
    }
}
