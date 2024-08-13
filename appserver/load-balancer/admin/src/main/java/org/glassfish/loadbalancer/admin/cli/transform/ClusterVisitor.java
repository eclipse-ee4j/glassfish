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

package org.glassfish.loadbalancer.admin.cli.transform;

import org.glassfish.loadbalancer.admin.cli.beans.Cluster;
import org.glassfish.loadbalancer.admin.cli.beans.WebModule;
import org.glassfish.loadbalancer.admin.cli.reader.api.BaseReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.ClusterReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.HealthCheckerReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.InstanceReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.WebModuleReader;

/**
 * Provides transform capabilites for cluster
 *
 * @author Satish Viswanatham
 */
public class ClusterVisitor implements Visitor {

    // ------ CTOR ------
    public ClusterVisitor(Cluster c) {
        _c = c;
    }

    /**
     * Visit reader class
     */
    @Override
    public void visit(BaseReader br) throws Exception {
        // FIXME, make as assert here about no class cast exception
        if (br instanceof ClusterReader) {
            ClusterReader cRdr = (ClusterReader) br;
            _c.setName(cRdr.getName());
            _c.setPolicy(cRdr.getLbPolicy());
            _c.setPolicyModule(cRdr.getLbPolicyModule());
            InstanceReader[] iRdrs = null;
            iRdrs = cRdr.getInstances();

            if ((iRdrs != null) && (iRdrs.length > 0)) {
                boolean[] values = new boolean[iRdrs.length];
                // XXX check if setting to true is required and is ok.
                for (int i = 0; i < iRdrs.length; i++) {
                    values[i] = true;
                }
                _c.setInstance(values);
                for (int i = 0; i < iRdrs.length; i++) {
                    iRdrs[i].accept(new InstanceVisitor(_c, i));
                }
            }

            HealthCheckerReader hcRdr = cRdr.getHealthChecker();

            if (hcRdr != null) {
                hcRdr.accept(new HealthCheckerVisitor(_c));
            }

            WebModuleReader[] wRdrs = cRdr.getWebModules();

            if ((wRdrs != null) && (wRdrs.length > 0)) {
                WebModule[] wMods = new WebModule[wRdrs.length];
                for (int i = 0; i < wRdrs.length; i++) {
                    wMods[i] = new WebModule();
                    wRdrs[i].accept(new WebModuleVisitor(wMods[i], _c));
                }
                _c.setWebModule(wMods);
            }
        }
    }
    //--- PRIVATE VARS ----
    Cluster _c = null;
}
