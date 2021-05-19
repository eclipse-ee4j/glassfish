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

package com.sun.enterprise.deployment.node.runtime.application.gf;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.DTDRegistry;

import java.util.Map;


/**
 * This node is responsible for handling all runtime information for
 * application.
 */
public class GFApplicationRuntimeNode extends ApplicationRuntimeNode {

    public GFApplicationRuntimeNode(Application descriptor) {
        super(descriptor);
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    protected XMLElement getXMLRootTag() {
        return new XMLElement(RuntimeTagNames.GF_APPLICATION_RUNTIME_TAG);
    }

    /**
     * @return the DOCTYPE that should be written to the XML file
     */
    public String getDocType() {
        return DTDRegistry.GF_APPLICATION_601_DTD_PUBLIC_ID;
    }

    /**
     * @return the SystemID of the XML file
     */
    public String getSystemID() {
        return DTDRegistry.GF_APPLICATION_601_DTD_SYSTEM_ID;
    }

   /**
    * register this node as a root node capable of loading entire DD files
    *
    * @param publicIDToDTD is a mapping between xml Public-ID to DTD
    * @return the doctype tag name
    */
   public static String registerBundle(Map publicIDToDTD) {
       publicIDToDTD.put(DTDRegistry.GF_APPLICATION_601_DTD_PUBLIC_ID, DTDRegistry.GF_APPLICATION_601_DTD_SYSTEM_ID);
       return RuntimeTagNames.GF_APPLICATION_RUNTIME_TAG;
   }
}
