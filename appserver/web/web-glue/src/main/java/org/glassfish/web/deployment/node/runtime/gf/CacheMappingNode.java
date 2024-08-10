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

package org.glassfish.web.deployment.node.runtime.gf;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.Map;

import org.glassfish.web.deployment.runtime.CacheMapping;
import org.glassfish.web.deployment.runtime.ConstraintField;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
* node for cache-mapping tag
*
* @author Jerome Dochez
*/
public class CacheMappingNode extends RuntimeDescriptorNode<CacheMapping> {

    public CacheMappingNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.CONSTRAINT_FIELD), ConstraintFieldNode.class,
            "addNewConstraintField");
    }

    protected CacheMapping descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public CacheMapping getDescriptor() {
        if (descriptor==null) {
            descriptor = new CacheMapping();
        }
        return descriptor;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> dispatchTable = super.getDispatchTable();
        dispatchTable.put(RuntimeTagNames.SERVLET_NAME, "setServletName");
        dispatchTable.put(RuntimeTagNames.URL_PATTERN, "setURLPattern");
        dispatchTable.put(RuntimeTagNames.CACHE_HELPER_REF, "setCacheHelperRef");
        dispatchTable.put(RuntimeTagNames.TIMEOUT, "setTimeout");
        dispatchTable.put(RuntimeTagNames.HTTP_METHOD, "addNewHttpMethod");
        dispatchTable.put(RuntimeTagNames.DISPATCHER, "addNewDispatcher");
        return dispatchTable;
    }

    @Override
    public void startElement(XMLElement element, Attributes attributes) {
        CacheMapping descriptor = getDescriptor();
        if (element.getQName().equals(RuntimeTagNames.TIMEOUT)) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (RuntimeTagNames.NAME.equals(attributes.getQName(i))) {
                    descriptor.setAttributeValue(CacheMapping.TIMEOUT, CacheMapping.NAME, attributes.getValue(i));
                } else if (RuntimeTagNames.SCOPE.equals(attributes.getQName(i))) {
                    int index = 0;
                    while (descriptor.getAttributeValue(CacheMapping.TIMEOUT, index, CacheMapping.NAME) != null) {
                        index++;
                    }
                    descriptor.setAttributeValue(CacheMapping.TIMEOUT, index-1, CacheMapping.SCOPE, attributes.getValue(i));
                }
            }
        } else if (element.getQName().equals(RuntimeTagNames.REFRESH_FIELD)) {
            descriptor.setRefreshField(true);
            for (int i=0; i<attributes.getLength();i++) {
                if (RuntimeTagNames.NAME.equals(attributes.getQName(i))) {
                    descriptor.setAttributeValue(CacheMapping.REFRESH_FIELD, 0, CacheMapping.NAME, attributes.getValue(i));
                } else if (RuntimeTagNames.SCOPE.equals(attributes.getQName(i))) {
                    descriptor.setAttributeValue(CacheMapping.REFRESH_FIELD, 0, CacheMapping.SCOPE, attributes.getValue(i));
                }
            }
        } else if (element.getQName().equals(RuntimeTagNames.KEY_FIELD)) {
            descriptor.addKeyField(true);
            for (int i=0; i<attributes.getLength();i++) {
                if (RuntimeTagNames.NAME.equals(attributes.getQName(i))) {
                    descriptor.setAttributeValue(CacheMapping.KEY_FIELD, CacheMapping.NAME, attributes.getValue(i));
                } else if (RuntimeTagNames.SCOPE.equals(attributes.getQName(i))) {
                    int index = descriptor.sizeKeyField();
                    descriptor.setAttributeValue(CacheMapping.KEY_FIELD, index-1, CacheMapping.SCOPE, attributes.getValue(i));
                }
            }
        } else {
            super.startElement(element, attributes);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, CacheMapping descriptor) {
        Node cacheMapping = super.writeDescriptor(parent, nodeName, descriptor);
        if (descriptor.getServletName()!=null) {
            appendTextChild(cacheMapping, RuntimeTagNames.SERVLET_NAME, descriptor.getServletName());
        } else {
            appendTextChild(cacheMapping, RuntimeTagNames.URL_PATTERN, descriptor.getURLPattern());
        }

        // cache-helper-ref
        appendTextChild(cacheMapping, RuntimeTagNames.CACHE_HELPER_REF,
            (String) descriptor.getValue(CacheMapping.CACHE_HELPER_REF));

        //dispatcher*
        String[] dispatchers = descriptor.getDispatcher();
        if (dispatchers!=null) {
            for (String dispatcher : dispatchers) {
                appendTextChild(cacheMapping, RuntimeTagNames.DISPATCHER, dispatcher);
            }
        }

        // timeout?
        Element timeout = (Element) forceAppendTextChild(cacheMapping, RuntimeTagNames.TIMEOUT,
            (String) descriptor.getValue(CacheMapping.TIMEOUT));
        // timeout attributes
        String name = descriptor.getAttributeValue(CacheMapping.TIMEOUT, CacheMapping.NAME);
        if (name!=null) {
            setAttribute(timeout, RuntimeTagNames.NAME, name);
        }
        String scope = descriptor.getAttributeValue(CacheMapping.TIMEOUT, CacheMapping.SCOPE);
        if (scope!=null) {
            setAttribute(timeout, RuntimeTagNames.SCOPE, scope);
        }

        //refresh-field?,
        if (descriptor.isRefreshField()) {
            Element refreshField = appendChild(cacheMapping, RuntimeTagNames.REFRESH_FIELD);
            setAttribute(refreshField, RuntimeTagNames.NAME,
                descriptor.getAttributeValue(CacheMapping.REFRESH_FIELD, CacheMapping.NAME));
            setAttribute(refreshField, RuntimeTagNames.SCOPE,
                descriptor.getAttributeValue(CacheMapping.REFRESH_FIELD, CacheMapping.SCOPE));
        }

        //http-method*
        String[] httpMethods = descriptor.getHttpMethod();
        if (httpMethods!=null) {
            for (String httpMethod : httpMethods) {
                appendTextChild(cacheMapping, RuntimeTagNames.HTTP_METHOD, httpMethod);
            }
        }

        //key-field*
        if (descriptor.sizeKeyField() > 0) {
            for (int i = 0; i < descriptor.sizeKeyField(); i++) {

                if (descriptor.isKeyField(i)) {
                    Element keyField = appendChild(cacheMapping, RuntimeTagNames.KEY_FIELD);
                    setAttribute(keyField, RuntimeTagNames.NAME,
                        descriptor.getAttributeValue(CacheMapping.KEY_FIELD, i, CacheMapping.NAME));
                    setAttribute(keyField, RuntimeTagNames.SCOPE,
                        descriptor.getAttributeValue(CacheMapping.KEY_FIELD, i, CacheMapping.SCOPE));
                }
            }
        }

        //constraint-field*
        if (descriptor.sizeConstraintField()>0) {
            ConstraintField[] constraintFields = descriptor.getConstraintField();
            ConstraintFieldNode cfn = new ConstraintFieldNode();
            cfn.writeDescriptor(cacheMapping, RuntimeTagNames.CONSTRAINT_FIELD, constraintFields);
        }

        return cacheMapping;
    }
}
