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

import org.glassfish.loadbalancer.admin.cli.reader.api.BaseReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.WebModuleReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.IdempotentUrlPatternReader;

import org.glassfish.loadbalancer.admin.cli.beans.Cluster;
import org.glassfish.loadbalancer.admin.cli.beans.WebModule;
import org.netbeans.modules.schema2beans.AttrProp;

/**
 * Provides transform capabilites for web module
 *
 * @author Satish Viswanatham
 */
public class WebModuleVisitor implements Visitor {

    // ------ CTOR ------
    public WebModuleVisitor(WebModule w, Cluster c) {
        _w = w;
        _c = c;
    }

    /**
     * Visit reader class
     */
    @Override
    public void visit(BaseReader br) throws Exception {
        // FIXME, make as assert here about no class cast exception
        if (br instanceof BaseReader) {
            WebModuleReader wRdr = (WebModuleReader) br;

            _w.setContextRoot(wRdr.getContextRoot());

            String url = wRdr.getErrorUrl();
            if ((url != null) && (!"".equals(url))) {
                // XXX start of bug fix for 6171814
                _c.createAttribute(Cluster.WEB_MODULE, "error-url", "ErrorUrl",
                        AttrProp.CDATA, null, "");
                // XXX end of bug fix for 6171814
                _w.setErrorUrl(wRdr.getErrorUrl());
            }

            _w.setEnabled(Boolean.toString(wRdr.getLbEnabled()));

            _w.setDisableTimeoutInMinutes(wRdr.getDisableTimeoutInMinutes());

            IdempotentUrlPatternReader[] iRdrs = wRdr.getIdempotentUrlPattern();

            if ((iRdrs != null) && (iRdrs.length > 0)) {
                for (int i = 0; i < iRdrs.length; i++) {
                    iRdrs[i].accept(new IdempotentUrlPatternVisitor(_w, i));
                }
            }
        }
    }
    //--- PRIVATE VARS ----
    WebModule _w = null;
    Cluster _c = null;
}
