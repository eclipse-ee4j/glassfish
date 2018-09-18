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

import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.DiagnosticServiceConfig;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.logging.Level;

/**
 */
public final class DiagnosticServiceConfigTest
        extends AMXTestBase {
    public DiagnosticServiceConfigTest() {
    }

    public void
    testCreateRemove() {
        final ConfigConfig config = getConfigConfig();

        // set defaults
        String computeChecksum = "" + true;
        String captureInstallLog = "" + true;
        String captureSystemInfo = "" + true;
        String captureHADBInfo = "" + true;
        String captureAppDD = "" + true;
        String verifyConfig = "" + true;
        String minLogLevel = Level.INFO.toString();
        String maxLongEntries = "10000";

        DiagnosticServiceConfig ds = config.getDiagnosticServiceConfig();
        if (ds != null) {
            // remember current settings
            computeChecksum = ds.getComputeChecksum();
            captureInstallLog = ds.getCaptureInstallLog();
            captureSystemInfo = ds.getCaptureSystemInfo();
            captureHADBInfo = ds.getCaptureHADBInfo();
            captureAppDD = ds.getCaptureAppDD();
            verifyConfig = ds.getVerifyConfig();
            minLogLevel = ds.getMinLogLevel();
            maxLongEntries = ds.getMaxLogEntries();

            config.removeDiagnosticServiceConfig();
            ds = null;
        }

        ds = config.createDiagnosticServiceConfig();
        config.removeDiagnosticServiceConfig();
        ds = null;

        // re-create it so one stays around
        ds = config.createDiagnosticServiceConfig();
        ds.setComputeChecksum(computeChecksum);
        ds.setCaptureInstallLog(captureInstallLog);
        ds.setCaptureSystemInfo(captureSystemInfo);
        ds.setCaptureHADBInfo(captureHADBInfo);
        ds.setCaptureAppDD(captureAppDD);
        ds.setVerifyConfig(verifyConfig);
        ds.setMinLogLevel(minLogLevel);
        ds.setMaxLogEntries(maxLongEntries);
    }

}


