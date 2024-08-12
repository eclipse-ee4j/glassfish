/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.deployment.admin;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;

/**
 * Records information shared between DeployCommand and PostDeployCommand (a
 * supplemental command that is run after deployment on the DAS).
 *
 * @author Tim Quinn
 */
public class DeployCommandSupplementalInfo implements Serializable{

    private transient ExtendedDeploymentContext dc = null;
    private List<String> previousTargets = null;
    private Collection<? extends AccessCheck> accessChecks = null;

    public void setDeploymentContext(final ExtendedDeploymentContext dc) {
        this.dc = dc;
        /*
         * Save the previous targets (if any), because the deploy command
         * processing will clear the transient app metadata which is
         * where the previous targets are stored.  The previous targets are used
         * during redeployment if the target is "domain" which means "to all
         * targets where the app is already deployed."
         */
        if (dc != null) {
            previousTargets = dc.getTransientAppMetaData("previousTargets", List.class);
        }
    }

    public void setAccessChecks(final Collection<? extends AccessCheck> accessChecks) {
        this.accessChecks = accessChecks;
    }

    public Collection<? extends AccessCheck> getAccessChecks() {
        return accessChecks;
    }

    public ExtendedDeploymentContext deploymentContext() {
        return dc;
    }

    public List<String> previousTargets() {
        return previousTargets;
    }
}
