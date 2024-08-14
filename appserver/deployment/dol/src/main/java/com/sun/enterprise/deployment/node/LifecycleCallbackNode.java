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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Map;

import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;


/**
 * This node handles all information relative to injection-complete xml tag
 */
public class LifecycleCallbackNode extends DeploymentDescriptorNode<LifecycleCallbackDescriptor> {

    private LifecycleCallbackDescriptor descriptor;

    @Override
    public LifecycleCallbackDescriptor getDescriptor() {

       if (descriptor==null) {
            descriptor = new LifecycleCallbackDescriptor();
            Descriptor parentDesc = (Descriptor)getParentNode().getDescriptor();
            if (parentDesc instanceof EjbDescriptor) {
                EjbDescriptor ejbDesc = (EjbDescriptor)parentDesc;
                descriptor.setDefaultLifecycleCallbackClass(
                    ejbDesc.getEjbClassName());
            } else if (parentDesc instanceof EjbInterceptor) {
                EjbInterceptor ejbInterceptor =
                    (EjbInterceptor)parentDesc;
                descriptor.setDefaultLifecycleCallbackClass(
                    ejbInterceptor.getInterceptorClassName());
            }
            // we set the default lifecycle callback class for appclient
            // later in validate since the appclient Main class is not
            // available at this point
        }
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(TagNames.LIFECYCLE_CALLBACK_CLASS, "setLifecycleCallbackClass");
        table.put(TagNames.LIFECYCLE_CALLBACK_METHOD, "setLifecycleCallbackMethod");
        return table;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, LifecycleCallbackDescriptor descriptor) {
        Node myNode = appendChild(parent, nodeName);
        appendTextChild(myNode, TagNames.LIFECYCLE_CALLBACK_CLASS,
            descriptor.getLifecycleCallbackClass());
        appendTextChild(myNode, TagNames.LIFECYCLE_CALLBACK_METHOD,
            descriptor.getLifecycleCallbackMethod());
        return myNode;
    }
}
