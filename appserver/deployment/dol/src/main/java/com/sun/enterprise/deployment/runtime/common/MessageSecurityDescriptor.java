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

package com.sun.enterprise.deployment.runtime.common;

import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;

import java.util.ArrayList;

public class MessageSecurityDescriptor extends RuntimeDescriptor {
    public static final String MESSAGE = "Message";
    public static final String REQUEST_PROTECTION = "RequestProtection";
    public static final String RESPONSE_PROTECTION = "ResponseProtection";

    private ArrayList messageDescs = new ArrayList();
    private ProtectionDescriptor requestProtectionDesc = null;
    private ProtectionDescriptor responseProtectionDesc = null;

    public MessageSecurityDescriptor() {}

    public void addMessageDescriptor(MessageDescriptor messageDesc) {
       messageDescs.add(messageDesc);
    }

    public ArrayList getMessageDescriptors() {
        return messageDescs;
    }

    public ProtectionDescriptor getRequestProtectionDescriptor() {
        return requestProtectionDesc;
    }

    public void setRequestProtectionDescriptor(ProtectionDescriptor proDesc) {
        requestProtectionDesc = proDesc;
    }

    public ProtectionDescriptor getResponseProtectionDescriptor() {
        return responseProtectionDesc;
    }

    public void setResponseProtectionDescriptor(ProtectionDescriptor proDesc) {
        responseProtectionDesc = proDesc;
    }

    // return all the methods defined in the message elements inside this
    // message-security element
    public ArrayList getAllMessageMethods() {
        //FIXME
        // need to convert operation to message
        // need to union all the methods
        return new ArrayList();
    }

}
