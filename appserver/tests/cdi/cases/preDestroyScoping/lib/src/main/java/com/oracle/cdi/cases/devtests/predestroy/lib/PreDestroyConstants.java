/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.cdi.cases.devtests.predestroy.lib;

/**
 * @author jwells
 *
 */
public class PreDestroyConstants {
    public static final String CREATED = "created ";
    public static final String PRODUCER_PRE_DESTROY_IN = "calling request bean from preDestroy";
    public static final String EXPECTED_EXCEPTION = "request bean properly threw ContextNotActiveException";
    public static final String IN_REQUEST_METHOD = "beanMethod called";

}
