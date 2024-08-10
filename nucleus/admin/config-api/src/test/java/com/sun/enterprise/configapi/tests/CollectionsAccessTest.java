/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;

import jakarta.inject.Inject;

import java.util.List;

import org.glassfish.api.admin.config.ApplicationName;
import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * User: Jerome Dochez
 * Date: Apr 8, 2008
 * Time: 9:45:21 PM
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class CollectionsAccessTest {
    @Inject
    private ServiceLocator locator;
    @Inject
    private Applications apps;

    @Test
    public void unprotectedAccess() {
        assertNotNull(apps);
        assertThrows(IllegalStateException.class, () -> apps.getModules().add(null));
    }

    @Test
    public void semiProtectedTest() throws TransactionFailure {
        assertNotNull(apps);
        SingleConfigCode<Applications> configCode = proxy -> {
            List<ApplicationName> modules = proxy.getModules();
            Application m = proxy.createChild(Application.class);
            modules.add(m);
            return m;
        };
        assertThrows(TransactionFailure.class, () -> ConfigSupport.apply(configCode, apps));
    }

    @Test
    public void protectedTest() throws TransactionFailure {
        assertNotNull(apps);
        SingleConfigCode<Applications> configCode = proxy -> {
            List<ApplicationName> modules = proxy.getModules();
            Application m = proxy.createChild(Application.class);
            m.setName( "ejb-test" );
            m.setLocation("test-location");
            m.setObjectType("ejb");
            modules.add(m);
            modules.remove(m);
            return m;
        };
        ConfigSupport.apply(configCode, apps);
    }
}

