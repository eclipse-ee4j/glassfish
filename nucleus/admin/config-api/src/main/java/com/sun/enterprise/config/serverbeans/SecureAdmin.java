/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.serverbeans;

import java.util.List;

import org.glassfish.api.I18n;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.Create;
import org.glassfish.config.support.Delete;
import org.glassfish.config.support.Listing;
import org.glassfish.config.support.TypeAndNameResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.Transaction;

import static com.sun.enterprise.config.serverbeans.SecureAdminHelperHolder.getSecureAdminHelper;

/**
 * Represents the admin security settings for the domain.
 */
@Configured
public interface SecureAdmin extends ConfigBeanProxy {

    String DEFAULT_INSTANCE_ALIAS = "glassfish-instance";

    String DEFAULT_ADMIN_ALIAS = "s1as";

    String ADMIN_INDICATOR_HEADER_NAME = "X-GlassFish-admin";

    String ADMIN_INDICATOR_DEFAULT_VALUE = "true";

    String ADMIN_ONE_TIME_AUTH_TOKEN_HEADER_NAME = "X-GlassFish-authToken";

    @Element
    @Create(
            value = "enable-secure-admin-principal",
            decorator = SecureAdminPrincipal.CrDecorator.class,
            i18n = @I18n("enable.secure.admin.principal.command"),
            cluster = @ExecuteOn(value = {RuntimeType.DAS, RuntimeType.INSTANCE})
    )
    @Delete(
            value = "disable-secure-admin-principal",
            resolver = SecureAdminPrincipal.Resolver.class,
            i18n = @I18n("disable.secure.admin.principal.command"),
            cluster = @ExecuteOn(value = {RuntimeType.DAS, RuntimeType.INSTANCE})
    )
    @Listing(value = "list-secure-admin-principals", i18n = @I18n("list.secure.admin.principals.command"))
    List<SecureAdminPrincipal> getSecureAdminPrincipal();

    @Element
    @Create(
            value = "enable-secure-admin-internal-user",
            decorator = SecureAdminInternalUser.CrDecorator.class,
            i18n = @I18n("enable.secure.admin.internal.user.command"),
            cluster = @ExecuteOn(value = {RuntimeType.DAS, RuntimeType.INSTANCE})
    )
    @Delete(
            value = "disable-secure-admin-internal-user",
            resolver = TypeAndNameResolver.class,
            i18n = @I18n("disable.secure.admin.internal.user.command"),
            cluster = @ExecuteOn(value = {RuntimeType.DAS, RuntimeType.INSTANCE})
    )
    @Listing(value = "list-secure-admin-internal-users", i18n = @I18n("list.secure.admin.internal.user.command"))
    List<SecureAdminInternalUser> getSecureAdminInternalUser();

    /**
     * Gets whether admin security is turned on.
     *
     * @return {@link String} containing the type
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getEnabled();

    /**
     * Sets whether admin security is turned on.
     *
     * @param enabled whether admin security should be on or off ({@code true} or {@code false})
     */
    void setEnabled(String enabled);

    @Attribute(defaultValue = ADMIN_INDICATOR_DEFAULT_VALUE)
    String getSpecialAdminIndicator();

    void setSpecialAdminIndicator(String adminIndicator);

    @Attribute(defaultValue = DEFAULT_ADMIN_ALIAS)
    String dasAlias();

    void setDasAlias(String alias);

    @Attribute(defaultValue = DEFAULT_INSTANCE_ALIAS)
    String instanceAlias();

    void setInstanceAlias(String alias);

    default String getInstanceAlias() {
        return instanceAlias();
    }

    default String getDasAlias() {
        return dasAlias();
    }

    /**
     * Reports whether secure admin is enabled.
     *
     * @param secureAdmin the {@link SecureAdmin}, typically returned from {@code domain.getSecureAdmin()}
     * @return {@code true} if secure admin is enabled; {@code false} otherwise
     */
    static boolean isEnabled(final SecureAdmin secureAdmin) {
        return (secureAdmin != null && Boolean.parseBoolean(secureAdmin.getEnabled()));
    }

    /**
     * Returns the configured (which could be the default) value for the special admin indicator.
     *
     * @param secureAdmin the {@link SecureAdmin}, typically returned from {@code domain.getSecureAdmin()}
     * @return the current value for the admin indicator
     */
    static String configuredAdminIndicator(final SecureAdmin secureAdmin) {
        return (secureAdmin == null ? ADMIN_INDICATOR_DEFAULT_VALUE : secureAdmin.getSpecialAdminIndicator());
    }

    static String DASAlias(final SecureAdmin secureAdmin) {
        return (secureAdmin == null) ? DEFAULT_ADMIN_ALIAS : secureAdmin.getDasAlias();
    }

    static String instanceAlias(final SecureAdmin secureAdmin) {
        return (secureAdmin == null) ? DEFAULT_INSTANCE_ALIAS : secureAdmin.getInstanceAlias();
    }

    static SecureAdminInternalUser secureAdminInternalUser(final SecureAdmin secureAdmin) {
        final List<SecureAdminInternalUser> secureAdminUsers = secureAdminInternalUsers(secureAdmin);
        return (secureAdminUsers.isEmpty() ? null : secureAdminUsers.get(0));
    }

    private static List<SecureAdminInternalUser> secureAdminInternalUsers(final SecureAdmin secureAdmin) {
        return (secureAdmin == null) ? List.of() : secureAdmin.getSecureAdminInternalUser();
    }

    static boolean isUsingUsernamePasswordAuth(final SecureAdmin secureAdmin) {
        return !secureAdminInternalUsers(secureAdmin).isEmpty();
    }

    static List<SecureAdminPrincipal> secureAdminPrincipals(final SecureAdmin secureAdmin, final ServiceLocator habitat) {
        List<SecureAdminPrincipal> principals = List.of();
        if (secureAdmin != null) {
            principals = secureAdmin.getSecureAdminPrincipal();
            if (principals.isEmpty()) {
                try {
                    final Transaction tx = new Transaction();
                    final SecureAdmin secureAdmin_w = tx.enroll(secureAdmin);
                    principals = secureAdmin_w.getSecureAdminPrincipal();
                    final SecureAdminPrincipal dasPrincipal = secureAdmin_w.createChild(SecureAdminPrincipal.class);
                    dasPrincipal.setDn(getSecureAdminHelper(habitat).getDN(secureAdmin.dasAlias(), true));
                    principals.add(dasPrincipal);

                    final SecureAdminPrincipal instancePrincipal = secureAdmin_w.createChild(SecureAdminPrincipal.class);
                    instancePrincipal.setDn(getSecureAdminHelper(habitat).getDN(secureAdmin.instanceAlias(), true));
                    principals.add(instancePrincipal);
                    tx.commit();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return principals;
    }
}
