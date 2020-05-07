/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.containers;


import java.util.Date;
import java.util.TimerTask;


/*
 * JDK timer task for timer expirations.  
 *
 * @author Kenneth Saks
 */
public class EJBTimerTask extends TimerTask {
    
    private Date timeout_;
    private TimerPrimaryKey timerId_;
    private EJBTimerService timerService_;

    EJBTimerTask(Date timeout, TimerPrimaryKey timerId, 
                 EJBTimerService timerService)
    { 
        timeout_ = timeout;
        timerId_ = timerId;
        timerService_ = timerService;
    }
    
    public void run() {
        // Delegate to Timer Service.
        timerService_.taskExpired(timerId_);
    }
    
    public Date getTimeout() {
        return timeout_;
    }

} 

