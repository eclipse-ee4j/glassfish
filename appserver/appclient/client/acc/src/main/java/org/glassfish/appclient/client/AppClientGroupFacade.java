/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.appclient.client.acc.AppClientContainer;
import org.glassfish.embeddable.client.ApplicationClientClassLoader;
import org.glassfish.embeddable.client.UserError;

/**
 * The AppClientContainerAgent initializes instrumentation, class loaders and also
 * the {@link AppClientContainer} instance, which must be reachable for this class then
 * - and that was a problem. The solution is to "smuggle" the container using
 * the class loader, which is the only thing which can be shared with the agent.
 * <p>
 * With the usage of JPMS it got yet bit more complicated as we had to keep some order
 * between classes and class loaders and we also noticed an issue with Windows which limit
 * the length of the command line; therefore we introduced another layer which automatically
 * detects some GlassFish's jar files and we don't need to add them to the -classpath argument.
 *
 * @author tjquinn
 * @author David Matejcek
 */
public class AppClientGroupFacade {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            ApplicationClientClassLoader loader = (ApplicationClientClassLoader) Thread.currentThread()
                .getContextClassLoader();
            loader.getApplicationClientContainer().launch(args);
        } catch (UserError ue) {
            ue.displayAndExit();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
