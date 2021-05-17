/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment;


import org.glassfish.deployment.common.Descriptor;

public class LicenseDescriptor extends Descriptor {

    private Boolean licenseRequired;

    /** get value for licenseRequired
    */
    public Boolean getLicenseRequired() {
        return licenseRequired;
    }

    /** get value for licenseRequired
    */
    public String getLicenseRequiredValue() {
        if (licenseRequired.booleanValue() == true)
            return "true";
        else return "false";
    }

    /** set value for licenseRequired
    */
    public void setLicenseRequired(boolean licenseRequired) {
        if(licenseRequired==true)
            this.licenseRequired =Boolean.TRUE;
        else
            this.licenseRequired =Boolean.FALSE;
    }

    /** set value for licenseRequired
    */
    public void setLicenseRequired(String licenseRequired) {
        if(licenseRequired.equals("true"))
            this.licenseRequired =Boolean.TRUE;
        else if(licenseRequired.equals("false"))
            this.licenseRequired =Boolean.FALSE;
    }
}
