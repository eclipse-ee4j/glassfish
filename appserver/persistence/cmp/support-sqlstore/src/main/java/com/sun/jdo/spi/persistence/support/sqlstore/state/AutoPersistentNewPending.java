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
 *  AutoPersistentNewPending.java    March 10, 2000    Steffi Rauschenbach
 */

package com.sun.jdo.spi.persistence.support.sqlstore.state;

import com.sun.jdo.api.persistence.support.JDOUserException;
import com.sun.jdo.spi.persistence.support.sqlstore.ActionDesc;

import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

public class AutoPersistentNewPending extends AutoPersistentNew {
    /**
     * I18N message handler
     */
    private final static ResourceBundle messages = I18NHelper.loadBundle(
            "com.sun.jdo.spi.persistence.support.sqlstore.Bundle",  // NOI18N
            AutoPersistentNewPending.class.getClassLoader());

    public AutoPersistentNewPending() {
        super();

        updateAction = ActionDesc.LOG_NOOP;

        stateType = AP_NEW_PENDING;
    }

    public LifeCycleState transitionDeletePersistent() {
        throw new JDOUserException(I18NHelper.getMessage(messages,
                "jdo.lifecycle.deleted.accessField")); // NOI18N
    }
}










