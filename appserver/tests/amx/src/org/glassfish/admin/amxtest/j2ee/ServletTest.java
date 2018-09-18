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

package org.glassfish.admin.amxtest.j2ee;

import com.sun.appserv.management.j2ee.Servlet;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.Set;

/**
 Test serialization on the AMX Stats/Statistics classes which travel
 from server to client.
 */
public final class ServletTest
        extends AMXTestBase {
    public ServletTest() {
    }

    public void
    testFindMappings() {
        final Set<Servlet> servlets = getTestUtil().getAllAMX(Servlet.class);

        for (final Servlet servlet : servlets) {
            final String[] mappings = servlet.findMappings();

            servlet.getClassLoadTime();
            servlet.getEngineName();
            servlet.getLoadTime();
            servlet.getModelerType();
        }
    }

}

