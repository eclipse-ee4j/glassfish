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
 * Indices into log record fields as returned by
 * {@link LogQuery#queryServerLog}. Also
 *
 * @since AppServer 9.0
 */
@Taxonomy(stability = Stability.EXPERIMENTAL)
public final class LogRecordFields {

    private LogRecordFields() {
    }

    /** Value is of class java.lang.Integer */
    public final static int RECORD_NUMBER_INDEX = 0;

    /** Value is of class java.util.Date */
    public final static int DATE_INDEX = 1;

    /** Value is of class java.lang.String */
    public final static int LEVEL_INDEX = 2;

    /** Value is of class java.lang.String */
    public final static int PRODUCT_NAME_INDEX = 3;

    /** Value is of class java.lang.Integer */
    public final static int MESSAGE_INDEX = 7;

    /** Value is of class java.lang.String */
    public final static int MESSAGE_ID_INDEX = 6; // need to extract from the message text

    /** Value is of class java.lang.String */
    public final static int MODULE_INDEX = 4;

    /** Value is of class java.lang.String */
    public final static int NAME_VALUE_PAIRS_INDEX = 5;

    /** Number of fields provided by {@link LogQuery#queryServerLog} */
    public final static int NUM_FIELDS = MESSAGE_INDEX + 1;

    public final static String THREAD_ID_KEY = "_ThreadID";
    public final static String OBJECTNAME_KEY = "_ObjectName";
}
