/*
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

package org.glassfish.security.services.provider.authorization;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.security.services.api.authorization.AuthorizationAdminConstants;
import org.glassfish.security.services.api.authorization.AzAttributeResolver;
import org.glassfish.security.services.api.authorization.AzEnvironment;
import org.glassfish.security.services.api.authorization.AzResource;
import org.glassfish.security.services.api.authorization.AzSubject;
import org.glassfish.security.services.api.authorization.RoleMappingService;
import org.glassfish.security.services.common.Secure;
import org.glassfish.security.services.config.SecurityProvider;
import org.glassfish.security.services.impl.ServiceLogging;
import org.glassfish.security.services.spi.authorization.RoleMappingProvider;
import org.jvnet.hk2.annotations.Service;

@Service (name="simpleRoleMapping")
@Secure(accessPermissionName="security/service/rolemapper/provider/simple")
@PerLookup
public class SimpleRoleMappingProviderImpl implements RoleMappingProvider {
    private static final Level DEBUG_LEVEL = Level.FINER;
    private static final Logger _logger =
        Logger.getLogger(ServiceLogging.SEC_PROV_LOGGER,ServiceLogging.SHARED_LOGMESSAGE_RESOURCE);

    private static final String ADMIN = "Admin";

    private RoleMappingProviderConfig cfg;
    private boolean deployable;
    private String version;
    private Map<String, ?> options;

    private boolean isDebug() {
        return _logger.isLoggable(DEBUG_LEVEL);
    }

    private boolean isAdminResource(AzResource resource) {
        return "admin".equals(resource.getUri().getScheme());
    }

    private boolean containsAdminGroup(AzSubject subject) {
        // Only checking for principal name
        for (Principal p : subject.getSubject().getPrincipals()) {
            if (AuthorizationAdminConstants.ADMIN_GROUP.equals(p.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initialize(SecurityProvider providerConfig) {
        cfg = (RoleMappingProviderConfig)providerConfig.getSecurityProviderConfig().get(0);
        deployable = cfg.getSupportRoleDeploy();
        version = cfg.getVersion();
        options = cfg.getProviderOptions();
        if (isDebug()) {
            _logger.log(DEBUG_LEVEL, "provider deploy:  " + deployable);
            _logger.log(DEBUG_LEVEL, "provider version: " + version);
            _logger.log(DEBUG_LEVEL, "provider options: " + options);
        }
    }

    @Override
    public boolean isUserInRole(String appContext, AzSubject subject, AzResource resource, String role, AzEnvironment environment, List<AzAttributeResolver> resolvers) {
        boolean result = false;
        if (isDebug()) {
            _logger.log(DEBUG_LEVEL, "isUserInRole() - " + role);
        }

        if (!isAdminResource(resource)) {
            // Log a warning if the resource is not correct
            final String resourceName = resource.getUri() == null ? "null" : resource.getUri().toASCIIString();
            _logger.log(Level.WARNING, ROLEPROV_BAD_RESOURCE, resourceName);
            _logger.log(Level.WARNING, "IllegalArgumentException", new IllegalArgumentException(resourceName));
        }

        // Only support for admin role
        if (ADMIN.equals(role)) {
            result = containsAdminGroup(subject);
        }

        if (isDebug()) {
            _logger.log(DEBUG_LEVEL, "isUserInRole() - returning " + result);
        }
        return result;
    }

    @Override
    public RoleMappingService.RoleDeploymentContext findOrCreateDeploymentContext(String appContext) {
        // Not Supported
        return null;
    }

    @LogMessageInfo(
        message = "Role Mapping Provider supplied an invalid resource: {0}",
        level = "WARNING")
    private static final String ROLEPROV_BAD_RESOURCE = "SEC-PROV-00150";
}
