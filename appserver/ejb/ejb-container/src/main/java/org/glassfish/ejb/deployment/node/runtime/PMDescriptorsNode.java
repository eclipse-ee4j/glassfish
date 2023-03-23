/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.node.runtime;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.w3c.dom.Node;

/**
 * This node handles the pm-descriptors runtime xml element
 *
 * @author Jerome Dochez
 */
@Deprecated(forRemoval = true, since = "3.1")
public class PMDescriptorsNode extends RuntimeDescriptorNode<EjbBundleDescriptorImpl> {

    public PMDescriptorsNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.PM_DESCRIPTOR), PMDescriptorNode.class, "addPersistenceManager");
        registerElementHandler(new XMLElement(RuntimeTagNames.PM_INUSE), PMInUseNode.class, "setPersistenceManagerInUse");
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, EjbBundleDescriptorImpl descriptor) {
        return null;
    }
}
