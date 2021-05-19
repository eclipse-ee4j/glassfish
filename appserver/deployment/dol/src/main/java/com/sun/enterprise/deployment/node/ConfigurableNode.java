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

/*
 * RelationshipRoleSourceNode.java
 *
 * Created on February 1, 2002, 3:36 PM
 */

package com.sun.enterprise.deployment.node;

import java.util.Map;

/**
 * ConfigurableNode able to treat dispatch element values to descriptors based
 * on initialized values
 *
 * @author  Jerome Dochez
 * @version
 */
public class ConfigurableNode extends DeploymentDescriptorNode {

    private Map dispatchTable;
    private Object descriptor;

    public ConfigurableNode(Object instance, Map dispatchTable) {
        super();
        this.dispatchTable = dispatchTable;
        this.descriptor = instance;
    }

    public ConfigurableNode(Object descriptor, Map dispatchTable, XMLElement element) {
        super();
        this.dispatchTable = dispatchTable;
        this.descriptor = descriptor;
    super.setXMLRootTag(element);
    }

    /**
    * @return the descriptor instance to associate with this XMLNode
    */
    public Object getDescriptor() {
        return descriptor;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        return dispatchTable;
    }
}
