/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.File;

import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.security.store.PasswordAdapter.PASSWORD_ALIAS_KEYSTORE;
import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;

/**
 * Exposes as a service the PKCS12 implementation of the domain-scoped password alias store.
 *
 * @deprecated JCEKS is obsoleted, so internally we use PKCS12.
 *
 * @author tjquinn
 */
@Service
@Named("JCEKS")
@PerLookup
@Deprecated
public class JCEKSDomainPasswordAliasStore extends JCEKSPasswordAliasStore implements DomainScopedPasswordAliasStore  {

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
        return System.getProperty(INSTANCE_ROOT.getSystemPropertyName()) + File.separator + "config" + File.separator
            + PASSWORD_ALIAS_KEYSTORE;
    }
}
