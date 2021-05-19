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

import org.jvnet.hk2.annotations.Contract;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
 * This interface defines the protocol associated with all the nodes. An
 * XML node is responsible for reading the XML file  into a object
 * representation
 *
 * @author  Jerome Dochez
 * @version
 */
@Contract
public interface XMLNode<T> {

    /**
     * notification of the start of an XML element tag in the processed
     * XML source file.
     *
     * @param element the XML element type name
     * @param attributes the specified or defaultted attritutes
     */
    public void startElement(XMLElement element, Attributes attributes);

    /**
     * sets the value of an XML element
     *
     * @param element the XML element type name
     * @param value the element value
     */
    public void setElementValue(XMLElement element, String value);

    /**
     * notification of the end of an XML element in the source XML
     * file.
     *
     * @param element the XML element type name
     * @return true if this node is done with the processing of elements
     * in the processing
     */
    public boolean endElement(XMLElement element);

    /**
     * Return true if the XMLNode is responisble for handling the
     * XML element
     *
     * @param element the XML element type name
     * @return true if the node processes this element name
     */
    public boolean handlesElement(XMLElement element);

    /**
     * Return the XMLNode implementation respionsible for
     * handling the sub-element of the current node
     *
     * @param element the XML element type name
     * @return XMLNode implementation responsible for handling
     * the XML tag
     */
    public XMLNode getHandlerFor(XMLElement element);

    /**
     * @return the parent node for this XMLNode
     */
    public XMLNode getParentNode();

    /**
     * @return the root node for this XMLNode
     */
    public XMLNode getRootNode();

    /**
     * @return the XMLPath for the element name this node
     * is handling. The XML path can be a absolute or a
     * relative XMLPath.
     */
    public String getXMLPath();

    /**
     * @return the Descriptor subclass that was populated  by reading
     * the source XML file
     */
    public T getDescriptor();

    /**
     * Add a new descriptor to the current descriptor associated with
     * this node. This method is usually called by sub XMLNodes
     * (Returned by getHandlerFor) to add the result of their parsing
     * to the main descriptor.
     *
     * @param descriptor the new descriptor to be added to the current
     * descriptor.
     */
    public void addDescriptor(Object descriptor);

    /**
     * write the descriptor to an JAXP DOM node and return it
     *
     * @param parent node in the DOM tree
     * @param descriptor the descriptor to be written
     * @return the JAXP DOM node for this descriptor
     */
    public Node writeDescriptor(Node parent, T descriptor);

    /**
     * notify of a new prefix mapping used from this node
     */
    public void addPrefixMapping(String prefix, String uri);

    /**
     * Resolve a QName prefix to its corresponding Namespace URI by
     * searching up node chain starting with the child.
     */
    public String resolvePrefix(XMLElement element, String prefix);
}

