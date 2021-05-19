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

package org.glassfish.web.deployment.node;

import com.sun.enterprise.deployment.node.*;
import org.glassfish.web.deployment.descriptor.WebFragmentDescriptor;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.jvnet.hk2.annotations.Service;
import org.w3c.dom.Node;

import java.util.*;

/**
 * This node is responsible for handling the web-fragment xml tree
 *
 * @author  Shing Wai Chan
 * @version
 */
@Service
public class WebFragmentNode extends WebCommonNode<WebFragmentDescriptor> {

   public final static XMLElement tag = new XMLElement(WebTagNames.WEB_FRAGMENT);

    /**
     * The system ID of my documents.
     */
    public final static String SCHEMA_ID = "web-fragment_3_0.xsd";
    private final static List<String> systemIDs = initSystemIDs();

    private static List<String> initSystemIDs() {
        List<String> systemIDs = new ArrayList<String>();
        systemIDs.add(SCHEMA_ID);
        return Collections.unmodifiableList(systemIDs);
    }

    /**
     * register this node as a root node capable of loading entire DD files
     *
     * @param publicIDToDTD is a mapping between xml Public-ID to DTD
     * @return the doctype tag name
     */
    @Override
    public String registerBundle(Map publicIDToDTD) {
        return tag.getQName();
    }


    @Override
    public Map<String,Class> registerRuntimeBundle(final Map<String,String> publicIDToDTD, Map<String, List<Class>> versionUpgrades) {
        return Collections.emptyMap();
    }

    /** Creates new WebBundleNode */
    public WebFragmentNode()  {
        super();
        registerElementHandler(new XMLElement(WebTagNames.ORDERING),
                OrderingNode.class, "setOrderingDescriptor");
        SaxParserHandler.registerBundleNode(this, WebTagNames.WEB_FRAGMENT);
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
        table.put(WebTagNames.COMMON_NAME, "setName");
        return table;
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    @Override
    protected XMLElement getXMLRootTag() {
        return tag;
    }

    /**
     * @return the DOCTYPE of the XML file
     */
    public String getDocType() {
        return null;
    }

    /**
     * @return the SystemID of the XML file
     */
    public String getSystemID() {
        return SCHEMA_ID;
    }

    /**
     * @return the list of SystemID of the XML schema supported
     */
    public List<String> getSystemIDs() {
        return systemIDs;
    }

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public WebFragmentDescriptor getDescriptor() {
        // no default bundle for web-fragment
        if (descriptor==null) {
            descriptor = new WebFragmentDescriptor();
        }
        return descriptor;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param webFragmentDesc the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent,
        WebFragmentDescriptor webFragmentDesc) {
        Node jarNode = super.writeDescriptor(parent, webFragmentDesc);
        if (webFragmentDesc.getOrderingDescriptor() != null) {
            OrderingNode orderingNode = new OrderingNode();
            orderingNode.writeDescriptor(jarNode, WebTagNames.ORDERING,
                    webFragmentDesc.getOrderingDescriptor());
        }
        return jarNode;
    }


}
