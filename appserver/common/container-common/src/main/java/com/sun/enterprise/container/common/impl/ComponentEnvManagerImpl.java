/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2024 Payara Foundation and/or its affiliates
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

package com.sun.enterprise.container.common.impl;

import com.sun.enterprise.container.common.spi.EjbNamingReferenceManager;
import com.sun.enterprise.container.common.spi.WebServiceReferenceManager;
import com.sun.enterprise.container.common.spi.util.CallFlowAgent;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.AdministeredObjectDefinitionDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.ConnectionFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.ContextServiceDefinitionDescriptor;
import com.sun.enterprise.deployment.DataSourceDefinitionDescriptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerFactoryReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.JMSConnectionFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.MailSessionDescriptor;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.ManagedExecutorDefinitionDescriptor;
import com.sun.enterprise.deployment.ManagedScheduledExecutorDefinitionDescriptor;
import com.sun.enterprise.deployment.ManagedThreadFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.naming.spi.NamingObjectFactory;
import com.sun.enterprise.naming.spi.NamingUtils;

import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.invocation.ApplicationEnvironment;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.ComponentNamingUtil;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.JNDIBinding;
import org.glassfish.api.naming.NamingObjectProxy;
import org.glassfish.api.naming.SimpleJndiName;
//import org.glassfish.concurro.internal.ConcurrencyManagedCDIBeans;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.javaee.services.CommonResourceProxy;
import org.glassfish.javaee.services.JMSCFResourcePMProxy;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.util.ResourceManagerFactory;
import org.jvnet.hk2.annotations.Service;

import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getPMJndiName;
import static com.sun.enterprise.deployment.util.DOLUtils.getApplicationFromEnv;
import static com.sun.enterprise.deployment.util.DOLUtils.getApplicationName;
import static com.sun.enterprise.deployment.util.DOLUtils.getModuleName;
import static com.sun.enterprise.deployment.util.DOLUtils.getTreatComponentAsModule;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT_ENV;
import static org.glassfish.deployment.common.JavaEEResourceType.AODD;
import static org.glassfish.deployment.common.JavaEEResourceType.CFD;
import static org.glassfish.deployment.common.JavaEEResourceType.CSDD;
import static org.glassfish.deployment.common.JavaEEResourceType.DSD;
import static org.glassfish.deployment.common.JavaEEResourceType.JMSCFDD;
import static org.glassfish.deployment.common.JavaEEResourceType.JMSDD;
import static org.glassfish.deployment.common.JavaEEResourceType.MEDD;
import static org.glassfish.deployment.common.JavaEEResourceType.MSD;
import static org.glassfish.deployment.common.JavaEEResourceType.MSEDD;
import static org.glassfish.deployment.common.JavaEEResourceType.MTFDD;

@Service
public class ComponentEnvManagerImpl implements ComponentEnvManager {

    private static final Logger LOG = Logger.getLogger(ComponentEnvManagerImpl.class.getName());

    @Inject
    private ServiceLocator locator;

    @Inject
    GlassfishNamingManager namingManager;

    @Inject
    ComponentNamingUtil componentNamingUtil;

    @Inject
    private transient CallFlowAgent callFlowAgent;

    @Inject
    private transient TransactionManager transactionManager;

    @Inject
    private ProcessEnvironment processEnvironment;

    // FIXME: container-common shouldn't depend on EJB stuff, right?
    // this seems like the abstraction design failure.
    @Inject
    private NamingUtils namingUtils;

    @Inject
    private InvocationManager invocationManager;

    private final ConcurrentMap<String, RefCountJndiNameEnvironment> compId2Env = new ConcurrentHashMap<>();

    /**
     * Keep track of number of components using the same component ID so that we can match register
     * calls with unregister calls.
     * EJBs in war files will use the same component ID as the web bundle.
     */
    private static class RefCountJndiNameEnvironment {
        public RefCountJndiNameEnvironment(JndiNameEnvironment env) {
            this.env = env;
            this.refcnt = new AtomicInteger(1);
        }

        public JndiNameEnvironment env;
        public AtomicInteger refcnt;
    }

    public void register(String componentId, JndiNameEnvironment env) {
        LOG.log(Level.FINEST, "register(componentId={0}, env.class={1})", new Object[] {componentId, env.getClass()});
        RefCountJndiNameEnvironment refCountJndiNameEnvironment = compId2Env.putIfAbsent(componentId,
            new RefCountJndiNameEnvironment(env));
        if (refCountJndiNameEnvironment != null) {
            refCountJndiNameEnvironment.refcnt.incrementAndGet();
        }
    }

    public void unregister(String componentId) {
        RefCountJndiNameEnvironment refCountJndiEnvironment = compId2Env.get(componentId);
        if (refCountJndiEnvironment != null && refCountJndiEnvironment.refcnt.decrementAndGet() == 0) {
            compId2Env.remove(componentId);
        }
    }

    @Override
    public JndiNameEnvironment getJndiNameEnvironment(String componentId) {
        LOG.log(Level.FINEST, "getJndiNameEnvironment(componentId={0})", componentId);
        if (componentId == null) {
            return null;
        }
        RefCountJndiNameEnvironment refCountJndiEnvironment = compId2Env.get(componentId);
        return refCountJndiEnvironment == null ? null : refCountJndiEnvironment.env;
    }

    @Override
    public JndiNameEnvironment getCurrentJndiNameEnvironment() {
        ComponentInvocation inv = invocationManager.getCurrentInvocation();
        if (inv == null) {
            return null;
        }
        return getJndiNameEnvironment(inv.componentId);
    }

    @Override
    public String bindToComponentNamespace(final JndiNameEnvironment jndiEnvironment) throws NamingException {
        String componentEnvId = getComponentEnvId(jndiEnvironment);

        Collection<JNDIBinding> bindings = new ArrayList<>();

        // Add all java:comp, java:module, and java:app(except for app clients) dependencies
        // for the specified environment
        addJNDIBindings(jndiEnvironment, ScopeType.COMPONENT, bindings);
        addJNDIBindings(jndiEnvironment, ScopeType.MODULE, bindings);

        if (!(jndiEnvironment instanceof ApplicationClientDescriptor)) {
            addJNDIBindings(jndiEnvironment, ScopeType.APP, bindings);
        }

        if (jndiEnvironment instanceof Application) {
            Application app = (Application) jndiEnvironment;

            // Add any java:app entries defined by any app clients. These must
            // live in the server so they are accessible by other modules in the .ear. Likewise,
            // those same entries will not be registered within the app client JVM itself.
            for (JndiNameEnvironment bundleDescriptor : app.getBundleDescriptors(ApplicationClientDescriptor.class)) {
                addJNDIBindings(bundleDescriptor, ScopeType.APP, bindings);
            }

            namingManager.bindToAppNamespace(getApplicationName(jndiEnvironment), bindings);
        } else {
            // Bind dependencies to the namespace for this component
            namingManager.bindToComponentNamespace(
                    getApplicationName(jndiEnvironment),
                    getModuleName(jndiEnvironment),
                    componentEnvId,
                    getTreatComponentAsModule(jndiEnvironment),
                    bindings);
            // FIXME: Depends on some side effects of preceding calls.
            componentEnvId = getComponentEnvId(jndiEnvironment);
        }

        if (!(jndiEnvironment instanceof ApplicationClientDescriptor)) {
            // Publish any dependencies with java:global names defined by the current env
            // to the global namespace
            Collection<JNDIBinding> globalBindings = new ArrayList<>();
            addJNDIBindings(jndiEnvironment, ScopeType.GLOBAL, globalBindings);

            if (jndiEnvironment instanceof Application) {
                Application app = (Application) jndiEnvironment;

                // Add any java:global entries defined by any app clients. These must
                // live in the server so they are accessible by other modules in the .ear. Likewise,
                // those same entries will not be registered within the app client JVM itself.
                for (JndiNameEnvironment bundleDescriptor : app.getBundleDescriptors(ApplicationClientDescriptor.class)) {
                    addJNDIBindings(bundleDescriptor, ScopeType.GLOBAL, globalBindings);
                }
            }

            for (JNDIBinding globalBinding : globalBindings) {
                namingManager.publishObject(globalBinding.getName(), globalBinding.getValue(), true);
            }
        }

        // If the app contains any application client modules (and the given env isn't
        // an application client) register any java:app dependencies under a well-known
        // internal portion of the global namespace based on the application name. This
        // will allow app client access to application-wide objects within the server.
        Application app = getApplicationFromEnv(jndiEnvironment);
        if (!(jndiEnvironment instanceof ApplicationClientDescriptor)
            && !app.getBundleDescriptors(ApplicationClientDescriptor.class).isEmpty()) {
            for (JNDIBinding binding : bindings) {
                if (dependencyAppliesToScope(binding.getName(), ScopeType.APP)) {
                    namingManager.publishObject(
                        componentNamingUtil.composeInternalGlobalJavaAppName(app.getAppName(), binding.getName()),
                        binding.getValue(),
                        true);
                }
            }
        }

        if (componentEnvId != null) {
            register(componentEnvId, jndiEnvironment);
        }

        return componentEnvId;
    }

    @Override
    public void addToComponentNamespace(JndiNameEnvironment origEnv, Collection<EnvironmentProperty> envProps,
        Collection<ResourceReferenceDescriptor> resRefs) throws NamingException {
        String componentEnvId = getComponentEnvId(origEnv);

        Collection<JNDIBinding> bindings = new ArrayList<>();

        addEnvironmentProperties(ScopeType.COMPONENT, envProps, bindings);
        addResourceReferences(ScopeType.COMPONENT, resRefs, bindings);

        boolean treatComponentAsModule = getTreatComponentAsModule(origEnv);

        // Bind dependencies to the namespace for this component
        namingManager.bindToComponentNamespace(
                getApplicationName(origEnv),
                getModuleName(origEnv),
                componentEnvId,
                treatComponentAsModule, bindings);
    }

    private String getResourceId(JndiNameEnvironment env, Descriptor desc) {
        if (dependencyAppliesToScope(desc, ScopeType.COMPONENT)) {
            return getApplicationName(env) + "/" + getModuleName(env) + "/" + getComponentEnvId(env);
        }
        if (dependencyAppliesToScope(desc, ScopeType.MODULE)) {
            return getApplicationName(env) + "/" + getModuleName(env);
        }
        if (dependencyAppliesToScope(desc, ScopeType.APP)) {
            return getApplicationName(env);
        }
        return "";
    }

    private void addAllDescriptorBindings(JndiNameEnvironment jndiEnv, ScopeType scope, Collection<JNDIBinding> jndiBindings) {
        Set<ResourceDescriptor> allDescriptors = new HashSet<>();
        Set<ResourceDescriptor> dataSourceDefinitions = jndiEnv.getResourceDescriptors(DSD);
        Set<ResourceDescriptor> messagingConnectionFactoryDefinitions = jndiEnv.getResourceDescriptors(JMSCFDD);
        Set<ResourceDescriptor> mailSessionDefinitions = jndiEnv.getResourceDescriptors(MSD);
        Set<ResourceDescriptor> messagingDestinationDefinitions = jndiEnv.getResourceDescriptors(JMSDD);

        Set<ResourceDescriptor> managedExecutorDefinitions = jndiEnv.getResourceDescriptors(MEDD);
        Set<ResourceDescriptor> managedScheduledDefinitions = jndiEnv.getResourceDescriptors(MSEDD);
        Set<ResourceDescriptor> managedThreadfactoryDefintions = jndiEnv.getResourceDescriptors(MTFDD);
        Set<ResourceDescriptor> contextServiceDefinitions = jndiEnv.getResourceDescriptors(CSDD);

        if (jndiEnv instanceof ApplicationClientDescriptor) {
            LOG.fine("No support for connection-factory in client module.");
        } else {
            Set<ResourceDescriptor> connectionFactoryDefinitions = jndiEnv.getResourceDescriptors(CFD);
            allDescriptors.addAll(connectionFactoryDefinitions);
        }

        if (jndiEnv instanceof ApplicationClientDescriptor) {
            LOG.fine("No support for administered-object in client module.");
        } else {
            Set<ResourceDescriptor> administeredObjectDefinitions = jndiEnv.getResourceDescriptors(AODD);
            allDescriptors.addAll(administeredObjectDefinitions);
        }

        allDescriptors.addAll(dataSourceDefinitions);
        allDescriptors.addAll(messagingConnectionFactoryDefinitions);
        allDescriptors.addAll(mailSessionDefinitions);
        allDescriptors.addAll(messagingDestinationDefinitions);

        allDescriptors.addAll(managedExecutorDefinitions);
        allDescriptors.addAll(managedScheduledDefinitions);
        allDescriptors.addAll(managedThreadfactoryDefintions);
        allDescriptors.addAll(contextServiceDefinitions);


        for (ResourceDescriptor descriptor : allDescriptors) {
            if (!dependencyAppliesToScope(descriptor, scope)) {
                continue;
            }

            if (descriptor.getResourceType() == DSD) {
                if (descriptor instanceof DataSourceDefinitionDescriptor && ((DataSourceDefinitionDescriptor) descriptor).isDeployed()) {
                    continue;
                }
            }

            // FIXME: Alert, it modifies the descriptor as a side effect!!! What are consequences?
            descriptor.setResourceId(getResourceId(jndiEnv, descriptor));

            CommonResourceProxy proxy = locator.getService(CommonResourceProxy.class);
            proxy.setDescriptor(descriptor);

            SimpleJndiName logicalJndiName = toLogicalJndiName(descriptor);
            CompEnvBinding envBinding = new CompEnvBinding(logicalJndiName, proxy);
            jndiBindings.add(envBinding);

            // Add another proxy with __PM suffix
            if (descriptor.getResourceType() == JavaEEResourceType.JMSCFDD) {
                CommonResourceProxy jmscfProxy = locator.getService(JMSCFResourcePMProxy.class);
                jmscfProxy.setDescriptor(descriptor);
                CompEnvBinding jmscfEnvBinding = new CompEnvBinding(getPMJndiName(logicalJndiName), jmscfProxy);
                jndiBindings.add(jmscfEnvBinding);
            }
        }

        if (scope == ScopeType.APP) {
            Set<ResourceDescriptor> concurrencyDescs = new HashSet<>();
            concurrencyDescs.addAll(managedExecutorDefinitions);
            concurrencyDescs.addAll(managedThreadfactoryDefintions);
            concurrencyDescs.addAll(managedScheduledDefinitions);
            concurrencyDescs.addAll(contextServiceDefinitions);
            registerConcurrencyCDIQualifiers(jndiBindings, concurrencyDescs);
        }
    }

    private void registerConcurrencyCDIQualifiers(Collection<JNDIBinding> jndiBindings, Set<ResourceDescriptor> concurrencyDescs) {
        if (!concurrencyDescs.isEmpty()) {
            /*
            ConcurrencyManagedCDIBeans setup = new ConcurrencyManagedCDIBeans();
            for (ResourceDescriptor desc : concurrencyDescs) {
                // TODO: preferably introduce common predecessor for all the descriptors
                if (desc instanceof ContextServiceDefinitionDescriptor) {
                    Set<String> qualifiers = Arrays.asList(((ContextServiceDefinitionDescriptor)desc).getQualifiers()).stream().map(c -> c.getName()).collect(Collectors.toSet());
                    String concurrencyType = "CONTEXT_SERVICE";
                    setup.addDefinition(ConcurrencyManagedCDIBeans.Type.valueOf(concurrencyType), qualifiers, desc.getName());
                } else if (desc instanceof ManagedExecutorDefinitionDescriptor) {
                    Set<String> qualifiers = Arrays.asList(((ManagedExecutorDefinitionDescriptor)desc).getQualifiers()).stream().map(c -> c.getName()).collect(Collectors.toSet());
                    String concurrencyType = "MANAGED_EXECUTOR_SERVICE";
                    setup.addDefinition(ConcurrencyManagedCDIBeans.Type.valueOf(concurrencyType), qualifiers, desc.getName());
                } else if (desc instanceof ManagedScheduledExecutorDefinitionDescriptor) {
                    Set<String> qualifiers = Arrays.asList(((ManagedScheduledExecutorDefinitionDescriptor)desc).getQualifiers()).stream().map(c -> c.getName()).collect(Collectors.toSet());
                    String concurrencyType = "MANAGED_SCHEDULED_EXECUTOR_SERVICE";
                    setup.addDefinition(ConcurrencyManagedCDIBeans.Type.valueOf(concurrencyType), qualifiers, desc.getName());
                } else if (desc instanceof ManagedThreadFactoryDefinitionDescriptor) {
                    Set<String> qualifiers = Arrays.asList(((ManagedThreadFactoryDefinitionDescriptor)desc).getQualifiers()).stream().map(c -> c.getName()).collect(Collectors.toSet());
                    String concurrencyType = "MANAGED_THREAD_FACTORY";
                    setup.addDefinition(ConcurrencyManagedCDIBeans.Type.valueOf(concurrencyType), qualifiers, desc.getName());
                } else {
                    LOG.severe(() -> "Unexpected Concurrency type! Expected ContextServiceDefinitionDescriptor, ManagedExecutorDefinitionDescriptor, ManagedScheduledExecutorDefinitionDescriptor, or ManagedThreadFactoryDefinitionDescriptor, got " + desc);
                }
            }
            jndiBindings.add(new CompEnvBinding(new SimpleJndiName(ConcurrencyManagedCDIBeans.JDNI_NAME), setup));
             */
        }
    }

    private ResourceDeployer getResourceDeployer(Object resource) {
        return locator.getService(ResourceManagerFactory.class).getResourceDeployer(resource);
    }

    @Override
    public void unbindFromComponentNamespace(JndiNameEnvironment JndiEnvironment) throws NamingException {
        // undeploy all descriptors
        undeployAllDescriptors(JndiEnvironment);

        // Unpublish any global entries exported by this environment
        Collection<JNDIBinding> globalBindings = new ArrayList<>();
        addJNDIBindings(JndiEnvironment, ScopeType.GLOBAL, globalBindings);

        for (JNDIBinding globalBinding : globalBindings) {
            namingManager.unpublishObject(globalBinding.getName());
        }

        Application app = getApplicationFromEnv(JndiEnvironment);

        // undeploy data-sources & mail-sessions exposed by app-client descriptors.
        Set<ApplicationClientDescriptor> appClientDescriptors = app.getBundleDescriptors(ApplicationClientDescriptor.class);
        for (ApplicationClientDescriptor appClientDescriptor : appClientDescriptors) {
            undeployAllDescriptors(appClientDescriptor);
        }

        if (!(JndiEnvironment instanceof ApplicationClientDescriptor) && (app.getBundleDescriptors(ApplicationClientDescriptor.class).size() > 0)) {
            Collection<JNDIBinding> appBindings = new ArrayList<>();
            addJNDIBindings(JndiEnvironment, ScopeType.APP, appBindings);
            for (JNDIBinding appBinding : appBindings) {
                namingManager.unpublishObject(
                    componentNamingUtil.composeInternalGlobalJavaAppName(app.getAppName(), appBinding.getName()));

            }
        }

        if (JndiEnvironment instanceof Application) {
            namingManager.unbindAppObjects(getApplicationName(JndiEnvironment));
        } else {
            // Unbind anything in the component namespace
            String componentEnvId = getComponentEnvId(JndiEnvironment);
            namingManager.unbindComponentObjects(componentEnvId);
            this.unregister(componentEnvId);
        }

    }

    private void undeployAllDescriptors(JndiNameEnvironment env) {
        Set<ResourceDescriptor> allDescriptors = env.getAllResourcesDescriptors(env.getClass());

        for (ResourceDescriptor descriptor : allDescriptors) {
            switch (descriptor.getResourceType()) {
            case DSD:
                if (descriptor instanceof DataSourceDefinitionDescriptor) {
                    DataSourceDefinitionDescriptor dataSourceDefinitionDescriptor = (DataSourceDefinitionDescriptor) descriptor;
                    if (dataSourceDefinitionDescriptor.isDeployed()) {
                        if (undepoyResource(dataSourceDefinitionDescriptor)) {
                            dataSourceDefinitionDescriptor.setDeployed(false);
                        }
                    }
                }
                break;
            case MSD:
                if (descriptor instanceof MailSessionDescriptor) {
                    MailSessionDescriptor mailSessionDescriptor = (MailSessionDescriptor) descriptor;
                    if (mailSessionDescriptor.isDeployed()) {
                        if (undepoyResource(mailSessionDescriptor)) {
                            mailSessionDescriptor.setDeployed(false);
                        }
                    }
                }
                break;
            case CFD:
                if (descriptor instanceof ConnectionFactoryDefinitionDescriptor) {
                    ConnectionFactoryDefinitionDescriptor connectionFactoryDefinitionDescriptor = (ConnectionFactoryDefinitionDescriptor) descriptor;
                    undepoyResource(connectionFactoryDefinitionDescriptor);
                }
                break;
            case JMSCFDD:
                if (descriptor instanceof JMSConnectionFactoryDefinitionDescriptor) {
                    JMSConnectionFactoryDefinitionDescriptor jmsConnectionFactoryDefinitionDescriptor = (JMSConnectionFactoryDefinitionDescriptor) descriptor;
                    undepoyResource(jmsConnectionFactoryDefinitionDescriptor);
                }
                break;
            case AODD:
                if (descriptor instanceof AdministeredObjectDefinitionDescriptor) {
                    AdministeredObjectDefinitionDescriptor administeredObjectDefinitionDescriptor = (AdministeredObjectDefinitionDescriptor) descriptor;
                    undepoyResource(administeredObjectDefinitionDescriptor);
                }
                break;
            default:
                break;
            }
        }
    }

    private boolean undepoyResource(Descriptor descriptor) {
        try {
            ResourceDeployer<Descriptor> deployer = getResourceDeployer(descriptor);
            deployer.undeployResource(descriptor);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unable to undeploy Descriptor [ " + descriptor.getName() + " ] ", e);
            return false;
        }
    }


    private void addEnvironmentProperties(ScopeType scope, Collection<EnvironmentProperty> envProps,
        Collection<JNDIBinding> jndiBindings) {
        for (EnvironmentProperty environmentProperty : envProps) {
            if (!dependencyAppliesToScope(environmentProperty, scope)) {
                continue;
            }
            if (environmentProperty.hasContent()) {
                SimpleJndiName name = toLogicalJndiName(environmentProperty);
                final Object value;
                if (environmentProperty.hasLookupName()) {
                    value = namingUtils.createLazyNamingObjectFactory(name, environmentProperty.getLookupName(), true);
                } else if (environmentProperty.getMappedName().isEmpty()) {
                    value = namingUtils.createSimpleNamingObjectFactory(name, environmentProperty.getValueObject(null));
                } else {
                    value = namingUtils.createLazyNamingObjectFactory(name, environmentProperty.getMappedName(), true);
                }
                jndiBindings.add(new CompEnvBinding(name, value));
            }
        }
    }

    private void addResourceReferences(ScopeType scope, Collection<ResourceReferenceDescriptor> resRefs, Collection<JNDIBinding> jndiBindings) {
        for (ResourceReferenceDescriptor resourceRef : resRefs) {
            if (!dependencyAppliesToScope(resourceRef, scope)) {
                continue;
            }
            resourceRef.checkType();

            SimpleJndiName name = toLogicalJndiName(resourceRef);
            SimpleJndiName physicalJndiName = resourceRef.getJndiName();

            // the jndi-name of URL resource can be either the actual URL value,
            // or another jndi-name that can be looked up
            Object value = null;
            if (resourceRef.isURLResource()) {
                if (physicalJndiName.isJavaGlobal() || physicalJndiName.isJavaApp() || physicalJndiName.isJavaModule()
                    || physicalJndiName.isJavaComponent()) {
                    // for jndi-name or lookup-name like "java:module/env/url/testUrl"
                    value = namingUtils.createLazyNamingObjectFactory(name, physicalJndiName, false);
                } else {
                    try {
                        // For jndi-name like "http://localhost:8080/index.html"
                        value = namingUtils.createCloningNamingObjectFactory(name,
                            namingUtils.createSimpleNamingObjectFactory(name, new URL(physicalJndiName.toString())));
                    } catch (MalformedURLException e) {
                        // For jndi-name or lookup-name like "url/testUrl"
                        value = namingUtils.createLazyNamingObjectFactory(name, physicalJndiName, false);
                    }
                }
            } else if (resourceRef.isORB()) {
                // TODO handle non-default ORBs
                value = namingUtils.createLazyNamingObjectFactory(name, physicalJndiName, false);
            } else if (resourceRef.isWebServiceContext()) {
                WebServiceReferenceManager wsRefMgr = locator.getService(WebServiceReferenceManager.class);
                if (wsRefMgr != null) {
                    value = wsRefMgr.getWSContextObject();
                } else {
                    LOG.log(SEVERE, "Cannot find the following class to proceed with @Resource WebServiceContext"
                        + wsRefMgr + "Please confirm if webservices module is installed ");
                }

            } else if (resourceRef.isJDBCResource() || resourceRef.isJMSConnectionFactory()
                || resourceRef.isMailResource() || resourceRef.isResourceConnectionFactory()) {
                value = namingUtils.createLazyInitializationNamingObjectFactory(name, physicalJndiName, false);
            } else {
                value = namingUtils.createLazyNamingObjectFactory(name, physicalJndiName, false);
            }

            jndiBindings.add(new CompEnvBinding(name, value));

        }
    }


    private void addJNDIBindings(final JndiNameEnvironment env, final ScopeType scope,
        final Collection<JNDIBinding> jndiBindings) {
        // Create objects to be bound for each env dependency. Only add bindings that
        // match the given scope.

        addEnvironmentProperties(scope, env.getEnvironmentProperties(), jndiBindings);

        for (ResourceEnvReferenceDescriptor descriptor : env.getResourceEnvReferenceDescriptors()) {
            if (!dependencyAppliesToScope(descriptor, scope)) {
                continue;
            }
            descriptor.checkType();
            jndiBindings.add(getCompEnvBinding(descriptor));
        }

        addAllDescriptorBindings(env, scope, jndiBindings);

        for (EjbReferenceDescriptor descriptor : env.getEjbReferenceDescriptors()) {
            if (!dependencyAppliesToScope(descriptor, scope)) {
                continue;
            }
            SimpleJndiName name = toLogicalJndiName(descriptor);
            EjbReferenceProxy proxy = new EjbReferenceProxy(descriptor);
            jndiBindings.add(new CompEnvBinding(name, proxy));
        }

        for (MessageDestinationReferenceDescriptor descriptor : env.getMessageDestinationReferenceDescriptors()) {
            if (!dependencyAppliesToScope(descriptor, scope)) {
                continue;
            }
            jndiBindings.add(getCompEnvBinding(descriptor));
        }

        addResourceReferences(scope, env.getResourceReferenceDescriptors(), jndiBindings);

        for (EntityManagerFactoryReferenceDescriptor descriptor : env.getEntityManagerFactoryReferenceDescriptors()) {

            if (!dependencyAppliesToScope(descriptor, scope)) {
                continue;
            }

            SimpleJndiName name = toLogicalJndiName(descriptor);
            Object value = new FactoryForEntityManagerFactoryWrapper(descriptor.getUnitName(), invocationManager, this);
            jndiBindings.add(new CompEnvBinding(name, value));
        }

        for (ServiceReferenceDescriptor descriptor : env.getServiceReferenceDescriptors()) {
            if (!dependencyAppliesToScope(descriptor, scope)) {
                continue;
            }

            if (descriptor.getMappedName() != null) {
                descriptor.setName(descriptor.getMappedName().toString());
            }

            SimpleJndiName name = toLogicalJndiName(descriptor);
            WebServiceRefProxy value = new WebServiceRefProxy(descriptor);
            jndiBindings.add(new CompEnvBinding(name, value));
        }

        for (EntityManagerReferenceDescriptor descriptor : env.getEntityManagerReferenceDescriptors()) {
            if (!dependencyAppliesToScope(descriptor, scope)) {
                continue;
            }
            SimpleJndiName name = toLogicalJndiName(descriptor);
            FactoryForEntityManagerWrapper value = new FactoryForEntityManagerWrapper(descriptor, this);
            jndiBindings.add(new CompEnvBinding(name, value));
        }
    }

    private CompEnvBinding getCompEnvBinding(final ResourceEnvReferenceDescriptor next) {
        final SimpleJndiName name = toLogicalJndiName(next);
        final Object value;
        if (next.isEJBContext()) {
            value = new EjbContextProxy(next.getRefType());
        } else if (next.isValidator()) {
            value = new ValidatorProxy(LOG);
        } else if (next.isValidatorFactory()) {
            value = new ValidatorFactoryProxy(LOG);
        } else if (next.isCDIBeanManager()) {
            value = namingUtils.createLazyNamingObjectFactory(name,
                new SimpleJndiName(JNDI_CTX_JAVA_COMPONENT + "BeanManager"), false);
        } else if (next.isManagedBean()) {
            ManagedBeanDescriptor managedBeanDesc = next.getManagedBeanDescriptor();
            if (processEnvironment.getProcessType().isServer()) {
                value = namingUtils.createLazyNamingObjectFactory(name, next.getJndiName(), false);
            } else {
                value = namingUtils.createLazyNamingObjectFactory(name, managedBeanDesc.getAppJndiName(), false);
            }
        } else {
            // lookup in the InitialContext
            value = new NamingObjectFactory() {
                // It might be mapped to a managed bean, so turn off caching to ensure that a
                // new instance is created each time.
                NamingObjectFactory delegate = namingUtils.createLazyNamingObjectFactory(name, next.getJndiName(), false);

                @Override
                public boolean isCreateResultCacheable() {
                    return false;
                }

                @Override
                public <T> T create(Context ic) throws NamingException {
                    return delegate.create(ic);
                }
            };
        }

        return new CompEnvBinding(name, value);
    }

    private CompEnvBinding getCompEnvBinding(MessageDestinationReferenceDescriptor messageDestinationRef) {
        final SimpleJndiName name = toLogicalJndiName(messageDestinationRef);
        final SimpleJndiName physicalJndiName;
        if (messageDestinationRef.isLinkedToMessageDestination()) {
            physicalJndiName = messageDestinationRef.getMessageDestination().getJndiName();
        } else {
            physicalJndiName = messageDestinationRef.getJndiName();
        }
        return new CompEnvBinding(name, namingUtils.createLazyNamingObjectFactory(name, physicalJndiName, true));
    }

    private boolean dependencyAppliesToScope(Descriptor descriptor, ScopeType scope) {
        return dependencyAppliesToScope(new SimpleJndiName(descriptor.getName()), scope);
    }

    private boolean dependencyAppliesToScope(SimpleJndiName name, ScopeType scope) {
        LOG.log(Level.FINEST, "dependencyAppliesToScope(name={0}, scope={1})", new Object[] {name, scope});
        switch (scope) {
            case COMPONENT:
                // Env names without an explicit java: prefix default to java:comp
                return name.isJavaComponent() || !name.hasJavaPrefix();
            case MODULE:
                return name.isJavaModule();
            case APP:
                return name.isJavaApp();
            case GLOBAL:
                return name.isJavaGlobal();
            default:
                return false;
        }
    }

    /**
     * Generate the name of an environment dependency in the java: namespace.
     * This is the lookup string used by a component to access the dependency.
     */
    private SimpleJndiName toLogicalJndiName(Descriptor descriptor) {
        // If no java: prefix is specified, default to component scope.
        String rawName = descriptor.getName();
        LOG.log(Level.FINEST, "toLogicalJndiName(descriptor); rawName={0}", rawName);
        return new SimpleJndiName(rawName.startsWith(JNDI_CTX_JAVA) ? rawName : (JNDI_CTX_JAVA_COMPONENT_ENV + rawName));
    }

    /**
     * Generate a unique id name for each EE component.
     */
    @Override
    public String getComponentEnvId(JndiNameEnvironment env) {
        String componentEnvId = DOLUtils.getComponentEnvId(env);
        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, "ApplicationName={0}, ComponentId={1}",
                new Object[] {getApplicationName(env), componentEnvId});
        }
        return componentEnvId;
    }

    @Override
    public ApplicationEnvironment getCurrentApplicationEnvironment() {
        return invocationManager.peekAppEnvironment();
    }

    private class FactoryForEntityManagerWrapper implements NamingObjectProxy {

        private final EntityManagerReferenceDescriptor entityManagerRefDescriptor;
        private final ComponentEnvManager componentEnvManager;

        FactoryForEntityManagerWrapper(EntityManagerReferenceDescriptor refDesc, ComponentEnvManager compEnvMgr) {
            this.entityManagerRefDescriptor = refDesc;
            this.componentEnvManager = compEnvMgr;
        }

        @Override
        public Object create(Context ctx) {
            EntityManagerWrapper emWrapper =
                new EntityManagerWrapper(transactionManager, invocationManager, componentEnvManager, callFlowAgent);

            emWrapper.initializeEMWrapper(
                entityManagerRefDescriptor.getUnitName(),
                entityManagerRefDescriptor.getPersistenceContextType(),
                entityManagerRefDescriptor.getSynchronizationType(),
                entityManagerRefDescriptor.getProperties());

            return emWrapper;
        }
    }

    private class EjbContextProxy implements NamingObjectProxy {

        private volatile EjbNamingReferenceManager ejbNamingRefManager;
        private final String contextType;

        EjbContextProxy(String contextType) {
            this.contextType = contextType;
        }

        @Override
        public <T> T create(Context ctx) throws NamingException {
            Object result = null;

            if (ejbNamingRefManager == null) {
                ejbNamingRefManager = locator.getService(EjbNamingReferenceManager.class);
            }

            if (ejbNamingRefManager != null) {
                result = ejbNamingRefManager.getEJBContextObject(contextType);
            }

            if (result == null) {
                throw new NameNotFoundException("Can not resolve EJB context of type " + contextType);
            }

            return (T) result;
        }

    }

    private static class ValidatorProxy implements NamingObjectProxy {

        private volatile ValidatorFactory validatorFactory;
        private volatile Validator validator;
        private final Logger logger;

        private ValidatorProxy(Logger logger) {
            this.logger = logger;
        }

        @Override
        public Validator create(Context ctx) throws NamingException {
            String exceptionMessage = "Can not obtain reference to Validator instance ";

            // Phase 1, obtain a reference to the Validator

            // Create a new Validator instance
            if (validator == null) {

                // no validatorFactory
                if (validatorFactory == null) {
                    ValidatorFactoryProxy factoryProxy = new ValidatorFactoryProxy(logger);
                    validatorFactory = factoryProxy.create(ctx);
                }

                // Use the ValidatorFactory to create a Validator
                if (validatorFactory != null) {
                    ValidatorContext validatorContext = validatorFactory.usingContext();
                    validator = validatorContext.getValidator();
                }
            }

            if (validator == null) {
                throw new NameNotFoundException(exceptionMessage);
            }

            return validator;
        }

    }

    private static class ValidatorFactoryProxy implements NamingObjectProxy {
        private static final String nameForValidatorFactory = JNDI_CTX_JAVA_COMPONENT + "ValidatorFactory";
        private volatile ValidatorFactory validatorFactory;
        private final Logger _logger;

        private ValidatorFactoryProxy(Logger logger) {
            _logger = logger;
        }

        @Override
        public ValidatorFactory create(Context ctx) throws NamingException {

            // create the ValidatorFactory using the spec.
            if (validatorFactory == null) {
                try {
                    validatorFactory = Validation.buildDefaultValidatorFactory();
                } catch (ValidationException e) {
                    _logger.log(Level.WARNING, "Unable to lookup {0}, or build a default Bean Validator Factory: {1}",
                            new Object[] { nameForValidatorFactory, e });
                    NameNotFoundException ne = new NameNotFoundException();
                    ne.initCause(e);
                    throw ne;
                }
            }

            return validatorFactory;
        }
    }

    private class WebServiceRefProxy implements NamingObjectProxy {

        private WebServiceReferenceManager webServiceRefManager;
        private final ServiceReferenceDescriptor serviceRef;

        WebServiceRefProxy(ServiceReferenceDescriptor servRef) {
            this.serviceRef = servRef;
        }

        @Override
        public <T> T create(Context ctx) throws NamingException {

            final T result;
            webServiceRefManager = locator.getService(WebServiceReferenceManager.class);
            if (webServiceRefManager == null) {
                LOG.log(SEVERE, "Cannot find the WebServiceReferenceManager to proceed with @WebServiceRef."
                    + " Please check if webservices module is installed ");
                result = null;
            } else {
                result = (T) webServiceRefManager.resolveWSReference(serviceRef, ctx);
            }

            if (result == null) {
                throw new NameNotFoundException("Can not resolve webservice context of type " + serviceRef.getName());
            }
            return result;
        }
    }

    private class EjbReferenceProxy implements NamingObjectProxy {

        private final EjbReferenceDescriptor ejbRef;

        private volatile EjbNamingReferenceManager ejbRefMgr;
        private volatile Object cachedResult;
        private Boolean cacheable;

        // Note : V2 had a limited form of ejb-ref caching. It only applied
        // to EJB 2.x Home references where the target lived in the same application
        // as the client. It's not clear how useful that even is and it's of limited
        // value given the behavior is different for EJB 3.x references. For now,
        // all ejb-ref caching is turned off.

        EjbReferenceProxy(EjbReferenceDescriptor ejbRef) {
            this.ejbRef = ejbRef;
        }

        @Override
        public <T> T create(Context ctx) throws NamingException {

            Object result = null;
            if (ejbRefMgr == null) {
                synchronized (this) {
                    if (ejbRefMgr == null) {
                        ejbRefMgr = locator.getService(EjbNamingReferenceManager.class);
                        cacheable = ejbRefMgr.isEjbReferenceCacheable(ejbRef);
                    }
                }
            }

            if (ejbRefMgr != null) {
                if (cacheable != null && cacheable.booleanValue()) {
                    if (cachedResult == null) {
                        result = cachedResult = ejbRefMgr.resolveEjbReference(ejbRef, ctx);
                    } else {
                        result = cachedResult;
                    }
                } else {
                    result = ejbRefMgr.resolveEjbReference(ejbRef, ctx);
                }
            }

            if (result == null) {
                throw new NameNotFoundException("Can not resolve ejb reference " + ejbRef.getName() + " : " + ejbRef);
            }

            return (T) result;
        }
    }

    private static class CompEnvBinding implements JNDIBinding {

        private final SimpleJndiName name;
        private final Object value;

        CompEnvBinding(SimpleJndiName name, Object value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public SimpleJndiName getName() {
            return name;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[name=" + name + ", value=" + value + "]";
        }
    }

    private enum ScopeType {
        COMPONENT, MODULE, APP, GLOBAL
    }

}
