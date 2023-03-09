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
import com.sun.enterprise.deployment.web.SecurityRole;
import com.sun.enterprise.deployment.web.SecurityRoleReference;
import com.sun.enterprise.deployment.web.ServletFilter;
import com.sun.enterprise.deployment.web.ServletFilterMapping;
import com.sun.enterprise.deployment.web.SessionConfig;
import com.sun.enterprise.deployment.web.WebResourceCollection;

import jakarta.servlet.ServletContextListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.event.EventTypes;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.grizzly.http.util.MimeType;
import org.glassfish.security.common.Role;

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
    private JspConfigDefinitionDescriptor jspConfigDescriptor = new JspConfigDefinitionDescriptor();

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
        addWelcomeFiles(webBundleDescriptor.getWelcomeFilesSet());
        addCommonWebBundleDescriptor(webBundleDescriptor, true);
    }


    /**
     * Merge the contents of this and given descriptor.
     *
     * @param descriptor
     */
    public void addDefaultWebBundleDescriptor(WebBundleDescriptor descriptor) {
        if (getWelcomeFilesSet().isEmpty()) {
            addWelcomeFiles(descriptor.getWelcomeFilesSet());
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
    public void addAppListeners(Collection<? extends AppListenerDescriptor> listeners) {
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
    public Set<ContextParameter> getContextParametersSet() {
        return contextParameters;
    }


    /**
     * Adds all context parameters to my list.
     */
    public void addContextParameters(Collection<ContextParameter> contextParameters) {
        this.contextParameters.addAll(contextParameters);
    }


    /**
     * Adds a new context parameter to my list.
     */
    public void addContextParameter(ContextParameter contextParameter) {
        contextParameters.add(contextParameter);
    }


    /**
     * Removes the given context parameter from my list.
     */
    public void removeContextParameter(ContextParameter contextParameter) {
        contextParameters.remove(contextParameter);
    }


    /**
     * @return the enumeration of my references to Enterprise Beans.
     */
    public final Enumeration<EjbReferenceDescriptor> getEjbReferences() {
        return Collections.enumeration(getEjbReferenceDescriptors());
    }


    @Override
    public Set<EjbReferenceDescriptor> getEjbReferenceDescriptors() {
        return ejbReferences;
    }


    /**
     * @return {@link EjbReferenceDescriptor} with the matching name or throw.
     */
    public EjbReferenceDescriptor getEjbReferenceByName(String name) {
        return getEjbReference(name);
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


    /**
     * @return enumeration of {@link EnvironmentProperty}, must not be null but may be empty.
     */
    public final Enumeration<EnvironmentProperty> getEnvironmentEntries() {
        return Collections.enumeration(getEnvironmentEntrySet());
    }


    @Override
    public Set<EnvironmentProperty> getEnvironmentProperties() {
        return getEnvironmentEntrySet();
    }


    /**
     * @return {@link Set} of {@link EnvironmentProperty}, must not be null but may be empty.
     */
    public Set<EnvironmentProperty> getEnvironmentEntrySet() {
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
     * @param name
     * @return null or {@link EnvironmentProperty} found by the name. Case sensitive.
     */
    protected EnvironmentProperty findEnvironmentEntryByName(String name) {
        for (EnvironmentProperty ev : getEnvironmentEntrySet()) {
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
     */
    public void removeEnvironmentEntry(EnvironmentProperty environmentEntry) {
        environmentEntries.remove(environmentEntry);
    }


    /**
     * @return an enumeration of the error pages I have.
     */
    public Enumeration<ErrorPageDescriptor> getErrorPageDescriptors() {
        return Collections.enumeration(getErrorPageDescriptorsSet());
    }


    public Set<ErrorPageDescriptor> getErrorPageDescriptorsSet() {
        return errorPageDescriptors;
    }


    /**
     * Adds a new error page to my list.
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
        for (ErrorPageDescriptor next : getErrorPageDescriptorsSet()) {
            if (next.getErrorSignifierAsString().equals(signifier)) {
                return next;
            }
        }
        return null;
    }


    /**
     * Removes the given error page from my list.
     */
    public void removeErrorPageDescriptor(ErrorPageDescriptor errorPageDescriptor) {
        errorPageDescriptors.remove(errorPageDescriptor);
    }


    /**
     * This method return an unmodifiable map of jarName2WebFragNameMap.
     *
     * @return unmodifiable {@link Map}
     */
    public Map<String, String> getJarNameToWebFragmentNameMap() {
        return Collections.unmodifiableMap(jarName2WebFragNameMap);
    }


    public void putJarNameWebFragmentNamePair(String jarName, String webFragName) {
        jarName2WebFragNameMap.put(jarName, webFragName);
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
     * @return an enumeration of my mime mappings.
     */
    public Enumeration<MimeMapping> getMimeMappings() {
        return Collections.enumeration(getMimeMappingsSet());
    }


    public Set<MimeMapping> getMimeMappingsSet() {
        return mimeMappings;
    }


    /**
     * Add the given mime mapping to my list if the given MimeType is not added
     *
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
     * Removes the given mime mapping from my list.
     */
    public void removeMimeMapping(MimeMapping mimeMapping) {
        mimeMappings.remove(mimeMapping);
    }


    public List<String> getOrderedLibs() {
        return orderedLibs;
    }


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


    public Enumeration<ResourceReferenceDescriptor> getResourceReferences() {
        return Collections.enumeration(getResourceReferenceDescriptors());
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
     * @return My list of security constraints.
     */
    public Enumeration<SecurityConstraint> getSecurityConstraints() {
        return Collections.enumeration(getSecurityConstraintsSet());
    }


    public Set<SecurityConstraint> getSecurityConstraintsSet() {
        return securityConstraints;
    }


    public Collection<SecurityConstraint> getSecurityConstraintsForUrlPattern(String urlPattern) {
        Set<SecurityConstraint> constraints = new HashSet<>();
        for (SecurityConstraint constraint : securityConstraints) {
            boolean include = false;
            for (WebResourceCollection nextCol : constraint.getWebResourceCollections()) {
                for (String nextPattern : nextCol.getUrlPatterns()) {
                    if (urlPattern != null && urlPattern.equals(nextPattern)) {
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
     */
    public void addSecurityConstraint(SecurityConstraint securityConstraint) {
        securityConstraints.add(securityConstraint);
    }


    /**
     * Remove the given security constraint.
     */
    public void removeSecurityConstraint(SecurityConstraint securityConstraint) {
        securityConstraints.remove(securityConstraint);
    }


    /**
     * @return an Enumeration of my {@link SecurityRoleDescriptor} objects.
     */
    public Enumeration<SecurityRoleDescriptor> getSecurityRoles() {
        return Collections
            .enumeration(getRoles().stream().map(SecurityRoleDescriptor::new).collect(Collectors.toList()));
    }


    public void addSecurityRole(SecurityRoleDescriptor securityRole) {
        addSecurityRole((SecurityRole) securityRole);
    }


    public void addSecurityRole(SecurityRole securityRole) {
        addRole(new Role(securityRole.getName(), securityRole.getDescription()));
    }


    /**
     * @return all the references by a given component (by name) to the given rolename.
     */
    public SecurityRoleReference getSecurityRoleReferenceByName(String compName, String roleName) {
        for (WebComponentDescriptor comp : getWebComponentDescriptors()) {
            if (!comp.getCanonicalName().equals(compName)) {
                continue;
            }
            SecurityRoleReference r = comp.getSecurityRoleReferenceByName(roleName);
            if (r != null) {
                return r;
            }
        }
        return null;
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
     */
    public void addServletFilter(ServletFilter ref) {
        String name = ref.getName();
        boolean found = false;
        for (ServletFilter servletFilter : servletFilters) {
            if (name.equals(servletFilter.getName())) {
                found = true;
                break;
            }
        }

        if (!found) {
            servletFilters.add(ref);
        }
    }


    /**
     * Removes the given servlet filter from this web component.
     */
    public void removeServletFilter(ServletFilter ref) {
        removeItem(servletFilters, ref);
    }


    /**
     * @return a Vector of servlet filter mappings that I have.
     */
    public List<ServletFilterMapping> getServletFilterMappingDescriptors() {
        return List.copyOf(getServletFilterMappings());
    }


    /**
     * @return a Vector of servlet filters that I have.
     */
    public List<ServletFilterMapping> getServletFilterMappings() {
        return servletFilterMappings;
    }


    /**
     * Adds a servlet filter mapping to this web component.
     */
    public void addServletFilterMapping(ServletFilterMapping ref) {
        if (!servletFilterMappings.contains(ref)) {
            servletFilterMappings.add(ref);
        }
    }


    /**
     * Removes the given servlet filter mapping from this web component.
     */
    public void removeServletFilterMapping(ServletFilterMapping ref) {
        removeItem(servletFilterMappings, ref);
    }


    /**
     * Moves the given servlet filter mapping to a new relative location in the list
     */
    public void moveServletFilterMapping(ServletFilterMapping ref, int relPos) {
        moveItem(servletFilterMappings, ref, relPos);
    }


    /**
     * @return the Set of Web COmponent Descriptors (JSP or JavaServlets) in me.
     */
    public Set<WebComponentDescriptor> getWebComponentDescriptors() {
        return webComponentDescriptors;
    }


    /**
     * @return {@link WebComponentDescriptor} by name or null
     */
    public WebComponentDescriptor getWebComponentByName(String name) {
        for (WebComponentDescriptor webComponent : webComponentDescriptors) {
            if (webComponent.getName().equals(name)) {
                return webComponent;
            }
        }
        return null;
    }


    /**
     * @return {@link WebComponentDescriptor} by canonical name or null
     */
    public WebComponentDescriptor getWebComponentByCanonicalName(String name) {
        for (WebComponentDescriptor next : webComponentDescriptors) {
            if (next.getCanonicalName().equals(name)) {
                return next;
            }
        }
        return null;
    }


    /**
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


    public void resetUrlPatternToServletNameMap() {
        urlPattern2ServletName = null;
    }


    public Set<WebComponentDescriptor> getServletDescriptors() {
        Set<WebComponentDescriptor> servletDescriptors = new HashSet<>();
        for (WebComponentDescriptor webComponent : webComponentDescriptors) {
            if (webComponent.isServlet()) {
                servletDescriptors.add(webComponent);
            }
        }
        return servletDescriptors;
    }


    /**
     * The returned map is supposed to be only modified by the corresponding url patterns set.
     *
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
     * @return an enumeration of the welcome files I have..
     */
    public Enumeration<String> getWelcomeFiles() {
        return Collections.enumeration(getWelcomeFilesSet());
    }


    public Set<String> getWelcomeFilesSet() {
        return welcomeFiles;
    }


    /**
     * Adds a new welcome file to my list.
     */
    public void addWelcomeFile(String fileUri) {
        welcomeFiles.add(fileUri);
    }


    /**
     * Adds the collection to my welcome files.
     */
    public void addWelcomeFiles(Set<String> welcomeFiles) {
        this.welcomeFiles.addAll(welcomeFiles);
    }


    /**
     * Removes a welcome file from my list.
     */
    public void removeWelcomeFile(String fileUri) {
        welcomeFiles.remove(fileUri);
    }


    /**
     * This returns the extra web sun specific info not in the RI DID.
     *
     * @return {@link SunWebApp} or null
     */
    public SunWebApp getSunDescriptor() {
        return null;
    }


    /**
     * This sets the extra web sun specific info not in the RI DID.
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
    protected List<InjectionCapable> getInjectableResourcesByClass(String className, JndiNameEnvironment jndiNameEnv) {
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
        toStringBuffer.append("\n mimeMappings ").append(getMimeMappingsSet());
        toStringBuffer.append("\n welcomeFiles ").append(getWelcomeFilesSet());
        toStringBuffer.append("\n errorPageDescriptors ").append(errorPageDescriptors);
        toStringBuffer.append("\n appListenerDescriptors ").append(getAppListenersCopy());
        toStringBuffer.append("\n contextParameters ").append(getContextParametersSet());
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
        toStringBuffer.append("\n securityRoles ").append(getSecurityRoles());
        toStringBuffer.append("\n securityConstraints ").append(getSecurityConstraintsSet());
        toStringBuffer.append("\n contextRoot ").append(getContextRoot());
        toStringBuffer.append("\n loginConfiguration ").append(getLoginConfiguration());
        toStringBuffer.append("\n webComponentDescriptors ");
        printDescriptorSet(getWebComponentDescriptors(), toStringBuffer);
        toStringBuffer.append("\n environmentEntries ");
        printDescriptorSet(getEnvironmentEntrySet(), toStringBuffer);

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

    /**
     * Removes a specific object from the given list.
     * <p>
     * Does not rely on 'equals', must be the same instance
     */
    private static <T> boolean removeItem(List<T> list, T ref) {
        for (Iterator<T> i = list.iterator(); i.hasNext();) {
            if (ref == i.next()) {
                i.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Moves the given object to a new relative location in the specified list
     */
    private static <T> void moveItem(List<T> list, T ref, int rpos) {
        /* get current position of ref */
        // 'indexOf' is not used because it is base on 'equals()' which may not be unique.
        int size = list.size();
        int old_pos = size - 1;
        for (; old_pos >= 0; old_pos--) {
            if (ref == list.get(old_pos)) {
                break;
            }
        }
        if (old_pos < 0) {
            // not found
            return;
        }

        // limit up/down movement
        int new_pos = old_pos + rpos;
        if (new_pos < 0) {
            new_pos = 0;
        } else if (new_pos >= size) {
            new_pos = size - 1;
        }

        // is it really moving?
        if (new_pos == old_pos) {
            // it's not moving
            return;
        }

        // move it
        list.remove(old_pos);
        list.add(new_pos, ref);
    }
}

