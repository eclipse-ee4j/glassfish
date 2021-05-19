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

package com.sun.enterprise.admin.servermgmt;

import com.sun.enterprise.util.i18n.StringManager;

/**
 * Validator class for domain config entries whose type is java.lang.String.
 */
public class StringValidator extends Validator {
    /**
     * i18n strings manager object
     */
    private static final StringManager strMgr = StringManager.getManager(StringValidator.class);

    /**
     * Constructs new StringValidator object.
     *
     * @param name
     */
    public StringValidator(String name) {
        super(name, java.lang.String.class);
    }

    /**
     * Validates the given value for the given entry. This method first invokes its superclass's validate method and then
     * checks additional string validations such as 0 length.
     *
     * @throws InvalidConfigException
     */
    public void validate(Object str) throws InvalidConfigException {
        super.validate(str);
        int length = ((String) str).length();
        if (length == 0) {
            throw new InvalidConfigException(strMgr.getString("validator.invalid_value", getName(), str));
        }
    }
}
