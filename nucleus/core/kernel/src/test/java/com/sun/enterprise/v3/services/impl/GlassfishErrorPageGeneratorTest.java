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

package com.sun.enterprise.v3.services.impl;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;

public class GlassfishErrorPageGeneratorTest {

    private GlassfishErrorPageGenerator generator = new GlassfishErrorPageGenerator();

    @Test
    void testGenerateFor404Status() {
        String result = generator.generate(null, 404, null, null, null);
        assertThat(result, stringContainsInOrder("<!DOCTYPE", "<head><title>GlassFish</title>", "</head><body>",
            "<h1>HTTP Status 404</h1>", "The requested resource is not available.", "</body></html>"));
    }


    @Test
    void testGenerateFor500Status() {
        String result = generator.generate(null, 500, null, null, null);
        assertThat(result, stringContainsInOrder("<!DOCTYPE", "<head><title>GlassFish</title>", "</head><body>",
            "<h1>HTTP Status 500</h1>", "The server encountered an internal error", "</body></html>"));
    }


    @Test
    void testGenerateFor403Status() {
        String result = generator.generate(null, 403, null, null, null);
        assertThat(result, stringContainsInOrder("<!DOCTYPE", "<head><title>GlassFish</title>", "</head><body>",
            "<h1>HTTP Status 403</h1>", "The server encountered an internal error", "</body></html>"));
    }


    @Test
    void testGenerateWith200StatusCode() {
        String result = generator.generate(null, 200, null, null, null);
        assertThat(result, stringContainsInOrder("<!DOCTYPE", "<head><title>GlassFish</title>", "</head><body>",
            "<h1>HTTP Status 200</h1>", "OK", "</body></html>"));
    }
}
