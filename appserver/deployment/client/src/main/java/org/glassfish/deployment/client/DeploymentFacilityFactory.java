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

package org.glassfish.deployment.client;

/**
 * This factory is for retrieving an implementation instance of DeploymentFacility.
 * <p>
 * Currently is returns only a remote implementation.  The local implementation
 * cannot/should not be referenced from here due to module dependencies.  The
 * local implementation refers to classes that are loaded into the DAS, classes
 * which are not present in a remote deployment client and therefore should not
 * be referenced from remote clients.
 */
public class DeploymentFacilityFactory {

    
    public static DeploymentFacility getDeploymentFacility() {
        return new RemoteDeploymentFacility();
    }
}
