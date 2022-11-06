/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.deployment.AbstractConnectorResourceDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerFactoryReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitsDescriptor;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.resourcebase.resources.api.ResourceConstants;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP_ENV;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT_ENV;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_GLOBAL;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_MODULE;

/**
 * Created by Krishna Deepak on 6/9/17.
 */
@Service
public class ResourceValidator implements EventListener, ResourceValidatorVisitor {

    private static final Logger LOG = com.sun.enterprise.deployment.util.DOLUtils.deplLogger;

    @LogMessageInfo(
            message = "JNDI lookup failed for the resource: Name: {0}, Lookup: {1}, Type: {2}.",
            level = "SEVERE",
            cause = "JNDI lookup for the specified resource failed.",
            action = "Configure the required resources before deploying the application.",
            comment = "For the method validateJNDIRefs of com.sun.enterprise.deployment.util.ResourceValidator."
    )
    private static final String RESOURCE_REF_JNDI_LOOKUP_FAILED = "AS-DEPLOYMENT-00026";

    @LogMessageInfo(
            message = "Resource Adapter not present: RA Name: {0}, Type: {1}.",
            level = "SEVERE",
            cause = "Resource apapter specified is invalid.",
            action = "Configure the required resource adapter."
    )
    private static final String RESOURCE_REF_INVALID_RA = "AS-DEPLOYMENT-00027";

    @LogMessageInfo(message = "Skipping resource validation")
    private static final String SKIP_RESOURCE_VALIDATION = "AS-DEPLOYMENT-00028";

    private static final Set<SimpleJndiName> DEFAULT_JNDI_NAMES = Set
        .of(JNDI_CTX_JAVA_COMPONENT + "DefaultDataSource",
            JNDI_CTX_JAVA_COMPONENT + "DefaultJMSConnectionFactory",
            JNDI_CTX_JAVA_COMPONENT + "ORB",
            JNDI_CTX_JAVA_COMPONENT + "DefaultManagedExecutorService",
            JNDI_CTX_JAVA_COMPONENT + "DefaultManagedScheduledExecutorService",
            JNDI_CTX_JAVA_COMPONENT + "DefaultManagedThreadFactory",
            JNDI_CTX_JAVA_COMPONENT + "DefaultContextService",
            JNDI_CTX_JAVA_COMPONENT + "UserTransaction",
            JNDI_CTX_JAVA_COMPONENT + "TransactionSynchronizationRegistry",
            JNDI_CTX_JAVA_COMPONENT + "BeanManager",
            JNDI_CTX_JAVA_COMPONENT + "ValidatorFactory",
            JNDI_CTX_JAVA_COMPONENT + "Validator",
            JNDI_CTX_JAVA_COMPONENT + "InAppClientContainer",
            JNDI_CTX_JAVA_MODULE + "ModuleName",
            JNDI_CTX_JAVA_APP + "AppName")
        .stream().map(SimpleJndiName::new).collect(Collectors.toUnmodifiableSet());

    private String target;

    private DeploymentContext dc;

    private Application application;

    @Inject
    private Events events;

    @Inject
    private Domain domain;

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ResourceValidator.class);

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Server server;

    @PostConstruct
    public void postConstruct() {
        events.register(this);
    }

    @Override
    public void event(Event<?> event) {
        if (event.is(Deployment.AFTER_APPLICATION_CLASSLOADER_CREATION)) {
            dc = (DeploymentContext) event.hook();
            application = dc.getModuleMetaData(Application.class);
            DeployCommandParameters commandParams = dc.getCommandParameters(DeployCommandParameters.class);
            target = commandParams.target;
            if (System.getProperty("deployment.resource.validation", "true").equals("false")) {
                LOG.log(Level.INFO, SKIP_RESOURCE_VALIDATION);
                return;
            }
            if (application == null) {
                return;
            }
            AppResources appResources = new AppResources();
            parseResources(appResources);
            validateResources(appResources);
        }
    }

    /**
     * Store all the resources before starting the validation.
     */
    private void parseResources(AppResources appResources) {
        parseResources(application, appResources);
        for (BundleDescriptor bd : application.getBundleDescriptors()) {
            if (bd instanceof WebBundleDescriptor || bd instanceof ApplicationClientDescriptor) {
                parseResources(bd, appResources);
            }
            if (bd instanceof EjbBundleDescriptor) {
                // Resources from Java files in the ejb.jar which are neither an EJB nor a managed bean are stored here.
                // Skip validation for them, validate only Managed Beans.
                for (ManagedBeanDescriptor mbd: bd.getManagedBeans()) {
                    parseResources(mbd, (JndiNameEnvironment) bd, appResources);
                }
                EjbBundleDescriptor ebd = (EjbBundleDescriptor) bd;
                for (EjbDescriptor ejb : ebd.getEjbs()) {
                    parseEJB(ejb, appResources);
                }
            }
        }

        parseManagedBeans(appResources);

        // Parse AppScoped resources
        String appName = DOLUtils.getApplicationName(application);
        @SuppressWarnings("unchecked")
        Map<String, List<SimpleJndiName>> resourcesList = (Map<String, List<SimpleJndiName>>) dc
            .getTransientAppMetadata().get(ResourceConstants.APP_SCOPED_RESOURCES_JNDI_NAMES);
        appResources.storeAppScopedResources(resourcesList, appName);
    }

    /**
     * Code logic from BaseContainer.java. Store portable and non-portable JNDI names in our namespace.
     * Internal JNDI names not processed as they will not be called from an application.
     *
     * @param ejb
     */
    private void parseEJB(EjbDescriptor ejb, AppResources appResources) {
        SimpleJndiName javaGlobalName = getJavaGlobalJndiNamePrefix(ejb);

        boolean disableNonPortableJndiName = false;
        // TODO: Need to get the value of system-property
        // server.ejb-container.property.disable-nonportable-jndi-names
        Boolean disableInDD = ejb.getEjbBundleDescriptor().getDisableNonportableJndiNames();
        if (disableInDD != null) {
            // explicitly set in glassfish-ejb-jar.xml
            disableNonPortableJndiName = disableInDD;
        }

        SimpleJndiName glassfishSpecificJndiName;
        if (disableNonPortableJndiName) {
            glassfishSpecificJndiName = null;
        } else {
            glassfishSpecificJndiName = ejb.getJndiName();
        }
        if (glassfishSpecificJndiName != null
            && (glassfishSpecificJndiName.isEmpty() || glassfishSpecificJndiName.equals(javaGlobalName))) {
            glassfishSpecificJndiName = null;
        }

        // used to decide whether the javaGlobalName needs to be stored
        int countPortableJndiNames = 0;

        // interfaces now
        if (ejb.isRemoteInterfacesSupported()) {
            String intf = ejb.getHomeClassName();
            SimpleJndiName fullyQualifiedJavaGlobalName = new SimpleJndiName(javaGlobalName + "!" + intf);
            appResources.storeInNamespace(fullyQualifiedJavaGlobalName, ejb);
            countPortableJndiNames++;
            // non-portable
            if(glassfishSpecificJndiName != null) {
                appResources.storeInNamespace(glassfishSpecificJndiName, ejb);
            }
        }

        if (ejb.isRemoteBusinessInterfacesSupported()) {
            int count = 0;
            for (String intf : ejb.getRemoteBusinessClassNames()) {
                count++;
                SimpleJndiName fullyQualifiedJavaGlobalName = new SimpleJndiName(javaGlobalName + "!" + intf);
                appResources.storeInNamespace(fullyQualifiedJavaGlobalName, ejb);
                countPortableJndiNames++;
                // non-portable - interface specific
                if(glassfishSpecificJndiName != null) {
                    SimpleJndiName remoteJndiName = getRemoteEjbJndiName(true, intf, glassfishSpecificJndiName);
                    appResources.storeInNamespace(remoteJndiName, ejb);
                }
            }
            // non-portable - if only one remote business interface exists and no remote home interfaces exist,
            // then by default this can be used to lookup the remote interface.
            if(glassfishSpecificJndiName != null && !ejb.isRemoteInterfacesSupported() && count == 1) {
                appResources.storeInNamespace(glassfishSpecificJndiName, ejb);
            }
        }

        if (ejb.isLocalInterfacesSupported()) {
            String intf = ejb.getLocalHomeClassName();
            SimpleJndiName fullyQualifiedJavaGlobalName = new SimpleJndiName(javaGlobalName + "!" + intf);
            appResources.storeInNamespace(fullyQualifiedJavaGlobalName, ejb);
            countPortableJndiNames++;
        }

        if (ejb.isLocalBusinessInterfacesSupported()) {
            for (String intf : ejb.getLocalBusinessClassNames()) {
                SimpleJndiName fullyQualifiedJavaGlobalName = new SimpleJndiName(javaGlobalName + "!" + intf);
                appResources.storeInNamespace(fullyQualifiedJavaGlobalName, ejb);
                countPortableJndiNames++;
            }
        }

        if (ejb.isLocalBean()) {
            String intf = ejb.getEjbClassName();
            SimpleJndiName fullyQualifiedJavaGlobalName = new SimpleJndiName(javaGlobalName + "!" + intf);
            appResources.storeInNamespace(fullyQualifiedJavaGlobalName, ejb);
            countPortableJndiNames++;
        }

        if (countPortableJndiNames == 1) {
            appResources.storeInNamespace(javaGlobalName, ejb);
        }
        parseResources(ejb, appResources);
    }

    private SimpleJndiName getJavaGlobalJndiNamePrefix(EjbDescriptor ejbDescriptor) {
        final Application app = ejbDescriptor.getApplication();
        final String appName = app.isVirtual() ? null : app.getAppName();
        StringBuilder javaGlobalPrefix = new StringBuilder(JNDI_CTX_JAVA_GLOBAL);
        if (appName != null) {
            javaGlobalPrefix.append(appName);
            javaGlobalPrefix.append('/');
        }
        javaGlobalPrefix.append(ejbDescriptor.getEjbBundleDescriptor().getModuleDescriptor().getModuleName());
        javaGlobalPrefix.append('/');
        javaGlobalPrefix.append(ejbDescriptor.getName());

        return new SimpleJndiName(javaGlobalPrefix.toString());
    }


    private SimpleJndiName getRemoteEjbJndiName(EjbReferenceDescriptor refDesc) {
        String intf = refDesc.isEJB30ClientView() ? refDesc.getEjbInterface() : refDesc.getHomeClassName();
        return getRemoteEjbJndiName(refDesc.isEJB30ClientView(), intf, refDesc.getJndiName());
    }


    private SimpleJndiName getRemoteEjbJndiName(boolean businessView, String interfaceName, SimpleJndiName jndiName) {
        String portableFullyQualifiedPortion = "!" + interfaceName;
        String glassfishFullyQualifiedPortion = "#" + interfaceName;

        if (businessView) {
            if (!jndiName.hasCorbaPrefix()) {
                if (jndiName.isJavaGlobal()) {
                    return checkFullyQualifiedJndiName(jndiName, portableFullyQualifiedPortion);
                }
                return checkFullyQualifiedJndiName(jndiName, glassfishFullyQualifiedPortion);
            }
        } else {
            // Only in the portable global case, convert to a fully-qualified name
            if (jndiName.isJavaGlobal()) {
                return checkFullyQualifiedJndiName(jndiName, portableFullyQualifiedPortion);
            }
        }

        return jndiName;
    }


    private static SimpleJndiName checkFullyQualifiedJndiName(SimpleJndiName origJndiName, String fullyQualifiedPortion) {
        if (origJndiName.hasSuffix(fullyQualifiedPortion)) {
            return origJndiName;
        }
        return new SimpleJndiName(origJndiName + fullyQualifiedPortion);
    }


    private void parseManagedBeans(AppResources appResources) {
        for (BundleDescriptor bd : application.getBundleDescriptors()) {
            for (ManagedBeanDescriptor managedBean : bd.getManagedBeans()) {
                appResources.storeInNamespace(managedBean.getGlobalJndiName(), (JndiNameEnvironment) bd);
            }
        }
    }


    private void parseResources(BundleDescriptor bd, AppResources appResources) {
        if (!(bd instanceof JndiNameEnvironment)) {
            return;
        }
        JndiNameEnvironment env = (JndiNameEnvironment) bd;
        for (ResourceReferenceDescriptor next : env.getResourceReferenceDescriptors()) {
            parseResources(next, env, appResources);
        }

        for (ResourceEnvReferenceDescriptor next : env.getResourceEnvReferenceDescriptors()) {
            parseResources(next, env, appResources);
        }

        for (MessageDestinationReferenceDescriptor next : env.getMessageDestinationReferenceDescriptors()) {
            parseResources(next, env, appResources);
        }

        for (EnvironmentProperty next : env.getEnvironmentProperties()) {
            parseResources(next, env, appResources);
        }

        for (ResourceDescriptor next : env.getAllResourcesDescriptors()) {
            parseResources(next, env, appResources);
        }

        for (EntityManagerReferenceDescriptor next : env.getEntityManagerReferenceDescriptors()) {
            storeInNamespace(SimpleJndiName.of(next.getName()), env, appResources);
        }

        for (EntityManagerFactoryReferenceDescriptor next : env.getEntityManagerFactoryReferenceDescriptors()) {
            storeInNamespace(SimpleJndiName.of(next.getName()), env, appResources);
        }

        for (EjbReferenceDescriptor next : env.getEjbReferenceDescriptors()) {
            parseResources(next, env, appResources);
        }

        for (ServiceReferenceDescriptor next : env.getServiceReferenceDescriptors()) {
            parseResources(next, env, appResources);
        }

        for (PersistenceUnitsDescriptor pus : bd.getExtensionsDescriptors(PersistenceUnitsDescriptor.class)) {
            for (PersistenceUnitDescriptor pu : pus.getPersistenceUnitDescriptors()) {
                parseResources(pu, env, appResources);
            }
        }

        for (ManagedBeanDescriptor mbd : bd.getManagedBeans()) {
            parseResources(mbd, env, appResources);
        }
    }


    /**
     * Store resources in ResourceRefDescriptor.
     */
    private void parseResources(ResourceReferenceDescriptor resRef, JndiNameEnvironment env, AppResources appResources) {
        resRef.checkType();
        SimpleJndiName name = getLogicalJNDIName(SimpleJndiName.of(resRef.getName()), env);
        String type = resRef.getType();
        SimpleJndiName jndiName = resRef.getJndiName();
        AppResource resRefResource = new AppResource(name, jndiName, type, env, true);

        if (resRef.isURLResource()) {
            if (jndiName != null && !jndiName.hasJavaPrefix()) {
                try {
                    // for jndi-name like "http://localhost:8080/index.html"
                    new URL(jndiName.toString());
                    resRefResource.noValidation();
                } catch (MalformedURLException e) {
                    // If jndi-name is not an actual url, we might want to lookup the name
                }
            }
        }
        if (resRef.isWebServiceContext()) {
            resRefResource.noValidation();
        }

        appResources.store(resRefResource);
    }

    /**
     * Store resources in ResourceEnvRefDescriptor.
     */
    private void parseResources(ResourceEnvReferenceDescriptor resEnvRef, JndiNameEnvironment env, AppResources appResources) {
        resEnvRef.checkType();
        SimpleJndiName name = getLogicalJNDIName(SimpleJndiName.of(resEnvRef.getName()), env);
        String type = resEnvRef.getType();
        SimpleJndiName jndiName = resEnvRef.getJndiName();
        AppResource resEnvRefResource = new AppResource(name, jndiName, type, env, true);

        if (resEnvRef.isEJBContext() || resEnvRef.isValidator() || resEnvRef.isValidatorFactory() || resEnvRef.isCDIBeanManager()) {
            resEnvRefResource.noValidation();
        }

        appResources.store(resEnvRefResource);
    }

    /**
     * If the message destination ref is linked to a message destination, fetch the linked destination and validate it.
     * We might be duplicating our validation efforts since we are already validating message destination separately.
     */
    private void parseResources(MessageDestinationReferenceDescriptor msgDestRef, JndiNameEnvironment env, AppResources appResources) {
        SimpleJndiName name = getLogicalJNDIName(SimpleJndiName.of(msgDestRef.getName()), env);
        SimpleJndiName jndiName;
        if (msgDestRef.isLinkedToMessageDestination()) {
            jndiName = msgDestRef.getMessageDestination().getJndiName();
        } else {
            jndiName = msgDestRef.getJndiName();
        }
        appResources.store(new AppResource(name, jndiName, msgDestRef.getType(), env, true));
    }

    /**
     * Store references to environment entries.
     * Also validate custom resources of primitive data types.
     */
    private void parseResources(EnvironmentProperty envProp, JndiNameEnvironment env, AppResources appResources) {
        SimpleJndiName name = getLogicalJNDIName(SimpleJndiName.of(envProp.getName()), env);
        SimpleJndiName jndiName;
        if (envProp.hasLookupName()) {
            jndiName = envProp.getLookupName();
        } else if (!envProp.getMappedName().isEmpty()) {
            jndiName = envProp.getMappedName();
        } else {
            jndiName = new SimpleJndiName("");
        }

        AppResource envPropResource = new AppResource(name, jndiName, envProp.getType(), env, true);
        // If lookup/mapped name is not present, then we do not need to validate.
        if (jndiName.isEmpty()) {
            envPropResource.noValidation();
        }

        appResources.store(envPropResource);

        // Store EnvProps even if they do not have a valid lookup element
        appResources.storeInNamespace(name, env);
    }


    /**
     * Logic from EjbNamingReferenceManagerImpl.java - Here EJB references get resolved
     */
    private void parseResources(EjbReferenceDescriptor ejbRef, JndiNameEnvironment env, AppResources appResources) {
        SimpleJndiName name = getLogicalJNDIName(SimpleJndiName.of(ejbRef.getName()), env);
        // we only need to worry about those references which are not linked yet
        if (ejbRef.getEjbDescriptor() != null) {
            appResources.storeInNamespace(name, env);
            return;
        }

        SimpleJndiName jndiName = new SimpleJndiName("");
        // Should we use an inverse approach i.e., skip validation only in special cases?
        // Not sure if that is required as the below approach works fine while resolving EJB references
        boolean validationRequired = false;

        // local
        if (ejbRef.isLocal()) {
            // mapped name has no meaning for local ejb-ref as non-portable JNDI names don't have any meaning in this case?
            if (ejbRef.hasLookupName()) {
                jndiName = ejbRef.getLookupName();
                validationRequired = true;
            }
        } else {
            // remote
            // mapped-name takes precedence over lookup name
            SimpleJndiName ejbRefJndiName = getJndiName(ejbRef);
            if (ejbRefJndiName == null && ejbRef.hasLookupName()) {
                jndiName = ejbRef.getLookupName();
                validationRequired = true;
            } else if (ejbRefJndiName != null && ejbRefJndiName.isJavaApp()
                && !ejbRefJndiName.hasPrefix(JNDI_CTX_JAVA_APP_ENV)) {
                // TODO: A case skipped from EjbNamingRefManager
                // Why does the below logic exist in the EjbNamingRefMan code?
                // Intentionally or not, this resolves the java:app mapped names
                // Seems suspicious as the corresponding java:global case is handled in the getRemoteEjbJndiName function call
                String appName = DOLUtils.getApplicationName(application);
                jndiName = ejbRefJndiName.changePrefix(JNDI_CTX_JAVA_GLOBAL + appName + '/');
                validationRequired = true;
            } else {
                SimpleJndiName remoteJndiName = getRemoteEjbJndiName(ejbRef);
                // TODO: CORBA case
                if (!remoteJndiName.hasCorbaPrefix()) {
                    validationRequired = true;
                    jndiName = remoteJndiName;
                }
            }
        }

        appResources.store(new AppResource(name, jndiName, ejbRef.getType(), env, validationRequired));
    }


    private SimpleJndiName getJndiName(EjbReferenceDescriptor descriptor) {
        if (!descriptor.hasJndiName()) {
            return null;
        }
        return descriptor.getJndiName();
    }


    private void parseResources(ServiceReferenceDescriptor serviceRef, JndiNameEnvironment env,
        AppResources appResources) {
        SimpleJndiName name = getLogicalJNDIName(SimpleJndiName.of(serviceRef.getName()), env);
        if (serviceRef.hasLookupName()) {
            SimpleJndiName lookupName = serviceRef.getLookupName();
            appResources.store(new AppResource(name, lookupName, serviceRef.getType(), env, true));
        } else {
            appResources.storeInNamespace(name, env);
        }
    }


    /**
     * Store the resource definitions in our namespace.
     * CFD and AODD are not valid in an AppClient. O/w need to validate the ra-name in them.
     */
    private void parseResources(ResourceDescriptor resourceDescriptor, JndiNameEnvironment env, AppResources appResources) {
        JavaEEResourceType type = resourceDescriptor.getResourceType();
        if (type == JavaEEResourceType.CFD || type == JavaEEResourceType.AODD) {
            if (env instanceof ApplicationClientDescriptor) {
                return;
            }
            // No need to type check as CFD and AODD extend from AbstractConnectorResourceDescriptor
            AbstractConnectorResourceDescriptor acrd = (AbstractConnectorResourceDescriptor) resourceDescriptor;
            SimpleJndiName raJndiName = SimpleJndiName.of(acrd.getResourceAdapter());
            appResources
                .store(new AppResource(resourceDescriptor.getJndiName(), raJndiName, type.toString(), env, true));
        } else {
            // nothing to validate here. store the definitions in our namespace.
            storeInNamespace(resourceDescriptor.getJndiName(), env, appResources);
        }
    }


    /**
     * Record the Data Source specified in PUD.
     */
    private void parseResources(PersistenceUnitDescriptor pu, JndiNameEnvironment env, AppResources appResources) {
        SimpleJndiName jtaDataSourceName = pu.getJtaDataSource();
        SimpleJndiName nonJtaDataSourceName = pu.getNonJtaDataSource();
        if (jtaDataSourceName != null && !jtaDataSourceName.isEmpty()) {
            appResources.store(new AppResource(pu.getJndiName(), jtaDataSourceName, "javax.sql.DataSource", env, true));
        }
        if (nonJtaDataSourceName != null && !nonJtaDataSourceName.isEmpty()) {
            appResources.store(new AppResource(pu.getJndiName(), nonJtaDataSourceName, "javax.sql.DataSource", env, true));
        }
    }


    private void parseResources(ManagedBeanDescriptor managedBean, JndiNameEnvironment env, AppResources appResources) {
        for (ResourceReferenceDescriptor next : managedBean.getResourceReferenceDescriptors()) {
            parseResources(next, env, appResources);
        }

        for (ResourceEnvReferenceDescriptor next : managedBean.getResourceEnvReferenceDescriptors()) {
            parseResources(next, env, appResources);
        }

        for (MessageDestinationReferenceDescriptor next : managedBean.getMessageDestinationReferenceDescriptors()) {
            parseResources(next, env, appResources);
        }

        for (EjbReferenceDescriptor next : managedBean.getEjbReferenceDescriptors()) {
            parseResources(next, env, appResources);
        }

        for (EnvironmentProperty next : managedBean.getEnvironmentProperties()) {
            parseResources(next, env, appResources);
        }

        for (ResourceDescriptor next : env.getAllResourcesDescriptors()) {
            parseResources(next, env, appResources);
        }
    }


    private void parseResources(EjbDescriptor ejb, AppResources appResources) {
        for (ResourceReferenceDescriptor next : ejb.getResourceReferenceDescriptors()) {
            parseResources(next, ejb, appResources);
        }

        for (ResourceEnvReferenceDescriptor next : ejb.getResourceEnvReferenceDescriptors()) {
            parseResources(next, ejb, appResources);
        }

        for (MessageDestinationReferenceDescriptor next : ejb.getMessageDestinationReferenceDescriptors()) {
            parseResources(next, ejb, appResources);
        }

        for (EnvironmentProperty next : ejb.getEnvironmentProperties()) {
            parseResources(next, ejb, appResources);
        }

        for (EjbReferenceDescriptor next : ejb.getEjbReferenceDescriptors()) {
            parseResources(next, ejb, appResources);
        }

        for (ResourceDescriptor next : ejb.getAllResourcesDescriptors()) {
            parseResources(next, ejb, appResources);
        }
    }


    private void storeInNamespace(SimpleJndiName name, JndiNameEnvironment env, AppResources appResources) {
        SimpleJndiName logicalJNDIName = getLogicalJNDIName(name, env);
        appResources.storeInNamespace(logicalJNDIName, env);
    }


    /**
     * @param rawName to be converted
     * @return The logical JNDI name which has a java: prefix
     */
    private SimpleJndiName getLogicalJNDIName(SimpleJndiName rawName, JndiNameEnvironment env) {
        SimpleJndiName jndiName = rawName.hasJavaPrefix() ? rawName : rawName.changePrefix(JNDI_CTX_JAVA_COMPONENT_ENV);
        boolean treatComponentAsModule = DOLUtils.getTreatComponentAsModule(env);
        if (treatComponentAsModule && jndiName.isJavaComponent()) {
            return jndiName.changePrefix(JNDI_CTX_JAVA_MODULE);
        }
        return jndiName;
    }


    /**
     * Convert JNDI names beginning with java:module and java:app to their corresponding java:global
     * names.
     *
     * @return the converted name with java:global JNDI prefix.
     */
    private SimpleJndiName convertModuleOrAppJNDIName(SimpleJndiName jndiName, JndiNameEnvironment env) {

        if (jndiName == null) {
            return null;
        }

        final BundleDescriptor bd;
        if (env instanceof EjbDescriptor) {
            bd = ((EjbDescriptor) env).getEjbBundleDescriptor();
        } else if (env instanceof BundleDescriptor) {
            bd = (BundleDescriptor) env;
        } else {
            bd = null;
        }

        if (bd == null) {
            return new SimpleJndiName("");
        }
        final String appName = application.isVirtual() ? null : application.getAppName();
        final String moduleName = bd.getModuleDescriptor().getModuleName();
        final StringBuilder javaGlobalName = new StringBuilder(JNDI_CTX_JAVA_GLOBAL);
        if (jndiName.isJavaApp()) {
            if (appName != null) {
                javaGlobalName.append(appName);
                javaGlobalName.append('/');
            }
            javaGlobalName.append(jndiName.removePrefix(JNDI_CTX_JAVA_APP));
        } else if (jndiName.isJavaModule()) {
            if (appName != null) {
                javaGlobalName.append(appName);
                javaGlobalName.append('/');
            }

            javaGlobalName.append(moduleName);
            javaGlobalName.append('/');
            javaGlobalName.append(jndiName.removePrefix(JNDI_CTX_JAVA_MODULE));
        } else {
            return new SimpleJndiName("");
        }
        return new SimpleJndiName(javaGlobalName.toString());
    }


    /**
     * Start of validation logic.
     */
    private void validateResources(AppResources appResources) {
        for (AppResource resource : appResources.myResources) {
            if (!resource.validate) {
                continue;
            }
            if (JavaEEResourceType.CFD.name().equals(resource.getType())
                || JavaEEResourceType.AODD.name().equals(resource.getType())) {
                validateRAName(resource);
            } else {
                validateJNDIRefs(resource, appResources.myNamespace);
            }
        }
        // Validate the ra-names of app scoped resources
        // RA-name and the type of this resource are stored
        List<Map.Entry<String, String>> raNames = (List<Map.Entry<String, String>>) dc.getTransientAppMetadata()
            .get(ResourceConstants.APP_SCOPED_RESOURCES_RA_NAMES);
        if (raNames == null) {
            return;
        }
        for (Map.Entry<String, String> entry : raNames) {
            validateRAName(entry.getKey(), entry.getValue());
        }
    }


    /**
     * Validate the resource adapter names of @CFD, @AODD.
     */
    private void validateRAName(AppResource resource) {
        String raName = resource.getJndiName() == null ? null : resource.getJndiName().toString();
        validateRAName(raName, resource.getType());
    }


    /**
     * Strategy to validate the resource adapter name:
     * 1) In case of stand-alone RA, look in the domain.xml and for default system RA's
     * 2) In case of embedded RA, compare it with names of RAR descriptors
     * In case of null ra name, we fail the deployment.
     */
    private void validateRAName(String raName, String type) {
        // No ra-name specified
        if (raName == null || raName.isEmpty()) {
            LOG.log(Level.SEVERE, RESOURCE_REF_INVALID_RA, new Object[] {null, type});
            throw new DeploymentException(localStrings.getLocalString("enterprise.deployment.util.ra.validation",
                "Resource Adapter not present: RA Name: {0}, Type: {1}.", null, type));
        }
        int poundIndex = raName.indexOf("#");

        // Pound not present: check for app named raname in domain.xml, check for system ra's
        if (poundIndex < 0) {
            if (domain.getApplications().getApplication(raName) != null) {
                return;
            }
            // System RA's - Copied from ConnectorConstants.java
            if (raName.equals("jmsra") || raName.equals("__ds_jdbc_ra") || raName.equals("jaxr-ra") ||
                    raName.equals("__cp_jdbc_ra") || raName.equals("__xa_jdbc_ra") || raName.equals("__dm_jdbc_ra")) {
                return;
            }
            if (isEmbedded(raName)) {
                return;
            }
        } else if (raName.substring(0, poundIndex).equals(application.getAppName())) {
            // Embedded RA
            // In case the app name does not match, we fail the deployment
            raName = raName.substring(poundIndex + 1);
            if (isEmbedded(raName)) {
                return;
            }
        }
        LOG.log(Level.SEVERE, RESOURCE_REF_INVALID_RA, new Object[] {raName, type});
        throw new DeploymentException(localStrings.getLocalString(
                "enterprise.deployment.util.ra.validation",
                "Resource Adapter not present: RA Name: {0}, Type: {1}.",
                raName, type));
    }


    private boolean isEmbedded(String raName) {
        String ranameWithRAR = raName + ".rar";
        // check for rar named this
        for (BundleDescriptor bd : application.getBundleDescriptors(ConnectorDescriptor.class)) {
            if (raName.equals(bd.getModuleName()) || ranameWithRAR.equals(bd.getModuleName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Strategy for validating a given jndi name
     * 1) Check in domain.xml
     * 2) Check in the resources defined within the app. These have not been binded to the namespace yet.
     * 3) Check for resources defined by an earlier application.
     * In case a null jndi name is passed, we fail the deployment.
     *
     * @param resource to be validated.
     */
    private void validateJNDIRefs(AppResource resource, JNDINamespace namespace) {
        // In case lookup is not present, check if another resource with the same name exists
        if (!resource.hasLookup()) {
            if (namespace.find(resource.getName(), resource.getEnv())) {
                return;
            }
            LOG.log(Level.SEVERE, RESOURCE_REF_JNDI_LOOKUP_FAILED,
                new Object[] {resource.getName(), null, resource.getType()});
            throw new DeploymentException(
                MessageFormat.format("JNDI lookup failed for the resource: Name: {0}, Lookup: {1}, Type: {2}",
                    resource.getName(), null, resource.getType()));
        }

        // hasLookup returned true, so it cannot be null.
        SimpleJndiName jndiName = resource.getJndiName();
        JndiNameEnvironment env = resource.getEnv();

        if (isResourceInDomainXML(jndiName) || DEFAULT_JNDI_NAMES.contains(jndiName)) {
            return;
        }

        // Managed Bean & EJB portable JNDI names
        if (jndiName.isJavaModule() || jndiName.isJavaApp()) {
            SimpleJndiName newName = convertModuleOrAppJNDIName(jndiName, resource.getEnv());
            if (namespace.find(newName, env)) {
                return;
            }
        }

        // EJB Non-portable JNDI names
        if (!jndiName.hasJavaPrefix()) {
            if (namespace.find(jndiName, env)) {
                return;
            }
        }

        // convert comp to module if req
        SimpleJndiName convertedJndiName = getLogicalJNDIName(jndiName, env);
        if (namespace.find(convertedJndiName, env)) {
            return;
        }

        try {
            if (loadOnCurrentInstance()) {
                InitialContext.doLookup(jndiName.toString());
            }
        } catch (NamingException e) {
            throw new DeploymentException(
                MessageFormat.format("JNDI lookup failed for the resource: Name: {0}, Lookup: {1}, Type: {2}",
                    resource.getName(), jndiName, resource.getType()),
                e);
        }
    }


    /**
     * Validate the given resource in the corresponding target using domain.xml server beans.
     * For resources defined outside the application.
     *
     * @param jndiName to be validated
     * @return True if resource is present in domain.xml in the corresponding target. False otherwise.
     */
    private boolean isResourceInDomainXML(SimpleJndiName jndiName) {
        if (jndiName == null) {
            return false;
        }

        Server svr = domain.getServerNamed(target);
        if (svr != null) {
            return svr.isResourceRefExists(jndiName);
        }

        Cluster cluster = domain.getClusterNamed(target);
        return cluster != null && cluster.isResourceRefExists(jndiName);
    }

    private static class AppResource {
        private final SimpleJndiName name;

        private final SimpleJndiName lookup;

        private final String type;

        private final JndiNameEnvironment env;

        boolean validate;

        private AppResource(SimpleJndiName name, SimpleJndiName lookup, String type, JndiNameEnvironment env, boolean validate) {
            this.name = name;
            this.lookup = lookup;
            this.type = type;
            this.env = env;
            this.validate = validate;
        }

        private SimpleJndiName getJndiName() {
            return lookup;
        }

        private JndiNameEnvironment getEnv() {
            return env;
        }

        private SimpleJndiName getName() {
            return name;
        }

        private String getType() {
            return type;
        }

        private boolean hasLookup() {
            return lookup != null && !lookup.isEmpty();
        }

        private void noValidation() {
            validate = false;
        }
    }

    private static class AppResources {
        List<AppResource> myResources;
        JNDINamespace myNamespace;

        private AppResources() {
            myResources = new ArrayList<>();
            myNamespace = new JNDINamespace();
        }

        /**
         * Store in namespace only if it has a valid lookup value. This is because we do not want to store invalid
         * resources in our namespace.
         */
        private void store(AppResource resource) {
            myResources.add(resource);
            if (resource.hasLookup()) {
                myNamespace.store(resource.name, resource.env);
            }
        }

        /**
         * If we know that the name points to a valid resource, directly store in namespace.
         */
        private void storeInNamespace(SimpleJndiName name, JndiNameEnvironment env) {
            myNamespace.store(name, env);
        }

        private void storeAppScopedResources(Map<String, List<SimpleJndiName>> resourcesList, String appName) {
            myNamespace.storeAppScopedResources(resourcesList, appName);
        }
    }

    /**
     * A class to record all the logical JNDI names of resources defined in the application in the appropriate scopes.
     * App scoped resources, Resource Definitions are also stored in this data structure.
     */
    private static class JNDINamespace {
        private final Map<String, List<SimpleJndiName>> componentNamespaces;
        private final Map<String, List<SimpleJndiName>> moduleNamespaces;
        private final List<SimpleJndiName> appNamespace;
        private final List<SimpleJndiName> globalNameSpace;
        private final List<SimpleJndiName> nonPortableJndiNames;

        private JNDINamespace() {
            componentNamespaces = new HashMap<>();
            moduleNamespaces = new HashMap<>();
            appNamespace = new ArrayList<>();
            globalNameSpace = new ArrayList<>();
            nonPortableJndiNames = new ArrayList<>();
        }

        /**
         * Store app scoped resources in this namespace to facilitate lookup during validation.
         *
         * @param resources - App scoped resources
         * @param appName - Application name
         */
        private void storeAppScopedResources(Map<String, List<SimpleJndiName>> resources, String appName) {
            if (resources == null) {
                return;
            }
            List<SimpleJndiName> appLevelResources = resources.get(appName);
            if (appLevelResources != null) {
                appNamespace.addAll(appLevelResources);
            }
            for (Map.Entry<String, List<SimpleJndiName>> resource: resources.entrySet()) {
                if (!resource.getKey().equals(appName)) {
                    String moduleName = getActualModuleName(resource.getKey());
                    List<SimpleJndiName> jndiNames = moduleNamespaces.get(moduleName);
                    if (jndiNames == null) {
                        jndiNames = new ArrayList<>();
                        jndiNames.addAll(resource.getValue());
                        moduleNamespaces.put(moduleName, jndiNames);
                    } else {
                        jndiNames.addAll(resource.getValue());
                    }
                }
            }
        }


        /**
         * Store the jndi name in the correct scope. Will be stored only if jndi name is javaURL.
         */
        public void store(SimpleJndiName jndiName, JndiNameEnvironment env) {
            LOG.log(Level.FINEST, "store(jndiName={0}, env)", jndiName);
            if (jndiName.isJavaComponent()) {
                String componentId = DOLUtils.getComponentEnvId(env);
                List<SimpleJndiName> jndiNames = componentNamespaces.get(componentId);
                if (jndiNames == null) {
                    jndiNames = new ArrayList<>();
                    jndiNames.add(jndiName);
                    componentNamespaces.put(componentId, jndiNames);
                } else {
                    jndiNames.add(jndiName);
                }
            } else if (jndiName.isJavaModule()) {
                String moduleName = getActualModuleName(DOLUtils.getModuleName(env));
                List<SimpleJndiName> jndiNames = moduleNamespaces.get(moduleName);
                if (jndiNames == null) {
                    jndiNames = new ArrayList<>();
                    jndiNames.add(jndiName);
                    moduleNamespaces.put(moduleName, jndiNames);
                } else {
                    jndiNames.add(jndiName);
                }
            } else if (jndiName.isJavaApp()) {
                appNamespace.add(jndiName);
            } else if (jndiName.isJavaGlobal()) {
                globalNameSpace.add(jndiName);
            } else {
                nonPortableJndiNames.add(jndiName);
            }
        }


        /**
         * Find the jndi name in our namespace.
         *
         * @return True if the jndi name is found in the namespace. False otherwise.
         */
        public boolean find(SimpleJndiName jndiName, JndiNameEnvironment env) {
            LOG.log(Level.FINE, "find(jndiName={0}, env)", jndiName);
            if (jndiName == null) {
                return false;
            }
            if (jndiName.isJavaComponent()) {
                String componentId = DOLUtils.getComponentEnvId(env);
                List<?> jndiNames = componentNamespaces.get(componentId);
                return jndiNames != null && jndiNames.contains(jndiName);
            } else if (jndiName.isJavaModule()) {
                String moduleName = getActualModuleName(DOLUtils.getModuleName(env));
                List<SimpleJndiName> jndiNames = moduleNamespaces.get(moduleName);
                return jndiNames != null && jndiNames.contains(jndiName);
            } else if (jndiName.isJavaApp()) {
                return appNamespace.contains(jndiName);
            } else if (jndiName.isJavaGlobal()) {
                return globalNameSpace.contains(jndiName);
            } else {
                return nonPortableJndiNames.contains(jndiName);
            }
        }


        /**
         * Remove suffix from the module name.
         */
        private String getActualModuleName(String moduleName) {
            if (moduleName != null) {
                if (moduleName.endsWith(".jar") || moduleName.endsWith(".war") || moduleName.endsWith(".rar")) {
                    moduleName = moduleName.substring(0, moduleName.length() - 4);
                }
            }
            return moduleName;
        }
    }

    /**
     * Copy from ApplicationLifeCycle.java
     */
    private boolean loadOnCurrentInstance() {
        final DeployCommandParameters commandParams = dc.getCommandParameters(DeployCommandParameters.class);
        final Properties appProps = dc.getAppProps();
        if (commandParams.enabled) {
            // if the current instance match with the target
            if (domain.isCurrentInstanceMatchingTarget(commandParams.target, commandParams.name(), server.getName(),
                    dc.getTransientAppMetaData(DeploymentProperties.PREVIOUS_TARGETS, List.class))) {
                return true;
            }
            if (server.isDas()) {
                String objectType = appProps.getProperty(ServerTags.OBJECT_TYPE);
                if (objectType != null) {
                    // if it's a system application needs to be loaded on DAS
                    if (objectType.equals(DeploymentProperties.SYSTEM_ADMIN)
                        || objectType.equals(DeploymentProperties.SYSTEM_ALL)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
