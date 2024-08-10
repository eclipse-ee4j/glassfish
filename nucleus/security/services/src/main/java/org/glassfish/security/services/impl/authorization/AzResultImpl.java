/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.impl.authorization;

import java.text.MessageFormat;

import org.glassfish.security.services.api.authorization.AzObligations;
import org.glassfish.security.services.api.authorization.AzResult;

public final class AzResultImpl implements AzResult {

    private final Decision decision;
    private final Status status;
    private final AzObligations obligations;

    public AzResultImpl(Decision d, Status s, AzObligations o) {
        decision = d;
        status = s;
        obligations = o;
    }

    @Override
    public Decision getDecision() {
        return decision;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public AzObligations getObligations() {
        return obligations;
    }


    @Override
    public String toString() {
        return MessageFormat.format("status={0}, decision=[1]",
            status, decision);
    }
}
