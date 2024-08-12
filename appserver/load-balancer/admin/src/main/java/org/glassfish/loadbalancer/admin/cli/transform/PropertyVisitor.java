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

import org.glassfish.loadbalancer.admin.cli.beans.Property;
import org.glassfish.loadbalancer.admin.cli.reader.api.BaseReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.PropertyReader;

/**
 * Provides transform capabilites for properties
 *
 * @author Satish Viswanatham
 */
public class PropertyVisitor implements Visitor {

    // ------ CTOR ------
    public PropertyVisitor(Property prop) {
        _prop = prop;
    }

    /**
     * Visit reader class
     */
    @Override
    public void visit(BaseReader br) throws Exception {
        // FIXME, make as assert here about no class cast exception
        if (br instanceof PropertyReader) {
            PropertyReader pRdr = (PropertyReader) br;
            _prop.setName(pRdr.getName());
            _prop.setValue(pRdr.getValue());
            _prop.setDescription(pRdr.getDescription());
        }
    }
    //--- PRIVATE VARS ----
    Property _prop = null;
}
