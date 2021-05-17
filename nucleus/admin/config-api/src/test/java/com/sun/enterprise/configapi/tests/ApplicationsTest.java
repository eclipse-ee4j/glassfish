/*
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

package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.glassfish.api.admin.config.ApplicationName;
import org.jvnet.hk2.config.*;
import com.sun.enterprise.config.serverbeans.*;

import java.util.List;
import java.beans.*;

/**
 * Applications related tests
 * @author Jerome Dochez
 */
public class ApplicationsTest extends ConfigApiTest {


    public String getFileName() {
        return "DomainTest";
    }

    @Test
    public void appsExistTest() {
        Applications apps = getHabitat().getService(Applications.class);
        assertTrue(apps!=null);
    }

    @Test
    public void getModulesTest() {
        Applications apps = getHabitat().getService(Applications.class);
        List<ApplicationName> modules = apps.getModules();
        for (ApplicationName module : modules) {
            logger.fine("Module = " + module.getName());
        }
        assertTrue(modules!=null);
    }

    @Test
    public void getApplicationTest() {
        Applications apps = getHabitat().getService(Applications.class);
        Application app = apps.getApplication("simple");
        assertTrue(app != null);
    }

    /**
     * Test which is expecting an UnsupportedOperationException since we are
     * operating on a copy list of the original getModules() list.
     *
     * @throws TransactionFailure
     */
    @Test(expected = UnsupportedOperationException.class)
    public void removalTest() throws Throwable {
        Applications apps = getHabitat().getService(Applications.class);
        try {
            ConfigSupport.apply(new SingleConfigCode<Applications>() {
                public Object run(Applications param) throws PropertyVetoException, TransactionFailure {
                    List<Application> appList = param.getApplications();
                    for (Application application : param.getApplicationsWithSnifferType("web")) {
                        assertTrue(appList.remove(application));
                    }
                    return null;
                }
            }, apps);
        } catch(TransactionFailure e) {
            // good, an exception was thrown, hopfully the right one !
            throw e.getCause();
        }
    }
}
