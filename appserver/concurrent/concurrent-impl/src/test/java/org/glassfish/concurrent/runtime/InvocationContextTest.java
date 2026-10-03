/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.concurrent.runtime;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.WebBundleDescriptor;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.partialMockBuilder;
import static org.easymock.EasyMock.replay;
import static org.glassfish.concurrent.runtime.InvocationContext.toRegistrationName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InvocationContextTest {

    /**
     * The EE application name of a versioned application doesn't contain the version identifier,
     * see issue #24080.
     */
    @Test
    public void registrationNameOfVersionedApplication() {
        Application application = createMock(Application.class);
        expect(application.getRegistrationName()).andStubReturn("myapp:1.0");
        replay(application);
        WebBundleDescriptor webBundle = partialMockBuilder(WebBundleDescriptor.class).withConstructor().createMock();
        webBundle.setApplication(application);

        ComponentInvocation invocation = createInvocation("myapp");
        invocation.setJNDIEnvironment(webBundle);

        assertEquals("myapp:1.0", toRegistrationName(invocation));
    }


    @Test
    public void registrationNameOfUnsupportedJndiEnvironment() {
        JndiNameEnvironment environment = createMock(JndiNameEnvironment.class);
        replay(environment);

        ComponentInvocation invocation = createInvocation("myapp");
        invocation.setJNDIEnvironment(environment);

        assertEquals("myapp", toRegistrationName(invocation));
    }


    @Test
    public void registrationNameWithoutJndiEnvironment() {
        assertEquals("myapp", toRegistrationName(createInvocation("myapp")));
    }


    @Test
    public void registrationNameWithoutInvocation() {
        assertNull(toRegistrationName(null));
    }


    private static ComponentInvocation createInvocation(String appName) {
        return new ComponentInvocation("component", ComponentInvocationType.SERVLET_INVOCATION, null, appName,
            "module");
    }
}
