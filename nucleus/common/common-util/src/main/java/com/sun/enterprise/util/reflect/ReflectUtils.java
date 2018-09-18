/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util.reflect;

import java.lang.reflect.Method;

/**
 * Place to put utility methods that the JVM doesn't supply
 * @author Byron Nevins
 */
public final class ReflectUtils {
    /**
     *
     * @param m1 Method to compare
     * @param m2 Method to compare
     * @return null if they have the same signature.  A String describing the differences
     * if they have different signatures.
     */
    public static String equalSignatures(Method m1, Method m2) {
        StringBuilder sb = new StringBuilder();

        if (!m1.getReturnType().equals(m2.getReturnType())) {
            sb.append(Strings.get("return_type_mismatch", m1.getReturnType(), m2.getReturnType()));
            sb.append("  ");
        }

        Class<?>[] types1 = m1.getParameterTypes();
        Class<?>[] types2 = m2.getParameterTypes();

        if (types1.length != types2.length) {
            sb.append(Strings.get("parameter_number_mismatch", types1.length, types2.length));
        }
        else { // don't want to go in here if the lengths don't match!!
            for (int i = 0; i < types1.length; i++) {
                if (!types1[i].equals(types2[i])) {
                    sb.append(Strings.get("parameter_type_mismatch", i, types1[i], types2[i]));
                    sb.append("  ");
                }
            }
        }

        if (sb.length() == 0)
            return null;

        sb.append('\n').append(m1.toGenericString());
        sb.append('\n').append(m2.toGenericString());
        return sb.toString();
    }
}
