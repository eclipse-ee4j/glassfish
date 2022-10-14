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

package com.sun.enterprise.deployment.annotation.handlers;

import jakarta.enterprise.concurrent.ContextServiceDefinition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author David Matejcek
 */
public enum StandardContextType {
    /** Same as {@link ContextServiceDefinition#ALL_REMAINING}*/
    Remaining,
    /** Subtype of {@link ContextServiceDefinition#APPLICATION} */
    Classloader,
    /** Subtype of {@link ContextServiceDefinition#APPLICATION} */
    JNDI,
    /** Same as {@link ContextServiceDefinition#SECURITY}*/
    Security,
    /** Same as {@link ContextServiceDefinition#TRANSACTION}*/
    WorkArea,
    ;

    public static Set<String> names() {
        return Arrays.stream(StandardContextType.values()).map(Enum::name).collect(Collectors.toSet());
    }


    public static StandardContextType parse(String name) {
        for (StandardContextType ctxType : StandardContextType.values()) {
            if (ctxType.name().equalsIgnoreCase(name)) {
                return ctxType;
            }
        }
        // obsoleted constants may be still used.
        if ("NAMING".equalsIgnoreCase(name)) {
            return JNDI;
        }
        if ("CLASSLOADING".equalsIgnoreCase(name)) {
            return Classloader;
        }
        return null;
    }


    public static Set<String> standardize(Set<String> contexts) {
        Set<String> result = new HashSet<>();
        for (String input : contexts) {
            if (ContextServiceDefinition.TRANSACTION.equalsIgnoreCase(input)) {
                result.add(WorkArea.name());
            }
            if (ContextServiceDefinition.APPLICATION.equals(input)) {
                result.add(Classloader.name());
                result.add(JNDI.name());
            }
            StandardContextType contextType = parse(input);
            result.add(contextType == null ? input : contextType.name());
        }
        return result;
    }
}
