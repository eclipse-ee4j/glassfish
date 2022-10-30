/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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


package com.sun.enterprise.naming.spi;

import org.glassfish.api.naming.SimpleJndiName;
import org.jvnet.hk2.annotations.Contract;

@Contract
public interface NamingUtils {

    <T> NamingObjectFactory createSimpleNamingObjectFactory(SimpleJndiName name, T value);

    NamingObjectFactory createLazyInitializationNamingObjectFactory(SimpleJndiName name, SimpleJndiName jndiName, boolean cacheResult);

    NamingObjectFactory createLazyNamingObjectFactory(SimpleJndiName name, SimpleJndiName jndiName, boolean cacheResult);

    NamingObjectFactory createCloningNamingObjectFactory(SimpleJndiName name, NamingObjectFactory delegate);

    <T> T makeCopyOfObject(T obj);
}
