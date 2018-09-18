/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.common.test;

import java.util.List;
import java.util.Map;

import org.glassfish.admingui.common.util.RestResponse;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 *
 * @author jasonlee
 */
public class RestResponseTest {
    @Test
    public void testMessageParts() {
        RestResponse response = new DummyRestResponse();
        assertEquals(10, 10);
    }
}


class DummyRestResponse extends RestResponse {

    @Override
    public String getResponseBody() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<action-report description=\"dummy AdminCommand\" exit-code=\"SUCCESS\">" +
                "<message-part message=\"Part 1\"/>" +
                "<message-part message=\"Part 2\"/>" +
                "<message-part message=\"Part 3\"/>" +
                "<message-part message=\"Part 4\"/>" +
                "<message-part message=\"Part 5\"/>" +
                "<message-part message=\"Part 6\"/>" +
                "<message-part message=\"Part 7\"/>" +
                "<message-part message=\"Part 8\"/>" +
                "<message-part message=\"Part 9\"/>" +
                "<message-part message=\"Part 10\"/>" +
                "</action-report>";
    }

    @Override
    public int getResponseCode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, Object> getResponse() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
}
}
