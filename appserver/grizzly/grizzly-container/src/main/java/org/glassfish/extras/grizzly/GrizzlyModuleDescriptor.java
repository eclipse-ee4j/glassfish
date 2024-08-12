/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.extras.grizzly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Descriptor for a grizzly application.
 *
 * @author Jerome Dochez
 */
public class GrizzlyModuleDescriptor {

    private final static String[] HANDLER_ELEMENTS = {"adapter", "http-handler"};
    final static String DescriptorPath = "META-INF/grizzly-glassfish.xml";
    final Map<String, String> tuples = new HashMap<String, String>();
    final Map<String, ArrayList<GrizzlyProperty>> adapterProperties = new HashMap<String,  ArrayList<GrizzlyProperty>>();

    GrizzlyModuleDescriptor(ReadableArchive source, Logger logger) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            parse(factory.newDocumentBuilder().parse(source.getEntry(DescriptorPath)));
        } catch (SAXException e) {
            logger.log(Level.SEVERE, e.getMessage(),e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(),e);
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            logger.log(Level.SEVERE, e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    private void parse(Document document) {
        Element element = document.getDocumentElement();
        for (String handlerElement : HANDLER_ELEMENTS) {
            NodeList adapters = element.getElementsByTagName(handlerElement);
            for (int i=0;i<adapters.getLength();i++) {
                Node adapter = adapters.item(i);
                NamedNodeMap attrs = adapter.getAttributes();
                NodeList properties = adapter.getChildNodes();
                ArrayList<GrizzlyProperty> list = new ArrayList<GrizzlyProperty>();

                // Read the properties to be set on a GrizzlyAdapter
                for (int j=0; j < properties.getLength(); j++){
                    Node property = properties.item(j);
                    NamedNodeMap values = property.getAttributes();
                   if (values != null){
                        list.add(new GrizzlyProperty(values.getNamedItem("name").getNodeValue(),
                                              values.getNamedItem("value").getNodeValue()));
                    }
                }

                adapterProperties.put(attrs.getNamedItem("class-name").getNodeValue(), list);
                addAdapter(attrs.getNamedItem("context-root").getNodeValue(),
                        attrs.getNamedItem("class-name").getNodeValue());
            }
        }
    }

    public void addAdapter(String contextRoot, String className) {
        if (tuples.containsKey(contextRoot)) {
            throw new RuntimeException("duplicate context root in configuration :" + contextRoot);
        }
        tuples.put(contextRoot, className);
    }

    public Map<String, String> getAdapters() {
        return tuples;
    }

    static class GrizzlyProperty{

        String name ="";
        String value = "";

        public GrizzlyProperty(String name, String value) {
            this.name = name;
            this.value = value;
        }

    }

    /**
     * Return the properties to be set on {@link Adapter}
     */
    Map<String,ArrayList<GrizzlyProperty>> getProperties(){
        return adapterProperties;
    }
}
