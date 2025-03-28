/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.node.RootXMLNode;
import com.sun.enterprise.deployment.types.EntityManagerFactoryReference;
import com.sun.enterprise.deployment.types.EntityManagerReference;
import com.sun.enterprise.deployment.util.ApplicationValidator;
import com.sun.enterprise.deployment.util.ComponentVisitor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.Utility;
import com.sun.enterprise.util.io.FileUtils;

import jakarta.persistence.EntityManagerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.security.common.Role;

import static java.util.Collections.emptyList;

/**
 * I am an abstract class representing all the deployment information common to all component container structures held
 * by an application.
 *
 * @author Danny Coward
 */
public abstract class BundleDescriptor extends RootDeploymentDescriptor implements Roles {

    private static final long serialVersionUID = 1L;
    private static final String PERSISTENCE_UNIT_NAME_SEPARATOR = "#";
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(BundleDescriptor.class);

    private final static String DEPLOYMENT_DESCRIPTOR_DIR = "META-INF";
    private final static String WSDL_DIR = "wsdl";

    // the spec versions we should start to look at annotations
    private final static double ANNOTATION_RAR_VER = 1.6;
    private final static double ANNOTATION_EJB_VER = 3.0;
    private final static double ANNOTATION_WAR_VER = 2.5;
    private final static double ANNOTATION_CAR_VER = 5.0;

    private boolean fullFlag;
    private boolean fullAttribute;

    private Application application;
    private Set<Role> roles;
    private Set<MessageDestinationDescriptor> messageDestinations = new HashSet<>();
    private final WebServicesDescriptor webServices = new WebServicesDescriptor();

    private final Set<ManagedBeanDescriptor> managedBeans = new HashSet<>();

    // Physical entity manager factory corresponding to the unit name of
    // each module-level persistence unit. Only available at runtime.
    private final Map<String, EntityManagerFactory> entityManagerFactories = new HashMap<>();

    // table for caching InjectionInfo with the class name as index
    private final Hashtable<InjectionInfoCacheKey, InjectionInfo> injectionInfos = new Hashtable<>();

    private boolean policyModified;
    private String compatValue;
    private boolean keepState;

    private final HashMap<String, RootXMLNode<?>> rootNodes = new HashMap<>();

    /**
     * Construct a new BundleDescriptor
     */
    public BundleDescriptor() {
        super();
        webServices.setBundleDescriptor(this);
    }

    /**
     * Construct a new BundleDescriptor with a name and description
     */
    public BundleDescriptor(String name, String description) {
        super(name, description);
        webServices.setBundleDescriptor(this);
    }

    /**
     * Sets the application to which I belong.
     */
    public final void setApplication(Application application) {
        this.application = application;
        for (List<? extends RootDeploymentDescriptor> extensionsByType : extensions.values()) {
            for (RootDeploymentDescriptor extension : extensionsByType) {
                if (extension instanceof BundleDescriptor) {
                    ((BundleDescriptor) extension).setApplication(application);
                }
            }
        }
    }

    public void addBundleDescriptor(BundleDescriptor bundleDescriptor) {
        getRoles().addAll(bundleDescriptor.getRoles());
        for (MessageDestinationDescriptor mdDesc : bundleDescriptor.getMessageDestinations()) {
            addMessageDestination(mdDesc);
        }
    }

    /**
     * Return true if the other bundle descriptor comes from the same module
     *
     * @param other the other bundle descriptor
     * @return true if co-packaged in the same module
     */
    public boolean isPackagedAsSingleModule(BundleDescriptor other) {
        return getModuleDescriptor().equals(other.getModuleDescriptor());
    }

    /**
     * @return true if this module is an application object
     */
    @Override
    public boolean isApplication() {
        return false;
    }

    /**
     * @return true if this module is a standalone deployment unit
     */
    public boolean isStandalone() {
        return application.isVirtual();
    }

    /**
     * The application to which I belong, or none if I am standalone.
     */
    public final Application getApplication() {
        return application;
    }

    public void addRootNode(String ddPath, RootXMLNode<?> rootNode) {
        rootNodes.put(ddPath, rootNode);
    }

    public RootXMLNode<?> getRootNode(String ddPath) {
        return rootNodes.get(ddPath);
    }

    /**
     * Set the physical entity manager factory for a persistence unit within this module.
     */
    public void addEntityManagerFactory(String unitName, EntityManagerFactory entityManagerFactory) {
        entityManagerFactories.put(unitName, entityManagerFactory);
    }

    /**
     * Retrieve the physical entity manager factory associated with the unitName of a persistence unit within this module.
     * Returns null if no matching entry is found.
     */
    public EntityManagerFactory getEntityManagerFactory(String unitName) {
        return entityManagerFactories.get(unitName);
    }

    /**
     * Returns the set of physical entity manager factories associated with persistence units in this module.
     */
    public Set<EntityManagerFactory> getEntityManagerFactories() {
        return new HashSet<>(entityManagerFactories.values());
    }

    public void addManagedBean(ManagedBeanDescriptor desc) {
        if (!hasManagedBeanByBeanClass(desc.getBeanClassName())) {
            // check for uniqueness of ManagedBean name, if defined
            if (desc.isNamed()) {
                for (ManagedBeanDescriptor managedBeanDescriptor : managedBeans) {
                    if (managedBeanDescriptor.isNamed() && desc.getName().equals(managedBeanDescriptor.getName())) {
                        // duplicate ManagedBean found
                        throw new RuntimeException(
                                localStrings.getLocalString("entreprise.deployment.exceptionduplicatemanagedbeandefinition",
                                        "ManagedBean [{0}] cannot have same name [{1}] already used by " + "another ManagedBean [{2}]",
                                        new Object[] { desc.getBeanClassName(), managedBeanDescriptor.getName(),
                                                managedBeanDescriptor.getBeanClassName() }));
                    }
                }
            }
            managedBeans.add(desc);
            desc.setBundle(this);
        }
    }

    public boolean hasManagedBeanByBeanClass(String beanClassName) {
        ManagedBeanDescriptor descriptor = getManagedBeanByBeanClass(beanClassName);
        return (descriptor != null);
    }

    public ManagedBeanDescriptor getManagedBeanByBeanClass(String beanClassName) {
        for (ManagedBeanDescriptor next : managedBeans) {
            if (beanClassName.equals(next.getBeanClassName())) {
                return next;
            }
        }
        return null;
    }

    public Set<ManagedBeanDescriptor> getManagedBeans() {
        return new HashSet<>(managedBeans);
    }

    /**
     * Return web services defined for this module. Not applicable for application clients.
     */
    public WebServicesDescriptor getWebServices() {
        return webServices;
    }

    public WebServiceEndpoint getWebServiceEndpointByName(String name) {
        return webServices.getEndpointByName(name);
    }

    /**
     * @return true if this bundle descriptor defines web service clients
     */
    public boolean hasWebServiceClients() {
        return false;
    }

    /**
     * @return true if this bundle descriptor defines web services
     */
    public boolean hasWebServices() {
        return getWebServices().hasWebServices();
    }

    /**
     * Return the Set of message destinations I have
     */
    public Set<MessageDestinationDescriptor> getMessageDestinations() {
        if (messageDestinations == null) {
            messageDestinations = new HashSet<>();
        }
        return messageDestinations;
    }

    /**
     * Returns true if I have an message destiation by that name.
     */
    public boolean hasMessageDestinationByName(String name) {
        for (MessageDestinationDescriptor mtd : getMessageDestinations()) {
            if (mtd.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a message destination descriptor that I have by the same name, or throws an IllegalArgumentException
     */
    public MessageDestinationDescriptor getMessageDestinationByName(String name) {
        for (MessageDestinationDescriptor mtd : getMessageDestinations()) {
            if (mtd.getName().equals(name)) {
                return mtd;
            }
        }
        throw new IllegalArgumentException(localStrings.getLocalString("enterprise.deployment.exceptionmessagedestbundle",
                "Referencing error: this bundle has no message destination of name: {0}", new Object[] { name }));
    }

    /**
     * Add a message destination to me.
     */
    public void addMessageDestination(MessageDestinationDescriptor messageDestination) {
        messageDestination.setBundleDescriptor(this);
        this.getMessageDestinations().add(messageDestination);
    }

    /**
     * Remove the given message destination descriptor from my (by equality).
     */
    public void removeMessageDestination(MessageDestinationDescriptor messageDestinationDescriptor) {
        messageDestinationDescriptor.setBundleDescriptor(null);
        this.getMessageDestinations().remove(messageDestinationDescriptor);
    }

    /**
     * Return the set of com.sun.enterprise.deployment.Role objects I have plus the ones from application
     */
    @Override
    public Set<Role> getRoles() {
        if (roles == null) {
            roles = new OrderedSet<>();
        }

        if (application != null) {
            roles.addAll(application.getAppRoles());
        }

        return roles;
    }

    /**
     * Adds a role object to me.
     */
    @Override
    public void addRole(Role role) {
        getRoles().add(role);
    }

    /**
     * Adds a Role object based on the supplied SecurityRoleDescriptor.
     *
     * <p/>
     * A change in SecurityRoleNode to fix bug 4933385 causes the DOL to use SecurityRoleDescriptor, rather than Role, to
     * contain information about security roles. To minimize the impact on BundleDescriptor, this method has been added for
     * use by the DOL as it processes security-role elements.
     *
     * <p/>
     * This method creates a new Role object based on the characteristics of the SecurityRoleDescriptor and then delegates
     * to addRole(Role) to preserve the rest of the behavior of this class.
     *
     * @param descriptor SecurityRoleDescriptor that describes the username and description of the role
     */
    public void addRole(SecurityRoleDescriptor descriptor) {
        addRole(new Role(descriptor.getName(), descriptor.getDescription()));
    }

    /**
     * Removes a role object from me.
     */
    @Override
    public void removeRole(Role role) {
        this.getRoles().remove(role);
    }

    /**
     * Utility method for iterating the set of named descriptors in the supplied nameEnvironment
     */
    protected Collection<NamedDescriptor> getNamedDescriptorsFrom(JndiNameEnvironment nameEnvironment) {
        Collection<NamedDescriptor> namedDescriptors = new Vector<>();
        for (ResourceReferenceDescriptor element : nameEnvironment.getResourceReferenceDescriptors()) {
            namedDescriptors.add(element);
        }
        for (EjbReferenceDescriptor element : nameEnvironment.getEjbReferenceDescriptors()) {
            namedDescriptors.add(element);
        }
        for (ResourceEnvReferenceDescriptor element : nameEnvironment.getResourceEnvReferenceDescriptors()) {
            namedDescriptors.add(element);
        }

        return namedDescriptors;
    }

    /**
     * Utility method for iterating the set of NameReference pairs in the supplied nameEnvironment
     */
    protected Vector<NamedReferencePair> getNamedReferencePairsFrom(JndiNameEnvironment nameEnvironment) {
        Vector<NamedReferencePair> pairs = new Vector<>();
        for (ResourceReferenceDescriptor element : nameEnvironment.getResourceReferenceDescriptors()) {
            pairs.add(NamedReferencePair.createResourceRefPair((Descriptor) nameEnvironment, element));
        }
        for (EjbReferenceDescriptor element : nameEnvironment.getEjbReferenceDescriptors()) {
            pairs.add(NamedReferencePair.createEjbRefPair((Descriptor) nameEnvironment, element));
        }
        for (ResourceEnvReferenceDescriptor element : nameEnvironment.getResourceEnvReferenceDescriptors()) {
            pairs.add(NamedReferencePair.createResourceEnvRefPair((Descriptor) nameEnvironment, element));
        }
        return pairs;
    }

    private static final class InjectionInfoCacheKey {
        String beanName;
        Class<?> clazz;
        int hc;

        InjectionInfoCacheKey(String beanName, Class<?> clazz) {
            this.beanName = beanName;
            this.clazz = clazz;
            hc = beanName.hashCode();
        }

        @Override
        public int hashCode() {
            return hc;
        }

        @Override
        public boolean equals(Object o) {
            boolean result = false;
            if (o instanceof InjectionInfoCacheKey) {
                InjectionInfoCacheKey other = (InjectionInfoCacheKey) o;
                if (hc == other.hc) {
                    return (clazz == other.clazz && beanName.equals(other.beanName));
                }
            }
            return result;
        }
    }

    public InjectionInfo getInjectionInfoByClass(Class<?> clazz, JndiNameEnvironment jndiNameEnv) {
        // first look in the cache
        InjectionInfoCacheKey key = null;
        if (jndiNameEnv instanceof EjbDescriptor) {
            EjbDescriptor jndiEjbDesc = (EjbDescriptor) jndiNameEnv;
            key = new InjectionInfoCacheKey(jndiEjbDesc.getName(), clazz);
        } else {
            key = new InjectionInfoCacheKey(clazz.getName(), clazz);
        }

        InjectionInfo injectionInfo = injectionInfos.get(key);
        if (injectionInfo != null) {
            return injectionInfo;
        }

        String className = clazz.getName();

        // if it's not in the cache, create a new one
        LifecycleCallbackDescriptor postConstructDesc = getPostConstructDescriptorByClass(className, jndiNameEnv);
        String postConstructMethodName = postConstructDesc == null ? null : postConstructDesc.getLifecycleCallbackMethod();
        LifecycleCallbackDescriptor preDestroyDesc = getPreDestroyDescriptorByClass(className, jndiNameEnv);
        String preDestroyMethodName = preDestroyDesc == null ? null : preDestroyDesc.getLifecycleCallbackMethod();
        List<InjectionCapable> resourcesByClass = getInjectableResourcesByClass(className, jndiNameEnv);
        injectionInfo = new InjectionInfo(className, postConstructMethodName, preDestroyMethodName, resourcesByClass);

        // store it in the cache and return
        injectionInfos.put(key, injectionInfo);
        return injectionInfo;
    }

    public LifecycleCallbackDescriptor getPostConstructDescriptorByClass(String className, JndiNameEnvironment jndiNameEnv) {
        for (LifecycleCallbackDescriptor next : jndiNameEnv.getPostConstructDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                return next;
            }
        }

        return null;
    }

    public LifecycleCallbackDescriptor getPreDestroyDescriptorByClass(String className, JndiNameEnvironment jndiNameEnv) {
        for (LifecycleCallbackDescriptor next : jndiNameEnv.getPreDestroyDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                return next;
            }
        }

        return null;
    }

    public List<InjectionCapable> getInjectableResources(JndiNameEnvironment jndiNameEnv) {
        List<InjectionCapable> injectables = new LinkedList<>();
        addJndiNameEnvironmentInjectables(jndiNameEnv, injectables);

        return injectables;
    }

    private void addJndiNameEnvironmentInjectables(JndiNameEnvironment jndiNameEnv, List<InjectionCapable> injectables) {
        Collection<InjectionCapable> allEnvProps = new HashSet<>();

        for (EnvironmentProperty envEntry : jndiNameEnv.getEnvironmentProperties()) {
            // If the jndiNameEnv is an EjbBundleDescriptor then we have to account for this because
            // there can be injection points on classes inside the ejb jar but not accounted for
            // in the deployment descriptor.
            if (envEntry.hasContent() || jndiNameEnv instanceof EjbBundleDescriptor) {
                allEnvProps.add(envEntry);
            }
        }

        allEnvProps.addAll(jndiNameEnv.getEjbReferenceDescriptors());
        allEnvProps.addAll(jndiNameEnv.getServiceReferenceDescriptors());
        allEnvProps.addAll(jndiNameEnv.getResourceReferenceDescriptors());
        allEnvProps.addAll(jndiNameEnv.getResourceEnvReferenceDescriptors());
        allEnvProps.addAll(jndiNameEnv.getMessageDestinationReferenceDescriptors());

        allEnvProps.addAll(jndiNameEnv.getEntityManagerFactoryReferenceDescriptors());
        allEnvProps.addAll(jndiNameEnv.getEntityManagerReferenceDescriptors());

        for (InjectionCapable next : allEnvProps) {
            if (next.isInjectable()) {
                injectables.add(next);
            }
        }
    }

    /**
     * Define implementation of getInjectableResourceByClass here so it isn't replicated across appclient, web, ejb
     * descriptors.
     */
    public List<InjectionCapable> getInjectableResourcesByClass(String className, JndiNameEnvironment jndiNameEnv) {
        List<InjectionCapable> injectables = new LinkedList<>();

        for (InjectionCapable next : getInjectableResources(jndiNameEnv)) {
            if (next.isInjectable()) {
                for (InjectionTarget target : next.getInjectionTargets()) {
                    if (target.getClassName().equals(className)) {
                        injectables.add(next);
                    }
                }
            }
        }
        return injectables;
    }

    /**
     * @return the class loader associated with this module
     */
    @Override
    public ClassLoader getClassLoader() {
        if (classLoader != null) {
            return classLoader;
        }
        if (application != null) {
            return application.getClassLoader();
        }
        return classLoader;
    }

    /**
     * Prints a formatted string representing my state.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("\n");
        super.print(toStringBuffer);
        toStringBuffer.append("\n Roles[] = ").append(roles);
        if (getWebServices().hasWebServices()) {
            toStringBuffer.append("\n WebServices ");
            (getWebServices()).print(toStringBuffer);
        }
    }

    /**
     * @return the type of this bundle descriptor
     */
    @Override
    public abstract ArchiveType getModuleType();

    /**
     * @return the visitor for this bundle descriptor
     */
    public ComponentVisitor getBundleVisitor() {
        return new ApplicationValidator();
    }

    /**
     * visitor API implementation
     */
    public void visit(ComponentVisitor aVisitor) {
        aVisitor.accept(this);
    }

    /**
     * @return the module ID for this module descriptor
     */
    @Override
    public String getModuleID() {
        if (super.getModuleID() == null) {
            setModuleID(getModuleDescriptor().getArchiveUri());
        }
        if (getModuleDescriptor().isStandalone()) {
            return super.getModuleID();
        }
        if (application != null && !application.isVirtual()) {
            return application.getRegistrationName() + "#" + getModuleDescriptor().getArchiveUri();
        }
        return super.getModuleID();
    }

    public String getRawModuleID() {
        return super.getModuleID();
    }

    /**
     * @return the deployment descriptor directory location inside the archive file
     */
    public String getDeploymentDescriptorDir() {
        return DEPLOYMENT_DESCRIPTOR_DIR;
    }

    /**
     * @return the wsdl directory location inside the archive file
     */
    public String getWsdlDir() {
        return getDeploymentDescriptorDir() + "/" + WSDL_DIR;
    }

    /**
     * This method returns all the persistence units that are referenced by this module.
     *
     * <p>
     * Depending on the type of component, a persistence unit can be referenced by one of the four following ways:
     *  <ol>
     *  <li> &lt;persistence-context-ref>
     *  <li> @PersistenceContext
     *  <li> &lt;persistence-unit-ref>
     *  <li> @PersistenceUnit
     *  </ol>
     *
     * Only EjbBundleDescriptor, ApplicationClientDescriptor and WebBundleDescriptor have useful implementation of
     * this method.
     *
     * @return persistence units that are referenced by this module
     */
    public Collection<? extends PersistenceUnitDescriptor> findReferencedPUs() {
        return emptyList();
    }

    /**
     * helper method: find all PUs referenced via @PersistenceUnit or <persistence-unit-ref>
     */
    protected static Collection<? extends PersistenceUnitDescriptor> findReferencedPUsViaPURefs(JndiNameEnvironment component) {
        Collection<PersistenceUnitDescriptor> persistenceUnitDescriptors = new HashSet<>();

        for (var entityManagerFactoryReference : component.getEntityManagerFactoryReferenceDescriptors()) {
            persistenceUnitDescriptors.add(findReferencedPUViaEMFRef(entityManagerFactoryReference));
        }

        return persistenceUnitDescriptors;
    }

    protected static PersistenceUnitDescriptor findReferencedPUViaEMFRef(EntityManagerFactoryReference entityManagerFactoryReference) {
        BundleDescriptor bundle = entityManagerFactoryReference.getReferringBundleDescriptor();

        PersistenceUnitDescriptor persistenceUnitDescriptor =
            bundle.findReferencedPU(entityManagerFactoryReference.getUnitName());

        if (persistenceUnitDescriptor == null) {
            throw new RuntimeException(localStrings.getLocalString("enterprise.deployment.exception-unresolved-pu-ref", "xxx",
                    new Object[] { entityManagerFactoryReference.getName(), bundle.getName() }));
        }

        return persistenceUnitDescriptor;
    }

    /**
     * helper method: find all PUs referenced via @PersistenceContext or <persistence-context-ref>
     */
    protected static Collection<? extends PersistenceUnitDescriptor> findReferencedPUsViaPCRefs(JndiNameEnvironment component) {
        Collection<PersistenceUnitDescriptor> persistenceUnitDescriptors = new HashSet<>();

        for (var entityManagerReference : component.getEntityManagerReferenceDescriptors()) {
            persistenceUnitDescriptors.add(findReferencedPUViaEMRef(entityManagerReference));
        }

        return persistenceUnitDescriptors;
    }

    protected static PersistenceUnitDescriptor findReferencedPUViaEMRef(EntityManagerReference entityManagerReference) {
        BundleDescriptor bundle = entityManagerReference.getReferringBundleDescriptor();

        PersistenceUnitDescriptor persistenceUnitDescriptor = bundle.findReferencedPU(entityManagerReference.getUnitName());

        if (persistenceUnitDescriptor == null) {
            throw new RuntimeException(localStrings.getLocalString("enterprise.deployment.exception-unresolved-pc-ref", "xxx",
                new Object[] { entityManagerReference.getName(), bundle.getName() }));
        }

        if ("RESOURCE_LOCAL".equals(persistenceUnitDescriptor.getTransactionType())) {
            throw new RuntimeException(localStrings.getLocalString("enterprise.deployment.exception-non-jta-container-managed-em", "xxx",
                new Object[] { entityManagerReference.getName(), bundle.getName(), persistenceUnitDescriptor.getName() }));
        }

        return persistenceUnitDescriptor;
    }

    /**
     * It accepts both a qualified (e.g.) "lib/a.jar#FooPU" as well as unqualified name (e.g.) "FooPU". It then searched
     * all the PersistenceUnits that are defined in the scope of this bundle descriptor to see which one matches the give
     * name.
     *
     * @param unitName as used in @PersistenceUnit, @PersistenceContext <persistence-context-ref> or
     * <persistence-unit-name>. If null, this method returns the default persistence unit, if available. The reason it accepts null for
     * default persistence unit is because "" gets converted to null in EntityManagerReferenceHandler.processNewEmRefAnnotation.
     * @return PersistenceUnitDescriptor that this unitName resolves to. Returns null, if unitName could not be resolved.
     */
    public PersistenceUnitDescriptor findReferencedPU(String unitName) {
        if (Utility.isEmpty(unitName)) { // uses default PU.
            return findDefaultPU();
        }

        return findReferencedPU0(unitName);
    }

    /**
     * This method is responsible for finding default persistence unit for a bundle descriptor.
     *
     * @return the default persistence unit for this bundle. returns null, if there isno PU defined or default can not be
     * calculated because there are more than 1 PUs defined.
     */
    public PersistenceUnitDescriptor findDefaultPU() {
        // step #1: see if we have only one PU in the local scope.
        PersistenceUnitDescriptor persistenceUnitDescriptor = null;

        int totalNumberOfPUInBundle = 0;
        for (PersistenceUnitsDescriptor nextPUs : getModuleDescriptor().getDescriptor().getExtensionsDescriptors(PersistenceUnitsDescriptor.class)) {
            for (var nextPersistenceUnitDescriptor : nextPUs.getPersistenceUnitDescriptors()) {
                persistenceUnitDescriptor = nextPersistenceUnitDescriptor;
                totalNumberOfPUInBundle++;
            }
        }

        if (totalNumberOfPUInBundle == 1) { // there is only one PU in this bundle.
            return persistenceUnitDescriptor;
        }

        if (totalNumberOfPUInBundle == 0) { // there are no PUs in this bundle.
            // step #2: see if we have only one PU in the ear.
            int totalNumberOfPUInEar = 0;

            for (PersistenceUnitsDescriptor nextPUs : getApplication().getExtensionsDescriptors(PersistenceUnitsDescriptor.class)) {
                for (PersistenceUnitDescriptor nextPersistenceUnitDescriptor : nextPUs.getPersistenceUnitDescriptors()) {
                    persistenceUnitDescriptor = nextPersistenceUnitDescriptor;
                    totalNumberOfPUInEar++;
                }
            }

            if (totalNumberOfPUInEar == 1) {
                return persistenceUnitDescriptor;
            }
        }

        return null;
    }

    /**
     * Internal method. This method is used to find referenced PU with a given name. It does not accept null or empty unit
     * name.
     *
     * @param unitName
     * @return
     */
    private PersistenceUnitDescriptor findReferencedPU0(String unitName) {
        int separatorIndex = unitName.lastIndexOf(PERSISTENCE_UNIT_NAME_SEPARATOR);

        if (separatorIndex != -1) { // qualified name

            // Uses # => must be defined in a utility jar at ear scope.
            String unqualifiedUnitName = unitName.substring(separatorIndex + 1);
            String path = unitName.substring(0, separatorIndex);

            // It's necessary to call getTargetUri as that takes care of
            // converting ././b to canonical forms.
            String puRoot = getTargetUri(this, path);

            PersistenceUnitsDescriptor persistenceUnitsDescriptor = getApplication().getExtensionsDescriptors(PersistenceUnitsDescriptor.class, puRoot);
            if (persistenceUnitsDescriptor != null) {
                for (var persistenceUnitDescriptor : persistenceUnitsDescriptor.getPersistenceUnitDescriptors()) {
                    if (persistenceUnitDescriptor.getName().equals(unqualifiedUnitName)) {
                        return persistenceUnitDescriptor;
                    }
                }
            }
        } else { // uses unqualified name.
            // First look to see if there is a match with unqualified name, because local scope takes precedence.
            Map<String, PersistenceUnitDescriptor> visiblePUs = getVisiblePUs();
            PersistenceUnitDescriptor result = visiblePUs.get(unitName);
            if (result != null) {
                return result;
            }

            // Next look to see if there is unique match in ear scope.
            int sameNamedEarScopedPUCount = 0;
            Set<Map.Entry<String, PersistenceUnitDescriptor>> entrySet = visiblePUs.entrySet();
            for (Entry<String, PersistenceUnitDescriptor> entry : entrySet) {
                String s = entry.getKey();
                int idx = s.lastIndexOf(PERSISTENCE_UNIT_NAME_SEPARATOR);
                if (idx != -1 // ear scoped
                        && s.substring(idx + 1).matches(unitName)) {
                    result = entry.getValue();
                    sameNamedEarScopedPUCount++;
                }
            }

            // if there are more than one ear scoped PU with same name (this
            // is possible when PU is inside two different library jar),
            // then user can not use unqualified name.
            if (sameNamedEarScopedPUCount == 1) {
                return result;
            }
        }

        return null;
    }

    /**
     * This method returns all the PUs that are defined in this bundle as well as the PUs defined in the ear level.
     * e.g. for the following ear:
     *    ear/lib/a.jar#defines FooPU /lib/b.jar#defines FooPU
     *    ejb.jar#defines FooPU
     *
     * For the EjbBundleDescriptor (ejb.jar), the map will contain {(lib/a.jar#FooPU, PU1), (lib/b.jar#FooPU, PU2), (FooPU, PU3)}.
     *
     * @return a map of names to PUDescriptors that are visbible to this bundle descriptor. The name is a qualified name for
     * ear scoped PUs where as it is in unqualified form for local PUs.
     */
    public Map<String, PersistenceUnitDescriptor> getVisiblePUs() {
        Map<String, PersistenceUnitDescriptor> visiblePersistenceUnits = new HashMap<>();

        // Local scoped persistence units
        for (var persistenceUnitsDescriptor : getModuleDescriptor().getDescriptor().getExtensionsDescriptors(PersistenceUnitsDescriptor.class)) {
            for (var persistenceUnitDescriptor : persistenceUnitsDescriptor.getPersistenceUnitDescriptors()) {
                // For local persistence units, use unqualified name.
                visiblePersistenceUnits.put(
                    persistenceUnitDescriptor.getName(),
                    persistenceUnitDescriptor);
            }
        }

        // Ear scoped PUs
        Application application = getApplication();
        if (application != null) {
            for (var persistenceUnitsDescriptor : application.getExtensionsDescriptors(PersistenceUnitsDescriptor.class)) {
                for (var persistenceUnitDescriptor : persistenceUnitsDescriptor.getPersistenceUnitDescriptors()) {
                    // Use fully qualified name for ear scoped PU
                    visiblePersistenceUnits.put(
                        persistenceUnitDescriptor.getPuRoot() + PERSISTENCE_UNIT_NAME_SEPARATOR + persistenceUnitDescriptor.getName(),
                        persistenceUnitDescriptor);
                }
            }
        }

        return visiblePersistenceUnits;
    }

    /**
     * Get the uri of a target based on a source module and a a relative uri from the perspective of that source module.
     *
     * @param origin bundle descriptor within an application
     * @param relativeTargetUri relative uri from the given bundle descriptor
     * @return target uri
     */
    private String getTargetUri(BundleDescriptor origin, String relativeTargetUri) {
        try {
            String archiveUri = origin.getModuleDescriptor().getArchiveUri();
            return new URI(archiveUri).resolve(relativeTargetUri).getPath();
        } catch (URISyntaxException use) {
            throw new RuntimeException(use);
        }
    }

    public String getModuleName() {
        // for standalone jars, return its registration name
        // for applications, return the module uri
        if (getApplication().isVirtual()) {
            return getApplication().getRegistrationName();
        }
        return getModuleDescriptor().getArchiveUri();
    }

    // return a short unique representation of this BundleDescriptor
    public String getUniqueFriendlyId() {
        return FileUtils.makeFriendlyFilename(getModuleName());
    }

    public boolean isPolicyModified() {
        return policyModified;
    }

    public void setPolicyModified(boolean policyModified) {
        this.policyModified = policyModified;
    }

    public String getCompatibility() {
        return compatValue;
    }

    public void setCompatibility(String compatValue) {
        this.compatValue = compatValue;
    }

    public boolean getKeepState() {
        return keepState;
    }

    public void setKeepState(String keepStateVal) {
        this.keepState = Boolean.parseBoolean(keepStateVal);
    }

    /**
     * Sets the full flag of the bundle descriptor. Once set, the annotations of the classes contained in the archive
     * described by this bundle descriptor will be ignored.
     *
     * @param flag a boolean to set or unset the flag
     */
    public void setFullFlag(boolean flag) {
        fullFlag = flag;
    }

    /**
     * Sets the full attribute of the deployment descriptor
     *
     * @param value the full attribute
     */
    public void setFullAttribute(String value) {
        fullAttribute = Boolean.parseBoolean(value);
    }

    /**
     * Get the full attribute of the deployment descriptor
     *
     * @return the full attribute
     */
    public boolean isFullAttribute() {
        return fullAttribute;
    }

    /**
     * @return true for any of following cases:
     * <ol>
     * <li>When it's been tagged as "full" when processing annotations.
     * <li>When DD has a version which doesn't allow annotations.
     * </ol>
     * returns false otherwise.
     */
    public boolean isFullFlag() {
        // if it's been tagged as full,
        // return true
        if (fullFlag) {
            return true;
        }
        return isDDWithNoAnnotationAllowed();
    }

    /**
     * @return true for any of following cases:
     * <ol>
     * <li>ejb module and schema version earlier than 3.0;
     * <li>web module and schema version earlier than 2.5;
     * <li>appclient module and schema version earlier than 5.0.
     * <li>connector module and schema version earlier than 1.6
     * </ol>
     */
    public boolean isDDWithNoAnnotationAllowed() {
        ArchiveType moduleType = getModuleType();
        if (moduleType == null) {
            return false;
        }

        double specVersion = Double.parseDouble(getSpecVersion());

        // we do not process annotations for earlier versions of DD
        return (moduleType.equals(DOLUtils.ejbType()) && specVersion < ANNOTATION_EJB_VER
                || moduleType.equals(DOLUtils.warType()) && specVersion < ANNOTATION_WAR_VER
                || moduleType.equals(DOLUtils.carType()) && specVersion < ANNOTATION_CAR_VER
                || moduleType.equals(DOLUtils.rarType()) && specVersion < ANNOTATION_RAR_VER);
    }
}
