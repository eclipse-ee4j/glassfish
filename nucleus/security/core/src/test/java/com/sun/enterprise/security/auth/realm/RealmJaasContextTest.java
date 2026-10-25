/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.auth.realm;

import com.sun.enterprise.security.auth.realm.exceptions.BadRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * Verifies that a custom realm which only delegates to {@link Realm#init(Properties)} exposes
 * the configured {@code jaas-context} property through {@link Realm#getJAASContext()}.
 */
public class RealmJaasContextTest {

    @Test
    public void initStoresJaasContext() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Realm.JAAS_CONTEXT_PARAM, "customRealm");

        CustomRealm realm = new CustomRealm();
        realm.init(properties);

        assertThat(realm.getJAASContext(), equalTo("customRealm"));
    }

    @Test
    public void jaasContextIsNullWhenNotConfigured() throws Exception {
        CustomRealm realm = new CustomRealm();
        realm.init(new Properties());

        assertThat(realm.getJAASContext(), nullValue());
    }

    private static final class CustomRealm extends Realm {

        @Override
        public void init(Properties properties) throws BadRealmException, NoSuchRealmException {
            super.init(properties);
        }

        @Override
        public String getAuthType() {
            return "custom";
        }

        @Override
        public Enumeration<String> getGroupNames(String username) {
            return Collections.emptyEnumeration();
        }
    }
}
