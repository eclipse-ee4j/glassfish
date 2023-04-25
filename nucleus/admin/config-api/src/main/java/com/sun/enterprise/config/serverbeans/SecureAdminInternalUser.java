/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;

import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.config.support.CreationDecorator;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Records information about a username/password-alias pair to be used for authentication
 * internally among GlassFish processes (DAS to instance, for example).
 *
 * @author Tim Quinn
 */
@Configured
public interface SecureAdminInternalUser extends ConfigBeanProxy {

    /**
     * Sets the username for this authorized internal admin user entry.
     *
     * @param userName username
     */
    @Param(primary = true)
    void setUsername(String userName);

    /**
     * Retrieves the username for this authorized internal admin user entry.
     *
     * @return {@link String} containing the username
     */
    @Attribute(required = true, key = true)
    String getUsername();

    /**
     * Retrieves the password alias for this authorized internal admin user entry.
     *
     * @return {@link String} containing the password alias
     */
    @Attribute(required = true)
    String getPasswordAlias();

    /**
     * Sets the password alias for this authorized internal admin user entry.
     *
     * @param passwordAlias password alias
     */
    @Param
    void setPasswordAlias(String passwordAlias);

    @Service
    @PerLookup
    class CrDecorator implements CreationDecorator<SecureAdminInternalUser> {

        @Param(primary = true)
        private String username;

        @Param
        private String passwordAlias;

        @Inject
        private SecureAdminHelper helper;

        @Override
        public void decorate(AdminCommandContext context, SecureAdminInternalUser user) throws TransactionFailure, PropertyVetoException {
            try {
                helper.validateInternalUsernameAndPasswordAlias(username, passwordAlias);
            } catch (Exception ex) {
                throw new TransactionFailure("create", ex);
            }
            user.setUsername(username);
            user.setPasswordAlias(passwordAlias);
        }

    }
}
