/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.node.ApplicationNode;
import com.sun.enterprise.deployment.runtime.application.wls.ApplicationParam;
import com.sun.enterprise.deployment.runtime.common.SecurityRoleMapping;
import com.sun.enterprise.deployment.runtime.common.wls.SecurityRoleAssignment;
import com.sun.enterprise.deployment.types.EjbReferenceContainer;
import com.sun.enterprise.deployment.types.MessageDestinationReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceEnvReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceReferenceContainer;
import com.sun.enterprise.deployment.types.RoleMappingContainer;
import com.sun.enterprise.deployment.types.ServiceReferenceContainer;
import com.sun.enterprise.deployment.util.ApplicationVisitor;
import com.sun.enterprise.deployment.util.ComponentVisitor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.ApplicationPersistenceInfo;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.deployment.common.DescriptorVisitor;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.deployment.common.SecurityRoleMapper;
import org.glassfish.deployment.common.SecurityRoleMapperFactory;
import org.glassfish.deployment.versioning.VersioningUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.security.common.Role;
import org.jvnet.hk2.annotations.Optional;

/**
 * Objects of this type encapsulate the data and behaviour of a JEE application.
 *
 * @author Danny Coward
 */
public class Application extends CommonResourceBundleDescriptor
        implements RoleMappingContainer, WritableJndiNameEnvironment,
            EjbReferenceContainer, ResourceEnvReferenceContainer,
            ResourceReferenceContainer, ServiceReferenceContainer,
            MessageDestinationReferenceContainer, ApplicationPersistenceInfo {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = DOLUtils.getDefaultLogger();
    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(Application.class);


    /**
     * default value for the library-directory element
     */
    private static final String LIBRARY_DIRECTORY_DEFAULT_VALUE = "lib";

    private static final String PERSISTENCE_UNIT_NAME_SEPARATOR = "#";

    /**
     * Store generated XML dir to be able to get the generated WSDL
     */
    private String generatedXMLDir;

    // Set of modules in this application
    private final Set<ModuleDescriptor<BundleDescriptor>> modules = new OrderedSet<>();

    // True if unique id has been set.  Allows callers to avoid
    // applying unique ids to subcomponents multiple times.
    private boolean uniqueIdSet;

    // IASRI 4645310
    /**
     * unique id for this application
     */
    private long uniqueId;

    /**
     * represents the virtual status of this application object
     */
    private boolean virtual;

    /**
     * represents whether all ejb modules in an application will be pass by
     * value or pass by reference
     */
    private Boolean passByReference;

    // flag to indicate that the memory representation of this application
    // is not in sync with the disk representation
    private boolean isDirty;

    // data structure to map roles to users and groups
    private transient SecurityRoleMapper roleMapper;

    /**
     * name used to register this application
     */
    private String registrationName;

    private String appName;

    private String archiveName;

    private String compatValue;

    private boolean initializeInOrder;

    // realm associated with this application
    private String realm;

    @Inject
    @Optional
    private transient SecurityRoleMapperFactory securityRoleMapperFactory;

    /**
     * A flag to store the resolved keepstate value for the current application.
     * See org.glassfish.ejb.startup.EjbApplication for details.  This value
     * may be different from super.keepState.
     */
    private boolean keepStateResolved;

    // Physical entity manager factory corresponding to the unit name of
    // each application-level persistence unit.  Only available at runtime.
    private transient Map<String, EntityManagerFactory> entityManagerFactories = new HashMap<>();

    private final Set<String> entityManagerFactoryUnitNames = new HashSet<>();

    // the jndi environment entries
    private final Set<EnvironmentProperty> environmentProperties = new HashSet<>();
    private final Set<EjbReferenceDescriptor> ejbReferences = new HashSet<>();
    private final Set<ResourceEnvReferenceDescriptor> resourceEnvReferences = new HashSet<>();
    private final Set<MessageDestinationReferenceDescriptor> messageDestReferences = new HashSet<>();
    private final Set<ResourceReferenceDescriptor> resourceReferences = new HashSet<>();
    private final Set<ServiceReferenceDescriptor> serviceReferences = new HashSet<>();
    private final Set<EntityManagerFactoryReferenceDescriptor> entityManagerFactoryReferences = new HashSet<>();
    private final Set<EntityManagerReferenceDescriptor> entityManagerReferences = new HashSet<>();

    private Set<Role> appRoles;

    private String libraryDirectory;

    private final List<SecurityRoleMapping> roleMaps = new ArrayList<>();
    private final List<SecurityRoleAssignment> wlRoleAssignments = new ArrayList<>();

    private boolean loadedFromApplicationXml = true;

    private final Set<String> resourceAdapters = new HashSet<>();

    private final Set<ApplicationParam> applicationParams = new HashSet<>();

    private Application() {
        super("", I18N.getLocalString(
                "enterprise.deployment.application.description",
                "Application description"));
    }


    /**
     * @return the default version of the deployment descriptor
     *         loaded by this descriptor
     */
    @Override
    public String getDefaultSpecVersion() {
        return ApplicationNode.SPEC_VERSION;
    }

    @Override
    public boolean isEmpty() {
        return modules.isEmpty();
    }


    /**
     * Creates a new application to hold a standalone module
     *
     * @param name the application name
     * @param newModule the standalone module descriptor
     * @return the application
     */
    public static Application createVirtualApplication(String name, ModuleDescriptor<BundleDescriptor> newModule) {

        // create a new empty application
        Application application = createApplication();

        application.setVirtual(true);
        if (name == null && newModule != null && newModule.getDescriptor() != null) {
            name = newModule.getDescriptor().getDisplayName();

        }
        String untaggedName = VersioningUtils.getUntaggedName(name);
        if (name != null) {
            application.setDisplayName(untaggedName);
            application.setName(untaggedName);
            application.setAppName(untaggedName);
        }

        // add the module to it
        if (newModule != null) {
            newModule.setStandalone(true);
            newModule.setArchiveUri(untaggedName);
            if (newModule.getDescriptor() != null) {
                newModule.getDescriptor().setApplication(application);
            }
            application.addModule(newModule);
        }

        return application;
    }

    /**
     * @return a new empty application
     */
    public static Application createApplication() {
        ServiceLocator locator = Globals.getStaticBaseServiceLocator();
        Application application = locator.create(Application.class);
        locator.inject(application);
        locator.postConstruct(application);
        return application;
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
     * @return a reference to another ejb by the same name or throw an IllegalArgumentException.
     */
    public EjbReferenceDescriptor getEjbReferenceByName(String name) {
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
            MessageFormat.format("This app [{0}] has no ejb reference by the name of [{1}] ", getName(), name));
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
        for (ServiceReferenceDescriptor srd : this.getServiceReferenceDescriptors()) {
            if (srd.getName().equals(name)) {
                return srd;
            }
        }
        throw new IllegalArgumentException(
            I18N.getLocalString("enterprise.deployment.exceptionapphasnoservicerefbyname",
                "This app [{0}] has no service reference by the name of [{1}]",
                new Object[] {getRegistrationName(), name}));
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
    public MessageDestinationReferenceDescriptor getMessageDestinationReferenceByName(String name) {
        for (MessageDestinationReferenceDescriptor mdr : messageDestReferences) {
            if (mdr.getName().equals(name)) {
                return mdr;
            }
        }
        throw new IllegalArgumentException(I18N.getLocalString("exceptionapphasnomsgdestrefbyname",
            "This app [{0}] has no message destination reference by the name of [{1}]",
            new Object[] {getRegistrationName(), name}));
    }


    /**
     * Return the set of resource environment references this application declares.
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
        for (ResourceEnvReferenceDescriptor jdr : this.getResourceEnvReferenceDescriptors()) {
            if (jdr.getName().equals(name)) {
                return jdr;
            }
        }
        throw new IllegalArgumentException(I18N.getLocalString(
                "enterprise.deployment.exceptionapphasnoresourceenvrefbyname",
                "This app {0} has no resource environment reference by the name of {1}",
                new Object[] {getRegistrationName(), name}));
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
        for (Object element : this.getResourceReferenceDescriptors()) {
            ResourceReferenceDescriptor next = (ResourceReferenceDescriptor) element;
            if (next.getName().equals(name)) {
                return next;
            }
        }
        throw new IllegalArgumentException(I18N.getLocalString(
                "enterprise.deployment.exceptionapphasnoresourcerefbyname",
                "This app {0} has no resource reference by the name of {1}",
                new Object[]{getRegistrationName(), name}));
    }

    @Override
    public EnvironmentProperty getEnvironmentPropertyByName(String name) {
        for (EnvironmentProperty ev : this.getEnvironmentProperties()) {
            if (ev.getName().equals(name)) {
                return ev;
            }
        }
        throw new IllegalArgumentException(I18N.getLocalString(
                "enterprise.deployment.exceptionapphasnoenvpropertybyname",
                "This app {0} has no environment property by the name of {1}",
                new Object[]{getRegistrationName(), name}));
    }

    @Override
    public Set<EnvironmentProperty> getEnvironmentProperties() {
        return environmentProperties;
    }

    @Override
    public void addEnvironmentProperty(EnvironmentProperty environmentProperty) {
        this.environmentProperties.add(environmentProperty);
    }

    @Override
    public void removeEnvironmentProperty(EnvironmentProperty environmentProperty) {
        this.getEnvironmentProperties().remove(environmentProperty);
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
        throw new IllegalArgumentException(
            I18N.getLocalString("enterprise.deployment.exceptionapphasnoentitymgrfactoryrefbyname",
                "This app {0} has no entity manager factory reference by the name of {1}",
                new Object[] {getRegistrationName(), name}));
    }


    @Override
    public void addEntityManagerFactoryReferenceDescriptor(EntityManagerFactoryReferenceDescriptor reference) {
        reference.setReferringBundleDescriptor(this);
        this.entityManagerFactoryReferences.add(reference);
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
        throw new IllegalArgumentException(
            I18N.getLocalString("enterprise.deployment.exceptionapphasnoentitymgrrefbyname",
                "This app {0} has no entity manager reference by the name of {1}",
                new Object[] {getRegistrationName(), name}));
    }


    @Override
    public void addEntityManagerReferenceDescriptor(EntityManagerReferenceDescriptor reference) {
        reference.setReferringBundleDescriptor(this);
        this.getEntityManagerReferenceDescriptors().add(reference);
    }


    @Override
    public Set<LifecycleCallbackDescriptor> getPostConstructDescriptors() {
        throw new UnsupportedOperationException();
    }


    @Override
    public void addPostConstructDescriptor(LifecycleCallbackDescriptor postConstructDesc) {
        throw new UnsupportedOperationException();
    }


    @Override
    public LifecycleCallbackDescriptor getPostConstructDescriptorByClass(String className) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Set<LifecycleCallbackDescriptor> getPreDestroyDescriptors() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPreDestroyDescriptor(LifecycleCallbackDescriptor preDestroyDesc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LifecycleCallbackDescriptor getPreDestroyDescriptorByClass(String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<InjectionCapable> getInjectableResourcesByClass(String className) {
        return getInjectableResourcesByClass(className, this);
    }

    @Override
    public InjectionInfo getInjectionInfoByClass(Class<?> clazz) {
        return getInjectionInfoByClass(clazz, this);
    }

    public void setGeneratedXMLDirectory(String xmlDir) {
        generatedXMLDir = xmlDir;
    }

    public String getGeneratedXMLDirectory() {
        return generatedXMLDir;
    }

    // START OF IASRI 4648645 - application registration name
    /**
     * Sets the registration name for this application. This name is used
     * while deploying the application. The deployment process gurantees
     * that this name is unique.
     *
     * @param appId the registration name used for this application
     */
    public void setRegistrationName(String appId) {

        // at his point we need to swap our RoleMapper, if we have one...
        SecurityRoleMapper roleMapper;
        try {
            roleMapper = getRoleMapper();
        } catch (IllegalArgumentException ignore) {
            roleMapper = null;
        }

        if (roleMapper != null) {
            if (securityRoleMapperFactory == null) {
                throw new IllegalArgumentException(I18N.getLocalString(
                        "enterprise.deployment.norolemapperfactorydefine",
                        "This application has no role mapper factory defined"));
            }
            securityRoleMapperFactory.removeRoleMapper(getName());
            roleMapper.setName(appId);
            securityRoleMapperFactory.setRoleMapper(appId, roleMapper);
        }

        this.registrationName = appId;
    }

    /**
     * Returns the registration name of this application.
     *
     * @return the registration name of this application
     */
    public String getRegistrationName() {
        if (registrationName == null) {
            return getName();
        }
        return registrationName;
    }
    // END OF IASRI 4648645


    /**
     * Returns the value of the app-name element in the application.xml if
     * it's defined. The default EE app name is the unqualified name of
     * the .ear or stand-alone module, minus the file extension.
     *
     * @return the EE app name of this application
     */
    public String getAppName() {
        return VersioningUtils.getUntaggedName(appName);
    }

    /**
     * Sets the EE app name
     * @param appName the EE app name of this application
     */
    public void setAppName(String appName) {
        this.appName = VersioningUtils.getUntaggedName(appName);
    }

    /**
     * Returns the value of the archive-name element in the sun-application.xml
     * When the app-name is not present in application.xml and archive-name is
     * present in sun-application.xml, the value of archive-name minus file
     * extension will be used as the default name of the app-name.
     *
     * @return the EE app name of this application
     */
    public String getArchiveName() {
        return archiveName;
    }

    /**
     * Sets the archive name using the archive-name element defined
     * in sun-application.xml
     * @param archiveName archiveName to calculate default EE6 app-name
     */
    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    @Override
    public String getCompatibility() {
        return compatValue;
    }

    @Override
    public void setCompatibility(String compatValue) {
        this.compatValue = compatValue;
    }


    /**
     * @return the initializeInOrder flag
     * when the return value is true, the modules inside ear will be loaded
     * by their declaration order in application.xml
     */
    public boolean isInitializeInOrder() {
        return initializeInOrder;
    }

    /**
     * Sets the initializeInOrder flag
     * @param initializeInOrder
     */
    public void setInitializeInOrder(boolean initializeInOrder) {
        this.initializeInOrder = initializeInOrder;
    }

    /**
     * Set the physical entity manager factory for a persistence unit
     * within this application.
     * This method takes a parameter called persistenceRootUri to support for
     * fully-qualified persistence-unit-name syntax within
     * persistence-unit-refs and persistence-context-refs. The syntax is similar
     * to ejb-link and messge-destination-link. See (EJB 3 core spec: 15.10.2)
     *
     * @param unitName           Name of the persistence-unit
     * @param persistenceRootUri uri of the root of the persistence.xml
     *                            (excluding META-INF) in which the persistence unit was defined.
     *                            This uri is relative to the top of the .ear.
     * @param emf                an entity manager factory.
     */
    public void addEntityManagerFactory(
            String unitName,
            String persistenceRootUri,
            EntityManagerFactory emf) {

        String fullyQualifiedUnitName = persistenceRootUri + PERSISTENCE_UNIT_NAME_SEPARATOR + unitName;

        // Always allow fully qualified lookup.
        entityManagerFactories.put(fullyQualifiedUnitName, emf);

        // Allow unqualified lookup, unless there are multiple .ear level
        // persistence units declaring the same persistence unit name. In that
        // case, only a fully-qualified lookup will work.  Note that even
        // though the entity manager factory map might contain more than one
        // key pointing to the same entity manager factory, the behavior
        // of getEntityManagerFactories() is not affected since it returns a Set.
        if (entityManagerFactoryUnitNames.contains(unitName)) {
            entityManagerFactories.remove(unitName);
        } else {
            entityManagerFactories.put(unitName, emf);
            entityManagerFactoryUnitNames.add(unitName);
        }
    }


    /**
     * Retrieve the physical entity manager factory associated with the
     * unitName of an application-level persistence unit. Returns null if
     * no matching entry is found.
     */
    public EntityManagerFactory getEntityManagerFactory(String unitName, BundleDescriptor declaringModule) {
        String lookupString = unitName;

        int separatorIndex = unitName.lastIndexOf(PERSISTENCE_UNIT_NAME_SEPARATOR);

        if (separatorIndex != -1) {
            String unqualifiedUnitName = unitName.substring(separatorIndex + 1);
            String path = unitName.substring(0, separatorIndex);

            String persistenceRootUri = getTargetUri(declaringModule, path);

            lookupString = persistenceRootUri + PERSISTENCE_UNIT_NAME_SEPARATOR + unqualifiedUnitName;
        }

        return entityManagerFactories.get(lookupString);
    }

    /**
     * Returns the set of physical entity manager factories associated with
     * persistence units in this application.
     *
     * @return Set of entity manager factories available in the app
     */
    @Override
    public Set<EntityManagerFactory> getEntityManagerFactories() {
        return new HashSet<>(entityManagerFactories.values());
    }

    /**
     * Return the set of roles used in this application. Currently, for release 1.0, it is an
     * * aggregation of all the roles in the sub-modules of the application.
     *
     * @return the Set of roles in the application.
     */
    @Override
    public Set<Role> getRoles() {
        Set<Role> roles = new HashSet<>();
        for (BundleDescriptor bd : getBundleDescriptors()) {
            if (bd != null) {
                roles.addAll(bd.getRoles());
            }
        }
        return roles;
    }

    /**
     * Return the set of org.glassfish.security.common.Role objects
     * I have (the ones defined in application xml).
     */
    public Set<Role> getAppRoles() {
        if (this.appRoles == null) {
            this.appRoles = new HashSet<>();
        }
        return this.appRoles;
    }


    // used by reflection, see ApplicationNode
    public void addAppRole(SecurityRoleDescriptor descriptor) {
        Role role = new Role(descriptor.getName(), descriptor.getDescription());
        getAppRoles().add(role);
    }


    /**
     * Adds a new abstract role
     */
    @Override
    public void addRole(Role role) {
        for (BundleDescriptor bd : getBundleDescriptors()) {
            bd.addRole(role);
        }
    }

    /**
     * Removes the given role.
     */
    @Override
    public void removeRole(Role role) {
        getAppRoles().remove(role);
        for (BundleDescriptor bd : getBundleDescriptors()) {
            bd.removeRole(role);
        }
    }

    /**
     * Reset the display name of this application.
     *
     * @param name the display name of the application.
     */
    @Override
    public void setName(String name) {
        // for deploying from NT to solaris & vice versa. This will
        // need to be cleaned when we clean up the backend for registering apps
        name = name.replace('/', '-').replace('\\', '-');
        super.setName(name);
        if (this.getRoleMapper() != null) {
            this.getRoleMapper().setName(name);
        }
    }

    public void setLibraryDirectory(String value) {
        libraryDirectory = value;
    }


    /**
     * Returns an "intelligent" value for the library directory setting, meaning
     * the current value if it has been set to a non-null, non-empty value;
     * the default value if the value has never been set, and null if the value
     * has been set to empty.
     *
     * @return String value of the library directory setting
     */
    public String getLibraryDirectory() {
        if (libraryDirectory == null) {
            return LIBRARY_DIRECTORY_DEFAULT_VALUE;
        }
        return libraryDirectory.isEmpty() ? null : libraryDirectory;
    }


    public String getLibraryDirectoryRawValue() {
        return libraryDirectory;
    }


    public void removeModule(ModuleDescriptor<BundleDescriptor> descriptor) {
        if (modules.contains(descriptor)) {
            if (descriptor.getDescriptor() != null) {
                descriptor.getDescriptor().setApplication(null);
            }
            modules.remove(descriptor);
        }
    }


    public void addModule(ModuleDescriptor<BundleDescriptor> descriptor) {
        modules.add(descriptor);
        if (descriptor.getDescriptor() != null) {
            descriptor.getDescriptor().setApplication(this);
        }
    }


    /**
     * Obtain a full set of module descriptors
     *
     * @return the set of bundle descriptors
     */
    public Set<ModuleDescriptor<BundleDescriptor>> getModules() {
        return modules;
    }


    /**
     * Get the uri of a target based on a source module and a
     * a relative uri from the perspective of that source module.
     *
     * @param origin bundle descriptor within this application
     * @param relativeTargetUri relative uri from the given bundle descriptor
     * @return target uri
     */
    public String getTargetUri(BundleDescriptor origin, String relativeTargetUri) {
        try {
            String archiveUri = origin.getModuleDescriptor().getArchiveUri();
            URI originUri = new URI(archiveUri);
            URI resolvedUri = originUri.resolve(relativeTargetUri);
            return resolvedUri.getPath();
        } catch (URISyntaxException use) {
            LOG.log(Level.FINE, "origin " + origin + " has invalid syntax", use);
            return null;
        }
    }


    /**
     * Get a target bundle descriptor based on an input bundle descriptor and
     * a relative uri from the perspective of the input bundle descriptor.
     *
     * @param origin bundle descriptor within this application
     * @param relativeTargetUri relative uri from the given bundle descriptor
     *            to another bundle within the application.
     * @return target BundleDescriptor or null if not found.
     */
    public BundleDescriptor getRelativeBundle(BundleDescriptor origin, String relativeTargetUri) {
        String targetBundleUri = getTargetUri(origin, relativeTargetUri);
        return targetBundleUri == null ? null : getModuleByUri(targetBundleUri);
    }


    /**
     * Return the relative uri between two modules, from the perspective
     * of the first bundle.
     *
     * @return relative uri or empty string if the two bundles are the same
     */
    public String getRelativeUri(BundleDescriptor origin, BundleDescriptor target) {
        String originUri = origin.getModuleDescriptor().getArchiveUri();
        String targetUri = target.getModuleDescriptor().getArchiveUri();

        StringTokenizer tokenizer = new StringTokenizer(originUri, "/");
        int numTokens = tokenizer.countTokens();
        int numSeparators = numTokens > 0 ? numTokens - 1 : 0;

        StringBuilder relativeUri = new StringBuilder();

        // The simplest way to compute a relative uri is to add one "../"
        // for each sub-path in the origin URI, then add the target URI.
        // It's possible for the result to not be normalized if the origin
        // and target have at least one common root, but that shouldn't
        // matter as long as when the relative URI is resolved against the
        // origin it produces the target.
        for (int i = 0; i < numSeparators; i++) {
            relativeUri.append("../");
        }

        relativeUri.append(targetUri);
        return relativeUri.toString();
    }


    /**
     * Lookup module by uri.
     *
     * @param uri the module path in the application archive
     * @return a bundle descriptor in this application identified by uri
     *         or null if not found.
     */
    public ModuleDescriptor<BundleDescriptor> getModuleDescriptorByUri(String uri) {
        for (ModuleDescriptor<BundleDescriptor> aModule : getModules()) {
            if (aModule.getArchiveUri().equals(uri)) {
                return aModule;
            }
        }
        return null;
    }


    /**
     * Lookup module by uri.
     *
     * @return a bundle descriptor in this application identified by uri
     *         or null if not found.
     */
    public Collection<ModuleDescriptor<BundleDescriptor>> getModuleDescriptorsByType(ArchiveType type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        LinkedList<ModuleDescriptor<BundleDescriptor>> results = new LinkedList<>();
        for (ModuleDescriptor<BundleDescriptor> aModule : getModules()) {
            if (type.equals(aModule.getModuleType())) {
                results.add(aModule);
            }
        }
        return results;
    }


    /**
     * Lookup module by uri.
     *
     * @param uri the module path in the application archive
     * @return a bundle descriptor in this application identified by uri
     *         or null if not found.
     */
    public BundleDescriptor getModuleByUri(String uri) {
        ModuleDescriptor<BundleDescriptor> md = getModuleDescriptorByUri(uri);
        if (md != null) {
            return md.getDescriptor();
        }
        return null;
    }


    /**
     * @param type the module type
     * @param uri  the module path in the application archive
     * @return a bundle descriptor in this application identified by its type and uri
     */
    public <T extends BundleDescriptor> T getModuleByTypeAndUri(Class<T> type, String uri) {
        for (ModuleDescriptor<BundleDescriptor> aModule : getModules()) {
            try {
                T descriptor = type.cast(aModule.getDescriptor());
                if (descriptor.getModuleDescriptor().getArchiveUri().equals(uri)) {
                    return descriptor;
                }
            } catch(ClassCastException e) {
                // ignore
            }
        }
        return null;
    }


    /**
     * Obtain the EJB in this application of the given display name. If the EJB is not
     * present, throw an IllegalArgumentException.
     *
     * @param ejbName the name of the bean
     * @return the EjbDescriptor object with the given display name
     */
    public EjbDescriptor getEjbByName(String ejbName) {
        for (EjbBundleDescriptor ejbd : getBundleDescriptors(EjbBundleDescriptor.class)) {
            if (ejbd.hasEjbByName(ejbName)) {
                return ejbd.getEjbByName(ejbName);
            }
        }
        throw new IllegalArgumentException(I18N.getLocalString(
            "enterprise.deployment.exceptionapphasnobeannamed", "This application has no beans of name {0}", ejbName));
    }


    public EjbDescriptor getEjbByName(String moduleName, String ejbName) {
        for (EjbBundleDescriptor ejbd : getBundleDescriptors(EjbBundleDescriptor.class)) {
            if (Objects.equals(moduleName, ejbd.getModuleDescriptor().getModuleName())) {
                if (ejbd.hasEjbByName(ejbName)) {
                    return ejbd.getEjbByName(ejbName);
                }
            }
        }
        throw new IllegalArgumentException(I18N.getLocalString("enterprise.deployment.exceptionapphasnobeannamed",
                                                               "This application has no beans of name {0} in module {1}",
                                                               ejbName,
                                                               moduleName));
    }

    /**
     * Return whether the application contains the given ejb by name..
     *
     * @param ejbName the name of the bean
     * @return true if there is a bean matching the given name
     */
    public boolean hasEjbByName(String ejbName) {
        for (EjbBundleDescriptor ebd : getBundleDescriptors(EjbBundleDescriptor.class)) {
            if (ebd.hasEjbByName(ejbName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEjbByName(String moduleName, String ejbName) {
        for (EjbBundleDescriptor ebd : getBundleDescriptors(EjbBundleDescriptor.class)) {
            if (Objects.equals(moduleName, ebd.getModuleDescriptor().getModuleName())) {
                if (ebd.hasEjbByName(ejbName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * if this application object is virtual, return the standalone
     * bundle descriptor it is wrapping otherwise return null
     *
     * @return the wrapped standalone bundle descriptor
     */
    public BundleDescriptor getStandaloneBundleDescriptor() {
        if (!isVirtual()) {
            return null;
        }
        if (getModules().size() > 1) {
            // this is an error, the application is virtual,
            // which mean a wrapper for a standalone module and
            // it seems I have more than one module in my list...
            throw new IllegalStateException("Virtual application contains more than one module");
        }
        return getModules().iterator().next().getDescriptor();
    }


    /**
     * Obtain a full set of bundle descriptors for a particular type
     *
     * @param type the bundle descriptor type requested
     * @return the set of bundle descriptors
     */
    public <T extends BundleDescriptor> Set<T> getBundleDescriptors(Class<T> type) {
        if (type == null) {
            return null;
        }
        Set<T> bundleSet = new OrderedSet<>();
        for (ModuleDescriptor<BundleDescriptor> aModule : getModules()) {
            try {
                T descriptor = type.cast(aModule.getDescriptor());
                bundleSet.add(descriptor);
            } catch(ClassCastException e) {
                // ignore
            }
            // any children, this need to happen outside of the casting as the parent
            // type has nothing to do with the children extensions.
            if (aModule.getDescriptor() != null) {
                bundleSet.addAll(aModule.getDescriptor().getExtensionsDescriptors(type));
            }
        }
        return bundleSet;
    }

    /**
     * Obtain a full set of bundle descriptors for a particular type
     *
     * @param bundleType the bundle descriptor type requested
     * @return the set of bundle descriptors
     */
    public Set<BundleDescriptor> getBundleDescriptorsOfType(ArchiveType bundleType) {
        if (bundleType == null) {
            return Collections.emptySet();
        }
        Set<BundleDescriptor> bundleSet = new OrderedSet<>();
        for (ModuleDescriptor<BundleDescriptor> aModule : getModules()) {
            if (Objects.equals(aModule.getDescriptor().getModuleType(), bundleType)) {
                bundleSet.add(aModule.getDescriptor());
            }
            for (RootDeploymentDescriptor rd : aModule.getDescriptor().getExtensionsDescriptors()) {
                if (rd instanceof BundleDescriptor) {
                    BundleDescriptor bundleDescriptor = (BundleDescriptor) rd;
                    if (Objects.equals(bundleDescriptor.getModuleType(), bundleType)) {
                        bundleSet.add(bundleDescriptor);
                    }
                }
            }
        }
        return bundleSet;
    }


    /**
     * Obtain a set of all bundle descriptors, regardless of type
     *
     * @return the set of bundle descriptors
     */
    public Set<BundleDescriptor> getBundleDescriptors() {
        Set<BundleDescriptor> bundleSet = new OrderedSet<>();
        for (ModuleDescriptor<BundleDescriptor> aModule : getModules()) {
            BundleDescriptor bundleDesc = aModule.getDescriptor();
            if (bundleDesc == null) {
                LOG.log(Level.FINE, () -> "Null descriptor for module " + aModule);
                continue;
            }
            bundleSet.add(bundleDesc);
            for (RootDeploymentDescriptor rd : bundleDesc.getExtensionsDescriptors()) {
                if (rd instanceof BundleDescriptor) {
                    bundleSet.add((BundleDescriptor) rd);
                }
            }
        }
        return bundleSet;
    }


    /**
     * Add a bundle descriptor to this application.
     *
     * @param bundleDescriptor the bundle descriptor to add
     */
    @Override
    public void addBundleDescriptor(BundleDescriptor bundleDescriptor) {
        ModuleDescriptor<BundleDescriptor> newModule = bundleDescriptor.getModuleDescriptor();
        addModule(newModule);
    }


    /**
     * Remove a web bundle descriptor from this application.
     *
     * @param bundleDescriptor the web bundle descriptor to remove
     */
    public void removeBundleDescriptor(BundleDescriptor bundleDescriptor) {
        bundleDescriptor.setApplication(null);
        getBundleDescriptors().remove(bundleDescriptor);
    }

    /**
     * Return the Vector of ejb deployment objects.
     */
    public Vector<EjbDescriptor> getEjbDescriptors() {
        Vector<EjbDescriptor> ejbDescriptors = new Vector<>();
        for (EjbBundleDescriptor ejbBundleDescriptor : getBundleDescriptors(EjbBundleDescriptor.class)) {
            ejbDescriptors.addAll(ejbBundleDescriptor.getEjbs());
        }

        return ejbDescriptors;
    }


    /**
     * Returns all the ejb descriptor in this application in ordered form.
     * The comparison is done based on the descriptor's name.
     *
     * @return all ejb descriptors in ordered form
     */
    public EjbDescriptor[] getSortedEjbDescriptors() {
        Comparator<EjbDescriptor> urlComparator = Comparator
            .comparing(desc -> desc.getEjbBundleDescriptor().getModuleDescriptor().getArchiveUri());
        Comparator<EjbDescriptor> nameComparator = urlComparator.thenComparing(EjbDescriptor::getName);
        return getEjbDescriptors().stream().sorted(nameComparator).toArray(EjbDescriptor[]::new);
    }


    /**
     * Sets the virtual status of this application.
     * If this application object represents a stand alone module,
     * virtaul status should be true; else false.
     *
     * @param virtual new value of this application's virtaul status
     */
    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    /**
     * Returns the virtual status of this application.
     *
     * @return true if this application obj represents a stand alone module
     */
    public boolean isVirtual() {
        return this.virtual;
    }

    public boolean isUniqueIdSet() {
        return uniqueIdSet;
    }

    /**
     * Sets the unique id for this application.  It traverses through all
     * the  ejbs in the application and sets the unique id for each of them.
     * The traversal is done in ascending element order.
     *
     * NOTE : assumption is that the id has already been left shifted 16
     *        bits to allow space for the component ids.
     *
     * @param id unique id for this application
     */
    public void setUniqueId(long id) {
        LOG.log(Level.FINE, () -> "setUniqueId called for application " + getName() + " with value: " + id);
        this.uniqueId = id;

        EjbDescriptor[] descs = getSortedEjbDescriptors();

        for (int i = 0; i < descs.length; i++) {
            // Maximum of 2^16 beans max per application
            descs[i].setUniqueId((id | i));
            if (LOG.isLoggable(Level.FINE)) {
                String module = descs[i].getEjbBundleDescriptor().getModuleDescriptor().getArchiveUri();
                LOG.log(Level.FINE, "Ejb  " + module + ":" + descs[i].getName() + " id = " +
                        descs[i].getUniqueId());
            }
        }

        uniqueIdSet = true;
    }

    /**
     * Returns the unique id used for this application.
     *
     * @return unique id used for this application
     */
    public long getUniqueId() {
        return this.uniqueId;
    }
    // END OF IASRI 4645310

    // START IASRI 4662001, 4720955

    /**
     * Sets the pass-by-reference property for this application.
     * EJB spec requires pass-by-value (false) which is the default.
     * This can be set to true for non-compliant operation and possibly
     * higher performance.  For a stand-alone  server, this can be used.
     * By setting pass-by-reference in sun-application.xml, it can apply to
     * all the enclosed ejb modules.
     *
     * @param passByReference boolean true or false - pass-by-reference property of application.
     *                        true - application is pass-by-reference
     *                        false - application is pass-by-value
     */
    public void setPassByReference(boolean passByReference) {
        this.passByReference = Boolean.valueOf(passByReference);
    }

    /**
     * Gets the value of pass-by-reference property for this application
     * Checks to see if the pass-by-reference property is defined.  If
     * this application's pass-by-reference property is defined, this method
     * returns the value of the application's pass-by-reference property.
     * Otherwise, if the application's pass-by-reference property is undefined,
     * this method returns a default value of false.
     *
     * @return boolean pass-by-reference property for this application
     */
    public boolean getPassByReference() {
        if (this.isPassByReferenceDefined()) {
            return this.passByReference.booleanValue();
        }
        return false;
    }


    /**
     * Determines if the application's pass-by-reference property has been
     * defined or undefined in sun-application.xml
     *
     * @return true - pass-by-reference is defined in sun-application.xml
     *         false - pass-by-reference is undefined in sun-application.xml
     */
    public boolean isPassByReferenceDefined() {
        return this.passByReference != null;
    }


    /**
     * Add all the deployment information about the given application to me.
     */
    public void addApplication(Application application) {
        for (ModuleDescriptor<BundleDescriptor> md : application.getModules()) {
            addModule(md);
        }
    }


    /**
     * Sets the mapping of rolename to users and groups on a particular server.
     */
    public void setRoleMapper(SecurityRoleMapper roleMapper) {
        // should verify against the roles
        this.roleMapper = roleMapper;
    }


    /**
     * Return true if I have information to do with deployment on a
     * particular operational environment.
     */
    public boolean hasRuntimeInformation() {
        return true;
    }


    /**
     * Return my mapping of rolename to users and groups on a particular
     * server.
     */
    @Override
    public SecurityRoleMapper getRoleMapper() {
        if (this.roleMapper == null) {
            if (securityRoleMapperFactory == null) {
                LOG.log(Level.FINE, "SecurityRoleMapperFactory NOT set.");
            } else {
                this.roleMapper = securityRoleMapperFactory.getRoleMapper(this.getName());
            }
        }
        return this.roleMapper;
    }


    /**
     * Sets the realm for this application
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }


    /**
     * @return the realm for this application
     */
    public String getRealm() {
        return realm;
    }


    /**
     * A flag to indicate that my data has changed since the last save.
     */
    public boolean isDirty() {
        return this.isDirty;
    }


    /**
     * @return the class loader associated with this application
     */
    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }


    /**
     * A formatted String representing my state.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("Application");
        toStringBuffer.append("\n");
        super.print(toStringBuffer);
        toStringBuffer.append("\n smallIcon ").append(super.getSmallIconUri());
        for (ModuleDescriptor<BundleDescriptor> aModule : getModules()) {
            toStringBuffer.append("\n  Module : ");
            aModule.print(toStringBuffer);
        }
        toStringBuffer.append("\n Bundles: \n");
        printDescriptorSet(this.getBundleDescriptors(), toStringBuffer);
        toStringBuffer.append("\n roles ").append(getRoles());
        toStringBuffer.append("\n RoleMapper ").append(this.getRoleMapper());
        toStringBuffer.append("\n Realm ").append(realm);
    }


    private void printDescriptorSet(Set<BundleDescriptor> descSet, StringBuffer sbuf) {
        for (BundleDescriptor obj : descSet) {
            obj.print(sbuf);
        }
    }


    /**
     * visit the descriptor and all sub descriptors with a DOL visitor implementation
     *
     * @param aVisitor visitor to traverse the descriptors
     */
    @Override
    public void visit(DescriptorVisitor aVisitor) {
        if (aVisitor instanceof ApplicationVisitor) {
            visit((ComponentVisitor) aVisitor);
        } else {
            super.visit(aVisitor);
        }
    }


    /**
     * @return the module ID for this module descriptor
     */
    @Override
    public String getModuleID() {
        if (appName != null) {
            return appName;
        }
        return getRawModuleID();
    }


    /**
     * @return true if this module is an application object
     */
    @Override
    public boolean isApplication() {
        return true;
    }


    /**
     * @return the module type for this bundle descriptor
     */
    @Override
    public ArchiveType getModuleType() {
        return DOLUtils.earType();
    }


    public void addSecurityRoleMapping(SecurityRoleMapping roleMapping) {
        roleMaps.add(roleMapping);
    }


    public List<SecurityRoleMapping> getSecurityRoleMappings() {
        return roleMaps;
    }


    public List<SecurityRoleAssignment> getWlRoleAssignments() {
        return wlRoleAssignments;
    }


    public void addWLRoleAssignments(SecurityRoleAssignment wlRoleAssignment) {
        wlRoleAssignments.add(wlRoleAssignment);
    }


    /**
     * This method records how this Application object is constructed.  We
     * keep this information to avoid additional disk access in
     * DescriptorArchivist.write() when deciding if the application.xml
     * should be copied or written to the generated/xml directory.
     */
    public void setLoadedFromApplicationXml(boolean bool) {
        loadedFromApplicationXml = bool;
    }

    /**
     * @return true if this Application is from reading application.xml from
     *         disk;  false if this Application object is derived from the content
     *         of the ear file.
     */
    public boolean isLoadedFromApplicationXml() {
        return loadedFromApplicationXml;
    }


    /**
     * add a resource-adapter to referred resource-adapters list
     *
     * @param raName resource-adapter name
     */
    public void addResourceAdapter(String raName) {
        resourceAdapters.add(raName);
    }


    /**
     * get the list of resource-adapters referred by the application
     *
     * @return resource-adapters list
     */
    public Set<String> getResourceAdapters() {
        return resourceAdapters;
    }


    /**
     * @return the Set of application paramaters.
     */
    public Set<ApplicationParam> getApplicationParams() {
        return applicationParams;
    }


    /**
     * Adds a new context parameter to my list.
     */
    public void addApplicationParam(ApplicationParam appParam) {
        applicationParams.add(appParam);
    }


    @Override
    public boolean getKeepState() {
        // for standalone module, get the keep-state value specified in
        // module glassfish-*.xml
        if (isVirtual()) {
            BundleDescriptor bundleDesc = getStandaloneBundleDescriptor();
            if (bundleDesc != null) {
                return bundleDesc.getKeepState();
            }
        }
        return super.getKeepState();
    }


    /**
     * Returns the resolved keepstate value.
     *
     * @return keepStateResolved
     */
    public boolean getKeepStateResolved() {
        return keepStateResolved;
    }


    /**
     * Sets the resolved keepstate value.
     *
     * @param keepStateResolved
     */
    public void setKeepStateResolved(String keepStateResolved) {
        this.keepStateResolved = Boolean.parseBoolean(keepStateResolved);
    }
}
