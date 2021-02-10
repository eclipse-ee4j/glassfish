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

package org.glassfish.api.jdbc;

/**
 * This interface can be implemented to provide a mechanism to trace the SQL trace record objects. SQL trace record
 * objects contain SQL statements executed by applications.
 *
 * @author Shalini M
 */
public interface SQLTraceListener {
    /**
     * Notify listeners with SQL trace information.
     *
     * @param record SQLTraceRecord that has information related to the SQL operation
     */
    void sqlTrace(SQLTraceRecord record);

}
