/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.devtests.web.portunif;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.portunif.PUContext;
import org.glassfish.grizzly.portunif.ProtocolFinder;


public class DummyProtocolFinder implements ProtocolFinder {
    private final static String name = "dummy-protocol";
    private byte[] signature = name.getBytes();

    public Result find(PUContext puc, FilterChainContext fcc) {
        final Buffer buffer = fcc.getMessage();
        if (buffer.remaining() >= signature.length) {
            final int start = buffer.position();

            for (int i = 0; i < signature.length; i++) {
                if (buffer.get(start + i) != signature[i]) {
                    return Result.NOT_FOUND;
                }
            }

            return Result.FOUND;
        }

        return Result.NEED_MORE_DATA;
    }
}
