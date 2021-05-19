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

import java.util.Map;

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.types.EjbReference;
import com.sun.enterprise.deployment.xml.TagNames;
import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;

/**
 * This class handles all information in the ejb-reference xml node
 *
 * @author  Jerome Dochez
 * @version
 */
public class EjbReferenceNode extends DeploymentDescriptorNode<EjbReference> {

    protected EjbReference descriptor;

    public EjbReferenceNode() {
        super();
        registerElementHandler(new XMLElement(TagNames.INJECTION_TARGET),
                                InjectionTargetNode.class, "addInjectionTarget");
    }

    @Override
    public EjbReference getDescriptor() {
        if (descriptor==null) {
            descriptor = new EjbReferenceDescriptor();
            descriptor.setLocal(false);
        }
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(TagNames.EJB_REFERENCE_NAME, "setName");
        table.put(TagNames.EJB_REFERENCE_TYPE, "setType");
        table.put(TagNames.HOME, "setEjbHomeInterface");
        table.put(TagNames.REMOTE, "setEjbInterface");
        table.put(TagNames.LOCAL_HOME, "setEjbHomeInterface");
        table.put(TagNames.LOCAL, "setEjbInterface");
        table.put(TagNames.EJB_LINK, "setLinkName");
        table.put(TagNames.MAPPED_NAME, "setMappedName");
        table.put(TagNames.LOOKUP_NAME, "setLookupName");
        return table;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, EjbReference descriptor) {
        Node ejbRefNode = appendChild(parent, nodeName);
        if (descriptor instanceof Descriptor) {
            Descriptor ejbRefDesc = (Descriptor)descriptor;
            writeLocalizedDescriptions(ejbRefNode, ejbRefDesc);
        }
        appendTextChild(ejbRefNode, TagNames.EJB_REFERENCE_NAME, descriptor.getName());
        appendTextChild(ejbRefNode, TagNames.EJB_REFERENCE_TYPE, descriptor.getType());
        if (descriptor.isLocal()) {
            appendTextChild(ejbRefNode, TagNames.LOCAL_HOME, descriptor.getEjbHomeInterface());
            appendTextChild(ejbRefNode, TagNames.LOCAL, descriptor.getEjbInterface());
        } else {
            appendTextChild(ejbRefNode, TagNames.HOME, descriptor.getEjbHomeInterface());
            appendTextChild(ejbRefNode, TagNames.REMOTE, descriptor.getEjbInterface());
        }
        appendTextChild(ejbRefNode, TagNames.EJB_LINK, descriptor.getLinkName());

        if( descriptor instanceof EnvironmentProperty) {
            EnvironmentProperty envProp = (EnvironmentProperty)descriptor;
            appendTextChild(ejbRefNode, TagNames.MAPPED_NAME, envProp.getMappedName());
        }
        if( descriptor.isInjectable() ) {
            InjectionTargetNode ijNode = new InjectionTargetNode();
            for (InjectionTarget target : descriptor.getInjectionTargets()) {
                ijNode.writeDescriptor(ejbRefNode, TagNames.INJECTION_TARGET, target);
            }
        }

        if( descriptor.hasLookupName() ) {
            appendTextChild(ejbRefNode, TagNames.LOOKUP_NAME, descriptor.getLookupName());
        }

        return ejbRefNode;
    }

}
