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

import com.sun.enterprise.deployment.types.ConcurrencyContextType;
import com.sun.enterprise.deployment.types.CustomContextType;
import com.sun.enterprise.deployment.types.StandardContextType;

import jakarta.enterprise.concurrent.ContextServiceDefinition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author David Matejcek
 */
final class CfgParser {

    private CfgParser() {
        // utility class
    }


    static Set<ConcurrencyContextType> parseContextInfo(String contextInfo, String contextInfoEnabled) {
        if (contextInfo == null || !Boolean.TRUE.toString().equalsIgnoreCase(contextInfoEnabled)) {
            return Set.of(StandardContextType.values());
        }
        List<String> strings = Arrays.asList(contextInfo.split(","));
        return standardize(strings);
    }


    /**
     * Converts strings from annotations and xml to enums.
     * If the provided context name is not supported, it is returned without changes.
     *
     * @param contexts
     * @return set of enum names.
     */
    public static Set<ConcurrencyContextType> standardize(Iterable<String> contexts) {
        Set<ConcurrencyContextType> result = new HashSet<>();
        for (String input : contexts) {
            final String context = input.trim();
            if (context.isEmpty()) {
                continue;
            }
            if (ContextServiceDefinition.TRANSACTION.equalsIgnoreCase(context)) {
                result.add(StandardContextType.WorkArea);
            }
            if (ContextServiceDefinition.APPLICATION.equals(context)) {
                result.add(StandardContextType.Classloader);
                result.add(StandardContextType.JNDI);
            }
            StandardContextType contextType = StandardContextType.parse(context);
            result.add(contextType == null ? new CustomContextType(context) : contextType);
        }
        return result;
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
