/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resourcebase.resources;

import com.sun.enterprise.config.serverbeans.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.jvnet.hk2.annotations.Service;

/**
 * @author naman 2012
 */
@Service
public class ResourceTypeOrderProcessor {

    public Collection<Resource> getOrderedResources(Collection<Resource> resources) {
        ArrayList<Resource> resourceList = new ArrayList<>(resources);
        Collections.sort(resourceList,new ResourceComparator());
        return resourceList;
    }

    private class ResourceComparator implements Comparator<Resource> {

        @Override
        public int compare(Resource o1, Resource o2) {
            Class<?> o1Class = null;
            Class<?> o2Class = null;
            Class<?>[] interfaces = o1.getClass().getInterfaces();
            if (interfaces != null) {
                for (Class<?> clz : interfaces) {
                    if (Resource.class.isAssignableFrom(clz)) {
                        o1Class = clz;
                    }
                }
            }

            interfaces = o2.getClass().getInterfaces();
            if (interfaces != null) {
                for (Class<?> clz : interfaces) {
                    if (Resource.class.isAssignableFrom(clz)) {
                        o2Class = clz;
                    }
                }
            }

            if (o1Class == null && o2Class == null) {
                return 0;
            }
            if (o1Class == null && o2Class != null) {
                return -1;
            }
            if (o1Class != null && o2Class == null) {
                return 1;
            }

            if (o1Class.equals(o2Class)) {
                int i1 = Integer.parseInt(o1.getDeploymentOrder());
                int i2 = Integer.parseInt(o2.getDeploymentOrder());
                return (i2 > i1 ? -1 : (i1 == i2 ? 0 : 1));
            }

            int o1deploymentOrder = 100;
            int o2deploymentOrder = 100;
            Class<?>[] allInterfaces = o1.getClass().getInterfaces();
            for (Class<?> resourceInterface : allInterfaces) {
                ResourceTypeOrder resourceTypeOrder = resourceInterface.getAnnotation(ResourceTypeOrder.class);
                if (resourceTypeOrder != null) {
                    o1deploymentOrder = resourceTypeOrder.deploymentOrder().getResourceDeploymentOrder();
                }
            }
            allInterfaces = o2.getClass().getInterfaces();
            for (Class<?> resourceInterface : allInterfaces) {
                ResourceTypeOrder resourceTypeOrder = resourceInterface.getAnnotation(ResourceTypeOrder.class);
                if (resourceTypeOrder != null) {
                    o2deploymentOrder = resourceTypeOrder.deploymentOrder().getResourceDeploymentOrder();
                }
            }
            return o2deploymentOrder > o1deploymentOrder ? -1 : o2deploymentOrder == o1deploymentOrder ? 0 : 1;
        }
    }

}
