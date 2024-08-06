/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.impl.authorization;

import static org.glassfish.security.services.impl.ServiceLogging.SEC_SVCS_LOGGER;
import static org.glassfish.security.services.impl.ServiceLogging.SHARED_LOGMESSAGE_RESOURCE;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.security.services.api.authorization.AzAttributeResolver;
import org.glassfish.security.services.api.authorization.AzResource;
import org.glassfish.security.services.api.authorization.AzSubject;
import org.glassfish.security.services.api.authorization.RoleMappingService;
import org.glassfish.security.services.config.SecurityConfiguration;
import org.glassfish.security.services.config.SecurityProvider;
import org.glassfish.security.services.impl.ServiceFactory;
import org.glassfish.security.services.spi.authorization.RoleMappingProvider;
import org.jvnet.hk2.annotations.Service;

/**
 * <code>RoleMappingServiceImpl</code> implements
 * <code>{@link org.glassfish.security.services.api.authorization.RoleMappingService}</code>
 * by delegating role mapping decisions to configured
 * <code>{@link org.glassfish.security.services.spi.RoleMappingProvider}</code>
 * instances.
 */
@Service
@Singleton
public final class RoleMappingServiceImpl implements RoleMappingService, PostConstruct {
    private static final Level DEBUG_LEVEL = Level.FINER;
    private static final Logger logger = Logger.getLogger(SEC_SVCS_LOGGER,SHARED_LOGMESSAGE_RESOURCE);
    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(RoleMappingServiceImpl.class);

    @Inject
    private Domain domain;

    @Inject
    private ServiceLocator serviceLocator;

    // Role Mapping Service Configuration Information
    private org.glassfish.security.services.config.RoleMappingService config;
    private RoleMappingProvider provider;

    // Service lifecycle
    enum InitializationState {
        NOT_INITIALIZED,
        SUCCESS_INIT,
        FAILED_INIT
    }
    private volatile InitializationState initialized = InitializationState.NOT_INITIALIZED;
    private volatile String reasonInitFailed =
        localStrings.getLocalString("service.role.not_config","The Role Mapping Service was not configured properly.");

    InitializationState getInitializationState() {
        return initialized;
    }

    String getReasonInitializationFailed() {
        return reasonInitFailed;
    }

    void checkServiceAvailability() {
        if (InitializationState.SUCCESS_INIT != getInitializationState()) {
            throw new IllegalStateException(
                localStrings.getLocalString("service.role.not_avail","The Role Mapping Service is not available.")
                + getReasonInitializationFailed());
        }
    }

    private final List<AzAttributeResolver> attributeResolvers =
        Collections.synchronizedList(new java.util.ArrayList<AzAttributeResolver>());

    private boolean isDebug() {
        return logger.isLoggable(DEBUG_LEVEL);
    }

    // Helpers
    private AzSubject makeAzSubject(final Subject subject) {
        return new AzSubjectImpl(subject);
    }

    private AzResource makeAzResource(final URI resource) {
        return new AzResourceImpl(resource);
    }

    /**
     * Initialize the Role Mapping service with the configured role mapping provider.
     */
    @Override
    public void initialize(SecurityConfiguration securityServiceConfiguration) {
        if (InitializationState.NOT_INITIALIZED != initialized) {
            return;
        }

        try {
            // Get the Role Mapping Service configuration
            config = (org.glassfish.security.services.config.RoleMappingService) securityServiceConfiguration;
            if (config != null) {
                // Get the role mapping provider configuration
                // Consider only one provider for now and take the first provider found!
                List<SecurityProvider> providersConfig = config.getSecurityProviders();
                SecurityProvider roleProviderConfig = null;
                if (providersConfig != null) {
                    roleProviderConfig = providersConfig.get(0);
                }
                if (roleProviderConfig != null) {
                    // Get the provider
                    String providerName = roleProviderConfig.getName();
                    if (isDebug()) {
                        logger.log(DEBUG_LEVEL, "Attempting to get Role Mapping Provider \"{0}\".", providerName );
                    }

                    provider = serviceLocator.getService(RoleMappingProvider.class, providerName);
                    if (provider == null) {
                        throw new IllegalStateException(localStrings.getLocalString("service.role.not_provider",
                            "Role Mapping Provider {0} not found.", providerName));
                    }

                    // Initialize the provider
                    provider.initialize(roleProviderConfig);

                    // Service setup complete
                    initialized = InitializationState.SUCCESS_INIT;
                    reasonInitFailed = null;

                    // Log initialized
                    logger.log(Level.INFO, ROLEMAPSVC_INITIALIZED);
                }
            }
        } catch (Exception e) {
            String eMsg = e.getMessage();
            String eClass = e.getClass().getName();
            reasonInitFailed = localStrings.getLocalString("service.role.init_failed",
                "Role Mapping Service initialization failed, exception {0}, message {1}", eClass, eMsg);
            logger.log(Level.WARNING, ROLEMAPSVC_INIT_FAILED, new Object[] {eClass, eMsg});
            throw new RuntimeException(reasonInitFailed, e);
        } finally {
            if (InitializationState.SUCCESS_INIT != initialized) {
                initialized = InitializationState.FAILED_INIT;
            }
        }
    }

    /**
     * Determine the user's role by converting arguments into security authorization data types.
     *
     * @see <code>{@link org.glassfish.security.services.api.authorization.RoleMappingService}</code>
     */
    @Override
    public boolean isUserInRole(String appContext, Subject subject, URI resource, String role) {
        // Validate inputs
        if (subject == null) {
            throw new IllegalArgumentException(
                localStrings.getLocalString("service.subject_null", "The supplied Subject is null."));
        }
        if (resource == null) {
            throw new IllegalArgumentException(
                localStrings.getLocalString("service.resource_null", "The supplied Resource is null."));
        }

        // Convert arguments
        return isUserInRole(appContext, makeAzSubject(subject), makeAzResource(resource), role);
    }

    /**
     * Determine if the user's is in the specified role.
     *
     * @see <code>{@link org.glassfish.security.services.api.authorization.RoleMappingService}</code>
     */
    @Override
    public boolean isUserInRole(String appContext, AzSubject subject, AzResource resource, String role) {
        boolean result = false;

        // Validate inputs
        if (subject == null) {
            throw new IllegalArgumentException(
                localStrings.getLocalString("service.subject_null", "The supplied Subject is null."));
        }
        if (resource == null) {
            throw new IllegalArgumentException(
                localStrings.getLocalString("service.resource_null", "The supplied Resource is null."));
        }

        // Make sure provider and config have been setup...
        checkServiceAvailability();

        // Call provider - AzEnvironment and AzAttributeResolver are placeholders
        result = provider.isUserInRole(appContext, subject, resource, role, new AzEnvironmentImpl(), attributeResolvers);

        // Display and return results
        if (isDebug()) {
            logger.log(DEBUG_LEVEL, "Role Mapping Service result {0}"
                + " for role {1} with resource {2} using subject {3} in context {4}.",
                new String[]{ Boolean.toString(result), role,
                    resource.toString(), subject.toString(), appContext});
        }
        return result;
    }

    /**
     * Find an existing <code>RoleDeploymentContext</code>, or create a new one if one does not
     * already exist for the specified application context.
     *
     * @see <code>{@link org.glassfish.security.services.api.authorization.RoleMappingService}</code>
     */
    @Override
    public RoleMappingService.RoleDeploymentContext findOrCreateDeploymentContext(String appContext) {
        checkServiceAvailability();
        return provider.findOrCreateDeploymentContext(appContext);
    }

    /**
     * Handle lookup of role mapping service configuration and initialization.
     * If no service or provider is configured the service run-time will throw exceptions.
     *
     * Addresses alternate configuration handling until adopt @Proxiable support.
     */
    @Override
    public void postConstruct() {
        org.glassfish.security.services.config.RoleMappingService roleConfiguration =
            ServiceFactory.getSecurityServiceConfiguration(
                domain, org.glassfish.security.services.config.RoleMappingService.class);
        initialize(roleConfiguration);
    }

    //
    // Log Messages
    //

    @LogMessageInfo(
        message = "Role Mapping Service has successfully initialized.",
        level = "INFO")
    private static final String ROLEMAPSVC_INITIALIZED = "SEC-SVCS-00150";

    @LogMessageInfo(
        message = "Role Mapping Service initialization failed, exception {0}, message {1}",
        level = "WARNING")
    private static final String ROLEMAPSVC_INIT_FAILED = "SEC-SVCS-00151";
}
