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

/*
 * ConcurrencyDBExplicit.java
 *
 * Created on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql.concurrency;

import com.sun.jdo.spi.persistence.support.sqlstore.sql.RetrieveDescImpl;
import com.sun.jdo.spi.persistence.support.sqlstore.sql.generator.SelectQueryPlan;

/**
 */
public class ConcurrencyDBExplicit extends ConcurrencyDBNative {

    public void select(SelectQueryPlan plan) {
        // Save the info that update lock is required in the queryplan.
        // This info would be used by SQLStoreManger::retrieve()
        // to process distinct manually if required.
        plan.options = plan.options | RetrieveDescImpl.OPT_FOR_UPDATE;
    }

}
