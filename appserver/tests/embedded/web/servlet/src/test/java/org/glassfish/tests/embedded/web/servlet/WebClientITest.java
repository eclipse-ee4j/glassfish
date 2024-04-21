/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.web.servlet;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WebClientITest {

    @Test
    public void test() throws Exception {
        try (Application app = Application.start(); WebClient webClient = new WebClient()) {
            Page page = webClient.getPage(app.getEndpoint());
            System.out.println("Got response " + page.getWebResponse().getContentAsString());
            assertThat(page.getWebResponse().getContentAsString(), startsWith("Hello World"));
            String hostName = System.getProperty("com.sun.aas.hostName");
            assertNotNull(hostName);
            page = webClient.getPage(app.getEndpoint().toExternalForm().replaceFirst("localhost", hostName));
            System.out.println("Got response " + page.getWebResponse().getContentAsString());
            assertThat(page.getWebResponse().getContentAsString(), startsWith("Hello World"));
        }
    }
}
