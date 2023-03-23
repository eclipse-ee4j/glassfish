/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.util.DOLUtils;

import java.io.OutputStream;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class is responsible for producing DOM document instances from
 * the descriptor classes
 *
 * @author Jerome Dochez
 */
public class J2EEDocumentBuilder {

    /** Creates new J2EEDocumentBuilder */
    public J2EEDocumentBuilder() {
    }

    /**
     * Creates and Return a new DOM document based on the current
     * configuration
     *
     * @return the new DOM Document object
     */
    public static Document newDocument() {
        try {
            // always use system default, see IT 8229
            ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(J2EEDocumentBuilder.class.getClassLoader());
            DocumentBuilderFactory factory = null;
            try {
                factory = DocumentBuilderFactory.newInstance();
            } finally {
                Thread.currentThread().setContextClassLoader(currentLoader);
            }
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            return document;
        } catch (Exception e) {
            DOLUtils.getDefaultLogger().log(Level.SEVERE, DOLUtils.INVALILD_DESCRIPTOR_SHORT, e.getMessage());
            DOLUtils.getDefaultLogger().log(Level.WARNING, "Error occurred", e);
            return null;
        }
    }

    /**
     * Return a document containing a result node based
     * on the given result descriptor.
     */
    public static Document getDocument(Descriptor descriptor, XMLNode node) {
        try {
            Node domNode = node.writeDescriptor(newDocument(), descriptor);
            if (domNode instanceof Document) {
                return (Document) domNode;
            }
            return domNode.getOwnerDocument();
        } catch (Exception e) {
            DOLUtils.getDefaultLogger().log(Level.WARNING, "Error occurred", e);
            return null;
        }
    }


    public static void write(Descriptor descriptor, final RootXMLNode node, final OutputStream os) throws Exception {
        Result output = new StreamResult(os);
        write(descriptor, node, output);
    }

    public static void write(Descriptor descriptor, final RootXMLNode node, final Result output) throws Exception {
        if (node == null) {
            DOLUtils.getDefaultLogger().log(Level.SEVERE, "Root XML node is null for descriptor: " + descriptor);
            return;
        }
        Document document = getDocument(descriptor, node);
        Source source = new DOMSource(document);
        Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
        setTransformerProperties(node, transformer);
        transformer.transform(source, output);
    }

    private static void setTransformerProperties (RootXMLNode node, Transformer transformer) {
        if (node.getDocType() != null) {
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, node.getDocType());
            if (node.getSystemID() != null) {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, node.getSystemID());
            }
        }
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    }


    public static String descriptorToString(Descriptor descriptor, final DeploymentDescriptorFile ddFile)
        throws Exception {
        java.io.StringWriter sw = new java.io.StringWriter();
        StreamResult sr = new StreamResult(sw);
        if (descriptor != null) {
            write(descriptor, ddFile.getRootXMLNode(descriptor), sr);
        }
        return sw.toString();
    }
}
