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

import static com.sun.enterprise.deployment.annotation.handlers.StandardContextType.Classloader;
import static com.sun.enterprise.deployment.annotation.handlers.StandardContextType.JNDI;
import static com.sun.enterprise.deployment.annotation.handlers.StandardContextType.Security;
import static com.sun.enterprise.deployment.annotation.handlers.StandardContextType.WorkArea;

/**
 * @author David Matejcek
 */
public final class ContextInfoParser {

    private ContextInfoParser() {
        // utility class
    }


    public static Set<String> parseContextInfo(String contextInfo, boolean isContextInfoEnabled) {
        Set<String> contextTypeArray = new HashSet<>();
        if (contextInfo == null) {
            // by default, if no context info is passed, we propagate all context types
            // FIXME: and remaining?
            contextTypeArray.add(Classloader.name());
            contextTypeArray.add(JNDI.name());
            contextTypeArray.add(Security.name());
            contextTypeArray.add(WorkArea.name());
        } else if (isContextInfoEnabled) {
            String[] strings = contextInfo.split(",");
            for (String string : strings) {
                String contextType = string.trim();
                StandardContextType standardContextType = StandardContextType.parse(contextType);
                contextTypeArray.add(standardContextType == null ?  contextType : standardContextType.name());
            }
        }
        return contextTypeArray;
    }
}
