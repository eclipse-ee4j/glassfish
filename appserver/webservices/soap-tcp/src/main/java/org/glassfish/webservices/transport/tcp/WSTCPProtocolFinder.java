/*
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

package org.glassfish.webservices.transport.tcp;

import com.sun.xml.ws.transport.tcp.util.TCPConstants;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.portunif.PUContext;
import org.glassfish.grizzly.portunif.ProtocolFinder;

/**
 *
 * @author Alexey Stashok
 */
public class WSTCPProtocolFinder implements ProtocolFinder {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final static byte[] PROTOCOL_SCHEMA_BYTES;

    static {
        byte[] bytes;
        try {
            bytes = TCPConstants.PROTOCOL_SCHEMA.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.WARNING, LogUtils.CANNOT_CONVERT_PROTOCOL_ID, e);
            bytes = TCPConstants.PROTOCOL_SCHEMA.getBytes();
        }

        PROTOCOL_SCHEMA_BYTES = bytes;
    }

    @Override
    public Result find(final PUContext puContext,
            final FilterChainContext filterChainContext) {

        final Buffer buffer = filterChainContext.getMessage();
        if (buffer.remaining() < PROTOCOL_SCHEMA_BYTES.length) {
            return Result.NEED_MORE_DATA;
        }

        final int pos = buffer.position();
        for (int i = 0; i < PROTOCOL_SCHEMA_BYTES.length; i++) {
            if (buffer.get(pos + i) != PROTOCOL_SCHEMA_BYTES[i]) {
                return Result.NOT_FOUND;
            }
        }

        return Result.FOUND;
    }
}
