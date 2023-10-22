/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;

import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.config.Named;
import org.glassfish.config.support.CreationDecorator;
import org.glassfish.config.support.CrudResolver;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Represents a security Principal, identified using an SSL cert, that is authorized
 * to perform admin operations. Used both to identify the DAS and instances to each other
 * and also for any end-user cert that should be accepted as authorization for admin operations.
 */
@Configured
public interface SecureAdminPrincipal extends ConfigBeanProxy {

    /**
     * Sets the DN of the {@code SecureAdminPrincipal}
     *
     * @param dn the DN
     */
    @Param(primary = true)
    void setDn(String dn);

    /**
     * Gets the distinguished name for this {@code SecureAdminPrincipal}
     *
     * @return {@link String} containing the DN
     */
    @Attribute(key = true)
    String getDn();

    /**
     * Invoked during creation of a new {@code SecureAdminPrincipal}.
     */
    @Service
    @PerLookup
    class CrDecorator implements CreationDecorator<SecureAdminPrincipal> {

        @Inject
        private SecureAdminHelper helper;

        @Param(name = "value", primary = true)
        private String value;

        @Param(optional = true, name = "alias", defaultValue = "false")
        private boolean isAlias = true;

        @Override
        public void decorate(AdminCommandContext context, SecureAdminPrincipal principal) throws TransactionFailure, PropertyVetoException {
            try {
                /*
                 * The user might have specified an alias, so delegate to the
                 * helper to return the DN for that alias (or the DN if that's
                 * what the user specified).
                 */
                principal.setDn(helper.getDN(value, isAlias));
            } catch (Exception ex) {
                throw new TransactionFailure("create", ex);
            }
        }
    }

    /**
     * Resolves using the type and any name, with no restrictions on the name and with an optional mapping
     * from a cert alias to the name.
     *
     * <p>The similar {@link org.glassfish.config.support.TypeAndNameResolver} restricts the name to one
     * that excludes commas, because {@link org.glassfish.config.support.TypeAndNameResolver} uses
     * {@code habitat.getComponent()} which (ultimately) uses {@code habitat.getInhabitantByContract()}
     * which splits the name using a comma to get a list of names to try to match against.
     *
     * <p>In some cases the name might actually contain a comma, so this resolver supports those cases.
     *
     * <p>This resolver also allows the caller to specify an alias instead of the name (the DN) itself,
     * in which case the resolver maps the alias to the corresponding cert's DN and uses that as the name.
     *
     * @author Tim Quinn
     */
    @Service
    @PerLookup
    class Resolver implements CrudResolver {

        @Param(primary = true)
        private String value;

        @Param(optional = true, name = "alias", defaultValue = "false")
        private boolean isAlias = true;

        @Inject
        ServiceLocator habitat;

        @Inject
        private SecureAdminHelper helper;

        final protected static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(SecureAdminPrincipal.class);

        @Override
        public <T extends ConfigBeanProxy> T resolve(AdminCommandContext context, Class<T> type) {
            /*
             * First, convert the alias to the DN (if the name is an alias).
             */
            try {
                value = helper.getDN(value, isAlias);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            if (!SecureAdminPrincipal.class.isAssignableFrom(type)) {
                final String msg = localStrings.getLocalString(SecureAdminPrincipal.class,
                        "SecureAdminPrincipalResolver.configTypeNotNamed",
                        "Config type {0} must extend {1} but does not",
                        type.getSimpleName(),
                        Named.class.getName());
                throw new IllegalArgumentException(msg);
            }

            /*
             * Look among all instances of this contract type for a match on the
             * full name.
             */
            for (T candidate : habitat.getAllServices(type)) {
                if (value.equals(((SecureAdminPrincipal) candidate).getDn())) {
                    return candidate;
                }
            }
            String msg = localStrings.getLocalString(SecureAdminPrincipal.class,
                    "SecureAdminPrincipalResolver.target_object_not_found",
                    "Cannot find a {0} with a name {1}",
                    type.getSimpleName(),
                    value);
            throw new RuntimeException(msg);
        }
    }
}
