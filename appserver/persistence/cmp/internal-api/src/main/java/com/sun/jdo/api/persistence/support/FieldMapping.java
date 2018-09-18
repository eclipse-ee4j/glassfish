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

package com.sun.jdo.api.persistence.support;

/**
 * FieldMapping inteface is information of a column element for
 * external user.
 * @author Shing Wai Chan
 */
public interface FieldMapping {
    /**
     * This method will return int corresponding to java.sql.Types.
     */
    public int getColumnType();

    /**
     * This method will return the name of the column.
     */
    public String getColumnName();

    /**
     * This method return -1 if column length is unknown.
     */
    public int getColumnLength();
}
