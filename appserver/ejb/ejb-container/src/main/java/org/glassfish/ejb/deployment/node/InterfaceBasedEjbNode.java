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

package org.glassfish.ejb.deployment.node;

import java.util.Map;

import com.sun.enterprise.deployment.node.SecurityRoleRefNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;
import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.w3c.dom.Node;

/**
 * This class is responsible for reading/writing all information
 * common to all EJB which are interfaces based (entity, session)
 *
 * @author  Jerome Dochez
 * @version
 */
public abstract class InterfaceBasedEjbNode<S extends EjbDescriptor> extends EjbNode<S> {

   public InterfaceBasedEjbNode() {
       super();
       // register sub XMLNodes
       registerElementHandler(new XMLElement(TagNames.ROLE_REFERENCE), SecurityRoleRefNode.class, "addRoleReference");
    }

    @Override
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();
        table.put(EjbTagNames.HOME, "setHomeClassName");
        table.put(EjbTagNames.REMOTE, "setRemoteClassName");
        table.put(EjbTagNames.LOCAL_HOME, "setLocalHomeClassName");
        table.put(EjbTagNames.LOCAL, "setLocalClassName");
        table.put(EjbTagNames.BUSINESS_LOCAL, "addLocalBusinessClassName");
        table.put(EjbTagNames.BUSINESS_REMOTE, "addRemoteBusinessClassName");
        table.put(EjbTagNames.SERVICE_ENDPOINT_INTERFACE,
                  "setWebServiceEndpointInterfaceName");
        return table;
    }

    @Override
    protected void writeCommonHeaderEjbDescriptor(Node ejbNode, EjbDescriptor descriptor) {
        super.writeCommonHeaderEjbDescriptor(ejbNode, descriptor);
        appendTextChild(ejbNode, EjbTagNames.HOME, descriptor.getHomeClassName());
        appendTextChild(ejbNode, EjbTagNames.REMOTE, descriptor.getRemoteClassName());
        appendTextChild(ejbNode, EjbTagNames.LOCAL_HOME, descriptor.getLocalHomeClassName());
        appendTextChild(ejbNode, EjbTagNames.LOCAL, descriptor.getLocalClassName());

        for(String next : descriptor.getLocalBusinessClassNames()) {
            appendTextChild(ejbNode, EjbTagNames.BUSINESS_LOCAL, next);
        }

        for(String next : descriptor.getRemoteBusinessClassNames()) {
            appendTextChild(ejbNode, EjbTagNames.BUSINESS_REMOTE, next);
        }

        if( descriptor.isLocalBean()) {
            appendChild(ejbNode, EjbTagNames.LOCAL_BEAN);
        }

        appendTextChild(ejbNode, EjbTagNames.SERVICE_ENDPOINT_INTERFACE,
                        descriptor.getWebServiceEndpointInterfaceName());
        appendTextChild(ejbNode, EjbTagNames.EJB_CLASS, descriptor.getEjbClassName());
    }
}
