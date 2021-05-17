/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.config.support.datatypes;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.DataType;
import org.jvnet.hk2.config.ValidationException;

/**
 * Represents an integer from 0 to Integer.MAX_VALUE. It's modeled as a functional class.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
@Service
public class NonNegativeInteger implements DataType {

    /**
     * Validates the value as a non-negative integer.
     *
     * @param value
     * @throws org.jvnet.hk2.config.ValidationException
     */
    public void validate(String value) throws ValidationException {
        if (value == null)
            throw new ValidationException("null value is not of type NonNegativeInteger");
        if (isTokenized(value))
            return; //a token is always valid
        try {
            long number = Long.parseLong(value);
            if (number < 0 || number > Integer.MAX_VALUE) {
                String msg = "value: " + number + " not applicable for NonNegativeInteger [0, " + Integer.MAX_VALUE + "] data type";
                throw new ValidationException(msg);
            }
        } catch (NumberFormatException e) {
            throw new ValidationException(e);
        }
    }

    /*package*/
    static boolean isTokenized(String value) {
        if (value != null && value.startsWith("${") && value.endsWith("}"))
            return true;
        return false;
    }
}
