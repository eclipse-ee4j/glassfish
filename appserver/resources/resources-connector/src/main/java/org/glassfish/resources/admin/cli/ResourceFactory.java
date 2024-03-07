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

package org.glassfish.resources.admin.cli;

import org.glassfish.api.I18n;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.resources.api.Resource;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

import jakarta.inject.Inject;

/**
 *
 * @author PRASHANTH ABBAGANI
 *
 * Factory class which returns the appropriate ResourceManager
 */
@Service(name="resource-factory")
@PerLookup
@I18n("add.resources")
public class ResourceFactory {

    @Inject
    private IterableProvider<ResourceManager> resourceManagers;

    public ResourceManager getResourceManager(Resource resource) {
        String resourceType = resource.getType();

        ResourceManager resourceManager = null;

        for (ResourceManager rm : resourceManagers) {
            if ((rm.getResourceType()).equals(resourceType)) {
                resourceManager = rm;
                break;
            }
        }

        return resourceManager;
    }

}
