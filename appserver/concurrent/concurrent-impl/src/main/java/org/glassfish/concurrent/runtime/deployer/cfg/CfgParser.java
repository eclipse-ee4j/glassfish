/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.runtime.deployer.cfg;

import com.sun.enterprise.deployment.annotation.handlers.StandardContextType;

import java.util.HashSet;
import java.util.Set;

/**
 * @author David Matejcek
 */
final class CfgParser {

    private CfgParser() {
        // utility class
    }


    static Set<String> parseContextInfo(String contextInfo, String contextInfoEnabled) {
        Set<String> contextTypeArray = new HashSet<>();
        if (contextInfo == null || !Boolean.TRUE.toString().equalsIgnoreCase(contextInfoEnabled)) {
            return StandardContextType.names();
        }
        String[] strings = contextInfo.split(",");
        for (String string : strings) {
            String contextType = string.trim();
            if (contextType.isEmpty()) {
                continue;
            }
            StandardContextType standardContextType = StandardContextType.parse(contextType);
            contextTypeArray.add(standardContextType == null ?  contextType : standardContextType.name());
        }
        return contextTypeArray;
    }


    static int parseInt(String strValue, int defaultValue) {
        if (strValue != null) {
            try {
                return Integer.parseInt(strValue);
            } catch (NumberFormatException e) {
                // ignore, just return default in this case
            }
        }
        return defaultValue;
    }


    static long parseLong(String strValue, long defaultValue) {
        if (strValue != null) {
            try {
                return Long.parseLong(strValue);
            } catch (NumberFormatException e) {
                // ignore, just return default in this case
            }
        }
        return defaultValue;
    }
}
