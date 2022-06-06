/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.jboss.weld.tck.glassfish;

import com.sun.enterprise.container.common.impl.util.JavaEEIOUtilsImpl;

import java.io.IOException;

import org.jboss.cdi.tck.spi.Beans;

/**
 * @author David Matejcek
 */
public class GlassFishBeansImpl implements Beans {

    @Override
    public boolean isProxy(Object instance) {
        return instance.getClass().getName().indexOf("_$$_Weld") > 0;
    }

    @Override
    public byte[] passivate(Object instance) throws IOException {
        JavaEEIOUtilsImpl utils = new JavaEEIOUtilsImpl();
        return utils.serializeObject(instance, true);
    }

    @Override
    public Object activate(byte[] bytes) throws IOException, ClassNotFoundException {
        JavaEEIOUtilsImpl utils = new JavaEEIOUtilsImpl();
        try {
            return utils.deserializeObject(bytes, true, Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize the object.", e);
        }
    }
}
