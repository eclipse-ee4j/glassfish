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

import jakarta.resource.spi.ConfigProperty;

//@README : test to make sure that administed objects defined in ra.xml (no @AdministeredObject annotation)
// are considered for @ConfigProperty annotation

public class MyAdminObject_1 implements java.io.Serializable {

    @ConfigProperty(
            defaultValue = "NORESET",
            type = java.lang.String.class
    )
    //@README : we are setting default value to NORESET only in annotation.
    //getter (getResetControl) will throw exception if it is not NORESET
    private String resetControl;
    private Integer expectedResults;

    public void setResetControl (String value) {
        resetControl = value;
    }

    public String getResetControl () {
        if(resetControl == null || !resetControl.equals("NORESET")){
            throw new RuntimeException("reset control not initialized, should have been initialized via annotation");
        }
        return resetControl;
    }

    @ConfigProperty(
            type = java.lang.Integer.class,
            defaultValue = "88"
    )
    //@README : we are setting default value to 88 only in annotation.
    //getter (getExpectedResults) will throw exception if it is not 88
    public void setExpectedResults (Integer value) {
        expectedResults = value;
    }

    public Integer getExpectedResults () {
        if(expectedResults != 88){
            throw new RuntimeException("expected results not initialized, should have been initialized via annotation");
        }
        return expectedResults;
    }
}
