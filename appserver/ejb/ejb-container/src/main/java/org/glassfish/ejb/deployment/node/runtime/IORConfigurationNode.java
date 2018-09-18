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

/*
 * IORConfigurationNode.java
 *
 * Created on March 12, 2002, 9:51 AM
 */

package org.glassfish.ejb.deployment.node.runtime;

import java.util.Map;

import com.sun.enterprise.deployment.EjbIORConfigurationDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.w3c.dom.Node;

/**
 * This node handles all EJB IOR Configuration information
 *
 * @author Jerome Dochez
 * @version 
 */
public class IORConfigurationNode extends DeploymentDescriptorNode<EjbIORConfigurationDescriptor> {

    private EjbIORConfigurationDescriptor descriptor;

    @Override
    public EjbIORConfigurationDescriptor getDescriptor() {
        if (descriptor == null) descriptor = new EjbIORConfigurationDescriptor();
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {    
        Map table = super.getDispatchTable();
        
        // transport-config
        table.put(RuntimeTagNames.INTEGRITY, "setIntegrity");
        table.put(RuntimeTagNames.CONFIDENTIALITY, "setConfidentiality");
        table.put(RuntimeTagNames.ESTABLISH_TRUST_IN_TARGET, "setEstablishTrustInTarget");
        table.put(RuntimeTagNames.ESTABLISH_TRUST_IN_CLIENT, "setEstablishTrustInClient");
        
        // as-context
        table.put(RuntimeTagNames.AUTH_METHOD, "setAuthenticationMethod");
        table.put(RuntimeTagNames.REALM, "setRealmName");
        table.put(RuntimeTagNames.REQUIRED, "setAuthMethodRequired");
        
        // sas-context
        table.put(RuntimeTagNames.CALLER_PROPAGATION, "setCallerPropagation");
        
        return table;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, EjbIORConfigurationDescriptor iorDesc) {    
        Node iorNode = appendChild(parent, nodeName);
        Node transportNode = appendChild(iorNode, RuntimeTagNames.TRANSPORT_CONFIG);

        appendTextChild(transportNode, RuntimeTagNames.INTEGRITY, iorDesc.getIntegrity());
        appendTextChild(transportNode, RuntimeTagNames.CONFIDENTIALITY, iorDesc.getConfidentiality());
        appendTextChild(transportNode, RuntimeTagNames.ESTABLISH_TRUST_IN_TARGET, 
                        iorDesc.getEstablishTrustInTarget());
        appendTextChild(transportNode, RuntimeTagNames.ESTABLISH_TRUST_IN_CLIENT,
                        iorDesc.getEstablishTrustInClient());

        // These two sub-elements should only be added if needed.
        Node asContextNode = appendChild(iorNode, RuntimeTagNames.AS_CONTEXT);        
        appendTextChild(asContextNode, RuntimeTagNames.AUTH_METHOD, iorDesc.getAuthenticationMethod());
        appendTextChild(asContextNode, RuntimeTagNames.REALM, iorDesc.getRealmName());
        appendTextChild(asContextNode, RuntimeTagNames.REQUIRED,
                        Boolean.valueOf(iorDesc.isAuthMethodRequired()).toString());

        Node sasContextNode = appendChild(iorNode, RuntimeTagNames.SAS_CONTEXT);
        appendTextChild(sasContextNode, RuntimeTagNames.CALLER_PROPAGATION, iorDesc.getCallerPropagation());   
        return iorNode;
    }
}
