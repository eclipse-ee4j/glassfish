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

package org.glassfish.admin.amxtest;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import org.glassfish.admin.amx.util.AMXDebugStuff;
import com.sun.appserv.management.ext.coverage.CoverageInfo;
import com.sun.appserv.management.ext.coverage.CoverageInfoDummy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 Analyze the CoverageInfo for AMX MBeans. Use only after tests
 have been run.
 */
public final class CoverageInfoAnalyzer {
    private final DomainRoot mDomainRoot;
    private final QueryMgr mQueryMgr;
    private final TestUtil mTestUtil;

    private final String NEWLINE;

    public CoverageInfoAnalyzer(final DomainRoot domainRoot) {
        mDomainRoot = domainRoot;
        mQueryMgr = domainRoot.getQueryMgr();
        mTestUtil = new TestUtil(domainRoot);

        final AMXDebugStuff debugRoot = mTestUtil.asAMXDebugStuff(mDomainRoot);
        if (debugRoot == null) {
            throw new RuntimeException("AMX-DEBUG/CoverageInfo is not enabled");
        }

        final CoverageInfo coverageInfo = debugRoot.getCoverageInfo();

        if (coverageInfo instanceof CoverageInfoDummy) {
            throw new IllegalArgumentException("Coverage disabled--add system property " +
                    "'-DAMX-DEBUG=true', then restart server");
        }

        NEWLINE = System.getProperty("line.separator");
    }

    private static final Set<String> IGNORE_METHODS =
            GSetUtil.newUnmodifiableStringSet(
                    "addNotificationListener(javax.management.NotificationListener",
                    "removeNotificationListener(javax.management.NotificationListener",
                    "getAMXDebug",
                    "setAMXDebug",
                    "enableAMXDebug",
                    "getImplString",
                    "enableCoverageInfo",
                    "clearCoverageInfo"
            );

    private static final Set<String> IGNORE_UNKNOWN =
            GSetUtil.newUnmodifiableStringSet(
                    "ContaineeJ2EETypes",
                    "eventProvider"
            );

    /**
     Certain methods will never be called via remote access due to
     the JMX implementation. removeNotificationListener() in particular
     is only invoked in one way by the MBeanServer in response to
     remote clients registering listeners.  Omit these cases, since
     they will never be invoked.
     */
    private void
    handleSpecialCases(final CoverageInfo coverageInfo) {
        final Set<String> notInvoked = coverageInfo.getOperationsNotInvoked();

        for (final String op : notInvoked) {
            for (final String prefix : IGNORE_METHODS) {
                if (op.startsWith(prefix)) {
                    coverageInfo.markAsInvoked(op);
                }
            }
        }

        // make a copy, we'll be modifying it
        final Set<String> unknown = coverageInfo.getUnknownAttributes().keySet();
        for (final String s : unknown) {
            if (s.startsWith("bogus") ||
                    IGNORE_UNKNOWN.contains(s)) {
                coverageInfo.ignoreUnknownAttribute(s);
            }
        }
    }


    public Map<String, CoverageInfo>
    getCoverage(final Set<AMX> candidates) {
        final Map<String, CoverageInfo> coverageMap = new HashMap<String, CoverageInfo>();

        for (final AMX amx : candidates) {
            final AMXDebugStuff debug = mTestUtil.asAMXDebugStuff(amx);
            final CoverageInfo coverageInfo = debug.getCoverageInfo();
            assert (coverageInfo != null);
            handleSpecialCases(coverageInfo);

            final String j2eeType = amx.getJ2EEType();
            final CoverageInfo existing = coverageMap.get(j2eeType);
            if (existing != null) {
                existing.merge(coverageInfo);
            } else {
                coverageMap.put(j2eeType, coverageInfo);
            }
        }

        return coverageMap;
    }

    public String
    getCoverageSummary() {
        final Set<AMX> amx = mTestUtil.getAllAMX();
        final Map<String, CoverageInfo> coverage = getCoverage(amx);

        final String[] j2eeTypes = GSetUtil.toStringArray(coverage.keySet());
        Arrays.sort(j2eeTypes);

        final String LINE_SEP = System.getProperty("line.separator");

        final StringBuilder builder = new StringBuilder();
        for (final String j2eeType : j2eeTypes) {
            final CoverageInfo info = coverage.get(j2eeType);

            final String infoString =
                    "Coverage for j2eeType = " + j2eeType +
                            ": " + (info.getFullCoverage() ? "100%" : "INCOMPLETE COVERAGE") +
                            LINE_SEP +
                            info.toString(false) + LINE_SEP + LINE_SEP;

            builder.append(infoString);
        }

        final String msg =
                "No AMX MBeans having the following types " +
                        "were ever present, and so were NEVER TESTED:" + NEWLINE;
        builder.append(createMissingString(msg));

        return builder.toString();
    }


    /**
     @return Set of j2eeTypes for which no MBeans exist
     */
    protected Set<String>
    findMissingJ2EETypes() {
        final Set<String> missing = new HashSet<String>();
/*
        missing.addAll( XTypesMapper.getInstance().getJ2EETypes() );
        missing.addAll( J2EETypesMapper.getInstance().getJ2EETypes() );

        missing.removeAll( mTestUtil.findRegisteredJ2EETypes() );

        final Set<ObjectName>   registered  =
            Observer.getInstance().getRegistrationListener().getRegistered();
        for( final ObjectName objectName : registered )
        {
            final String    j2eeType    = Util.getJ2EEType( objectName );
            if ( j2eeType != null )
            {
                missing.remove( j2eeType );
            }
        }
*/

        return missing;
    }

    protected void
    groupMissingJ2EETypes(
            final Set<String> allMissing,
            final Set<String> missingConfigs,
            final Set<String> missingMonitors,
            final Set<String> missingOthers
    ) {
        for (final String j2eeType : allMissing) {
            if (j2eeType.endsWith("Config")) {
                missingConfigs.add(j2eeType);
            } else if (j2eeType.endsWith("Monitor")) {
                missingMonitors.add(j2eeType);
            } else {
                missingOthers.add(j2eeType);
            }
        }
    }

    private String
    setToSortedString(
            final Set<String> s,
            final String delim) {
        final String[] a = GSetUtil.toStringArray(s);
        Arrays.sort(a);

        return StringUtil.toString(NEWLINE, (Object[]) a);
    }


    protected String
    createMissingString(final String msg) {
        String result = "";

        final Set<String> missing = findMissingJ2EETypes();
        if (missing.size() != 0) {
            final Set<String> missingConfig = new HashSet<String>();
            final Set<String> missingMonitors = new HashSet<String>();
            final Set<String> missingOthers = new HashSet<String>();

            groupMissingJ2EETypes(missing, missingConfig, missingMonitors, missingOthers);

            result = msg + NEWLINE +
                    "Config: " + NEWLINE +
                    setToSortedString(missingConfig, NEWLINE) + NEWLINE + NEWLINE +
                    "Monitor: " + NEWLINE +
                    setToSortedString(missingMonitors, NEWLINE) + NEWLINE + NEWLINE +
                    "J2EE/Other: " + NEWLINE +
                    setToSortedString(missingOthers, NEWLINE);
        }

        return result;
    }
}

















