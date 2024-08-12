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

import org.glassfish.loadbalancer.admin.cli.beans.WebModule;
import org.glassfish.loadbalancer.admin.cli.reader.api.BaseReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.IdempotentUrlPatternReader;

/**
 * Provides transform capabilites for IdempotentUrlPattern
 *
 * @author Satish Viswanatham
 */
public class IdempotentUrlPatternVisitor implements Visitor {

    // ------ CTOR ------
    public IdempotentUrlPatternVisitor(WebModule m, int i) {
        _m = m;
        _i = i;
    }

    /**
     * Visit reader class
     */
    @Override
    public void visit(BaseReader br) throws Exception {
        // FIXME, make as assert here about no class cast exception
        if (br instanceof IdempotentUrlPatternReader) {
            IdempotentUrlPatternReader iRdr = (IdempotentUrlPatternReader) br;
            _m.addIdempotentUrlPattern(true);
            _m.setAttributeValue(WebModule.IDEMPOTENT_URL_PATTERN, _i,
                    URL_PATTERN, iRdr.getUrlPattern());
            _m.setAttributeValue(WebModule.IDEMPOTENT_URL_PATTERN, _i, RETRIES,
                    iRdr.getNoOfRetries());
        }
    }
    //--- PRIVATE VARS ----
    WebModule _m = null;
    int _i = 0;
    private static final String URL_PATTERN = "UrlPattern";    //NOI18N
    private static final String RETRIES = "NoOfRetries";     //NOI18N
}
