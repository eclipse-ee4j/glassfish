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

package org.glassfish.appclient.server.core.jws.servedcontent;

import java.io.IOException;
import java.net.URI;

/**
 *
 * @author tjquinn
 */
public interface Content {

    public enum State {
        AVAILABLE,
        UNAVAILABLE,
        SUSPENDED
    }

    public State state();

    public boolean isAvailable(final URI requestURI) throws IOException;

    public void suspend();

    public void resume();

    public void start();

    public void stop();

    public class Adapter implements Content {

        private State state = State.AVAILABLE;

        public State state() {
            return state;
        }

        public boolean isAvailable(final URI requestURI) throws IOException {
            return state == State.AVAILABLE;
        }

        public void suspend() {
            state = State.SUSPENDED;
        }

        public void resume() {
            state = State.AVAILABLE;
        }

        public void start() {
            state = State.AVAILABLE;
        }

        public void stop() {
            state = State.UNAVAILABLE;
        }
    }
}
