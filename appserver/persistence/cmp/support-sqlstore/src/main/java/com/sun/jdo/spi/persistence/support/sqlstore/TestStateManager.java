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

package com.sun.jdo.spi.persistence.support.sqlstore;

/**
 * This interface is used to unit test the StateManager.
 */
public interface TestStateManager
{
    /**
     * This method is used to test if a field identified by fieldName
     * is loaded in memory. It returns true is the field is loaded and
     * false, otehrwise.
     */
    boolean testIsLoaded(String fieldName);

    /**
     * This method is used to test if a field identified by fieldNumber
     * is loaded in memory. It returns true is the field is loaded and
     * false otehrwise.
     */
    boolean testIsLoaded(int fieldNumber);

    /**
     * This method is used to determine if an instance is in the autopersistent
     * state. It returns true if the instance is autopersistence and false otherwise.
     */
    boolean testIsAutoPersistent();
}

