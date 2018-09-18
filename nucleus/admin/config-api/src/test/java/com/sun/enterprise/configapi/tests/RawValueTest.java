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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.glassfish.config.support.GlassFishConfigBean;
import com.sun.enterprise.config.serverbeans.Domain;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 30, 2008
 * Time: 11:18:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class RawValueTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setup() {
        System.setProperty("com.sun.aas.instanceRoot", "cafebabe");
    }


    @Test
    public void testAppRoot() {
        Domain domain = getHabitat().getService(Domain.class);
        Domain rawDomain = GlassFishConfigBean.getRawView(domain);
        String appRoot = domain.getApplicationRoot();
        String appRawRoot = rawDomain.getApplicationRoot();
        assertFalse(appRawRoot.equals(appRoot));
        assertTrue(appRawRoot.startsWith("${"));
    }    
}
