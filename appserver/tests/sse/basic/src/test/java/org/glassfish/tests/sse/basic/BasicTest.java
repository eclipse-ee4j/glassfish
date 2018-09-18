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

package org.glassfish.tests.sse.basic;

import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;

/**
 * This tests basic Server-Sent Events API
 *
 * @author Jitendra Kotamraju
 */
public class BasicTest {
    @Test
    public void testException() throws Exception {
        URL url = new URL("http://localhost:8080/sse-basic-test/exception");
        HttpURLConnection uc = (HttpURLConnection) url.openConnection();
        if (uc.getResponseCode() != 200) {
            throw new RuntimeException("Invoked main page, got HTTP response code="+uc.getResponseCode());
        }
        BufferedReader rdr = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String str = rdr.readLine();
        Assert.assertEquals("data:message1", str);
        str = rdr.readLine();
        Assert.assertEquals("", str);
        str = rdr.readLine();
        Assert.assertNull(str);
        rdr.close();
    }
}
