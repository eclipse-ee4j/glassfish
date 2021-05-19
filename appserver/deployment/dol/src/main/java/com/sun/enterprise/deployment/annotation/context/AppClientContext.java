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

package com.sun.enterprise.deployment.annotation.context;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.types.HandlerChainContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * This provides a context for Application Client.
 *
 * @Author Shing Wai Chan
 */
public class AppClientContext extends ResourceContainerContextImpl {
    public AppClientContext(ApplicationClientDescriptor appClientDescriptor) {
        super(appClientDescriptor);
        componentClassName = appClientDescriptor.getMainClassName();
    }

    public ApplicationClientDescriptor getDescriptor() {
        return (ApplicationClientDescriptor)descriptor;
    }

    public HandlerChainContainer[]
            getHandlerChainContainers(boolean serviceSideHandlerChain, Class declaringClass) {
        if(serviceSideHandlerChain) {
            // We should not come here at all - anyway return null
            return null;
        } else {
            List<ServiceReferenceDescriptor> result = new ArrayList<ServiceReferenceDescriptor>();
            result.addAll(getDescriptor().getServiceReferenceDescriptors());
            return(result.toArray(new HandlerChainContainer[result.size()]));
        }
    }
}
