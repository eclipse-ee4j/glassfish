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
import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.RoleReference;
import com.sun.enterprise.deployment.runtime.common.SecurityRoleMapping;
import com.sun.enterprise.deployment.util.ComponentPostVisitor;
import com.sun.enterprise.deployment.util.ComponentVisitor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.util.EjbBundleVisitor;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.DescriptorVisitor;
import org.glassfish.ejb.deployment.descriptor.runtime.IASPersistenceManagerDescriptor;
import org.glassfish.ejb.deployment.descriptor.runtime.PersistenceManagerInUse;
import org.glassfish.ejb.deployment.node.EjbBundleNode;
import org.glassfish.ejb.deployment.util.EjbBundleTracerVisitor;
import org.glassfish.ejb.deployment.util.EjbBundleValidator;
import org.glassfish.security.common.Role;

public class EjbBundleDescriptorImpl extends EjbBundleDescriptor {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = DOLUtils.getLogger();
    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(EjbBundleDescriptorImpl.class);

    private Set<Long> ejbIDs;
    private final Set<RelationshipDescriptor> relationships = new HashSet<>();
    private String relationshipsDescription;
    private String ejbClientJarUri;

    // list of configured persistence manager
    private Vector<IASPersistenceManagerDescriptor> configured_pms;
    private PersistenceManagerInUse pm_inuse;

    // the resource (database) to be used for persisting CMP EntityBeans
    // the same resource is used for all beans in this ejb jar.
    private ResourceReferenceDescriptor cmpResourceReference;




    private final List<SecurityRoleMapping> roleMaps = new ArrayList<>();

    private LinkedList<InterceptorBindingDescriptor> interceptorBindings = new LinkedList<>();

    private final List<NameValuePairDescriptor> enterpriseBeansProperties = new ArrayList<>();



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
     *
     * @return Collection of unique ID of EJBs within the same module
     */
    public Collection<Long> getDescriptorIds() {
        return ejbIDs;
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


    public void prependInterceptorBinding(InterceptorBindingDescriptor binding) {
        interceptorBindings.addFirst(binding);
    }


    public void appendInterceptorBinding(InterceptorBindingDescriptor binding) {
        interceptorBindings.addLast(binding);
    }


    public List<InterceptorBindingDescriptor> getInterceptorBindings() {
        return new LinkedList<>(interceptorBindings);
    }


    public void setInterceptorBindings(List<InterceptorBindingDescriptor> bindings) {
        interceptorBindings = new LinkedList<>();
        interceptorBindings.addAll(bindings);
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
     * @return true if I have roles, permissioned roles or container transactions.
     */
    public boolean hasAssemblyInformation() {
        return (!getRoles().isEmpty()) || hasPermissionedRoles() || hasContainerTransactions();
    }


    /**
     * Add a RelationshipDescriptor which describes a CMR field
     * between a bean/DO/entityRef in this ejb-jar.
     */
    public void addRelationship(RelationshipDescriptor relDesc) {
        relationships.add(relDesc);

    }


    /**
     * Add a RelationshipDescriptor which describes a CMR field
     * between a bean/DO/entityRef in this ejb-jar.
     */
    public void removeRelationship(RelationshipDescriptor relDesc) {
        relationships.remove(relDesc);

    }


    /**
     * EJB2.0: get description for <relationships> element.
     */
    public String getRelationshipsDescription() {
        if (relationshipsDescription == null) {
            relationshipsDescription = "";
        }
        return relationshipsDescription;
    }


    /**
     * EJB2.0: set description for <relationships> element.
     */
    public void setRelationshipsDescription(String relationshipsDescription) {
        this.relationshipsDescription = relationshipsDescription;
    }


    /**
     * Get all relationships in this ejb-jar.
     *
     * @return a Set of RelationshipDescriptors.
     */
    public Set<RelationshipDescriptor> getRelationships() {
        return relationships;
    }


    public boolean hasRelationships() {
        return !relationships.isEmpty();
    }


    /**
     * Returns true if given relationship is already part of this
     * ejb-jar.
     */
    public boolean hasRelationship(RelationshipDescriptor rd) {
        return relationships.contains(rd);
    }

    /**
     * Return the Resource I use for CMP.
     */
    public ResourceReferenceDescriptor getCMPResourceReference() {
        return cmpResourceReference;
    }


    /**
     * Sets the resource reference I use for CMP.
     */
    public void setCMPResourceReference(ResourceReferenceDescriptor resourceReference) {
        this.cmpResourceReference = resourceReference;
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



    /**
    * Returns a formatted String representing my state.
    */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("EjbBundleDescriptor\n");
        super.print(toStringBuffer);
        if (cmpResourceReference != null) {
            toStringBuffer.append("\ncmp resource ");
            cmpResourceReference.print(toStringBuffer);
        }
        toStringBuffer.append("\nclient JAR ").append(getEjbClientJarUri());
        for (Descriptor o : getEjbs()) {
            toStringBuffer.append("\n------------\n");
            o.print(toStringBuffer);
            toStringBuffer.append("\n------------");
        }
    }


    public void setPersistenceManagerInuse(String id, String ver) {
        LOG.log(Level.DEBUG, "setPersistenceManagerInuse(id={0}, ver={1})", id, ver);
        pm_inuse = new PersistenceManagerInUse(id, ver);
    }


    public void setPersistenceManagerInUse(PersistenceManagerInUse inuse) {
        pm_inuse = inuse;
    }


    public PersistenceManagerInUse getPersistenceManagerInUse() {
        return pm_inuse;
    }


    public void addPersistenceManager(IASPersistenceManagerDescriptor pmDesc) {
        if (configured_pms == null) {
            configured_pms = new Vector<>();
        }
        configured_pms.add(pmDesc);
    }


    public IASPersistenceManagerDescriptor getPreferredPersistenceManager() {
        if (configured_pms == null || configured_pms.isEmpty()) {
            // return the default persistence manager descriptor
            return null;
        }

        String pminuse_id = pm_inuse.get_pm_identifier().trim();
        String pminuse_ver = pm_inuse.get_pm_version().trim();
        int size = configured_pms.size();
        for (int i = 0; i < size; i++) {
            IASPersistenceManagerDescriptor pmdesc = configured_pms.elementAt(i);
            String pmdesc_id = pmdesc.getPersistenceManagerIdentifier();
            String pmdesc_ver = pmdesc.getPersistenceManagerVersion();
            if (pmdesc_id.trim().equals(pminuse_id) && pmdesc_ver.trim().equals(pminuse_ver)) {
                return pmdesc;
            }
        }
        throw new IllegalArgumentException(
            I18N.getLocalString(
                "enterprise.deployment.nomatchingpminusefound",
                "No PersistenceManager found that matches specified PersistenceManager in use."));
    }


    public Vector<IASPersistenceManagerDescriptor> getPersistenceManagers() {
        return configured_pms;
    }


    public void addSecurityRoleMapping(SecurityRoleMapping roleMapping) {
        roleMaps.add(roleMapping);
    }


    public List<SecurityRoleMapping> getSecurityRoleMappings() {
        return roleMaps;
    }


    public String getEnterpriseBeansProperty(String key) {
        for (NameValuePairDescriptor p : enterpriseBeansProperties) {
            if (p.getName().equals(key)) {
                return p.getValue();
            }
        }
        return null;
    }


    public void addEnterpriseBeansProperty(NameValuePairDescriptor newProp) {
        enterpriseBeansProperties.add(newProp);
    }


    public List<NameValuePairDescriptor> getEnterpriseBeansProperties() {
        return enterpriseBeansProperties;
    }
}
