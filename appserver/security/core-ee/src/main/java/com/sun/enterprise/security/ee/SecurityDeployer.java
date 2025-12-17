/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ee;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.security.AppCNonceCacheMap;
import com.sun.enterprise.security.CNonceCacheFactory;
import com.sun.enterprise.security.EjbSecurityPolicyProbeProvider;
import com.sun.enterprise.security.WebSecurityDeployerProbeProvider;
import com.sun.enterprise.security.ee.authorization.GlassFishAuthorizationService;
import com.sun.enterprise.security.ee.web.integration.WebSecurityManager;
import com.sun.enterprise.security.ee.web.integration.WebSecurityManagerFactory;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.api.invocation.RegisteredComponentInvocationHandler;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.DummyApplication;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ModuleInfo;
import org.glassfish.security.common.CNonceCache;
import org.glassfish.security.common.HAUtil;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.deployment.WebBundleDescriptor.AFTER_SERVLET_CONTEXT_INITIALIZED_EVENT;
import static com.sun.enterprise.security.ee.authorization.AuthorizationUtil.getContextID;
import static com.sun.enterprise.security.ee.authorization.AuthorizationUtil.removeRoleMapper;
import static com.sun.enterprise.util.Utility.isEmpty;
import static java.util.logging.Level.WARNING;
import static org.glassfish.internal.deployment.Deployment.APPLICATION_LOADED;
import static org.glassfish.internal.deployment.Deployment.APPLICATION_PREPARED;
import static org.glassfish.internal.deployment.Deployment.MODULE_LOADED;

/**
 * Security Deployer which generate and clean the security policies
 *
 */
@Service(name = "Security")
public class SecurityDeployer extends SimpleDeployer<SecurityContainer, DummyApplication> implements PostConstruct {

    private static final Logger LOGGER = LogDomains.getLogger(SecurityDeployer.class, LogDomains.SECURITY_LOGGER);

    @Inject
    private ServerContext serverContext;

    @Inject
    @Named("webSecurityCIH")
    private Provider<RegisteredComponentInvocationHandler> registeredComponentInvocationHandlerProvider;

    @Inject
    private Provider<Events> eventsProvider;

    @Inject
    private Provider<HAUtil> haUtilProvider;

    @Inject
    private Provider<AppCNonceCacheMap> appCNonceCacheMapProvider;

    @Inject
    private Provider<CNonceCacheFactory> cNonceCacheFactoryProvider;

    @Inject
    private WebSecurityManagerFactory webSecurityManagerFactory;

    // required for HA Enabling CNonceCache for HTTPDigest Auth
    private AppCNonceCacheMap appCnonceMap;
    private HAUtil haUtil;
    private CNonceCacheFactory cnonceCacheFactory;
    private static final String HA_CNONCE_BS_NAME = "HA-CNonceCache-Backingstore";

    private EventListener listener;
    private static WebSecurityDeployerProbeProvider websecurityProbeProvider = new WebSecurityDeployerProbeProvider();
    private static EjbSecurityPolicyProbeProvider ejbProbeProvider = new EjbSecurityPolicyProbeProvider();

    private class AppDeployEventListener implements EventListener {

        @Override
        public void event(Event<?> event) {
            Application application;

            if (MODULE_LOADED.equals(event.type())) {
                ModuleInfo moduleInfo = (ModuleInfo) event.hook();
                if (moduleInfo instanceof ApplicationInfo) {
                    return;
                }

                WebBundleDescriptor webBundleDescriptor = (WebBundleDescriptor)
                    moduleInfo.getMetaData("org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl");
                loadWebPolicy(webBundleDescriptor, false);

            } else if (APPLICATION_LOADED.equals(event.type())) {
                ApplicationInfo applicationInfo = (ApplicationInfo) event.hook();
                application = applicationInfo.getMetaData(Application.class);
                if (application == null) {
                    // this is not a Jakarta EE module, just return
                    return;
                }

                Set<WebBundleDescriptor> webBundleDescriptors = application.getBundleDescriptors(WebBundleDescriptor.class);
                linkPolicies(application, webBundleDescriptors);
                commitEjbPolicies(application);

                if (!isEmpty(webBundleDescriptors)) {
                    // Register the WebSecurityComponentInvocationHandler
                    RegisteredComponentInvocationHandler handler = registeredComponentInvocationHandlerProvider.get();
                    if (handler != null) {
                        handler.register();
                    }
                }
            } else if (AFTER_SERVLET_CONTEXT_INITIALIZED_EVENT.equals(event.type())) {
                commitWebPolicy((WebBundleDescriptor) event.hook());
            }
        }
    }

    public static List<EventTypes<?>> getDeploymentEvents() {
        return List.of(APPLICATION_PREPARED);
    }

    @Override
    public void postConstruct() {
        listener = new AppDeployEventListener();
        eventsProvider.get().register(listener);
    }

    // Creates security policy if needed
    @Override
    protected void generateArtifacts(DeploymentContext deploymentContext) throws DeploymentException {
        OpsParams params = deploymentContext.getCommandParameters(OpsParams.class);
        if (params.origin.isArtifactsPresent()) {
            return;
        }

        String applicationName = params.name();
        try {
            Application application = deploymentContext.getModuleMetaData(Application.class);
            Set<WebBundleDescriptor> webBundleDescriptors = application.getBundleDescriptors(WebBundleDescriptor.class);
            if (webBundleDescriptors == null) {
                return;
            }

            for (WebBundleDescriptor webBundleDescriptor : webBundleDescriptors) {
                webBundleDescriptor.setApplicationClassLoader(deploymentContext.getFinalClassLoader());
                loadWebPolicy(webBundleDescriptor, false);
            }

        } catch (Exception se) {
            throw new DeploymentException("Error in generating security policy for " + applicationName, se);
        }
    }

    // Removes security policy if needed
    @Override
    protected void cleanArtifacts(DeploymentContext deploymentContext) throws DeploymentException {
        deletePolicy(deploymentContext);
        removeRoleMapper(deploymentContext);

        OpsParams params = deploymentContext.getCommandParameters(OpsParams.class);
        if (appCnonceMap != null) {
            CNonceCache cache = appCnonceMap.remove(params.name());
            if (cache != null) {
                cache.destroy();
            }
        }
    }

    @Override
    public DummyApplication load(SecurityContainer container, DeploymentContext context) {
        DeployCommandParameters dparams = context.getCommandParameters(DeployCommandParameters.class);
        Application application = context.getModuleMetaData(Application.class);

        handleCNonceCacheBSInit(
            application.getAppName(),
            application.getBundleDescriptors(WebBundleDescriptor.class),
            dparams.availabilityenabled);

        return new DummyApplication();
    }

    @Override
    public void unload(DummyApplication container, DeploymentContext context) {
        cleanSecurityContext(context.getCommandParameters(OpsParams.class).name());
    }



    // ### Private methods


    /**
     * Translate Web Bundle Policy
     *
     * @param webBundleDescriptor
     * @param remove boolean indicated whether any existing policy statements are removed form context before translation
     * @throws DeploymentException
     */
    private void loadWebPolicy(WebBundleDescriptor webBundleDescriptor, boolean remove) throws DeploymentException {
        try {
            if (webBundleDescriptor != null) {
                if (remove) {
                    String contextId = getContextID(webBundleDescriptor);
                    WebSecurityManager webSecurityManager = webSecurityManagerFactory.getManager(contextId, true);
                    if (webSecurityManager != null) {
                        webSecurityManager.release();
                    }
                }
                webSecurityManagerFactory.createManager(webBundleDescriptor, true, serverContext);
            }

        } catch (Exception se) {
            throw new DeploymentException(
                "Error in generating security policy for " + webBundleDescriptor.getModuleDescriptor().getModuleName(), se);
        }
    }

    /**
     * Puts Web Bundle Policy In Service, repeats translation if Descriptor indicates policy was changed by ContextListener.
     *
     * @param webBundleDescriptor
     * @throws DeploymentException
     */
    private void commitWebPolicy(WebBundleDescriptor webBundleDescriptor) throws DeploymentException {
        try {
            if (webBundleDescriptor != null) {
                if (webBundleDescriptor.isPolicyModified()) {
                    // Redo policy translation for web module
                    loadWebPolicy(webBundleDescriptor, true);
                }

                String contextId = getContextID(webBundleDescriptor);
                websecurityProbeProvider.policyCreationStartedEvent(contextId);

                commitViaManager(contextId);

                websecurityProbeProvider.policyCreationEndedEvent(contextId);
                websecurityProbeProvider.policyCreationEvent(contextId);

            }
        } catch (Exception se) {
            throw new DeploymentException(
                "Error in generating security policy for " + webBundleDescriptor.getModuleDescriptor().getModuleName(), se);
        }
    }

    /**
     * commits ejb policy contexts. This should occur in EjbApplication, being done here until issue with
     * ejb-ejb31-singleton-multimoduleApp.ear is resolved
     *
     * @param ejbs
     */
    private void commitEjbPolicies(Application application) throws DeploymentException {
        Set<EjbBundleDescriptor> ejbDescriptors = application.getBundleDescriptors(EjbBundleDescriptor.class);
        try {
            for (EjbBundleDescriptor ejbDescriptor : ejbDescriptors) {
                String contextId = getContextID(ejbDescriptor);
                ejbProbeProvider.policyCreationStartedEvent(contextId);

                commitViaManager(contextId);

                ejbProbeProvider.policyCreationEndedEvent(contextId);
                ejbProbeProvider.policyCreationEvent(contextId);

            }
        } catch (Exception se) {
            throw new DeploymentException(
                "Error in committing security policy for ejbs of " + application.getRegistrationName(), se);
        }
    }

    /**
     * Links the policy contexts of the application
     *
     * @param application
     * @param webBundleDescriptors
     */
    private void linkPolicies(Application application, Collection<WebBundleDescriptor> webBundleDescriptors) throws DeploymentException {
        try {
            String linkedContextId = null;
            boolean lastInService = false;

            for (WebBundleDescriptor webBundleDescriptor : webBundleDescriptors) {
                String contextId = getContextID(webBundleDescriptor);

                WebSecurityManager manager = webSecurityManagerFactory.getManager(contextId);
                if (manager != null) {
                    lastInService = GlassFishAuthorizationService.linkPolicy(contextId, linkedContextId, lastInService);
                    linkedContextId = contextId;
                }
            }

            Set<EjbBundleDescriptor> ejbBundleDescriptors = application.getBundleDescriptors(EjbBundleDescriptor.class);
            for (EjbBundleDescriptor ejbBundleDescriptor : ejbBundleDescriptors) {
                String contextId = getContextID(ejbBundleDescriptor);

                WebSecurityManager manager = webSecurityManagerFactory.getManager(contextId);
                if (manager != null) {
                    lastInService = GlassFishAuthorizationService.linkPolicy(contextId, linkedContextId, lastInService);
                    linkedContextId = contextId;
                }
            }

        } catch (IllegalStateException se) {
            throw new DeploymentException("Error in linking security policy for " + application.getRegistrationName(), se);
        }
    }

    private void deletePolicy(DeploymentContext deploymentContext) throws DeploymentException {
        OpsParams params = deploymentContext.getCommandParameters(OpsParams.class);
        if (!params.origin.needsCleanArtifacts()) {
            return;
        }

        String applicationName = params.name();

        // Remove policy files only if managers are not destroyed by cleanup
        try {
            String[] contextIds = webSecurityManagerFactory.getContextsForApp(applicationName, false);
            if (contextIds != null) {
                for (String contextId : contextIds) {
                    if (contextId != null) {
                        websecurityProbeProvider.policyDestructionStartedEvent(contextId);

                        deleteViaManager(contextId);

                        websecurityProbeProvider.policyDestructionEndedEvent(contextId);
                        websecurityProbeProvider.policyDestructionEvent(contextId);
                    }
                }
            }
        } catch (IllegalStateException ex) {
            String msg = "Error in removing security policy for " + applicationName;
            LOGGER.log(WARNING, msg, ex);
            throw new DeploymentException(msg, ex);
        }

        // Destroy the managers if present
        cleanSecurityContext(applicationName);
    }

    boolean linkViaManager(String contextId, String linkedContextId, boolean lastInService) {
        WebSecurityManager securityManager = webSecurityManagerFactory.getManager(contextId);
        if (securityManager != null) {
            return securityManager.getAuthorizationService().linkPolicy(linkedContextId, lastInService);
        }

        return GlassFishAuthorizationService.linkPolicy(contextId, linkedContextId, lastInService);

    }

    void commitViaManager(String contextId) {
        WebSecurityManager securityManager = webSecurityManagerFactory.getManager(contextId);
        if (securityManager != null) {
            securityManager.getAuthorizationService().commitPolicy();
        } else {
            GlassFishAuthorizationService.commitPolicy(contextId);
        }
    }

    void deleteViaManager(String contextId) {
        WebSecurityManager securityManager = webSecurityManagerFactory.getManager(contextId);
        if (securityManager != null) {
            securityManager.getAuthorizationService().deletePolicy();
        } else {
            GlassFishAuthorizationService.deletePolicy(contextId);
        }
    }

    @Override
    public MetaData getMetaData() {
        return new MetaData(false, null, new Class[] { Application.class });
    }

    /**
     * Clean security policy generated at deployment time. NOTE: This routine calls destroy on the WebSecurityManagers, but
     * that does not cause deletion of the underlying policy (files). The underlying policy is deleted when removePolicy (in
     * AppDeployerBase and WebModuleDeployer) is called.
     *
     * @param appName the app name
     */
    private boolean cleanSecurityContext(String appName) {
        boolean cleanUpDone = false;
        ArrayList<WebSecurityManager> managers = webSecurityManagerFactory.getManagersForApp(appName, false);
        for (int i = 0; managers != null && i < managers.size(); i++) {
            try {
                websecurityProbeProvider.securityManagerDestructionStartedEvent(appName);
                managers.get(i).destroy();
                websecurityProbeProvider.securityManagerDestructionEndedEvent(appName);
                websecurityProbeProvider.securityManagerDestructionEvent(appName);
                cleanUpDone = true;
            } catch (Exception pce) {
                // log it and continue
                LOGGER.log(WARNING, "Unable to destroy WebSecurityManager", pce);
            }

        }
        return cleanUpDone;
    }


    private boolean isHaEnabled() {
        boolean haEnabled = false;
        // lazily init the required services instead of
        // eagerly injecting them.
        synchronized (this) {
            if (haUtil == null) {
                haUtil = haUtilProvider.get();
            }
        }

        if (haUtil != null && haUtil.isHAEnabled()) {
            haEnabled = true;
            synchronized (this) {
                if (appCnonceMap == null) {
                    appCnonceMap = appCNonceCacheMapProvider.get();
                }
                if (cnonceCacheFactory == null) {
                    cnonceCacheFactory = cNonceCacheFactoryProvider.get();
                }
            }
        }

        return haEnabled;
    }

    private void handleCNonceCacheBSInit(String appName, Set<WebBundleDescriptor> webDesc, boolean isHA) {
        boolean hasDigest = false;
        for (WebBundleDescriptor webBD : webDesc) {
            LoginConfiguration lc = webBD.getLoginConfiguration();
            if (lc != null && LoginConfiguration.DIGEST_AUTHENTICATION.equals(lc.getAuthenticationMethod())) {
                hasDigest = true;
                break;
            }
        }
        if (!hasDigest) {
            return;
        }
        // initialize the backing stores as well for cnonce cache.
        if (isHaEnabled() && isHA) {
            final String clusterName = haUtil.getClusterName();
            final String instanceName = haUtil.getInstanceName();
            if (cnonceCacheFactory != null) {
                CNonceCache cache = cnonceCacheFactory.createCNonceCache(appName, clusterName, instanceName, HA_CNONCE_BS_NAME);
                this.appCnonceMap.put(appName, cache);
            }

        }
    }
}
