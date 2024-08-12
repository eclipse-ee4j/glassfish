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

package org.glassfish.security.services.impl;

import com.sun.enterprise.security.store.DomainScopedPasswordAliasStore;
import com.sun.enterprise.security.store.IdentityManagement;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.File;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.security.services.common.Secure;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * Exposes as a service the JCEKS implementation of the
 * domain-scoped password alias store.
 * @author tjquinn
 */
@Service
@Named("JCEKS")
@PerLookup
@Secure(accessPermissionName = "security/service/credential/provider/jceks")
public class JCEKSDomainPasswordAliasStore extends JCEKSPasswordAliasStore implements DomainScopedPasswordAliasStore  {

    private static final String PASSWORD_ALIAS_KEYSTORE = "domain-passwords";

    @Inject @Optional
    private IdentityManagement idm;

    @PostConstruct
    private void initStore() {
        try {
            init(pathToDomainAliasStore(), getMasterPassword());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private char[] getMasterPassword() {
        return idm == null ? null : idm.getMasterPassword();
    }

    private static String pathToDomainAliasStore() {
        return System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) +
                File.separator + "config" + File.separator + PASSWORD_ALIAS_KEYSTORE;
    }
}
