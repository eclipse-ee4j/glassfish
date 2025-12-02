/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.web;

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.web.ContextParameter;
import com.sun.enterprise.util.Result;
import com.sun.enterprise.web.session.PersistenceType;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.deployment.common.ApplicationConfigInfo;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.web.LogFacade;
import org.glassfish.web.config.serverbeans.ContextParam;
import org.glassfish.web.config.serverbeans.EnvEntry;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;
import org.glassfish.web.deployment.runtime.SessionManager;
import org.glassfish.web.deployment.runtime.SunWebAppImpl;

public class WebApplication implements ApplicationContainer<WebBundleDescriptorImpl> {

    private static final Logger logger = LogFacade.getLogger();

    private static final ResourceBundle rb = logger.getResourceBundle();

    private final WebContainer container;
    private final WebModuleConfig wmInfo;
    private final Set<WebModule> webModules = new HashSet<>();
    private final org.glassfish.web.config.serverbeans.WebModuleConfig appConfigCustomizations;

    public WebApplication(WebContainer container, WebModuleConfig config,
            final ApplicationConfigInfo appConfigInfo) {
        this.container = container;
        this.wmInfo = config;
        this.appConfigCustomizations = extractCustomizations(appConfigInfo);
    }

    @Override
    public boolean start(ApplicationContext appContext) throws Exception {

        webModules.clear();

        Properties props = null;

        if (appContext!=null) {
            wmInfo.setAppClassLoader(appContext.getClassLoader());
            if (appContext instanceof DeploymentContext) {
                DeploymentContext deployContext = (DeploymentContext)appContext;
                wmInfo.setDeploymentContext(deployContext);
                if (isKeepState(deployContext, true)) {
                    props = deployContext.getAppProps();
                }
            }
            applyApplicationConfig(appContext);
        }

        List<Result<WebModule>> results = container.loadWebModule(wmInfo, "null", props);
        // release DeploymentContext in memory
        wmInfo.setDeploymentContext(null);

        if (results.isEmpty()) {
            logger.log(Level.SEVERE, "WEB0670: Unknown error, loadWebModule returned null, file a bug");
            return false;
        }

        boolean isFailure = false;
        StringBuilder sb = null;
        for (Result<WebModule> result : results) {
            if (result.isFailure()) {
                if (sb == null) {
                    sb = new StringBuilder(result.exception().toString());
                } else {
                    sb.append(result.exception().toString());
                }
                logger.log(Level.WARNING, result.exception().toString(),
                           result.exception());
                isFailure = true;
            } else {
                webModules.add(result.result());
            }
        }

        if (isFailure) {
            webModules.clear();
            throw new Exception(sb.toString());
        }

        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, LogFacade.LOADING_APP,
                new Object[] {wmInfo.getDescriptor().getName(), wmInfo.getDescriptor().getContextRoot()});
        }

        return true;
    }

    @Override
    public boolean stop(ApplicationContext stopContext) {

        if (stopContext instanceof DeploymentContext) {
            DeploymentContext deployContext = (DeploymentContext)stopContext;

            Properties props = null;
            boolean keepSessions = isKeepState(deployContext, false);
            if (keepSessions) {
                props = new Properties();
            }

            container.unloadWebModule(getDescriptor().getContextRoot(),
                                      getDescriptor().getApplication().getRegistrationName(),
                                      wmInfo.getVirtualServers(), props);

            if (keepSessions) {
                Properties actionReportProps = getActionReportProperties(deployContext);
                // should not be null here
                if (actionReportProps != null) {
                    actionReportProps.putAll(props);
                }
            }
        }

        stopCoherenceWeb();

        return true;
    }

    /**
     * Suspends this application on all virtual servers.
     */
    @Override
    public boolean suspend() {
        return container.suspendWebModule(
            wmInfo.getDescriptor().getContextRoot(), "null", null);
    }

    /**
     * Resumes this application on all virtual servers.
     */
    @Override
    public boolean resume() throws Exception {
        // WebContainer.loadWebModule(), which is called by start(),
        // already checks if the web module has been suspended, and if so,
        // just resumes it and returns
        return start(null);
    }

    /**
     * Returns the class loader associated with this application
     *
     * @return ClassLoader for this app
     */
    @Override
    public ClassLoader getClassLoader() {
        return wmInfo.getAppClassLoader();
    }

    /**
     * Gets a set of all the WebModule instances (one per virtual
     * server deployment) of this WebApplication.
     *
     * <p>For each WebModule in the returned set, the corresponding
     * ServletContext may be obtained by calling WebModule#getServletContext
     */
    public Set<WebModule> getWebModules() {
        return webModules;
    }

    /**
     * Returns the deployment descriptor associated with this application
     *
     * @return deployment descriptor if they exist or null if not
     */
    @Override
    public WebBundleDescriptorImpl getDescriptor() {
        return wmInfo.getDescriptor();
    }

    private boolean isKeepState(DeploymentContext deployContext, boolean isDeploy) {
        Boolean keepState = null;
        if (isDeploy) {
            DeployCommandParameters dcp = deployContext.getCommandParameters(DeployCommandParameters.class);
            if (dcp != null) {
                keepState = dcp.keepstate;
            }
        } else {
            UndeployCommandParameters ucp = deployContext.getCommandParameters(UndeployCommandParameters.class);
            if (ucp != null) {
                keepState = ucp.keepstate;
            }
        }

        if (keepState == null) {
            String keepSessionsString = deployContext.getAppProps().getProperty(DeploymentProperties.KEEP_SESSIONS);
            if (keepSessionsString != null && keepSessionsString.trim().length() > 0) {
                keepState = Boolean.valueOf(keepSessionsString);
            } else {
                keepState = getDescriptor().getApplication().getKeepState();
            }
        }

        return ((keepState != null) ? keepState : false);
    }

    /**
     * Extracts the application config information for the web container
     * from the saved config info.  The saved config info is from the
     * in-memory configuration (domain.xml) if this app was already deployed
     * and is being redeployed.
     *
     * @param appConfigInfo
     * @return
     */
    private org.glassfish.web.config.serverbeans.WebModuleConfig extractCustomizations(
            final ApplicationConfigInfo appConfigInfo) {
        return appConfigInfo.get(trimmedModuleName(wmInfo.getName()), "web");
    }

    private String trimmedModuleName(String moduleName) {
        final int hash = moduleName.indexOf('#');
        if (hash == -1) {
            return moduleName;
        }
        return moduleName.substring(hash + 1);
    }
    /**
     * Applies application config customization (stored temporarily in the
     * start-up context's start-up parameters) to the web app's descriptor.
     * @param appContext
     */
    private void applyApplicationConfig(ApplicationContext appContext) {

        WebBundleDescriptorImpl descriptor = wmInfo.getDescriptor();

        try {
            if (appConfigCustomizations != null) {
                EnvEntryCustomizer envEntryCustomizer = new EnvEntryCustomizer(descriptor.getEnvironmentEntries(),
                    appConfigCustomizations.getEnvEntry());
                ContextParamCustomizer contextParamCustomizer = new ContextParamCustomizer(
                    descriptor.getContextParameters(), appConfigCustomizations.getContextParam());

                envEntryCustomizer.applyCustomizations();
                contextParamCustomizer.applyCustomizations();
            }
        } catch (ClassCastException ex) {
            /*
             * If the user specified an env-entry value that does not
             * work with the env-entry type it can cause a class cast
             * exception.  Log the warning but continue working.
             */
            logger.log(Level.WARNING, "Environment entry customization failed.", ex);
        }
    }

    private Properties getActionReportProperties(DeploymentContext deployContext) {
        if (!wmInfo.getDescriptor().getApplication().isVirtual()) {
            deployContext = ((ExtendedDeploymentContext)deployContext).getParentContext();
        }

        return deployContext.getActionReport().getExtraProperties();
    }

    /*
     * Convenience class for applying customizations to descriptor items.
     * <p>
     * Much of the logic is the same for the different types of customizations -
     * and this class abstracts all the common behavior.  This may seem like
     * overkill, factoring this logic out like this, but the applyCustomizations
     * logic is not something we want to have two copies of.
     */
    private abstract class Customizer<T,U> {

        protected Set<T> descriptorItems;
        protected List<U> customizations;

        private final String descriptorItemName;

        private Customizer(Set<T> descriptorItems, List<U> customizations, String descriptorItemName) {
            this.descriptorItems = descriptorItems;
            this.customizations = customizations;
            this.descriptorItemName = descriptorItemName;
        }

        /**
         * Indicates whether the customization says to ignore any corresponding
         * descriptor entry.
         * @param customization the customization
         * @return true if the user wants to ignore any corresponding descriptor entry; false otherwise
         */
        protected abstract boolean isIgnoreDescriptorItem(U customization);

        /**
         * Creates a new descriptor item using the information from the
         * customization.
         * @param customization the customization the gives the value(s) for the new descriptor
         * @return the new descriptor item
         */
        protected abstract T newDescriptorItem(U customization);

        /**
         * Assigns the values from the customization to the existing descriptor
         * item.
         * @param descriptorItem descriptor item to change
         * @param customization customization containing the new values to be set in the descriptor item
         */
        protected abstract void setDescriptorItemValue(T descriptorItem, U customization);

        /**
         * Returns the name from the descriptor item
         * @param descriptorItem
         * @return name from the descriptor item
         */
        protected abstract String getName(T descriptorItem);

        /**
         * Returns the value from the descriptor item
         * @param descriptorItem
         * @return value from the descriptor item
         */
        protected abstract String getValue(T descriptorItem);

        /**
         * Returns the name from the customization
         * @param customization
         * @return name from the customization
         */
        protected abstract String getCustomizationName(U customization);

        /**
         * Represents the customization as a String for logging.
         * @param customization
         * @return
         */
        protected abstract String toString(U customization);


        /**
         * Removes the descriptor item from the descriptor's collection
         * of this type of item.
         *
         * @param descriptorItem the item to remove
         */
        protected void removeDescriptorItem(T descriptorItem) {
            descriptorItems.remove(descriptorItem);
        }

        /**
         * Adds a new descriptor item to the descriptor's collection of
         * items, basing the new one on the customization the user created.
         *
         * @param customization
         * @return the newly-created item
         */
        protected T addDescriptorItem(U customization) {
            T newItem = newDescriptorItem(customization);
            descriptorItems.add(newItem);
            return newItem;
        }

        /**
         * Applies the set of customizations to the descriptor's set of
         * items.
         */
        void applyCustomizations () {
            boolean isFiner = logger.isLoggable(Level.FINER);

          nextCustomization:
            for (U customization : customizations) {
                /*
                 * For each customization try to find a descriptor item with
                 * the same name.  If there is one, either ignore the descriptor
                 * item (if that is what the customization specifies) or override
                 * the descriptor items'a value with the value from the
                 * customization.
                 */
                for (Iterator<T> it = descriptorItems.iterator(); it.hasNext();) {
                    T descriptorItem = it.next();
                    String dItemName = getName(descriptorItem);
                    String customizationItemName = getCustomizationName(customization);
                    if (dItemName.equals(customizationItemName)) {
                        /*
                         * We found a descriptor item that matches this
                         * customization's name.
                         */
                        if (isIgnoreDescriptorItem(customization)) {
                            /*
                             * The user wants to ignore this descriptor item
                             * so remove it from the descriptor's collection
                             * of items.
                             */
                            it.remove();
                            if (isFiner) {
                                logger.log(Level.FINER,
                                        LogFacade.IGNORE_DESCRIPTOR,
                                        new Object[]{descriptorItemName, getName(descriptorItem)});
                            }
                        } else {
                            /*
                             * The user wants to override the setting of this
                             * descriptor item using the customized settings.
                             */
                            String oldValue = getValue(descriptorItem); // for logging purposes only
                            try {
                                setDescriptorItemValue(descriptorItem, customization);
                                if (isFiner) {
                                    logger.log(Level.FINER, LogFacade.OVERIDE_DESCRIPTOR,
                                            descriptorItemName + " " +
                                            getName(descriptorItem) + "=" +
                                            oldValue +
                                            " with " + toString(customization));
                                }
                            } catch (Exception e) {
                                logger.warning(toString(customization) + " " + e.getLocalizedMessage());
                            }
                        }
                        /*
                         * We have matched this customization with a descriptor
                         * item, so we can skip to the next customization.
                         */
                        continue nextCustomization;
                    }
                }
                /*
                 * The customization matched no existing descriptor item, so
                 * add a new descriptor item.
                 */
                try {
                    T newItem = addDescriptorItem(customization);
                    if (isFiner) {
                        logger.log(Level.FINER,
                                LogFacade.CREATE_DESCRIPTOR,
                                descriptorItemName + getName(newItem) + "=" + getValue(newItem));
                    }
                } catch (Exception e) {
                    logger.warning(toString(customization) + " " + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Concrete implementation of the context-parameter customizer.
     */
    private class ContextParamCustomizer extends Customizer<ContextParameter,ContextParam> {

        private ContextParamCustomizer(Set<ContextParameter> descriptorItems, List<ContextParam> customizations) {
            super(descriptorItems, customizations, "context-param"); // NOI18N
        }

        @Override
        protected boolean isIgnoreDescriptorItem(ContextParam customization) {
            return Boolean.parseBoolean(customization.getIgnoreDescriptorItem());
        }

        @Override
        protected void setDescriptorItemValue(ContextParameter descriptorItem, ContextParam customization) {
            descriptorItem.setValue(customization.getParamValue());
        }

        @Override
        protected ContextParameter newDescriptorItem(ContextParam customization) {
            ContextParameter newItem =
                    new EnvironmentProperty(
                        customization.getParamName(),
                        customization.getParamValue(),
                        "" /* description */);
            return newItem;
        }

        @Override
        protected String getName(ContextParameter descriptorItem) {
            return descriptorItem.getName();
        }

        @Override
        protected String getCustomizationName(ContextParam customization) {
            return customization.getParamName();
        }

        @Override
        protected String getValue(ContextParameter descriptorItem) {
            return descriptorItem.getValue();
        }

        @Override
        protected String toString(ContextParam customization) {
            return "Context-param: name=" + customization.getParamName() + ", value=" + customization.getParamValue();
        }

    }

    /**
     * Concrete implementation for the EnvEntry customizer.
     */
    private class EnvEntryCustomizer extends Customizer<EnvironmentProperty, EnvEntry> {

        private EnvEntryCustomizer(Set<EnvironmentProperty> descriptorItems, List<EnvEntry> customizations) {
            super(descriptorItems, customizations, "env-entry"); // NOI18N
        }

        @Override
        protected boolean isIgnoreDescriptorItem(EnvEntry customization) {
            return Boolean.parseBoolean(customization.getIgnoreDescriptorItem());
        }

        @Override
        protected void setDescriptorItemValue(EnvironmentProperty descriptorItem, EnvEntry customization) {
            customization.validateValue();
            descriptorItem.setValue(customization.getEnvEntryValue());
            descriptorItem.setType(customization.getEnvEntryType());
        }

        @Override
        protected EnvironmentProperty newDescriptorItem(EnvEntry customization) {
            customization.validateValue();
            EnvironmentProperty newItem =
                    new EnvironmentProperty(
                        customization.getEnvEntryName(),
                        customization.getEnvEntryValue(),
                        customization.getDescription(),
                        customization.getEnvEntryType());
            /*
             * Invoke setValue which records that the value has been set.
             * Otherwise naming does not bind the name.
             */
            newItem.setValue(customization.getEnvEntryValue());
            return newItem;
        }

        @Override
        protected String getName(EnvironmentProperty descriptorItem) {
            return descriptorItem.getName();
        }

        @Override
        protected String getCustomizationName(EnvEntry customization) {
            return customization.getEnvEntryName();
        }

        @Override
        protected String getValue(EnvironmentProperty descriptorItem) {
            return descriptorItem.getValue();
        }

        @Override
        protected String toString(EnvEntry customization) {
            return "EnvEntry: name=" + customization.getEnvEntryName() +
                    ", type=" + customization.getEnvEntryType() +
                    ", value=" + customization.getEnvEntryValue() +
                    ", desc=" + customization.getDescription();
        }
    }

    private void stopCoherenceWeb() {
        if (wmInfo.getDescriptor() != null && wmInfo.getDescriptor().getSunDescriptor() != null) {
            SunWebAppImpl sunWebApp = (SunWebAppImpl) wmInfo.getDescriptor().getSunDescriptor();
            if (sunWebApp.getSessionConfig() != null && sunWebApp.getSessionConfig().getSessionManager() != null) {
                SessionManager sessionManager = sunWebApp.getSessionConfig().getSessionManager();
                String persistenceType = sessionManager.getAttributeValue(SessionManager.PERSISTENCE_TYPE);
                if (PersistenceType.COHERENCE_WEB.getType().equals(persistenceType)) {
                    ClassLoader cloader = wmInfo.getAppClassLoader();
                    try {
                        Class<?> cacheFactoryClass = cloader.loadClass("com.tangosol.net.CacheFactory");
                        if (cacheFactoryClass != null) {
                            Method shutdownMethod = cacheFactoryClass.getMethod("shutdown");
                            if (shutdownMethod != null) {
                                shutdownMethod.invoke(null);
                            }
                        }
                    } catch (Exception ex) {
                        if (logger.isLoggable(Level.WARNING)) {
                            String msg = rb.getString(LogFacade.EXCEPTION_SHUTDOWN_COHERENCE_WEB);
                            msg = MessageFormat.format(msg, wmInfo.getDescriptor().getName());
                            logger.log(Level.WARNING, msg, ex);
                        }
                    }
                }
            }
        }
    }
}
