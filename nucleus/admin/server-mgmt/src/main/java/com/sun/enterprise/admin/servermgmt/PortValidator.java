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

/*
 * PortValidator.java
 *
 * Created on August 11, 2003, 7:44 PM
 */

package com.sun.enterprise.admin.servermgmt;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.net.NetUtils;

/**
 * This validator ensures that the specified port is not in use.
 *
 * @author kebbs
 */
public class PortValidator extends Validator {

    private static final StringManager _strMgr = StringManager.getManager(PortValidator.class);

    /** Creates a new instance of PortValidator */
    public PortValidator(String name) {
        super(name, java.lang.Integer.class);
    }

    /**
     * Validates the given Port.
     *
     * @param str Must be the absolute path of the File that will be validated.
     * @throws InvalidConfigException
     */
    public void validate(Object obj) throws InvalidConfigException {
        super.validate(obj);
        if (!NetUtils.isPortFree(((Integer) obj).intValue())) {
            throw new InvalidConfigException(_strMgr.getString("portValidator.in_use", obj));
        }
    }
}
