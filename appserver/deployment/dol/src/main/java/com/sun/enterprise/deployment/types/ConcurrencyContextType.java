/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.types;

import jakarta.enterprise.concurrent.spi.ThreadContextProvider;

import java.io.Serializable;

/**
 * Context type can be standard or provided by some custom {@link ThreadContextProvider}.
 *
 * @author David Matejcek
 */
public interface ConcurrencyContextType extends Serializable {

    /**
     * @return name of the context type supported by GlassFish or by the application's
     *         {@link ThreadContextProvider}.
     */
    String name();
}
