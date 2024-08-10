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

import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.MethodNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.List;
import java.util.Map;

import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.InterceptorBindingDescriptor;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

public class InterceptorBindingNode extends DeploymentDescriptorNode<InterceptorBindingDescriptor> {

    private MethodDescriptor businessMethod;
    private boolean needsOverloadResolution;
    private InterceptorBindingDescriptor descriptor;

    @Override
    public InterceptorBindingDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new InterceptorBindingDescriptor();
        }
        return descriptor;
    }


    @Override
    public void startElement(XMLElement element, Attributes attributes) {
        if (EjbTagNames.METHOD.equals(element.getQName())) {
            businessMethod = new MethodDescriptor();
            // Assume we need overloaded method resolution until we
            // encounter at least one method-param element.
            needsOverloadResolution = true;
        } else if (TagNames.METHOD_PARAMS.equals(element.getQName())) {
            // If there's a method-params element, regardless of whether there
            // are any <method-param> sub-elements, there's no overload
            // resolution needed.
            needsOverloadResolution = false;
        }
        super.startElement(element, attributes);
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.METHOD_NAME.equals(element.getQName())) {
            businessMethod.setName(value);
        } else if (TagNames.METHOD_PARAM.equals(element.getQName())) {
            if ((value != null) && (value.trim().length() > 0)) {
                businessMethod.addParameterClass(value.trim());
            }
        } else {
            super.setElementValue(element, value);
        }
    }


    /**
     * receives notification of the end of an XML element by the Parser
     *
     * @param element the xml tag identification
     * @return true if this node is done processing the XML sub tree
     */
    @Override
    public boolean endElement(XMLElement element) {
        if (EjbTagNames.INTERCEPTOR_ORDER.equals(element.getQName())) {
            InterceptorBindingDescriptor desc = getDescriptor();
            desc.setIsTotalOrdering(true);
        } else if (TagNames.METHOD_PARAMS.equals(element.getQName())) {
            // this means we have an empty method-params element
            // which means this method has no input parameter
            if (businessMethod.getParameterClassNames() == null) {
                businessMethod.setEmptyParameterClassNames();
            }
        } else if (EjbTagNames.METHOD.equals(element.getQName())) {
            InterceptorBindingDescriptor bindingDesc = getDescriptor();
            businessMethod.setEjbClassSymbol(MethodDescriptor.EJB_BEAN);
            bindingDesc.setBusinessMethod(businessMethod);
            if (needsOverloadResolution) {
                bindingDesc.setNeedsOverloadResolution(true);
            }
            businessMethod = null;
            needsOverloadResolution = false;
        }
        return super.endElement(element);
    }


    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(TagNames.EJB_NAME, "setEjbName");
        table.put(EjbTagNames.INTERCEPTOR_CLASS, "appendInterceptorClass");
        table.put(EjbTagNames.EXCLUDE_DEFAULT_INTERCEPTORS, "setExcludeDefaultInterceptors");
        table.put(EjbTagNames.EXCLUDE_CLASS_INTERCEPTORS, "setExcludeClassInterceptors");
        return table;
    }


    /**
     * Write interceptor bindings for this ejb.
     *
     * @param parent node in the DOM tree
     * @param ejbDesc the descriptor to write
     */
    public void writeBindings(Node parent, EjbDescriptor ejbDesc) {
        List<EjbInterceptor> classInterceptors = ejbDesc.getInterceptorChain();
        if (!classInterceptors.isEmpty()) {
            writeTotalOrdering(parent, classInterceptors, ejbDesc, null);
        }

        Map<MethodDescriptor, List<EjbInterceptor>> methodInterceptorsMap = ejbDesc.getMethodInterceptorsMap();
        for (Map.Entry<MethodDescriptor, List<EjbInterceptor>> mapEntry : methodInterceptorsMap.entrySet()) {
            List<EjbInterceptor> interceptors = mapEntry.getValue();
            if (interceptors.isEmpty()) {
                writeExclusionBinding(parent, ejbDesc, mapEntry.getKey());
            } else {
                writeTotalOrdering(parent, interceptors, ejbDesc, mapEntry.getKey());
            }
        }
    }


    private void writeTotalOrdering(Node parent, List<EjbInterceptor> interceptors, EjbDescriptor ejbDesc,
        MethodDescriptor method) {
        Node bindingNode = appendChild(parent, EjbTagNames.INTERCEPTOR_BINDING);

        appendTextChild(bindingNode, TagNames.EJB_NAME, ejbDesc.getName());

        Node totalOrderingNode = appendChild(bindingNode, EjbTagNames.INTERCEPTOR_ORDER);
        for (EjbInterceptor next : interceptors) {
            appendTextChild(totalOrderingNode, EjbTagNames.INTERCEPTOR_CLASS, next.getInterceptorClassName());
        }

        if (method != null) {
            MethodNode methodNode = new MethodNode();

            // Write out method description. void methods will be written
            // out using an empty method-params element so they will not
            // be interpreted as overloaded when processed.
            methodNode.writeJavaMethodDescriptor(bindingNode, EjbTagNames.INTERCEPTOR_BUSINESS_METHOD, method, true);
        }
    }


    private void writeExclusionBinding(Node parent, EjbDescriptor ejbDesc, MethodDescriptor method) {
        Node bindingNode = appendChild(parent, EjbTagNames.INTERCEPTOR_BINDING);

        appendTextChild(bindingNode, TagNames.EJB_NAME, ejbDesc.getName());
        appendTextChild(bindingNode, EjbTagNames.EXCLUDE_CLASS_INTERCEPTORS, "true");

        MethodNode methodNode = new MethodNode();

        // Write out method description. void methods will be written
        // out using an empty method-params element so they will not
        // be interpreted as overloaded when processed.
        methodNode.writeJavaMethodDescriptor(bindingNode, EjbTagNames.INTERCEPTOR_BUSINESS_METHOD, method, true);
    }
}
