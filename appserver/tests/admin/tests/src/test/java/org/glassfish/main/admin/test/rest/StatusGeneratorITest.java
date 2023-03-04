/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.admin.test.rest;

import jakarta.ws.rs.core.Response;

import org.glassfish.main.itest.tools.DomainAdminRestClient;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author David Matejcek
 */
public class StatusGeneratorITest extends RestTestBase {

    @Test
    public void testApplicationDeployment() throws Exception {
        String url = getBaseAdminUrl() + CONTEXT_ROOT_MANAGEMENT;
        try (DomainAdminRestClient statusClient = new DomainAdminRestClient(url, "text/plain")) {
            Response response = statusClient.get("/status/");
            assertAll(
                () -> assertThat(response.getStatus(), equalTo(200)),
                () -> assertThat(response.readEntity(String.class),
                    stringContainsInOrder("All Commands used in REST Admin",
                        "Missing Commands not used in REST Admin",
                        "create-instance",
                        "REST-REDIRECT Commands defined on ConfigBeans",
                        "Commands to Resources Mapping Usage in REST Admin",
                        "Resources with Delete Commands in REST Admin (not counting RESTREDIRECT")
                    )
            );
        }
    }
}
