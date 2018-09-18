/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.cdi.transaction;

import javax.transaction.Transactional;

/**
 * User: paulparkinson
 * Date: 5/3/13
 * Time: 10:46 AM
 */
public class BeanMethodLevelAll {

@javax.transaction.Transactional(value = Transactional.TxType.MANDATORY)
    public String fooMANDATORY() {
      return "In " + this + ".foo()";
    }

@javax.transaction.Transactional(value = Transactional.TxType.NEVER)
    public String fooNEVER() {
      return "In " + this + ".foo()";
    }

@javax.transaction.Transactional(value = Transactional.TxType.NOT_SUPPORTED)
    public String fooNOT_SUPPORTED() {
      return "In " + this + ".foo()";
    }

@javax.transaction.Transactional(value = Transactional.TxType.REQUIRED)
    public String fooREQUIRED() {
      return "In " + this + ".foo()";
    }

@javax.transaction.Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public String fooREQUIRES_NEW() {
      return "In " + this + ".foo()";
    }

@javax.transaction.Transactional(value = Transactional.TxType.SUPPORTS)
    public String fooSUPPORTS() {
      return "In " + this + ".foo()";
    }
}
