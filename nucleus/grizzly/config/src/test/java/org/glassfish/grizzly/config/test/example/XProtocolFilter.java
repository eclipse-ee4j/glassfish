/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.grizzly.config.test.example;

import java.io.IOException;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.EmptyCompletionHandler;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.memory.MemoryManager;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 *
 * @author oleksiys
 */
public class XProtocolFilter extends BaseFilter {
    @Override
    public NextAction handleRead(final FilterChainContext ctx) throws IOException {
        final Connection<?> connection = ctx.getConnection();
        final MemoryManager<?> memoryManager = connection.getTransport().getMemoryManager();
        ctx.write(Buffers.wrap(memoryManager, "X-Protocol-Response", ISO_8859_1));

        ctx.flush(new EmptyCompletionHandler<>() {

            @Override
            public void completed(Object result) {
                connection.closeSilently();
            }

        });
        return ctx.getStopAction();
    }
}
