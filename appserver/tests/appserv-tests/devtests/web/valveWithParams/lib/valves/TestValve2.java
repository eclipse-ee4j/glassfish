/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package valves;

import java.io.IOException;
import jakarta.servlet.ServletException;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.glassfish.web.valve.GlassFishValve;

public class TestValve2 implements GlassFishValve {

    private String testProperty = null;

    public String getInfo() {
        return getClass().getName();
    }

    public int invoke(Request request, Response response) throws IOException, ServletException {
        if (testProperty == null) {
            request.getRequest().setAttribute("valve6", "null_value");
        }
        else if ("".equals(testProperty)) {
            request.getRequest().setAttribute("valve5", "empty_value");
        }
        else {
            request.getRequest().setAttribute("valve4", testProperty);
        }
        return GlassFishValve.INVOKE_NEXT;
    }

    public void postInvoke(Request req, Response resp) throws IOException, ServletException {
    }

    public void setTestProperty(String val) {
        testProperty = val;
    }
}

