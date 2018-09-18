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

package connector;

public class MyAdminObject implements java.io.Serializable {

    Controls control = Controls.getControls();

    public void setup() {
        control.setupInitialWorks();
    }

    public void submit() {
        control.startTestWorks();
    }

    public void triggerWork() {
        control.trigger();
    }

    public void checkResult () {
        control.checkResult();
    }

    public void setNumberOfSetupWorks(int count) {
        control.setNumberOfSetupWorks(count);
    }
                                 
    public int getNumberOfSetupWorks() {
        return control.getNumberOfSetupWorks();
    }
     
    public void setNumberOfActualWorks(int num) {
        control.setNumberOfActualWorks(num);
    }
     
    public int getNumberOfActualWorks() {
        return control.getNumberOfActualWorks();
    }

}

