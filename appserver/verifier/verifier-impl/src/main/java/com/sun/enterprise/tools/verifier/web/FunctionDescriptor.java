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

package com.sun.enterprise.tools.verifier.web;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * @author Sudipto Ghosh
 */
public class FunctionDescriptor {
    Node node;
    public FunctionDescriptor(Node n){
        this.node = n;
    }

    public String getFunctionClass(){
        NodeList n1 = node.getChildNodes();
        int i = 0;
        String className = null;
        for (int k = 0; k < n1.getLength(); k++) {
            String name = n1.item(k).getNodeName();
            if (name == "function-class") { // NOI18N
                className = n1.item(k).getFirstChild().getNodeValue();
            }
        }
        return className;
    }

    public String getFunctionSignature() {
        NodeList n1 = node.getChildNodes();
        int i = 0;
        String signature = null;
        for (int k = 0; k < n1.getLength(); k++) {
            String name = n1.item(k).getNodeName();
            if (name == "function-signature") // NOI18N
                signature = n1.item(k).getFirstChild().getNodeValue();
        }
        return signature;
    }

}
