/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Applications related tests
 * @author Jerome Dochez
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class ApplicationsTest {
    @Inject
    private ServiceLocator locator;

    @Test
    public void appsExistTest() {
        Applications apps = locator.getService(Applications.class);
        assertNotNull(apps);
        assertThat(apps.getApplications(), hasSize(1));
    }

    @Test
    public void getModulesTest() {
        Applications apps = locator.getService(Applications.class);
        List<ApplicationName> modules = apps.getModules();
        assertThat(modules, hasSize(1));
    }

    @Test
    public void getApplicationTest() {
        Applications apps = locator.getService(Applications.class);
        Application app = apps.getApplication("simple");
        assertNotNull(app);
    }

    /**
     * Test which is expecting an UnsupportedOperationException since we are
     * operating on a copy list of the original getModules() list.
     */
    @Test
    public void removalTest() {
        final Applications apps = locator.getService(Applications.class);
        final SingleConfigCode<Applications> configCode = param -> {
            List<Application> appList = param.getApplications();
            for (Application application : param.getApplicationsWithSnifferType("web")) {
                assertTrue(appList.remove(application));
            }
            return null;
        };
        final TransactionFailure failure = assertThrows(TransactionFailure.class, () -> {
            ConfigSupport.apply(configCode, apps);
        });
        assertThat(failure.getCause(), instanceOf(UnsupportedOperationException.class));
    }
}
