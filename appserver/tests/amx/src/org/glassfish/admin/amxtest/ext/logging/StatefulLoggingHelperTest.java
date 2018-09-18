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

package org.glassfish.admin.amxtest.ext.logging;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.ext.logging.LogQueryResult;
import com.sun.appserv.management.ext.logging.Logging;
import com.sun.appserv.management.helper.StatefulLoggingHelper;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.Set;


/**
 Test the StatefulLoggingHelper.
 */
public final class StatefulLoggingHelperTest
        extends AMXTestBase {
    public StatefulLoggingHelperTest() {
    }

    final Set<Logging>
    getAllLogging() {
        return getQueryMgr().queryJ2EETypeSet(XTypes.LOGGING);
    }

    public StatefulLoggingHelper
    createHelper(final Logging logging) {
        return new StatefulLoggingHelper(logging);
    }

    public void
    testCreate() {
        final Set<Logging> loggings = getAllLogging();

        for (final Logging logging : loggings) {
            assert (createHelper(logging) != null);
        }
    }

    private void
    validateResult(final LogQueryResult result) {
        assert (result != null);
        assert (result.getFieldNames() != null);
        assert (result.getEntries() != null);
    }


    public void
    testQuery() {
        final long start = now();

        final Set<Logging> loggings = getAllLogging();

        for (final Logging logging : loggings) {
            assert (logging != null);
            final StatefulLoggingHelper helper = createHelper(logging);

            final LogQueryResult result = helper.query();
            validateResult(result);
        }

        printElapsed("testQuery", start);
    }

}





















