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

package org.glassfish.appclient.client;

import org.glassfish.appclient.client.acc.UserError;

/**
 *
 * @author tjquinn
 */
public class AppClientGroupFacade {


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       try {
            if (AppClientFacade.acc() == null) {
                /*
                 * The facade JAR has been run directly, not via the appclient
                 * script and not via Java Web Start.  So we have no agent
                 * arguments and no instrumentation for registering transformations.
                 */
                AppClientFacade.prepareACC(null, null);
            }
            AppClientFacade.launch(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        } catch (UserError ue) {
            ue.displayAndExit();
        }
    }
}
