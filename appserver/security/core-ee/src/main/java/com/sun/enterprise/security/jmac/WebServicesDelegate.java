/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
import jakarta.security.auth.message.MessageInfo;
import java.util.Map;
import org.jvnet.hk2.annotations.Contract;

/**
 * A Delegate Interface for handling WebServices Specific Security and Jakarta Authentication config provider.
 *
 * <p>
 * This insulates the GlassFish Web-Bundle from any WebServices Dependencies.
 *
 * @author kumar.jayanti
 */
@Contract
public interface WebServicesDelegate {
    /**
     *
     * @param svcRef The ServiceReferenceDescriptor
     * @param properties The Properties Map passed to WebServices Code Via PipeCreator
     * @return The MessageSecurityBindingDescriptor
     */
    MessageSecurityBindingDescriptor getBinding(ServiceReferenceDescriptor svcRef, Map properties);

    /**
     * @return the classname of the Default Jakarta Authentication WebServices Security Provider (A.k.a Metro Security Provider)
     */
    String getDefaultWebServicesProvider();

    /**
     * @param messageInfo The MessageInfo
     * @return the AuthContextID computed from the argument MessageInfo
     */
    String getAuthContextID(MessageInfo messageInfo);

}
