/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2021 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.utils.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import javax.security.auth.Subject;

import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.security.common.UserNameAndPassword;

import static org.glassfish.hk2.utilities.BuilderHelper.createConstantDescriptor;

/**
 * Generates some objects useful for tests, but not simply available in the test context.
 *
 * @author David Matejcek
 */
public class MockGenerator {

    private static final InvocationHandler MOCK_HANDLER = (proxy, method, args) -> {
        throw new UnsupportedOperationException("Feature-free dummy implementation for injection only");
    };


    /**
     * Creates a new {@link Subject} instance holding two principals:
     * <ul>
     * <li>asadmin
     * <li>_InternalSystemAdministrator_
     * </ul>
     *
     * @return new {@link Subject} instance
     */
    public Subject createAsadminSubject() {
        final Subject subject = new Subject();
        subject.getPrincipals().add(new UserNameAndPassword("asadmin"));
        subject.getPrincipals().add(new UserNameAndPassword("_InternalSystemAdministrator_"));
        return subject;
    }


    /**
     * Creates a mock implementation for the given interface and a descriptor for it.
     * Every method of the mock will throw {@link UnsupportedOperationException}, but it can be
     * useful in cases where we need a mandatory dependency which is unused in real time.
     * The descriptor has maximal ranking and is reified.
     *
     * @param <T> Requested API of the generated class.
     * @param iface
     * @return generated descriptor of the proxy.
     */
    public <T> AbstractActiveDescriptor<T> createMockDescriptor(final Class<T> iface) {
        final T mock = createMockThrowingExceptions(iface);
        final AbstractActiveDescriptor<T> descriptor = createConstantDescriptor(mock, null, iface);
        // high ranking to override detected HK2 service
        descriptor.setRanking(Integer.MAX_VALUE);
        descriptor.setReified(true);
        return descriptor;
    }


    /**
     * Creates a mock implementation for the given interface.
     * Every method of the mock will throw {@link UnsupportedOperationException}
     *
     * @param <T>
     * @param clazz
     * @return the implementation
     */
    @SuppressWarnings("unchecked")
    public <T> T createMockThrowingExceptions(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz}, MOCK_HANDLER);
    }
}
