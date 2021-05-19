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

package org.glassfish.ejb.deployment.node.runtime;

import java.util.List;
import java.util.Map;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeBundleNode;
import com.sun.enterprise.deployment.node.runtime.common.SecurityRoleMappingNode;
import com.sun.enterprise.deployment.runtime.common.PrincipalNameDescriptor;
import com.sun.enterprise.deployment.runtime.common.SecurityRoleMapping;
import com.sun.enterprise.deployment.xml.DTDRegistry;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.glassfish.deployment.common.SecurityRoleMapper;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.node.EjbBundleNode;
import org.glassfish.security.common.Group;
import org.glassfish.security.common.Role;
import org.w3c.dom.Node;

/**
 * This node handles runtime deployment descriptors for ejb bundle
 *
 * @author  Jerome Dochez
 * @version
 */
public class EjbBundleRuntimeNode extends
        RuntimeBundleNode<EjbBundleDescriptorImpl> {

    public EjbBundleRuntimeNode(EjbBundleDescriptorImpl descriptor) {
        super(descriptor);
        //trigger registration in standard node, if it hasn't happened
        habitat.getService(EjbBundleNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.SECURITY_ROLE_MAPPING),
                SecurityRoleMappingNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.EJBS),
                EnterpriseBeansRuntimeNode.class);
    }

    public EjbBundleRuntimeNode() {
        this(null);
    }

    @Override
    public String getDocType() {
        return DTDRegistry.SUN_EJBJAR_310_DTD_PUBLIC_ID;
    }

    @Override
    public String getSystemID() {
        return DTDRegistry.SUN_EJBJAR_310_DTD_SYSTEM_ID;
    }

    /**
     * @return NULL for all runtime nodes.
     */
    @Override
    public List<String> getSystemIDs() {
        return null;
    }

    @Override
    protected XMLElement getXMLRootTag() {
        return new XMLElement(RuntimeTagNames.S1AS_EJB_RUNTIME_TAG);
    }

   /**
    * register this node as a root node capable of loading entire DD files
    *
    * @param publicIDToDTD is a mapping between xml Public-ID to DTD
    * @return the doctype tag name
    */
   public static String registerBundle(Map publicIDToDTD) {
       publicIDToDTD.put(DTDRegistry.SUN_EJBJAR_200_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_200_DTD_SYSTEM_ID);
       publicIDToDTD.put(DTDRegistry.SUN_EJBJAR_201_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_201_DTD_SYSTEM_ID);
       publicIDToDTD.put(DTDRegistry.SUN_EJBJAR_210_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_210_DTD_SYSTEM_ID);
       publicIDToDTD.put(DTDRegistry.SUN_EJBJAR_211_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_211_DTD_SYSTEM_ID);
       publicIDToDTD.put(DTDRegistry.SUN_EJBJAR_300_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_300_DTD_SYSTEM_ID);
       publicIDToDTD.put(DTDRegistry.SUN_EJBJAR_310_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_310_DTD_SYSTEM_ID);

       if (!restrictDTDDeclarations()) {
           publicIDToDTD.put(DTDRegistry.SUN_EJBJAR_210beta_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_210beta_DTD_SYSTEM_ID);
       }
       return RuntimeTagNames.S1AS_EJB_RUNTIME_TAG;
   }

    @Override
    public EjbBundleDescriptorImpl getDescriptor() {
        return descriptor;
    }

    @Override
    public void setElementValue(XMLElement element, String value) {
        if (element.getQName().equals(RuntimeTagNames.COMPATIBILITY)) {
            descriptor.setCompatibility(value);
        } else if (element.getQName().equals(RuntimeTagNames.DISABLE_NONPORTABLE_JNDI_NAMES)) {
            descriptor.setDisableNonportableJndiNames(value);
        } else if (element.getQName().equals(RuntimeTagNames.KEEP_STATE)) {
            descriptor.setKeepState(value);
        } else if (element.getQName().equals(RuntimeTagNames.VERSION_IDENTIFIER)) {
        } else {
            super.setElementValue(element, value);
        }
    }

    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof SecurityRoleMapping) {
            SecurityRoleMapping roleMap = (SecurityRoleMapping)newDescriptor;
            descriptor.addSecurityRoleMapping(roleMap);
            Application app = descriptor.getApplication();
            if (app!=null) {
                Role role = new Role(roleMap.getRoleName());
                SecurityRoleMapper rm = app.getRoleMapper();
                if (rm != null) {
                    List<PrincipalNameDescriptor> principals = roleMap.getPrincipalNames();
                    for (int i = 0; i < principals.size(); i++) {
                        rm.assignRole(principals.get(i).getPrincipal(),
                            role, descriptor);
                    }
                    List<String> groups = roleMap.getGroupNames();
                    for (int i = 0; i < groups.size(); i++) {
                        rm.assignRole(new Group(groups.get(i)),
                            role, descriptor);
                    }
                }
            }
        }
    }

    @Override
    public Node writeDescriptor(Node parent, EjbBundleDescriptorImpl bundleDescriptor) {
        Node ejbs = super.writeDescriptor(parent, bundleDescriptor);

        // security-role-mapping*
        List<SecurityRoleMapping> roleMappings = bundleDescriptor.getSecurityRoleMappings();
        for (int i = 0; i < roleMappings.size(); i++) {
            SecurityRoleMappingNode srmn = new SecurityRoleMappingNode();
            srmn.writeDescriptor(ejbs, RuntimeTagNames.SECURITY_ROLE_MAPPING, roleMappings.get(i));
        }

        // entreprise-beans
        EnterpriseBeansRuntimeNode ejbsNode = new EnterpriseBeansRuntimeNode();
        ejbsNode.writeDescriptor(ejbs, RuntimeTagNames.EJBS, bundleDescriptor);

        // compatibility
        appendTextChild(ejbs, RuntimeTagNames.COMPATIBILITY, bundleDescriptor.getCompatibility());

        //disable-nonportable-jndi-names
        Boolean djndi = bundleDescriptor.getDisableNonportableJndiNames();
        if (djndi != null) {
            appendTextChild(ejbs, RuntimeTagNames.DISABLE_NONPORTABLE_JNDI_NAMES, String.valueOf(djndi));
        }

        // keep-state
        appendTextChild(ejbs, RuntimeTagNames.KEEP_STATE, String.valueOf(bundleDescriptor.getKeepState()));

        return ejbs;
    }
}
