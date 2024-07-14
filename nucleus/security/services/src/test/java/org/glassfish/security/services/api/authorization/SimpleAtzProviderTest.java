/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

import jakarta.inject.Inject;

import java.net.URI;

import javax.security.auth.Subject;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.security.common.UserNameAndPassword;
import org.glassfish.security.services.api.common.Attributes;
import org.glassfish.security.services.api.context.SecurityContextService;
import org.glassfish.security.services.impl.authorization.AuthorizationServiceImpl;
import org.glassfish.security.services.impl.authorization.AzEnvironmentImpl;
import org.glassfish.security.services.spi.authorization.AuthorizationProvider;
import org.glassfish.tests.utils.junit.hk2.HK2JUnit5Extension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(HK2JUnit5Extension.class)
public class SimpleAtzProviderTest {

    @Inject
    private ServiceLocator testLocator;
    private AuthorizationProvider simpleAtzPrv;
    private SecurityContextService contextService;

    @BeforeEach
    public void before() {
        simpleAtzPrv = testLocator.getService(AuthorizationProvider.class, "simpleAuthorization");
        contextService = testLocator.getService(SecurityContextService.class);

        assertNotNull(simpleAtzPrv);
        assertNotNull(contextService);
        contextService.getEnvironmentAttributes()
            .addAttribute(AuthorizationAdminConstants.ISDAS_ATTRIBUTE, "true", true);
    }

    @Test
    public void testService() throws Exception {
        final AuthorizationService authorizationService = new AuthorizationServiceImpl();
        assertNotNull(simpleAtzPrv);
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
        assertEquals(AzResult.Decision.PERMIT, ds);

    }

    private Subject adminSubject() {
        final Subject result = new Subject();
        result.getPrincipals().add(new UserNameAndPassword("asadmin"));
        return result;
    }
}
