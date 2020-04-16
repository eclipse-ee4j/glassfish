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
import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 *
 * @author mohit
 */

@Path("/bundleuninstaller")
public class BundleUninstaller {

    @Context ServletContext ctx;
    String returnMessage = "FAIL";

    @POST
    public String uninstallBundle(@FormParam("bundleId") String bundleId) {
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
                bundle.stop();
                returnMessage = "Stopped Bundle : " + bundle.getSymbolicName();
                bundle.uninstall();
                returnMessage = returnMessage + " Uninstalled Bundle : PASS";
                //Unset current BundleId.
                ctx.setAttribute("bundleId", 0);
            } else {
                returnMessage = "Please specify the bundleId to be uninstalled : FAIL";
            }
        } catch (Exception ex) {
            returnMessage = "Exception while uninstalling bundle : FAIL";
            ex.printStackTrace();
        }
        return returnMessage;
    }

}
