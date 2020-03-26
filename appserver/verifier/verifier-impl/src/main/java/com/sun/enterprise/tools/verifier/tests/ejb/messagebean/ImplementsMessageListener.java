/*
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

package com.sun.enterprise.tools.verifier.tests.ejb.messagebean;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;


/**
 * Verify that message beans implement the MessageListener interface
 *
 * @author  Jerome Dochez
 * @version 
 */
public class ImplementsMessageListener extends MessageBeanTest {

    /** 
     * Run a verifier test against an individual declared message
     * drive bean component
     * 
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbMessageBeanDescriptor descriptor) {
        
        Result result = getInitializedResult();
ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        Class mbc = loadMessageBeanClass(descriptor, result);
        if (descriptor.getEjbBundleDescriptor().getSpecVersion().equals("2.0"))
            testImplementationOf(mbc, jakarta.jms.MessageListener.class.getName(), result);
        else
            testImplementationOf(mbc, descriptor.getMessageListenerType(), result);
        return result;
    }
}
