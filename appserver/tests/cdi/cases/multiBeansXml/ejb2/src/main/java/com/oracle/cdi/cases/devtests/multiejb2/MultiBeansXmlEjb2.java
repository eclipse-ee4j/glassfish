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

package com.oracle.cdi.cases.devtests.multiejb2;

import java.util.List;

/**
 * @author jwells
 *
 */
public interface MultiBeansXmlEjb2 {
    public final static String INTERCEPTOR2 = "Interceptor2";
    public final static String CALL_ME2 = "CallMe2";

    /**
     * This method should have all interceptors listed in the return list
     *
     * @param callerList An empty, non-null list
     * @return The list of all the classes that intercepted (and the final impl class) this call
     */
    public List<String> callMe(List<String> callerList);

}
