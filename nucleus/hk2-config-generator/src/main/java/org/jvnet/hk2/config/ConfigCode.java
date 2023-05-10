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

import java.beans.PropertyVetoException;

/**
 * Allows multiple object as part of the transaction but requires manual casting.
 *
 * @see SingleConfigCode Single Oject equivalent
 *
 * @author Jerome Dochez
 */
public interface ConfigCode {

    /**
     * Runs the following command passing the configration object. The code will be run
     * within a transaction, returning true will commit the transaction, false will abort
     * it.
     *
     * @param params is the list of configuration objects protected by the transaction
     * @return true if the changes on param should be commited or false for abort.
     * @throws PropertyVetoException if the changes cannot be applied
     * to the configuration
     */
    Object run(ConfigBeanProxy... params) throws PropertyVetoException, TransactionFailure;
}
