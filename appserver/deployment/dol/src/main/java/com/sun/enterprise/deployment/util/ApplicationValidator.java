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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.AbstractConnectorResourceDescriptor;
import com.sun.enterprise.deployment.AdministeredObjectDefinitionDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.CommonResourceBundleDescriptor;
import com.sun.enterprise.deployment.CommonResourceValidator;
import com.sun.enterprise.deployment.ConnectionFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.DataSourceDefinitionDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbIORConfigurationDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.InjectionCapable;
import com.sun.enterprise.deployment.MailSessionDescriptor;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.types.EjbReference;
import com.sun.enterprise.deployment.types.MessageDestinationReferencer;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.DescriptorVisitor;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.deployment.util.DOLUtils.INVALID_NAMESPACE;

/**
 * This class is responsible for validating the loaded DOL classes and
 * transform some of the raw XML information into refined values used
 * by the DOL runtime
 *
 * @author Jerome Dochez
 */
@Service(name="application_deploy")
public class ApplicationValidator extends ComponentValidator implements ApplicationVisitor, ManagedBeanVisitor {

    @LogMessageInfo(
        message = "Application validation failed for application: {0}, jndi-name: {1}, resource adapter name: {2} is wrong.",
        level="SEVERE",
        cause = "For embedded resource adapter, its name should begin with '#' symbol",
        action = "Remove application name before the '#' symbol in the resource adapter name.",
        comment = "For the method validateResourceDescriptor of com.sun.enterprise.deployment.util.ApplicationValidator"
    )
    private static final String RESOURCE_ADAPTER_NAME_INVALID = "AS-DEPLOYMENT-00020";

    private static final Logger LOG = DOLUtils.getDefaultLogger();

    /** Used to store all descriptor details for validation purpose */
    private final HashMap<SimpleJndiName, CommonResourceValidator> allResourceDescriptors = new HashMap<>();

    /** Used to store keys and descriptor names for validation purpose */
    private final HashMap<String, Vector<String>> validNameSpaceDetails = new HashMap<>();

    private static final String APPCLIENT_KEYS = "APPCLIENT_KEYS";
    private static final String EJBBUNDLE_KEYS = "EJBBUNDLE_KEYS";
    private static final String APP_KEYS = "APP_KEYS";
    private static final String WEBBUNDLE_KEYS = "WEBBUNDLE_KEYS";
    private static final String EJB_KEYS = "EJB_KEYS";
    private static final String CONNECTOR_KEYS = "CONNECTOR_KEYS";

    private static final String JNDI_COMP = "java:comp";
    private static final String JNDI_MODULE = "java:module";
    private static final String JNDI_APP = "java:app";

    private boolean allUniqueResource = true;

    private SimpleJndiName inValidJndiName;

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ApplicationValidator.class);

    @Override
    public void accept(BundleDescriptor descriptor) {
        if (descriptor instanceof Application) {
            Application application = (Application) descriptor;
            accept(application);

            if (!validateResourceDescriptor(application)) {
                LOG.log(Level.SEVERE, DOLUtils.APPLICATION_VALIDATION_FAILS, application.getAppName());
                throw new IllegalStateException(
                    localStrings.getLocalString("enterprise.deployment.util.application.fail",
                        "Application validation fails for given application {0} for jndi-name {1}",
                        application.getAppName(), inValidJndiName));
            }

            // valdiate env entries
            validateEnvEntries(application);

            for (BundleDescriptor ebd : application.getBundleDescriptorsOfType(DOLUtils.ejbType())) {
                ebd.visit(getSubDescriptorVisitor(ebd));
            }

            for (BundleDescriptor wbd : application.getBundleDescriptorsOfType(DOLUtils.warType())) {
                // This might be null in the case of an appclient
                // processing a client stubs .jar whose original .ear contained
                // a .war. This will be fixed correctly in the deployment
                // stage but until then adding a non-null check will prevent
                // the validation step from bombing.
                if (wbd != null) {
                    wbd.visit(getSubDescriptorVisitor(wbd));
                }
            }

            for (BundleDescriptor cd :  application.getBundleDescriptorsOfType(DOLUtils.rarType())) {
                cd.visit(getSubDescriptorVisitor(cd));
            }

            for (BundleDescriptor acd : application.getBundleDescriptorsOfType(DOLUtils.carType())) {
                acd.visit(getSubDescriptorVisitor(acd));
            }

            // Visit all injectables first.  In some cases, basic type
            // information has to be derived from target inject method or
            // inject field.
            for (InjectionCapable injectable : application.getInjectableResources(application)) {
                accept(injectable);
            }

            for (BundleDescriptor bundle : application.getBundleDescriptors()) {
                for (ManagedBeanDescriptor next : bundle.getManagedBeans()) {
                    next.validate();
                }
            }
        }
        super.accept(descriptor);
    }

    /**
     * Visit an application object
     *
     * @param application the application descriptor
     */
    @Override
    public void accept(Application application) {
        this.application = application;
        if (application.getBundleDescriptors().isEmpty()) {
            throw new IllegalArgumentException(
                "Application [" + application.getRegistrationName() + "] contains no valid components");
        }

        // now resolve any conflicted module names in the application

        // list to store the current conflicted modules
        List<ModuleDescriptor<BundleDescriptor>> conflicted = new ArrayList<>();
        // make sure all the modules have unique names
        Set<ModuleDescriptor<BundleDescriptor>> modules = application.getModules();
        for (ModuleDescriptor<BundleDescriptor> module : modules) {
            // if this module is already added to the conflicted list
            // no need to process it again
            if (conflicted.contains(module)) {
                continue;
            }
            boolean foundConflictedModule = false;
            for (ModuleDescriptor<BundleDescriptor> module2 : modules) {
                // if this module is already added to the conflicted list
                // no need to process it again
                if (conflicted.contains(module2)) {
                    continue;
                }
                if (!module.equals(module2) && module.getModuleName().equals(module2.getModuleName())) {
                    conflicted.add(module2);
                    foundConflictedModule = true;
                }
            }
            if (foundConflictedModule) {
                conflicted.add(module);
            }
        }

        // append the conflicted module names with their module type to make the names unique
        for (ModuleDescriptor<BundleDescriptor> cModule : conflicted) {
            cModule.setModuleName(cModule.getModuleName() + cModule.getModuleType().toString());
        }
    }

//    FIXME by srini - add support in the new structure
    public void accept(EjbBundleDescriptor bundleDescriptor) {
        this.bundleDescriptor = bundleDescriptor;
        this.application = bundleDescriptor.getApplication();
        super.accept(bundleDescriptor);
        /**
         * set the realm name on each ejb to match the ones on this application
         * this is required right now to pass the stringent CSIv2 criteria
         * whereby the realm-name for the ejb being authenticated on
         * has to match the one on the application. We look at the IORConfigurator
         * descriptor
         *
         * @todo: change the csiv2 layer so that it does not look at
         *        IORConfiguratorDescriptor.
         * @see iiop/security/SecurityMechanismSelector.evaluateClientConformance.
         */
        String rlm = application.getRealm();
        if (rlm != null) {
            for (EjbDescriptor ejb : bundleDescriptor.getEjbs()) {
                for (EjbIORConfigurationDescriptor desc : ejb.getIORConfigurationDescriptors()) {
                    desc.setRealmName(rlm);
                }
            }
        }
    }

    @Override
    public void accept(ManagedBeanDescriptor managedBean) {
        this.bundleDescriptor = managedBean.getBundle();
        this.application = bundleDescriptor.getApplication();

        for (EjbReference element : managedBean.getEjbReferenceDescriptors()) {
            accept(element);
        }

        for (ResourceReferenceDescriptor element : managedBean.getResourceReferenceDescriptors()) {
            accept(element);
        }

        for (ResourceEnvReferenceDescriptor element : managedBean.getResourceEnvReferenceDescriptors()) {
            accept(element);
        }

        for (MessageDestinationReferencer element : managedBean.getMessageDestinationReferenceDescriptors()) {
            accept(element);
        }

        for (ServiceReferenceDescriptor serviceRef : managedBean.getServiceReferenceDescriptors()) {
            accept(serviceRef);
        }
    }

    @Override
    protected Collection<EjbDescriptor> getEjbDescriptors() {
        if (application != null) {
            return application.getEjbDescriptors();
        }
        return new HashSet<>();
    }


    @Override
    protected Application getApplication() {
        return application;
    }


    @Override
    public DescriptorVisitor getSubDescriptorVisitor(Descriptor subDescriptor) {
        if (subDescriptor instanceof BundleDescriptor) {
            return ((BundleDescriptor) subDescriptor).getBundleVisitor();
        }
        return super.getSubDescriptorVisitor(subDescriptor);
    }

    /**
     * Method to read complete application and all defined descriptor for given app. Method is used to identify
     * scope and validation for all defined jndi names at different namespace.
     *
     * @param application
     */
    public boolean validateResourceDescriptor(Application application) {
        /*
         * Below final String is the prefix which I am appending with each module
         * name to avoid duplicates. In ejblite case application name, ejb bundle
         * name and web bundle name always returns the same name if not specified.
         * So my validation fails so to avoid the same appending difference prefix
         * with each module name.
         * For two ejb-jar.xml in two different modules as part of the ear, they
         * must have unique module names (this is per spec requirement), so the
         * module scoped resources just needs to be unique within their modules. So
         * in that case all bundle name must be unique so appending extra string is
         * not fail anything.
         * It is used for internal reference only.
         */
        final String APP_LEVEL = "AppLevel:";
        final String EJBBUNDLE_LEVEL = "EBDLevel:";
        final String EJB_LEVEL = "EJBLevel:";
        final String APPCLIENTBUNDLE_LEVEL = "ACDevel:";
        final String APPCLIENT_LEVEL = "ACLevel:";
        final String WEBBUNDLE_LEVEL = "WBDLevel:";

        Set<EnvironmentProperty> environmentProperties = application == null
            ? null
            : application.getEnvironmentProperties();
        if (environmentProperties != null) {
            for (EnvironmentProperty environmentProperty : environmentProperties) {
                final SimpleJndiName jndiName;
                if (environmentProperty.hasLookupName()) {
                    jndiName = environmentProperty.getLookupName();
                } else if (!environmentProperty.getMappedName().isEmpty()) {
                    jndiName = environmentProperty.getMappedName();
                } else {
                    jndiName = SimpleJndiName.of(environmentProperty.getName());
                }

                if (jndiName.isJavaComponent() || jndiName.isJavaModule()) {
                    inValidJndiName = jndiName;
                    return false;
                }
            }
        }

        // Reads resource definition descriptor at application level
        CommonResourceBundleDescriptor commonResourceBundleDescriptor = application == null
            ? null
            : (CommonResourceBundleDescriptor) application;
        Vector<String> appLevel = new Vector<>();
        if (commonResourceBundleDescriptor != null) {
            Set<ResourceDescriptor> resourceDescriptors = commonResourceBundleDescriptor.getAllResourcesDescriptors();
            if (findExistingDescriptors(resourceDescriptors, APP_LEVEL + commonResourceBundleDescriptor.getName())) {
                return false;
            }
            appLevel.add(APP_LEVEL + commonResourceBundleDescriptor.getName());
            validNameSpaceDetails.put(APP_KEYS, appLevel);
        }

        // Reads resource definition descriptor at application-client level
        Set<ApplicationClientDescriptor> appClientDescs = application
            .getBundleDescriptors(ApplicationClientDescriptor.class);
        Vector<String> appClientLevel = new Vector<>();
        for (ApplicationClientDescriptor acd : appClientDescs) {
            Set<ResourceDescriptor> resourceDescriptors = acd
                .getAllResourcesDescriptors(ApplicationClientDescriptor.class);
            if (findExistingDescriptors(resourceDescriptors, APPCLIENTBUNDLE_LEVEL + acd.getName())) {
                return false;
            }
            appClientLevel.add(APPCLIENTBUNDLE_LEVEL + acd.getName());
        }
        validNameSpaceDetails.put(APPCLIENT_KEYS, appClientLevel);

        // Reads resource definition descriptor at connector level

        Set<ConnectorDescriptor> connectorDescs = application.getBundleDescriptors(ConnectorDescriptor.class);
        Vector<String> cdLevel = new Vector<>();
        for (ConnectorDescriptor cd : connectorDescs) {
            Set<ResourceDescriptor> resourceDescriptors = cd
                .getAllResourcesDescriptors(ApplicationClientDescriptor.class);
            if (findExistingDescriptors(resourceDescriptors, APPCLIENT_LEVEL + cd.getName())) {
                return false;
            }
            cdLevel.add(APPCLIENT_LEVEL + cd.getName());
        }
        validNameSpaceDetails.put(CONNECTOR_KEYS, cdLevel);

        // Reads resource definition descriptor at ejb-bundle level
        Set<EjbBundleDescriptor> ejbBundleDescs = application.getBundleDescriptors(EjbBundleDescriptor.class);
        Vector<String> ebdLevel = new Vector<>();
        Vector<String> edLevel = new Vector<>();
        for (EjbBundleDescriptor ebd : ejbBundleDescs) {
            Set<ResourceDescriptor> resourceDescriptors = ebd.getAllResourcesDescriptors();
            if (findExistingDescriptors(resourceDescriptors, EJBBUNDLE_LEVEL + ebd.getName())) {
                return false;
            }
            ebdLevel.add(EJBBUNDLE_LEVEL + ebd.getName());

            // Reads resource definition descriptor at ejb level
            Set<EjbDescriptor> ejbDescriptors = (Set<EjbDescriptor>) ebd.getEjbs();
            for (EjbDescriptor ejbDescriptor : ejbDescriptors) {
                resourceDescriptors = ejbDescriptor.getAllResourcesDescriptors();
                if (findExistingDescriptors(resourceDescriptors,
                    EJB_LEVEL + ebd.getName() + "#" + ejbDescriptor.getName())) {
                    return false;
                }
                edLevel.add(EJB_LEVEL + ebd.getName() + "#" + ejbDescriptor.getName());
            }
        }
        validNameSpaceDetails.put(EJBBUNDLE_KEYS, ebdLevel);
        validNameSpaceDetails.put(EJB_KEYS, edLevel);

        // Reads resource definition descriptor at web-bundle level

        Set<WebBundleDescriptor> webBundleDescs = application.getBundleDescriptors(WebBundleDescriptor.class);
        Vector<String> wbdLevel = new Vector<>();
        for (WebBundleDescriptor wbd : webBundleDescs) {
            Set<ResourceDescriptor> resourceDescriptors = wbd.getAllResourcesDescriptors();
            if (findExistingDescriptors(resourceDescriptors, WEBBUNDLE_LEVEL + wbd.getName())) {
                return false;
            }
            wbdLevel.add(WEBBUNDLE_LEVEL + wbd.getName());
        }
        validNameSpaceDetails.put(WEBBUNDLE_KEYS, wbdLevel);

        // process connector based resource, if it resource adapter name is of the
        // format "#raName"
        // then it will be deployed on an embedded resource adapter.
        // In this case, insert application name before the symbol "#".
        // Because in the ConnectorRegistry, embedded RA is indexed by
        // "appName#raName"

        for (CommonResourceValidator rv : allResourceDescriptors.values()) {
            Descriptor desc = rv.getDescriptor();
            if (desc instanceof AbstractConnectorResourceDescriptor) {
                AbstractConnectorResourceDescriptor acrd = (AbstractConnectorResourceDescriptor) desc;
                if (acrd.getResourceAdapter() == null) {
                    continue;
                }
                int poundIndex = acrd.getResourceAdapter().indexOf("#");
                if (poundIndex == 0) {
                    // the resource adapter name is of the format "#xxx", it is an
                    // embedded resource adapter
                    acrd.setResourceAdapter(application.getName() + acrd.getResourceAdapter());
                } else if (poundIndex < 0) {
                    // the resource adapter name do not contains # symbol, it is a
                    // standalone resource adapter
                } else {
                    // the resource adapter name is of the format "xx#xxx", this is an
                    // invalid name
                    LOG.log(Level.SEVERE, RESOURCE_ADAPTER_NAME_INVALID,
                        new Object[] {application.getAppName(), acrd.getName(), acrd.getResourceAdapter()});
                    return false;
                }
            }
        }

        // if all resources names are unique then validate each descriptor is unique
        // or not
        if (allUniqueResource) {
            return compareDescriptors();
        }

        return allUniqueResource;
    }


    private void validateEnvEntries(Application application) {
        EnvEntriesValidator envValidator = new EnvEntriesValidator();

        // validate env entries on resource definition descriptor at application level
        envValidator.validateEnvEntries(application);

        // validate env entries at application-client level
        Set<ApplicationClientDescriptor> appClientDescs = application
            .getBundleDescriptors(ApplicationClientDescriptor.class);
        for (ApplicationClientDescriptor acd : appClientDescs) {
            envValidator.validateEnvEntries(acd);
        }

        // validate env entries at ejb-bundle level
        Set<EjbBundleDescriptor> ejbBundleDescs = application.getBundleDescriptors(EjbBundleDescriptor.class);
        for (EjbBundleDescriptor ebd : ejbBundleDescs) {
            // Reads resource definition descriptor at ejb level
            Set<EjbDescriptor> ejbDescriptors = (Set<EjbDescriptor>) ebd.getEjbs();
            for (EjbDescriptor ejbDescriptor : ejbDescriptors) {
                envValidator.validateEnvEntries(ejbDescriptor);
            }
        }

        // validate env entries at web-bundle level
        Set<WebBundleDescriptor> webBundleDescs = application.getBundleDescriptors(WebBundleDescriptor.class);
        for (WebBundleDescriptor wbd : webBundleDescs) {
            envValidator.validateEnvEntries(wbd);
        }
    }


    /**
     * Method to validate MSD is unique or not
     */
    private boolean findExistingDescriptors(Set<ResourceDescriptor> descriptors, String scope) {
        for (ResourceDescriptor descriptor : descriptors) {
            if (isExistsDescriptor(descriptor.getJndiName(), descriptor, scope)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Method to compare existing descriptor with other descriptors. If both descriptor is equal then deployment
     * should be failed. scope is nothing but app level,connector level, ejb level etc., which is used later to
     * compare same jndi name is defined at different scope or not.
     */
    private boolean isExistsDescriptor(SimpleJndiName name, ResourceDescriptor descriptor, String scope) {
        if (descriptor == null) {
            return false;
        }
        CommonResourceValidator commonResourceValidator = allResourceDescriptors.get(name);
        if (commonResourceValidator == null) {
            Vector<String> vectorScope = new Vector<>();
            vectorScope.add(scope);
            allResourceDescriptors.put(name, new CommonResourceValidator(descriptor, name, vectorScope));
        } else {
            Descriptor existingDescriptor = commonResourceValidator.getDescriptor();
            if (descriptor instanceof MailSessionDescriptor && existingDescriptor instanceof MailSessionDescriptor) {
                if (descriptor.equals(existingDescriptor)) {
                    LOG.log(Level.SEVERE, DOLUtils.DUPLICATE_DESCRIPTOR, new Object[] {descriptor.getName()});
                } else {
                    allUniqueResource = false;
                    return true;
                }
            } else if (descriptor instanceof DataSourceDefinitionDescriptor
                && existingDescriptor instanceof DataSourceDefinitionDescriptor) {
                if (descriptor.equals(existingDescriptor)) {
                    LOG.log(Level.SEVERE, DOLUtils.DUPLICATE_DESCRIPTOR, new Object[] {descriptor.getName()});
                } else {
                    allUniqueResource = false;
                    return true;
                }
            } else if (descriptor instanceof ConnectionFactoryDefinitionDescriptor
                && existingDescriptor instanceof ConnectionFactoryDefinitionDescriptor) {
                if (descriptor.equals(existingDescriptor)) {
                    LOG.log(Level.SEVERE, DOLUtils.DUPLICATE_DESCRIPTOR, new Object[] {descriptor.getName()});
                } else {
                    allUniqueResource = false;
                    return true;
                }
            } else if (descriptor instanceof AdministeredObjectDefinitionDescriptor
                && existingDescriptor instanceof AdministeredObjectDefinitionDescriptor) {
                if (descriptor.equals(existingDescriptor)) {
                    LOG.log(Level.SEVERE, DOLUtils.DUPLICATE_DESCRIPTOR, new Object[] {descriptor.getName()});
                } else {
                    allUniqueResource = false;
                    return true;
                }
            }

            Vector<String> vectorScope = commonResourceValidator.getScope();
            if (vectorScope != null) {
                vectorScope.add(scope);
            }
            commonResourceValidator.setScope(vectorScope);
            allResourceDescriptors.put(name, commonResourceValidator);
        }
        return false;
    }

    /**
     * Compare descriptor at given scope is valid and unique.
     */
    private boolean compareDescriptors() {

        Vector<String> appVectorName = validNameSpaceDetails.get(APP_KEYS);
        Vector<String> ebdVectorName = validNameSpaceDetails.get(EJBBUNDLE_KEYS);
        for (Entry<SimpleJndiName, CommonResourceValidator> entry : allResourceDescriptors.entrySet()) {
            CommonResourceValidator commonResourceValidator = entry.getValue();
            Vector<String> scopeVector = commonResourceValidator.getScope();
            SimpleJndiName jndiName = commonResourceValidator.getJndiName();

            // FIXME: startsWith?
            if (jndiName.contains(JNDI_COMP)) {
                for (String scope : scopeVector) {
                    for (String element2 : appVectorName) {
                        if (scope.equals(element2)) {
                            inValidJndiName = jndiName;
                            LOG.log(Level.SEVERE, DOLUtils.INVALID_JNDI_SCOPE, jndiName);
                            return false;
                        }
                    }
                    for (String element2 : ebdVectorName) {
                        if (scope.equals(element2)) {
                            inValidJndiName = jndiName;
                            LOG.log(Level.SEVERE, DOLUtils.INVALID_JNDI_SCOPE, jndiName);
                            return false;
                        }
                    }
                }
            }

            if (jndiName.contains(JNDI_MODULE)) {
                for (String scope : scopeVector) {
                    for (String element2 : appVectorName) {
                        if (scope.equals(element2)) {
                            inValidJndiName = jndiName;
                            LOG.log(Level.SEVERE, DOLUtils.INVALID_JNDI_SCOPE, jndiName);
                            return false;
                        }
                    }
                }
            }

            if (scopeVector.size() > 1) {
                if (jndiName.contains(JNDI_COMP)) {
                    if (!compareVectorForComp(scopeVector, jndiName)) {
                        return false;
                    }
                } else if (jndiName.contains(JNDI_MODULE)) {
                    if (!compareVectorForModule(scopeVector, jndiName)) {
                        return false;
                    }
                } else if (jndiName.contains(JNDI_APP)) {
                    if (!compareVectorForApp(scopeVector, jndiName)) {
                        return false;
                    }
                } else {
                    try {
                        InitialContext ic = new InitialContext();
                        Object lookup = ic.lookup(jndiName.toString());
                        if (lookup != null) {
                            inValidJndiName = jndiName;
                            LOG.log(Level.SEVERE, DOLUtils.JNDI_LOOKUP_FAILED, jndiName);
                            return false;
                        }
                    } catch (NamingException e) {
                        /*
                         Do nothing, this is expected.
                         A failed lookup means there's no conflict with a resource defined on the server.
                        */
                    }
                }
            }
        }
        return true;
    }

    /**
     * Method to validate jndi name for app namespace
     */
    private boolean compareVectorForApp(Vector<String> myVector, SimpleJndiName jndiName) {
        for (int j = 0; j < myVector.size(); j++) {
            String firstElement = myVector.get(j);
            if (firstElement.contains("#")) {
                firstElement = firstElement.substring(0, firstElement.indexOf("#"));
            }
            for (int i = j + 1; i < myVector.size(); i++) {
                String otherElements = myVector.get(i);
                if (otherElements.contains("#")) {
                    otherElements = otherElements.substring(0, otherElements.indexOf("#"));
                }
                if (firstElement.equals(otherElements)) {
                    inValidJndiName = jndiName;
                    LOG.log(Level.SEVERE, DOLUtils.INVALID_NAMESPACE,
                        new Object[] {jndiName, application.getAppName()});
                }
            }
        }
        return true;
    }


    /**
     * Method to validate jndi name for module namespace
     */
    private boolean compareVectorForModule(Vector<String> myVector, SimpleJndiName jndiName) {
        if (!compareVectorForApp(myVector, jndiName)) {
            return false;
        }

        for (int j = 0; j < myVector.size(); j++) {
            String firstElement = myVector.firstElement();
            if (firstElement.contains("#")) {
                firstElement = firstElement.substring(firstElement.indexOf("#") + 1, firstElement.length());
            }
            if (firstElement.contains("#")) {
                firstElement = firstElement.substring(0, firstElement.indexOf("#"));
            }
            for (int i = j + 1; i < myVector.size(); i++) {
                String otherElements = myVector.get(i);
                if (otherElements.contains("#")) {
                    otherElements = otherElements.substring(otherElements.indexOf("#") + 1, otherElements.length());
                }
                if (otherElements.contains("#")) {
                    otherElements = otherElements.substring(0, otherElements.indexOf("#"));
                }
                if (firstElement.equals(otherElements)) {
                    inValidJndiName = jndiName;
                    LOG.log(Level.SEVERE, INVALID_NAMESPACE, new Object[] {jndiName, application.getAppName()});
                }
            }
        }
        return true;
    }


    /**
     * Method to validate jndi name for comp namespace
     */
    private boolean compareVectorForComp(Vector<String> myVector, SimpleJndiName jndiName) {
        if (!compareVectorForModule(myVector, jndiName)) {
            return false;
        }

        for (int j = 0; j < myVector.size(); j++) {
            String firstElement = myVector.firstElement();
            if (firstElement.contains("#")) {
                firstElement = firstElement.substring(firstElement.lastIndexOf("#") + 1, firstElement.length());
            }
            if (firstElement.contains("#")) {
                firstElement = firstElement.substring(firstElement.lastIndexOf("#") + 1, firstElement.length());
            }
            for (int i = j + 1; i < myVector.size(); i++) {
                String otherElements = myVector.get(i);
                if (otherElements.contains("#")) {
                    otherElements = otherElements.substring(otherElements.lastIndexOf("#") + 1, otherElements.length());
                }
                if (otherElements.contains("#")) {
                    otherElements = otherElements.substring(otherElements.lastIndexOf("#") + 1, otherElements.length());
                }
                if (firstElement.equals(otherElements)) {
                    inValidJndiName = jndiName;
                    LOG.log(Level.SEVERE, INVALID_NAMESPACE, new Object[] {jndiName, application.getAppName()});
                    return false;
                }
            }
        }
        return true;
    }
}
