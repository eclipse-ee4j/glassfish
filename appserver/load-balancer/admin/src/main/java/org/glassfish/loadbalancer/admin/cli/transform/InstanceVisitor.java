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
import org.glassfish.loadbalancer.admin.cli.reader.api.BaseReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.InstanceReader;

/**
 * Provides transform capabilites for instance
 *
 * @author Satish Viswanatham
 */
public class InstanceVisitor implements Visitor {

    // ------ CTOR ------
    public InstanceVisitor(Cluster c, int i) {
        _c = c;
        _i = i;
    }

    /**
     * Visit reader class
     */
    @Override
    public void visit(BaseReader br) throws Exception{
        // FIXME, make as assert here about no class cast exception
        if (br instanceof InstanceReader) {
            InstanceReader iRdr = (InstanceReader) br;
            _c.setInstanceName(_i, iRdr.getName());
            _c.setInstanceEnabled(_i, "" + iRdr.getLbEnabled());
            _c.setInstanceDisableTimeoutInMinutes(_i,
                    iRdr.getDisableTimeoutInMinutes());
            _c.setInstanceListeners(_i, iRdr.getListeners());
            _c.setInstanceWeight(_i, iRdr.getWeight());
        }
    }

    //--- PRIVATE VARS ----
    Cluster _c = null;
    int _i = 0;
}
