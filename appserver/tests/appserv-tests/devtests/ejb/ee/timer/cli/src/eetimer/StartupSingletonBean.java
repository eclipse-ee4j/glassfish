/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package eetimer;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup()
public class StartupSingletonBean {
    public static final long SECONDS_TO_EXPIRE = 120;
    public static final String TIMER_INFO = "timer created by StartupSingletonBean";

    @EJB
    private CreateTimersBean createTimerBean;

    @PostConstruct
    private void postConstruct() {
        createTimerBean.createTimer(SECONDS_TO_EXPIRE * 1000, TIMER_INFO);
    }

}
