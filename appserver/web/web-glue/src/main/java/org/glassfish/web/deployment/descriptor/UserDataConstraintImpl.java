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

package org.glassfish.web.deployment.descriptor;

import com.sun.enterprise.deployment.web.UserDataConstraint;
import com.sun.enterprise.util.LocalStringManagerImpl;

import org.glassfish.deployment.common.Descriptor;


/**
 * I represent the information about how the web application's data should be protected.
 *
 * @author Danny Coward
 */
public class UserDataConstraintImpl extends Descriptor implements UserDataConstraint {

    /** The transport is unspecified.*/
    public static final String TRANSPORT_GUARANTEE_NONE = UserDataConstraint.NONE_TRANSPORT;
    /** HTTP.*/
    public static final String TRANSPORT_GUARANTEE_INTEGRAL = UserDataConstraint.INTEGRAL_TRANSPORT;
    /** HTTPS */
    public static final String TRANSPORT_GUARANTEE_CONFIDENTIAL = UserDataConstraint.CONFIDENTIAL_TRANSPORT;

    /** JACC Specific **/
    public static final String TRANSPORT_GUARANTEE_CLEAR = UserDataConstraint.CLEAR;
    private String transportGuarantee;
    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(UserDataConstraintImpl.class);

    /**
     * Return my transport type.
     */
    public String getTransportGuarantee() {
        if (transportGuarantee == null) {
            transportGuarantee = TRANSPORT_GUARANTEE_NONE;
        }
        return transportGuarantee;
    }

    public String[] getUnacceptableTransportGuarantees(){
        String acceptable = getTransportGuarantee();
        if(acceptable.equals(TRANSPORT_GUARANTEE_NONE))
            return (String[]) null;
        else if (acceptable.equals(TRANSPORT_GUARANTEE_INTEGRAL)){
            String[] ret = new String[] {TRANSPORT_GUARANTEE_CLEAR,  TRANSPORT_GUARANTEE_CONFIDENTIAL };
            return ret;
        } else if (acceptable.equals(TRANSPORT_GUARANTEE_CONFIDENTIAL)){
            String[] ret = new String[] {TRANSPORT_GUARANTEE_CLEAR,  TRANSPORT_GUARANTEE_INTEGRAL };
            return ret;
        }
        return (String[]) null;
    }
    /**
     * Sets my transport type to the given value. Throws an illegal argument exception
     * if the value is not allowed.
     */
    public void setTransportGuarantee(String transportGuarantee) {
        if (this.isBoundsChecking()) {
            if ( !UserDataConstraint.NONE_TRANSPORT.equals(transportGuarantee)
                && !UserDataConstraint.INTEGRAL_TRANSPORT.equals(transportGuarantee)
                && !UserDataConstraint.CONFIDENTIAL_TRANSPORT.equals(transportGuarantee)) {
                throw new IllegalArgumentException(localStrings.getLocalString(
                    "web.deployment.exceptiontransportguarentee",
                    "{0} is not a valid transport guarantee", new Object[] {transportGuarantee}));
            }
        }
        this.transportGuarantee = transportGuarantee;
    }

    /**
     * Returns a formatted String of my state.
     */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("UserDataConstraint ");
        toStringBuffer.append(" description ").append(super.getDescription());
        toStringBuffer.append(" transportGuarantee ").append(getTransportGuarantee());
    }
}
