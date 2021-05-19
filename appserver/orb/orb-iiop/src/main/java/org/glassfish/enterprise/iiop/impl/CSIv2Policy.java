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

package org.glassfish.enterprise.iiop.impl;


import com.sun.enterprise.deployment.EjbDescriptor;

//TODO: This class is in an Impl dir, we probably need to create a
//Contract in the orb-connector module that can be used inside security/ejb.security
public class CSIv2Policy extends org.omg.CORBA.LocalObject implements org.omg.CORBA.Policy {

    private EjbDescriptor ejbDescriptor;

    public CSIv2Policy(EjbDescriptor ejbDescriptor) {
        this.ejbDescriptor = ejbDescriptor;
    }


    @Override
    public int policy_type() {
        return POARemoteReferenceFactory.CSIv2_POLICY_TYPE;
    }


    @Override
    public org.omg.CORBA.Policy copy() {
        return new CSIv2Policy(ejbDescriptor);
    }


    @Override
    public void destroy() {
    }


    public EjbDescriptor getEjbDescriptor() {
        return ejbDescriptor;
    }
}
