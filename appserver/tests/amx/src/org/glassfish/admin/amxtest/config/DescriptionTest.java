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

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.config.Description;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.Set;


/**
 */
public final class DescriptionTest
        extends AMXTestBase {
    public DescriptionTest() {
    }

    public void
    testGetSetDescription() {
        final Set<Description> all = getTestUtil().getAllAMX(Description.class);

        for (final Description d : all) {
            final String description = d.getDescription();

            d.setDescription(description == null ? "test" : description);
        }
    }

}


