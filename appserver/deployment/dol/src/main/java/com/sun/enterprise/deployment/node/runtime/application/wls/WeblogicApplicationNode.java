/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.node.runtime.application.wls;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.runtime.application.wls.ApplicationParam;
import com.sun.enterprise.deployment.node.runtime.RuntimeBundleNode;
import com.sun.enterprise.deployment.node.DataSourceNameVersionUpgrade;
import com.sun.enterprise.deployment.node.StartMdbsWithApplicationVersionUpgrade;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * This node is responsible for handling all WebLogic runtime information for
 * application.
 */
public class WeblogicApplicationNode extends RuntimeBundleNode<Application> {

    public final static String SCHEMA_ID = "weblogic-application.xsd";

    private final static List<String> systemIDs = initSystemIDs();

    private static List<String> initSystemIDs() {
        List<String> systemIDs = new ArrayList<>();
        systemIDs.add(SCHEMA_ID);
        return Collections.unmodifiableList(systemIDs);
    }

    public final static String PUBLIC_DTD_ID_2 = "-//BEA Systems, Inc.//DTD WebLogic Application 8.1.0//EN";
    public final static String SYSTEM_ID_2 = "http://www.beasys.com/servers/wls810/dtd/weblogic-application_2_0.dtd";

    /** Creates new WeblogicApplicationNode */
    public WeblogicApplicationNode(Application descriptor) {
        super(descriptor);
    }

    /** Creates new WeblogicApplicationNode */
    public WeblogicApplicationNode() {
        super(null);
    }

    /**
     * Initialize the child handlers
     */
    @Override
    protected void init() {
        super.init();
        registerElementHandler(new XMLElement(
                RuntimeTagNames.APPLICATION_PARAM), ApplicationParamNode.class);
    }

   /**
    * register this node as a root node capable of loading entire DD files
    *
    * @param publicIDToDTD is a mapping between xml Public-ID to DTD
    * @param versionUpgrades The list of upgrades from older versions
    * @return the doctype tag name
    */
    public static String registerBundle(Map publicIDToDTD,
                                        Map<String, List<Class>> versionUpgrades) {
        // TODO: fill in all the previously supported DTD versions
        // for backward compatibility
        publicIDToDTD.put(PUBLIC_DTD_ID_2, SYSTEM_ID_2);
        List<Class> list = new ArrayList<>();
        list.add(DataSourceNameVersionUpgrade.class);
        list.add(StartMdbsWithApplicationVersionUpgrade.class);
        versionUpgrades.put(RuntimeTagNames.WLS_APPLICATION_RUNTIME_TAG,
                            list);
        return RuntimeTagNames.WLS_APPLICATION_RUNTIME_TAG;
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    @Override
    protected XMLElement getXMLRootTag() {
        return new XMLElement(RuntimeTagNames.WLS_APPLICATION_RUNTIME_TAG);
    }

    /**
     * @return the DOCTYPE that should be written to the XML file
     */
    @Override
    public String getDocType() {
        return null;
    }

    /**
     * @return the SystemID of the XML file
     */
    @Override
    public String getSystemID() {
        return SCHEMA_ID;
    }

    /**
     * @return the list of SystemID of the XML schema supported
     */
    @Override
    public List<String> getSystemIDs() {
        return systemIDs;
    }

   /**
    * @return the application instance to associate with this XMLNode
    */
    @Override
    public Application getDescriptor() {
        return descriptor;
    }

    /**
     * Adds  a new DOL descriptor instance to the descriptor instance
     * associated with this XMLNode
     *
     * @param newDescriptor the new descriptor
     */
    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof EnvironmentProperty) {
            descriptor.addApplicationParam((ApplicationParam)newDescriptor);
        } else {
            super.addDescriptor(newDescriptor);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName the node name
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, Application application) {
        Element root = appendChildNS(parent, getXMLRootTag().getQName(),
                    TagNames.WLS_APPLICATION_NAMESPACE);
        root.setAttributeNS(TagNames.XMLNS,
                            TagNames.XMLNS_XSI,
                            TagNames.W3C_XML_SCHEMA_INSTANCE);
        root.setAttributeNS(TagNames.W3C_XML_SCHEMA_INSTANCE,
                            TagNames.SCHEMA_LOCATION_TAG,
                            TagNames.WLS_APPLICATION_SCHEMA_LOCATION);
        root.setAttribute(TagNames.VERSION, getSpecVersion());

        writeSubDescriptors(root, nodeName, application);

        return root;

    }
}
