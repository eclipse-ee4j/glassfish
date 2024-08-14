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

package com.sun.enterprise.web.deploy;

import com.sun.enterprise.deployment.EjbReferenceDescriptor;

import org.apache.catalina.deploy.ContextEjb;

/**
 * Decorator of class <code>org.apache.catalina.deploy.ContextEjb</code>
 *
 * @author Jean-Francois Arcand
 */
public class ContextEjbDecorator extends ContextEjb{

    private EjbReferenceDescriptor decoree;

    public ContextEjbDecorator(EjbReferenceDescriptor decoree){
        this.decoree = decoree;
    }

    // ------------------------------------------------------------- Properties


    public String getDescription() {
        return decoree.getDescription();
    }


    public String getHome() {
        return decoree.getEjbHomeInterface();
    }


    public String getLink() {
        return decoree.getLinkName();
    }


    public String getLocal() {
        return decoree.getEjbInterface();
    }


    public String getName() {
        return decoree.getName();
    }


    public String getType() {
        return decoree.getType();
    }



}
