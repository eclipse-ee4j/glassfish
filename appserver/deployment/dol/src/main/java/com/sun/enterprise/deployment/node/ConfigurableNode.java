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

package com.sun.enterprise.deployment.node;

import java.util.Map;

import org.glassfish.deployment.common.Descriptor;

/**
 * ConfigurableNode able to treat dispatch element values to descriptors based
 * on initialized values
 *
 * @author Jerome Dochez
 */
public class ConfigurableNode extends DeploymentDescriptorNode<Descriptor> {

    private final Map<String, String> dispatchTable;
    private final Descriptor descriptor;


    public ConfigurableNode(Descriptor descriptor, Map<String, String> dispatchTable, XMLElement element) {
        this.dispatchTable = dispatchTable;
        this.descriptor = descriptor;
        super.setXMLRootTag(element);
    }


    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        return dispatchTable;
    }
}
