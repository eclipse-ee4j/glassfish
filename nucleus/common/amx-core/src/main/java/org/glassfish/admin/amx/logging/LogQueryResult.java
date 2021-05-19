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

package org.glassfish.admin.amx.logging;

import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
        Interface which can be applied over the CompositeData returned
        from {@link LogQuery#queryServerLog}.

        @since AS 9.0
        @see LogQueryEntry
        @see LogQuery
 */
@Taxonomy(stability = Stability.EXPERIMENTAL)
public interface LogQueryResult
{
    /**
        Get field names for each field of a {@link LogQueryEntry}.
        Log entries in the server <i>log file</i> are of the form:<br>
        <code><pre>[#|DATE|LEVEL|PRODUCT_NAME|MODULE|NAME_VALUE_PAIRS|MESSAGE|#]</pre></code><br>
        The metadata contains most of these fields, but does <b not> contain
        the PRODUCT_NAME or '#' columns.
     */
    public String[]    getFieldNames();

    /**
        Return all log entries found by the query <b>not including the field headers</b>.
     */
    public LogQueryEntry[]  getEntries();
}






