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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jvnet.hk2.config.types.Property;

/**
 *
 * @author Nandini Ektare
 */
public class JDBCRealmPropertyCheckValidator implements ConstraintValidator<JDBCRealmPropertyCheck, AuthRealm> {

    private static final String JDBC_REALM = "com.sun.enterprise.security.ee.authentication.glassfish.jdbc.JDBCRealm";
    private static final String DEFAULT_DIGEST_ALGORITHM = "MD5";

    public void initialize(final JDBCRealmPropertyCheck fqcn) {
    }

    public boolean isValid(final AuthRealm realm, final ConstraintValidatorContext constraintValidatorContext) {

        if (realm.getClassname().equals(JDBC_REALM)) {
            Property jaas_context = realm.getProperty("jaas-context");
            Property ds_jndi = realm.getProperty("datasource-jndi");
            Property user_table = realm.getProperty("user-table");
            Property group_table = realm.getProperty("group-table");
            Property user_name_col = realm.getProperty("user-name-column");
            Property passwd_col = realm.getProperty("password-column");
            Property grp_name_col = realm.getProperty("group-name-column");
            Property digest_algo = realm.getProperty("digest-algorithm");

            if ((jaas_context == null) || (ds_jndi == null) || (user_table == null) || (group_table == null) || (user_name_col == null)
                    || (passwd_col == null) || (grp_name_col == null)) {

                return false;
            }

            if (digest_algo != null) {
                String algoName = digest_algo.getValue();

                if (!("none".equalsIgnoreCase(algoName))) {
                    try {
                        MessageDigest.getInstance(algoName);
                    } catch (NoSuchAlgorithmException e) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
