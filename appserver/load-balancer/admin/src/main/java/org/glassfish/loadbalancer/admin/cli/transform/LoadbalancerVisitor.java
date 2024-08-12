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
import org.glassfish.loadbalancer.admin.cli.beans.Loadbalancer;
import org.glassfish.loadbalancer.admin.cli.beans.Property;
import org.glassfish.loadbalancer.admin.cli.reader.api.BaseReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.ClusterReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.LoadbalancerReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.PropertyReader;

/**
 * Provides transform capabilites for LB
 *
 * @author Satish Viswanatham
 */
public class LoadbalancerVisitor implements Visitor {

    // ------ CTOR ------
    public LoadbalancerVisitor(Loadbalancer lb) {
        _lb = lb;
    }

    /**
     * Visit reader class
     */
    @Override
    public void visit(BaseReader br) throws Exception {
        // FIXME, make as assert here about no class cast exception
        if (br instanceof LoadbalancerReader) {
            LoadbalancerReader lbRdr = (LoadbalancerReader) br;

            PropertyReader[] pRdrs = lbRdr.getProperties();

            if ((pRdrs != null) && (pRdrs.length > 0)) {
                Property[] props = new Property[pRdrs.length];
                for (int i = 0; i < pRdrs.length; i++) {
                    props[i] = new Property();
                    pRdrs[i].accept(new PropertyVisitor(props[i]));
                }
                _lb.setProperty2(props);
            }

            ClusterReader[] cRdrs = lbRdr.getClusters();

            if ((cRdrs != null) && (cRdrs.length > 0)) {
                Cluster[] cls = new Cluster[cRdrs.length];
                for (int i = 0; i < cRdrs.length; i++) {
                    cls[i] = new Cluster();
                    cRdrs[i].accept(new ClusterVisitor(cls[i]));
                }
                _lb.setCluster(cls);
            }
        }
    }
    //--- PRIVATE VARS ----
    Loadbalancer _lb = null;
}
