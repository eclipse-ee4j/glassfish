/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.node;

import java.io.File;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;

import static com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY;

/**
 *Provides the appropriate implementation depending on the current
 *runtime environment.
 *
 * @author tjquinn
 */
public class SaxParserHandlerFactory {

    /** Creates a new instance of SaxParserHandlerFactory */
    public SaxParserHandlerFactory() {
    }

    public static SaxParserHandler newInstance() {
        SaxParserHandler result = null;

        /*
         *If the property com.sun.aas.installRoot is defined, use the
         *original implementation (SaxParserHandler) which fetches DTDs and
         *schemas from the installation directory tree.  Otherwise, assume that
         *the app client container is running under Java Web Start. In that
         *case, there is no product installation directory (at least none can
         *be assumed).  The DTDs and schemas will be retrieved from the
         *JWS-specific jar file instead (SaxParserHandlerBundled).
         *
         *bnevins, Oct 16, 2008.  On Oct. 8, 2008 installRoot was changed to be setup
         *earlier in the startup.  As a result, Embedded GF broke.  It sets up a fake installRoot,
         *because there is *no* install-root.
         *Improvement: don't just see if installRoot is set -- make sure installRoot
         *is bonafide.
          */

        final ServiceLocator habitat = Globals.getDefaultHabitat();

        if(installRootIsValid())
            result = habitat.getService(SaxParserHandler.class);
        else
            result = habitat.getService(SaxParserHandlerBundled.class);

        return result;
    }

    private static boolean installRootIsValid() {
        // In the context of this class, we need to make sure that we know if we
        //have a route to local DTDs.  Period.

        String ir = System.getProperty(INSTALL_ROOT_PROPERTY);

        if(!ok(ir))
            return false;

        File dtds = new File(new File(ir), "lib/dtds");

        if(!dtds.isDirectory())
            return false;

        return true;
    }

    private static boolean ok(String ir) {
        return ir != null && ir.length() > 0;
    }
}
