/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;

import java.util.Properties;

import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Contract;

/**
 * Contract for all ResourceManagers
 *
 * @author Prashanth Abbagani
 */
@Contract
public interface ResourceManager {

    /**
     * creates the resource as a child to the <i>resources</i> provided
     * @param resources parent for the resource to be created
     * @param attributes resource configuration
     * @param properties properties
     * @param target target
     * @return ResourceStatus indicating the status of resource creation
     * @throws Exception when unable to create the resource
     */
    ResourceStatus create(Resources resources, ResourceAttributes attributes, final Properties properties,
        String target) throws Exception;

    /**
     * creates config-bean equivalent for the resource configuration provided as attributes and
     * properties<br>
     * Does not persist the configuration<br>
     *
     * @param resources parent for the resource to be created
     * @param attributes attributes of the resource
     * @param properties properties of the resource
     * @param validate indicate whether config validation is required or not
     * @return Config-Bean equivalent of the resource
     * @throws Exception when unable to create config-bean-equivalent
     */
    Resource createConfigBean(Resources resources, ResourceAttributes attributes, Properties properties,
        boolean validate) throws Exception;

    /**
     * @return resource-type
     */
    String getResourceType();
}
