/*
 * Copyright (c) 2019 Payara Services Ltd.
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
package com.sun.jsftemplating.handlers;

import com.sun.jsftemplating.layout.descriptors.handler.Handler;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContextImpl;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerDefinition;
import com.sun.jsftemplating.layout.descriptors.handler.IODescriptor;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jonathan coustick
 */
public class UtilHandlerTest {

    @Test
    public void mapPutNullTest() {
        HandlerContext context = new HandlerContextImpl(null, null, null, null);
        HandlerDefinition mapPutDefinition = new HandlerDefinition("mapPut");
        mapPutDefinition.setHandlerMethod(UtilHandlers.class.getCanonicalName(), "mapPut");
        IODescriptor mapDescriptor = new IODescriptor("map", Map.class.getCanonicalName());
        Map<String, IODescriptor> inputsMap = new HashMap<>();
        inputsMap.put("map", mapDescriptor);
        mapPutDefinition.setInputDefs(inputsMap);

        Handler mapPutHandler = new Handler(mapPutDefinition);
        mapPutHandler.setInputValue("map", null);

        context.setHandler(mapPutHandler);
        try {
            UtilHandlers.mapPut(context);
            Assert.fail("mapPut failed to throw exception");
        } catch(HandlerException ex) {
            //expected result
        }
    }



}
