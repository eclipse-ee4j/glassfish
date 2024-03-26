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

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.MessageDestinationDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class defines all the common behaviour among nodes responsibles for handling bundles
 *
 * @param <T> {@link RootDeploymentDescriptor} type
 *
 * @author Jerome Dochez
 */
public abstract class AbstractBundleNode<T extends RootDeploymentDescriptor>
        extends DisplayableComponentNode<T> implements BundleNode, RootXMLNode<T> {

    protected static final String W3C_XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
    protected static final String SCHEMA_LOCATION_TAG = "xsi:schemaLocation";

    private String docType;

    /**
     * set the DOCTYPE as read in the input XML File
     * @param docType for the xml
     */
    @Override
    public void setDocType(String docType) {
        this.docType = docType;
        setSpecVersion();
    }


    public static Element appendChildNS(Node parent, String elementName, String nameSpace) {
        Element child = getOwnerDocument(parent).createElementNS(nameSpace, elementName);
        parent.appendChild(child);
        return child;
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
        dispatchTable.put(TagNames.DISPLAY_NAME, "setDisplayName");
        dispatchTable.put(TagNames.VERSION, "setSpecVersion");
        return dispatchTable;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        if (!DOLUtils.setElementValue(element, value, getDescriptor())) {
            super.setElementValue(element, value);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, T descriptor) {
        Node bundleNode;
        if (getDocType() == null) {
            // we are using schemas for this DDs
            bundleNode = appendChildNS(parent, getXMLRootTag().getQName(), TagNames.JAKARTAEE_NAMESPACE);
            addBundleNodeAttributes((Element) bundleNode, descriptor);
        } else {
            bundleNode = appendChild(parent, getXMLRootTag().getQName());
        }
        appendTextChild(bundleNode, topLevelTagName(), topLevelTagValue(descriptor));

        // description, display-name, icons...
        writeDisplayableComponentInfo(bundleNode, descriptor);
        return bundleNode;
    }

    /**
     * Gives the element (tag) name to be used for the top-level element of
     * descriptors corresponding to this bundle node type.
     *
     * @return top-level element name for the descriptor
     */
    protected String topLevelTagName() {
        return TagNames.MODULE_NAME;
    }

    /**
     * Gives the text value to be used for the top-level element in the descriptor
     * corresponding to this bundle node type.
     *
     * @param descriptor descriptor data structure for the current node
     */
    protected String topLevelTagValue(T descriptor) {
        return descriptor.getModuleDescriptor().getModuleName();
    }

    @Override
    public Collection<String> elementsAllowingEmptyValue() {
        return Collections.emptySet();
    }

    @Override
    public Collection<String> elementsPreservingWhiteSpace() {
        return Collections.emptySet();
    }


    protected void writeMessageDestinations(Node parentNode, Iterator msgDestinations) {
        if (msgDestinations == null || !msgDestinations.hasNext()) {
            return;
        }

        MessageDestinationNode subNode = new MessageDestinationNode();
        while (msgDestinations.hasNext()) {
            MessageDestinationDescriptor next = (MessageDestinationDescriptor) msgDestinations.next();
            subNode.writeDescriptor(parentNode, TagNames.MESSAGE_DESTINATION, next);
        }
    }

    /**
     * write the necessary attributes for the root node of this DDs document
     */
    protected void addBundleNodeAttributes(Element bundleNode, RootDeploymentDescriptor descriptor) {
        String schemaLocation = TagNames.JAVAEE_NAMESPACE + " " + getSchemaURL();
        bundleNode.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", W3C_XML_SCHEMA_INSTANCE);

        // add all custom global namespaces
        addNamespaceDeclaration(bundleNode, descriptor);

        bundleNode.setAttributeNS(W3C_XML_SCHEMA_INSTANCE, SCHEMA_LOCATION_TAG, schemaLocation);
        bundleNode.setAttribute(TagNames.VERSION, getSpecVersion());

        // Write out full attribute for DD which allows annotations.
        // The full attribute should always be written out as true since
        // when we come here to write it out, the annotation information
        // has already been processed and saved in DD, so written out DD
        // is always a full DD.
        if (descriptor instanceof BundleDescriptor && !(descriptor instanceof Application)) {
            BundleDescriptor bundleDesc = (BundleDescriptor) descriptor;
            // In the common case that metadata-complete isn't already set to
            // true, set it to true.
            if (! bundleDesc.isDDWithNoAnnotationAllowed()) {
                bundleNode.setAttribute(TagNames.METADATA_COMPLETE, "true");
            }
        }
    }

    /**
     * notify of a new prefix mapping used in this document
     */
    @Override
    public void addPrefixMapping(String prefix, String uri) {
        // we don't care about the default ones...
        if (uri.equals(TagNames.J2EE_NAMESPACE)) {
            return;
        }
        if (uri.equals(TagNames.JAVAEE_NAMESPACE)) {
            return;
        }
        if (uri.equals(TagNames.JAKARTAEE_NAMESPACE)) {
            return;
        }
        if (uri.equals(W3C_XML_SCHEMA_INSTANCE)) {
            return;
        }
        super.addPrefixMapping(prefix, uri);
    }

    /**
     * @return the complete URL for JAKARTAEE schemas
     */
    protected String getSchemaURL() {
        // by default, it comes from our web site
        return TagNames.JAKARTAEE_NAMESPACE + "/" + getSystemID();
    }

    /**
     * Sets the specVersion for this descriptor depending on the docType
     */
    protected void setSpecVersion() {
        if (docType == null) {
            return;
        }
        StringTokenizer st = new StringTokenizer(docType, "//");
        while (st.hasMoreElements()) {
            String tmp = st.nextToken();
            if (tmp.startsWith("DTD")) {
                // this is the string we are interested in
                StringTokenizer versionST = new StringTokenizer(tmp);
                while (versionST.hasMoreElements()) {
                    String versionStr = versionST.nextToken();
                    try {
                        Float.valueOf(versionStr);
                        RootDeploymentDescriptor rdd = getDescriptor();
                        rdd.setSpecVersion(versionStr);
                        return;
                    } catch(NumberFormatException nfe) {
                        // ignore, this is just the other info of the publicID
                    }
                }
            }
        }
    }
}
