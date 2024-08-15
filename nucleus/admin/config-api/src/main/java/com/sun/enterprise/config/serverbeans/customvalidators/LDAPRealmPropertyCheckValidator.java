/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.serverbeans.customvalidators;

import com.sun.enterprise.config.serverbeans.AuthRealm;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.jvnet.hk2.config.types.Property;

/**
 *
 * @author Nandini Ektare
 */
public class LDAPRealmPropertyCheckValidator implements ConstraintValidator<LDAPRealmPropertyCheck, AuthRealm> {

    private static final String LDAP_REALM = "com.sun.enterprise.security.auth.realm.ldap.LDAPRealm";

    public void initialize(final LDAPRealmPropertyCheck fqcn) {
    }

    public boolean isValid(final AuthRealm realm, final ConstraintValidatorContext constraintValidatorContext) {

        if (realm.getClassname().equals(LDAP_REALM)) {
            Property jaas_context = realm.getProperty("jaas-context");
            Property dn = realm.getProperty("base-dn");
            Property url = realm.getProperty("directory");

            if (jaas_context == null || jaas_context.getName().equals(""))
                return false;

            if (url == null || url.getName().equals(""))
                return false;

            if (dn == null || dn.getName().equals(""))
                return false;
        }

        return true;
    }
}
