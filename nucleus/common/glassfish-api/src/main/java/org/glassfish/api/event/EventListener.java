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

package org.glassfish.api.event;

/**
 * Code that wish to listen to glassfish events should implement this interface and register it to the Events contract
 * implementation.
 *
 * User: Jerome Dochez
 */
public interface EventListener {

    void event(Event event);

    public class Event<T> {
        final long inception;
        final EventTypes<T> type;
        final T hook;

        public Event(EventTypes<T> type) {
            if (type.getHookType() != null) {
                throw new IllegalArgumentException("Null event hook [" + type.getHookType() + "]");
            }
            inception = System.currentTimeMillis();
            this.type = type;
            this.hook = null;
        }

        public Event(EventTypes<T> type, T hook) {
            inception = System.currentTimeMillis();
            this.type = type;
            this.hook = hook;
        }

        public long inception() {
            return inception;
        }

        public String name() {
            return type.type();
        }

        public boolean is(EventTypes type) {
            return type == this.type;
        }

        public T hook() {
            return type.getHookType().cast(hook);
        }

        public EventTypes<T> type() {
            return type;
        }
    }

}
