/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.types.EjbReference;

import jakarta.ejb.Schedule;
import jakarta.ejb.Timeout;
import jakarta.interceptor.AroundInvoke;

import java.lang.System.Logger;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
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

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.glassfish.security.common.Role;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

/**
 * @author Danny Coward
 * @author Sanjeev Krishnan
 * @author David Matejcek
 */
public abstract class EjbBeanDescriptor extends CommonResourceDescriptor implements EjbDescriptor {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = System.getLogger(EjbBeanDescriptor.class.getName());

    private String ejbClassName;
    private SimpleJndiName jndiName = SimpleJndiName.of("");
    private String mappedName;

    private String transactionType;
    private long uniqueId;

    private String homeClassName;
    private boolean localBean;
    private final Set<String> localBusinessClassNames;
    private String localClassName;
    private String localHomeClassName;
    private final Set<LifecycleCallbackDescriptor> postConstructDescs = new HashSet<>();
    private final Set<LifecycleCallbackDescriptor> preDestroyDescs = new HashSet<>();
    private final Set<String> remoteBusinessClassNames;
    private String remoteClassName;
    private MethodDescriptor timedObjectMethod;
    private List<MethodDescriptor> timerMethodDescriptors = new ArrayList<>();
    private List<ScheduledTimerDescriptor> timerSchedules = new ArrayList<>();

    // For EJB2.0
    private RunAsIdentityDescriptor runAsIdentity;
    private Boolean usesCallerIdentity;
    private final Set<RoleReference> roleReferences = new HashSet<>();

    /**
     * This is the value of the EJB 2.1 deployment descriptor entry for service endpoint interface.
     */
    private String webServiceEndpointInterfaceName;

    /** If non-null, refer all environment refs here */
    private WritableJndiNameEnvironment env;

    private final Set<EjbReferenceDescriptor> ejbReferences = new HashSet<>();
    private final Set<EntityManagerFactoryReferenceDescriptor> entityManagerFactoryReferences = new HashSet<>();
    private final Set<EntityManagerReferenceDescriptor> entityManagerReferences = new HashSet<>();
    private final Set<EnvironmentProperty> environmentProperties = new HashSet<>();
    /**
     * The set of all interceptor classes applicable to this bean. This includes any interceptor
     * class that is present at *either* the class level or method-level.
     */
    private final Set<EjbInterceptor> allInterceptorClasses = new HashSet<>();
    /** Ordered list of class-level interceptors for this bean. */
    private final List<EjbInterceptor> interceptorChain = new LinkedList<>();
    /**
     * Interceptor info per business method. If the map does not contain an entry for the business
     * method, there is no method-specific interceptor information for that method.
     * In that case the standard class-level interceptor information applies.
     * If there is an entry for the business method, the corresponding list represents
     * the *complete* ordered list of interceptor classes for that method. An empty list would mean
     * all the interceptors have been disabled for that particular business method.
     */
    private final Map<MethodDescriptor, List<EjbInterceptor>> methodInterceptorsMap = new HashMap<>();

    private final Set<EjbIORConfigurationDescriptor> iorConfigDescriptors = new OrderedSet<>();
    private final Set<MessageDestinationReferenceDescriptor> messageDestReferences = new HashSet<>();
    private final HashMap<MethodPermission, ArrayList<MethodDescriptor>> methodPermissionsFromDD = new HashMap<>();
    private final Map<MethodPermission, Set<MethodDescriptor>> permissionedMethodsByPermission = new Hashtable<>();
    /**
     * Sets of method descriptor that can be of style 1 or style 2.
     * Ee initialize it so we force at least on method conversion
     * to fill up unspecified method with the unchecked permission
     */
    private Map<MethodDescriptor, Set<MethodPermission>> styledMethodDescriptors = new HashMap<>();
    private final Set<ResourceEnvReferenceDescriptor> resourceEnvReferences = new HashSet<>();
    private final Set<ResourceReferenceDescriptor> resourceReferences = new HashSet<>();
    private final Set<ServiceReferenceDescriptor> serviceReferences = new HashSet<>();


    /**
     * Default constructor.
     */
    protected EjbBeanDescriptor() {
        this.localBusinessClassNames = new HashSet<>();
        this.remoteBusinessClassNames = new HashSet<>();
    }


    /**
     * The copy constructor.
     *
     * @param other the source descriptor
     */
    public EjbBeanDescriptor(EjbBeanDescriptor other) {
        super(other);
        this.jndiName = other.jndiName;

        this.homeClassName = other.homeClassName;
        this.localBean = other.localBean;
        this.localBusinessClassNames = new HashSet<>(other.localBusinessClassNames);
        this.localClassName = other.localClassName;
        this.localHomeClassName = other.localHomeClassName;
        this.remoteBusinessClassNames = new HashSet<>(other.remoteBusinessClassNames);
        this.remoteClassName = other.remoteClassName;

        this.webServiceEndpointInterfaceName = other.webServiceEndpointInterfaceName;
    }


    @Override
    public Application getApplication() {
        if (getEjbBundleDescriptor() == null) {
            return null;
        }
        return getEjbBundleDescriptor().getApplication();
    }


    @Override
    public final String getEjbClassName() {
        return this.ejbClassName;
    }


    /**
     * Sets the classname of the ejb.
     *
     * @param ejbClassName class name of the EJB
     */
    public void setEjbClassName(String ejbClassName) {
        this.ejbClassName = ejbClassName;
    }


    @Override
    public SimpleJndiName getJndiName() {
        if (this.jndiName == null) {
            this.jndiName = new SimpleJndiName("");
        }
        return jndiName.isEmpty() ? new SimpleJndiName(getMappedName()) : jndiName;
    }


    @Override
    public void setJndiName(SimpleJndiName jndiName) {
        this.jndiName = jndiName;
        if (this.getName().isEmpty()) {
            super.setName(jndiName == null ? null : jndiName.toString());
        }
    }


    /**
     * @return global mapped name
     */
    public String getMappedName() {
        return mappedName == null ? "" : mappedName;
    }


    /**
     * @param mappedName global mapped name
     */
    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }


    /**
     * @return EJB name
     */
    @Override
    public String getName() {
        return super.getName();
    }


    @Override
    public long getUniqueId() {
        return uniqueId;
    }


    @Override
    public void setUniqueId(long id) {
        uniqueId = id;
    }


    @Override
    public boolean isRemoteInterfacesSupported() {
        return getHomeClassName() != null;
    }


    @Override
    public String getHomeClassName() {
        return this.homeClassName;
    }


    /**
     * @param homeClassName classname of the Home interface of this ejb.
     */
    public void setHomeClassName(String homeClassName) {
        this.homeClassName = homeClassName;
    }


    @Override
    public boolean isLocalBean() {
        return localBean;
    }


    /**
     * @param localBean if this is an EJB provides a no interface Local view.
     */
    public void setLocalBean(boolean localBean) {
        this.localBean = localBean;
    }


    @Override
    public boolean isLocalBusinessInterfacesSupported() {
        return !localBusinessClassNames.isEmpty();
    }


    @Override
    public Set<String> getLocalBusinessClassNames() {
        return new HashSet<>(localBusinessClassNames);
    }


    /**
     * @param className local business interface name
     */
    public void addLocalBusinessClassName(String className) {
        localBusinessClassNames.add(className);
    }


    @Override
    public String getLocalClassName() {
        return localClassName;
    }


    /**
     * Sets the classname for the local interface of this ejb
     *
     * @param localClassName fully qualified class name for the interface
     */
    public void setLocalClassName(String localClassName) {
        this.localClassName = localClassName;
    }


    @Override
    public boolean isLocalInterfacesSupported() {
        return getLocalHomeClassName() != null;
    }


    @Override
    public final String getLocalHomeClassName() {
        return localHomeClassName;
    }


    /**
     * Sets the classname for the local home interface of this ejb
     *
     * @param localHomeClassName fully qualified class name for the interface
     */
    public void setLocalHomeClassName(String localHomeClassName) {
        this.localHomeClassName = localHomeClassName;
    }


    @Override
    public final Set<EjbReferenceDescriptor> getEjbReferenceDescriptors() {
        if (env == null) {
            return ejbReferences;
        }
        return env.getEjbReferenceDescriptors();
    }


    @Override
    public EjbReferenceDescriptor getEjbReference(String name) {
        for (EjbReferenceDescriptor reference : getEjbReferenceDescriptors()) {
            if (reference.getName().equals(name)) {
                return reference;
            }
        }
        throw new IllegalArgumentException(
            MessageFormat.format("This bean {0} has no ejb reference by the name of {1}", getName(), name));
    }


    @Override
    public final void addEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
        try {
            EjbReference existing = getEjbReference(ejbReference.getName());
            for (InjectionTarget next : ejbReference.getInjectionTargets()) {
                existing.addInjectionTarget(next);
            }
        } catch (IllegalArgumentException e) {
            if (env == null) {
                ejbReferences.add(ejbReference);
            } else {
                env.addEjbReferenceDescriptor(ejbReference);
            }
            ejbReference.setReferringBundleDescriptor(getEjbBundleDescriptor());
        }
    }


    @Override
    public final List<InjectionCapable> getInjectableResourcesByClass(String className) {
        if (env == null) {
            return getEjbBundleDescriptor().getInjectableResourcesByClass(className, this);
        }
        return env.getInjectableResourcesByClass(className);
    }


    @Override
    public final InjectionInfo getInjectionInfoByClass(Class<?> clazz) {
        if (env == null) {
            return getEjbBundleDescriptor().getInjectionInfoByClass(clazz, this);
        }
        return env.getInjectionInfoByClass(clazz);
    }


    @Override
    public final Set<MessageDestinationReferenceDescriptor> getMessageDestinationReferenceDescriptors() {
        if (env == null) {
            return messageDestReferences;
        }
        return env.getMessageDestinationReferenceDescriptors();
    }


    @Override
    public final void removeEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
        if (env == null) {
            ejbReferences.remove(ejbReference);
        } else {
            env.removeEjbReferenceDescriptor(ejbReference);
        }
        ejbReference.setReferringBundleDescriptor(null);
    }


    @Override
    public final Set<EnvironmentProperty> getEnvironmentProperties() {
        if (env == null) {
            return environmentProperties;
        }
        return env.getEnvironmentProperties();
    }


    @Override
    public final Set<EntityManagerFactoryReferenceDescriptor> getEntityManagerFactoryReferenceDescriptors() {
        if (env == null) {
            return entityManagerFactoryReferences;
        }
        return env.getEntityManagerFactoryReferenceDescriptors();
    }


    @Override
    public final EntityManagerFactoryReferenceDescriptor getEntityManagerFactoryReferenceByName(String name) {
        if (env != null) {
            return env.getEntityManagerFactoryReferenceByName(name);
        }
        for (EntityManagerFactoryReferenceDescriptor reference : getEntityManagerFactoryReferenceDescriptors()) {
            if (reference.getName().equals(name)) {
                return reference;
            }
        }
        throw new IllegalArgumentException(MessageFormat
            .format("This ejb {0} has no entity manager factory reference by the name of {1}", getName(), name));
    }


    @Override
    public final void addEntityManagerFactoryReferenceDescriptor(EntityManagerFactoryReferenceDescriptor reference) {
        try {
            String refName = reference.getName();
            EntityManagerFactoryReferenceDescriptor existing = getEntityManagerFactoryReferenceByName(refName);
            for (InjectionTarget injectionTarget : reference.getInjectionTargets()) {
                existing.addInjectionTarget(injectionTarget);
            }
        } catch (IllegalArgumentException e) {
            EjbBundleDescriptor bundleDescriptor = getEjbBundleDescriptor();
            if (bundleDescriptor != null) {
                reference.setReferringBundleDescriptor(bundleDescriptor);
            }
            if (env == null) {
                entityManagerFactoryReferences.add(reference);
            } else {
                env.addEntityManagerFactoryReferenceDescriptor(reference);
            }
        }
    }


    @Override
    public final Set<EntityManagerReferenceDescriptor> getEntityManagerReferenceDescriptors() {
        if (env == null) {
            return entityManagerReferences;
        }
        return env.getEntityManagerReferenceDescriptors();
    }


    @Override
    public final EntityManagerReferenceDescriptor getEntityManagerReferenceByName(String name)
        throws IllegalArgumentException {
        if (env != null) {
            return env.getEntityManagerReferenceByName(name);
        }
        for (EntityManagerReferenceDescriptor reference : getEntityManagerReferenceDescriptors()) {
            if (reference.getName().equals(name)) {
                return reference;
            }
        }
        throw new IllegalArgumentException(
            MessageFormat.format("This ejb {0} has no entity manager reference by the name of {1}", getName(), name));
    }


    @Override
    public final void addEntityManagerReferenceDescriptor(EntityManagerReferenceDescriptor reference) {
        try {
            EntityManagerReferenceDescriptor existing = this.getEntityManagerReferenceByName(reference.getName());
            for (InjectionTarget next : reference.getInjectionTargets()) {
                existing.addInjectionTarget(next);
            }
        } catch (IllegalArgumentException e) {
            if (getEjbBundleDescriptor() != null) {
                reference.setReferringBundleDescriptor(getEjbBundleDescriptor());
            }
            if (env == null) {
                getEntityManagerReferenceDescriptors().add(reference);
            } else {
                env.addEntityManagerReferenceDescriptor(reference);
            }
        }
    }


    @Override
    public final EnvironmentProperty getEnvironmentPropertyByName(String name) {
        if (env != null) {
            return env.getEnvironmentPropertyByName(name);
        }
        for (EnvironmentProperty property : getEnvironmentProperties()) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        throw new IllegalArgumentException(
            MessageFormat.format("This bean {0} has no environment property by the name of {1}", getName(), name));
    }


    @Override
    public final void addEnvironmentProperty(EnvironmentProperty environmentProperty) {
        if (env != null) {
            env.addEnvironmentProperty(environmentProperty);
            return;
        }
        if (environmentProperties.contains(environmentProperty)) {
            removeEnvironmentProperty(environmentProperty);
            addEnvironmentProperty(environmentProperty);
        } else {
            environmentProperties.add(environmentProperty);
        }
    }


    @Override
    public final void removeEnvironmentProperty(EnvironmentProperty environmentProperty) {
        if (env == null) {
            getEnvironmentProperties().remove(environmentProperty);
        } else {
            env.removeEnvironmentProperty(environmentProperty);
        }
    }


    /**
     * @return an unordered set of the names of all interceptor classes for this bean. This list
     *         does not include the name of the bean class itself, even if the bean class declares
     *         {@link AroundInvoke} methods and/or callbacks.
     */
    public Set<String> getInterceptorClassNames() {
        HashSet<String> classNames = new HashSet<>();
        for (EjbInterceptor interceptor : getInterceptorClasses()) {
            classNames.add(interceptor.getInterceptorClassName());
        }
        return classNames;
    }


    /**
     * @return a new unordered set of interceptor descriptors for this bean. This list does not
     *         include interceptor info for the bean class itself, even if the bean class declares
     *         {@link AroundInvoke} methods and/or callbacks.
     */
    public Set<EjbInterceptor> getInterceptorClasses() {
        return new HashSet<>(allInterceptorClasses);
    }


    @Override
    public boolean hasInterceptorClass(String interceptorClassName) {
        for (String interceptor : getInterceptorClassNames()) {
            if (interceptor.equals(interceptorClassName)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void addInterceptorClass(EjbInterceptor interceptor) {
        allInterceptorClasses.add(interceptor);
    }


    protected void setInterceptorClasses(Collection<EjbInterceptor> classes) {
        allInterceptorClasses.clear();
        allInterceptorClasses.addAll(classes);
    }


    /**
     * @return new list of {@link EjbInterceptor}s
     */
    public final List<EjbInterceptor> getInterceptorChain() {
        return new LinkedList<>(interceptorChain);
    }


    @Override
    public void appendToInterceptorChain(List<EjbInterceptor> chain) {
        interceptorChain.addAll(chain);
    }


    protected void setInterceptorChain(List<EjbInterceptor> interceptors) {
        interceptorChain.clear();
        interceptorChain.addAll(interceptors);
    }


    /**
     * @return new map
     */
    public final Map<MethodDescriptor, List<EjbInterceptor>> getMethodInterceptorsMap() {
        return new HashMap<>(methodInterceptorsMap);
    }


    protected void setMethodInterceptorsMap(Map<MethodDescriptor, List<EjbInterceptor>> map) {
        methodInterceptorsMap.clear();
        methodInterceptorsMap.putAll(map);
    }


    @Override
    public Set<EjbIORConfigurationDescriptor> getIORConfigurationDescriptors() {
        return iorConfigDescriptors;
    }


    public void addIORConfigurationDescriptor(EjbIORConfigurationDescriptor val) {
        iorConfigDescriptors.add(val);
    }


    @Override
    public final MessageDestinationReferenceDescriptor getMessageDestinationReferenceByName(String name) {
        if (env != null) {
            return env.getMessageDestinationReferenceByName(name);
        }
        for (MessageDestinationReferenceDescriptor mdr : messageDestReferences) {
            if (mdr.getName().equals(name)) {
                return mdr;
            }
        }
        throw new IllegalArgumentException(MessageFormat
            .format("This ejb [{0}] has no message destination reference by the name of [{1}]", getName(), name));
    }


    @Override
    public final void addMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor messageDestRef) {
        try {
            MessageDestinationReferenceDescriptor existing = getMessageDestinationReferenceByName(messageDestRef.getName());
            for (InjectionTarget injectionTarget : messageDestRef.getInjectionTargets()) {
                existing.addInjectionTarget(injectionTarget);
            }
        } catch (IllegalArgumentException e) {
            if (env == null) {
                messageDestReferences.add(messageDestRef);
            } else {
                env.addMessageDestinationReferenceDescriptor(messageDestRef);
            }
            if (getEjbBundleDescriptor() != null) {
                messageDestRef.setReferringBundleDescriptor(getEjbBundleDescriptor());
            }
        }
    }


    @Override
    public final void removeMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor msgDestRef) {
        if (env == null) {
            messageDestReferences.remove(msgDestRef);
        } else {
            env.removeMessageDestinationReferenceDescriptor(msgDestRef);
        }
    }


    @Override
    public final HashMap<MethodPermission, ArrayList<MethodDescriptor>> getMethodPermissionsFromDD() {
        return methodPermissionsFromDD;
    }


    @Override
    public Set<MethodDescriptor> getMethodDescriptors() {
        ClassLoader classLoader = getEjbBundleDescriptor().getClassLoader();
        Set<MethodDescriptor> methods = getBusinessMethodDescriptors();
        try {
            if (isRemoteInterfacesSupported()) {
                addAllInterfaceMethodsIn(methods, classLoader.loadClass(getHomeClassName()), MethodDescriptor.EJB_HOME);
            }
            if (isLocalInterfacesSupported()) {
                addAllInterfaceMethodsIn(methods, classLoader.loadClass(getLocalHomeClassName()),
                    MethodDescriptor.EJB_LOCALHOME);
            }
        } catch (Exception t) {
            throw new RuntimeException(getEjbClassName(), t);
        }
        return methods;
    }


    /**
     * @return the set of method permission assigned to a ejb method descriptor.
     */
    @Override
    public Set<MethodPermission> getMethodPermissionsFor(MethodDescriptor methodDescriptor) {
        if (needToConvertMethodPermissions()) {
            convertMethodPermissions();
        }
        return getMethodPermissions(methodDescriptor);
    }


    /**
     * @eturn the set of roles to which have been assigned method permissions.
     */
    @Override
    public Set<Role> getPermissionedRoles() {
        if (needToConvertMethodPermissions()) {
            convertMethodPermissions();
        }
        Set<Role> allPermissionedRoles = new HashSet<>();
        for (MethodPermission pm : getPermissionedMethodsByPermission().keySet()) {
            if (pm.isRoleBased()) {
                allPermissionedRoles.add(pm.getRole());
            }
        }
        return allPermissionedRoles;
    }


    /**
     * @return a Set of method descriptors for all the methoda assoicated with an excluded method permission
     */
    public Set<MethodDescriptor> getDenyAllMethodPermission() {
        if (needToConvertMethodPermissions()) {
            convertMethodPermissions();
        }
        Map<MethodPermission, Set<MethodDescriptor>> all = getPermissionedMethodsByPermission();
        LOG.log(INFO, "getDenyAllMethodPermission, all: \n{0}", all);
        return all.get(MethodPermission.getDenyAllMethodPermission());
    }


    /**
     * @return a Set of method descriptors for all the methods associated with an unchecked method permission
     */
    public Set<MethodDescriptor> getPermitAllMethodDescriptors() {
        if (needToConvertMethodPermissions()) {
            convertMethodPermissions();
        }
        Map<MethodPermission, Set<MethodDescriptor>> all = getPermissionedMethodsByPermission();
        LOG.log(INFO, "getPermitAllMethodDescriptors, all: \n{0}", all);
        return all.get(MethodPermission.getPermitAllMethodPermission());
    }


    @Override
    public void addPermissionedMethod(MethodPermission mp, MethodDescriptor md) {
        LOG.log(DEBUG, "addPermissionedMethod {0}: {1}", mp, md);
        if (getEjbBundleDescriptor() == null) {
            throw new IllegalArgumentException("Cannot add roles when the descriptor is not part of a bundle");
        }
        if (mp.isRoleBased()) {
            if (!getEjbBundleDescriptor().getRoles().contains(mp.getRole())) {
                // Check for the any authenticated user role '**' as this role
                // will be implicitly defined when not listed as a security-role
                if (!"**".equals(mp.getRole().getName())) {
                    throw new IllegalArgumentException("Cannot add roles when the bundle does not have them");
                }
            }
        }
        if (md.isExact()) {
            updateMethodPermissionForMethod(mp, md);
        } else {
            addMethodPermissionForStyledMethodDescriptor(mp, md);
        }
        saveMethodPermissionFromDD(mp, md);
    }


    /**
     * Remove a method permission from a method or a set of methods
     *
     * @param mp is the method permission to remove
     * @param md describe the method or set of methods this permission apply to
     */
    public void removePermissionedMethod(MethodPermission mp, MethodDescriptor md) {
        if (getEjbBundleDescriptor() == null) {
            throw new IllegalArgumentException("Cannot add roles when the descriptor is not part of a bundle");
        }
        if (mp.isRoleBased()) {
            if (!getEjbBundleDescriptor().getRoles().contains(mp.getRole())) {
                throw new IllegalArgumentException("Cannot add roles when the bundle does not have them");
            }
        }
        if (this.getPermissionedMethodsByPermission().containsKey(mp)) {
            Set<MethodDescriptor> alreadyPermissionedMethodsForThisRole = this.getPermissionedMethodsByPermission()
                .get(mp);
            alreadyPermissionedMethodsForThisRole.remove(md);
            this.getPermissionedMethodsByPermission().put(mp, alreadyPermissionedMethodsForThisRole);
        }
    }


    @Override
    public Set<MethodDescriptor> getSecurityBusinessMethodDescriptors() {
        return getBusinessMethodDescriptors();
    }


    /**
     * @return a map of permission to style 1 or 2 method descriptors
     */
    public Map<MethodPermission, Set<MethodDescriptor>> getStyledPermissionedMethodsByPermission() {
        if (styledMethodDescriptors == null) {
            return null;
        }
        // the current info is structured as MethodDescriptors as keys to
        // method permission, let's reverse this to make the Map using the
        // method permission as a key.
        Map<MethodPermission, Set<MethodDescriptor>> styledMethodDescriptorsByPermission = new HashMap<>();
        for (Entry<MethodDescriptor, Set<MethodPermission>> md : styledMethodDescriptors.entrySet()) {
            Set<MethodPermission> methodPermissions = md.getValue();
            for (MethodPermission mp : methodPermissions) {
                Set<MethodDescriptor> methodDescriptors = styledMethodDescriptorsByPermission.get(mp);
                if (methodDescriptors == null) {
                    methodDescriptors = new HashSet<>();
                }
                methodDescriptors.add(md.getKey());
                styledMethodDescriptorsByPermission.put(mp, methodDescriptors);
            }
        }
        return styledMethodDescriptorsByPermission;
    }


    /**
     * @return true if we have unconverted style 1 or style 2 method descriptors
     */
    private boolean needToConvertMethodPermissions() {
        return styledMethodDescriptors != null;
    }


    /**
     * @return full set of business method descriptors I have
     */
    private Set<MethodDescriptor> getBusinessMethodDescriptors() {
        ClassLoader classLoader = getEjbBundleDescriptor().getClassLoader();
        Set<MethodDescriptor> methods = getLocalRemoteBusinessMethodDescriptors();
        try {
            if (hasWebServiceEndpointInterface()) {
                addAllInterfaceMethodsIn(methods, classLoader.loadClass(getWebServiceEndpointInterfaceName()),
                        MethodDescriptor.EJB_WEB_SERVICE);
            }
        } catch (Exception t) {
            throw new RuntimeException(getEjbClassName(), t);
        }
        return methods;
    }


    protected void addAllInterfaceMethodsIn(Collection<MethodDescriptor> methodDescriptors, Class<?> c, String methodIntf) {
        Method[] methods = c.getMethods();
        for (Method method : methods) {
            if (method.getDeclaringClass() != Object.class) {
                methodDescriptors.add(new MethodDescriptor(method, methodIntf));
            }
        }
    }


    /**
     * @return full set of business method descriptors I have
     */
    private Set<MethodDescriptor> getLocalRemoteBusinessMethodDescriptors() {
        ClassLoader classLoader = getEjbBundleDescriptor().getClassLoader();
        Set<MethodDescriptor> methods = new HashSet<>();
        try {
            if (isRemoteInterfacesSupported()) {
                addAllInterfaceMethodsIn(methods, classLoader.loadClass(getRemoteClassName()), MethodDescriptor.EJB_REMOTE);
            }
            if (isRemoteBusinessInterfacesSupported()) {
                for (String intf : getRemoteBusinessClassNames()) {
                    addAllInterfaceMethodsIn(methods, classLoader.loadClass(intf), MethodDescriptor.EJB_REMOTE);
                }
            }
            if (isLocalInterfacesSupported()) {
                addAllInterfaceMethodsIn(methods, classLoader.loadClass(getLocalClassName()), MethodDescriptor.EJB_LOCAL);
            }
            if (isLocalBusinessInterfacesSupported()) {
                for (String intf : getLocalBusinessClassNames()) {
                    addAllInterfaceMethodsIn(methods, classLoader.loadClass(intf), MethodDescriptor.EJB_LOCAL);
                }
            }
            if (isLocalBean()) {
                addAllInterfaceMethodsIn(methods, classLoader.loadClass(getEjbClassName()), MethodDescriptor.EJB_LOCAL);
            }
        } catch (Exception t) {
            throw new RuntimeException(getEjbClassName(), t);
        }
        return methods;
    }


    /**
     * Convert all style 1 and style 2 method descriptors contained in our tables into style 3 method descriptors.
     */
    private void convertMethodPermissions() {
        if (styledMethodDescriptors == null) {
            return;
        }
        Set<MethodDescriptor> allMethods = getMethodDescriptors();
        Set<MethodDescriptor> unpermissionedMethods = getMethodDescriptors();
        Set<Entry<MethodDescriptor, Set<MethodPermission>>> methodDescriptors = styledMethodDescriptors.entrySet();
        for (Entry<MethodDescriptor, Set<MethodPermission>> entry : methodDescriptors) {
            MethodDescriptor styledMd = entry.getKey();

            // Get the new permissions we are trying to set for this
            // method(s)
            Set<MethodPermission> newPermissions = entry.getValue();

            // Convert to style 3 method descriptors
            Vector<MethodDescriptor> mds = styledMd.doStyleConversion(this, allMethods);
            for (MethodDescriptor md : mds) {
                // remove it from the list of unpermissioned methods.
                // it will be used at the end to set all remaining methods
                // with the unchecked method permission
                unpermissionedMethods.remove(md);

                // iterator over the new set of method permissions for that
                // method descriptor and update the table
                for (MethodPermission newMp : newPermissions) {
                    updateMethodPermissionForMethod(newMp, md);
                }
            }
        }

        // All remaining methods should now be defined as unchecked...
        MethodPermission permitAll = MethodPermission.getPermitAllMethodPermission();
        for (MethodDescriptor md : unpermissionedMethods) {
            if (getMethodPermissions(md).isEmpty()) {
                addMethodPermissionForMethod(permitAll, md);
            }
        }

        // finally we reset the list of method descriptors that need style conversion
        styledMethodDescriptors = null;
    }


    /**
     * Update a method descriptor set of method permission with a new method permission
     * The new method permission is added to the list of existing method permissions given it
     * respect the EJB 2.0 paragraph 21.3.2 on priorities of method permissions
     *
     * @param mp is the method permission to be added
     * @param md is the method descriptor (style3 only) to add the method permission to
     */
    private void updateMethodPermissionForMethod(MethodPermission mp, MethodDescriptor md) {
        // Get the current set of method permissions for that method
        Set<MethodPermission> oldPermissions = getMethodPermissions(md);
        if (oldPermissions.isEmpty()) {
            // this is easy, just add the new one
            addMethodPermissionForMethod(mp, md);
            return;
        }

        // The order of method permssion setting is very important
        // EJB 2.0 Spec 21.3.2
        // excluded method permission is always used when multiple methos permission are present
        // unchecked is considered like a role based method permission and is added to the list
        // therefore making the method callable by anyone.
        if (mp.isExcluded()) {
            // Excluded methods takes precedence on any other form of method permission
            // remove all existing method permission...
            for (MethodPermission oldMp : oldPermissions) {
                removePermissionedMethod(oldMp, md);
            }
            // add the excluded
            addMethodPermissionForMethod(mp, md);
            return;
        }
        if (mp.isUnchecked()) {
            // we are trying to add an unchecked method permisison, all role-based
            // method permission should be removed since unchecked is now used, if a
            // particular method has an excluded method permision, we do not add it
            for (MethodPermission oldMp : oldPermissions) {
                if (!oldMp.isExcluded()) {
                    removePermissionedMethod(oldMp, md);
                    addMethodPermissionForMethod(mp, md);
                }
            }
            return;
        }
        // we are trying to add a role based method permission. Check that
        // unchecked or excluded method permissions have not been set
        // and add it to the current list of role based permission
        for (MethodPermission oldMp : oldPermissions) {
            if (!oldMp.isExcluded()) {
                if (!oldMp.isUnchecked()) {
                    addMethodPermissionForMethod(mp, md);
                }
            }
        }
    }


    private Set<MethodPermission> getMethodPermissions(MethodDescriptor methodDescriptor) {
        Set<MethodPermission> methodPermissionsForMethod = new HashSet<>();
        for (MethodPermission nextPermission : getPermissionedMethodsByPermission().keySet()) {
            Set<MethodDescriptor> permissionedMethods = getPermissionedMethodsByPermission().get(nextPermission);
            for (MethodDescriptor md : permissionedMethods) {
                if (md.equals(methodDescriptor)) {
                    methodPermissionsForMethod.add(nextPermission);
                }
            }
        }
        return methodPermissionsForMethod;
    }


    private void addMethodPermissionForMethod(MethodPermission mp, MethodDescriptor md) {
        if (getPermissionedMethodsByPermission().containsKey(mp)) {
            Set<MethodDescriptor> alreadyPermissionedMethodsForThisRole = getPermissionedMethodsByPermission().get(mp);
            alreadyPermissionedMethodsForThisRole.add(md);
            getPermissionedMethodsByPermission().put(mp, alreadyPermissionedMethodsForThisRole);
        } else {
            Set<MethodDescriptor> permissionedMethodsForThisRole = new HashSet<>();
            permissionedMethodsForThisRole.add(md);
            getPermissionedMethodsByPermission().put(mp, permissionedMethodsForThisRole);
        }
    }


    /**
     * add a style 1 or 2 in our tables
     */
    private void addMethodPermissionForStyledMethodDescriptor(MethodPermission mp, MethodDescriptor md) {
        if (styledMethodDescriptors == null) {
            styledMethodDescriptors = new HashMap<>();
        }
        // we organize per method descriptors, makes it easier...
        Set<MethodPermission> permissions = styledMethodDescriptors.get(md);
        if (permissions == null) {
            permissions = new HashSet<>();
        }
        permissions.add(mp);
        styledMethodDescriptors.put(md, permissions);
    }


    /**
     * Keep a record of all the Method Permissions exactly as they were in the DD
     */
    private void saveMethodPermissionFromDD(MethodPermission methodPermission, MethodDescriptor methodDescriptor) {
        // We organize by permission, makes it easier...
        // Use Array List as opposed to HashMap or Table because MethodDescriptor
        // Equality once did not take into account differences in
        // method interface, and will process sequentially.
        LOG.log(DEBUG, "saveMethodPermissionFromDD {0}: {1}", methodPermission, methodDescriptor);
        methodPermissionsFromDD.computeIfAbsent(methodPermission, e -> new ArrayList<>()).add(methodDescriptor);
    }


    @Override
    public final Map<MethodPermission, Set<MethodDescriptor>> getPermissionedMethodsByPermission() {
        return permissionedMethodsByPermission;
    }


    @Override
    public final Set<LifecycleCallbackDescriptor> getPostConstructDescriptors() {
        return postConstructDescs;
    }


    @Override
    public final void addPostConstructDescriptor(LifecycleCallbackDescriptor postConstructDesc) {
        String className = postConstructDesc.getLifecycleCallbackClass();
        for (LifecycleCallbackDescriptor callback : postConstructDescs) {
            if (callback.getLifecycleCallbackClass().equals(className)) {
                return;
            }
        }
        postConstructDescs.add(postConstructDesc);
    }


    @Override
    public final LifecycleCallbackDescriptor getPostConstructDescriptorByClass(String className) {
        return getEjbBundleDescriptor().getPostConstructDescriptorByClass(className, this);
    }


    @Override
    public final Set<LifecycleCallbackDescriptor> getPreDestroyDescriptors() {
        return preDestroyDescs;
    }


    @Override
    public final LifecycleCallbackDescriptor getPreDestroyDescriptorByClass(String className) {
        return getEjbBundleDescriptor().getPreDestroyDescriptorByClass(className, this);
    }


    @Override
    public final void addPreDestroyDescriptor(LifecycleCallbackDescriptor preDestroyDesc) {
        String className = preDestroyDesc.getLifecycleCallbackClass();
        for (LifecycleCallbackDescriptor callback : preDestroyDescs) {
            if (callback.getLifecycleCallbackClass().equals(className)) {
                return;
            }
        }
        preDestroyDescs.add(preDestroyDesc);
    }


    /**
     * This method determines if all the mechanisms defined in the CSIV2 CompoundSecMechList
     * structure require protected
     * invocations.
     */
    @Override
    public boolean allMechanismsRequireSSL() {
        if (iorConfigDescriptors == null || iorConfigDescriptors.isEmpty()) {
            return false;
        }
        for (EjbIORConfigurationDescriptor iorDesc : iorConfigDescriptors) {
            if (EjbIORConfigurationDescriptor.REQUIRED.equalsIgnoreCase(iorDesc.getConfidentiality())) {
                continue;
            } else if (EjbIORConfigurationDescriptor.REQUIRED.equalsIgnoreCase(iorDesc.getConfidentiality())) {
                continue;
            } else if (EjbIORConfigurationDescriptor.REQUIRED.equalsIgnoreCase(iorDesc.getEstablishTrustInTarget())) {
                continue;
            } else if (EjbIORConfigurationDescriptor.REQUIRED.equalsIgnoreCase(iorDesc.getEstablishTrustInClient())) {
                continue;
            }
            return false;
        }
        return true;
    }


    @Override
    public boolean isRemoteBusinessInterfacesSupported() {
        return !remoteBusinessClassNames.isEmpty();
    }


    @Override
    public Set<String> getRemoteBusinessClassNames() {
        return new HashSet<>(remoteBusinessClassNames);
    }


    /**
     * @param className remote business interface name
     */
    public void addRemoteBusinessClassName(String className) {
        remoteBusinessClassNames.add(className);
    }


    @Override
    public String getRemoteClassName() {
        return this.remoteClassName;
    }


    /**
     * @param remoteClassName classname of the Remote interface of this ejb.
     */
    public void setRemoteClassName(String remoteClassName) {
        this.remoteClassName = remoteClassName;
    }


    @Override
    public final Set<ResourceDescriptor> getAllResourcesDescriptors() {
        if (env == null) {
            return super.getAllResourcesDescriptors();
        }
        return env.getAllResourcesDescriptors();
    }


    @Override
    public final Set<ResourceDescriptor> getAllResourcesDescriptors(Class<?> givenClazz) {
        if (env == null) {
            return super.getAllResourcesDescriptors(givenClazz);
        }
        return env.getAllResourcesDescriptors(givenClazz);
    }


    @Override
    public final Set<ResourceDescriptor> getResourceDescriptors(JavaEEResourceType type) {
        if (env == null) {
            return super.getResourceDescriptors(type);
        }
        return env.getResourceDescriptors(type);
    }


    @Override
    public final void addResourceDescriptor(ResourceDescriptor descriptor) {
        if (env == null) {
            super.addResourceDescriptor(descriptor);
        } else {
            env.addResourceDescriptor(descriptor);
        }
    }


    @Override
    public final void removeResourceDescriptor(ResourceDescriptor descriptor) {
        if (env == null) {
            super.removeResourceDescriptor(descriptor);
        } else {
            env.removeResourceDescriptor(descriptor);
        }
    }


    /**
     * Return the set of resource environment references this ejb declares.
     */
    @Override
    public final Set<ResourceEnvReferenceDescriptor> getResourceEnvReferenceDescriptors() {
        if (env == null) {
            return resourceEnvReferences;
        }
        return env.getResourceEnvReferenceDescriptors();
    }


    @Override
    public final ResourceEnvReferenceDescriptor getResourceEnvReferenceByName(String name) {
        for (ResourceEnvReferenceDescriptor jdr : getResourceEnvReferenceDescriptors()) {
            if (jdr.getName().equals(name)) {
                return jdr;
            }
        }
        throw new IllegalArgumentException(MessageFormat
            .format("This bean {0} has no resource environment reference by the name of {1}", getName(), name));
    }


    @Override
    public final void addResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvReference) {
        try {
            ResourceEnvReferenceDescriptor existing = getResourceEnvReferenceByName(resourceEnvReference.getName());
            for (InjectionTarget injectionTarget : resourceEnvReference.getInjectionTargets()) {
                existing.addInjectionTarget(injectionTarget);
            }
        } catch (IllegalArgumentException e) {
            if (env == null) {
                resourceEnvReferences.add(resourceEnvReference);
            } else {
                env.addResourceEnvReferenceDescriptor(resourceEnvReference);
            }
        }
    }


    @Override
    public final void removeResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvReference) {
        if (env == null) {
            resourceEnvReferences.remove(resourceEnvReference);
        } else {
            env.removeResourceEnvReferenceDescriptor(resourceEnvReference);
        }
    }


    /**
     * @param resolved true -> just resolved, false -> just unresolved
     * @return set of resource references this ejb declares that have or have not been resolved.
     */
    public Set<ResourceReferenceDescriptor> getResourceReferenceDescriptors(boolean resolved) {
        Set<ResourceReferenceDescriptor> toReturn = new HashSet<>();
        for (ResourceReferenceDescriptor reference : getResourceReferenceDescriptors()) {
            if (reference.isResolved() == resolved) {
                toReturn.add(reference);
            }
        }
        return toReturn;
    }


    @Override
    public final Set<ResourceReferenceDescriptor> getResourceReferenceDescriptors() {
        if (env == null) {
            return resourceReferences;
        }
        return env.getResourceReferenceDescriptors();
    }


    @Override
    public ResourceReferenceDescriptor getResourceReferenceByName(String name) {
        for (ResourceReferenceDescriptor reference : getResourceReferenceDescriptors()) {
            if (reference.getName().equals(name)) {
                return reference;
            }
        }
        throw new IllegalArgumentException(
            MessageFormat.format("This bean {0} has no resource reference by the name of {1}", getName(), name));
    }


    @Override
    public final void addResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
        try {
            ResourceReferenceDescriptor existing = getResourceReferenceByName(resourceReference.getName());
            for (InjectionTarget next : resourceReference.getInjectionTargets()) {
                existing.addInjectionTarget(next);
            }
        } catch (IllegalArgumentException e) {
            if (env == null) {
                resourceReferences.add(resourceReference);
            } else {
                env.addResourceReferenceDescriptor(resourceReference);
            }
        }
    }


    /**
     * Removes the given resource reference from this EJB.
     */
    @Override
    public final void removeResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
        if (env == null) {
            resourceReferences.remove(resourceReference);
        } else {
            env.removeResourceReferenceDescriptor(resourceReference);
        }
    }


    @Override
    public Boolean getUsesCallerIdentity() {
        return usesCallerIdentity;
    }


    @Override
    public void setUsesCallerIdentity(boolean flag) {
        usesCallerIdentity = flag;
    }


    @Override
    public void setRunAsIdentity(RunAsIdentityDescriptor desc) {
        if (usesCallerIdentity == null || usesCallerIdentity) {
            throw new IllegalStateException("Cannot set RunAs identity when using caller identity."
                + " Set usesCallerIdentity to false first.");
        }
        this.runAsIdentity = desc;
    }


    @Override
    public RunAsIdentityDescriptor getRunAsIdentity() {
        return runAsIdentity;
    }


    @Override
    public final Set<RoleReference> getRoleReferences() {
        return roleReferences;
    }


    @Override
    public RoleReference getRoleReferenceByName(String roleReferenceName) {
        for (RoleReference nextRR : roleReferences) {
            if (nextRR.getName().equals(roleReferenceName)) {
                return nextRR;
            }
        }
        return null;
    }


    @Override
    public void addRoleReference(RoleReference roleReference) {
        roleReferences.add(roleReference);
    }


    @Override
    public void removeRole(Role role) {
        getPermissionedMethodsByPermission().remove(new MethodPermission(role));
        Set<RoleReference> refs = new HashSet<>(roleReferences);
        for (RoleReference roleReference : refs) {
            if (roleReference.getRole().equals(role)) {
                roleReference.setValue("");
            }
        }
    }


    /**
     * Removes a role reference.
     *
     * @param roleReference
     */
    public void removeRoleReference(RoleReference roleReference) {
        roleReferences.remove(roleReference);
    }


    @Override
    public final Set<ServiceReferenceDescriptor> getServiceReferenceDescriptors() {
        if (env == null) {
            return serviceReferences;
        }
        return env.getServiceReferenceDescriptors();
    }


    /**
     * Looks up an service reference with the given name. Throws an IllegalArgumentException if it
     * is not found.
     */
    @Override
    public final ServiceReferenceDescriptor getServiceReferenceByName(String name) {
        if (env != null) {
            return env.getServiceReferenceByName(name);
        }
        for (ServiceReferenceDescriptor srd : serviceReferences) {
            if (srd.getName().equals(name)) {
                return srd;
            }
        }
        throw new IllegalArgumentException(
            MessageFormat.format("This ejb [{0}] has no service reference by the name of [{1}]", getName(), name));
    }


    @Override
    public final void addServiceReferenceDescriptor(ServiceReferenceDescriptor serviceRef) {
        try {
            ServiceReferenceDescriptor existing = getServiceReferenceByName(serviceRef.getName());
            for (InjectionTarget next : serviceRef.getInjectionTargets()) {
                existing.addInjectionTarget(next);
            }
        } catch (IllegalArgumentException e) {
            if (env == null) {
                serviceReferences.add(serviceRef);
            } else {
                env.addServiceReferenceDescriptor(serviceRef);
            }
            serviceRef.setBundleDescriptor(getEjbBundleDescriptor());
        }
    }


    @Override
    public final void removeServiceReferenceDescriptor(ServiceReferenceDescriptor serviceRef) {
        if (env == null) {
            serviceReferences.remove(serviceRef);
        } else {
            env.removeServiceReferenceDescriptor(serviceRef);
        }
    }


    /**
     * @return true if the EJB has a {@link Timeout} or {@link Schedule} method.
     */
    public boolean isTimedObject() {
        return timedObjectMethod != null || !timerSchedules.isEmpty();
    }


    /**
     * @return a method annotated by {@link Timeout} or configured in xml the same way.
     */
    public MethodDescriptor getEjbTimeoutMethod() {
        return timedObjectMethod;
    }


    /**
      * @param method a method annotated by {@link Timeout} or configured in xml the same way.
     */
    public void setEjbTimeoutMethod(MethodDescriptor method) {
        timedObjectMethod = method;
    }


    /**
     * @return list of {@link ScheduledTimerDescriptor}, never null
     */
    public List<ScheduledTimerDescriptor> getScheduledTimerDescriptors() {
        return timerSchedules;
    }


    /**
     * @param scheduleDescriptor a method annotated by {@link Schedule} or configured in xml
     *            the same way.
     */
    public void addScheduledTimerDescriptor(ScheduledTimerDescriptor scheduleDescriptor) {
        timerSchedules.add(scheduleDescriptor);
    }


    /**
     * @param scheduleDescriptor a method configured in xml as if it would have a {@link Schedule}
     *            annnotation
     */
    // Used by reflection from EjbNode
    public void addScheduledTimerDescriptorFromDD(ScheduledTimerDescriptor scheduleDescriptor) {
        timerMethodDescriptors.add(scheduleDescriptor.getTimeoutMethod());
        timerSchedules.add(scheduleDescriptor);
    }


    /**
     * @param timerMethod
     * @return true if the EJB has a method configured in xml as if it would have a {@link Timeout}
     *         or {@link Schedule} method.
     */
    public boolean hasScheduledTimerMethodFromDD(Method timerMethod) {
        for (MethodDescriptor descriptor : timerMethodDescriptors) {
            if (descriptor.getName().equals(timerMethod.getName())
                && (descriptor.getParameterClassNames() == null
                    || descriptor.getParameterClassNames().length == timerMethod.getParameterTypes().length)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public final String getTransactionType() {
        return this.transactionType;
    }


    /**
     * Set the transaction type of this ejb.
     *
     * @param transactionType Bean/Container
     */
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }


    /**
     * @return the full set of transactional business method descriptors I have.
     */
    public Set<MethodDescriptor> getTxBusinessMethodDescriptors() {
        Set<MethodDescriptor> txBusMethods = getBusinessMethodDescriptors();
        if (isTimedObject()) {
            if (timedObjectMethod != null) {
                txBusMethods.add(timedObjectMethod);
            }
        }
        return txBusMethods;
    }


    @Override
    public final boolean hasWebServiceEndpointInterface() {
        return getWebServiceEndpointInterfaceName() != null;
    }


    @Override
    public final String getWebServiceEndpointInterfaceName() {
        return webServiceEndpointInterfaceName;
    }


    @Override
    public final void setWebServiceEndpointInterfaceName(String name) {
        this.webServiceEndpointInterfaceName = name;
    }


    @Override
    public void addMethodLevelChain(List<EjbInterceptor> chain, Method m, boolean aroundInvoke) {
        if (chain.isEmpty()) {
            return;
        }
        MethodDescriptor methodDesc = new MethodDescriptor(m);
        List<EjbInterceptor> existingChain = null;
        for (MethodDescriptor next : methodInterceptorsMap.keySet()) {
            if (next.implies(methodDesc)) {
                existingChain = methodInterceptorsMap.get(methodDesc);
                break;
            }
        }
        if (existingChain != null) {
            existingChain.addAll(chain);
            return;
        }

        List<EjbInterceptor> newChain = new LinkedList<>();
        for (EjbInterceptor interceptor : interceptorChain) {
            boolean include = aroundInvoke ? interceptor.hasAroundInvokeDescriptor()
                : interceptor.hasAroundTimeoutDescriptor();
            if (include) {
                newChain.add(interceptor);
            }
        }
        newChain.addAll(chain);
        methodInterceptorsMap.put(methodDesc, newChain);
    }


    @Override
    public void notifyNewModule(WebBundleDescriptor wbd) {
        wbd.addJndiNameEnvironment(this);
        // clear our entries
        environmentProperties.clear();
        ejbReferences.clear();
        resourceEnvReferences.clear();
        messageDestReferences.clear();
        resourceReferences.clear();
        serviceReferences.clear();
        entityManagerFactoryReferences.clear();
        entityManagerReferences.clear();
        // switch to the web bundle as the source of JNDI entries
        env = wbd;
    }


    @Override
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        toStringBuffer.append("\n jndiName=").append(jndiName);
        toStringBuffer.append("\n ejbClassName=").append(ejbClassName);
        toStringBuffer.append("\n homeClassName=").append(homeClassName);
        toStringBuffer.append("\n isLocalBean=").append(isLocalBean()).append("\n");
        toStringBuffer.append("\n localBusinessIntfs=").append(localBusinessClassNames);
        toStringBuffer.append("\n localClassName=").append(localClassName);
        toStringBuffer.append("\n localHomeClassName=").append(localHomeClassName);
        toStringBuffer.append("\n remoteBusinessIntfs=").append(remoteBusinessClassNames);
        toStringBuffer.append("\n remoteClassName=").append(remoteClassName);
        toStringBuffer.append("\n transactionType=").append(transactionType);
        toStringBuffer.append("\n ejbReferences=");
        printDescriptorSet(ejbReferences, toStringBuffer);
        toStringBuffer.append("\n environmentProperties=");
        printDescriptorSet(environmentProperties, toStringBuffer);
        toStringBuffer.append("\n messageDestReferences=");
        printDescriptorSet(messageDestReferences, toStringBuffer);
        toStringBuffer.append("\n resourceEnvReferences=");
        printDescriptorSet(resourceEnvReferences, toStringBuffer);
        toStringBuffer.append("\n resourceReferences=");
        printDescriptorSet(resourceReferences, toStringBuffer);
        toStringBuffer.append("\n roleReferences=");
        printDescriptorSet(roleReferences, toStringBuffer);
        toStringBuffer.append("\n serviceReferences=");
        printDescriptorSet(serviceReferences, toStringBuffer);
    }


    protected static void copyEjbDescriptor(EjbBeanDescriptor source, EjbBeanDescriptor target) {
        target.ejbClassName = source.ejbClassName;
        target.transactionType = source.transactionType;
        target.roleReferences.addAll(source.getRoleReferences());
        target.permissionedMethodsByPermission.putAll(source.getPermissionedMethodsByPermission());
        if (target.env == null) {
            // only add target information if it's contained in the target EjbDescriptor
            target.getEnvironmentProperties().addAll(source.getEnvironmentProperties());
            target.getEjbReferenceDescriptors().addAll(source.getEjbReferenceDescriptors());
            target.getResourceEnvReferenceDescriptors().addAll(source.getResourceEnvReferenceDescriptors());
            target.getMessageDestinationReferenceDescriptors().addAll(source.getMessageDestinationReferenceDescriptors());
            target.getResourceReferenceDescriptors().addAll(source.getResourceReferenceDescriptors());
            target.getServiceReferenceDescriptors().addAll(source.getServiceReferenceDescriptors());
            Set<ResourceDescriptor> allResources = source.getAllResourcesDescriptors();
            if (!allResources.isEmpty()) {
                for (ResourceDescriptor resource : allResources) {
                    source.addResourceDescriptor(resource);
                }
            }
            target.getEntityManagerFactoryReferenceDescriptors().addAll(source.getEntityManagerFactoryReferenceDescriptors());
            target.getEntityManagerReferenceDescriptors().addAll(source.getEntityManagerReferenceDescriptors());
        }
        target.timerMethodDescriptors = new ArrayList<>(source.timerMethodDescriptors);
        target.timerSchedules = new ArrayList<>(source.timerSchedules);
        target.usesCallerIdentity = source.usesCallerIdentity;
        target.getIORConfigurationDescriptors().addAll(source.getIORConfigurationDescriptors());
    }


    private static void printDescriptorSet(Set<? extends Descriptor> descSet, StringBuffer sbuf) {
        for (Descriptor obj : descSet) {
            obj.print(sbuf);
        }
    }
}
