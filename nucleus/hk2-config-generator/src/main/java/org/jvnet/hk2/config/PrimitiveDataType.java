/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import org.jvnet.hk2.annotations.Service;

/** Represents a Java primitive (and its wrapper) data type. Not all Java primitives
 *  are relevant from a configuration standpoint.
 * @see DataType
 * @see DomDocument#PRIMS
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since hk2 0.3.10
 */
public final class PrimitiveDataType implements DataType {

    private final String realType;
    PrimitiveDataType(String realType) {
        assert DomDocument.PRIMS.contains(realType) : "This class can't validate: " + realType;
        this.realType = realType;
    }

    public void validate(String value) throws ValidationException {
        if (value.startsWith("${") && value.endsWith("}")) //it's a token
          return;
        boolean match = false;
        if ("int".equals(realType) || "java.lang.Integer".equals(realType))
            match = representsInteger(value);
        else if ("boolean".equals(realType) || "java.lang.Boolean".endsWith(realType))
            match = representsBoolean(value);
        else if ("char".equals(realType) || "java.lang.Character".equals(realType))
            representsChar(value);
        //no need for else as we are asserting it in the constructor
        if (!match) {
            String msg = "This value: " + value + " is not of type: " + realType + ", validation failed";
            throw new ValidationException(msg);
        }
    }

    private static boolean representsBoolean(String value) {
        boolean isBoolean = "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
        return (isBoolean);
    }
    private static boolean representsChar(String value) {
            if (value.length() == 1)
                return true;
            return false;
    }
    private static boolean representsInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch(NumberFormatException ne) {
            return false;
        }
    }

}
