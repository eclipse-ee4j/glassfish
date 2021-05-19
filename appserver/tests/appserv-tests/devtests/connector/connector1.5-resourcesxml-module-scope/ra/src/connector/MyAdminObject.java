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

    private String resetControl="NORESET";
    private Integer expectedResults;

    public void setResetControl (String value) {
        resetControl = value;
    }

    public String getResetControl () {
        return resetControl;
    }

    public void setExpectedResults (Integer value) {
        expectedResults = value;
    }

    public Integer getExpectedResults () {
        return expectedResults;
    }

    public void initialize() {
        System.out.println("[MyAdminObject] Initializing the Controls to false:"+resetControl);
        if (resetControl.equals("BEGINNING")) {
            synchronized (Controls.readyLock){
                Controls.done=false;
            }
           System.out.println("[MyAdminObject] Initialized the Controls to false");
        }
    }

    public boolean done() {
        synchronized (Controls.readyLock){
         return Controls.done;
        }
    }

    public int expectedResults(){
        synchronized (Controls.readyLock){

        return Controls.expectedResults;
        }
    }

    public Object getLockObject(){
        return Controls.readyLock;
    }

}

