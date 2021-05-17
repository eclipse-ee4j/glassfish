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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

@Path("/bundlecontroller")
public class BundleController {

    @Context ServletContext ctx;
    @Context HttpServletResponse resp;

    @POST
    public void bundleController(@FormParam("id") String bundleId, @FormParam("bsubmit") String requestType) {
        System.out.println("Controller :" + requestType);
        long uninstallId = 0;
        BundleContext bundleContext = (BundleContext) ctx.getAttribute("osgi-bundlecontext");

        if(bundleId == null) {
            uninstallId = (Long) ctx.getAttribute("bundleId");
        } else {
            uninstallId = Long.parseLong(bundleId);
        }
        try {
            if(uninstallId != 0) {
                Bundle bundle = bundleContext.getBundle(uninstallId);
                if (bundle != null) {
                    if (requestType.equalsIgnoreCase("Stop") && bundle.getState() == bundle.ACTIVE) {
                        bundle.stop();
                    } else if (requestType.equalsIgnoreCase("Start") &&
                            (bundle.getState() == bundle.RESOLVED || bundle.getState() == bundle.INSTALLED)) {
                        bundle.start();
                    } else if (requestType.equalsIgnoreCase("Uninstall")) {
                        bundle.uninstall();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                resp.sendRedirect(resp.encodeRedirectURL("bundleviewer"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
