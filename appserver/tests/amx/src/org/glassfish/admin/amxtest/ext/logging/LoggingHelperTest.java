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
import com.sun.appserv.management.ext.logging.LogModuleNames;
import com.sun.appserv.management.ext.logging.LogQueryResult;
import com.sun.appserv.management.ext.logging.Logging;
import static com.sun.appserv.management.ext.logging.Logging.LOWEST_SUPPORTED_QUERY_LEVEL;
import static com.sun.appserv.management.ext.logging.Logging.SERVER_KEY;
import com.sun.appserv.management.helper.LoggingHelper;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.Set;


/**
 Test the LoggingHelper.
 */
public final class LoggingHelperTest
        extends AMXTestBase {
    public LoggingHelperTest() {
    }

    final Set<Logging>
    getAllLogging() {
        return getQueryMgr().queryJ2EETypeSet(XTypes.LOGGING);
    }

    public LoggingHelper
    createHelper(final Logging logging) {
        return new LoggingHelper(logging);
    }


    private void
    validateResult(final LogQueryResult result) {
        assert (result != null);
        assert (result.getFieldNames() != null);
        assert (result.getEntries() != null);
    }

    public void
    testQueryServerLogSingle() {
        final Set<Logging> loggings = getAllLogging();

        for (final Logging logging : loggings) {
            final LoggingHelper helper = createHelper(logging);
            final LogQueryResult result =
                    helper.queryServerLog(LOWEST_SUPPORTED_QUERY_LEVEL, "EJB");
            validateResult(result);
        }
    }


    public void
    testQueryServerLogLevelAndModules() {
        final Set<Logging> loggings = getAllLogging();

        for (final Logging logging : loggings) {
            final LoggingHelper helper = createHelper(logging);

            final LogQueryResult result =
                    helper.queryServerLog(LOWEST_SUPPORTED_QUERY_LEVEL,
                                          LogModuleNames.ALL_NAMES);
            validateResult(result);
        }
    }


    public void
    testQueryServerLogLevel() {
        final Set<Logging> loggings = getAllLogging();

        for (final Logging logging : loggings) {
            final LoggingHelper helper = createHelper(logging);

            final LogQueryResult result =
                    helper.queryServerLog(LOWEST_SUPPORTED_QUERY_LEVEL);
            validateResult(result);
        }
    }


    public void
    testQueryAllCurrent() {
        final Set<Logging> loggings = getAllLogging();

        for (final Logging logging : loggings) {
            final LoggingHelper helper = createHelper(logging);

            final LogQueryResult result = helper.queryAllCurrent();
            validateResult(result);
        }
    }


    private static final int HOUR_MILLIS = 60 * 60 * 1000;

    public void
    testQueryServerLogRecent() {
        final Set<Logging> loggings = getAllLogging();

        for (final Logging logging : loggings) {
            final LoggingHelper helper = createHelper(logging);

            final LogQueryResult result =
                    helper.queryServerLogRecent(HOUR_MILLIS);
            validateResult(result);
        }
    }


    public void
    testQueryServerLogRecentWithModules() {
        final Set<Logging> loggings = getAllLogging();

        for (final Logging logging : loggings) {
            final LoggingHelper helper = createHelper(logging);

            final LogQueryResult result =
                    helper.queryServerLogRecent(
                            HOUR_MILLIS, LogModuleNames.ALL_NAMES);
            validateResult(result);
        }
    }


    public void
    testQueryAllInFile() {
        final Set<Logging> loggings = getAllLogging();

        for (final Logging logging : loggings) {
            final LoggingHelper helper = createHelper(logging);

            final String[] names = logging.getLogFileNames(SERVER_KEY);
            for (final String name : names) {
                final LogQueryResult result = helper.queryAllInFile(name);
                validateResult(result);
            }
        }
    }

    public void
    testQueryAll() {
        final Set<Logging> loggings = getAllLogging();

        for (final Logging logging : loggings) {
            final LoggingHelper helper = createHelper(logging);

            final LogQueryResult[] results = helper.queryAll();
            for (final LogQueryResult result : results) {
                validateResult(result);
            }
        }
    }


}





















