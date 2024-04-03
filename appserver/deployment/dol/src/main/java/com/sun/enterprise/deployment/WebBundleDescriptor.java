/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.types.EjbReferenceContainer;
import com.sun.enterprise.deployment.types.MessageDestinationReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceEnvReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceReferenceContainer;
import com.sun.enterprise.deployment.types.ServiceReferenceContainer;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.web.AppListenerDescriptor;
import com.sun.enterprise.deployment.web.ContextParameter;
import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.deployment.web.MimeMapping;
import com.sun.enterprise.deployment.web.ResourceReference;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.ServletFilter;
import com.sun.enterprise.deployment.web.ServletFilterMapping;
import com.sun.enterprise.deployment.web.SessionConfig;
import com.sun.enterprise.deployment.web.WebResourceCollection;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.event.EventTypes;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.grizzly.http.util.MimeType;

/**
 * I am an object that represents all the deployment information about a web application.
 *
 * @author Danny Coward
 */
public abstract class WebBundleDescriptor extends CommonResourceBundleDescriptor
    implements WritableJndiNameEnvironment, ResourceReferenceContainer, ResourceEnvReferenceContainer,
    EjbReferenceContainer, MessageDestinationReferenceContainer, ServiceReferenceContainer {

    private static final long serialVersionUID = 5599255661969873669L;

    private static final String DEPLOYMENT_DESCRIPTOR_DIR = "WEB-INF";

    /** Used by the deployer and the web container */
    public static final EventTypes<WebBundleDescriptor> AFTER_SERVLET_CONTEXT_INITIALIZED_EVENT = EventTypes
        .create("After_Servlet_Context_Initialized", WebBundleDescriptor.class);

    private String contextRoot;
    private boolean denyUncoveredHttpMethods;
    private boolean distributable;
    private LocaleEncodingMappingListDescriptor localeEncodingMappingListDesc;
    private LoginConfiguration loginConfiguration;
    private String requestCharacterEncoding;
    private String responseCharacterEncoding;
    private SessionConfig sessionConfig;
    private boolean showArchivedRealPathEnabled = true;
    private JspConfigDefinitionDescriptor jspConfigDescriptor;

    private ClassLoader applicationClassLoader;

    /**
     * An entry here, may be set to indicate additional processing.
     * This entry may be set, for example, by a Deployer.
     */
    private final Map<String, String> extensionProperty = new HashMap<>(4);

    private final List<AppListenerDescriptor> appListenerDescriptors = new ArrayList<>();
    private final Set<ContextParameter> contextParameters = new OrderedSet<>();
    private final Set<EjbReferenceDescriptor> ejbReferences = new OrderedSet<>();
    private final Set<EntityManagerFactoryReferenceDescriptor> entityManagerFactoryReferences = new HashSet<>();
    private final Set<EntityManagerReferenceDescriptor> entityManagerReferences = new HashSet<>();
    private final Set<EnvironmentProperty> environmentEntries = new OrderedSet<>();
    private final Set<ErrorPageDescriptor> errorPageDescriptors = new HashSet<>();
    private final Map<String, String> jarName2WebFragNameMap = new HashMap<>();
    private final Set<MessageDestinationReferenceDescriptor> messageDestReferences = new OrderedSet<>();
    private final Set<MimeMapping> mimeMappings = new OrderedSet<>();
    private final List<String> orderedLibs = new ArrayList<>();
    private final Set<LifecycleCallbackDescriptor> postConstructDescs = new HashSet<>();
    private final Set<LifecycleCallbackDescriptor> preDestroyDescs = new HashSet<>();
    private final Set<ResourceEnvReferenceDescriptor> resourceEnvRefReferences = new OrderedSet<>();
    private final Set<ResourceReferenceDescriptor> resourceReferences = new OrderedSet<>();
    private final Set<SecurityConstraint> securityConstraints = new HashSet<>();
    private final Set<ServiceReferenceDescriptor> serviceReferences = new OrderedSet<>();
    private final List<ServletFilter> servletFilters = new ArrayList<>();
    private final List<ServletFilterMapping> servletFilterMappings = new ArrayList<>();
    private final Set<WebComponentDescriptor> webComponentDescriptors = new OrderedSet<>();
    private final Set<String> welcomeFiles = new OrderedSet<>();


    /** this is for checking whether there are more than one servlets for a given url-pattern */
    private Map<String, String> urlPattern2ServletName;


    /**
     * Add JNDI entries of the provided {@link JndiNameEnvironment} to this descriptor.
     * The implementation decides what entries are relevant and how to process them.
     * <p>
     * This method can be used ie by an EJB descriptor.
     *
     * @param env
     */
    public abstract void addJndiNameEnvironment(JndiNameEnvironment env);

    /**
     * Combine all except welcome file set for two webBundleDescriptors.
     *
     * @param descriptor full default descriptor or a fragment
     * @param descriptorFragment true if the descriptor is just a fragment
     */
    protected abstract void addCommonWebBundleDescriptor(WebBundleDescriptor descriptor, boolean descriptorFragment);


    /**
     * Merge the contents of this and given descriptor.
     *
     * @param webBundleDescriptor
     */
    public void addWebBundleDescriptor(WebBundleDescriptor webBundleDescriptor) {
        this.welcomeFiles.addAll(webBundleDescriptor.getWelcomeFiles());
        addCommonWebBundleDescriptor(webBundleDescriptor, true);
    }


    /**
     * Merge the contents of this and given descriptor.
     *
     * @param descriptor
     */
    public void addDefaultWebBundleDescriptor(WebBundleDescriptor descriptor) {
        if (getWelcomeFiles().isEmpty()) {
            this.welcomeFiles.addAll(descriptor.getWelcomeFiles());
        }
        if (this.requestCharacterEncoding == null) {
            this.requestCharacterEncoding = descriptor.getRequestCharacterEncoding();
        }
        if (this.responseCharacterEncoding == null) {
            this.responseCharacterEncoding = descriptor.getResponseCharacterEncoding();
        }
        addCommonWebBundleDescriptor(descriptor, false);
    }


    @Override
    public String getDeploymentDescriptorDir() {
        return DEPLOYMENT_DESCRIPTOR_DIR;
    }


    @Override
    public final ArchiveType getModuleType() {
        return DOLUtils.warType();
    }


    @Override
    public boolean isEmpty() {
        return webComponentDescriptors == null || webComponentDescriptors.isEmpty();
    }


    /**
     * @return relative context of the web application
     */
    public String getContextRoot() {
        if (getModuleDescriptor() != null && getModuleDescriptor().getContextRoot() != null) {
            return getModuleDescriptor().getContextRoot();
        }
        if (contextRoot == null) {
            contextRoot = "";
        }
        return contextRoot;
    }

    /**
     * @param contextRoot relative context of the web application
     */
    public void setContextRoot(String contextRoot) {
        if (getModuleDescriptor() != null) {
            getModuleDescriptor().setContextRoot(contextRoot);
        }
        this.contextRoot = contextRoot;
    }


    /**
     * @return true to deny access to methods not covered by authorization rules.
     */
    public boolean isDenyUncoveredHttpMethods() {
        return denyUncoveredHttpMethods;
    }


    /**
     * @param denyUncoveredHttpMethods true to deny access to methods not covered by authorization rules.
     */
    public void setDenyUncoveredHttpMethods(boolean denyUncoveredHttpMethods) {
        this.denyUncoveredHttpMethods = denyUncoveredHttpMethods;
    }


    /**
     * @return true if this web app [{0}] can be distributed across different processes.
     */
    public boolean isDistributable() {
        return distributable;
    }


    /**
     * Sets whether this web app [{0}] can be distributed across different processes.
     *
     * @param distributable true if allowed
     */
    public void setDistributable(boolean distributable) {
        this.distributable = distributable;
    }


    /**
     * @return Mapping of locales and encoding charsets
     */
    public LocaleEncodingMappingListDescriptor getLocaleEncodingMappingListDescriptor() {
        return localeEncodingMappingListDesc;
    }


    /**
     * @param lemListDesc Mapping of locales and encoding charsets
     */
    public void setLocaleEncodingMappingListDescriptor(LocaleEncodingMappingListDescriptor lemListDesc) {
        localeEncodingMappingListDesc = lemListDesc;
    }


    /**
     * @param lemDesc Mapping of locale and encoding charset
     */
    public void addLocaleEncodingMappingDescriptor(LocaleEncodingMappingDescriptor lemDesc) {
        if (localeEncodingMappingListDesc == null) {
            localeEncodingMappingListDesc = new LocaleEncodingMappingListDescriptor();
        }
        localeEncodingMappingListDesc.addLocaleEncodingMapping(lemDesc);
    }


    /**
     * @return {@link LoginConfiguration}
     */
    public LoginConfiguration getLoginConfiguration() {
        return loginConfiguration;
    }


    /**
     * @param loginConfiguration Specifies the information about authentication.
     */
    public void setLoginConfiguration(LoginConfiguration loginConfiguration) {
        this.loginConfiguration = loginConfiguration;
    }


    /**
     * @return the request encoding (charset)
     */
    public String getRequestCharacterEncoding() {
        return this.requestCharacterEncoding;
    }


    /**
     * @param requestCharacterEncoding request encoding (charset)
     */
    public void setRequestCharacterEncoding(String requestCharacterEncoding) {
        this.requestCharacterEncoding = requestCharacterEncoding;
    }


    /**
     * @return the response encoding (charset)
     */
    public String getResponseCharacterEncoding() {
        return this.responseCharacterEncoding;
    }


    /**
     * @param responseCharacterEncoding response encoding (charset)
     */
    public void setResponseCharacterEncoding(String responseCharacterEncoding) {
        this.responseCharacterEncoding = responseCharacterEncoding;
    }


    /**
     * @return configuration related to sessions
     */
    public SessionConfig getSessionConfig() {
        return sessionConfig;
    }


    /**
     * @param sessionConfig configuration related to sessions
     */
    public void setSessionConfig(SessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
    }


    /**
     * @return false to return null instead of real paths in cookies etc. Default is true.
     */
    public boolean isShowArchivedRealPathEnabled() {
        return showArchivedRealPathEnabled;
    }


    /**
     * @param enabled false to return null instead of real paths in cookies etc. Default is true.
     */
    public void setShowArchivedRealPathEnabled(boolean enabled) {
        showArchivedRealPathEnabled = enabled;
    }


    /**
     * @param descriptor JSP related configuration
     */
    public void addJspDescriptor(JspConfigDefinitionDescriptor descriptor) {
        jspConfigDescriptor.add(descriptor);
    }


    /**
     * @return {@link Set} of jsps.
     */
    public Set<WebComponentDescriptor> getJspDescriptors() {
        Set<WebComponentDescriptor> jspDescriptors = new HashSet<>();
        for (WebComponentDescriptor webComponent : getWebComponentDescriptors()) {
            if (!webComponent.isServlet()) {
                jspDescriptors.add(webComponent);
            }
        }
        return jspDescriptors;
    }


    /**
     * @return JSP related configuration
     */
    public JspConfigDefinitionDescriptor getJspConfigDescriptor() {
        return jspConfigDescriptor;
    }


    /**
     * @param descriptor JSP related configuration
     */
    public void setJspConfigDescriptor(JspConfigDefinitionDescriptor descriptor) {
        jspConfigDescriptor = descriptor;
    }

    /**
     * @return the applicationClassLoader
     */
    public ClassLoader getApplicationClassLoader() {
        return applicationClassLoader;
    }

    /**
     * @param applicationClassLoader the applicationClassLoader to set
     */
    public void setApplicationClassLoader(ClassLoader applicationClassLoader) {
        this.applicationClassLoader = applicationClassLoader;
    }

    /**
     * This property can be used to indicate a special processing to an extension.
     * For example, a Deployer may set this property.
     *
     * @param key non-null key, see extension's documentation.
     * @param value
     */
    public void setExtensionProperty(String key, String value) {
        extensionProperty.put(key, value);
    }


    /**
     * Determine if an extension property has been set. Case sensitive.
     *
     * @param key can be null, but then returns false.
     * @return true if the key is present.
     */
    public boolean hasExtensionProperty(String key) {
        return extensionProperty != null && extensionProperty.containsKey(key);
    }


    /**
     * @return unmodifiable copy of the list of {@link AppListenerDescriptor}s to use
     *         {@link ServletContextListener}s
     */
    public List<AppListenerDescriptor> getAppListenersCopy() {
        return List.copyOf(getAppListeners());
    }


    /**
     * @return list of {@link AppListenerDescriptor}s to use {@link ServletContextListener}s
     */
    public List<AppListenerDescriptor> getAppListeners() {
        return appListenerDescriptors;
    }


    /**
     * Clears the current list and adds all from the provided parameter.
     *
     * @param listeners list of {@link AppListenerDescriptor}s to use {@link ServletContextListener}s
     */
    public void setAppListeners(Collection<? extends AppListenerDescriptor> listeners) {
        this.appListenerDescriptors.clear();
        addAppListeners(listeners);
    }


    /**
     * @param listeners list of {@link AppListenerDescriptor}s to use {@link ServletContextListener}s
     */
    protected void addAppListeners(Collection<? extends AppListenerDescriptor> listeners) {
        this.appListenerDescriptors.addAll(listeners);
    }


    /**
     * Adds the listener as the last one if it is not already present.
     *
     * @param descriptor descriptor for a {@link ServletContextListener}.
     */
    public void addAppListenerDescriptor(AppListenerDescriptor descriptor) {
        if (!this.appListenerDescriptors.contains(descriptor)) {
            this.appListenerDescriptors.add(descriptor);
        }
    }


    /**
     * Adds the listener as the first one if it is not already present.
     *
     * @param descriptor descriptor for a {@link ServletContextListener}.
     */
    public void addAppListenerDescriptorToFirst(AppListenerDescriptor descriptor) {
        if (!this.appListenerDescriptors.contains(descriptor)) {
            this.appListenerDescriptors.add(0, descriptor);
        }
    }


    /**
     * @return the Set of my Context Parameters.
     */
    public Set<ContextParameter> getContextParameters() {
        return contextParameters;
    }


    /**
     * Adds all context parameters to my list.
     *
     * @param parameters
     */
    protected void addContextParameters(Collection<ContextParameter> parameters) {
        this.contextParameters.addAll(parameters);
    }


    /**
     * Adds a new context parameter to my list.
     *
     * @param contextParameter
     */
    public void addContextParameter(ContextParameter contextParameter) {
        contextParameters.add(contextParameter);
    }


    /**
     * Removes the given context parameter from my list.
     * Equals is used for the comparison.
     *
     * @param contextParameter
     */
    public void removeContextParameter(ContextParameter contextParameter) {
        contextParameters.remove(contextParameter);
    }


    @Override
    public Set<EjbReferenceDescriptor> getEjbReferenceDescriptors() {
        return ejbReferences;
    }


    @Override
    public EjbReferenceDescriptor getEjbReference(String name) {
        EjbReferenceDescriptor er = findEjbReference(name);
        if (er != null) {
            return er;
        }
        throw new IllegalArgumentException(
            MessageFormat.format("This web app [{0}] has no ejb reference by the name of [{1}] ", getName(), name));
    }


    /**
     * @param name
     * @return null or {@link EjbReferenceDescriptor} found by the name. Case sensitive.
     */
    protected EjbReferenceDescriptor findEjbReference(String name) {
        for (EjbReferenceDescriptor er : ejbReferences) {
            if (er.getName().equals(name)) {
                return er;
            }
        }
        return null;
    }


    @Override
    public void addEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
        if (ejbReferences.add(ejbReference)) {
            ejbReference.setReferringBundleDescriptor(this);
        }
    }


    @Override
    public void removeEjbReferenceDescriptor(EjbReferenceDescriptor ejbReferenceDescriptor) {
        if (ejbReferences.remove(ejbReferenceDescriptor)) {
            ejbReferenceDescriptor.setReferringBundleDescriptor(null);
        }
    }


    @Override
    public Set<EntityManagerFactoryReferenceDescriptor> getEntityManagerFactoryReferenceDescriptors() {
        return entityManagerFactoryReferences;
    }


    @Override
    public EntityManagerFactoryReferenceDescriptor getEntityManagerFactoryReferenceByName(String name) {
        EntityManagerFactoryReferenceDescriptor emfr = findEntityManagerFactoryReferenceByName(name);
        if (emfr != null) {
            return emfr;
        }
        throw new IllegalArgumentException(MessageFormat.format(
            "This web app [{0}] has no entity manager factory reference by the name of [{1}]", getName(), name));
    }


    /**
     * Case sensitive search.
     *
     * @param name
     * @return null or {@link EntityManagerFactoryReferenceDescriptor} found by the name.
     */
    protected EntityManagerFactoryReferenceDescriptor findEntityManagerFactoryReferenceByName(String name) {
        for (EntityManagerFactoryReferenceDescriptor next : entityManagerFactoryReferences) {
            if (next.getName().equals(name)) {
                return next;
            }
        }
        return null;
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


    @Override
    public EntityManagerReferenceDescriptor getEntityManagerReferenceByName(String name) {
        EntityManagerReferenceDescriptor emr = findEntityManagerReferenceByName(name);
        if (emr != null) {
            return emr;
        }
        throw new IllegalArgumentException(MessageFormat
            .format("This web app [{0}] has no entity manager reference by the name of [{1}]", getName(), name));
    }


    /**
     * Case sensitive search.
     *
     * @param name
     * @return null or {@link EntityManagerReferenceDescriptor} found by the name.
     */
    protected EntityManagerReferenceDescriptor findEntityManagerReferenceByName(String name) {
        for (EntityManagerReferenceDescriptor next : entityManagerReferences) {
            if (next.getName().equals(name)) {
                return next;
            }
        }
        return null;
    }


    @Override
    public void addEntityManagerReferenceDescriptor(EntityManagerReferenceDescriptor reference) {
        reference.setReferringBundleDescriptor(this);
        entityManagerReferences.add(reference);
    }


    @Override
    public Set<EnvironmentProperty> getEnvironmentProperties() {
        return getEnvironmentEntries();
    }


    /**
     * @return {@link Set} of {@link EnvironmentProperty}, never null but may be empty.
     */
    public Set<EnvironmentProperty> getEnvironmentEntries() {
        return environmentEntries;
    }


    @Override
    public EnvironmentProperty getEnvironmentPropertyByName(String name) {
        EnvironmentProperty entry = findEnvironmentEntryByName(name);
        if (entry != null) {
            return entry;
        }
        throw new IllegalArgumentException(MessageFormat
            .format("This web app [{0}] has no environment property by the name of [{1}]", getName(), name));
    }


    /**
     * Case sensitive search.
     *
     * @param name
     * @return null or {@link EnvironmentProperty} found by the name.
     */
    protected EnvironmentProperty findEnvironmentEntryByName(String name) {
        for (EnvironmentProperty ev : environmentEntries) {
            if (ev.getName().equals(name)) {
                return ev;
            }
        }
        return null;
    }


    @Override
    public void addEnvironmentProperty(EnvironmentProperty environmentProperty) {
        addEnvironmentEntry(environmentProperty);
    }


    /**
     * Adds this given environment property to my list.
     *
     * @param environmentEntry
     */
    public void addEnvironmentEntry(EnvironmentProperty environmentEntry) {
        environmentEntries.add(environmentEntry);
    }


    @Override
    public void removeEnvironmentProperty(EnvironmentProperty environmentProperty) {
        removeEnvironmentEntry(environmentProperty);
    }


    /**
     * Removes this given environment property from my list.
     *
     * @param environmentEntry
     */
    public void removeEnvironmentEntry(EnvironmentProperty environmentEntry) {
        environmentEntries.remove(environmentEntry);
    }


    /**
     * @return {@link Set} of {@link ErrorPageDescriptor}, never null but may be empty.
     */
    public Set<ErrorPageDescriptor> getErrorPageDescriptors() {
        return errorPageDescriptors;
    }


    /**
     * Adds a new error page to my list.
     *
     * @param errorPageDescriptor
     */
    public void addErrorPageDescriptor(ErrorPageDescriptor errorPageDescriptor) {
        String errorSignifier = errorPageDescriptor.getErrorSignifierAsString();
        ErrorPageDescriptor errPageDesc = getErrorPageDescriptorBySignifier(errorSignifier);
        if (errPageDesc == null) {
            errorPageDescriptors.add(errorPageDescriptor);
        }
    }


    /**
     * Search my error pages for one with thei given signifier or null if there isn't one.
     * Case sensitive.
     *
     * @return {@link ErrorPageDescriptor} or null
     */
    private ErrorPageDescriptor getErrorPageDescriptorBySignifier(String signifier) {
        for (ErrorPageDescriptor errorPage : errorPageDescriptors) {
            if (errorPage.getErrorSignifierAsString().equals(signifier)) {
                return errorPage;
            }
        }
        return null;
    }


    /**
     * Removes the given error page from my list.
     * Equals is used for the comparison.
     *
     * @param errorPageDescriptor
     */
    public void removeErrorPageDescriptor(ErrorPageDescriptor errorPageDescriptor) {
        errorPageDescriptors.remove(errorPageDescriptor);
    }


    /**
     * This method return an unmodifiable map of a jar name mappings to a web.xml fragment names.
     *
     * @return unmodifiable {@link Map}
     */
    public Map<String, String> getJarNameToWebFragmentNameMap() {
        return Collections.unmodifiableMap(jarName2WebFragNameMap);
    }


    /**
     * Registers a jar name mapping to a web.xml fragment's name
     *
     * @param jarName
     * @param webFragmentName
     */
    public void putJarNameWebFragmentNamePair(String jarName, String webFragmentName) {
        jarName2WebFragNameMap.put(jarName, webFragmentName);
    }


    @Override
    public Set<MessageDestinationReferenceDescriptor> getMessageDestinationReferenceDescriptors() {
        return messageDestReferences;
    }


    @Override
    public MessageDestinationReferenceDescriptor getMessageDestinationReferenceByName(String name) {
        MessageDestinationReferenceDescriptor mdr = findMessageDestinationReferenceByName(name);
        if (mdr != null) {
            return mdr;
        }
        throw new IllegalArgumentException(MessageFormat
            .format("This web app [{0}] has no message destination reference by the name of [{1}]", getName(), name));
    }


    /**
     * Case sensitive search.
     *
     * @param name
     * @return null or {@link MessageDestinationReferenceDescriptor} found by the name.
     */
    protected MessageDestinationReferenceDescriptor findMessageDestinationReferenceByName(String name) {
        for (MessageDestinationReferenceDescriptor mdr : messageDestReferences) {
            if (mdr.getName().equals(name)) {
                return mdr;
            }
        }
        return null;
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
     * @return a set of mime mappings.
     */
    public Set<MimeMapping> getMimeMappings() {
        return mimeMappings;
    }


    /**
     * Add the given mime mapping to my list if the given MimeType is not added
     *
     * @param mimeMapping
     * @return the result {@link MimeType} of the {@link MimeMapping} in the resulting
     *         set of MimeMapping
     */
    public String addMimeMapping(MimeMapping mimeMapping) {
        // there should be at most one mapping per extension
        MimeMapping resultMimeMapping = null;
        for (MimeMapping mm : mimeMappings) {
            if (mm.getExtension().equals(mimeMapping.getExtension())) {
                resultMimeMapping = mm;
                break;
            }
        }
        if (resultMimeMapping == null) {
            resultMimeMapping = mimeMapping;
            this.mimeMappings.add(mimeMapping);
        }
        return resultMimeMapping.getMimeType();
    }


    /**
     * Removes the given mime mapping from my list. Uses equals for comparison.
     *
     * @param mimeMapping
     */
    public void removeMimeMapping(MimeMapping mimeMapping) {
        mimeMappings.remove(mimeMapping);
    }

    /**
     * @return the list for the {@link ServletContext#ORDERED_LIBS}
     */
    public List<String> getOrderedLibs() {
        return orderedLibs;
    }


    /**
     * @param libName an item for the {@link ServletContext#ORDERED_LIBS}
     */
    public void addOrderedLib(String libName) {
        orderedLibs.add(libName);
    }


    @Override
    public Set<LifecycleCallbackDescriptor> getPostConstructDescriptors() {
        return postConstructDescs;
    }


    @Override
    public LifecycleCallbackDescriptor getPostConstructDescriptorByClass(String className) {
        return getPostConstructDescriptorByClass(className, this);
    }


    @Override
    public void addPostConstructDescriptor(LifecycleCallbackDescriptor postConstructDesc) {
        String className = postConstructDesc.getLifecycleCallbackClass();
        boolean found = false;
        for (LifecycleCallbackDescriptor next : postConstructDescs) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                found = true;
                break;
            }
        }
        if (!found) {
            postConstructDescs.add(postConstructDesc);
        }
    }


    @Override
    public Set<LifecycleCallbackDescriptor> getPreDestroyDescriptors() {
        return preDestroyDescs;
    }


    @Override
    public LifecycleCallbackDescriptor getPreDestroyDescriptorByClass(String className) {
        return getPreDestroyDescriptorByClass(className, this);
    }


    @Override
    public void addPreDestroyDescriptor(LifecycleCallbackDescriptor preDestroyDesc) {
        String className = preDestroyDesc.getLifecycleCallbackClass();
        boolean found = false;
        for (LifecycleCallbackDescriptor next : preDestroyDescs) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                found = true;
                break;
            }
        }
        if (!found) {
            preDestroyDescs.add(preDestroyDesc);
        }
    }


    @Override
    public Set<ResourceEnvReferenceDescriptor> getResourceEnvReferenceDescriptors() {
        return resourceEnvRefReferences;
    }


    @Override
    public ResourceEnvReferenceDescriptor getResourceEnvReferenceByName(String name) {
        ResourceEnvReferenceDescriptor jrd = findResourceEnvReferenceByName(name);
        if (jrd != null) {
            return jrd;
        }
        throw new IllegalArgumentException(MessageFormat
            .format("This web app [{0}] has no resource environment reference by the name of [{1}]", getName(), name));
    }


    /**
     * Case sensitive search.
     *
     * @param name
     * @return null or {@link ResourceEnvReferenceDescriptor} found by the name.
     */
    protected ResourceEnvReferenceDescriptor findResourceEnvReferenceByName(String name) {
        for (ResourceEnvReferenceDescriptor jdr : resourceEnvRefReferences) {
            if (jdr.getName().equals(name)) {
                return jdr;
            }
        }
        return null;
    }


    @Override
    public void addResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvRefReference) {
        resourceEnvRefReferences.add(resourceEnvRefReference);
    }


    @Override
    public void removeResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvRefReference) {
        resourceEnvRefReferences.remove(resourceEnvRefReference);
    }


    @Override
    public Set<ResourceReferenceDescriptor> getResourceReferenceDescriptors() {
        return resourceReferences;
    }


    @Override
    public ResourceReferenceDescriptor getResourceReferenceByName(String name) {
        ResourceReferenceDescriptor rrd = findResourceReferenceByName(name);
        if (rrd != null) {
            return rrd;
        }
        throw new IllegalArgumentException(
            MessageFormat.format("This web app [{0}] has no resource reference by the name of [{1}]", getName(), name));
    }


    /**
     * Case sensitive search.
     *
     * @param name
     * @return null or {@link ResourceReferenceDescriptor} found by the name.
     */
    protected ResourceReferenceDescriptor findResourceReferenceByName(String name) {
        for (ResourceReference next : resourceReferences) {
            if (next.getName().equals(name)) {
                return (ResourceReferenceDescriptor) next;
            }
        }
        return null;
    }


    @Override
    public void addResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
        resourceReferences.add(resourceReference);
    }


    @Override
    public void removeResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
        resourceReferences.remove(resourceReference);
    }


    /**
     * @return set of {@link SecurityConstraint}. Never null.
     */
    public Set<SecurityConstraint> getSecurityConstraints() {
        return securityConstraints;
    }


    /**
     * @param urlPattern
     * @return collection of all security constraints with the given url pattern.
     */
    public Collection<SecurityConstraint> getSecurityConstraintsForUrlPattern(String urlPattern) {
        if (urlPattern == null) {
            return Collections.emptySet();
        }
        Set<SecurityConstraint> constraints = new HashSet<>();
        for (SecurityConstraint constraint : securityConstraints) {
            boolean include = false;
            for (WebResourceCollection wsCollection : constraint.getWebResourceCollections()) {
                for (String wsCollectionUrlPattern : wsCollection.getUrlPatterns()) {
                    if (urlPattern.equals(wsCollectionUrlPattern)) {
                        include = true;
                        break;
                    }
                }
                if (include) {
                    break;
                }
            }
            if (include) {
                constraints.add(constraint);
            }
        }
        return constraints;
    }


    /**
     * Add a new security constraint.
     *
     * @param securityConstraint
     */
    public void addSecurityConstraint(SecurityConstraint securityConstraint) {
        securityConstraints.add(securityConstraint);
    }


    /**
     * Remove the given security constraint.
     *
     * @param securityConstraint
     */
    public void removeSecurityConstraint(SecurityConstraint securityConstraint) {
        securityConstraints.remove(securityConstraint);
    }


    /**
     * @return true if this bundle descriptor defines web service clients
     */
    @Override
    public boolean hasWebServiceClients() {
        return !serviceReferences.isEmpty();
    }


    /**
     * @return true if there are some
     */
    public boolean hasServiceReferenceDescriptors() {
        return !serviceReferences.isEmpty();
    }


    @Override
    public Set<ServiceReferenceDescriptor> getServiceReferenceDescriptors() {
        return serviceReferences;
    }


    @Override
    public ServiceReferenceDescriptor getServiceReferenceByName(String name) {
        ServiceReferenceDescriptor sr = findServiceReferenceByName(name);
        if (sr != null) {
            return sr;
        }
        throw new IllegalArgumentException(
            MessageFormat.format("This web app [{0}] has no service reference by the name of [{1}]", getName(), name));
    }


    /**
     * Case sensitive search.
     *
     * @param name
     * @return null or {@link ServiceReferenceDescriptor} found by the name.
     */
    protected ServiceReferenceDescriptor findServiceReferenceByName(String name) {
        for (ServiceReferenceDescriptor srd : serviceReferences) {
            if (srd.getName().equals(name)) {
                return srd;
            }
        }
        return null;
    }


    @Override
    public void addServiceReferenceDescriptor(ServiceReferenceDescriptor serviceRef) {
        serviceRef.setBundleDescriptor(this);
        serviceReferences.add(serviceRef);
    }


    @Override
    public void removeServiceReferenceDescriptor(ServiceReferenceDescriptor serviceRef) {
        serviceRef.setBundleDescriptor(null);
        serviceReferences.remove(serviceRef);
    }


    /**
     * @return a new list of servlet filters that I have.
     */
    public List<ServletFilter> getServletFiltersCopy() {
        return List.copyOf(getServletFilters());
    }


    /**
     * @return a list of servlet filters that I have.
     */
    public List<ServletFilter> getServletFilters() {
        return servletFilters;
    }


    /**
     * Adds a servlet filter to this web component.
     * If there already is a filter of the same name, the new filter will be ignored.
     *
     * @param filter
     */
    public void addServletFilter(ServletFilter filter) {
        String name = filter.getName();
        boolean found = false;
        for (ServletFilter servletFilter : servletFilters) {
            if (name.equals(servletFilter.getName())) {
                found = true;
                break;
            }
        }
        if (!found) {
            servletFilters.add(filter);
        }
    }


    /**
     * @return a list of servlet filters that I have.
     */
    public List<ServletFilterMapping> getServletFilterMappings() {
        return servletFilterMappings;
    }


    /**
     * Adds a servlet filter mapping to this web component.
     * If there already is an equal filter, the new one will be ignored.
     *
     * @param filter
     */
    public void addServletFilterMapping(ServletFilterMapping filter) {
        if (!servletFilterMappings.contains(filter)) {
            servletFilterMappings.add(filter);
        }
    }


    /**
     * @return the Set of Web COmponent Descriptors (JSP or JavaServlets) in me.
     */
    public Set<WebComponentDescriptor> getWebComponentDescriptors() {
        return webComponentDescriptors;
    }


    /**
     * @param canonicalName
     * @return {@link WebComponentDescriptor} by canonical name or null
     */
    public WebComponentDescriptor getWebComponentByCanonicalName(String canonicalName) {
        for (WebComponentDescriptor descriptor : webComponentDescriptors) {
            if (descriptor.getCanonicalName().equals(canonicalName)) {
                return descriptor;
            }
        }
        return null;
    }


    /**
     * @param webComponentImplementation
     * @return {@link WebComponentDescriptor} by web component implementation, never null.
     */
    public WebComponentDescriptor[] getWebComponentByImplName(String webComponentImplementation) {
        ArrayList<WebComponentDescriptor> webCompList = new ArrayList<>();
        for (WebComponentDescriptor webComp : webComponentDescriptors) {
            if (webComp.getWebComponentImplementation().equals(webComponentImplementation)) {
                webCompList.add(webComp);
            }
        }
        return webCompList.toArray(new WebComponentDescriptor[webCompList.size()]);
    }


    /**
     * Adds a new Web Component Descriptor to me.
     *
     * @param webComponentDescriptor
     */
    public void addWebComponentDescriptor(WebComponentDescriptor webComponentDescriptor) {
        webComponentDescriptors.add(webComponentDescriptor);
    }


    /**
     * Forgets the URL pattern to servlet mapping.
     */
    public void resetUrlPatternToServletNameMap() {
        urlPattern2ServletName = null;
    }


    /**
     * The returned map is supposed to be only modified by the corresponding url patterns set.
     *
     * @param initializeIfNull
     * @return the internal urlPattern2ServletName map
     */
    public Map<String, String> getUrlPatternToServletNameMap(boolean initializeIfNull) {
        if (urlPattern2ServletName == null && initializeIfNull) {
            urlPattern2ServletName = new HashMap<>();
            for (WebComponentDescriptor wc : webComponentDescriptors) {
                String name = wc.getCanonicalName();
                for (String up : wc.getUrlPatternsSet()) {
                    String oldName = urlPattern2ServletName.put(up, name);
                    if (oldName != null && !oldName.equals(name)) {
                        throw new IllegalStateException(MessageFormat.format(
                            "Servlet [{0}] and Servlet [{1}] have the same url pattern: [{2}]", oldName, name, up));
                    }
                }
            }
        }
        return urlPattern2ServletName;
    }


    /**
     * @return set of the welcome files I have..
     */
    public Set<String> getWelcomeFiles() {
        return welcomeFiles;
    }


    /**
     * Adds a new welcome file to my list.
     *
     * @param fileUri
     */
    public void addWelcomeFile(String fileUri) {
        welcomeFiles.add(fileUri);
    }


    /**
     * This returns the extra web sun specific info not in the spec.
     *
     * @return {@link SunWebApp} or null
     */
    public SunWebApp getSunDescriptor() {
        return null;
    }


    /**
     * This sets the extra web sun specific info not in the spec.
     *
     * @param webApp SunWebApp object representation of web deployment descriptor
     */
    public void setSunDescriptor(SunWebApp webApp) {
        throw new UnsupportedOperationException("setSunDescriptor");
    }


    @Override
    public List<InjectionCapable> getInjectableResourcesByClass(String className) {
        return getInjectableResourcesByClass(className, this);
    }


    @Override
    public List<InjectionCapable> getInjectableResourcesByClass(String className, JndiNameEnvironment jndiNameEnv) {
        List<InjectionCapable> injectables = new LinkedList<>();
        for (InjectionCapable next : getInjectableResources(jndiNameEnv)) {
            if (!next.isInjectable()) {
                continue;
            }
            for (InjectionTarget target : next.getInjectionTargets()) {
                if (target.getClassName().equals(className)) {
                    injectables.add(next);
                }
            }
        }

        if (((WebBundleDescriptor) jndiNameEnv).hasWebServices()) {
            // Add @Resource WebServiceContext present in endpoint impl class to the list of
            // injectable resources; We do this for servelt endpoint only because the actual
            // endpoint impl class gets replaced by JAXWSServlet in web.xml and hence
            // will never be added as an injectable resource
            for (InjectionCapable next : getInjectableResources(this)) {
                if (!next.isInjectable()) {
                    continue;
                }
                for (InjectionTarget target : next.getInjectionTargets()) {
                    for (WebServiceEndpoint endpoint : getWebServices().getEndpoints()) {
                        String servletImplClass = endpoint.getServletImplClass();
                        if (target.getClassName().equals(servletImplClass)) {
                            injectables.add(next);
                        }
                    }
                }
            }
        }
        return injectables;
    }


    @Override
    public InjectionInfo getInjectionInfoByClass(Class<?> clazz) {
        return getInjectionInfoByClass(clazz, this);
    }


    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append('\n').append(getClass().getSimpleName()).append('\n');
        super.print(toStringBuffer);
        toStringBuffer.append("\n context root ").append(getContextRoot());
        if (getSessionConfig() != null) {
            toStringBuffer.append(getSessionConfig());
        }
        String wname = getName();
        if (wname != null && !wname.isEmpty()) {
            toStringBuffer.append("\n name ").append(wname);
        }
        toStringBuffer.append("\n mimeMappings ").append(getMimeMappings());
        toStringBuffer.append("\n welcomeFiles ").append(getWelcomeFiles());
        toStringBuffer.append("\n errorPageDescriptors ").append(errorPageDescriptors);
        toStringBuffer.append("\n appListenerDescriptors ").append(getAppListenersCopy());
        toStringBuffer.append("\n contextParameters ").append(getContextParameters());
        toStringBuffer.append("\n ejbReferences ");
        printDescriptorSet(getEjbReferenceDescriptors(), toStringBuffer);
        toStringBuffer.append("\n resourceEnvRefReferences ");
        printDescriptorSet(getResourceEnvReferenceDescriptors(), toStringBuffer);
        toStringBuffer.append("\n messageDestReferences ");
        printDescriptorSet(getMessageDestinationReferenceDescriptors(), toStringBuffer);
        toStringBuffer.append("\n resourceReferences ");
        printDescriptorSet(getResourceReferenceDescriptors(), toStringBuffer);
        toStringBuffer.append("\n serviceReferences ");
        printDescriptorSet(getServiceReferenceDescriptors(), toStringBuffer);
        toStringBuffer.append("\n distributable ").append(isDistributable());
        toStringBuffer.append("\n denyUncoveredHttpMethods ").append(isDenyUncoveredHttpMethods());
        toStringBuffer.append("\n securityRoles ").append(getRoles());
        toStringBuffer.append("\n securityConstraints ").append(getSecurityConstraints());
        toStringBuffer.append("\n contextRoot ").append(getContextRoot());
        toStringBuffer.append("\n loginConfiguration ").append(getLoginConfiguration());
        toStringBuffer.append("\n webComponentDescriptors ");
        printDescriptorSet(getWebComponentDescriptors(), toStringBuffer);
        toStringBuffer.append("\n environmentEntries ");
        printDescriptorSet(getEnvironmentEntries(), toStringBuffer);

    }

    private static void printDescriptorSet(Set<?> descSet, StringBuffer sbuf) {
        if (descSet == null) {
            return;
        }
        for (Object obj : descSet) {
            if (obj instanceof Descriptor) {
                ((Descriptor) obj).print(sbuf);
            } else {
                sbuf.append(obj);
            }
        }
    }
}
