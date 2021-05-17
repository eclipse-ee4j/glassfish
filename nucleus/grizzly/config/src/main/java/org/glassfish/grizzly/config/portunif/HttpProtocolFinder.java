/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config.portunif;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.portunif.PUContext;
import org.glassfish.grizzly.portunif.ProtocolFinder;

/**
 * A {@link ProtocolFinder} implementation that parse the available
 * SocketChannel bytes looking for the 'http' bytes. An http request will
 * always has the form of:
 *
 * METHOD URI PROTOCOL/VERSION
 *
 * example: GET / HTTP/1.1
 *
 * The algorithm will try to find the protocol token.
 *
 * @author Jeanfrancois Arcand
 * @author Alexey Stashok
 */
public class HttpProtocolFinder implements ProtocolFinder {
    private static final char[] METHOD_FIRST_LETTERS = new char[] {'G', 'P', 'O', 'H', 'D', 'T', 'C'};
    private final Attribute<ParsingState> parsingStateAttribute =
            Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute(
                    HttpProtocolFinder.class + "-" + hashCode()
                            + ".parsingStateAttribute");

    private final int maxRequestLineSize;

    public HttpProtocolFinder() {
        this(2048);
    }

    public HttpProtocolFinder(int maxRequestLineSize) {
        this.maxRequestLineSize = maxRequestLineSize;
    }

    @Override
    public Result find(final PUContext puContext, final FilterChainContext ctx) {
        final Connection connection = ctx.getConnection();
        final Buffer buffer = ctx.getMessage();

        final ParsingState parsingState = parsingStateAttribute.get(connection);

        final int limit = buffer.limit();

        int position;
        int state;

        if (parsingState == null) {
            position = buffer.position();
            state = 0;
        } else {
            position = parsingState.position;
            state = parsingState.state;
        }

        byte c = 0;
        byte c2;

        // Rule b - try to determine the context-root
        while (position < limit) {
            c2 = c;
            c = buffer.get(position++);
            // State Machine
            // 0 - Search for the first SPACE ' ' between the method and the
            //     the request URI
            // 1 - Search for the second SPACE ' ' between the request URI
            //     and the method
            _1:
            switch (state) {
                case 0:
                    // Check method name
                    for (int i = 0; i < METHOD_FIRST_LETTERS.length; i++) {
                        if (c == METHOD_FIRST_LETTERS[i]) {
                            state = 1;
                            break _1;
                        }
                    }

                    return Result.NOT_FOUND;
                case 1:
                    // Search for first ' '
                    if (c == 0x20) {
                        state = 2;
                    }
                    break;
                case 2:
                    // Search for next ' '
                    if (c == 0x20) {
                        state = 3;
                    }
                    break;
                case 3:
                    // Check 'H' part of HTTP/
                    if (c == 'H') {
                        state = 4;
                        break;
                    }
                    return Result.NOT_FOUND;

                case 4:
                    // Search for P/ (part of HTTP/)
                    if (c == 0x2f && c2 == 'P') {
                        // find SSL preprocessor
                        if (parsingState != null) {
                            parsingStateAttribute.remove(connection);
                        }

                        return Result.FOUND;
                    }
                    break;
                default:
                    return Result.NOT_FOUND;
            }
        }

        if (position >= maxRequestLineSize) {
            return Result.NOT_FOUND;
        }

        if (parsingState == null) {
            parsingStateAttribute.set(connection, new ParsingState(position, state));
        } else {
            parsingState.position = position;
            parsingState.state = state;
        }

        return Result.NEED_MORE_DATA;
    }

    private static final class ParsingState {
        int position;
        int state;

        public ParsingState(int position, int state) {
            this.position = position;
            this.state = state;
        }
    }
}
