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

package org.glassfish.ejb.deployment.node.runtime;

import com.sun.enterprise.deployment.EjbIORConfigurationDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;

import java.util.Map;

import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.RuntimeTagNames.AS_CONTEXT;
import static com.sun.enterprise.deployment.xml.RuntimeTagNames.AUTH_METHOD;
import static com.sun.enterprise.deployment.xml.RuntimeTagNames.CALLER_PROPAGATION;
import static com.sun.enterprise.deployment.xml.RuntimeTagNames.CONFIDENTIALITY;
import static com.sun.enterprise.deployment.xml.RuntimeTagNames.ESTABLISH_TRUST_IN_CLIENT;
import static com.sun.enterprise.deployment.xml.RuntimeTagNames.ESTABLISH_TRUST_IN_TARGET;
import static com.sun.enterprise.deployment.xml.RuntimeTagNames.INTEGRITY;
import static com.sun.enterprise.deployment.xml.RuntimeTagNames.REALM;
import static com.sun.enterprise.deployment.xml.RuntimeTagNames.REQUIRED;
import static com.sun.enterprise.deployment.xml.RuntimeTagNames.SAS_CONTEXT;
import static com.sun.enterprise.deployment.xml.RuntimeTagNames.TRANSPORT_CONFIG;

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
        if (descriptor == null) {
            descriptor = new EjbIORConfigurationDescriptor();
        }
        return descriptor;
    }

    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();

        // transport-config
        table.put(INTEGRITY, "setIntegrity");
        table.put(CONFIDENTIALITY, "setConfidentiality");
        table.put(ESTABLISH_TRUST_IN_TARGET, "setEstablishTrustInTarget");
        table.put(ESTABLISH_TRUST_IN_CLIENT, "setEstablishTrustInClient");

        // as-context
        table.put(AUTH_METHOD, "setAuthenticationMethod");
        table.put(REALM, "setRealmName");
        table.put(REQUIRED, "setAuthMethodRequired");

        // sas-context
        table.put(CALLER_PROPAGATION, "setCallerPropagation");

        return table;
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, EjbIORConfigurationDescriptor iorDesc) {
        Node iorNode = appendChild(parent, nodeName);
        Node transportNode = appendChild(iorNode, TRANSPORT_CONFIG);

        appendTextChild(transportNode, INTEGRITY, iorDesc.getIntegrity());
        appendTextChild(transportNode, CONFIDENTIALITY, iorDesc.getConfidentiality());
        appendTextChild(transportNode, ESTABLISH_TRUST_IN_TARGET, iorDesc.getEstablishTrustInTarget());
        appendTextChild(transportNode, ESTABLISH_TRUST_IN_CLIENT, iorDesc.getEstablishTrustInClient());

        // These two sub-elements should only be added if needed.
        Node asContextNode = appendChild(iorNode, AS_CONTEXT);
        appendTextChild(asContextNode, AUTH_METHOD, iorDesc.getAuthenticationMethod());
        appendTextChild(asContextNode, REALM, iorDesc.getRealmName());
        appendTextChild(asContextNode, REQUIRED, Boolean.valueOf(iorDesc.isAuthMethodRequired()).toString());

        Node sasContextNode = appendChild(iorNode, SAS_CONTEXT);
        appendTextChild(sasContextNode, CALLER_PROPAGATION, iorDesc.getCallerPropagation());
        return iorNode;
    }
}
