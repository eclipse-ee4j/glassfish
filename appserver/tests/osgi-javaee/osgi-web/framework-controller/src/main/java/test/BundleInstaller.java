/*
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

 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 *
 * @author mohit
 */

@Path("/bundleinstaller")
public class BundleInstaller {

    @Context ServletContext ctx;

    String returnMessage = "FAIL";

    @POST
    public String installBundle(@FormParam("installUrl") String installUrl) {
        BundleContext bundleContext = (BundleContext) ctx.getAttribute("osgi-bundlecontext");
        try {
            if (installUrl != null) {
                //Examples.
                //String installURL = "file:///space/v3work/v3/tests/osgi-javaee/test1/generated/test1.war";
                //String installURL = "reference:file:/space/v3work/v3/tests/osgi-javaee/test6";
                Bundle bundle = bundleContext.installBundle(installUrl);
                returnMessage = "Bundle deployed with ID : " + bundle.getBundleId();
                bundle.start();
                returnMessage = returnMessage + " Started : PASS";
                //Save current BundleId.
                ctx.setAttribute("bundleId", bundle.getBundleId());
            } else {
                returnMessage = "Please specify Installation Type and Bundle Path : FAIL";
            }
        } catch (Exception ex) {
            returnMessage = "Exception installing the bundle : FAIL";
            ex.printStackTrace();
        }
        return returnMessage;
    }
}
