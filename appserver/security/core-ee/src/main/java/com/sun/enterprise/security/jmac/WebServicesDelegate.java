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

package com.sun.enterprise.security.jmac;

import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.runtime.common.MessageSecurityBindingDescriptor;
import com.sun.enterprise.security.jmac.config.ConfigHelper;
import java.util.Map;
import javax.security.auth.message.MessageInfo;
import com.sun.enterprise.security.jauth.AuthParam;
import org.glassfish.api.invocation.ComponentInvocation;
import org.jvnet.hk2.annotations.Contract;

/**
 * A Delegate Interface for handling WebServices Specific Security and  JSR 196 Providers
 * This insulates the GF Web-Bundle from any WebServices Dependencies.
 * @author kumar.jayanti
 */
@Contract
public interface  WebServicesDelegate {
    /**
     * 
     * @param svcRef The ServiceReferenceDescriptor
     * @param properties The Properties Map passed to WebServices Code Via PipeCreator
     * @return The MessageSecurityBindingDescriptor
     */
    public MessageSecurityBindingDescriptor getBinding(ServiceReferenceDescriptor svcRef, Map properties);
    /**
     * remove the registration of the argument listener from the Pipe
     * @param listener
     */
    public void removeListener(ConfigHelper.AuthConfigRegistrationWrapper listener);
    /** 
     * @return the classname of the Default JSR 196 WebServices Security Provider (A.k.a Metro Security Provider)
     */
    public String  getDefaultWebServicesProvider();
    
    /**
     * @param messageInfo  The MessageInfo
     * @return the AuthContextID computed from the argument MessageInfo
     */
    public String getAuthContextID(MessageInfo messageInfo);
    
    /**
     * @param messageInfo TheMessageInfo
     * @return a new instance of SOAPAuthParam
     */
    public AuthParam newSOAPAuthParam(MessageInfo messageInfo);
    
    /**
     * return the SOAP Message from the invocation, to be used by JACC PolicyContextHandler
     * @param inv the invocation
     * @return the SOAP Message
     */
    public Object getSOAPMessage(ComponentInvocation inv);
}
