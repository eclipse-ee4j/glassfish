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

package com.sun.s1asdev.ejb31.timer.keepstate;

import jakarta.ejb.*;
import java.util.List;
import java.util.ArrayList;

@Singleton
public class KeepStateBean implements KeepStateIF {
    private List<String> infos = new ArrayList<String>();

    /**
     * keepstate is passed from build.xml to appclient as application args, and to this
     * business method. The test EAR app is deployed once first, then KeepStateIF.INFO
     * is modified, rebuild, and redeployed.  If redeployed with keepstate true, no new
     * auto timers are created, and existing timers are carried over.  So these timers
     * still have the old timer.getInfo(), which is different than the current KeepStateIF.INFO.
     * If redeployed with keepstate false, old timers are destroyed and new auto timers are
     * created, and their timer.getInfo() will equal to current KeepStateIF.INFO.
     */
    public String verifyTimers(boolean keepState) throws Exception {
        String result = "keepstate: " + keepState + ", current INFO: " + INFO + ", timer infos: " + infos;
        for(String s : infos) {
            if(keepState && s.equals(INFO)) {
                throw new Exception(result);
            }
            if(!keepState && !s.equals(INFO)) {
                throw new Exception(result);
            }
        }
        return result;
    }

    @Schedule(second="*", minute="*", hour="*", info=INFO)
    private void timeout(Timer t) {
        infos.add((String) t.getInfo());
        System.out.println("In timeout method for timer with info: " + t.getInfo());
    }
}
