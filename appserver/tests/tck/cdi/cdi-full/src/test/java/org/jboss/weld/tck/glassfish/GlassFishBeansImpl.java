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

import java.io.IOException;
import java.util.Arrays;

import org.jboss.cdi.tck.spi.Beans;

/**
 * CDI TCK tests use this class as an adapter between the test application and server container.
 * Then it's implementation can simplify the behavior, ie. explicit passivation, while
 * in a real application the decision to passivate/activate some object is on the container
 * and cannot be requested by the application.
 * <p>
 * Until GlassFish provides standalone utility to do that, we have to fake
 * the passivation/activation.
 *
 * @author David Matejcek
 */
public class GlassFishBeansImpl implements Beans {

    private Object fakeSerialized;

    @Override
    public boolean isProxy(Object instance) {
        return instance.getClass().getName().indexOf("_$$_Weld") > 0;
    }


    @Override
    public byte[] passivate(Object instance) throws IOException {
        fakeSerialized = instance;
        return instance.toString().getBytes();
    }


    @Override
    public Object activate(byte[] bytes) throws IOException, ClassNotFoundException {
        if (Arrays.equals(fakeSerialized.toString().getBytes(), bytes)) {
            Object result = fakeSerialized;
            fakeSerialized = null;
            return result;
        }
        return null;
    }
}
