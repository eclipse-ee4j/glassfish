/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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
import static org.junit.Assert.*;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.glassfish.api.admin.config.ApplicationName;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Application;

import java.util.List;
import java.beans.PropertyVetoException;
import org.glassfish.api.admin.config.ApplicationName;

/**
 * User: Jerome Dochez
 * Date: Apr 8, 2008
 * Time: 9:45:21 PM
 */
public class CollectionsAccessTest extends ConfigApiTest  {


    public String getFileName() {
        return "DomainTest";
    }

    @Test(expected=IllegalStateException.class)
    public void unprotectedAccess() throws IllegalStateException {
        Applications apps = getHabitat().getService(Applications.class);
        assertTrue(apps!=null);
        apps.getModules().add(null);
    }

    @Test(expected= TransactionFailure.class)
    public void semiProtectedTest() throws TransactionFailure {
        final Applications apps = getHabitat().getService(Applications.class);
        assertTrue(apps!=null);
        ConfigSupport.apply(new SingleConfigCode<Applications>() {
            public Object run(Applications param) throws PropertyVetoException, TransactionFailure {
                // this is the bug, we should not get the list from apps but from param.
                List<ApplicationName> modules = apps.getModules();
                Application m = param.createChild(Application.class);
                modules.add(m); // should throw an exception
                return m;
            }
        }, apps);
    }

    @Test
    public void protectedTest() throws TransactionFailure {
        final Applications apps = getHabitat().getService(Applications.class);
        assertTrue(apps!=null);
        ConfigSupport.apply(new SingleConfigCode<Applications>() {
            public Object run(Applications param) throws PropertyVetoException, TransactionFailure {
                List<ApplicationName> modules = param.getModules();
                Application m = param.createChild(Application.class);
                m.setName( "ejb-test" );
                m.setLocation("test-location");
                m.setObjectType("ejb");
                modules.add(m);
                modules.remove(m);
                return m;
            }
        }, apps);
    }
}

