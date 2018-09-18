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

package com.sun.jts.trace;

/**
 * The class contains the constants for different trace levels.
 *
 * @author <a href="mailto:kannan.srinivasan@sun.com">Kannan Srinivasan</a>
 * @version 1.0
 */
public class TraceLevel
{
        public static final int IAS_JTS_TRACE_TRIVIAL = 0;
        public static final int IAS_JTS_TRACE_RECOVERY = 1;
        public static final int IAS_JTS_TRACE_CONFIGURATION = 2;
        public static final int IAS_JTS_TRACE_TIMEOUT = 3;
        public static final int IAS_JTS_TRACE_TRANSACTION_HIGH_LEVEL = 4;
        public static final int IAS_JTS_TRACE_TRANSACTION = 5;
        public static final int IAS_JTS_TRACE_LOGGING = 6;
        public static final int IAS_JTS_MAX_TRACE_LEVEL = 6;

}
