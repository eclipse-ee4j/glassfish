/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.nucleus.admin.rest;

/**
 *
 * @author jdlee
 */
public class UtilityTest {

    // TODO - JERSEY2
//    @Test
//    public void parameterResolutionTest() {
//        WebApplicationImpl wai = new WebApplicationImpl();
//        ContainerRequest r = new TestHttpRequestContext(wai,
//                "GET",
//                null,
//                "/management/domain/one/two/three/four/five/six/seven/eight/nine/ten/endpoint",
//                "/management/domain/");
//        UriInfo ui = new WebApplicationContext(wai, r, null);
//        Map<String, String> commandParams = new HashMap<String, String>() {{
//           put("foo", "$parent");
//           put("bar", "$grandparent3");
//           put("baz", "$grandparent5");
//        }};
//
//        ResourceUtil.resolveParamValues(commandParams, ui);
//        assertEquals("ten", commandParams.get("foo"));
//        assertEquals("seven", commandParams.get("bar"));
//        assertEquals("five", commandParams.get("baz"));
//    }
//
//    private class TestHttpRequestContext extends ContainerRequest {
//
//        public TestHttpRequestContext(
//                WebApplication wa,
//                String method,
//                InputStream entity,
//                String completeUri,
//                String baseUri) {
//
//            super(wa, method, URI.create(baseUri), URI.create(completeUri), new InBoundHeaders(), entity);
//        }
//    }
}
