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
 * Base class for all domain config validators. Validates the non-null ness of a domain config entry and its type.
 */
public class Validator {
    /**
     * i18n strings manager object
     */
    private static final StringManager strMgr = StringManager.getManager(Validator.class);

    /**
     * The accepted type of an entry.
     */
    private final Class type;

    /**
     * The name of an entry that is used in case of validation error.
     */
    private final String name;

    /**
     * Constructs new Validator object.
     *
     * @param name Name of an entry that is used in case of validation errors. If the name is null "" is used instead.
     * @param type
     */
    public Validator(String name, Class type) {
        this.name = (name != null) ? name : "";
        this.type = (type != null) ? type : java.lang.Object.class;
    }

    /**
     * Returns the name of the entry.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks the validity of the given value for the entry. This method does basic checks such as null ness & type.
     *
     * @param obj
     * @Throws InvalidConfigException
     */
    public void validate(Object obj) throws InvalidConfigException {
        if (obj == null) {
            throw new InvalidConfigException(strMgr.getString("validator.invalid_value", getName(), null));
        }
        Class c = obj.getClass();
        if (!type.isAssignableFrom(c)) {
            throw new InvalidConfigException(strMgr.getString("validator.invalid_type", getName(), type.getName(), c.getName()));
        }
    }
}
