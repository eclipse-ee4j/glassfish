/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.container.common.spi;

import com.sun.enterprise.container.common.spi.util.InterceptorInfo;

import org.jvnet.hk2.annotations.Contract;

/**
 * A factory for creating an interceptor builder.
 *
 * <p>
 * An interceptor builder abstracts out the resources needed to create a
 * proxy for invoking a target object with interceptors. There is typically one instance of an interceptor builder per
 * target class ,and one interceptor invoker per target class instance.
 */

@Contract
public interface JavaEEInterceptorBuilderFactory {

    JavaEEInterceptorBuilder createBuilder(InterceptorInfo info) throws Exception;

    /**
     * Tests if a given object is a client proxy associated with an interceptor invoker.
     */
    boolean isClientProxy(Object obj);

}
