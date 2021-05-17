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

/**
 * Since this service does NOT have a constructor marked
 * with {@link Inject} and does NOT have a zero-arg constructor
 * CDI will NOT consider this to be one of its own beans
 *
 * @author jwells
 *
 */
public class HK2ServiceImpl implements HK2Service {
    private final int jobValue;

    /**
     * Doing THIS makes this NOT a CDI service!
     *
     * @param jobValue
     */
    public HK2ServiceImpl(int jobValue) {
        this.jobValue = jobValue;
    }

    @Override
    public int doAJob() {
        return jobValue;
    }

}
