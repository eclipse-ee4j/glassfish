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

package org.glassfish.ejb.deployment.node;

import com.sun.enterprise.deployment.MethodPermission;
import com.sun.enterprise.deployment.MethodPermissionDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.MethodNode;
import com.sun.enterprise.deployment.node.XMLElement;

import org.glassfish.ejb.deployment.EjbTagNames;

/**
 * This class is responsible for handling the exclude-list DD tag
 *
 * @author Jeromeochez
 */
public class ExcludeListNode extends DeploymentDescriptorNode<MethodPermissionDescriptor> {

    private MethodPermissionDescriptor descriptor;

    public ExcludeListNode() {
        registerElementHandler(new XMLElement(EjbTagNames.METHOD), MethodNode.class, "addMethod");
    }


    @Override
    public MethodPermissionDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new MethodPermissionDescriptor();
            descriptor.addMethodPermission(MethodPermission.getDenyAllMethodPermission());
        }
        return descriptor;
    }
}
