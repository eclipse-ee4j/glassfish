/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.test.authpropagation;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import javax.ejb.*;
import java.util.logging.Logger;

@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class SchedulerService {
    private static final Logger logger = Logger.getLogger(SchedulerService.class.getName());

    @Resource
    private TimerService timerService;

    @Resource
    private SessionContext context;

    @PostConstruct
    public void postConstruct() {
        TimerConfig timerConfig = new TimerConfig();
        // ...
        timerService.createIntervalTimer(0, 1000, timerConfig);
    }

    @Timeout
    public void handleTimeout(Timer timer) {
        logger.info("handleTimeout()[" + context.getCallerPrincipal() + "]:" + Thread.currentThread().getName());
    }
}
