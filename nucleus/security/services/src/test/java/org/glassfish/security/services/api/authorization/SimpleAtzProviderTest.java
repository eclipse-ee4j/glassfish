/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.api.authorization;


import org.glassfish.security.services.impl.authorization.*;
import java.net.URI;

import javax.security.auth.Subject;
import org.glassfish.security.common.PrincipalImpl;
import org.glassfish.security.services.api.common.Attributes;
import org.glassfish.security.services.api.context.SecurityContextService;
import org.glassfish.security.services.impl.authorization.AuthorizationServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.jvnet.hk2.testing.junit.HK2Runner;

import org.glassfish.security.services.spi.authorization.AuthorizationProvider;

public class SimpleAtzProviderTest extends HK2Runner {

    private AuthorizationProvider simpleAtzPrv = null;
    private SecurityContextService contextService = null;


    @Before
    public void before() {
        super.before();

        String pf = System.getProperty("java.security.policy");
        System.out.println("policy file = " + pf);
        String bd = System.getProperty("build.dir");
        System.out.println("build dir = " + bd);

        String apsd = System.getProperty("appserver_dir");
        System.out.println("appserver dir = " + apsd);

        String local = System.getProperty("localRepository");
        System.out.println("local repository dir = " + local);

        simpleAtzPrv = testLocator.getService(AuthorizationProvider.class, "simpleAuthorization");
        contextService = testLocator.getService(SecurityContextService.class);

        Assert.assertNotNull(simpleAtzPrv);
        Assert.assertNotNull(contextService);

        contextService.getEnvironmentAttributes().addAttribute(
                AuthorizationAdminConstants.ISDAS_ATTRIBUTE, "true", true);
    }

    @Test
    public void testService() throws Exception {
        final AuthorizationService authorizationService = new AuthorizationServiceImpl();
        Assert.assertNotNull(simpleAtzPrv);
        final AzEnvironment env = new AzEnvironmentImpl();
        final Attributes attrs = contextService.getEnvironmentAttributes();
        for (String attrName : attrs.getAttributeNames()) {
            env.addAttribute(attrName, attrs.getAttributeValue(attrName), true);
        }
        AzSubject azS = authorizationService.makeAzSubject(adminSubject());
        AzResult rt = simpleAtzPrv.getAuthorizationDecision(
                azS,
                authorizationService.makeAzResource(URI.create("admin://some/path")),
                authorizationService.makeAzAction("read"),
                env,
                null
              );

        AzResult.Decision ds = rt.getDecision();

        Assert.assertEquals(AzResult.Decision.PERMIT, ds);

    }

    private Subject adminSubject() {
        final Subject result = new Subject();
        result.getPrincipals().add(new PrincipalImpl("asadmin"));
        return result;
    }
}
