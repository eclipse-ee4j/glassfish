/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

/**
 * HK2 has an Injection Manager.  CommandRunner makes an instance of this Injection
 * Manager and overrides/overrides some methods.  Now we throw an Exception out.  If it
 * is a ComponentException and if the field is optional -- HK2 swallows the
 * Exception.
 * So, instead, we throw this RuntimeException and HK2 will propagate it back as
 * a wrapped Exception.
 * Then we look at the cause and pull out the real error message.
 * @author bnevins
 */
class CommandNotFoundException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    CommandNotFoundException(String msg) {
        super(msg);
    }

}
