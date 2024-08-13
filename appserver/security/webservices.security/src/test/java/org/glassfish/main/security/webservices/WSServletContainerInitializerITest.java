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

package org.glassfish.main.security.webservices;

import com.sun.xml.ws.transport.http.servlet.WSServletContainerInitializer;

import jakarta.servlet.ServletContainerInitializer;

import java.util.ServiceLoader;
import java.util.Set;

import org.glassfish.soteria.servlet.SamRegistrationInstaller;
import org.glassfish.sse.impl.ServerSentEventServletContainerInitializer;
import org.glassfish.wasp.runtime.TldScanner;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * @author David Matejcek
 */
public class WSServletContainerInitializerITest {

    @Test
    public void serviceLoader_vs_ServletContainerInitializer() {
        ServiceLoader<ServletContainerInitializer> loader = ServiceLoader.load(ServletContainerInitializer.class);
        Set<ServletContainerInitializer> initializers = loader.stream().map(p -> p.get()).collect(toSet());
        assertThat(initializers, containsInAnyOrder(
            instanceOf(WSServletContainerInitializer.class),
            instanceOf(ServerSentEventServletContainerInitializer.class),
            instanceOf(TldScanner.class),
            instanceOf(SamRegistrationInstaller.class)
        ));
    }
}
