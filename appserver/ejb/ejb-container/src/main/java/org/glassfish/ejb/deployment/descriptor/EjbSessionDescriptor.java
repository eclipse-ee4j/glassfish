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

package org.glassfish.ejb.deployment.descriptor;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.util.TypeUtil;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.deployment.AnnotationTypesProvider;

/**
 * Objects of this kind represent the deployment information describing a single
 * Session Ejb : { stateful , stateless, singleton }
 *
 * @author Danny Coward
 */
public class EjbSessionDescriptor extends EjbDescriptor implements com.sun.enterprise.deployment.EjbSessionDescriptor {

    private static final long serialVersionUID = 1L;
    private Set<LifecycleCallbackDescriptor> postActivateDescs = new HashSet<>();
    private Set<LifecycleCallbackDescriptor> prePassivateDescs = new HashSet<>();

    // For EJB 3.0 stateful session beans, information about the assocation
    // between a business method and bean removal.
    private final Map<MethodDescriptor, EjbRemovalInfo> removeMethods = new HashMap<>();

    // For EJB 3.0 stateful session beans with adapted homes, list of
    // business methods corresponding to Home/LocalHome create methods.
    private final Set<EjbInitInfo> initMethods = new HashSet<>();

    private MethodDescriptor afterBeginMethod;
    private MethodDescriptor beforeCompletionMethod;
    private MethodDescriptor afterCompletionMethod;

    // Holds @StatefulTimeout or stateful-timeout from
    // ejb-jar.xml.  Only applies to stateful session beans.
    // Initialize to "not set"(null) state so annotation processing
    // can apply the correct overriding behavior.
    private Long statefulTimeoutValue;
    private TimeUnit statefulTimeoutUnit;

    private boolean sessionTypeIsSet;
    private boolean isStateless;
    private boolean isStateful;
    private boolean isSingleton;
    // ejb3.2 spec 4.6.5 Disabling Passivation of Stateful Session Beans
    private boolean isPassivationCapable = true;
    private boolean passivationCapableIsSet;

    private final List<MethodDescriptor> readLockMethods = new ArrayList<>();
    private final List<MethodDescriptor> writeLockMethods = new ArrayList<>();
    private final List<AccessTimeoutHolder> accessTimeoutMethods = new ArrayList<>();
    private final List<MethodDescriptor> asyncMethods = new ArrayList<>();

    // Controls eager vs. lazy Singleton initialization
    private Boolean initOnStartup;

    private String[] dependsOn = new String[0];

    private ConcurrencyManagementType concurrencyManagementType;

    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(EjbSessionDescriptor.class);

    /**
    *  Default constructor.
    */
    public EjbSessionDescriptor() {
    }

    @Override
    public String getEjbTypeForDisplay() {
        if (isStateful()) {
            return "StatefulSessionBean";
        } else if (isStateless()) {
            return "StatelessSessionBean";
        } else {
            return "SingletonSessionBean";
        }
    }

    @Override
    public boolean isPassivationCapable() {
        return isPassivationCapable;
    }

    public void setPassivationCapable(boolean passivationCapable) {
        isPassivationCapable = passivationCapable;
        passivationCapableIsSet = true;
    }

    public boolean isPassivationCapableSet() {
        return passivationCapableIsSet;
    }

    /**
    * Returns the type of this bean - always "Session".
    */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
    * Returns the string STATELESS or STATEFUL according as to whether
    * the bean is stateless or stateful.
    **/

    @Override
    public String getSessionType() {
        if (this.isStateless()) {
            return STATELESS;
        } else if( isStateful() ){
            return STATEFUL;
        } else {
            return SINGLETON;
        }
    }


    /**
     * Accepts the Strings STATELESS / STATEFUL / SINGLETON
     */
    public void setSessionType(String sessionType) {
        if (STATELESS.equals(sessionType)) {
            isStateless = true;
        } else if (STATEFUL.equals(sessionType)) {
            isStateful = true;
        } else if (SINGLETON.equals(sessionType)) {
            isSingleton = true;
        } else if (Descriptor.isBoundsChecking()) {
            throw new IllegalArgumentException(
                localStrings.getLocalString("enterprise.deployment.exceptionsessiontypenotlegaltype",
                    "{0} is not a legal session type for session ejbs. The type must be {1} or {2}",
                    new Object[] {sessionType, STATEFUL, STATELESS}));
        }
        sessionTypeIsSet = true;
    }

    /**
     * Useful for certain annotation / .xml processing.  ejb-jar.xml might
     * not set <session-type> if it's only being used for sparse overriding.
     */
    public boolean isSessionTypeSet() {
        return sessionTypeIsSet;
    }

    /**
    * Sets my type
    */
    @Override
    public void setType(String type) {
        throw new IllegalArgumentException(localStrings.getLocalString(
                                   "enterprise.deployment.exceptioncannotsettypeofsessionbean",
                                   "Cannot set the type of a session bean"));
    }



    /**
    *  Sets the transaction type for this bean. Must be either BEAN_TRANSACTION_TYPE or CONTAINER_TRANSACTION_TYPE.
    */
    @Override
    public void setTransactionType(String transactionType) {
        boolean isValidType = BEAN_TRANSACTION_TYPE.equals(transactionType)
            || CONTAINER_TRANSACTION_TYPE.equals(transactionType);

        if (!isValidType && Descriptor.isBoundsChecking()) {
            throw new IllegalArgumentException(localStrings.getLocalString(
                                       "enterprise.deployment..exceptointxtypenotlegaltype",
                                       "{0} is not a legal transaction type for session beans", new Object[] {transactionType}));
        }
        super.transactionType = transactionType;
        super.setMethodContainerTransactions(new Hashtable<>());
    }

    /**
    * Returns true if I am describing a stateless session bean.
    */
    @Override
    public boolean isStateless() {
        return isStateless;
    }

    @Override
    public boolean isStateful() {
        return isStateful;
    }

    @Override
    public boolean isSingleton() {
        return isSingleton;
    }

    public boolean hasAsynchronousMethods() {
        return !asyncMethods.isEmpty();
    }

    public void addAsynchronousMethod(MethodDescriptor m) {
        asyncMethods.add(m);
    }

    public List<MethodDescriptor> getAsynchronousMethods() {
        return new ArrayList<>(asyncMethods);
    }

    public boolean isAsynchronousMethod(Method m) {
        for (MethodDescriptor next : asyncMethods) {
            Method nextMethod = next.getMethod(this);
            if (nextMethod != null && TypeUtil.sameMethodSignature(m, nextMethod)) {
                return true;
            }
        }
        return false;
    }

    public void addStatefulTimeoutDescriptor(TimeoutValueDescriptor timeout) {
        statefulTimeoutValue = timeout.getValue();
        statefulTimeoutUnit  = timeout.getUnit();
    }

    public void setStatefulTimeout(Long value, TimeUnit unit) {
        statefulTimeoutValue = value;
        statefulTimeoutUnit = unit;
    }

    public boolean hasStatefulTimeout() {
        return (statefulTimeoutValue != null);
    }

    public Long getStatefulTimeoutValue() {
        return statefulTimeoutValue;
    }

    public TimeUnit getStatefulTimeoutUnit() {
        return statefulTimeoutUnit;
    }

    @Override
    public boolean hasRemoveMethods() {
        return (!removeMethods.isEmpty());
    }

    /**
     * @return remove method info for the given method or null if the
     * given method is not a remove method for this stateful session bean.
     */
    public EjbRemovalInfo getRemovalInfo(MethodDescriptor method) {
        // first try to find the exact match
        Iterator<Entry<MethodDescriptor, EjbRemovalInfo>> entryIterator = removeMethods.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Entry<MethodDescriptor, EjbRemovalInfo> entry = entryIterator.next();
            if (entry.getKey().equals(method)) {
                return entry.getValue();
            }
        }

        // if nothing is found, try to find the loose match
        entryIterator = removeMethods.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Entry<MethodDescriptor, EjbRemovalInfo> entry = entryIterator.next();
            if (entry.getKey().implies(method)) {
                return entry.getValue();
            }
        }

        return null;
    }


    public Set<EjbRemovalInfo> getAllRemovalInfo() {
        return new HashSet<>(removeMethods.values());
    }


    // FIXME by srini - validate changing CDI code to use this is fine
    @Override
    public Set<MethodDescriptor> getRemoveMethodDescriptors() {
        return new HashSet<>(removeMethods.keySet());
    }


    public void addRemoveMethod(EjbRemovalInfo removalInfo) {
        removeMethods.put(removalInfo.getRemoveMethod(), removalInfo);
    }


    public boolean hasInitMethods() {
        return !initMethods.isEmpty();
    }


    public Set<EjbInitInfo> getInitMethods() {
        return new HashSet<>(initMethods);
    }


    public void addInitMethod(EjbInitInfo initInfo) {
        initMethods.add(initInfo);
    }


    public Set<LifecycleCallbackDescriptor> getPostActivateDescriptors() {
        if (postActivateDescs == null) {
            postActivateDescs = new HashSet<>();
        }
        return postActivateDescs;
    }


    public void addPostActivateDescriptor(LifecycleCallbackDescriptor postActivateDesc) {
        String className = postActivateDesc.getLifecycleCallbackClass();
        boolean found = false;
        for (LifecycleCallbackDescriptor next : getPostActivateDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                found = true;
                break;
            }
        }
        if (!found) {
            getPostActivateDescriptors().add(postActivateDesc);
        }
    }


    public LifecycleCallbackDescriptor getPostActivateDescriptorByClass(String className) {
        for (LifecycleCallbackDescriptor next : getPostActivateDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                return next;
            }
        }
        return null;
    }


    public boolean hasPostActivateMethod() {
        return !getPostActivateDescriptors().isEmpty();
    }


    public Set<LifecycleCallbackDescriptor> getPrePassivateDescriptors() {
        if (prePassivateDescs == null) {
            prePassivateDescs = new HashSet<>();
        }
        return prePassivateDescs;
    }


    public void addPrePassivateDescriptor(LifecycleCallbackDescriptor prePassivateDesc) {
        String className = prePassivateDesc.getLifecycleCallbackClass();
        boolean found = false;
        for (LifecycleCallbackDescriptor next : getPrePassivateDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                found = true;
                break;
            }
        }
        if (!found) {
            getPrePassivateDescriptors().add(prePassivateDesc);
        }
    }


    public LifecycleCallbackDescriptor getPrePassivateDescriptorByClass(String className) {
        for (LifecycleCallbackDescriptor next : getPrePassivateDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                return next;
            }
        }
        return null;
    }


    public boolean hasPrePassivateMethod() {
        return !getPrePassivateDescriptors().isEmpty();
    }


    @Override
    public Vector<ContainerTransaction> getPossibleTransactionAttributes() {
        // Session beans that implement SessionSynchronization interface
        // have a limited set of possible transaction attributes.
        if (isStateful()) {
            try {
                EjbBundleDescriptorImpl ejbBundle = getEjbBundleDescriptor();
                ClassLoader classLoader = ejbBundle.getClassLoader();
                Class<?> ejbClass = classLoader.loadClass(getEjbClassName());
                ServiceLocator serviceLocator = Globals.getDefaultHabitat();
                AnnotationTypesProvider provider = serviceLocator.getService(AnnotationTypesProvider.class, "EJB");
                if (provider != null) {
                    Class<?> sessionSynchClass = provider.getType("jakarta.ejb.SessionSynchronization");
                    if (sessionSynchClass.isAssignableFrom(ejbClass)) {
                        Vector<ContainerTransaction> txAttributes = new Vector<>();
                        txAttributes.add(new ContainerTransaction(ContainerTransaction.REQUIRED, ""));
                        txAttributes.add(new ContainerTransaction(ContainerTransaction.REQUIRES_NEW, ""));
                        txAttributes.add(new ContainerTransaction(ContainerTransaction.MANDATORY, ""));
                        return txAttributes;
                    }
                }
            } catch (Exception e) {
                // Don't treat this as a fatal error.
                // Just return full set of possible transaction attributes.
            }
        }
        return super.getPossibleTransactionAttributes();
    }


    @Override
    public String getContainerFactoryQualifier() {
        if (isStateful) {
            return "StatefulContainerFactory";
        }
        if (isStateless) {
            return "StatelessContainerFactory";
        }
        return "SingletonContainerFactory";
    }


    public void addAfterBeginDescriptor(MethodDescriptor m) {
        afterBeginMethod = m;
    }


    public void addBeforeCompletionDescriptor(MethodDescriptor m) {
        beforeCompletionMethod = m;
    }


    public void addAfterCompletionDescriptor(MethodDescriptor m) {
        afterCompletionMethod = m;
    }


    /**
     * Set the Method annotated @AfterBegin.
     */
    public void setAfterBeginMethodIfNotSet(MethodDescriptor m) {
        if (afterBeginMethod == null) {
            afterBeginMethod = m;
        }
    }


    /**
     * Returns the Method annotated @AfterBegin.
     */
    public MethodDescriptor getAfterBeginMethod() {
        return afterBeginMethod;
    }


    /**
     * Set the Method annotated @BeforeCompletion.
     */
    public void setBeforeCompletionMethodIfNotSet(MethodDescriptor m) {
        if (beforeCompletionMethod == null) {
            beforeCompletionMethod = m;
        }
    }


    /**
     * Returns the Method annotated @AfterBegin.
     */
    public MethodDescriptor getBeforeCompletionMethod() {
        return beforeCompletionMethod;
    }


    /**
     * Set the Method annotated @AfterCompletion.
     */
    public void setAfterCompletionMethodIfNotSet(MethodDescriptor m) {
        if (afterCompletionMethod == null) {
            afterCompletionMethod = m;
        }
    }


    /**
     * Returns the Method annotated @AfterCompletion.
     */
    public MethodDescriptor getAfterCompletionMethod() {
        return afterCompletionMethod;
    }


    public boolean getInitOnStartup() {
        return initOnStartup != null && initOnStartup;
    }


    public void setInitOnStartup(boolean flag) {
        initOnStartup = flag;
    }


    public void setInitOnStartupIfNotAlreadySet(boolean flag) {
        if (initOnStartup == null) {
            setInitOnStartup(flag);
        }
    }


    public String[] getDependsOn() {
        return dependsOn;
    }


    public boolean hasDependsOn() {
        return dependsOn.length > 0;
    }


    public void setDependsOn(String[] dep) {
        dependsOn = dep == null ? new String[0] : dep;
    }


    public void setDependsOnIfNotSet(String[] dep) {
        if (!hasDependsOn()) {
            setDependsOn(dep);
        }
    }


    public ConcurrencyManagementType getConcurrencyManagementType() {
        return concurrencyManagementType == null ? ConcurrencyManagementType.Container : concurrencyManagementType;
    }


    public boolean hasContainerManagedConcurrency() {
        return getConcurrencyManagementType() == ConcurrencyManagementType.Container;
    }


    public boolean hasBeanManagedConcurrency() {
        return getConcurrencyManagementType() == ConcurrencyManagementType.Bean;
    }


    public void setConcurrencyManagementType(ConcurrencyManagementType type) {
        concurrencyManagementType = type;
    }


    public void setConcurrencyManagementTypeIfNotSet(ConcurrencyManagementType type) {
        if (concurrencyManagementType == null) {
            setConcurrencyManagementType(type);
        }
    }


    public void addConcurrentMethodFromXml(ConcurrentMethodDescriptor concMethod) {
        // .xml must contain a method. However, both READ/WRITE lock metadata
        // and access timeout are optional.

        MethodDescriptor methodDesc = concMethod.getConcurrentMethod();
        if (concMethod.hasLockMetadata()) {
            if (concMethod.isWriteLocked()) {
                addWriteLockMethod(methodDesc);
            } else {
                addReadLockMethod(methodDesc);
            }
        }

        if (concMethod.hasAccessTimeout()) {

            this.addAccessTimeoutMethod(methodDesc, concMethod.getAccessTimeoutValue(),
                concMethod.getAccessTimeoutUnit());
        }
    }


    public void addReadLockMethod(MethodDescriptor methodDescriptor) {
        readLockMethods.add(methodDescriptor);
    }


    public void addWriteLockMethod(MethodDescriptor methodDescriptor) {
        writeLockMethods.add(methodDescriptor);
    }


    public List<MethodDescriptor> getReadLockMethods() {
        return new ArrayList<>(readLockMethods);
    }


    public List<MethodDescriptor> getWriteLockMethods() {
        return new ArrayList<>(writeLockMethods);
    }


    public List<MethodDescriptor> getReadAndWriteLockMethods() {
        List<MethodDescriptor> readAndWriteLockMethods = new ArrayList<>();
        readAndWriteLockMethods.addAll(readLockMethods);
        readAndWriteLockMethods.addAll(writeLockMethods);
        return readAndWriteLockMethods;
    }


    public void addAccessTimeoutMethod(MethodDescriptor methodDescriptor, long value, TimeUnit unit) {
        accessTimeoutMethods.add(new AccessTimeoutHolder(value, unit, methodDescriptor));
    }


    public List<MethodDescriptor> getAccessTimeoutMethods() {
        List<MethodDescriptor> methods = new ArrayList<>();
        for (AccessTimeoutHolder holder : accessTimeoutMethods) {
            methods.add(holder.method);
        }
        return methods;
    }


    public List<AccessTimeoutHolder> getAccessTimeoutInfo() {
        List<AccessTimeoutHolder> all = new ArrayList<>();
        for (AccessTimeoutHolder holder : accessTimeoutMethods) {
            all.add(holder);
        }
        return all;
    }


    /**
     * Returns a formatted String of the attributes of this object.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("Session descriptor");
        toStringBuffer.append("\n sessionType ").append(getSessionType());
        super.print(toStringBuffer);
    }


    /**
     * Return the fully-qualified portable JNDI name for a given
     * client view (Remote, Local, or no-interface).
     */
    @Override
    public String getPortableJndiName(String clientViewType) {
        final Application app = getEjbBundleDescriptor().getApplication();
        final String appName;
        if (app.isVirtual()) {
            appName = null;
        } else {
            appName = app.getAppName();
        }

        StringBuilder javaGlobalPrefix = new StringBuilder("java:global/");

        if (appName != null) {
            javaGlobalPrefix.append(appName);
            javaGlobalPrefix.append('/');
        }

        javaGlobalPrefix.append(getEjbBundleDescriptor().getModuleDescriptor().getModuleName());
        javaGlobalPrefix.append('/');

        javaGlobalPrefix.append(getName());

        javaGlobalPrefix.append('!');
        javaGlobalPrefix.append(clientViewType);

        return javaGlobalPrefix.toString();
    }

    public static class AccessTimeoutHolder {

        public AccessTimeoutHolder(long v, TimeUnit u, MethodDescriptor m) {
            value = v;
            unit = u;
            method = m;
        }
        public long value;
        public TimeUnit unit;
        public MethodDescriptor method;
    }

    public enum ConcurrencyManagementType {
        Bean,
        Container,
    }

}
