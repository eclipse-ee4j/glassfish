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

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.util.DOLUtils;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;

/**
 * This class is responsible for producing DOM document instances from
 * the descriptor classes
 *
 * @author  Jerome Dochez
 * @version
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
            ClassLoader currentLoader =
                Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(
                J2EEDocumentBuilder.class.getClassLoader());
            DocumentBuilderFactory factory = null;
            try {
                factory = DocumentBuilderFactory.newInstance();
            } finally {
                Thread.currentThread().setContextClassLoader(currentLoader);
            }

            DocumentBuilder builder = factory.newDocumentBuilder();

            DOMImplementation domImplementation =
                builder.getDOMImplementation();

            Document document = builder.newDocument();
            return document;
        } catch (Exception e) {
            DOLUtils.getDefaultLogger().log(Level.SEVERE, "enterprise.deployment.backend.saxParserError",
                    new Object[] {"JAXP configuration error"});
            DOLUtils.getDefaultLogger().log(Level.WARNING, "Error occurred", e);
        }
        return null;
    }

    /**
     * Return a document containing a result node based
     * on the given result descriptor.
     */
    public static Document getDocument(Descriptor descriptor, XMLNode node) {
        try {
            Node domNode = node.writeDescriptor(newDocument(), descriptor);
            if (domNode instanceof Document)
                return (Document) domNode;
            else
                return domNode.getOwnerDocument();
        } catch (Exception e) {
            DOLUtils.getDefaultLogger().log(Level.WARNING, "Error occurred", e);
        }
        return null;
    }

    public static void write (Descriptor descriptor, final RootXMLNode node,  final File resultFile) throws Exception {
        if (node==null) {
            DOLUtils.getDefaultLogger().log(Level.SEVERE, DOLUtils.INVALID_DESC_MAPPING,
                new Object[] {descriptor, null});
            return;
        }
        if (resultFile.getParent() != null) {
            File f = new File(resultFile.getParent());
            if (!f.isDirectory() && !f.mkdirs())
                throw new IOException("Cannot create parent directory " + f.getAbsolutePath());
        }
        FileOutputStream out = new FileOutputStream(resultFile);
        try {
            write(descriptor, node, out);
        } finally {
            out.close();
        }
    }

    public static void write (Descriptor descriptor, final RootXMLNode node,  final OutputStream os) throws Exception {
        Result output = new StreamResult(os);
        write(descriptor, node, output);
    }

    public static void write (Descriptor descriptor, final RootXMLNode node,  final Result output)
                                throws Exception {
        if (node==null) {
            DOLUtils.getDefaultLogger().log(Level.SEVERE, DOLUtils.INVALID_DESC_MAPPING,
                new Object[] {descriptor, null});
            return;
        }
        try {
            Document document = getDocument(descriptor, node);
            Source source = new DOMSource(document);
            Transformer transformer = getTransformer();
            setTransformerProperties(node, transformer);
            transformer.transform(source, output);
        } catch(Exception e) {
            DOLUtils.getDefaultLogger().log(Level.WARNING, "Error occurred", e);
            throw e;
        }
    }

    private static Transformer getTransformer() throws Exception {
        //get current TCL and system property values
        ClassLoader currentTCL = Thread.currentThread().getContextClassLoader();
        String userTransformerFactory = System.getProperty("javax.xml.transform.TransformerFactory");

        Transformer transformer = null;
        try {
            //Set TCL to system classloader, and clear TransformerProperty
            //so that we obtain the VM default transformer factory from
            //the TransformerFactory
            Thread.currentThread().setContextClassLoader(
                J2EEDocumentBuilder.class.getClassLoader());
            if (userTransformerFactory != null) {
                System.clearProperty("javax.xml.transform.TransformerFactory");
            }

            //get the VM default transformer factory and use that for DOL
            //processing
            transformer = TransformerFactory.newInstance().newTransformer();
        } finally {
            //reset thread context classloader and system property to their
            //original values
            Thread.currentThread().setContextClassLoader(currentTCL);
            if (userTransformerFactory != null) {
                System.setProperty("javax.xml.transform.TransformerFactory", userTransformerFactory);
            }
        }
        return transformer;
    }

    private static void setTransformerProperties (RootXMLNode node, Transformer transformer) {
        if (node.getDocType()!=null) {
            transformer.setOutputProperty(
                OutputKeys.DOCTYPE_PUBLIC, node.getDocType());
            if (node.getSystemID()!=null) {
                transformer.setOutputProperty(
                    OutputKeys.DOCTYPE_SYSTEM, node.getSystemID());
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
