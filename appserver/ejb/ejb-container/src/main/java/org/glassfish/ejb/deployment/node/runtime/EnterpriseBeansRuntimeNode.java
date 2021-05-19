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

package org.glassfish.ejb.deployment.node.runtime;

import java.util.Iterator;

import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.MessageDestinationRuntimeNode;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.node.runtime.WebServiceRuntimeNode;
import com.sun.enterprise.deployment.node.runtime.common.RuntimeNameValuePairNode;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.w3c.dom.Node;

/**
 * This node handles runtime deployment descriptors for ejb bundle
 *
 * @author  Jerome Dochez
 * @version
 */
public class EnterpriseBeansRuntimeNode extends RuntimeDescriptorNode {

    public EnterpriseBeansRuntimeNode() {
        // we do not care about our standard DDS handles
        handlers = null;
        registerElementHandler(new XMLElement(RuntimeTagNames.EJB), EjbNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.PM_DESCRIPTORS), PMDescriptorsNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.CMP_RESOURCE), CmpResourceNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.MESSAGE_DESTINATION), MessageDestinationRuntimeNode.class);
        registerElementHandler(new XMLElement(WebServicesTagNames.WEB_SERVICE), WebServiceRuntimeNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.PROPERTY), RuntimeNameValuePairNode.class, "addEnterpriseBeansProperty");
    }

    @Override
    public Object getDescriptor() {
        return getParentNode().getDescriptor();
    }

    @Override
    protected XMLElement getXMLRootTag() {
        return new XMLElement(RuntimeTagNames.EJBS);
    }

    @Override
    public void setElementValue(XMLElement element, String value) {

        if (RuntimeTagNames.NAME.equals(element.getQName())) {
            DOLUtils.getDefaultLogger().finer("Ignoring runtime bundle name " + value);
            return;
        }

        if (RuntimeTagNames.UNIQUE_ID.equals(element.getQName())) {
            DOLUtils.getDefaultLogger().finer("Ignoring unique id");
            return;
        }
        super.setElementValue(element, value);
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, EjbBundleDescriptorImpl bundleDescriptor) {

        Node ejbs = super.writeDescriptor(parent, nodeName, bundleDescriptor);

        // NOTE : unique-id is no longer written out to sun-ejb-jar.xml.  It is persisted via
        // domain.xml deployment context properties instead.

        // ejb*
        EjbNode ejbNode = new EjbNode();
        for (Iterator ejbIterator = bundleDescriptor.getEjbs().iterator();ejbIterator.hasNext();) {
            EjbDescriptor ejbDescriptor = (EjbDescriptor) ejbIterator.next();
            ejbNode.writeDescriptor(ejbs, RuntimeTagNames.EJB, ejbDescriptor);
        }

        // pm-descriptors?
        PMDescriptorsNode pmsNode = new PMDescriptorsNode();
        pmsNode.writeDescriptor(ejbs, RuntimeTagNames.PM_DESCRIPTORS, bundleDescriptor);

        // cmpresource?
        ResourceReferenceDescriptor rrd = bundleDescriptor.getCMPResourceReference();
        if ( rrd != null ) {
            CmpResourceNode crn = new CmpResourceNode();
            crn.writeDescriptor(ejbs, RuntimeTagNames.CMP_RESOURCE, rrd);
        }

        // message-destination*
        writeMessageDestinationInfo(ejbs, bundleDescriptor);

        // webservice-description*
        WebServiceRuntimeNode webServiceNode = new WebServiceRuntimeNode();
        webServiceNode.writeWebServiceRuntimeInfo(ejbs, bundleDescriptor);

        for(NameValuePairDescriptor p : bundleDescriptor.getEnterpriseBeansProperties()) {
            RuntimeNameValuePairNode nameValNode = new RuntimeNameValuePairNode();
            nameValNode.writeDescriptor(ejbs, RuntimeTagNames.PROPERTY, p);
        }

        return ejbs;
    }

}
