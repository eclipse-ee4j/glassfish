/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.mdb;

import com.sun.ejb.codegen.EjbOptionalIntfGenerator;

import jakarta.resource.spi.endpoint.MessageEndpoint;

public class MessageBeanInterfaceGenerator extends EjbOptionalIntfGenerator {

    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> generateMessageBeanSubClass(Class<?> beanClass, Class<T> messageBeanInterface) throws Exception {
        final String generatedMessageBeanSubClassName = messageBeanInterface.getName() + "__Bean__";

        generateSubclass(beanClass, generatedMessageBeanSubClassName, messageBeanInterface, MessageEndpoint.class);
        return (Class<? extends T>) loadClass(generatedMessageBeanSubClassName, beanClass);
    }

    public Class<?> generateMessageBeanInterface(Class<?> beanClass) throws Exception {
        final String generatedMessageBeanInterfaceName = getGeneratedMessageBeanInterfaceName(beanClass);

        generateInterface(beanClass, generatedMessageBeanInterfaceName, MessageEndpoint.class);

        return loadClass(generatedMessageBeanInterfaceName, beanClass);
    }

    public static String getGeneratedMessageBeanInterfaceName(Class<?> ejbClass) {
        String className = ejbClass.getName();
        int dot = className.lastIndexOf('.');
        final String packageName = (dot == -1) ? null : className.substring(0, dot);
        final String name = "__EJB32_Generated__" + ejbClass.getSimpleName() + "__Intf__";

        return packageName != null ? packageName + "." + name : name;
    }
}
