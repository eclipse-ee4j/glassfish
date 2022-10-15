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

package com.sun.enterprise.deployment.types;

import jakarta.enterprise.concurrent.ContextServiceDefinition;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enum for supported standard context types.
 *
 * @author David Matejcek
 * @see ContextServiceDefinition
 */
public enum StandardContextType implements ConcurrencyContextType {
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

    /**
     * @return all enum names.
     */
    public static Set<String> names() {
        return Arrays.stream(StandardContextType.values()).map(Enum::name).collect(Collectors.toSet());
    }


    /**
     * Case insensitive parsing of enum names.<br>
     * Accepts also obsoleted NAMING and CLASSLOADING values as they can be simply converted
     * to known types.
     *
     * @param name
     * @return parsed {@link StandardContextType} or null.
     */
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
}
