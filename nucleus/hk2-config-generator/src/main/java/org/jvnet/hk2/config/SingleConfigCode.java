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
 * SimpleConfigCode is code snippet that can be used to apply some configuration
 * changes to one configuration object.
 *
 * For example say, you need to modify the HttpListener config object with a new
 * port number, you can do so by writing the following code snippet.
 *
 * <pre>
 * {@code
 * new SingleConfigCode<HttpListener>() {
 *     public boolean run(HttpListener httpListener) throws PropertyVetoException {
 *         httpListener.setPort("8989");
 *         return true;
 *     }
 * };
 * }
 * </pre>
 * This new SingleConfigCode can then be used with in the ConfigSupport utilities to
 * run this code within a Transaction freeing the developer to know/care about Transaction
 * APIs and semantics.
 *
 * @author Jerome Dochez
 */
public interface SingleConfigCode<T extends ConfigBeanProxy> {

    /**
     * Runs the following command passing the configration object. The code will be run
     * within a transaction, returning true will commit the transaction, false will abort
     * it.
     *
     * @param param is the configuration object protected by the transaction
     * @return any object that should be returned from within the transaction code
     * @throws PropertyVetoException if the changes cannot be applied
     * to the configuration
     */
    Object run(T param) throws PropertyVetoException, TransactionFailure;
}
