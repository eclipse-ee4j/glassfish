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

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;

@Singleton
public class CreateTimersBean implements TimedObject {
    private static final Logger logger = Logger.getLogger(CreateTimersBean.class.getName());

    @Resource
    private TimerService timerService;

    public Timer createTimer(long milliSeconds, Serializable info) {
        return timerService.createTimer(milliSeconds, info);
    }

    public void ejbTimeout(Timer timer) {
        logger.log(Level.INFO, "in ejbTimeout, timer: {0}, info: {1}",
                new Object[]{timer, timer.getInfo()});
    }
}
