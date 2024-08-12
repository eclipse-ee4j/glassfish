/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices;

import com.sun.xml.ws.transport.http.servlet.ServletAdapter;

import org.glassfish.api.invocation.ComponentInvocation;

/**
 * This class stores information for EjbRuntimeInfo
 * It stores the invocation object and the servlet Adapter
 *
 * @author Bhakti Mehta
 */
public class AdapterInvocationInfo {

    /**
     * This will store information about the inv which needs to
     * be started and ended  by the StatelessSessionContainer
     */
    private ComponentInvocation inv;

    /**
     * This will store information about the ServletAdapter which
     * wil be used to publish the wsdl
     */
    private  ServletAdapter adapter;

    public void setAdapter(ServletAdapter adapter) {
        this.adapter = adapter;
    }

    public void setInv(ComponentInvocation inv) {
        this.inv = inv;
    }

    public ServletAdapter getAdapter() {
        return adapter;
    }

    public ComponentInvocation getInv() {
        return inv;
    }

}
