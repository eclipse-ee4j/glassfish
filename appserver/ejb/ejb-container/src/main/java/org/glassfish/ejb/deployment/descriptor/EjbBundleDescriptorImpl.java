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
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerFactoryReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.InjectionCapable;
import com.sun.enterprise.deployment.InjectionInfo;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.RoleReference;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.runtime.common.SecurityRoleMapping;
import com.sun.enterprise.deployment.types.EjbReference;
import com.sun.enterprise.deployment.types.EntityManagerFactoryReference;
import com.sun.enterprise.deployment.types.EntityManagerReference;
import com.sun.enterprise.deployment.util.ComponentPostVisitor;
import com.sun.enterprise.deployment.util.ComponentVisitor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.util.EjbBundleVisitor;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.glassfish.api.deployment.archive.ArchiveType;
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

    private long uniqueId;
    private Boolean disableNonportableJndiNames;
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

    // Application exceptions defined for the ejbs in this module.
    private final Map<String, EjbApplicationExceptionInfo> applicationExceptions = new HashMap<>();

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(EjbBundleDescriptorImpl.class);


    private final List<SecurityRoleMapping> roleMaps = new ArrayList<>();

    private LinkedList<InterceptorBindingDescriptor> interceptorBindings = new LinkedList<>();

    private final List<NameValuePairDescriptor> enterpriseBeansProperties = new ArrayList<>();

    // EJB module level dependencies
    private final Set<EnvironmentProperty> environmentProperties = new HashSet<>();
    private final Set<EjbReferenceDescriptor> ejbReferences = new HashSet<>();
    private final Set<ResourceEnvReferenceDescriptor> resourceEnvReferences = new HashSet<>();
    private final Set<MessageDestinationReferenceDescriptor> messageDestReferences = new HashSet<>();
    private final Set<ResourceReferenceDescriptor> resourceReferences = new HashSet<>();
    private final Set<ServiceReferenceDescriptor> serviceReferences = new HashSet<>();
    private final Set<EntityManagerFactoryReferenceDescriptor> entityManagerFactoryReferences = new HashSet<>();
    private final Set<EntityManagerReferenceDescriptor> entityManagerReferences = new HashSet<>();


    @Override
    public String getDefaultSpecVersion() {
        return EjbBundleNode.SPEC_VERSION;
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


    public void setEjbClientJarUri(String ejbClientJarUri) {
        this.ejbClientJarUri = ejbClientJarUri;
    }

    public void addApplicationException(EjbApplicationExceptionInfo appExc) {
        applicationExceptions.put(appExc.getExceptionClassName(), appExc);
    }

    public Map<String, EjbApplicationExceptionInfo> getApplicationExceptions() {
        return new HashMap<>(applicationExceptions);
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
    * Checks whether the role references my ejbs have reference roles that I have.
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


    @Override
    public void removeRole(Role role) {
        if (super.getRoles().contains(role)) {
            for (EjbDescriptor ejb : getEjbs()) {
                ejb.removeRole(role);
            }
            super.removeRole(role);
        }
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
     * Returns my name.
     */

    @Override
    public String getName() {
        if ("".equals(super.getName())) {
            super.setName("Ejb1");
        }
        return super.getName();
    }


        // START OF IASRI 4645310
    /**
     * Sets the unique id for a stand alone ejb module. It traverses through
     * all the ejbs in this stand alone module and sets the unique id for
     * each of them. The traversal order is done in ascending element order.
     * <p>
     * Note: This method will not be called for application.
     *
     * @param id unique id for stand alone module
     */
    public void setUniqueId(long id) {
        uniqueId = id;

        // First sort the beans in alphabetical order.
        EjbDescriptor[] descs = getEjbs().toArray(EjbDescriptor[]::new);

        // The sorting algorithm used by this api is a modified mergesort.
        // This algorithm offers guaranteed n*log(n) performance, and
        // can approach linear performance on nearly sorted lists.
        Arrays.sort(descs, (o1, o2) -> o2.getName().compareTo(o1.getName()));

        for (int i = 0; i < descs.length; i++) {
            // 2^16 beans max per stand alone module
            descs[i].setUniqueId((id | i));
        }
    }

    /**
     * Returns the unique id used in a stand alone ejb module.
     * For application, this will return zero.
     *
     * @return    the unique if used in stand alone ejb module
     */
    public long getUniqueId() {
        return uniqueId;
    }


    public static int getIdFromEjbId(long ejbId) {
        long id = ejbId >> 32;
        return (int) id;
    }


    /**
     * @return true if this bundle descriptor defines web service clients
     */
    @Override
    public boolean hasWebServiceClients() {
        for (EjbDescriptor next : getEjbs()) {
            Collection<ServiceReferenceDescriptor> serviceRefs = next.getServiceReferenceDescriptors();
            if (!(serviceRefs.isEmpty())) {
                return true;
            }
        }
        return false;
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

    @Override
    public DescriptorVisitor getTracerVisitor() {
        return new EjbBundleTracerVisitor();
    }


    /**
     * @return the visitor for this bundle descriptor
     */
    @Override
    public ComponentVisitor getBundleVisitor() {
        return new EjbBundleValidator();
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

    /**
     * @return the module type for this bundle descriptor
     */
    @Override
    public ArchiveType getModuleType() {
        return DOLUtils.ejbType();
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
            localStrings.getLocalString(
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


    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<? extends PersistenceUnitDescriptor> findReferencedPUs() {
        Collection<PersistenceUnitDescriptor> pus = new HashSet<>();
        // Iterate through all the ejbs
        for (EjbDescriptor ejb : getEjbs()) {
            pus.addAll(findReferencedPUsViaPURefs(ejb));
            pus.addAll(findReferencedPUsViaPCRefs(ejb));
        }

        // Add bundle level artifacts added by e.g. CDDI
        for (EntityManagerFactoryReference emfRef : getEntityManagerFactoryReferenceDescriptors()) {
            pus.add(findReferencedPUViaEMFRef(emfRef));
        }

        for (EntityManagerReference emRef : getEntityManagerReferenceDescriptors()) {
            pus.add(findReferencedPUViaEMRef(emRef));
        }
        return pus;
    }


    /**
     * Returns the generated XML directory feturn the set of ejb references this ejb declares.
     */
    @Override
    public Set<EjbReferenceDescriptor> getEjbReferenceDescriptors() {
        return ejbReferences;
    }


    /**
     * Adds a reference to another ejb to me.
     */

    @Override
    public void addEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
        ejbReferences.add(ejbReference);
        ejbReference.setReferringBundleDescriptor(this);
    }


    @Override
    public void removeEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
        ejbReferences.remove(ejbReference);
    }


    /**
     * Return a reference to another ejb by the same name or throw an IllegalArgumentException.
     */
    public EjbReference getEjbReferenceByName(String name) {
        return getEjbReference(name);
    }


    @Override
    public EjbReferenceDescriptor getEjbReference(String name) {
        for (EjbReferenceDescriptor er : getEjbReferenceDescriptors()) {
            if (er.getName().equals(name)) {
                return er;
            }
        }
        throw new IllegalArgumentException(
            localStrings.getLocalString("enterprise.deployment.exceptionapphasnoejbrefbyname",
                "This ejb jar [{0}] has no ejb reference by the name of [{1}] ", new Object[] {getName(), name}));
    }


    @Override
    public Set<ServiceReferenceDescriptor> getServiceReferenceDescriptors() {
        return serviceReferences;
    }


    @Override
    public void addServiceReferenceDescriptor(ServiceReferenceDescriptor serviceRef) {
        serviceRef.setBundleDescriptor(this);
        serviceReferences.add(serviceRef);
    }


    @Override
    public void removeServiceReferenceDescriptor(ServiceReferenceDescriptor serviceRef) {
        serviceReferences.remove(serviceRef);
    }


    /**
     * Looks up an service reference with the given name.
     * Throws an IllegalArgumentException if it is not found.
     */
    @Override
    public ServiceReferenceDescriptor getServiceReferenceByName(String name) {
        for (Object element : getServiceReferenceDescriptors()) {
            ServiceReferenceDescriptor srd = (ServiceReferenceDescriptor) element;
            if (srd.getName().equals(name)) {
                return srd;
            }
        }
        throw new IllegalArgumentException("No service ref of name " + name);
    }


    @Override
    public Set<MessageDestinationReferenceDescriptor> getMessageDestinationReferenceDescriptors() {
        return messageDestReferences;
    }


    @Override
    public void addMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor messageDestRef) {
        messageDestRef.setReferringBundleDescriptor(this);
        messageDestReferences.add(messageDestRef);
    }


    @Override
    public void removeMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor msgDestRef) {
        messageDestReferences.remove(msgDestRef);
    }


    /**
     * Looks up an message destination reference with the given name.
     * Throws an IllegalArgumentException if it is not found.
     */
    @Override
    public MessageDestinationReferenceDescriptor
        getMessageDestinationReferenceByName(String name) {

        for (MessageDestinationReferenceDescriptor mdr : messageDestReferences) {
            if (mdr.getName().equals(name)) {
                return mdr;
            }
        }
        throw new IllegalArgumentException("No message destination ref of name " + name);
    }

    /**
     * Return the set of resource environment references this ejb declares.
     */
    @Override
    public Set<ResourceEnvReferenceDescriptor> getResourceEnvReferenceDescriptors() {
        return resourceEnvReferences;
    }

    @Override
    public void addResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvReference) {
        resourceEnvReferences.add(resourceEnvReference);
    }

    @Override
    public void removeResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvReference) {
        resourceEnvReferences.remove(resourceEnvReference);
    }

    /**
     * Return a reference to another ejb by the same name or throw an IllegalArgumentException.
     */
    @Override
    public ResourceEnvReferenceDescriptor getResourceEnvReferenceByName(String name) {
        for (ResourceEnvReferenceDescriptor element : getResourceEnvReferenceDescriptors()) {
            if (element.getName().equals(name)) {
                return element;
            }
        }
        throw new IllegalArgumentException("No resource env ref of name " + name);
    }


    /**
     * Return the set of resource references this ejb declares.
     */
    @Override
    public Set<ResourceReferenceDescriptor> getResourceReferenceDescriptors() {
        return resourceReferences;
    }
    /**
     * Adds a resource reference to me.
     */
    @Override
    public void addResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
        resourceReferences.add(resourceReference);
    }

    /**
     * Removes the given resource reference from me.
     */
    @Override
    public void removeResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
        resourceReferences.remove(resourceReference);
    }

    /**
     * Return the resource object corresponding to the supplied name or throw an illegal argument exception.
     */
    @Override
    public ResourceReferenceDescriptor getResourceReferenceByName(String name) {
        for (ResourceReferenceDescriptor element : getResourceReferenceDescriptors()) {
            if (element.getName().equals(name)) {
                return element;
            }
        }
        throw new IllegalArgumentException("no resource ref of name " + name);
    }

    /**
     * Returns the environment property object searching on the supplied key.
     * throws an illegal argument exception if no such environment property exists.
     */
    @Override
    public EnvironmentProperty getEnvironmentPropertyByName(String name) {
        for (EnvironmentProperty ev : getEnvironmentProperties()) {
            if (ev.getName().equals(name)) {
                return ev;
            }
        }
        throw new IllegalArgumentException("no env-entry of name " + name);
    }

    /**
     * Return a copy of the structure holding the environment properties.
     */
    @Override
    public Set<EnvironmentProperty> getEnvironmentProperties() {
        return environmentProperties;
    }

    @Override
    public void addEnvironmentProperty(EnvironmentProperty environmentProperty) {
        environmentProperties.add(environmentProperty);
    }

    /**
     * Removes the given environment property from me.
     */

    @Override
    public void removeEnvironmentProperty(EnvironmentProperty environmentProperty) {
        getEnvironmentProperties().remove(environmentProperty);

    }


    @Override
    public Set<EntityManagerFactoryReferenceDescriptor> getEntityManagerFactoryReferenceDescriptors() {
        return entityManagerFactoryReferences;
    }


    /**
     * Return the entity manager factory reference descriptor corresponding to
     * the given name.
     */
    @Override
    public EntityManagerFactoryReferenceDescriptor getEntityManagerFactoryReferenceByName(String name) {
        for (EntityManagerFactoryReferenceDescriptor next : getEntityManagerFactoryReferenceDescriptors()) {
            if (next.getName().equals(name)) {
                return next;
            }
        }
        throw new IllegalArgumentException("No entity manager factory reference of name " + name);
    }


    @Override
    public void addEntityManagerFactoryReferenceDescriptor(EntityManagerFactoryReferenceDescriptor reference) {
        reference.setReferringBundleDescriptor(this);
        entityManagerFactoryReferences.add(reference);
    }


    @Override
    public Set<EntityManagerReferenceDescriptor> getEntityManagerReferenceDescriptors() {
        return entityManagerReferences;
    }


    /**
     * Return the entity manager factory reference descriptor corresponding to
     * the given name.
     */
    @Override
    public EntityManagerReferenceDescriptor getEntityManagerReferenceByName(String name) {
        for (EntityManagerReferenceDescriptor next : getEntityManagerReferenceDescriptors()) {
            if (next.getName().equals(name)) {
                return next;
            }
        }
        throw new IllegalArgumentException("No entity manager reference of name " + name);
    }


    @Override
    public void addEntityManagerReferenceDescriptor(EntityManagerReferenceDescriptor reference) {
        reference.setReferringBundleDescriptor(this);
        getEntityManagerReferenceDescriptors().add(reference);
    }


    @Override
    public List<InjectionCapable> getInjectableResourcesByClass(String className) {
        return getInjectableResourcesByClass(className, this);
    }

    @Override
    public InjectionInfo getInjectionInfoByClass(Class clazz) {
        return getInjectionInfoByClass(clazz, this);
    }

    @Override
    public Boolean getDisableNonportableJndiNames() {
        return disableNonportableJndiNames;
    }

    public void setDisableNonportableJndiNames(String disableOrNot) {
        disableNonportableJndiNames = Boolean.valueOf(disableOrNot);
    }

    //
    // There is still some redundant DOL processing of the modules that can result in these
    // being called so just treat them as no-ops.
    //

    public Set<LifecycleCallbackDescriptor> getAroundConstructDescriptors() {
        return new HashSet<>();
    }


    public void addAroundConstructDescriptor(LifecycleCallbackDescriptor aroundConstructDesc) {
        // no-op
    }


    public LifecycleCallbackDescriptor getAroundConstructDescriptorByClass(String className) {
        return null;
    }

    @Override
    public Set<LifecycleCallbackDescriptor> getPostConstructDescriptors() {
        return new HashSet<>();
    }


    @Override
    public void addPostConstructDescriptor(LifecycleCallbackDescriptor postConstructDesc) {
        // no-op
    }


    @Override
    public LifecycleCallbackDescriptor getPostConstructDescriptorByClass(String className) {
        return null;
    }


    @Override
    public Set<LifecycleCallbackDescriptor> getPreDestroyDescriptors() {
        return new HashSet<>();
    }


    @Override
    public void addPreDestroyDescriptor(LifecycleCallbackDescriptor preDestroyDesc) {
        // no-op
    }


    @Override
    public LifecycleCallbackDescriptor getPreDestroyDescriptorByClass(String className) {
        return null;
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
