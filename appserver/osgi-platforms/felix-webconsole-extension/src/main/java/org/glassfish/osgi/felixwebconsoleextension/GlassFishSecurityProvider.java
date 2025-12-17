/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.osgi.felixwebconsoleextension;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.felix.webconsole.WebConsoleSecurityProvider;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.security.services.api.authentication.AuthenticationService;
import org.osgi.framework.BundleContext;

/**
 *
 * @author tangyong@cn.fujitsu.com
 * @author sanjeeb.sahoo@oracle.com
 */
public class GlassFishSecurityProvider implements WebConsoleSecurityProvider {

    private BundleContext ctx;
    private GlassFish gf;

    public void setBundleContext(BundleContext context){
        ctx = context;
    }

    private GlassFish getGlassFish() {
        GlassFish gf = (GlassFish) ctx.getService(ctx.getServiceReference(GlassFish.class.getName()));
        try {
            assert(gf.getStatus() == GlassFish.Status.STARTED);
        } catch (GlassFishException e) {
            throw new RuntimeException(e);
        }
        return gf;
    }

    @Override
    public Object authenticate(String username, String password) {
        gf = getGlassFish();
        AuthenticationService authService = null;
        try {
            authService = getAuthService();
        } catch (GlassFishException gfe) {
            gfe.printStackTrace();
            return null;
        }

        Subject fs = null;

       try {
           fs = authService.login(username, password.toCharArray(), fs);
        } catch (LoginException e) {
          e.printStackTrace();
          return null;
        }

          return fs;
    }

    private AuthenticationService getAuthService() throws GlassFishException {
        return gf.getService(AuthenticationService.class);
    }

    @Override
    public boolean authorize(Object user, String role) {
        // TODO Auto-generated method stub
        return false;
    }
}
