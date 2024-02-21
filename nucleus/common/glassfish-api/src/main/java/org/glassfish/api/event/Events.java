/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.api.event;

import org.glassfish.api.event.EventListener.Event;
import org.jvnet.hk2.annotations.Contract;

/**
 * Contract to register/unregister events listener.
 *
 * <p>This implementation is not meant to be used for performance sensitive message delivery.
 *
 * @author Jerome Dochez
 */
@Contract
public interface Events {

    /**
     * Registers a new listener for global events.
     *
     * @param listener the new listener
     */
    void register(EventListener listener);

    /**
     * Unregisters a listener.
     *
     * @param listener the register to remove
     * @return {@code true} if the removal was successful
     */
    boolean unregister(EventListener listener);

    /**
     * Sends an event asynchronously.
     *
     * @param event event to send
     */
    void send(Event<?> event);

    /**
     * Sends an event to all listener synchronously or asynchronously.
     *
     * @param event event to send
     * @param asynchronously {@code true} if the event should be sent asynchronously
     */
    void send(Event<?> event, boolean asynchronously);
}
