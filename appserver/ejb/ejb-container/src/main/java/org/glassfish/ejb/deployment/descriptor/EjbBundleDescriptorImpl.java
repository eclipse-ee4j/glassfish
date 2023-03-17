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

package org.glassfish.ejb.deployment.descriptor;

import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.RoleReference;
import com.sun.enterprise.deployment.runtime.common.SecurityRoleMapping;
import com.sun.enterprise.deployment.util.ComponentPostVisitor;
import com.sun.enterprise.deployment.util.ComponentVisitor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.util.EjbBundleVisitor;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.glassfish.deployment.common.DescriptorVisitor;
import org.glassfish.ejb.deployment.descriptor.runtime.IASPersistenceManagerDescriptor;
import org.glassfish.ejb.deployment.descriptor.runtime.PersistenceManagerInUse;
import org.glassfish.ejb.deployment.node.EjbBundleNode;
import org.glassfish.ejb.deployment.util.EjbBundleTracerVisitor;
import org.glassfish.ejb.deployment.util.EjbBundleValidator;
import org.glassfish.security.common.Role;

public final class EjbBundleDescriptorImpl extends EjbBundleDescriptor {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = DOLUtils.getLogger();

    private String ejbClientJarUri;

    // the resource (database) to be used for persisting CMP EntityBeans
    // the same resource is used for all beans in this ejb jar.
    private ResourceReferenceDescriptor cmpResourceReference;

    private final LinkedList<InterceptorBindingDescriptor> interceptorBindings = new LinkedList<>();
    private String relationshipsDescription;
    private final Set<RelationshipDescriptor> relationships = new HashSet<>();
    private final List<SecurityRoleMapping> roleMaps = new ArrayList<>();

    // non-descriptor runtime fields
    private Set<Long> ejbIDs;

    @Override
    public String getDefaultSpecVersion() {
        return EjbBundleNode.SPEC_VERSION;
    }


    /**
     * Returns a name or Ejb1 as a default.
     */
    @Override
    public String getName() {
        if ("".equals(super.getName())) {
            super.setName("Ejb1");
        }
        return super.getName();
    }


    /**
     * @return the empty String or the entry name of the ejb client JAR in my archive if I have one.
     */
    public String getEjbClientJarUri() {
        if (ejbClientJarUri == null) {
            ejbClientJarUri = "";
        }
        return ejbClientJarUri;
    }


    // Reflection in EjbBundleNode
    public void setEjbClientJarUri(String ejbClientJarUri) {
        this.ejbClientJarUri = ejbClientJarUri;
    }


    /**
     * @return the Resource I use for CMP.
     */
    public ResourceReferenceDescriptor getCMPResourceReference() {
        return cmpResourceReference;
    }


    /**
     * @param resourceReference the resource reference I use for CMP.
     */
    public void setCMPResourceReference(ResourceReferenceDescriptor resourceReference) {
        this.cmpResourceReference = resourceReference;
    }


    /**
     * @return list of {@link InterceptorBindingDescriptor}
     */
    public List<InterceptorBindingDescriptor> getInterceptorBindings() {
        return interceptorBindings;
    }


    /**
     * Adds the binding as the first in the internal list.
     *
     * @param binding
     */
    public void prependInterceptorBinding(InterceptorBindingDescriptor binding) {
        interceptorBindings.addFirst(binding);
    }


    /**
     * Adds the binding as the last in the internal list.
     *
     * @param binding
     */
    // Used by EjbBundleNode
    public void appendInterceptorBinding(InterceptorBindingDescriptor binding) {
        interceptorBindings.addLast(binding);
    }


    /**
     * Clears the internal list and adds all bindings in the same order.
     *
     * @param bindings
     */
    public void setInterceptorBindings(List<InterceptorBindingDescriptor> bindings) {
        interceptorBindings.clear();
        interceptorBindings.addAll(bindings);
    }


    /**
     * @return description for relationships element.
     */
    public String getRelationshipsDescription() {
        return relationshipsDescription;
    }


    /**
     * @param relationshipsDescription EJB2.0: set description for relationships element.
     */
    // Reflection in RelationshipsNode
    public void setRelationshipsDescription(String relationshipsDescription) {
        this.relationshipsDescription = relationshipsDescription;
    }


    /**
     * @return true if there are some definitions of relationships
     */
    public boolean hasRelationships() {
        return !relationships.isEmpty();
    }


    /**
     * Get all relationships in this ejb-jar.
     *
     * @return a Set of {@link RelationshipDescriptor}s.
     */
    public Set<RelationshipDescriptor> getRelationships() {
        return relationships;
    }


    /**
     * Add a RelationshipDescriptor which describes a CMR field between a bean/DO/entityRef
     * in this ejb-jar.
     *
     * @param relationship
     */
    public void addRelationship(RelationshipDescriptor relationship) {
        relationships.add(relationship);
    }


    /**
     * Remove a {@link RelationshipDescriptor}. Does nothing if it is not present.
     *
     * @param relationship
     */
    public void removeRelationship(RelationshipDescriptor relationship) {
        relationships.remove(relationship);
    }


    /**
     * Adds the mapping.
     *
     * @param roleMapping {@link SecurityRoleMapping}
     */
    public void addSecurityRoleMapping(SecurityRoleMapping roleMapping) {
        roleMaps.add(roleMapping);
    }


    /**
     * @return a list of {@link SecurityRoleMapping}s
     */
    public List<SecurityRoleMapping> getSecurityRoleMappings() {
        return roleMaps;
    }


    /**
     * Setup EJB Ids during deployment and shouldn't be called at runtime
     */
    public void setupDataStructuresForRuntime() {
        Set<Long> ids = new HashSet<>();
        for (EjbDescriptor ejbDescriptor : getEjbs()) {
            ids.add(ejbDescriptor.getUniqueId());
        }
        ejbIDs = Collections.unmodifiableSet(ids);
    }


    /**
     * @return Collection of unique ID of EJBs within the same module
     */
    public Collection<Long> getDescriptorIds() {
        return ejbIDs;
    }


    // temporary solution until we change the hierarchy to more consistent to be able to use getEjbs
    @Override
    public Set<EjbDescriptor> getEjbs() {
        return (Set<EjbDescriptor>) super.getEjbs();
    }


    @Override
    public EjbDescriptor getEjbByName(String name) {
        return (EjbDescriptor) super.getEjbByName(name);
    }


    @Override
    public EjbDescriptor getEjbByName(String name, boolean isCreateDummy) {
        return (EjbDescriptor) super.getEjbByName(name, isCreateDummy);
    }


    @Override
    protected DummyEjbDescriptor createDummyEjbDescriptor(String ejbName) {
        LOG.log(Level.DEBUG, "Construct a Dummy EJB Descriptor with name {0}", ejbName);
        DummyEjbDescriptor dummyEjbDesc = new DummyEjbDescriptor();
        dummyEjbDesc.setName(ejbName);
        return dummyEjbDesc;
    }


    /**
    * @return true if all ejb role references link to roles specified here.
    */
    public boolean areResourceReferencesValid() {
        // run through each of the ejb's role references, checking that the roles exist in this bundle
        for (EjbDescriptor ejbDescriptor : getEjbs()) {
            for (RoleReference element : ejbDescriptor.getRoleReferences()) {
                Role referredRole = element.getRole();
                Set<Role> roles = getRoles();
                if (!referredRole.getName().isEmpty() && !roles.contains(referredRole)) {
                    LOG.log(Level.WARNING, "Bad role reference to {0}, roles: {1}", referredRole, roles);
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * @return true if this bundle descriptor contains at least one CMP
     * EntityBean
     */
    public boolean containsCMPEntity() {
        Set<EjbDescriptor> ejbs = getEjbs();
        for (EjbDescriptor ejb : ejbs) {
            if (ejb instanceof EjbCMPEntityDescriptor) {
                return true;
            }
        }
        return false;
    }


    /**
     * @return true if I have roles, permissioned roles or container transactions.
     */
    public boolean hasAssemblyInformation() {
        return (!getRoles().isEmpty()) || hasPermissionedRoles() || hasContainerTransactions();
    }


    /**
     * @return true if any of my ejb's methods have been assigned transaction attributes.
     */
    public boolean hasContainerTransactions() {
        for (EjbDescriptor ejb : getEjbs()) {
            if (!ejb.getMethodContainerTransactions().isEmpty()) {
                return true;
            }
        }
        return false;
    }


    /**
     * @return true if I have Roles to which method permissions have been assigned.
     */
    public boolean hasPermissionedRoles() {
        for (EjbDescriptor ejb : getEjbs()) {
            if (!ejb.getPermissionedMethodsByPermission().isEmpty()) {
                return true;
            }
        }
        return false;
    }


    /**
     * visit the descriptor and all sub descriptors with a DOL visitor implementation
     *
     * @param aVisitor a visitor to traverse the descriptors
     */
    @Override
    public void visit(DescriptorVisitor aVisitor) {
        if (aVisitor instanceof EjbBundleVisitor ||
            aVisitor instanceof ComponentPostVisitor) {
            visit((ComponentVisitor) aVisitor);
        } else {
            super.visit(aVisitor);
        }
    }


    @Override
    public DescriptorVisitor getTracerVisitor() {
        return new EjbBundleTracerVisitor();
    }


    /**
     * @return new {@link EjbBundleValidator}
     */
    @Override
    public ComponentVisitor getBundleVisitor() {
        return new EjbBundleValidator();
    }


    // Used by reflection in PMDescriptorsNode
    @Deprecated(forRemoval = true, since = "3.1")
    public void setPersistenceManagerInUse(PersistenceManagerInUse inuse) {
        // ignored
    }


    // Used by reflection in PMDescriptorsNode
    @Deprecated(forRemoval = true, since = "3.1")
    public void addPersistenceManager(IASPersistenceManagerDescriptor pmDesc) {
        // ignored
    }


    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("EjbBundleDescriptor\n");
        super.print(toStringBuffer);
        if (cmpResourceReference != null) {
            toStringBuffer.append("\ncmp resource ");
            cmpResourceReference.print(toStringBuffer);
        }
        toStringBuffer.append("\nclient JAR ").append(getEjbClientJarUri());
        for (EjbDescriptor ejb : getEjbs()) {
            toStringBuffer.append("\n------------\n");
            toStringBuffer.append(ejb);
            toStringBuffer.append("\n------------");
        }
    }
}
