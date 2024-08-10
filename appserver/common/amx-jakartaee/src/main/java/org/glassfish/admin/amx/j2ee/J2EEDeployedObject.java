/*
 * Copyright (c) 1997, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.j2ee;

import javax.management.ObjectName;

import org.glassfish.admin.amx.annotation.Description;
import org.glassfish.admin.amx.annotation.ManagedAttribute;

/**
 */
public interface J2EEDeployedObject extends J2EEManagedObject, StateManageable {

    /**
     * The deploymentDescriptor string must contain the original XML
     * deployment descriptor that was created for this module during
     * the deployment process.
     * <p>
     * Note that the Attribute name is case-sensitive
     * "deploymentDescriptor" as defined by JSR 77.
     */
    @ManagedAttribute
    String getdeploymentDescriptor();


    /**
     * The J2EEServer this module is deployed on.
     * Get the ObjectNames, as String.
     * <p>
     * Note that the Attribute name is case-sensitive
     * "server" as defined by JSR 77.
     *
     * @return the ObjectName of the server, as a String
     */
    @ManagedAttribute
    String getserver();


    @ManagedAttribute
    @Description("Get the ObjectName of the corresponding config MBean, if any")
    ObjectName getCorrespondingConfig();
}
