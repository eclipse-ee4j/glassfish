/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.hk2.devtest.cdi.ejb1.scoped;

import org.jvnet.hk2.annotations.Contract;

/**
 * This service is created by hk2 and the implemetation
 * will be in a customized scope
 *
 * @author jwells
 *
 */
@Contract
public interface HK2Service {
    public static final int RETURN_VALUE = 381;

    /**
     * This method should always return RETURN_VALUE
     *
     * @return This method will always return RETURN_VALUE
     */
    public int doAJob();

}
