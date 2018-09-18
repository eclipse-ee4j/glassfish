/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config;

import org.glassfish.grizzly.IOEvent;
import org.glassfish.grizzly.nio.DefaultSelectionKeyHandler;
import org.glassfish.grizzly.nio.NIOConnection;
import org.glassfish.grizzly.nio.SelectionKeyHandler;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class DummySelectionKeyHandler implements SelectionKeyHandler {

    private final SelectionKeyHandler delegate = new DefaultSelectionKeyHandler();

    @Override
    public void onKeyRegistered(SelectionKey key) {
        delegate.onKeyRegistered(key);
    }

    @Override
    public void onKeyDeregistered(SelectionKey key) {
        delegate.onKeyDeregistered(key);
    }

    @Override
    public boolean onProcessInterest(SelectionKey key, int interest) throws IOException {
        return delegate.onProcessInterest(key, interest);
    }

    @Override
    public void cancel(SelectionKey key) throws IOException {
        delegate.cancel(key);
    }

    @Override
    public NIOConnection getConnectionForKey(SelectionKey selectionKey) {
        return delegate.getConnectionForKey(selectionKey);
    }

    @Override
    public void setConnectionForKey(NIOConnection connection, SelectionKey selectionKey) {
        delegate.setConnectionForKey(connection, selectionKey);
    }

    @Override
    public int ioEvent2SelectionKeyInterest(IOEvent ioEvent) {
        return delegate.ioEvent2SelectionKeyInterest(ioEvent);
    }

    @Override
    public IOEvent selectionKeyInterest2IoEvent(int selectionKeyInterest) {
        return delegate.selectionKeyInterest2IoEvent(selectionKeyInterest);
    }

    @Override
    public IOEvent[] getIOEvents(int interest) {
        return delegate.getIOEvents(interest);
    }
}
