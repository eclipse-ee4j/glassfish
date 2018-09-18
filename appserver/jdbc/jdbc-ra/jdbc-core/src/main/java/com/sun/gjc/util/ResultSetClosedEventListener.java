/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.gjc.util;

import java.sql.SQLException;

/**
 * Object closed listener that will be used to intimate listeners to perform
 * operations after an object is closed.
 *
 * @author Shalini M
 */
public interface ResultSetClosedEventListener {

    /**
     * Used to perform operations like statement closeOnCompletion when the
     * result set object is closed.
     */
    public void resultSetClosed() throws SQLException;
}
